<template>

  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <project-switch :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                 :default-active="pathName">
          <el-menu-item :index="'/track/home'" v-permission="['PROJECT_TRACK_HOME:READ']">
            {{ $t("i18n.home") }}
          </el-menu-item>
          <el-menu-item :index="caseListPath" v-permission="['PROJECT_TRACK_CASE:READ']">
            {{ $t("test_track.case.test_case") }}
          </el-menu-item>

          <el-menu-item :index="'/track/review/all'" v-permission="['PROJECT_TRACK_REVIEW:READ']"
                        popper-class="submenu">
            {{ $t('test_track.review.test_review') }}
          </el-menu-item>
          <el-menu-item :index="'/track/plan/all'" v-permission="['PROJECT_TRACK_PLAN:READ']"
                        popper-class="submenu">
            {{ $t('test_track.plan.test_plan') }}
          </el-menu-item>

          <el-menu-item :index="'/track/issue'" popper-class="submenu" v-permission="['PROJECT_TRACK_ISSUE:READ']">
            {{ $t('test_track.issue.issue_management') }}
          </el-menu-item>

          <el-menu-item :index="'/track/testPlan/reportList'" popper-class="submenu" v-permission="['PROJECT_TRACK_REPORT:READ']">
            {{ $t("commons.report") }}
          </el-menu-item>
        </el-menu>
      </el-col>
      <el-col :span="10">
        <ms-header-right-menus/>
      </el-col>
    </el-row>
  </div>

</template>
<script>

import ProjectSwitch from "metersphere-frontend/src/components/head/ProjectSwitch";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsHeaderRightMenus from "metersphere-frontend/src/components/layout/HeaderRightMenus";
import {PROJECT_NAME} from "metersphere-frontend/src/utils/constants";

export default {
  name: "TrackHeaderMenus",
  components: {ProjectSwitch, MsHeaderRightMenus},
  data() {
    return {
      testPlanViewPath: '',
      isRouterAlive: true,
      testCaseEditPath: '',
      testCaseReviewEditPath: '',
      testCaseProjectPath: '',
      isProjectActivation: true,
      currentProject: sessionStorage.getItem(PROJECT_NAME),
      pathName: '',
    };
  },
  watch: {
    '$route': {
      immediate: true,
      handler(to, from) {
        if (to.path.indexOf("/track/review") >= 0) {
          this.pathName = '/track/review/all';
        } else if (to.path.indexOf("/track/plan") >= 0) {
          this.pathName = '/track/plan/all';
        } else if (to.path.indexOf("/track/case") >= 0) {
          this.pathName = this.caseListPath;
        } else {
          this.pathName = to.path;
        }
        this.init();
      }
    }
  },
  mounted() {
    this.init();
  },
  computed: {
    caseListPath() {
      return '/track/case/all?projectId=' + this.getProjectId();
    }
  },
  methods: {
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true;
      });
    },
    init() {
      let path = this.$route.path;
      if (path.indexOf("/track/plan/view") >= 0) {
        this.testPlanViewPath = path;
        this.reload();
      }
      if (path.indexOf("/track/case/edit") >= 0) {
        this.testCaseEditPath = path;
        this.reload();
      }
      if (path.indexOf("/track/review/view") >= 0) {
        this.testCaseReviewEditPath = path;
        this.reload();
      }
    },
    getProjectId() {
      return getCurrentProjectID();
    }
  }
};

</script>

<style scoped>
.el-divider--horizontal {
  margin: 0;
}

#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.blank_item {
  display: none;
}

.el-divider--horizontal {
  margin: 0;
}

.deactivation :deep(.el-submenu__title) {
  border-bottom: white !important;
}

.el-menu-item {
  padding: 0 10px;
}
</style>
