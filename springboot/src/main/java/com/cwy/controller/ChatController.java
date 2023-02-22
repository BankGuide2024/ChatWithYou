package com.cwy.controller;

import com.cwy.common.Code;
import com.cwy.common.Result;
import com.cwy.entity.Chat;
import com.cwy.service.impl.ChatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chats")
public class ChatController {
	@Autowired
	private ChatServiceImpl chatService;

	@GetMapping("/{input}")
	public Result getByFuzzyQueryInput(@PathVariable String input){
		Chat chat = chatService.getByFuzzyQueryInput(input);
		Integer code = chat != null ? Code.GET_OK : Code.GET_ERR;
		String msg = chat != null ? "数据查询成功" : "数据查询失败，请重试！";
		return new Result(code,chat,msg);
	}
}
