<template>
  <div>
    <div class="container">
      <el-main>
        <test-plan-report-content :is-template="isTemplate" :is-share="isShare" :plan-id="planId" ref="reportContent"/>
      </el-main>
    </div>
  </div>
</template>

<script>
import TestPlanReportContent from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContent";

export default {
  name: "TestPlanDetailReport",
  components: {
    TestPlanReportContent,
  },
  computed: {
    planId() {
      return this.testPlan.id;
    },
  },
  props: ['testPlan', 'isTemplate', 'isShare'],
}
</script>

<style scoped>
</style>
