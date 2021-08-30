<template>
  <div>
    <el-main>
      <test-plan-report-content :is-template="true" :plan-id="testPlan.planId"/>
    </el-main>
  </div>
</template>

<script>
import TestPlanReportContent from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContent";
export default {
  name: "PlanReportTemplate",
  components: {TestPlanReportContent},
  data() {
    return {
      testPlan: {"id": "96791ef8-8a75-4335-a48c-832e0ddece5f"}
    }
  },
}
</script>

<style scoped>
</style>
