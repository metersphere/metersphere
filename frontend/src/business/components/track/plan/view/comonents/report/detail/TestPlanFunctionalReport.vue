<template>
  <test-plan-report-container id="functional" :title="'功能用例统计分析'">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane v-if="resultEnable" label="测试结果" name="first">
        <functional-result :function-result="report.functionResult"/>
      </el-tab-pane>
      <el-tab-pane v-if="failureEnable" name="second">
        <template v-slot:label>
          <tab-pane-count title="失败用例"  :count="failureSize"/>
        </template>
        <functional-cases :is-db="isDb" :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId" @setSize="setFailureSize"/>
      </el-tab-pane>
      <el-tab-pane v-if="issueEnable" name="third">
        <template v-slot:label>
          <tab-pane-count title="缺陷列表" :count="issueSize"/>
        </template>
        <functional-issue-list :is-db="isDb" :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId" @setSize="setIssueSize"/>
      </el-tab-pane>
      <el-tab-pane name="fourth" v-if="allEnable">
        <template v-slot:label>
          <tab-pane-count title="所有用例" :count="allSize"/>
        </template>
        <functional-cases :is-db="isDb" :is-all="true" :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId" @setSize="setAllSize"/>
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
      allSize: 0,
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
    failureEnable() {
      let disable = this.report.config && this.report.config.functional.children.failure.enable === false;
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
  },
  mounted() {
    this.initActiveName();
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
  },
  methods: {
    initActiveName() {
      if (this.resultEnable) {
        this.activeName = 'first';
      } else if (this.failureEnable) {
        this.activeName = 'second';
      } else if (this.issueEnable) {
        this.activeName = 'third';
      } else if (this.allEnable) {
        this.activeName = 'fourth';
      }
    },
    setFailureSize(size) {
      this.failureSize = size;
    },
    setIssueSize(size) {
      this.issueSize = size;
    },
    setAllSize(size) {
      this.allSize = size;
    },
    handleClick(tab, event) {
    }
  }

}
</script>

<style scoped>

</style>
