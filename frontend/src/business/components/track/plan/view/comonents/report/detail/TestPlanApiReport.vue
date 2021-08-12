<template>
  <test-plan-report-container :title="'接口用例统计'">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="测试结果" name="first">
        <api-result :api-result="report.apiResult"/>
      </el-tab-pane>
      <el-tab-pane label="失败用例" name="second">
        <api-failure-result :plan-id="planId"/>
      </el-tab-pane>

<!--      <el-tab-pane label="所有用例" name="fourth">所有用例</el-tab-pane>-->
    </el-tabs>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import ApiResult from "@/business/components/track/plan/view/comonents/report/detail/component/ApiResult";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import ApiFailureResult from "@/business/components/track/plan/view/comonents/report/detail/component/ApiFailureResult";
export default {
  name: "TestPlanApiReport",
  components: {ApiFailureResult, TestPlanReportContainer, ApiResult, MsFormDivider},
  data() {
    return {
      activeName: 'first'
    };
  },
  props: [
    'report', 'planId'
  ],
  methods: {
    handleClick(tab, event) {
    }
  }

}
</script>

<style scoped>

</style>
