<template>
  <el-menu menu-trigger="click"
           class="ms-menu-vertical ms-menu ms-menu-title horizontal-collapse-transition"
           text-color="#F2F2F2"
           active-text-color="#fff"
           :collapse="isCollapse"
           :background-color="color"
           :default-active="activeIndex"
           :key="menuKey"
           @select="handleSelect"
           router>
    <el-menu-item index="/workstation" v-xpack v-if="check('workstation')">
      <div>
        <img src="@/assets/module/workstation.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('commons.my_workstation') }}</span>
      </div>
    </el-menu-item>
    <el-menu-item index="/track" v-if="check('testTrack')" onselectstart="return false"
                  v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ', 'PROJECT_TRACK_ISSUE:READ', 'PROJECT_TRACK_REPORT:READ']">
      <div>
        <img src="@/assets/module/track.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('test_track.test_track') }}</span>
      </div>
    </el-menu-item>
    <el-menu-item index="/api" @click="active()" v-if="check('api')" onselectstart="return false"
                  v-permission="['PROJECT_API_DEFINITION:READ','PROJECT_API_SCENARIO:READ','PROJECT_API_REPORT:READ']">
      <div>
        <img src="@/assets/module/api.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('commons.api') }}</span>
      </div>
    </el-menu-item>
    <el-menu-item index="/ui" @click="active()" v-if="hasLicense() && check('ui')" onselectstart="return false"
                  v-permission="['PROJECT_UI_SCENARIO:READ','PROJECT_UI_REPORT:READ']">
      <div>
        <img src="@/assets/module/ui.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('commons.ui') }}</span>
      </div>
    </el-menu-item>
    <el-menu-item v-if="!hasLicense()" @click="clickPlanMenu">
      <div>
        <img src="@/assets/module/ui.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('commons.ui') }}</span>
      </div>
    </el-menu-item>
    <el-menu-item index="/performance" v-if="check('performance')"
                  onselectstart="return false"
                  v-permission="['PROJECT_PERFORMANCE_TEST:READ','PROJECT_PERFORMANCE_REPORT:READ']">
      <div>
        <img src="@/assets/module/performance.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('commons.performance') }}</span>
      </div>
    </el-menu-item>
    <el-menu-item index="/report" v-if="check('reportStat')" onselectstart="return false"
                  v-permission="['PROJECT_REPORT_ANALYSIS:READ']">
      <div>
        <img src="@/assets/module/report.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('commons.report_statistics.title') }}</span>
      </div>
    </el-menu-item>

    <el-menu-item index="/project" onselectstart="return false"
                  v-permission="['PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE',
                  'PROJECT_CUSTOM_CODE:READ','PROJECT_ERROR_REPORT_LIBRARY:READ', 'PROJECT_TEMPLATE:READ', 'PROJECT_MESSAGE:READ']">
      <div>
        <img src="@/assets/module/project.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('commons.project_setting') }}</span>
      </div>
    </el-menu-item>
    <el-menu-item index="/setting" onselectstart="return false"
                  v-permission="['SYSTEM_USER:READ', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_GROUP:READ', 'SYSTEM_TEST_POOL:READ', 'SYSTEM_SETTING:READ', 'SYSTEM_AUTH:READ', 'SYSTEM_QUOTA:READ','SYSTEM_OPERATING_LOG:READ',
                  'WORKSPACE_SERVICE:READ', 'WORKSPACE_USER:READ', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_OPERATING_LOG:READ']">
      <div>
        <img src="@/assets/module/system.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('commons.system_setting') }}</span>
      </div>
    </el-menu-item>
  </el-menu>
</template>

<script>
import {hasLicense} from "@/common/js/utils";
import {MODULE_CHANGE, ModuleEvent} from "@/business/components/common/head/ListEvent";
import {validateAndSetLicense} from "@/business/permission";
import axios from "axios";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const module = requireComponent.keys().length > 0 ? requireComponent("./module/Module.vue") : {};

export default {
  name: "MsAsideMenus",
  data() {
    return {
      activeIndex: '/',
      isReport: true,
      modules: {},
      menuKey: 0,
    };
  },
  props: {
    color: String,
    isCollapse: {
      type: Boolean,
      default: true,
    }
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

    axios.get('/license/validate').then(response => {
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
        message: this.$t('commons.ui_edition_tips'),
      });
      return false;
    },
  }
};
</script>
<style scoped>
.ms-menu-vertical:not(.el-menu--collapse) {
  width: var(--asideOpenWidth);
  border-right: 0px !important;
}

.el-menu--collapse {
  width: var(--asideWidth);
  border-right: 0px !important;
}

.ms-menu-item-title {
  margin-left: 11px;
  font-size: 13px;
  color: #fff;
}

.ms-menu-title {
  align-items: center;
}

/deep/ .el-menu-item {
  padding-left: 11px !important;
}

.ms-menu-img {
  margin: 0px;
  width: 22px;
  border: 0;
  display: inline-block;
  box-sizing: border-box;
  background-repeat: no-repeat;
  background-position: 50% center;
}

.ms-menu-vertical > li.is-active {
  background: var(--color) !important;
  mix-blend-mode: hard-light;
  color: #fff !important;
}

.horizontal-collapse-transition {
  transition: 0s width ease-in-out, 0s padding-left ease-in-out, 0s padding-right ease-in-out;
}
</style>
