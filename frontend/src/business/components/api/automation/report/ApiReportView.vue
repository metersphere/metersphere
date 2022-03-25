<template>
  <ms-api-report :report-id="reportId"></ms-api-report>
</template>

<script>
import MsApiReport from "@/business/components/api/automation/report/ApiReportDetail";
export default {
  name: "ApiReportView",
  components: {MsApiReport},
   computed: {
     reportId:function (){
       return this.$route.params.reportId
     }
   },
  created() {
    console.log(this.reportId)
  }
}

</script>

<style scoped>

</style>
