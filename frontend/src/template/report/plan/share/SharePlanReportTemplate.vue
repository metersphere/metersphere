<template>
  <test-plan-report-content :share-id="shareId" :report-id="reportId" :is-share="true" :is-db="isDb" :plan-id="planId"/>
</template>

<script>
import {getShareId} from "@/common/js/utils";
import {getShareInfo} from "@/network/share";
import TestPlanReportContent from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContent";
export default {
  name: "SharePlanReportTemplate",
  components: {TestPlanReportContent},
  data() {
    return {
      planId: '',
      reportId: '',
      visible: false,
      shareId: '',
      isDb: false
    }
  },
  created() {
    this.shareId = getShareId();
    getShareInfo(this.shareId, (data) => {
      if (!data) {
        this.$error('连接已失效，请重新获取!');
        return;
      }
      if (data.shareType === 'PLAN_REPORT') {
        this.planId = data.customData;
      } else if (data.shareType === 'PLAN_DB_REPORT') {
        this.reportId = data.customData;
        this.isDb = true;
      }
      this.visible = true;
    });
  },
}
</script>

<style scoped>
</style>
