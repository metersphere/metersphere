<template>
  <div>
    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('commons.report_statistics.report_data.all_report')" name="allReport">
        <history-report-data-card :report-data="allReportData" :show-options-button="false" @deleteReport="deleteReport" @selectReport="selectReport"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('commons.report_statistics.report_data.my_report')" name="myReport">
        <history-report-data-card :report-data="myReportData" :show-options-button="true" @deleteReport="deleteReport" @selectReport="selectReport"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
  import {getCurrentProjectID,getCurrentUserId} from "@/common/js/utils";
  import HistoryReportDataCard from "./compose/HistoryReportDataCard";
  export default {
    name: "HistoryReportData",
    components: {HistoryReportDataCard},
    data() {
      return {
        activeName: 'allReport',
        allReportData: [],
        myReportData: [],
      }
    },
    props:{
      reportType:String
    },
    created(){
      this.initReportData();
    },
    watch :{
      activeName(){
        this.initReportData();
      }
    },
    methods: {
      initReportData(){
        let projectId = getCurrentProjectID();
        let userId =  getCurrentUserId();
        this.allReportData = [];
        this.myReportData = [];

        let paramsObj = {
          projectId:getCurrentProjectID(),
          reportType:this.reportType,
        };
        this.$post('/history/report/selectByParams',paramsObj, response => {
          let allData = response.data;
          allData.forEach(item => {
            if(item){
              this.allReportData.push(item);
              if(item.createUser === userId){
                this.myReportData.push(item);
              }
            }
          });
        });
      },
      deleteReport(deleteId){
        let paramObj = {
          id:deleteId
        }
        this.$post('/history/report/deleteByParam',paramObj, response => {
          this.initReportData();
        });
        this.$emit("removeHistoryReportId");
      },
      selectReport(id){
        this.$emit("selectReport",id);
      }
    },
}
</script>

<style scoped>

.historyCard{
  border: 0px;
}
/deep/ .el-card__header{
  border: 0px;
}

</style>
