<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <ms-table-header :condition.sync="condition" :show-create="false"
                       @search="initTableData"/>
    </template>

    <ms-table
      v-loading="result.loading"
      operator-width="170px"
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
      ref="testPlanReportTable">

      <span v-for="item in fields" :key="item.key">

      <ms-table-column
        prop="name"
        :field="item"
        :fields-width="fieldsWidth"
        sortable
        :label="$t('test_track.report.list.name')"
        :show-overflow-tooltip="false"
        :editable="true"
        :edit-content="$t('report.rename_report')"
        @editColumn="openReNameDialog"
        min-width="200px">
      </ms-table-column>

      <ms-table-column
        prop="testPlanName"
        :field="item"
        :fields-width="fieldsWidth"
        :label="$t('test_track.report.list.test_plan')"
        min-width="100"/>

      <ms-table-column
        prop="creator"
        :field="item"
        :fields-width="fieldsWidth"
        :label="$t('test_track.report.list.creator')"/>

      <ms-table-column
        prop="createTime"
        :field="item"
        :fields-width="fieldsWidth"
        sortable
        :label="$t('test_track.report.list.create_time')"
        min-width="150px">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="triggerMode"
        :field="item"
        :fields-width="fieldsWidth"
        sortable
        :label="$t('test_track.report.list.trigger_mode')">
        <template v-slot:default="scope">
          <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="status"
        :field="item"
        :fields-width="fieldsWidth"
        sortable
        :label="$t('commons.status')">
        <template v-slot:default="scope">
          <ms-tag v-if="scope.row.status == 'RUNNING'" type="success" effect="plain" :content="'Running'"/>
          <ms-tag
            v-else-if="scope.row.status == 'COMPLETED'||scope.row.status == 'SUCCESS'||scope.row.status == 'FAILED'"
            type="info" effect="plain" :content="'Completed'"/>
          <ms-tag v-else type="effect" effect="plain" :content="scope.row.status"/>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="runTime"
        :field="item"
        :fields-width="fieldsWidth"
        sortable="custom"
        :label="$t('test_track.report.list.run_time')">
        <template v-slot:default="scope">
          <span v-if="scope.row.endTime != null">{{ (scope.row.runTime / 1000).toFixed(2) }}</span>
          <span v-else>/</span>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="passRate"
        :field="item"
        :fields-width="fieldsWidth"
        :label="$t('test_track.report.list.pass_rate')">
        <template v-slot:default="scope">
          <span>{{ (scope.row.passRate ? (scope.row.passRate  * 100 ).toFixed(1) : 0) + '%'}}</span>
        </template>
      </ms-table-column>

      </span>
    </ms-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <test-plan-report-view @refresh="initTableData" ref="testPlanReportView"/>
    <test-plan-db-report ref="dbReport"/>
    <ms-rename-report-dialog ref="renameDialog" @submit="rename"></ms-rename-report-dialog>
  </el-card>
</template>

