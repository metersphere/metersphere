<template>
  <ms-api-report :report-id="reportId" :share-id="shareId" :is-share="isShare" :is-plan="true" :show-cancel-button="false" ></ms-api-report>
</template>

<script>
import {getShareId} from "@/common/js/utils";
import {getShareInfo} from "@/network/share";
import MsApiReport from "@/business/components/api/automation/report/ApiReportDetail";

export default {
  name: "ShareApiReportTemplate",
  components: {MsApiReport},
  data() {
    return {
      reportId: '',
      shareId: '',
      isShare: true,
    };
  },
  created() {
    this.shareId = getShareId();
    getShareInfo(this.shareId, (data) => {
      if (!data) {
        this.$error('连接已失效，请重新获取!');
        return;
      }
      if (data.shareType === 'API_REPORT') {
        this.reportId = data.customData;
      }
    });
  },
};
</script>

<style scoped>
</style>
