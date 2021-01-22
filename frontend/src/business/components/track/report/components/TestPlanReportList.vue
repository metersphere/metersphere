<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <ms-table-header :is-tester-permission="true" :condition.sync="condition" :show-create="false"
                       @search="initTableData"
                       :title="$t('test_track.report.name')"/>
    </template>
    <el-table border class="adjust-table" :data="tableData"
      @filter-change="filter" @sort-change="sort">
      <el-table-column min-width="300" prop="name" :label="$t('test_track.report.list.name')" show-overflow-tooltip></el-table-column>
      <el-table-column prop="testPlanName" sortable :label="$t('test_track.report.list.test_plan')" show-overflow-tooltip></el-table-column>
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
<!--          <el-tag size="mini" type="success" v-if="row.status === 'Running'">{{ row.status }}</el-tag>-->
<!--          <el-tag size="mini" type="info" v-else-if="row.status === 'Completed'">{{ row.status }}</el-tag>-->
<!--          <el-tag size="mini" type="info" v-if="row.status === 'Completed'">{{ row.status }}</el-tag>-->
        </template>
      </el-table-column>
      <el-table-column min-width="150" :label="$t('commons.operating')">
        <template v-slot:default="scope">
          <ms-table-operator-button :tip="$t('test_track.plan_view.view_report')" icon="el-icon-document"
            @exec="openReport(scope.row.id)"/>
          <ms-table-operator-button type="danger" :tip="$t('commons.delete')" icon="el-icon-delete"
            @exec="handleDelete(scope.row)" />
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
import {_filter, _sort, checkoutTestManagerOrTestUser} from "@/common/js/utils";
import {TEST_PLAN_REPORT_CONFIGS} from "../../../common/components/search/search-components";
import {getCurrentProjectID} from "../../../../../common/js/utils";
import TestPlanReportView from "@/business/components/track/report/components/TestPlanReportView";
import ReportTriggerModeItem from "@/business/components/common/tableItem/ReportTriggerModeItem";
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: "TestPlanReportList",
  components: {
    TestPlanReportView,
    MsTableOperator, MsTableOperatorButton, MsTableHeader, MsTablePagination,
    ReportTriggerModeItem,MsTag
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
    }
  },
  watch: {
    '$route'(to, from) {
      // if (to.path.indexOf("/track/plan/all") >= 0) {
      //   this.initTableData();
      // }
    }
  },
  activated() {
    this.components = TEST_PLAN_REPORT_CONFIGS;
  },
  created() {
    this.projectId = this.$route.params.projectId;
    this.isTestManagerOrTestUser = checkoutTestManagerOrTestUser();
    this.initTableData();
  },
  methods: {
    initTableData() {
      if (this.planId) {
        this.condition.planId = this.planId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        this.condition.nodeIds = this.selectNodeIds;
      }
      if (!getCurrentProjectID()) {
        return;
      }
      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        // for (let i = 0; i < this.tableData.length; i++) {
        //   let path = "/test/plan/project";
        //   this.$post(path, {planId: this.tableData[i].id}, res => {
        //     let arr = res.data;
        //     let projectIds = arr.filter(d => d.id !== this.tableData[i].projectId).map(data => data.id);
        //     this.$set(this.tableData[i], "projectIds", projectIds);
        //   })
        // }
      });
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
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
</style>
