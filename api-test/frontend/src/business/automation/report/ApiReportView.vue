<template>
  <ms-api-report
    :report-id="reportIdByPath"
    :share-id="shareId"
    :is-share="isShare"
    :is-plan="isPlanReport"
    :template-report="response"
    :is-template="isTemplate" />
</template>

<script>
import MsApiReport from '@/business/automation/report/ApiReportDetail';

export default {
  name: 'ApiReportView',
  components: { MsApiReport },
  computed: {
    reportIdByPath() {
      return this.getRouteParam('reportId');
    },
    isShare() {
      return this.getRouteParam('isShare');
    },
    shareId() {
      return this.getRouteParam('shareId');
    },
    isPlanReport() {
      return this.getRouteParam('isPlanReport');
    },
    isTemplate() {
      return this.getRouteParam('isTemplate');
    },
    response() {
      return this.getRouteParam('response');
    },
  },
  methods: {
    getRouteParam(name) {
      if (this.$route) {
        return this.$route.params[name];
      } else {
        return null;
      }
    },
  },
  destroyed() {
    this.$route.params ['reportId'] = null;
  },
};
</script>

<style scoped></style>
