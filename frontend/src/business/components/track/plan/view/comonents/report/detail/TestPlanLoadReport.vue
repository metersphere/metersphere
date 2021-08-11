<template>
  <test-plan-report-container :title="'性能用例数'">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="测试结果" name="first">
        <load-result :load-result="report.loadResult"/>
      </el-tab-pane>
      <el-tab-pane label="失败用例" name="second">
        <load-failure-result :plan-id="planId"/>
      </el-tab-pane>
<!--      <el-tab-pane label="所有用例" name="fourth">所有用例</el-tab-pane>-->
    </el-tabs>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import LoadResult from "@/business/components/track/plan/view/comonents/report/detail/component/LoadResult";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import LoadFailureResult
  from "@/business/components/track/plan/view/comonents/report/detail/component/LoadFailureResult";

export default {
  name: "TestPlanLoadReport",
  components: {
    LoadFailureResult,
    TestPlanReportContainer,
    LoadResult, MsFormDivider},
  data() {
    return {
      activeName: 'first'
    };
  },
  props: [
    'report',
    'planId'
  ],
  methods: {
    handleClick(tab, event) {
    }
  }

}
</script>

<style scoped>

</style>
