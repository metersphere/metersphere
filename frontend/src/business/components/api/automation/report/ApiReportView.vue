<template>
  <ms-api-report :report-id="reportIdValue" :share-id="shareId" :is-share="isShare" :is-plan="isPlanReport" :is-template="false"></ms-api-report>
</template>

<script>
import MsApiReport from "@/business/components/api/automation/report/ApiReportDetail";
export default {
  name: "ApiReportView",
  components: {MsApiReport},
  props: {
    reportId:String,
    isShare: Boolean,
    shareId: String,
    isPlanReport: Boolean,
  },
  computed: {
    reportIdByPath:function (){
      if(this.$route){
        return this.$route.params.reportId
      }else{
        return null;
      }
    }
  },
  data(){
    return {
      reportIdValue:'',
    }
  },
  created() {
    console.log("this.reportId")
    console.log(this.reportId)
    if(!this.reportIdValue){
      this.reportIdValue = this.reportIdByPath ? this.reportIdByPath:this.reportId;
    }
  }
}

</script>

<style scoped>

</style>
