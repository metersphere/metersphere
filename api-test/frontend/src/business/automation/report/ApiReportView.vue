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
        if (name === 'reportId') {
          // 测试计划模块使用qiankun加载该页面时，会在reportId前面加上UUID和'[TEST-PLAN-REDIRECT]'分隔符
          let reportId = this.$route.params[name];
          if (reportId && reportId.indexOf('[TEST-PLAN-REDIRECT]') > -1) {
            return reportId.split('[TEST-PLAN-REDIRECT]')[1];
          } else {
            return reportId;
          }
        } else {
          return this.$route.params[name];
        }
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
