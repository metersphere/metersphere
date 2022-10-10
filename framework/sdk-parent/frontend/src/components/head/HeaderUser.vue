<template>
  <el-dropdown size="medium" @command="handleCommand" class="align-right">
    <span class="dropdown-link">
        {{ currentUser.name }}<i class="el-icon-caret-bottom el-icon--right"/>
    </span>
    <template v-slot:dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="personal" :disabled="checkPermissions()">
          {{ $t('commons.personal_information') }}
        </el-dropdown-item>
        <el-dropdown-item command="about">{{ $t('commons.about_us') }} <i class="el-icon-info"/></el-dropdown-item>
        <el-dropdown-item command="help">{{ $t('commons.help_documentation') }}</el-dropdown-item>
        <el-dropdown-item command="ApiHelp">{{ $t('commons.api_help_documentation') }}</el-dropdown-item>
        <el-dropdown-item command="logout">{{ $t('commons.exit_system') }}</el-dropdown-item>
      </el-dropdown-menu>
    </template>

    <about-us ref="aboutUs"/>
    <el-dialog :close-on-click-modal="false" width="80%"
               z-index="1000"
               :visible.sync="resVisible" class="api-import" destroy-on-close @close="closeDialog" append-to-body>
      <ms-person-router @closeDialog="closeDialog"/>
    </el-dialog>
  </el-dropdown>
</template>

<script>
import {getCurrentUser} from "../../utils/token";
import {hasPermissions} from "../../utils/permission";
import AboutUs from "./AboutUs";
import {useUserStore} from "@/store";
import MsPersonRouter from "../personal/PersonRouter";

const userStore = useUserStore();

export default {
  name: "MsUser",
  components: {AboutUs, MsPersonRouter},
  data() {
    return {
      resVisible: false,
    };
  },
  computed: {
    currentUser: () => {
      return getCurrentUser();
    },
  },
  mounted() {
    this.$EventBus.$on('showPersonInfo', this.handleCommand)
  },
  beforeDestroy() {
    this.$EventBus.$off("showPersonInfo")
  },
  methods: {
    handleCommand(command) {
      switch (command) {
        case "personal":
          // TODO 优化路由跳转，避免重复添加路由
          // this.$router.push('/setting/personsetting').catch(error => error);
          this.resVisible = true;
          break;
        case "logout":
          userStore.userLogout();
          break;
        case "about":
          this.$refs.aboutUs.open();
          break;
        case "help":
          window.open('https://metersphere.io/docs/index.html', "_blank");
          break;
        case "ApiHelp":
          window.open('/swagger-ui.html', "_blank");
          break;
        default:
          break;
      }
    },
    closeDialog() {
      this.resVisible = false;
    },
    checkPermissions() {
      return !hasPermissions('PERSONAL_INFORMATION:READ+EDIT',
        'PERSONAL_INFORMATION:READ+API_KEYS',
        'PERSONAL_INFORMATION:READ+EDIT_PASSWORD',
        'PERSONAL_INFORMATION:READ+THIRD_ACCOUNT',
        'PERSONAL_INFORMATION:READ+UI_SETTING'
      );
    }
  }
};
</script>

<style scoped>
.dropdown-link {
  cursor: pointer;
  font-size: 12px;
  line-height: 40px;
}

.align-right {
  float: right;
  margin-right: 20px;
}
</style>


