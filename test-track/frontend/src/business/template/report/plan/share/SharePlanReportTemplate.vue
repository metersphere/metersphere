<template>
  <test-plan-report-content :share-id="shareId" :report-id="reportId" :is-share="true" :is-db="isDb" :plan-id="planId"/>
</template>

<script>
import {getShareId} from "@/api/share";
import {getShareInfo} from "@/api/share";
import TestPlanReportContent from "@/business/plan/view/comonents/report/detail/TestPlanReportContent";
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
    getShareInfo(this.shareId)
      .then((response) => {
        let data = response.data;
        if (!data) {
          this.$error('连接已失效，请重新获取!');
          return;
        }
        if (data.lang) {
          this.$setLang(data.lang);
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
