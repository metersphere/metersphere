<template>
  <el-col v-if="auth">
    <el-row id="header-top" type="flex" justify="space-between" align="middle">
      <a class="logo"/>
      <ms-user/>
    </el-row>
    <el-row id="header-bottom" type="flex" justify="space-between" align="middle">
      <el-col :span="10">
        <ms-menus/>
      </el-col>
      <el-col :span="4">
        <el-row type="flex" justify="center" align="middle">
          <router-link to="/createTest">
            <el-button type="primary" size="small">创建测试</el-button>
          </router-link>
        </el-row>
      </el-col>
      <el-col :span="10">
        <ms-setting/>
      </el-col>
    </el-row>
    <ms-view/>
    <ms-web-socket/>
  </el-col>
</template>

<script>
  import MsMenus from "./components/HeaderMenus";
  import MsSetting from "./components/HeaderSetting";
  import MsView from "./components/router/View";
  import MsUser from "./components/HeaderUser";
  import MsWebSocket from "./components/websocket/WebSocket";

  export default {
    name: 'app',
    data() {
      return {
        auth: false,
      }
    },
    beforeCreate() {
      this.$get("/isLogin").then(response => {
        if (response.data.success) {
          this.auth = true;
        } else {
          window.location.href = "/login"
        }
      }).catch(() => {
        window.location.href = "/login"
      });
    },
    components: {MsWebSocket, MsUser, MsMenus, MsSetting, MsView},
    methods: {
    }
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

  #header-bottom {
    height: 40px;
    padding: 0 15px;
    border-bottom: 1px solid #E6E6E6;
    cursor: default;
    color: #404040;
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

