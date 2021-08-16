<template>
  <div>
    <el-main>
      <test-plan-detail-report :is-template="true" :test-plan="testPlan"/>
    </el-main>
  </div>
</template>

<script>
import TestPlanDetailReport from "@/business/components/track/plan/view/comonents/report/TestPlanDetailReport";
export default {
  name: "PlanReportTemplate",
  components: {TestPlanDetailReport},
  data() {
    return {
      testPlan: {"id": "96791ef8-8a75-4335-a48c-832e0ddece5f"}
    }
  },

  methods: {

  }
}
</script>

<style scoped>
</style>
