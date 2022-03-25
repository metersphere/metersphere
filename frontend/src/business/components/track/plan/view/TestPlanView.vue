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
          <el-menu-item index="load" v-modules="['performance']">{{ $t('test_track.performance_test_case') }}</el-menu-item>
          <el-menu-item index="report">{{ $t('test_track.report_statistics') }}</el-menu-item>
        </el-menu>
      </template>
    </ms-test-plan-header-bar>

    <test-plan-functional v-if="activeIndex === 'functional'" :redirectCharType="redirectCharType"
                          :clickType="clickType" :plan-id="planId" :version-enable="versionEnable"
                          ref="testPlanFunctional"/>
    <test-plan-api v-if="activeIndex === 'api'" :redirectCharType="redirectCharType" :clickType="clickType"
                   :plan-id="planId" :version-enable="versionEnable"/>
    <test-plan-load v-if="activeIndex === 'load'" :redirectCharType="redirectCharType" :clickType="clickType"
                    :plan-id="planId" :version-enable="versionEnable"/>
    <test-plan-report-content v-if="activeIndex === 'report'" :plan-id="planId" :version-enable="versionEnable"/>

    <is-change-confirm
      :title="'请保存脑图'"
      :tip="'脑图未保存，确认保存脑图吗？'"
      @confirm="changeConfirm"
      ref="isChangeConfirm"/>

  </div>

</template>

<script>

import NodeTree from "../../common/NodeTree";
import TestPlanTestCaseList from "./comonents/functional/FunctionalTestCaseList";
import SelectMenu from "../../common/SelectMenu";
import MsContainer from "../../../common/components/MsContainer";
import MsAsideContainer from "../../../common/components/MsAsideContainer";
import MsMainContainer from "../../../common/components/MsMainContainer";
import MsTestPlanHeaderBar from "./comonents/head/TestPlanHeaderBar";
import TestPlanFunctional from "./comonents/functional/TestPlanFunctional";
import TestPlanApi from "./comonents/api/TestPlanApi";
import TestPlanLoad from "@/business/components/track/plan/view/comonents/load/TestPlanLoad";
import {getCurrentProjectID, hasLicense} from "@/common/js/utils";
import TestPlanReportContent from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContent";
import IsChangeConfirm from "@/business/components/common/components/IsChangeConfirm";

export default {
  name: "TestPlanView",
  components: {
    IsChangeConfirm,
    TestPlanReportContent,
    TestPlanApi,
    TestPlanFunctional,
    MsTestPlanHeaderBar,
    MsMainContainer,
    MsAsideContainer, MsContainer, NodeTree, TestPlanTestCaseList, SelectMenu, TestPlanLoad
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
    this.$EventBus.$on('projectChange', () => {
      if (this.$route.name === 'planView') {
        this.$router.push('/track/plan/all');
      }
    });
    this.projectId = getCurrentProjectID();
    this.checkVersionEnable();
  },
  mounted() {
    this.getTestPlans();
  },
  activated() {
    this.genRedirectParam();
  },
  methods: {
    genRedirectParam() {
      this.redirectCharType = this.$route.params.charType;
      this.clickType = this.$route.params.clickType;
      if (this.redirectCharType != "") {
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
      this.$post('/test/plan/list/all', {projectId: getCurrentProjectID()}, response => {
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
      let isTestCaseMinderChanged = this.$store.state.isTestCaseMinderChanged;
      if (key !== 'functional' && isTestCaseMinderChanged) {
        this.$refs.isChangeConfirm.open();
        this.tmpActiveIndex = key;
        return;
      }
      this.activeIndex = key;
    },
    changeConfirm(isSave) {
      if (isSave) {
        this.$refs.testPlanFunctional.$refs.minder.save(window.minder.exportJson());
      }
      this.$store.commit('setIsTestCaseMinderChanged', false);
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
        this.$get('/project/version/enable/' + this.projectId, response => {
          this.versionEnable = response.data;
        });
      }
    },
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


</style>
