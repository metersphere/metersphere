<template>
  <div>
    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('commons.report_statistics.report_data.all_report')" name="allReport">
        <history-report-data-card :report-data="allReportData" :show-options-button="false" @deleteReport="deleteReport"
                                  @selectReport="selectReport"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('commons.report_statistics.report_data.my_report')" name="myReport">
        <history-report-data-card :report-data="myReportData" :show-options-button="true" @deleteReport="deleteReport"
                                  @selectReport="selectReport"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import {getCurrentProjectID, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import HistoryReportDataCard from "./compose/HistoryReportDataCard";
import {deleteHistoryReportByParams, selectHistoryReportByParams} from "@/api/history-report";

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
  props: {
    reportType: String
  },
  created() {
    this.initReportData();
  },
  watch: {
    activeName() {
      this.initReportData();
    }
  },
  methods: {
    initReportData() {
      let projectId = getCurrentProjectID();
      let userId = getCurrentUserId();
      this.allReportData = [];
      this.myReportData = [];

      let paramsObj = {
        projectId: getCurrentProjectID(),
        reportType: this.reportType,
      };
      selectHistoryReportByParams(paramsObj).then(request => {
        request.data.forEach(item => {
          if (item) {
            this.allReportData.push(item);
            if (item.createUser === userId) {
              this.myReportData.push(item);
            }
          }
        });
      });
    },
    deleteReport(deleteId) {
      let paramObj = {
        id: deleteId
      }
      deleteHistoryReportByParams(paramObj).then(() => {
        this.initReportData();
        this.$emit("removeHistoryReportId");
      });
    },
    selectReport(id) {
      this.$emit("selectReport", id);
    }
  },
}
</script>

<style scoped>

.historyCard {
  border: 0px;
}

:deep(.el-card__header ) {
  border: 0px;
}

</style>
