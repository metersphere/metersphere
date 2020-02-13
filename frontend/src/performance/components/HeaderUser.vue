<template>
  <el-dropdown size="medium" @command="handleCommand">
        <span class="dropdown-link">
            {{currentUser.name}}<i class="el-icon-caret-bottom el-icon--right"/>
        </span>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="personal">个人信息</el-dropdown-item>
      <el-dropdown-item command="logout">退出系统</el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
  import Cookies from 'js-cookie';
  import {TokenKey} from '../../common/constants';

  export default {
    name: "MsUser",
    computed: {
      currentUser: () => {
        let user = Cookies.get(TokenKey);
        window.console.log(user);
        return JSON.parse(user);
      }
    },
    methods: {
      handleCommand(command) {
        switch (command) {
          case "personal":
            this.$i18n.locale = "zh_CN";
            break;
          case "logout":
            this.$get("/signout", function () {
              Cookies.remove(TokenKey);
              window.location.href = "/login";
            });
            break;
          default:
            break;
        }
      }
    }
  }
</script>

<style scoped>
  .dropdown-link {
    cursor: pointer;
    font-size: 12px;
    color: rgb(245, 245, 245);
  }
</style>
