<template>
  <test-plan-report-container :title="'功能用例统计'">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="测试结果" name="first">
        <functional-result :function-result="report.functionResult"/>
      </el-tab-pane>
      <el-tab-pane label="失败用例" name="second">
        <functional-failure-result :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId"/>
      </el-tab-pane>
      <el-tab-pane label="缺陷列表" name="third">
        <functional-issue-list :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId"/>
      </el-tab-pane>
<!--      <el-tab-pane label="所有用例" name="fourth">所有用例</el-tab-pane>-->
    </el-tabs>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import FunctionalResult from "@/business/components/track/plan/view/comonents/report/detail/component/FunctionalResult";
import FunctionalFailureResult
  from "@/business/components/track/plan/view/comonents/report/detail/component/FunctionalFailureResult";
import FunctionalIssueList
  from "@/business/components/track/plan/view/comonents/report/detail/component/FunctionalIssueList";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
export default {
  name: "TestPlanFunctionalReport",
  components: {TestPlanReportContainer, FunctionalIssueList, FunctionalFailureResult, FunctionalResult, MsFormDivider},
  data() {
    return {
      activeName: 'first'
    };
  },
  props: [
    'report','planId', 'isTemplate', 'isShare', 'shareId'
  ],
  methods: {
    handleClick(tab, event) {
    }
  }

}
</script>

<style scoped>

</style>
