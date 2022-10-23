<template>
  <el-dialog :close-on-click-modal="false"
             :destroy-on-close="true"
             :title="$t('load_test.completed_test_report')" width="60%"
             v-loading="loading"
             :show-close="false"
             :visible.sync="loadReportVisible">

    <el-header class="header-btn">
      <ms-dialog-header :enable-cancel="true" @cancel="close" @confirm="handleCompare" btn-size="mini">
      </ms-dialog-header>
    </el-header>

    <ms-table
      :data="tableData"
      :show-select-all="false"
      :screen-height="screenHeight"
      ref="table"
    >
      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <i v-if="scope.row.id === report.id" class="el-icon-star-on"></i> {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column
        prop="userName"
        :label="$t('report.user_name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column prop="triggerMode"
                       :label="$t('test_track.report.list.trigger_mode')">
        <template v-slot:default="scope">
          <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <i class="el-icon-time"/>
          <span class="last-modified">{{ scope.row.createTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
    </ms-table>
    <ms-table-pagination :change="getCompareReports" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
  </el-dialog>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import ReportTriggerModeItem from "metersphere-frontend/src/components/tableItem/ReportTriggerModeItem";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsDialogHeader from "metersphere-frontend/src/components/MsDialogHeader";
import {searchReports} from "@/api/report";

export default {
  name: "SameTestReports",
  components: {MsDialogHeader, MsTable, ReportTriggerModeItem, MsDialogFooter, MsTablePagination},
  data() {
    return {
      loadReportVisible: false,
      loading: false,
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      report: {},
      compareReports: [],
      screenHeight: 'calc(100vh - 400px)',
    }
  },
  methods: {
    open(report) {
      this.report = report;
      this.compareReports = [];
      this.getCompareReports(report);

      this.compareReports.push(report);

      this.loadReportVisible = true;
    },
    close() {
      this.loadReportVisible = false;
    },
    getCompareReports() {
      let condition = {
        testId: this.report.testId,
        filters: {status: ["Completed"]}
      };
      this.loading = searchReports(this.currentPage, this.pageSize, condition)
        .then(res => {
          let data = res.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
        })
    },
    handleCompare() {

      let reportIds = [...this.$refs.table.selectIds];
      this.tableData
        .filter(r => reportIds.indexOf(r.id) > -1 && this.report.id !== r.id)
        .forEach(r => this.compareReports.push(r));

      sessionStorage.setItem("compareReports", JSON.stringify(this.compareReports));
      this.close();
      this.$router.push({path: '/performance/report/compare/' + reportIds[0]});
    },
  }
}
</script>

<style scoped>
.header-btn {
  position: absolute;
  top: 20px;
  right: 0;
  padding: 0;
  background: 0 0;
  border: none;
  outline: 0;
  cursor: pointer;
  height: 30px;
}
</style>
