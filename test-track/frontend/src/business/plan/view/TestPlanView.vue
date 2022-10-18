<template>

  <div>
    <ms-test-plan-header-bar>
      <template v-slot:info>
        <select-menu
          :data="testPlans"
          :current-data="currentPlan"
          :title="$t('test_track.plan_view.plan')"
          @dataChange="changePlan"/>
      </template>
      <template v-slot:menu>
        <el-menu v-if="isMenuShow" :active-text-color="color" :default-active="activeIndex"
                 class="el-menu-demo header-menu" mode="horizontal" @select="handleSelect">
          <el-menu-item index="functional">{{ $t('test_track.functional_test_case') }}</el-menu-item>
          <el-menu-item index="api" v-modules="['api']">{{ $t('test_track.api_test_case') }}</el-menu-item>
          <el-menu-item index="ui" v-modules="['ui']" v-if="hasLicense()">{{ $t('test_track.ui_test_case') }}</el-menu-item>
          <el-menu-item index="load" v-modules="['performance']">{{
              $t('test_track.performance_test_case')
            }}
          </el-menu-item>
          <el-menu-item index="report">{{ $t('test_track.report_statistics') }}</el-menu-item>
        </el-menu>
      </template>
    </ms-test-plan-header-bar>

    <test-plan-functional v-if="activeIndex === 'functional'" :redirectCharType="redirectCharType"
                          :clickType="clickType" :plan-id="planId" :version-enable="versionEnable"
                          :plan-status="currentPlan.status"
                          ref="testPlanFunctional"/>
    <test-plan-api
      v-if="activeIndex === 'api'"
      :redirectCharType="redirectCharType"
      :clickType="clickType"
      :plan-id="planId"
      :version-enable="versionEnable"
      :plan-status="currentPlan.status"/>
    <test-plan-ui
      v-if="activeIndex === 'ui'"
      :redirectCharType="redirectCharType"
      :clickType="clickType"
      :plan-id="planId"
      :version-enable="versionEnable"
      :plan-status="currentPlan.status"/>
    <test-plan-load
      v-if="activeIndex === 'load'"
      :redirectCharType="redirectCharType"
      :clickType="clickType"
      :plan-id="planId"
      :version-enable="versionEnable"
      :plan-status="currentPlan.status"/>
    <test-plan-report-content
      class="plan-report"
      v-if="activeIndex === 'report'"
      :need-move-bar="true"
      :plan-id="planId"
      :version-enable="versionEnable"/>

    <is-change-confirm
      @confirm="changeConfirm"
      ref="isChangeConfirm"/>

  </div>

</template>

<script>

import NodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import SelectMenu from "../../common/SelectMenu";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsTestPlanHeaderBar from "./comonents/head/TestPlanHeaderBar";
import TestPlanFunctional from "./comonents/functional/TestPlanFunctional";
import TestPlanApi from "./comonents/api/TestPlanApi";
import TestPlanUi from "./comonents/ui/TestPlanUi";
import TestPlanLoad from "@/business/plan/view/comonents/load/TestPlanLoad";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission"
import TestPlanReportContent from "@/business/plan/view/comonents/report/detail/TestPlanReportContent";
import IsChangeConfirm from "metersphere-frontend/src/components/IsChangeConfirm";
import {PROJECT_ID, WORKSPACE_ID} from "metersphere-frontend/src/utils/constants";
import {useStore} from "@/store";
import {testPlanListAll} from "@/api/remote/plan/test-plan";
import {isProjectVersionEnable} from "@/business/utils/sdk-utils";

