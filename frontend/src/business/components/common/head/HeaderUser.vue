<template>
  <el-dropdown size="medium" @command="handleCommand" class="align-right">
    <span class="dropdown-link">
        {{currentUser.name}}<i class="el-icon-caret-bottom el-icon--right"/>
    </span>
    <template v-slot:dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="personal">{{$t('commons.personal_information')}}</el-dropdown-item>
        <el-dropdown-item command="about">{{$t('commons.about_us')}} <i class="el-icon-info"/></el-dropdown-item>
        <el-dropdown-item command="help">{{$t('commons.help_documentation')}}</el-dropdown-item>
        <el-dropdown-item command="logout">{{$t('commons.exit_system')}}</el-dropdown-item>
      </el-dropdown-menu>
    </template>

    <about-us ref="aboutUs"/>
  </el-dropdown>
</template>

<script>
  import {getCurrentUser} from "../../../../common/js/utils";
  import AboutUs from "./AboutUs";
  import axios from "axios";

  export default {
    name: "MsUser",
    components: {AboutUs},
    computed: {
      currentUser: () => {
        return getCurrentUser();
      }
    },
    methods: {
      handleCommand(command) {
        switch (command) {
          case "personal":
            // TODO 优化路由跳转，避免重复添加路由
            this.$router.push('/setting/personsetting').catch(error => error);
            break;
          case "logout":
            axios.get("/signout").then(response => {
              if (response.data.success) {
                localStorage.clear();
                window.location.href = "/login";
              } else {
                if (response.data.message === 'sso') {
                  localStorage.clear();
                  window.location.href = "/sso/logout"
                }
              }
            }).catch(error => {
              localStorage.clear();
              window.location.href = "/login";
            });
            break;
          case "about":
            this.$refs.aboutUs.open();
            break;
          case "help":
            window.location.href = "https://metersphere.io/docs/index.html";
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
    line-height: 40px;
  }

  .align-right {
    float: right;
  }

</style>


