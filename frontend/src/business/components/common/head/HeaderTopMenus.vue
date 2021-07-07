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

    <el-menu-item index="/track" v-if="check('testTrack')"
                  v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ']">
      {{ $t('test_track.test_track') }}
    </el-menu-item>
    <el-menu-item index="/api" @click="active()" v-if="check('api')"
                  v-permission="['PROJECT_API_DEFINITION:READ','PROJECT_API_SCENARIO:READ','PROJECT_API_REPORT:READ']">
      {{ $t('commons.api') }}
    </el-menu-item>
    <el-menu-item index="/performance" v-if="check('performance')"
                  onselectstart="return false"
                  v-permission="['PROJECT_PERFORMANCE_TEST:READ','PROJECT_PERFORMANCE_REPORT:READ']">
      {{ $t('commons.performance') }}
    </el-menu-item>
    <el-menu-item index="/report"
                  v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ']"
                  v-if="isReport && check('reportStat')">
      {{ $t('commons.report_statistics.title') }}
    </el-menu-item>

    <el-menu-item index="/setting" onselectstart="return false">
      {{ $t('commons.system_setting') }}
    </el-menu-item>
  </el-menu>
</template>

<script>
import {LicenseKey} from '@/common/js/constants';
import {mapGetters} from "vuex";
import {hasLicense} from "@/common/js/utils";
import {MODULE_CHANGE, ModuleEvent} from "@/business/components/common/head/ListEvent";

const requireContext = require.context('@/business/components/xpack/', true, /router\.js$/);
const report = requireContext.keys().map(key => requireContext(key).report);
const isReport = report && report != null && report.length > 0 && report[0] != undefined ? true : false;

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const module = requireComponent.keys().length > 0 ? requireComponent("./module/Module.vue") : {};

export default {
  name: "MsTopMenus",
  data() {
    return {
      activeIndex: '/',
      isReport: isReport,
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
  mounted() {
    if (this.$route.matched.length > 0) {
      this.activeIndex = this.$route.matched[0].path;
    }
    let license = localStorage.getItem(LicenseKey);
    if (license != "valid") {
      this.isReport = false;
    } else {
      if (module.default) {
        module.default.listModules(this);
      }
    }

    this.registerEvents();
  },
  computed: {
    ...mapGetters([
      'isNewVersion',
      'isOldVersion',
    ])
  },
  methods: {
    handleSelect(index) {
      this.activeIndex = index;
    },
    active() {
      if (this.activeIndex === '/api') {
        if (this.isNewVersion) {
          window.location.href = "/#/api/home";
        } else if (this.isOldVersion) {
          window.location.href = "/#/api/home_obsolete";
        }
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
    }
  }
};
</script>
<style scoped>

.el-menu >>> .el-menu-item {
  box-sizing: border-box;
  height: 40px;
}

</style>
