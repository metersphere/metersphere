<template>
  <UiShareReportDetail :report-id="reportId" :share-id="shareId" :is-share="isShare" :is-plan="true" :show-cancel-button="false" ></UiShareReportDetail>
</template>

<script>
import {getShareId} from "@/common/js/utils";
import {getShareInfo} from "@/network/share";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const UiShareReportDetail = requireComponent.keys().length > 0 ? requireComponent("./ui/automation/report/UiShareReportDetail.vue") : {};
export default {
  name: "ShareUiReportTemplate",
  components: {'UiShareReportDetail' : UiShareReportDetail.default},
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
        this.$error('链接已失效，请重新获取!');
        return;
      }
      if (data.shareType === 'UI_REPORT') {
        this.reportId = data.customData;
      }
    });
  },
};
</script>

<style scoped>
</style>
