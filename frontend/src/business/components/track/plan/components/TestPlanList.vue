<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <ms-table-header :is-tester-permission="true" :condition.sync="condition"
                       @search="initTableData" @create="testPlanCreate"
                       :create-tip="$t('test_track.plan.create_plan')"
                       :title="$t('test_track.plan.test_plan')"/>
    </template>

    <el-table
      border
      class="adjust-table"
      :data="tableData"
      @filter-change="filter"
      @sort-change="sort"
      @row-click="intoPlan">
      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="principal"
        :label="$t('test_track.plan.plan_principal')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="status"
        column-key="status"
        :filters="statusFilters"
        :label="$t('test_track.plan.plan_status')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span @click.stop="clickt = 'stop'">
            <el-dropdown class="test-case-status" @command="statusChange">
              <span class="el-dropdown-link">
                <plan-status-table-item :value="scope.row.status"/>
              </span>
              <el-dropdown-menu slot="dropdown" chang>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser" :command="{id: scope.row.id, status: 'Prepare'}">
                  {{ $t('test_track.plan.plan_status_prepare') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                  :command="{id: scope.row.id, status: 'Underway'}">
                  {{ $t('test_track.plan.plan_status_running') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                  :command="{id: scope.row.id, status: 'Completed'}">
                  {{ $t('test_track.plan.plan_status_completed') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
        </template>
      </el-table-column>
      <el-table-column
        prop="stage"
        column-key="stage"
        :filters="stageFilters"
        :label="$t('test_track.plan.plan_stage')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <plan-stage-table-item :stage="scope.row.stage"/>
        </template>
      </el-table-column>
      <el-table-column
        prop="projectName"
        :label="$t('test_track.plan.plan_project')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        sortable
        prop="createTime"
        :label="$t('commons.create_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="updateTime"
        :label="$t('commons.update_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('commons.operating')">
        <template v-slot:default="scope">
          <ms-table-operator :is-tester-permission="true" @editClick="handleEdit(scope.row)"
                             @deleteClick="handleDelete(scope.row)">
            <template v-slot:middle>
              <ms-table-operator-button :isTesterPermission="true" type="success" v-if="!scope.row.reportId"
                                        :tip="$t('test_track.plan_view.create_report')" icon="el-icon-document"
                                        @exec="openTestReportTemplate(scope.row)"/>
              <ms-table-operator-button type="success" v-if="scope.row.reportId"
                                        :tip="$t('test_track.plan_view.view_report')" icon="el-icon-document"
                                        @exec="openReport(scope.row.id, scope.row.reportId)"/>
            </template>
          </ms-table-operator>
        </template>
      </el-table-column>
    </el-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <test-report-template-list @openReport="openReport" ref="testReportTemplateList"/>
    <test-case-report-view @refresh="initTableData" ref="testCaseReportView"/>
    <ms-delete-confirm :title="$t('test_track.plan.plan_delete')" @delete="_handleDelete" ref="deleteConfirm"/>

  </el-card>
</template>

<script>
import MsCreateBox from '../../../settings/CreateBox';
import MsTablePagination from '../../../../components/common/pagination/TablePagination';
import MsTableHeader from "../../../common/components/MsTableHeader";
import MsDialogFooter from "../../../common/components/MsDialogFooter";
import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
import MsTableOperator from "../../../common/components/MsTableOperator";
import PlanStatusTableItem from "../../common/tableItems/plan/PlanStatusTableItem";
import PlanStageTableItem from "../../common/tableItems/plan/PlanStageTableItem";
import {_filter, _sort, checkoutTestManagerOrTestUser} from "../../../../../common/js/utils";
import TestReportTemplateList from "../view/comonents/TestReportTemplateList";
import TestCaseReportView from "../view/comonents/report/TestCaseReportView";
import MsDeleteConfirm from "../../../common/components/MsDeleteConfirm";
import {TEST_PLAN_CONFIGS} from "../../../common/components/search/search-components";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";

export default {
  name: "TestPlanList",
  components: {
    MsDeleteConfirm,
    TestCaseReportView,
    TestReportTemplateList,
    PlanStageTableItem,
    PlanStatusTableItem,
    MsTableOperator, MsTableOperatorButton, MsDialogFooter, MsTableHeader, MsCreateBox, MsTablePagination
  },
  data() {
    return {
      result: {},
      queryPath: "/test/plan/list",
      deletePath: "/test/plan/delete",
      condition: {
        components: TEST_PLAN_CONFIGS
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
      if (to.path.indexOf("/track/plan/all") >= 0) {
        this.initTableData();
      }
    }
  },
  created() {
    this.projectId = this.$route.params.projectId;
    this.isTestManagerOrTestUser = checkoutTestManagerOrTestUser();
    this.initTableData();
  },
  methods: {
    initTableData() {
      if (this.planId) {
        // param.planId = this.planId;
        this.condition.planId = this.planId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        // param.nodeIds = this.selectNodeIds;
        this.condition.nodeIds = this.selectNodeIds;
      }
      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        for (let i = 0; i < this.tableData.length; i++) {
          let path = "/test/plan/project/name/" + this.tableData[i].id;
          this.$get(path, res => {
            this.$set(this.tableData[i], "projectName", res.data);
          })
        }
      });
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    testPlanCreate() {
      this.$emit('openTestPlanEditDialog');
    },
    handleEdit(testPlan) {
      this.$emit('testPlanEdit', testPlan);
    },
    statusChange(param) {
      this.$post('/test/plan/edit', param, () => {
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id == param.id) {
            this.tableData[i].status = param.status;
            break;
          }
        }
      });
    },
    handleDelete(testPlan) {
      this.$refs.deleteConfirm.open(testPlan);
    },
    _handleDelete(testPlan) {
      let testPlanId = testPlan.id;
      this.$post('/test/plan/delete/' + testPlanId, {}, () => {
        this.initTableData();
        this.$success(this.$t('commons.delete_success'));
        // 发送广播，刷新 head 上的最新列表
        TrackEvent.$emit(LIST_CHANGE);
      });
    },
    intoPlan(row, event, column) {
      this.$router.push('/track/plan/view/' + row.id);
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      _sort(column, this.condition);
      this.initTableData();
    },
    openTestReportTemplate(data) {
      this.$refs.testReportTemplateList.open(data.id);
    },
    openReport(planId, reportId) {
      if (reportId) {
        this.$refs.testCaseReportView.open(planId, reportId);
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
