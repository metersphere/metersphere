<template>
  <el-card class="table-card" v-loading="loading">
    <template v-slot:header>
      <ms-table-header
        :condition.sync="condition"
        :show-create="false"
        @search="initTableData"
      />
    </template>

    <ms-table
      v-loading="loading"
      operator-width="110px"
      row-key="id"
      :data="tableData"
      :condition="condition"
      :total="total"
      :page-size.sync="pageSize"
      :operators="operators"
      :screen-height="screenHeight"
      :batch-operators="batchButtons"
      :remember-order="true"
      :fields.sync="fields"
      :field-key="tableHeaderKey"
      @handlePageChange="initTableData"
      @refresh="initTableData"
      ref="testPlanReportTable"
    >
      <span v-for="item in fields" :key="item.key">
        <ms-table-column
          prop="name"
          sortable="custom"
          permission="PROJECT_TRACK_REPORT:READ+DELETE"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.report.list.name')"
          :show-overflow-tooltip="false"
          :editable="true"
          :edit-content="$t('report.rename_report')"
          @editColumn="openReNameDialog"
          min-width="200px"
        >
        </ms-table-column>

        <ms-table-column
          prop="testPlanName"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.report.list.test_plan')"
          min-width="160"
        />

        <ms-table-column
          prop="creator"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.report.list.creator')"
        />

        <ms-table-column
          prop="createTime"
          :field="item"
          :fields-width="fieldsWidth"
          sortable="custom"
          min-width="180"
          :label="$t('test_track.report.list.create_time')"
          :show-overflow-tooltip="true"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="triggerMode"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="120"
          sortable="custom"
          :label="$t('test_track.report.list.trigger_mode')"
        >
          <template v-slot:default="scope">
            <report-trigger-mode-item :trigger-mode="scope.row.triggerMode" />
          </template>
        </ms-table-column>

        <ms-table-column
          prop="status"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100"
          sortable="custom"
          :label="$t('commons.status')"
        >
          <template v-slot:default="scope">
            <ms-tag
              v-if="scope.row.status == 'RUNNING'"
              type="primary"
              effect="plain"
              :content="'Running'"
            />
            <ms-tag
              v-else-if="
                scope.row.status == 'COMPLETED' ||
                scope.row.status == 'SUCCESS' ||
                scope.row.status == 'FAILED'
              "
              type="info"
              effect="plain"
              :content="'Completed'"
            />
            <ms-tag
              v-else
              type="info"
              effect="plain"
              :content="scope.row.status"
            />
          </template>
        </ms-table-column>

        <ms-table-column
          prop="runTime"
          :field="item"
          :fields-width="fieldsWidth"
          sortable="custom"
          :label="$t('test_track.report.list.run_time')"
          min-width="120"
        >
          <template v-slot:default="scope">
            <span v-if="scope.row.endTime != null">{{
              (scope.row.runTime / 1000).toFixed(2)
            }}</span>
            <span v-else>/</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="passRate"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.report.list.pass_rate')"
        >
          <template v-slot:default="scope">
            <span>{{
              (scope.row.passRate ? (scope.row.passRate * 100).toFixed(1) : 0) +
              "%"
            }}</span>
          </template>
        </ms-table-column>
      </span>
    </ms-table>

    <ms-table-pagination
      :change="initTableData"
      :current-page.sync="currentPage"
      :page-size.sync="pageSize"
      :total="total"
    />
    <test-plan-db-report ref="dbReport" />
    <ms-rename-report-dialog
      ref="renameDialog"
      @submit="rename"
    ></ms-rename-report-dialog>
  </el-card>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import { TEST_PLAN_REPORT_CONFIGS } from "metersphere-frontend/src/components/search/search-components";
import ReportTriggerModeItem from "metersphere-frontend/src/components/tableItem/ReportTriggerModeItem";
import MsTag from "metersphere-frontend/src/components/MsTag";
import ShowMoreBtn from "metersphere-frontend/src/components/table/ShowMoreBtn";
import {
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  initCondition,
} from "metersphere-frontend/src/utils/tableUtils";
import MsTableHeaderSelectPopover from "metersphere-frontend/src/components/table/MsTableHeaderSelectPopover";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";
import TestPlanDbReport from "@/business/report/components/TestPlanDbReport";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsRenameReportDialog from "metersphere-frontend/src/components/report/MsRenameReportDialog";
import {
  testPlanReportBatchDelete,
  testPlanReportDelete,
  testPlanReportGetDb,
  testPlanReportList,
  testPlanReportReName,
} from "@/api/remote/plan/test-plan-report";