<script>
import MsTablePagination from '../../../../components/common/pagination/TablePagination';
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
import MsTableOperator from "../../../common/components/MsTableOperator";
import {TEST_PLAN_REPORT_CONFIGS} from "../../../common/components/search/search-components";
import TestPlanReportView from "@/business/components/track/report/components/TestPlanReportView";
import ReportTriggerModeItem from "@/business/components/common/tableItem/ReportTriggerModeItem";
import MsTag from "@/business/components/common/components/MsTag";
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import MsTableSelectAll from "@/business/components/common/components/table/MsTableSelectAll";
import {
  _filter,
  _handleSelect,
  _handleSelectAll,
  _sort,
  getLastTableSortField,
  getSelectDataCounts,
  initCondition,
  saveLastTableSortField,
  setUnSelectIds,
  toggleAllSelection,
  getCustomTableWidth,
  getCustomTableHeader
} from "@/common/js/tableUtils";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import {getCurrentProjectID} from "@/common/js/utils";
import TestPlanDbReport from "@/business/components/track/report/components/TestPlanDbReport";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsRenameReportDialog from "@/business/components/common/components/report/MsRenameReportDialog";
export default {
  name: "TestPlanReportList",
  components: {
    TestPlanDbReport,
    MsTableHeaderSelectPopover,
    TestPlanReportView,
    MsTableOperator, MsTableOperatorButton, MsTableHeader, MsTablePagination,
    ReportTriggerModeItem, MsTag,
    ShowMoreBtn, MsTableSelectAll,
    MsTableColumn,
    MsTable,
    MsRenameReportDialog
  },
  data() {
    return {
      result: {},
      enableDeleteTip: false,
      tableHeaderKey: "TRACK_REPORT_TABLE",
      queryPath: "/test/plan/report/list",
      condition: {
        components: TEST_PLAN_REPORT_CONFIGS,
      },
      currentPage: 1,
      pageSize: 10,
      isTestManagerOrTestUser: false,
      selectRows: new Set(),
      screenHeight: 'calc(100vh - 200px)', //屏幕高度
      total: 0,
      tableData: [],
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'}
      ],
      stageFilters: [
        {text: this.$t('test_track.plan.smoke_test'), value: 'smoke'},
        {text: this.$t('test_track.plan.system_test'), value: 'system'},
        {text: this.$t('test_track.plan.regression_test'), value: 'regression'},
      ],
      buttons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permission: ['PROJECT_TRACK_REPORT:READ+DELETE']
        },
      ],
      selectDataCounts: 0,

      fields: getCustomTableHeader('TRACK_REPORT_TABLE'),
      fieldsWidth: getCustomTableWidth('TRACK_REPORT_TABLE'),
      operators: [],
      batchButtons: [],
      publicButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permission: ['PROJECT_TRACK_REPORT:READ+DELETE'],
        },
      ],
      simpleOperators: [
        {
          tip: this.$t('test_track.plan_view.view_report'), icon: "el-icon-document",
          exec: this.openReport,
        },
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_TRACK_REPORT:READ+DELETE']
        },
      ],
    };
  },
  watch: {
    '$route'(to, from) {
    }
  },
  activated() {

  },
  created() {
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
      this.$get('/test/plan/report/db/' + this.$route.query.resourceId, response => {
        this.$refs.dbReport.open(response.data);
      });
    }
  },
  methods: {
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
      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
      });
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    handleDelete(testPlanReport) {
      this.$alert(this.$t('report.delete_confirm') + ' ' + testPlanReport.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let testPlanReportIdList = [testPlanReport.id];
            this.$post('/test/plan/report/delete/', testPlanReportIdList, () => {
              this.$success(this.$t('commons.delete_success'));
              this.initTableData();
            });
          }
        }
      });
    },
    handleDeleteBatch() {
      this.$alert(this.$t('report.delete_batch_confirm') + ' ' + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let deleteParam = {};
            let ids = this.$refs.testPlanReportTable.selectIds;
            deleteParam.dataIds = ids;
            deleteParam.projectId = this.projectId;
            deleteParam.selectAllDate = this.condition.selectAll;
            deleteParam.unSelectIds = this.condition.unSelection;
            deleteParam = Object.assign(deleteParam, this.condition);
            this.$post('/test/plan/report/deleteBatchByParams/', deleteParam, () => {
              this.$success(this.$t('commons.delete_success'));
              this.initTableData();
              this.selectRows.clear();
            });
          }
        }
      });
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets);
      let ids = rowArray.map(s => s.id);
      return ids;
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.saveSortField(this.tableHeaderKey, this.condition.orders);
      this.initTableData();
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
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      //设置勾选
      toggleAllSelection(this.$refs.testPlanReportTable, this.tableData, this.selectRows);
      //显示隐藏菜单
      _handleSelectAll(this, this.tableData, this.tableData, this.selectRows);
      //设置未选择ID(更新)
      this.condition.unSelectIds = [];
      //更新统计信息
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    saveSortField(key, orders) {
      saveLastTableSortField(key, JSON.stringify(orders));
    },
    openReNameDialog($event) {
      this.$refs.renameDialog.open($event);
    },
    rename(data) {
      this.$post("/test/plan/report/reName/", data, () => {
        this.$success(this.$t("organization.integration.successful_operation"));
        this.initTableData();
        this.$refs.renameDialog.close();
      });
    }
  }
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
  color: #1E90FF;
}

/*.ms-select-all >>> th:first-child {*/
/*  margin-top: 20px;*/
/*}*/

/*.ms-select-all >>> th:nth-child(2) .el-icon-arrow-down {*/
/*  top: -2px;*/
/*}*/
</style>
