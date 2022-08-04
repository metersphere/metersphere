<template>
  <test-plan-report-container id="functional" :title="$t('test_track.report.analysis_functional')">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane v-if="resultEnable" :label="$t('test_track.report.test_result')" name="first">
        <functional-result :function-result="report.functionResult"/>
      </el-tab-pane>
      <el-tab-pane v-if="issueEnable && (isTemplate || isShare || hasPermission('PROJECT_TRACK_ISSUE:READ'))" name="second">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.issue_list')" :count="issueSize"/>
        </template>
        <functional-issue-list
          :is-db="isDb"
          :share-id="shareId"
          :is-share="isShare"
          :is-template="isTemplate"
          :report="report"
          :plan-id="planId"
          @setSize="setIssueSize"/>
      </el-tab-pane>
      <el-tab-pane name="third" v-if="allEnable">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.all_case')" :count="allSize"/>
        </template>
        <functional-cases
          :is-db="isDb"
          :share-id="shareId"
          :is-share="isShare"
          :is-template="isTemplate"
          :report="report"
          :plan-id="planId"
          :all-test-case="allTestCase"
          @setSize="setAllSize"/>
      </el-tab-pane>
      <el-tab-pane v-if="failureEnable" name="fourth">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.fail_case')" :count="failureSize"/>
        </template>
        <functional-cases
          filter-status="Failure"
          :is-db="isDb"
          :share-id="shareId"
          :is-share="isShare"
          :is-template="isTemplate"
          :report="report"
          :plan-id="planId"
          :all-test-case="allTestCase"
          @setSize="setFailureSize"/>
      </el-tab-pane>
      <el-tab-pane v-if="blockingEnable" name="fifth">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.plan_view.blocking') + $t('commons.track')" :count="blockingSize"/>
        </template>
        <functional-cases
          filter-status="Blocking"
          :is-db="isDb"
          :share-id="shareId"
          :is-share="isShare"
          :is-template="isTemplate"
          :report="report"
          :plan-id="planId"
          :all-test-case="allTestCase"
          @setSize="setBlockingSize"/>
      </el-tab-pane>
      <el-tab-pane v-if="skipEnable" name="sixth">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.plan_view.skip') + $t('commons.track')" :count="skipSize"/>
        </template>
        <functional-cases
          filter-status="Skip"
          :is-db="isDb"
          :share-id="shareId"
          :is-share="isShare"
          :is-template="isTemplate"
          :report="report"
          :plan-id="planId"
          :all-test-case="allTestCase"
          @setSize="setSkipSize"/>
      </el-tab-pane>
    </el-tabs>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import FunctionalResult from "@/business/components/track/plan/view/comonents/report/detail/component/FunctionalResult";
import FunctionalCases
  from "@/business/components/track/plan/view/comonents/report/detail/component/FunctionalCases";
import FunctionalIssueList
  from "@/business/components/track/plan/view/comonents/report/detail/component/FunctionalIssueList";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import TabPaneCount from "@/business/components/track/plan/view/comonents/report/detail/component/TabPaneCount";
import {hasPermission} from "@/common/js/utils";
import {
  getPlanFunctionAllCase,
  getSharePlanFunctionAllCase,
} from "@/network/test-plan";
export default {
  name: "TestPlanFunctionalReport",
  components: {
    TabPaneCount,
    TestPlanReportContainer, FunctionalIssueList, FunctionalCases, FunctionalResult, MsFormDivider},
  data() {
    return {
      activeName: 'first',
      failureSize: 0,
      issueSize: 0,
      skipSize: 0,
      blockingSize: 0,
      allSize: 0,
      allTestCase: []
    };
  },
  props: [
    'report','planId', 'isTemplate', 'isShare', 'shareId', 'config', 'isDb'
  ],
  computed: {
    resultEnable() {
      let disable = this.report.config && this.report.config.functional.children.result.enable === false;
      return !disable;
    },
    issueEnable() {
      let disable = this.report.config && this.report.config.functional.children.issue.enable === false;
      return !disable;
    },
    allEnable() {
      let disable = this.report.config && this.report.config.functional.children.all.enable === false;
      return !disable;
    },
    failureEnable() {
      let disable = this.report.config && this.report.config.functional.children.failure.enable === false;
      return !disable;
    },
    blockingEnable() {
      let disable = this.report.config && this.report.config.functional.children.blocking.enable === false;
      return !disable;
    },
    skipEnable() {
      let disable = this.report.config && this.report.config.functional.children.skip.enable === false;
      return !disable;
    },
  },
  mounted() {
    this.initActiveName();
    this.getAllFunctionalTestCase();
  },
  watch: {
    resultEnable() {
      this.initActiveName();
    },
    failureEnable() {
      this.initActiveName();
    },
    issueEnable() {
      this.initActiveName();
    },
    allEnable() {
      this.initActiveName();
    },
    'report.config'() {
      this.getAllFunctionalTestCase();
    }
  },
  methods: {
    initActiveName() {
      if (this.resultEnable) {
        this.activeName = 'first';
      }else if (this.issueEnable) {
        this.activeName = 'second';
      } else if (this.allEnable) {
        this.activeName = 'third';
      } else if (this.failureEnable) {
        this.activeName = 'fourth';
      } else if (this.blockingEnable) {
        this.activeName = 'fifth';
      } else if (this.skipEnable) {
        this.activeName = 'sixth';
      }
    },
    setFailureSize(size) {
      this.failureSize = size;
    },
    setBlockingSize(size) {
      this.blockingSize = size;
    },
    setSkipSize(size) {
      this.skipSize = size;
    },
    setIssueSize(size) {
      this.issueSize = size;
    },
    setAllSize(size) {
      this.allSize = size;
    },
    handleClick(tab, event) {
    },
    hasPermission,
    getAllFunctionalTestCase() {
      if (this.isTemplate || this.isDb) {
        this.allTestCase = this.report.functionAllCases ? this.report.functionAllCases : [];
      } else if (this.isShare) {
        let param = this.getStatusList();
        if (param) {
          getSharePlanFunctionAllCase(this.shareId, this.planId, param, (data) => {
            this.allTestCase = data;
          });
        }
      } else {
        let param = this.getStatusList();
        if (param) {
          getPlanFunctionAllCase(this.planId, param, (data) => {
            this.allTestCase = data;
          });
        }
      }
    },
    getStatusList() {
      let statusList = [];
      if (this.allEnable) {
        return statusList;
      }
      if (this.failureEnable) {
        statusList.push('Failure');
      }
      if (this.blockingEnable) {
        statusList.push('Blocking');
      }
      if (this.skipEnable) {
        statusList.push('Skip');
      }
      return statusList.length > 0 ? statusList : null;
    }
  }
}
</script>

<style scoped>

</style>
