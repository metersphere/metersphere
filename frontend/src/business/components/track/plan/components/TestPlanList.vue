<template>
  <el-card class="table-card" v-loading="cardResult.loading">
    <template v-slot:header>
      <ms-table-header :create-permission="['PROJECT_TRACK_PLAN:READ+CREATE']" :condition.sync="condition"
                       @search="initTableData" @create="testPlanCreate"
                       :create-tip="$t('test_track.plan.create_plan')"/>

    </template>

    <ms-table
      v-loading="cardResult.loading"
      operator-width="220px"
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
      @handlePageChange="intoPlan"
      @refresh="initTableData"
      ref="testPlanLitTable"
      @handleRowClick="intoPlan">

      <span v-for="item in fields" :key="item.key">

        <ms-table-column
          prop="name"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :label="$t('commons.name')"
          min-width="200px">
        </ms-table-column>
        <ms-table-column
          prop="principalName"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :label="$t('test_track.plan.plan_principal')"
          min-width="200px">
        </ms-table-column>
        <ms-table-column
          prop="createUser"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.create_user')"
          min-width="200px">
        </ms-table-column>
        <ms-table-column
          prop="status"
          :filters="statusFilters"
          column-key="status"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="120px"
          :label="$t('test_track.plan.plan_status')">
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
        </ms-table-column>
        <ms-table-column
          prop="scheduleStatus"
          :filters="scheduleFilters"
          column-key="scheduleStatus"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="200px"
          :label="$t('commons.trigger_mode.schedule')">
           <template v-slot="scope">
            <span v-if="scope.row.scheduleStatus === 'OPEN'">
              <el-tooltip placement="bottom-start" effect="light">
                <div slot="content">
                  {{ $t('api_test.home_page.running_task_list.table_coloum.run_rule') }}: {{
                    scope.row.scheduleCorn
                  }}<br/>
                  {{ $t('test_track.plan.next_run_time') }}：<span>{{
                    scope.row.scheduleExecuteTime | timestampFormatDate
                  }}</span>
                </div>
                <el-switch
                  @click.stop.native
                  v-model="scope.row.scheduleOpen"
                  inactive-color="#DCDFE6"
                  @change="scheduleChange(scope.row)"
                  :disabled="!hasSchedulePermission">
                </el-switch>
              </el-tooltip>
            </span>
            <span v-else-if="scope.row.scheduleStatus === 'SHUT'">
              <el-switch
                @click.stop.native
                v-model="scope.row.scheduleOpen"
                inactive-color="#DCDFE6"
                @change="scheduleChange(scope.row)"
                :disabled="!hasSchedulePermission">
                </el-switch>
            </span>
            <span v-else>
             <el-link @click.stop="scheduleTask(scope.row)" :disabled="!hasSchedulePermission" style="color:#783887;">{{
                 $t('schedule.not_set')
               }}</el-link>
            </span>
          </template>
        </ms-table-column>
        <ms-table-column
          prop="follow"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :label="$t('test_track.plan.follow_people')"
          min-width="200px">
        </ms-table-column>
        <ms-table-column
          prop="stage"
          column-key="stage"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :filters="stageFilters"
          :label="$t('test_track.plan.plan_stage')"
          min-width="120px">
          <template v-slot:default="scope">
            <plan-stage-table-item :option="stageOption" :stage="scope.row.stage"/>
          </template>
        </ms-table-column>
        <ms-table-column
          prop="testRate"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.home.test_rate')"
          min-width="120px">
          <template v-slot:default="scope">
            <el-progress :percentage="scope.row.testRate"></el-progress>
          </template>
        </ms-table-column>
        <ms-table-column
          prop="projectName"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.plan.plan_project')"
          min-width="200px">
        </ms-table-column>
        <ms-table-column
          prop="tags"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :label="$t('api_test.automation.tag')"
          min-width="200px">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :content="itemName" style="margin-left: 0px; margin-right: 2px"></ms-tag>
          </template>
        </ms-table-column>
        <ms-table-column
          prop="executionTimes"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :label="$t('commons.execution_times')"
          min-width="160px">
        </ms-table-column>
        <ms-table-column
          prop="testPlanTestCaseCount"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.plan.test_plan_test_case_count')"
          min-width="200px">
        </ms-table-column>
        <ms-table-column
          prop="testPlanApiCaseCount"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.plan.test_plan_api_case_count')"
          min-width="200px">
        </ms-table-column>
        <ms-table-column
          prop="testPlanApiScenarioCount"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.plan.test_plan_api_scenario_count')"
          min-width="200px">
        </ms-table-column>
        <ms-table-column
          prop="testPlanLoadCaseCount"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.plan.test_plan_load_case_count')"
          min-width="200px">
        </ms-table-column>
        <ms-table-column
          prop="passRate"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.pass_rate')"
          min-width="120px">
        </ms-table-column>
        <ms-table-column
          prop="plannedStartTime"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :label="$t('test_track.plan.planned_start_time')"
          min-width="200px">
          <template v-slot:default="scope">
            <span>{{ scope.row.plannedStartTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>
        <ms-table-column
          prop="plannedEndTime"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :label="$t('test_track.plan.planned_end_time')"
          min-width="160px">
          <template v-slot:default="scope">
            <span>{{ scope.row.plannedEndTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>
        <ms-table-column
          prop="actualStartTime"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :label="$t('test_track.plan.actual_start_time')"
          min-width="200px">
          <template v-slot:default="scope">
            <span>{{ scope.row.actualStartTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>
        <ms-table-column
          prop="actualEndTime"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          :label="$t('test_track.plan.actual_end_time')"
          min-width="200px">
          <template v-slot:default="scope">
            <span>{{ scope.row.actualEndTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>
      </span>
      <template v-slot:opt-before="scope">
        <ms-table-operator-button :tip="$t('api_test.run')" icon="el-icon-video-play" class="run-button"
                                  @exec="handleRun(scope.row)" v-permission="['PROJECT_TRACK_PLAN:READ+RUN']"
                                  style="margin-right: 10px;"/>
      </template>
      <template v-slot:opt-behind="scope">
        <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!scope.row.showFollow">
          <i class="el-icon-star-off"
             style="color: #783987; font-size: 25px; cursor: pointer;padding-left: 5px;width: 28px;height: 28px; top: 5px; position: relative"
             @click="saveFollow(scope.row)"></i>
        </el-tooltip>
        <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="scope.row.showFollow">
          <i class="el-icon-star-on"
             style="color: #783987; font-size: 30px; cursor: pointer;padding-left: 5px;width: 28px;height: 28px; top: 6px; position: relative"
             @click="saveFollow(scope.row)"></i>
        </el-tooltip>
        <el-dropdown @command="handleCommand($event, scope.row)" class="scenario-ext-btn"
                     v-permission="['PROJECT_TRACK_PLAN:READ+DELETE','PROJECT_TRACK_PLAN:READ+SCHEDULE']">
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
      </template>

    </ms-table>
    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <ms-delete-confirm :title="$t('test_track.plan.plan_delete')" @delete="_handleDelete" ref="deleteConfirm"
                       :with-tip="enableDeleteTip">
      {{ $t('test_track.plan.plan_delete_tip') }}
    </ms-delete-confirm>
    <ms-test-plan-schedule-maintain ref="scheduleMaintain" @refreshTable="initTableData"/>
    <ms-test-plan-schedule-batch-switch ref="scheduleBatchSwitch" @refresh="refresh"/>
    <plan-run-mode-with-env @handleRunBatch="_handleRun" ref="runMode" :plan-case-ids="[]" :type="'plan'"
                            :plan-id="currentPlanId"/>
    <test-plan-report-review ref="testCaseReportView"/>
    <ms-task-center ref="taskCenter" :show-menu="false"/>
  </el-card>
</template>

<script>
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
  deepClone, getCustomTableHeader, getCustomTableWidth,
  getLastTableSortField,
  saveLastTableSortField
} from "@/common/js/tableUtils";
import {TEST_PLAN_LIST} from "@/common/js/constants";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import MsTag from "@/business/components/common/components/MsTag";
import MsTestPlanScheduleMaintain from "@/business/components/track/plan/components/ScheduleMaintain";
import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentUserId,
  hasPermission
} from "@/common/js/utils";
import PlanRunModeWithEnv from "@/business/components/track/plan/common/PlanRunModeWithEnv";
import TestPlanReportReview from "@/business/components/track/report/components/TestPlanReportReview";
import MsTaskCenter from "@/business/components/task/TaskCenter";
import {getPlanStageOption} from "@/network/test-plan";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTestPlanScheduleBatchSwitch from "@/business/components/track/plan/components/ScheduleBatchSwitch";


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
    MsDialogFooter, MsTableHeader,
    MsTablePagination, PlanRunModeWithEnv, MsTaskCenter,
    MsTableColumn,
    MsTable,
    MsTestPlanScheduleBatchSwitch
  },
  data() {
    return {
      createUser: "",
      tableHeaderKey: "TEST_PLAN_LIST",
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
      hasSchedulePermission: false,
      total: 0,
      tableData: [],
      fields: getCustomTableHeader('TEST_PLAN_LIST'),
      fieldsWidth: getCustomTableWidth('TEST_PLAN_LIST'),
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
      scheduleFilters: [
        {text: this.$t('test_track.plan.schedule_enabled'), value: 'OPEN'},
        {text: this.$t('test_track.issue.status_closed'), value: 'SHUT'},
        {text: this.$t('schedule.not_set'), value: 'NOTSET'}
      ],
      currentPlanId: "",
      stageOption: [],
      operators: [],
      batchButtons: [],
      publicButtons: [
        {
          name: this.$t('test_track.plan.test_plan_batch_switch'),
          handleClick: this.handleBatchSwitch,
          permissions: ['PROJECT_TRACK_PLAN:READ+SCHEDULE']
        }
      ],
      simpleOperators: [
        {
          tip: this.$t('commons.edit'),
          icon: "el-icon-edit",
          exec: this.handleEdit,
          permissions: ['PROJECT_TRACK_PLAN:READ+EDIT']
        },
        {
          tip: this.$t('commons.copy'),
          icon: "el-icon-copy-document",
          exec: this.handleCopy,
          permission: ['PROJECT_TRACK_PLAN:READ+COPY']
        },
        {
          tip: this.$t('test_track.plan_view.view_report'),
          icon: "el-icon-s-data",
          exec: this.openReport,
          permission: ['PROJECT_TRACK_PLAN:READ+EDIT']
        },
      ]
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
    this.batchButtons = this.publicButtons;
    this.operators = this.simpleOperators;
    if (!this.projectId) {
      this.projectId = getCurrentProjectID();
    }
    this.hasEditPermission = hasPermission('PROJECT_TRACK_PLAN:READ+EDIT');
    this.hasSchedulePermission = hasPermission('PROJECT_TRACK_PLAN:READ+SCHEDULE');
    this.condition.orders = getLastTableSortField(this.tableHeaderKey);
    getPlanStageOption((data) => {
      this.stageOption = data;
      if (this.stageOption.length > 0) {
        this.stageFilters = this.stageOption;
        this.stageFilters.forEach((stage) => {
          if (stage.system !== null && stage.system) {
            stage.text = this.$t(stage.text);
          }
        })
      }
    });
    this.initTableData();
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
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
        data.listObject.forEach(item => {
          if (item.tags) {
            item.tags = JSON.parse(item.tags);
            if(item.tags.length===0){
              item.tags = null;
            }
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
          //关注人
          this.$get("/test/plan/follow/" + item.id, res => {
            let data = res.data;
            let follow = "";
            let followIds = data.map(d => d.id);
            let showFollow = false;
            if (data) {
              data.forEach(d => {
                if (follow !== "") {
                  follow = follow + "、" + d.name;
                } else {
                  follow = follow + d.name;
                }
                if (this.currentUser().id === d.id) {
                  showFollow = true;
                }
              })
            }
            this.$set(item, "follow", follow);
            // 编辑时初始化id
            this.$set(item, "follows", followIds);
            this.$set(item, "showFollow", showFollow);
          })
        });
        this.tableData =data.listObject;
      });
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
    refresh() {
      this.$refs.testPlanLitTable.clear();
      this.$refs.testPlanLitTable.isSelectDataAll(false);
      this.initTableData();
    },
    handleBatchSwitch() {
      let param = [];
      let size = 0;
      if (this.condition.selectAll) {
        this.$post("/test/plan/schedule/enable/total", this.condition, response => {
          size = response.data;
          this.$refs.scheduleBatchSwitch.open(param, size, this.condition.selectAll, this.condition);
        });
      } else {
        this.$refs.testPlanLitTable.selectRows.forEach((item) => {
          if (item.scheduleStatus !== null && item.scheduleStatus !== 'NOTSET') {
            param.push(item.scheduleId);
            size++;
          }
        });
        this.$refs.scheduleBatchSwitch.open(param, size, this.condition.selectAll, this.condition);
      }
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
    scheduleChange(row) {
      let param = {};
      let message = this.$t('api_test.home_page.running_task_list.confirm.close_title');
      param.taskID = row.scheduleId;
      param.enable = row.scheduleOpen;
      if (row.scheduleOpen) {
        message = this.$t('api_test.home_page.running_task_list.confirm.open_title');
      }
      this.$confirm(message, this.$t('commons.prompt'), {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning',
      }).then(() => {
        this.result = this.$post('/test/plan/update/scheduleByEnable', param, response => {
          if (row.scheduleOpen) {
            row.scheduleStatus = 'OPEN'
            row.scheduleCorn = response.data.value;
            row.scheduleExecuteTime = response.data.scheduleExecuteTime;
          } else {
            row.scheduleStatus = 'SHUT'
          }
          this.$success(this.$t('commons.save_success'));
        });
      }).catch(() => {
        row.scheduleOpen = param.enable = !param.enable;
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
      this.saveSortField(this.tableHeaderKey, this.condition.orders);
      this.initTableData();
    },
    openReport(plan) {
      this.$refs.testCaseReportView.open(plan);
    },
    scheduleTask(row) {
      row.redirectFrom = "testPlan";
      this.$refs.scheduleMaintain.open(row);
    },
    saveSortField(key, orders) {
      saveLastTableSortField(key, JSON.stringify(orders));
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
      this.$post('test/plan/copy/' + row.id, {}, () => {
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
      let {
        mode,
        reportType,
        onSampleError,
        runWithinResourcePool,
        resourcePoolId,
        envMap,
        environmentType,
        environmentGroupId
      } = config;
      let param = {mode, reportType, onSampleError, runWithinResourcePool, resourcePoolId, envMap};
      param.testPlanId = this.currentPlanId;
      param.projectId = getCurrentProjectID();
      param.userId = getCurrentUserId();
      param.triggerMode = 'MANUAL';
      param.environmentType = environmentType;
      param.environmentGroupId = environmentGroupId;
      param.requestOriginator = "TEST_PLAN";
      this.$refs.taskCenter.open();
      this.result = this.$post('test/plan/run/', param, () => {
        this.$success(this.$t('commons.run_success'));
      }, error => {
        // this.$error(error.message);
      });
    },
    saveFollow(row) {
      if (row.showFollow) {
        row.showFollow = false;
        for (let i = 0; i < row.follows.length; i++) {
          if (row.follows[i] === this.currentUser().id) {
            row.follows.splice(i, 1)
            break;
          }
        }
        this.$post('/test/plan/edit/follows/' + row.id, row.follows, () => {
          this.$success(this.$t('commons.cancel_follow_success'));
        });
        return
      }
      if (!row.showFollow) {
        row.showFollow = true;
        if (!row.follows) {
          row.follows = [];
        }
        row.follows.push(this.currentUser().id);
        this.$post('/test/plan/edit/follows/' + row.id, row.follows, () => {
          this.$success(this.$t('commons.follow_success'));
        });
        return
      }

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
  color: #85888E;
  border-color: #85888E;
  border-width: thin;
}

.scenario-ext-btn {
  margin-left: 10px;
}
</style>
