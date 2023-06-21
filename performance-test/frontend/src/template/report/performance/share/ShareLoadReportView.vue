<template>
  <load-case-report-view :is-plan-report="isPlanReport"
                         :share-id="shareId"
                         :is-share="true"
                         :report-id="reportId"
                         ref="loadCaseReportView"/>
</template>

<script>
import LoadCaseReportView from "./LoadCaseReportView";

export default {
  name: "ShareLoadReportView",
  components: {LoadCaseReportView},
  computed: {
    reportId() {
      return this.getRouteParam('reportId');
    },
    shareId() {
      return this.getRouteParam('shareId');
    },
    isPlanReport() {
      return this.getRouteParam('isPlanReport');
    },
  },
  destroyed() {
    this.$route.params ['reportId'] = null;
  },
  methods: {
    getRouteParam(name) {
      if (this.$route) {
        return this.$route.params[name]
      } else {
        return null;
      }
    }
  }
}

</script>

<style scoped>

</style>
