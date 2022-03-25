<template>
  <ms-api-report :report-id="reportIdByPath" :share-id="shareId" :is-share="isShare" :is-plan="isPlanReport" :is-template="false"></ms-api-report>
</template>

<script>
import MsApiReport from "@/business/components/api/automation/report/ApiReportDetail";

export default {
  name: "ApiReportView",
  components: {MsApiReport},
  props: {
    reportId: String,
    isShare: Boolean,
    shareId: String,
    isPlanReport: Boolean,
  },
  computed: {
    reportIdByPath: function () {
      if (this.$route) {
        return this.$route.params.reportId
      } else {
        return null;
      }
    }
  },
}

</script>

<style scoped>

</style>
