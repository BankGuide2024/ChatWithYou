<template>
  <div class="home">
    <img alt="Vue logo" src="../assets/logo.png" width="480" height="200">
    <HelloWorld msg="Welcome to ChatGPT!"/>
    <el-divider />
    <div style="margin: 20px 0">
      <el-input
          v-model="search"
          :autosize="{ minRows: 2, maxRows: 4 }"
          placeholder="请输入您要询问的问题"
          style="width: 500px;height: 50px"
          clearable
      />
      <el-button text @click="open"></el-button>
      <div style="margin: 20px 0"/>
      <el-button type="primary" @click="chat">Chat!</el-button>
      <div style="margin: 20px 0"/>
      <el-divider />
      <div style="margin: 20px 0"/>
      <div>
        <h3>{{ message }}</h3>
      </div>
      <el-divider />
      <div style="margin: 20px 0"/>
      <div style="margin: 20px 0"/>
<!--      <el-table :data="tableData" border style="width: 100%">-->
<!--        <el-table-column prop="id" label="ID" sortable/>-->
<!--        <el-table-column prop="input" label="问题描述"/>-->
<!--        <el-table-column prop="output" label="问题回答"/>-->
<!--      </el-table>-->
    </div>

  </div>
</template>

<script>


import request from "@/utils/request";
import HelloWorld from "@/components/HelloWorld";

export default {

  name: 'HomeView',
  components: {
    HelloWorld
  },
  data() {
    return {
      form: {},
      search: '',
      message:'',
      // tableData: []
    }
  },
  methods: {
    // 在后端发起请求，然后把请求的结果返回给前端页面进行展示
    chat() {
      this.message = "AI正在努力思考这个问题……"
      if(this.message === ''){
        this.message = "您还没有问我问题哦！"
      }else {
        request.get("/chats/" + this.search).then(res => {
          this.message = res.data.output
        })
      }
    },
    open(){

    }
  }
}
</script>

<style>

</style>
