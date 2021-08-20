<template>
  <div>
    <el-main>
      <test-plan-detail-report v-if="visible" :is-share="true" :test-plan="testPlan"/>
    </el-main>
  </div>
</template>

<script>
import TestPlanDetailReport from "@/business/components/track/plan/view/comonents/report/TestPlanDetailReport";
import {getShareId} from "@/common/js/utils";
import {getShareInfo} from "@/network/share";
export default {
  name: "SharePlanReportTemplate",
  components: {TestPlanDetailReport},
  data() {
    return {
      testPlan: {"id": "96791ef8-8a75-4335-a48c-832e0ddece5f"},
      visible: false
    }
  },
  created() {
    let shareId = getShareId();
    getShareInfo(shareId, (data) => {
      this.testPlan.id = data.customData;
      this.visible = true;
    });
  },
}
</script>

<style scoped>
</style>
