<template>
  <ms-api-report
    :report-id="reportId"
    :share-id="shareId"
    :is-share="isShare"
    :is-plan="true"
    :show-cancel-button="false"></ms-api-report>
</template>

<script>
import { getShareId } from '@/api/share';
import { getShareInfo } from '@/api/share';
import MsApiReport from '@/business/automation/report/ApiReportDetail';

export default {
  name: 'ShareApiReportTemplate',
  components: { MsApiReport },
  data() {
    return {
      reportId: '',
      shareId: '',
      isShare: true,
    };
  },
  created() {
    this.shareId = getShareId();
    getShareInfo(this.shareId).then((res) => {
      if (!res.data) {
        this.$error('连接已失效，请重新获取!');
        return;
      }
      if (res.data.shareType === 'API_REPORT') {
        this.reportId = res.data.customData;
      }
    });
  },
};
</script>

<style scoped></style>
