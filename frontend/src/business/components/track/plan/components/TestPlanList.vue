<template>
  <el-card class="table-card" v-loading="cardResult.loading">
    <template v-slot:header>
      <ms-table-header :create-permission="['PROJECT_TRACK_PLAN:READ+CREATE']" :condition.sync="condition"
                       @search="initTableData" @create="testPlanCreate"
                       :create-tip="$t('test_track.plan.create_plan')"/>

    </template>

    <el-table
      border
      class="adjust-table"
      :data="tableData"
      @filter-change="filter"
      @sort-change="sort"
      :height="screenHeight"
      v-loading="result.loading"
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
          prop="principalName"
          :label="$t('test_track.plan.plan_principal')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id == 'createUser'"
          prop="createUser"
          :label="$t('commons.create_user')"
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
          :min-width="100"
          :key="index">
          <template v-slot:default="scope">
          <span @click.stop="clickt = 'stop'">
            <el-dropdown class="test-case-status" @command="statusChange">
              <span class="el-dropdown-link">
                <plan-status-table-item :value="scope.row.status"/>
              </span>
              <el-dropdown-menu slot="dropdown" chang>
                <el-dropdown-item :disabled="!hasEditPermission" :command="{item: scope.row, status: 'Prepare'}">
                  {{ $t('test_track.plan.plan_status_prepare') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!hasEditPermission"
                                  :command="{item: scope.row, status: 'Underway'}">
                  {{ $t('test_track.plan.plan_status_running') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!hasEditPermission"
                                  :command="{item: scope.row, status: 'Finished'}">
                  {{ $t('test_track.plan.plan_status_finished') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!hasEditPermission"
                                  :command="{item: scope.row, status: 'Completed'}">
                  {{ $t('test_track.plan.plan_status_completed') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id=='followPeople'"
          prop="followPeople"
          :label="$t('test_track.plan.follow_people')"
          show-overflow-tooltip
          :key="index"
        >
        </el-table-column>
        <el-table-column
          v-if="item.id == 'stage'"
          prop="stage"
          column-key="stage"
          :filters="stageFilters"
          :label="$t('test_track.plan.plan_stage')"
          show-overflow-tooltip
          :min-width="110"
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
            <el-progress :percentage="scope.row.testRate"></el-progress>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'projectName'"
          prop="projectName"
          :label="$t('test_track.plan.plan_project')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column v-if="item.id == 'tags'" prop="tags"
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
          :min-width="100"
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
          :min-width="110"
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
          :min-width="110"
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.plannedEndTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'actualStartTime'"
          sortable
          prop="actualStartTime"
          :min-width="110"
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
          :min-width="110"
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
        min-width="180"
        :label="$t('commons.operating')">
        <template slot="header">
          <header-label-operate @exec="customHeader"/>
        </template>
        <template v-slot:default="scope">
          <div>
            <ms-table-operator :edit-permission="['PROJECT_TRACK_PLAN:READ+EDIT']"
                               :show-delete="false"
                               @editClick="handleEdit(scope.row)">
              <template v-slot:front>
                <ms-table-operator-button :tip="$t('api_test.run')" icon="el-icon-video-play" class="run-button"
                                          @exec="handleRun(scope.row)" v-permission="['PROJECT_TRACK_PLAN:READ+RUN']"/>
              </template>
              <template v-slot:middle>
                <ms-table-operator-button :tip="$t('commons.copy')" icon="el-icon-copy-document"
                                          @exec="handleCopy(scope.row)" v-permission="['PROJECT_TRACK_PLAN:READ+COPY']"/>
                <ms-table-operator-button v-permission="['PROJECT_TRACK_PLAN:READ+EDIT']"
                                          :tip="$t('test_track.plan_view.view_report')" icon="el-icon-s-data"
                                          @exec="openReport(scope.row)"/>
              </template>
            </ms-table-operator>
            <el-dropdown @command="handleCommand($event, scope.row)" class="scenario-ext-btn" v-permission="['PROJECT_TRACK_PLAN:READ+DELETE','PROJECT_TRACK_PLAN:READ+SCHEDULE']">
              <el-link type="primary" :underline="false">
                <el-icon class="el-icon-more"></el-icon>
              </el-link>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="delete" v-permission="['PROJECT_TRACK_PLAN:READ+DELETE']">
                  {{ $t('commons.delete') }}
                </el-dropdown-item>
                <el-dropdown-item command="schedule_task" v-permission="['PROJECT_TRACK_PLAN:READ+SCHEDULE']">
                  {{ $t('commons.trigger_mode.schedule') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <header-custom ref="headerCustom" :initTableData="init" :optionalFields=headerItems
                   :type=type></header-custom>


    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <ms-delete-confirm :title="$t('test_track.plan.plan_delete')" @delete="_handleDelete" ref="deleteConfirm"
                       :with-tip="enableDeleteTip">
      {{ $t('test_track.plan.plan_delete_tip') }}
    </ms-delete-confirm>
    <ms-test-plan-schedule-maintain ref="scheduleMaintain" @refreshTable="initTableData"/>
    <plan-run-mode-with-env @handleRunBatch="_handleRun" ref="runMode" :plan-case-ids="[]" :type="'plan'" :plan-id="currentPlanId"/>
    <test-plan-report-review ref="testCaseReportView"/>
    <ms-task-center ref="taskCenter" :show-menu="false"/>
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
import MsDeleteConfirm from "../../../common/components/MsDeleteConfirm";
import {TEST_PLAN_CONFIGS} from "../../../common/components/search/search-components";
import {
  _filter,
  _sort,
  deepClone,
  getLabel,
  getLastTableSortField,
  saveLastTableSortField
} from "@/common/js/tableUtils";
import {TEST_PLAN_LIST} from "@/common/js/constants";
import {Test_Plan_List} from "@/business/components/common/model/JsonData";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import MsTag from "@/business/components/common/components/MsTag";
import MsTestPlanScheduleMaintain from "@/business/components/track/plan/components/ScheduleMaintain";
import {getCurrentProjectID, getCurrentUserId, hasPermission} from "@/common/js/utils";
import PlanRunModeWithEnv from "@/business/components/track/plan/common/PlanRunModeWithEnv";
import TestPlanReportReview from "@/business/components/track/report/components/TestPlanReportReview";
import MsTaskCenter from "@/business/components/task/TaskCenter";

export default {
  name: "TestPlanList",
  components: {
    TestPlanReportReview,
    MsTag,
    HeaderLabelOperate,
    HeaderCustom,
    MsDeleteConfirm,
    PlanStageTableItem,
    PlanStatusTableItem,
    MsTestPlanScheduleMaintain,
    MsTableOperator, MsTableOperatorButton,
    MsDialogFooter, MsTableHeader, MsCreateBox,
    MsTablePagination, PlanRunModeWithEnv, MsTaskCenter
  },
  data() {
    return {
      createUser: "",
      type: TEST_PLAN_LIST,
      headerItems: Test_Plan_List,
      tableHeaderKey:"TEST_PLAN_LIST",
      tableLabel: [],
      result: {},
      cardResult: {},
      enableDeleteTip: false,
      queryPath: "/test/plan/list",
      deletePath: "/test/plan/delete",
      condition: {
        components: TEST_PLAN_CONFIGS
      },
      currentPage: 1,
      pageSize: 10,
      hasEditPermission: false,
      total: 0,
      tableData: [],
      screenHeight: 'calc(100vh - 200px)',
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
      currentPlanId: ""
    };
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
      this.projectId = getCurrentProjectID();
    }
    this.hasEditPermission = hasPermission('PROJECT_TRACK_PLAN:READ+EDIT');
    this.condition.orders = getLastTableSortField(this.tableHeaderKey);

    this.initTableData();
  },
  methods: {
    init() {
      this.initTableData();
    },
    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
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
      this.condition.projectId = getCurrentProjectID();
      this.cardResult = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.tableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
          item.passRate = item.passRate + '%';
          this.$get("/test/plan/principal/" + item.id, res => {
            let data = res.data;
            let principal = "";
            let principalIds = data.map(d => d.id);
            if (data) {
              data.forEach(d => {
                if (principal !== "") {
                  principal = principal + "、" + d.name;
                } else {
                  principal = principal + d.name;
                }
              })
            }
            this.$set(item, "principalName", principal);
            // 编辑时初始化id
            this.$set(item, "principals", principalIds);
          })
        });
      });
      getLabel(this, TEST_PLAN_LIST);

    },
    copyData(status) {
      return JSON.parse(JSON.stringify(this.dataMap.get(status)));
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
    statusChange(data) {
      if (!hasPermission('PROJECT_TRACK_PLAN:READ+EDIT')) {
        return;
      }
      let oldStatus = data.item.status;
      let newStatus = data.status;
      let param = {};
      param.id = data.item.id;
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
      });
    },
    intoPlan(row, column, event) {
      if (column.label !== this.$t('commons.operating')) {
        this.$router.push('/track/plan/view/' + row.id);
      }
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
      this.saveSortField(this.tableHeaderKey,this.condition.orders);
      this.initTableData();
    },
    openReport(plan) {
      this.$refs.testCaseReportView.open(plan);
    },
    scheduleTask(row) {
      row.redirectFrom = "testPlan";
      this.$refs.scheduleMaintain.open(row);
    },
    saveSortField(key,orders){
      saveLastTableSortField(key,JSON.stringify(orders));
    },
    handleCommand(cmd, row) {
      switch (cmd) {
        case  "delete":
          this.handleDelete(row);
          break;
        case "schedule_task":
          this.scheduleTask(row);
          break;
      }
    },
    handleCopy(row) {
      this.cardResult.loading = true;
      this.$post('test/plan/copy/' + row.id, {},() => {
        this.initTableData();
      });
    },
    handleRun(row) {
      this.currentPlanId = row.id;
      this.$get("/test/plan/have/exec/case/" + row.id, res => {
        const haveExecCase = res.data;
        if (haveExecCase) {
          this.$refs.runMode.open('API');
        } else {
          this.$router.push('/track/plan/view/' + row.id);
        }
      })
    },
    _handleRun(config) {
      let {mode, reportType, onSampleError, runWithinResourcePool, resourcePoolId, envMap} = config;
      let param = {mode, reportType, onSampleError, runWithinResourcePool, resourcePoolId, envMap};
      param.testPlanId = this.currentPlanId;
      param.projectId = getCurrentProjectID();
      param.userId = getCurrentUserId();
      param.triggerMode = 'MANUAL';
      this.$refs.taskCenter.open();
      this.result = this.$post('test/plan/run/', param,() => {
        this.$success(this.$t('commons.run_success'));
      }, () => {
        this.$error(this.$t('commons.run_fail'));
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

.schedule-btn >>> .el-button {
  margin-left: 10px;
  color:#85888E;
  border-color: #85888E;
  border-width: thin;
}

.scenario-ext-btn {
  margin-left: 10px;
}
</style>
