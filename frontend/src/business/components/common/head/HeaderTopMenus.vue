<template>
  <el-menu mode="horizontal" menu-trigger="click"
           :background-color="color"
           class="header-top-menus"
           text-color="#F2F2F2"
           active-text-color="#fff"
           :default-active="activeIndex"
           @select="handleSelect"
           :key="menuKey"
           router>
    <el-menu-item index="/workstation" v-if="hasLicense() && check('workstation')">
      {{ $t('commons.my_workstation') }}
    </el-menu-item>
    <el-menu-item v-if="!hasLicense()" @click="clickPlanMenu">
      {{ $t('commons.my_workstation') }}
    </el-menu-item>
    <el-menu-item index="/track" v-if="check('testTrack')" onselectstart="return false"
                  v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ', 'PROJECT_TRACK_ISSUE:READ', 'PROJECT_TRACK_REPORT:READ']">
      {{ $t('test_track.test_track') }}
    </el-menu-item>
    <el-menu-item index="/api" @click="active()" v-if="check('api')" onselectstart="return false"
                  v-permission="['PROJECT_API_DEFINITION:READ','PROJECT_API_SCENARIO:READ','PROJECT_API_REPORT:READ']">
      {{ $t('commons.api') }}
    </el-menu-item>
    <el-menu-item index="/performance" v-if="check('performance')"
                  onselectstart="return false"
                  v-permission="['PROJECT_PERFORMANCE_TEST:READ','PROJECT_PERFORMANCE_REPORT:READ']">
      {{ $t('commons.performance') }}
    </el-menu-item>
    <el-menu-item index="/report" v-if="check('reportStat')" onselectstart="return false"
                  v-permission="['PROJECT_REPORT_ANALYSIS:READ']">
      {{ $t('commons.report_statistics.title') }}
    </el-menu-item>

    <el-menu-item index="/project" onselectstart="return false"
                  v-permission="['PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE',
                  'PROJECT_CUSTOM_CODE:READ','PROJECT_ERROR_REPORT_LIBRARY:READ', 'PROJECT_TEMPLATE:READ', 'PROJECT_MESSAGE:READ']">
      {{ $t('commons.project_setting') }}
    </el-menu-item>

    <el-menu-item index="/setting" onselectstart="return false"
                  v-permission="['SYSTEM_USER:READ', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_GROUP:READ', 'SYSTEM_TEST_POOL:READ', 'SYSTEM_SETTING:READ', 'SYSTEM_AUTH:READ', 'SYSTEM_QUOTA:READ','SYSTEM_OPERATING_LOG:READ',
                  'WORKSPACE_SERVICE:READ', 'PROJECT_MESSAGE:READ', 'WORKSPACE_USER:READ', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_OPERATING_LOG:READ']">
      {{ $t('commons.system_setting') }}
    </el-menu-item>
  </el-menu>
</template>

<script>
import {hasLicense} from "@/common/js/utils";
import {MODULE_CHANGE, ModuleEvent} from "@/business/components/common/head/ListEvent";
import {validateAndSetLicense} from "@/business/permission";
import axios from "axios";

// const requireContext = require.context('@/business/components/xpack/', true, /router\.js$/);
// const report = requireContext.keys().map(key => requireContext(key).report);
// const isReport = report && report != null && report.length > 0 && report[0] != undefined ? true : false;

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const module = requireComponent.keys().length > 0 ? requireComponent("./module/Module.vue") : {};

export default {
  name: "MsTopMenus",
  data() {
    return {
      activeIndex: '/',
      isReport: true,
      modules: {},
      menuKey: 0,
    };
  },
  props: {
    color: String
  },
  watch: {
    '$route'(to) {
      if (to.matched.length > 0) {
        this.activeIndex = to.matched[0].path;
      }
      this.handleSelect(this.activeIndex);
    }
  },
  created() {

  },
  mounted() {
    if (this.$route.matched.length > 0) {
      this.activeIndex = this.$route.matched[0].path;
    }

    axios.get('/license/valid').then(response => {
      validateAndSetLicense(response.data.data); // 在调用 listModules 之前删除校验失败的 license, axios 失败不弹框
      if (!hasLicense()) {
        this.isReport = false;
      } else {
        if (module.default) {
          module.default.listModules(this);
        }
      }
    }).catch(error => {
      window.console.error(error.response || error.message);
    });

    this.registerEvents();
  },
  methods: {
    hasLicense,
    handleSelect(index) {
      if (index) {
        this.activeIndex = index;
      }
    },
    active() {
      if (this.activeIndex === '/api') {
        window.location.href = "/#/api/home";
      }
    },
    check(key) {
      if (module.default !== undefined && hasLicense()) {
        return this.modules[key] === 'ENABLE';
      }
      return true;
    },
    registerEvents() {
      ModuleEvent.$on(MODULE_CHANGE, () => {
        if (module.default) {
          module.default.listModules(this).then(() => {
            this.menuKey++;
          });
        }
      });
    },
    clickPlanMenu() {
      this.$message({
        dangerouslyUseHTMLString: true,
        showClose: true,
        message: this.$t('commons.enterprise_edition_tips'),
      });
      return false;
    },
  }
};
</script>
<style scoped>

.el-menu >>> .el-menu-item {
  box-sizing: border-box;
  height: 40px;
}

</style>