export default {
  name: "TestPlanReportList",
  components: {
    TestPlanDbReport,
    MsTableHeaderSelectPopover,
    MsTableOperator,
    MsTableOperatorButton,
    MsTableHeader,
    MsTablePagination,
    ReportTriggerModeItem,
    MsTag,
    ShowMoreBtn,
    MsTableColumn,
    MsTable,
    MsRenameReportDialog,
  },
  data() {
    return {
      loading: false,
      enableDeleteTip: false,
      tableHeaderKey: "TRACK_REPORT_TABLE",
      condition: {
        components: TEST_PLAN_REPORT_CONFIGS,
      },
      currentPage: 1,
      pageSize: 10,
      isTestManagerOrTestUser: false,
      selectRows: new Set(),
      screenHeight: "calc(100vh - 200px)", //屏幕高度
      total: 0,
      tableData: [],
      statusFilters: [
        {
          text: this.$t("test_track.plan.plan_status_prepare"),
          value: "Prepare",
        },
        {
          text: this.$t("test_track.plan.plan_status_running"),
          value: "Underway",
        },
        {
          text: this.$t("test_track.plan.plan_status_completed"),
          value: "Completed",
        },
      ],
      stageFilters: [
        { text: this.$t("test_track.plan.smoke_test"), value: "smoke" },
        { text: this.$t("test_track.plan.system_test"), value: "system" },
        {
          text: this.$t("test_track.plan.regression_test"),
          value: "regression",
        },
      ],
      buttons: [
        {
          name: this.$t("api_test.definition.request.batch_delete"),
          handleClick: this.handleDeleteBatch,
          permissions: ["PROJECT_TRACK_REPORT:READ+DELETE"],
        },
      ],
      selectDataCounts: 0,

      fields: getCustomTableHeader("TRACK_REPORT_TABLE"),
      fieldsWidth: getCustomTableWidth("TRACK_REPORT_TABLE"),
      operators: [],
      batchButtons: [],
      publicButtons: [
        {
          name: this.$t("api_test.definition.request.batch_delete"),
          handleClick: this.handleDeleteBatch,
          permissions: ["PROJECT_TRACK_REPORT:READ+DELETE"],
        },
      ],
      simpleOperators: [
        {
          tip: this.$t("test_track.plan_view.view_report"),
          icon: "el-icon-document",
          exec: this.openReport,
        },
        {
          tip: this.$t("commons.delete"),
          icon: "el-icon-delete",
          type: "danger",
          exec: this.handleDelete,
          permissions: ["PROJECT_TRACK_REPORT:READ+DELETE"],
        },
      ],
    };
  },
  watch: {
    $route(to, from) {},
  },
  created() {
    window.addEventListener("resize", this.resizeTable, false);
  },
  activated() {
    this.init();
  },
  methods: {
    resizeTable() {
      if (this.$refs.testPlanReportTable) {
        this.$refs.testPlanReportTable.doLayout();
      }
    },
    init() {
      this.projectId = this.$route.params.projectId;
      this.batchButtons = this.publicButtons;
      this.operators = this.simpleOperators;
      if (!this.projectId) {
        this.projectId = getCurrentProjectID();
      }
      this.isTestManagerOrTestUser = true;

      this.initTableData();

      // 通知过来的数据跳转到报告
      if (this.$route.query.resourceId) {
        testPlanReportGetDb(this.$route.query.resourceId).then((response) => {
          this.$refs.dbReport.open(response.data);
        });
      }
    },
    initTableData() {
      initCondition(this.condition, this.condition.selectAll);
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);

      this.selectRows = new Set();
      if (this.planId) {
        this.condition.planId = this.planId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        this.condition.nodeIds = this.selectNodeIds;
      }
      if (!this.projectId) {
        return;
      }
      this.condition.projectId = getCurrentProjectID();
      this.formatCondition();
      this.loading = true;
      testPlanReportList(
        { pageNum: this.currentPage, pageSize: this.pageSize },
        this.condition
      ).then((response) => {
        this.loading = false;
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
      });
    },
    handleDelete(testPlanReport) {
      this.$alert(
        this.$t("report.delete_confirm") + " " + testPlanReport.name + " ？",
        "",
        {
          confirmButtonText: this.$t("commons.confirm"),
          callback: (action) => {
            if (action === "confirm") {
              let testPlanReportIdList = [testPlanReport.id];
              testPlanReportDelete(testPlanReportIdList).then(() => {
                this.$success(this.$t("commons.delete_success"));
                this.initTableData();
              });
            }
          },
        }
      );
    },
    handleDeleteBatch() {
      this.$alert(this.$t("report.delete_batch_confirm") + " " + " ？", "", {
        confirmButtonText: this.$t("commons.confirm"),
        callback: (action) => {
          if (action === "confirm") {
            let deleteParam = {};
            let ids = this.$refs.testPlanReportTable.selectIds;
            deleteParam.dataIds = ids;
            deleteParam.projectId = this.projectId;
            deleteParam.selectAllDate = this.condition.selectAll;
            deleteParam.unSelectIds = this.condition.unSelection;
            deleteParam = Object.assign(deleteParam, this.condition);
            testPlanReportBatchDelete(deleteParam).then(() => {
              this.$success(this.$t("commons.delete_success"));
              this.initTableData();
              this.selectRows.clear();
            });
          }
        },
      });
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets);
      let ids = rowArray.map((s) => s.id);
      return ids;
    },
    openReport(report) {
      if (report.id) {
        if (report.isNew) {
          this.$refs.dbReport.open(report);
        } else {
          this.$refs.testPlanReportView.open(report.id);
        }
      }
    },
    openReNameDialog($event) {
      this.$refs.renameDialog.open($event);
    },
    rename(data) {
      testPlanReportReName(data).then(() => {
        this.$success(this.$t("organization.integration.successful_operation"));
        this.initTableData();
        this.$refs.renameDialog.close();
      });
    },
    formatCondition() {
      if (
        this.condition &&
        this.condition.combine &&
        this.condition.combine.status &&
        this.condition.combine.status.value
      ) {
        let formatValueArr = [];
        this.condition.combine.status.value.forEach((item) => {
          if (item === "Underway") {
            formatValueArr.push("RUNNING");
          } else {
            formatValueArr.push(item);
          }
        });
        this.condition.combine.status.value = formatValueArr;
      }
    },
  },
};
</script>

<style scoped>
.table-page {
  padding-top: 20px;
  margin-right: -9px;
  float: right;
}

.el-table {
  cursor: pointer;
}

.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}

.request-method {
  padding: 0 5px;
  color: #1e90ff;
}
</style>
