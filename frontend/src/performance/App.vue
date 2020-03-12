<template>
  <el-col v-if="auth">
    <el-row id="header-top" type="flex" justify="space-between" align="middle">
      <el-col :span="3">
        <a class="logo"/>
      </el-col>
      <el-col :span="7">
        <ms-top-menus/>
      </el-col>
      <el-col :span="14">
        <ms-user/>
      </el-col>
    </el-row>

    <ms-view/>
    <ms-web-socket/>
  </el-col>
</template>

<script>
  import MsTopMenus from "./components/HeaderTopMenus";
  import MsView from "./components/router/View";
  import MsUser from "./components/HeaderUser";
  import MsWebSocket from "./components/websocket/WebSocket";

  export default {
    name: 'app',
    data() {
      return {
        auth: false
      }
    },
    beforeCreate() {
      this.$get("/isLogin").then(response => {
        if (response.data.success) {
          window.console.log(response.data);
          this.$setLang(response.data.data);
          this.auth = true;
        } else {
          window.location.href = "/login"
        }
      }).catch(() => {
        window.location.href = "/login"
      });
    },
    components: {MsWebSocket, MsUser, MsView, MsTopMenus},
    methods: {}
  }
</script>

<style>
  body {
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
    font-size: 14px;
    margin: 0;
  }
</style>

<style scoped>
  #header-top {
    width: 100%;
    height: 40px;
    padding: 0 10px;
    background-color: rgb(44, 42, 72);
    color: rgb(245, 245, 245);
    font-size: 14px;
  }

  .logo {
    width: 156px;
    margin-right: 20px;
    display: inline-block;
    background-size: 156px 30px;
    height: 40px;
    background-repeat: no-repeat;
    background-position: 50% center;
    background-image: url("../assets/MeterSphere-反白.png");
  }

  .menus > * {
    color: inherit;
    padding: 0;
    max-width: 180px;
    white-space: pre;
    cursor: pointer;
    line-height: 40px;
  }

  .menus > a {
    padding-right: 15px;
    text-decoration: none;
  }
</style>

