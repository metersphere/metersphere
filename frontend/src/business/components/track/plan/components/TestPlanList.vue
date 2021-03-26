<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <ms-table-header :is-tester-permission="true" :condition.sync="condition"
                       @search="initTableData" @create="testPlanCreate"
                       :create-tip="$t('test_track.plan.create_plan')"
                       :title="$t('test_track.plan.test_plan')"
      />

    </template>

    <el-table
      border
      class="adjust-table"
      :data="tableData"
      @filter-change="filter"
      @sort-change="sort"
      @row-click="intoPlan">
      <template v-for="(item, index) in tableLabel">
        <el-table-column
          v-if="item.id == 'name'"
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id == 'userName'"
          prop="userName"
          :label="$t('test_track.plan.plan_principal')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id == 'status'"
          prop="status"
          column-key="status"
          :filters="statusFilters"
          :label="$t('test_track.plan.plan_status')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
          <span @click.stop="clickt = 'stop'">
            <el-dropdown class="test-case-status" @command="statusChange">
              <span class="el-dropdown-link">
                <plan-status-table-item :value="scope.row.status"/>
              </span>
              <el-dropdown-menu slot="dropdown" chang>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser" :command="{item: scope.row, status: 'Prepare'}">
                  {{ $t('test_track.plan.plan_status_prepare') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                  :command="{item: scope.row, status: 'Underway'}">
                  {{ $t('test_track.plan.plan_status_running') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                  :command="{item: scope.row, status: 'Finished'}">
                  {{ $t('test_track.plan.plan_status_finished') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                  :command="{item: scope.row, status: 'Completed'}">
                  {{ $t('test_track.plan.plan_status_completed') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'stage'"
          prop="stage"
          column-key="stage"
          :filters="stageFilters"
          :label="$t('test_track.plan.plan_stage')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <plan-stage-table-item :stage="scope.row.stage"/>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'testRate'"
          prop="testRate"
          :label="$t('test_track.home.test_rate')"
          min-width="100"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <el-progress :percentage="calPassRate(scope)"></el-progress>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'projectName'"
          prop="projectName"
          :label="$t('test_track.plan.plan_project')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column  v-if="item.id == 'tags'" prop="tags"
                         :label="$t('api_test.automation.tag')" :key="index">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :content="itemName" style="margin-left: 0px; margin-right: 2px"></ms-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'executionTimes'"
          prop="executionTimes"
          :label="$t('commons.execution_times')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id == 'passRate'"
          prop="passRate"
          :label="$t('commons.pass_rate')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id == 'plannedStartTime'"
          sortable
          prop="plannedStartTime"
          :label="$t('test_track.plan.planned_start_time')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.plannedStartTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'plannedEndTime'"
          sortable
          prop="plannedEndTime"
          :label="$t('test_track.plan.planned_end_time')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.plannedEndTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'actualStartTime'"
          sortable
          prop="actualStartTime"
          :label="$t('test_track.plan.actual_start_time')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.actualStartTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'actualEndTime'"
          sortable
          prop="actualEndTime"
          :label="$t('test_track.plan.actual_end_time')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.actualEndTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </template>
      <el-table-column
        min-width="150"
        :label="$t('commons.operating')">
        <template slot="header">
          <header-label-operate @exec="customHeader"/>
        </template>
        <template v-slot:default="scope">
          <ms-table-operator :is-tester-permission="true" @editClick="handleEdit(scope.row)"
                             @deleteClick="handleDelete(scope.row)">
            <template v-slot:middle>
              <ms-table-operator-button :isTesterPermission="true"
                                        style="background-color: #85888E;border-color: #85888E"
                                        v-if="!scope.row.reportId"
                                        :tip="$t('test_track.plan_view.create_report')" icon="el-icon-s-data"
                                        @exec="openTestReportTemplate(scope.row)"/>
              <ms-table-operator-button v-if="scope.row.reportId"
                                        :tip="$t('test_track.plan_view.view_report')" icon="el-icon-s-data"
                                        @exec="openReport(scope.row.id, scope.row.reportId)"/>
            </template>
          </ms-table-operator>
          <ms-table-operator-button style="margin-left: 10px;color:#85888E;border-color: #85888E; border-width: thin;"
                                    v-if="!scope.row.scheduleOpen" type="text"
                                    :tip="$t('commons.trigger_mode.schedule')" icon="el-icon-time"
                                    @exec="scheduleTask(scope.row)"/>
          <ms-table-operator-button style="margin-left: 10px;color:#6C317C; border-color: #6C317C; border-width: thin;"
                                    v-if="scope.row.scheduleOpen" type="text"
                                    :tip="$t('commons.trigger_mode.schedule')" icon="el-icon-time"
                                    @exec="scheduleTask(scope.row)"/>
        </template>
      </el-table-column>
    </el-table>
    <header-custom ref="headerCustom" :initTableData="inite" :optionalFields=headerItems
                   :type=type></header-custom>


    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <test-report-template-list @openReport="openReport" ref="testReportTemplateList"/>
    <test-case-report-view @refresh="initTableData" ref="testCaseReportView"/>
    <ms-delete-confirm :title="$t('test_track.plan.plan_delete')" @delete="_handleDelete" ref="deleteConfirm"
                       :with-tip="enableDeleteTip">
      {{ $t('test_track.plan.plan_delete_tip') }}
    </ms-delete-confirm>
    <ms-schedule-maintain ref="scheduleMaintain" @refreshTable="initTableData"/>
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
import {checkoutTestManagerOrTestUser, getCurrentUser} from "@/common/js/utils";
import TestReportTemplateList from "../view/comonents/TestReportTemplateList";
import TestCaseReportView from "../view/comonents/report/TestCaseReportView";
import MsDeleteConfirm from "../../../common/components/MsDeleteConfirm";
import {TEST_PLAN_CONFIGS} from "../../../common/components/search/search-components";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
import MsScheduleMaintain from "@/business/components/api/automation/schedule/ScheduleMaintain"
import {_filter, _sort, getLabel, getSystemLabel} from "@/common/js/tableUtils";
import {TEST_PLAN_LIST} from "@/common/js/constants";
import {Test_Plan_List} from "@/business/components/common/model/JsonData";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import MsTag from "@/business/components/common/components/MsTag";


export default {
  name: "TestPlanList",
  components: {
    MsTag,
    HeaderLabelOperate,
    HeaderCustom,
    MsDeleteConfirm,
    TestCaseReportView,
    TestReportTemplateList,
    PlanStageTableItem,
    PlanStatusTableItem,
    MsScheduleMaintain,
    MsTableOperator, MsTableOperatorButton, MsDialogFooter, MsTableHeader, MsCreateBox, MsTablePagination
  },
  data() {
    return {
      type: TEST_PLAN_LIST,
      headerItems: Test_Plan_List,
      tableLabel: [],
      result: {},
      enableDeleteTip: false,
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
        {text: this.$t('test_track.plan.plan_status_finished'), value: 'Finished'},
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
    if (!this.projectId) {
      this.projectId = this.$store.state.projectId;
    }
    this.isTestManagerOrTestUser = checkoutTestManagerOrTestUser();
    this.initTableData();
    getSystemLabel(this, this.type)
  },
  methods: {
    inite() {
      this.initTableData()
    },
    calPassRate(scope) {
      let passRate = scope.row.passRate.substring(0, scope.row.passRate.length - 1);
      return Number.parseInt(passRate, 10);
    },
    customHeader() {
      this.$refs.headerCustom.open(this.tableLabel)
    },
    initTableData() {
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
        this.tableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
          item.passRate = item.passRate + '%'
        })
      });
      getLabel(this, TEST_PLAN_LIST);

    },
    copyData(status) {
      return JSON.parse(JSON.stringify(this.dataMap.get(status)))
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    testPlanCreate() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$emit('openTestPlanEditDialog');
    },
    handleEdit(testPlan) {
      this.$emit('testPlanEdit', testPlan);
    },
    statusChange(param) {
      console.log(this.tableData);
      let oldStatus = param.item.status;
      let newStatus = param.status;
      param = param.item;
      param.status = newStatus;
      this.$post('/test/plan/edit', param, () => {
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id == param.id) { //  手动修改当前状态后，前端结束时间先用当前时间，等刷新后变成后台数据（相等）
            if (oldStatus !== "Completed" && newStatus === "Completed") {
              this.tableData[i].actualEndTime = Date.now();
            } //  非完成->已完成，结束时间=null
            else if (oldStatus !== "Underway" && newStatus === "Underway") {
              this.tableData[i].actualStartTime = Date.now();
              this.tableData[i].actualEndTime = "";
            } //  非进行中->进行中，结束时间=null
            else if (oldStatus !== "Prepare" && newStatus === "Prepare") {
              this.tableData[i].actualStartTime = this.tableData[i].actualEndTime = "";
            } //  非未开始->未开始，结束时间=null
            this.tableData[i].status = newStatus;
            break;
          }
        }
      });
    },
    handleDelete(testPlan) {
      this.enableDeleteTip = testPlan.status === 'Underway' ? true : false;
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
    scheduleTask(row){
      row.redirectFrom = "testPlan";
      this.$refs.scheduleMaintain.open(row);
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
