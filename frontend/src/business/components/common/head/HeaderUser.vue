<template>
  <el-dropdown size="medium" @command="handleCommand" class="align-right">
    <span class="dropdown-link">
        {{ currentUser.name }}<i class="el-icon-caret-bottom el-icon--right"/>
    </span>
    <template v-slot:dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="personal">{{ $t('commons.personal_information') }}</el-dropdown-item>
        <el-dropdown-item command="about">{{ $t('commons.about_us') }} <i class="el-icon-info"/></el-dropdown-item>
        <el-dropdown-item command="help">{{ $t('commons.help_documentation') }}</el-dropdown-item>
        <el-dropdown-item command="ApiHelp">{{ $t('commons.api_help_documentation') }}</el-dropdown-item>
        <el-dropdown-item command="logout">{{ $t('commons.exit_system') }}</el-dropdown-item>
      </el-dropdown-menu>
    </template>

    <about-us ref="aboutUs"/>
    <el-dialog :close-on-click-modal="false" width="80%"
               :visible.sync="resVisible" class="api-import" destroy-on-close @close="closeDialog">
      <ms-person-router @closeDialog = "closeDialog"/>
    </el-dialog>
  </el-dropdown>
</template>

<script>
import {getCurrentUser} from "@/common/js/utils";
import AboutUs from "./AboutUs";
import {logout} from "@/network/user";

import  MsPersonRouter from "@/business/components/settings/components/PersonRouter"

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const auth = requireComponent.keys().length > 0 ? requireComponent("./auth/Auth.vue") : {};

export default {
  name: "MsUser",
  components: {AboutUs,MsPersonRouter},
  data() {
    return {
      resVisible:false,
    }
  },
  computed: {
    currentUser: () => {
      return getCurrentUser();
    },
  },
  methods: {
    logout: function () {
      logout();
    },
    handleCommand(command) {
      switch (command) {
        case "personal":
          // TODO 优化路由跳转，避免重复添加路由
         // this.$router.push('/setting/personsetting').catch(error => error);
          this.resVisible = true;
          break;
        case "logout":
          this.logout();
          break;
        case "about":
          this.$refs.aboutUs.open();
          break;
        case "help":
          window.open('https://metersphere.io/docs/index.html', "_blank");
          break;
        case "ApiHelp":
          window.open('/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config', "_blank");
          break;
        default:
          break;
      }
    },
    changeBar(item) {
      this.$store.commit('setVersionSwitch', item);
      if (item == "old") {
        window.location.href = "/#/api/home_obsolete";
      } else {
        window.location.href = "/#/api/home";
      }
    },
    closeDialog(){
      this.resVisible = false;
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


