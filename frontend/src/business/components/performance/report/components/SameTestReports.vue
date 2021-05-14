<template>
  <el-dialog :close-on-click-modal="false"
             :destroy-on-close="true"
             :title="$t('load_test.completed_test_report')" width="60%"
             :visible.sync="loadReportVisible">
    <el-table v-loading="reportLoadingResult.loading"
              class="basic-config"
              :data="tableData"
              @select-all="handleSelectAll"
              @select="handleSelectionChange">

      <el-table-column type="selection"/>
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
          <span class="last-modified">{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="getCompareReports" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleCompare"/>
    </template>
  </el-dialog>
</template>

<script>
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import ReportTriggerModeItem from "@/business/components/common/tableItem/ReportTriggerModeItem";
import {WORKSPACE_ID} from "@/common/js/constants";

export default {
  name: "SameTestReports",
  components: {ReportTriggerModeItem, MsDialogFooter, MsTablePagination},
  data() {
    return {
      loadReportVisible: false,
      reportLoadingResult: {},
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selectIds: new Set,
      report: {},
      compareReports: [],
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
      this.reportLoadingResult = this.$post('/performance/report/list/all/' + this.currentPage + "/" + this.pageSize, condition, res => {
        let data = res.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
      });
    },
    handleCompare() {
      let reportIds = [...this.selectIds];
      this.tableData
        .filter(r => reportIds.indexOf(r.id) > -1 && this.report.id !== r.id)
        .forEach(r => this.compareReports.push(r));

      localStorage.setItem("compareReports", JSON.stringify(this.compareReports));
      this.close();
      this.$router.push({path: '/performance/report/compare/' + reportIds[0]});
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.tableData.forEach(item => {
          this.selectIds.add(item.id);
        });
      } else {
        this.tableData.forEach(item => {
          if (this.selectIds.has(item.id)) {
            this.selectIds.delete(item.id);
          }
        });
      }
    },
    handleSelectionChange(selection, row) {
      if (this.selectIds.has(row.id)) {
        this.selectIds.delete(row.id);
      } else {
        this.selectIds.add(row.id);
      }
    },
  }
}
</script>

<style scoped>

</style>
