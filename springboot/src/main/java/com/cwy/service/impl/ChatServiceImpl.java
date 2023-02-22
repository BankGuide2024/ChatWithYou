package com.cwy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cwy.ai.Answer;
import com.cwy.ai.Choices;
import com.cwy.dao.ChatDao;
import com.cwy.entity.Chat;
import com.cwy.service.ChatService;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.rmi.ServerException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
	@Autowired
	private ChatDao chatDao;

	@Override
	public Chat getByFuzzyQueryInput(String input) {
		// 先看一下这个问题数据库中有没有，数据库如果有，那么直接从数据库里面拿出来
		LambdaQueryWrapper<Chat> lqw = new LambdaQueryWrapper<>();
		lqw.eq(Chat::getInput, input);
		Chat chatInMySQL = chatDao.selectOne(lqw);
		if(chatInMySQL != null){
			System.out.println(chatInMySQL);
			return chatInMySQL;
		}else {// 数据库没有，我再去调用chatgpt
			CloseableHttpClient httpClient = null;
			StringBuilder answer = new StringBuilder();
			try {
				httpClient = HttpClientBuilder.create().setSSLSocketFactory(getSslConnectionSocketFactory()).build();
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (KeyStoreException e) {
				throw new RuntimeException(e);
			} catch (KeyManagementException e) {
				throw new RuntimeException(e);
			}
			try {
				answer.append(submit(httpClient, getHttpPost(), input));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// 将ChatGPT的结果保存到数据库中
			Chat chatTemp = new Chat();
			chatTemp.setInput(input);
			chatTemp.setOutput(answer.toString());
			System.out.println(chatTemp);
			chatDao.insert(chatTemp);
			return chatTemp;
		}
	}

	private static String submit(CloseableHttpClient httpClient, HttpPost post, String input) throws IOException {
		StringEntity stringEntity = new StringEntity(getRequestJson(input), getContentType());
		post.setEntity(stringEntity);
		CloseableHttpResponse response = httpClient.execute(post);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			String responseJson = EntityUtils.toString(response.getEntity());
			Answer answer = JSON.parseObject(responseJson, Answer.class);
			StringBuilder answers = new StringBuilder();
			List<Choices> choices = answer.getChoices();
			for (Choices choice : choices) {
				answers.append(choice.getText());
			}
			return answers.substring(2, answers.length());
		} else if (response.getStatusLine().getStatusCode() == 429) {
			return "-- warning: Too Many Requests!";
		} else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			throw new ServerException("------ Server error, program terminated! ------");
		} else {
			return "-- warning: Error, please try again!";
		}
	}


	private static ContentType getContentType() {
		return ContentType.create("text/json", "UTF-8");
	}

	private static String getRequestJson(String question) {
		return "{\"model\": \"text-davinci-003\", \"prompt\": \"" + question + "\", \"temperature\": 0, \"max_tokens\": 1024}";
	}

	private static HttpPost getHttpPost() throws IOException {
//		Properties prop = new Properties();
//		InputStream inputStream = Files.newInputStream(Paths.get("src/main/resources/application.properties"));
//		prop.load(inputStream);
//		String openAiKey = prop.getProperty("SECRET_KEY");
//		String connectTimeout = prop.getProperty("connectTimeout");
//		String connectionRequestTimeout = prop.getProperty("connectionRequestTimeout");
//		String socketTimeout = prop.getProperty("socketTimeout");
		String openAiKey = "";
//		String openAiKey = "自己申请一个chatgpt的key";
		String connectTimeout = "60000";
		String connectionRequestTimeout = "60000";
		String socketTimeout = "60000";

		HttpPost post = new HttpPost("https://api.openai.com/v1/completions");
		post.addHeader("Content-Type", "application/json");
		post.addHeader("Authorization", "Bearer " + openAiKey);
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(Integer.parseInt(connectTimeout)).setConnectionRequestTimeout(Integer.parseInt(connectionRequestTimeout))
				.setSocketTimeout(Integer.parseInt(socketTimeout)).build();
		post.setConfig(requestConfig);
		return post;
	}

	private static LayeredConnectionSocketFactory getSslConnectionSocketFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
		return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
	}
}
