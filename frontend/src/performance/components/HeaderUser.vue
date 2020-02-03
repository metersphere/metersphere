<template>
  <el-dropdown size="medium" @command="handleCommand">
        <span class="dropdown-link">
            莫 鹍<i class="el-icon-caret-bottom el-icon--right"/>
        </span>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="personal">个人信息</el-dropdown-item>
      <el-dropdown-item command="logout">退出系统</el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
  export default {
    name: "MsUser",
    methods: {
      handleCommand(command) {
        switch (command) {
          case "personal":
            this.$i18n.locale = "zh_CN";
            break;
          case "logout":
            this.$get("/signout", function () {
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
