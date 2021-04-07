<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <ms-table-header :is-tester-permission="true" :condition.sync="condition" :show-create="false"
                       @search="initTableData"
                       :title="$t('test_track.report.name')"/>
    </template>
    <el-table border :data="tableData"
              @select-all="handleSelectAll"
              @select="handleSelect"
              :height="screenHeight"
              ref="testPlanReportTable"
              row-key="id" class="test-content adjust-table ms-select-all"
      @filter-change="filter" @sort-change="sort" >

      <el-table-column width="50" type="selection"/>
      <ms-table-select-all
        :page-size="pageSize>total?total:pageSize"
        :total="total"
        @selectPageAll="isSelectDataAll(false)"
        @selectAll="isSelectDataAll(true)"/>

      <el-table-column width="30" :resizable="false" align="center">
        <template v-slot:default="scope">
          <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts" v-tester/>
        </template>
      </el-table-column>

      <el-table-column min-width="300" prop="name" :label="$t('test_track.report.list.name')" show-overflow-tooltip></el-table-column>
      <el-table-column prop="testPlanName" min-width="150" sortable :label="$t('test_track.report.list.test_plan')" show-overflow-tooltip></el-table-column>
      <el-table-column prop="creator" :label="$t('test_track.report.list.creator')" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" sortable :label="$t('test_track.report.list.create_time' )" show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="triggerMode" :label="$t('test_track.report.list.trigger_mode')" show-overflow-tooltip>
        <template v-slot:default="scope">
          <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('commons.status')">
        <template v-slot:default="scope">
          <ms-tag v-if="scope.row.status == 'RUNNING'" type="success" effect="plain" :content="'Running'"/>
          <ms-tag v-else-if="scope.row.status == 'COMPLETED'||scope.row.status == 'SUCCESS'||scope.row.status == 'FAILED'" type="info" effect="plain" :content="'Completed'"/>
          <ms-tag v-else type="effect" effect="plain" :content="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column min-width="150" :label="$t('commons.operating')">
        <template v-slot:default="scope">
          <ms-table-operator-button :tip="$t('test_track.plan_view.view_report')" icon="el-icon-document"
            @exec="openReport(scope.row.id)"/>
          <ms-table-operator-button type="danger" :tip="$t('commons.delete')" icon="el-icon-delete"
            @exec="handleDelete(scope.row)" v-tester/>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <test-plan-report-view @refresh="initTableData" ref="testPlanReportView"/>
  </el-card>
</template>

<script>
import MsTablePagination from '../../../../components/common/pagination/TablePagination';
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
import MsTableOperator from "../../../common/components/MsTableOperator";
import {checkoutTestManagerOrTestUser} from "@/common/js/utils";
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
  _sort, checkTableRowIsSelect,
  getSelectDataCounts,
  initCondition,
  setUnSelectIds, toggleAllSelection,
} from "@/common/js/tableUtils";

export default {
  name: "TestPlanReportList",
  components: {
    TestPlanReportView,
    MsTableOperator, MsTableOperatorButton, MsTableHeader, MsTablePagination,
    ReportTriggerModeItem, MsTag,
    ShowMoreBtn, MsTableSelectAll,
  },
  data() {
    return {
      result: {},
      enableDeleteTip: false,
      queryPath: "/test/plan/report/list",
      condition: {
        components: TEST_PLAN_REPORT_CONFIGS
      },
      currentPage: 1,
      pageSize: 10,
      isTestManagerOrTestUser: false,
      selectRows: new Set(),
      screenHeight: document.documentElement.clientHeight - 296,//屏幕高度
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
        {name: this.$t('api_test.definition.request.batch_delete'), handleClick: this.handleDeleteBatch},
      ],
      selectDataCounts: 0,
    }
  },
  watch: {
    '$route'(to, from) {
    }
  },
  activated() {
    this.components = TEST_PLAN_REPORT_CONFIGS;
  },
  created() {
    this.projectId = this.$route.params.projectId;
    if (!this.projectId) {
      this.projectId = this.$store.state.projectId;
    }
    this.isTestManagerOrTestUser = checkoutTestManagerOrTestUser();
    this.initTableData();
  },
  methods: {
    initTableData() {
      initCondition(this.condition, this.condition.selectAll);
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
      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        if (this.$refs.testPlanReportTable) {
          // setTimeout(this.$refs.testPlanReportTable,200);
        }
        this.$nextTick(() => {
          checkTableRowIsSelect(this,this.condition,this.tableData,this.$refs.testPlanReportTable,this.selectRows);
        });
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
    handleDeleteBatch(){
      this.$alert(this.$t('report.delete_batch_confirm') + ' ' + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let deleteParam = {};
            let ids = Array.from(this.selectRows).map(row => row.id);
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
      let rowArray = Array.from(rowSets)
      let ids = rowArray.map(s => s.id);
      return ids;
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      _sort(column, this.condition);
      this.initTableData();
    },
    openReport(planId) {
      if (planId) {
        this.$refs.testPlanReportView.open(planId);
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
  }
}
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

.ms-select-all >>> th:first-child {
  margin-top: 20px;
}

.ms-select-all >>> th:nth-child(2) .el-icon-arrow-down {
  top: -2px;
}
</style>
