<template>
  <test-plan-report-content :share-id="shareId" :is-share="true" :plan-id="planId"/>
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
      visible: false,
      shareId: ''
    }
  },
  created() {
    this.shareId = getShareId();
    getShareInfo(this.shareId, (data) => {
      this.planId = data.customData;
      this.visible = true;
    });
  },
}
</script>

<style scoped>
</style>