export default {
  name: "TestPlanView",
  components: {
    IsChangeConfirm,
    TestPlanReportContent,
    TestPlanApi,
    TestPlanFunctional,
    MsTestPlanHeaderBar,
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
    NodeTree,
    SelectMenu,
    TestPlanLoad,
    TestPlanUi
  },
  data() {
    return {
      testPlans: [],
      currentPlan: {},
      activeIndex: "functional",
      isMenuShow: true,
      //报表跳转过来的参数-通过哪个图表跳转的
      redirectCharType: '',
      //报表跳转过来的参数-通过哪种数据跳转的
      clickType: '',
      tmpActiveIndex: '',
      versionEnable: false,
      projectId: null
    };
  },
  computed: {
    planId: function () {
      return this.$route.params.planId;
    },
    color: function () {
      return `var(--primary_color)`;
    }
  },
  watch: {
    '$route.query.projectId'() {
      let projectId = this.$route.query.projectId;
      if (projectId && projectId !== getCurrentProjectID()) {
        sessionStorage.setItem(PROJECT_ID, projectId);
      }
    },
    '$route.params.planId'() {
      this.genRedirectParam();
      this.getTestPlans();
    },
  },
  beforeRouteLeave(to, from, next) {
    if (!this.$refs.testPlanFunctional) {
      next();
    } else if (this.$refs.testPlanFunctional.handleBeforeRouteLeave(to)) {
      next();
    }
  },
  created() {
    let workspaceId = this.$route.params.workspaceId;
    if (workspaceId) {
      sessionStorage.setItem(WORKSPACE_ID, workspaceId);
    } else {
      if (this.$route.query.workspaceId) {
        workspaceId = this.$route.query.workspaceId;
        sessionStorage.setItem(WORKSPACE_ID, workspaceId);
      }
    }
    let projectId = this.$route.params.projectId;
    if (projectId) {
      sessionStorage.setItem(PROJECT_ID, projectId);
      this.projectId = projectId;
    } else {
      if (this.$route.query.projectId) {
        projectId = this.$route.query.projectId;
        sessionStorage.setItem(PROJECT_ID, this.$route.query.projectId);
        this.projectId = projectId;
      } else {
        this.projectId = getCurrentProjectID();
      }
    }
    this.$EventBus.$on('projectChange', this.handleProjectChange);
    this.checkVersionEnable();
  },
  destroyed() {
    this.$EventBus.$off('projectChange', this.handleProjectChange);
  },
  mounted() {
    this.getTestPlans();
  },
  activated() {
    this.genRedirectParam();
  },
  methods: {
    handleProjectChange() {
      if (this.$route.path.indexOf("plan/view") > 0) {
        this.$router.push('/track/plan/all');
      }
    },
    genRedirectParam() {
      if (this.$route.params.charType) {
        this.redirectCharType = this.$route.params.charType;
      } else {
        this.redirectCharType = this.$route.query.charType;
      }
      this.clickType = this.$route.params.clickType;
      if (this.redirectCharType) {
        if (this.redirectCharType == 'scenario') {
          this.activeIndex = 'api';
        } else if (this.redirectCharType != null && this.redirectCharType != '') {
          this.activeIndex = this.redirectCharType;
        }
      } else {
        this.activeIndex = "functional";
      }
    },
    getTestPlans() {
      testPlanListAll({projectId: getCurrentProjectID()})
        .then(response => {
          this.testPlans = response.data;
          this.testPlans.forEach(plan => {
            if (this.planId && plan.id === this.planId) {
              this.currentPlan = plan;
            }
          });
        });
    },
    changePlan(plan) {
      this.currentPlan = plan;
      this.$router.push('/track/plan/view/' + plan.id);
    },
    handleSelect(key) {
      let isTestCaseMinderChanged = useStore().isTestCaseMinderChanged;
      if (key !== 'functional' && isTestCaseMinderChanged) {
        if (this.currentPlan.status === 'Archived') {
          this.activeIndex = key;
        } else {
          this.$refs.isChangeConfirm.open();
          this.tmpActiveIndex = key;
          return;
        }
      }
      this.activeIndex = key;
    },
    changeConfirm(isSave) {
      if (isSave) {
        this.$refs.testPlanFunctional.$refs.minder.save(window.minder.exportJson());
      }
      useStore().isTestCaseMinderChanged = false;
      this.$nextTick(() => {
        if (this.tmpActiveIndex) {
          this.activeIndex = this.tmpActiveIndex;
          this.tmpActiveIndex = null;
        }
      });
    },
    reloadMenu() {
      this.isMenuShow = false;
      this.$nextTick(() => {
        this.isMenuShow = true;
      });
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        isProjectVersionEnable(this.projectId)
          .then(response => {
            this.versionEnable = response.data;
          });
      }
    },
    hasLicense(){
      return hasLicense();
    }
  },
};
</script>

<style scoped>

.select-menu {
  display: inline-block;
}

.header-menu.el-menu--horizontal > li {
  height: 49px;
  line-height: 50px;
  color: dimgray;
}

.plan-report :deep(.report-content) {
  height: calc(100vh - 140px);
}

.el-menu-item {
  padding: 0 10px;
}
</style>
