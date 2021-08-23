<template>
  <test-plan-report-container :title="'功能用例统计'">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane v-if="resultEnable" label="测试结果" name="first">
        <functional-result :function-result="report.functionResult"/>
      </el-tab-pane>
      <el-tab-pane v-if="failureEnable" label="失败用例" name="second">
        <functional-cases :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId"/>
      </el-tab-pane>
      <el-tab-pane v-if="issueEnable" label="缺陷列表" name="third">
        <functional-issue-list :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId"/>
      </el-tab-pane>
      <el-tab-pane label="所有用例" name="fourth">
        <functional-cases v-if="allEnable" :is-all="true" :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId"/>
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
export default {
  name: "TestPlanFunctionalReport",
  components: {TestPlanReportContainer, FunctionalIssueList, FunctionalCases, FunctionalResult, MsFormDivider},
  data() {
    return {
      activeName: 'first'
    };
  },
  props: [
    'report','planId', 'isTemplate', 'isShare', 'shareId', 'config'
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
    handleClick(tab, event) {
    }
  }

}
</script>

<style scoped>

</style>
