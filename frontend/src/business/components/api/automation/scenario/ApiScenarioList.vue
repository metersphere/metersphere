<template>
  <div>
    <el-card class="table-card-nopadding" v-loading="result.loading">
      <ms-table-header :condition.sync="condition" @search="selectByParam" title=""
                       :show-create="false" :tip="$t('commons.search_by_id_name_tag')"/>

      <ms-table
        :data="tableData"
        :screen-height="isRelate ? 'calc(100vh - 400px)' :  screenHeight"
        :condition="condition"
        :page-size="pageSize"
        :operators="isRelate ? [] : operators"
        :batch-operators="buttons"
        :total="total"
        :fields.sync="fields"
        :field-key=tableHeaderKey
        operator-width="200"
        @refresh="search(projectId)"
        @callBackSelectAll="callBackSelectAll"
        @callBackSelect="callBackSelect"
        @saveSortField="saveSortField"
        ref="scenarioTable">
        <ms-table-column
          prop="deleteTime"
          sortable
          v-if="this.trashEnable"
          :fields-width="fieldsWidth"
          :label="$t('commons.delete_time')"
          min-width="150px">
          <template v-slot:default="scope">
            <span>{{ scope.row.deleteTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="deleteUser"
          :fields-width="fieldsWidth"
          v-if="this.trashEnable"
          :label="$t('commons.delete_user')"
          min-width="120"/>

        <span v-for="(item) in fields" :key="item.key">

          <ms-table-column v-if="item.id == 'num' && !customNum"
                           prop="num"
                           label="ID"
                           sortable
                           :fields-width="fieldsWidth"
                           min-width="120px">
            <template slot-scope="scope">
              <!--<span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.num }} </span>-->
              <el-tooltip content="编辑">
                <a style="cursor:pointer" @click="edit(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column
            v-if="item.id == 'num' && customNum" prop="customNum"
            label="ID"
            sortable
            :fields-width="fieldsWidth"
            min-width="120px">
            <template slot-scope="scope">
              <!--<span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.customNum }} </span>-->
              <el-tooltip content="编辑">
                <a style="cursor:pointer" @click="edit(scope.row)"> {{ scope.row.customNum }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column prop="name"
                           sortable
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('api_test.automation.scenario_name')"
                           min-width="150px"/>

          <ms-table-column
            prop="level"
            sortable
            :field="item"
            :fields-width="fieldsWidth"
            :filters="LEVEL_FILTERS"
            min-width="130px"
            :label="$t('api_test.automation.case_level')">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level"/>
            </template>
          </ms-table-column>

          <ms-table-column prop="status"
                           :label="$t('test_track.plan.plan_status')"
                           sortable
                           :field="item"
                           :fields-width="fieldsWidth"
                           :filters="STATUS_FILTERS"
                           min-width="120px">
            <template v-slot:default="scope">
              <plan-status-table-item :value="scope.row.status"/>
            </template>
          </ms-table-column>

          <ms-table-column prop="tags"
                           :field="item"
                           :fields-width="fieldsWidth"
                           min-width="120px"
                           :showOverflowTooltip="false"
                           :label="$t('api_test.automation.tag')">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                      :content="itemName" :show-tooltip="true"
                      tooltip style="margin-left: 0px; margin-right: 2px"/>
              <span/>
            </template>
          </ms-table-column>

          <ms-table-column prop="principalName"
                           min-width="120px"
                           :label="$t('api_test.definition.api_principal')"
                           :filters="userFilters"
                           :field="item"
                           :fields-width="fieldsWidth"
                           sortable/>
          <ms-table-column prop="userName" min-width="120px"
                           :label="$t('api_test.automation.creator')"
                           :filters="userFilters"
                           :field="item"
                           :fields-width="fieldsWidth"
                           sortable="custom"/>
          <ms-table-column prop="updateTime"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('api_test.automation.update_time')"
                           sortable
                           min-width="180px">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </ms-table-column>
          <ms-table-column prop="createTime"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('commons.create_time')"
                           sortable
                           min-width="180px">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </ms-table-column>

          <ms-table-column prop="stepTotal"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('api_test.automation.step')"
                           min-width="80px"/>
          <ms-table-column prop="lastResult"
                           :label="$t('api_test.automation.last_result')"
                           :filters="RESULT_FILTERS"
                           :field="item"
                           :fields-width="fieldsWidth"
                           sortable
                           min-width="130px">
            <template v-slot:default="{row}">
              <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success'">
                {{ $t('api_test.automation.success') }}
              </el-link>
              <el-link type="danger" @click="showReport(row)" v-else-if="row.lastResult === 'Fail'">
                {{ $t('api_test.automation.fail') }}
              </el-link>
            </template>
          </ms-table-column>

          <ms-table-column prop="passRate"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('api_test.automation.passing_rate')"
                           min-width="120px"/>
        </span>

        <template v-slot:opt-behind="scope">
          <ms-scenario-extend-buttons v-if="!trashEnable" style="display: contents" @openScenario="openScenario" :row="scope.row"/>
        </template>

      </ms-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
      <div>
        <!-- 执行结果 -->
        <el-drawer :visible.sync="runVisible" :destroy-on-close="true" direction="ltr" :withHeader="true" :modal="false"
                   size="90%">
          <sysn-api-report-detail @refresh="search" :debug="true" :scenario="currentScenario" :scenarioId="scenarioId" :infoDb="infoDb" :report-id="reportId" :currentProjectId="projectId"/>
        </el-drawer>
        <!-- 执行结果 -->
        <el-drawer :visible.sync="showReportVisible" :destroy-on-close="true" direction="ltr" :withHeader="true" :modal="false"
                   size="90%">
          <ms-api-report-detail @refresh="search" :infoDb="infoDb" :report-id="reportId" :currentProjectId="projectId"/>
        </el-drawer>
        <!--测试计划-->
        <el-drawer :visible.sync="planVisible" :destroy-on-close="true" direction="ltr" :withHeader="false"
                   :title="$t('test_track.plan_view.test_result')" :modal="false" size="90%">
          <ms-test-plan-list @addTestPlan="addTestPlan(arguments)" @cancel="cancel" ref="testPlanList"
                             :scenario-condition="condition" :row="selectRows"/>
        </el-drawer>
      </div>
    </el-card>

    <batch-edit ref="batchEdit" @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr"
                :dialog-title="$t('test_track.case.batch_edit_case')"/>
    <batch-move @refresh="search" @moveSave="moveSave" ref="testBatchMove"/>
    <ms-run-mode @handleRunBatch="handleRunBatch" ref="runMode"/>
    <ms-task-center ref="taskCenter"/>
  </div>
</template>

<script>
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import MsTag from "../../../common/components/MsTag";
import {downloadFile, getCurrentProjectID, getUUID, objToStrMap, strMapToObj} from "@/common/js/utils";
import SysnApiReportDetail from "../report/SysnApiReportDetail";
import MsApiReportDetail from "../report/ApiReportDetail";
import MsTableMoreBtn from "./TableMoreBtn";
import MsScenarioExtendButtons from "@/business/components/api/automation/scenario/ScenarioExtendBtns";
import MsTestPlanList from "./testplan/TestPlanList";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import {API_SCENARIO_CONFIGS} from "@/business/components/common/components/search/search-components";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import PriorityTableItem from "../../../track/common/tableItems/planview/PriorityTableItem";
import PlanStatusTableItem from "../../../track/common/tableItems/plan/PlanStatusTableItem";
import BatchEdit from "../../../track/case/components/BatchEdit";
import {API_SCENARIO_LIST, PROJECT_NAME, WORKSPACE_ID} from "../../../../../common/js/constants";
import EnvironmentSelect from "../../definition/components/environment/EnvironmentSelect";
import BatchMove from "../../../track/case/components/BatchMove";
import MsRunMode from "./common/RunMode";
import MsTaskCenter from "../../../task/TaskCenter";

import {
  getCustomTableHeader, getCustomTableWidth, getLastTableSortField, saveLastTableSortField
} from "@/common/js/tableUtils";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import {API_SCENARIO_FILTERS} from "@/common/js/table-constants";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTable from "@/business/components/common/components/table/MsTable";

export default {
  name: "MsApiScenarioList",
  components: {
    MsTable,
    MsTableColumn,
    HeaderLabelOperate,
    HeaderCustom,
    BatchMove,
    EnvironmentSelect,
    BatchEdit,
    PlanStatusTableItem,
    PriorityTableItem,
    MsTableHeaderSelectPopover,
    MsTablePagination,
    MsTableMoreBtn,
    ShowMoreBtn,
    MsTableHeader,
    MsTag,
    MsApiReportDetail,
    SysnApiReportDetail,
    MsScenarioExtendButtons,
    MsTestPlanList,
    MsTableOperatorButton,
    MsRunMode,
    MsTaskCenter
  },
  props: {
    referenced: {
      type: Boolean,
      default: false,
    },
    isReferenceTable: {
      type: Boolean,
      default: false,
    },
    selectNodeIds: Array,
    selectProjectId: {
      type: String,
      default: ""
    },
    trashEnable: {
      type: Boolean,
      default: false,
    },
    moduleTree: {
      type: Array,
      default() {
        return [];
      },
    },
    moduleOptions: {
      type: Array,
      default() {
        return [];
      },
    },
    //用于判断是否是只读用户
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    customNum: {
      type: Boolean,
      default: false
    },
    initApiTableOpretion: String,
    isRelate: Boolean,
  },
  data() {
    return {
      projectName: "",
      result: {},
      tableHeaderKey: "API_SCENARIO",
      type: API_SCENARIO_LIST,
      fields: getCustomTableHeader('API_SCENARIO'),
      fieldsWidth: getCustomTableWidth('API_SCENARIO'),
      screenHeight: 'calc(100vh - 228px)',//屏幕高度,
      condition: {
        components: API_SCENARIO_CONFIGS
      },
      scenarioId: "",
      currentScenario: {},
      schedule: {},
      tableData: [],
      selectDataRange: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: "",
      batchReportId: "",
      content: {},
      infoDb: false,
      runVisible: false,
      showReportVisible: false,
      planVisible: false,
      runData: [],
      report: {},
      selectDataSize: 0,
      selectAll: false,
      userFilters: [],
      operators: [],
      selectRows: new Set(),
      trashOperators: [
        {
          tip: this.$t('commons.reduction'),
          icon: "el-icon-refresh-left",
          exec: this.reductionApi,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT']
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.remove,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
      ],
      unTrashOperators: [
        {
          tip: this.$t('api_test.automation.execute'),
          icon: "el-icon-video-play",
          exec: this.execute,
          class: "run-button",
          permissions: ['PROJECT_API_SCENARIO:READ+RUN']
        },
        {
          tip: this.$t('commons.edit'),
          icon: "el-icon-edit",
          exec: this.edit,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT']
        },
        {
          tip: this.$t('api_test.automation.copy'),
          icon: "el-icon-document-copy",
          exec: this.copy,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT']
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.remove,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
      ],
      buttons: [],
      trashButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
        {
          name: "批量恢复", handleClick: this.handleBatchRestore
        },
      ],
      unTrashButtons: [
        {
          name: this.$t('api_test.automation.batch_add_plan'),
          handleClick: this.handleBatchAddCase,
          permissions: ['PROJECT_API_SCENARIO:READ+MOVE_BATCH']
        },
        {
          name: this.$t('api_test.automation.batch_execute'),
          handleClick: this.handleBatchExecute,
          permissions: ['PROJECT_API_SCENARIO:READ+RUN']
        },
        {
          name: this.$t('test_track.case.batch_edit_case'),
          handleClick: this.handleBatchEdit,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT']
        },
        {
          name: this.$t('api_test.create_performance_test_batch'),
          handleClick: this.batchCreatePerformance,
          permissions: ['PROJECT_API_SCENARIO:READ+CREATE_PERFORMANCE_BATCH']
        },
        {
          name: this.$t('api_test.batch_copy'),
          handleClick: this.batchCopy,
          permissions: ['PROJECT_API_SCENARIO:READ+BATCH_COPY']
        },
        {
          name: this.$t('test_track.case.batch_move_case'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_API_SCENARIO:READ+MOVE_BATCH']
        },
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
      ],
      ...API_SCENARIO_FILTERS,
      typeArr: [
        {id: 'level', name: this.$t('test_track.case.priority')},
        {id: 'status', name: this.$t('test_track.plan.plan_status')},
        {
          id: 'principal',
          name: this.$t('api_test.definition.request.responsible'),
          optionMethod: this.getPrincipalOptions
        },
        // {id: 'environmentId', name: this.$t('api_test.definition.request.run_env'), optionMethod: this.getEnvsOptions},
        {id: 'projectEnv', name: this.$t('api_test.definition.request.run_env')},
      ],
      valueArr: {
        level: [
          {name: 'P0', id: 'P0'},
          {name: 'P1', id: 'P1'},
          {name: 'P2', id: 'P2'},
          {name: 'P3', id: 'P3'}
        ],
        status: [
          {name: this.$t('test_track.plan.plan_status_prepare'), id: 'Prepare'},
          {name: this.$t('test_track.plan.plan_status_running'), id: 'Underway'},
          {name: this.$t('test_track.plan.plan_status_completed'), id: 'Completed'}
        ],
        principal: [],
        environmentId: [],
        projectEnv: [],
        projectId: ''
      },
    };
  },
  created() {
    this.projectId = getCurrentProjectID();
    if (!this.projectName || this.projectName === "") {
      this.getProjectName();
    }
    this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};

    if (this.trashEnable) {
      this.condition.filters = {status: ["Trash"]};
      this.condition.moduleIds = [];
      this.operators = this.trashOperators;
      this.buttons = this.trashButtons;
    } else {
      if (!this.isReferenceTable) {
        this.operators = this.unTrashOperators;
        this.buttons = this.unTrashButtons;
      } else {
        this.operators = this.unTrashOperators;
        this.buttons = this.unTrashButtons;
      }
    }

    let orderArr = this.getSortField();
    if (orderArr) {
      this.condition.orders = orderArr;
    }
    this.search();
    this.getPrincipalOptions([]);

  },
  watch: {
    selectNodeIds() {
      this.currentPage = 1;
      this.$refs.scenarioTable.clear();
      this.selectProjectId ? this.search(this.selectProjectId) : this.search();
    },
    trashEnable() {
      if (this.trashEnable) {
        this.condition.filters = {status: ["Trash"]};
        this.condition.moduleIds = [];
        this.operators = this.trashOperators;
        this.buttons = this.trashButtons;
      } else {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
        this.operators = this.unTrashOperators;
        this.buttons = this.unTrashButtons;
      }
      this.$refs.scenarioTable.clear();
      this.search();
    },
    batchReportId() {
      this.result.loading = true;
      this.getReport();
    }
  },
  computed: {
    isNotRunning() {
      return "Running" !== this.report.status;
    },
  },
  methods: {
    getProjectName() {
      this.$get('project/get/' + this.projectId, response => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
        }
      });
    },
    selectByParam() {
      this.changeSelectDataRangeAll();
      this.search();
    },
    search(projectId) {
      if (this.needRefreshModule()) {
        this.$emit('refreshTree');
      }
      if (this.selectProjectId) {
        projectId = this.selectProjectId;
      }
      this.selectRows = new Set();
      this.condition.moduleIds = this.selectNodeIds;
      if (this.trashEnable) {
        this.condition.filters = {status: ["Trash"]};
        this.condition.moduleIds = [];
      }

      // todo
      if (projectId != null && typeof projectId === 'string') {
        this.condition.projectId = projectId;
      } else if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }

      //检查是否只查询本周数据
      this.condition.selectThisWeedData = false;
      this.condition.executeStatus = null;
      this.isSelectThissWeekData();
      switch (this.selectDataRange) {
        case 'thisWeekCount':
          this.condition.selectThisWeedData = true;
          break;
        case 'unExecute':
          this.condition.executeStatus = 'unExecute';
          break;
        case 'executeFailed':
          this.condition.executeStatus = 'executeFailed';
          break;
        case 'executePass':
          this.condition.executeStatus = 'executePass';
          break;
      }
      let url = "/api/automation/list/" + this.currentPage + "/" + this.pageSize;
      if (this.condition.projectId) {
        this.result = this.$post(url, this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          if (this.$refs.scenarioTable) {
            this.$refs.scenarioTable.clear();
            this.$nextTick(() => {
              this.$refs.scenarioTable.doLayout();
            });
          }
          this.$emit('getTrashCase');
        });
      }
    },
    handleCommand(cmd) {
      let table = this.$refs.scenarioTable;
      switch (cmd) {
        case "table":
          this.condition.selectAll = false;
          table.toggleAllSelection();
          break;
        case "all":
          this.condition.selectAll = true;
          break;
      }
    },
    handleBatchAddCase() {
      this.selectRows = this.$refs.scenarioTable.selectRows;
      this.planVisible = true;
    },
    handleBatchEdit() {
      this.$refs.batchEdit.setScenarioSelectRows(this.$refs.scenarioTable.selectRows, "scenario");
      if (this.condition.selectAll) {
        this.condition.ids = [];
        let param = {};
        this.buildBatchParam(param);
        this.$post('/api/automation/listWithIds/all', param, response => {
          let dataRows = response.data;
          this.$refs.batchEdit.open(dataRows.size);
          this.$refs.batchEdit.setAllDataRows(dataRows);
          this.$refs.batchEdit.open(this.$refs.scenarioTable.selectDataCounts);
        });
      } else {
        this.$refs.batchEdit.setAllDataRows(new Set());
        this.$refs.batchEdit.open(this.$refs.scenarioTable.selectDataCounts);
      }
    },
    handleBatchMove() {
      this.$refs.testBatchMove.open(this.moduleTree, [], this.moduleOptions);
    },
    moveSave(param) {
      this.buildBatchParam(param);
      param.apiScenarioModuleId = param.nodeId;
      param.modulePath = param.nodePath;
      this.$post('/api/automation/batch/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.testBatchMove.close();
        this.search();
      });
    },
    batchEdit(form) {
      // 批量修改环境
      if (form.type === 'projectEnv') {
        let param = {};
        param.mapping = strMapToObj(form.map);
        param.envMap = strMapToObj(form.projectEnvMap);
        this.$post('/api/automation/batch/update/env', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      } else {
        // 批量修改其它
        let param = {};
        param[form.type] = form.value;
        this.buildBatchParam(param);
        this.$post('/api/automation/batch/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      }


    },
    getPrincipalOptions(option) {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        option.push(...response.data);
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
    getEnvsOptions(option) {
      this.$get('/api/environment/list/' + this.projectId, response => {
        option.push(...response.data);
        option.forEach(environment => {
          if (!(environment.config instanceof Object)) {
            environment.config = JSON.parse(environment.config);
          }
        });
      });
    },
    cancel() {
      this.planVisible = false;
    },
    addTestPlan(params) {
      let obj = {planIds: params[0], scenarioIds: this.$refs.scenarioTable.selectIds};

      // todo 选取全部数据
      if (this.condition.selectAll) {
        this.$warning("暂不支持批量添加所有场景到测试计划！");
      }

      this.planVisible = false;

      obj.mapping = strMapToObj(params[2]);
      obj.envMap = strMapToObj(params[1]);

      this.$post("/api/automation/scenario/plan", obj, response => {
        this.$success(this.$t("commons.save_success"));
        this.search();
      });
    },
    getReport() {
      if (this.batchReportId) {
        this.result.loading = false;
        this.$success(this.$t('commons.run_message'));
        this.$refs.taskCenter.open();
      }
    },
    buildBatchParam(param) {
      param.ids = this.$refs.scenarioTable.selectIds;
      param.projectId = this.projectId;
      param.condition = this.condition;
    },
    handleBatchExecute() {
      this.$refs.runMode.open();

    },
    orderBySelectRows() {
      let selectIds = this.$refs.scenarioTable.selectIds;
      let array = [];
      for (let i in this.tableData) {
        if (selectIds.indexOf(this.tableData[i].id) !== -1) {
          array.push(this.tableData[i].id);
        }
      }
      return array;
    },

    handleRunBatch(config) {
      this.infoDb = false;
      let url = "/api/automation/run/batch";
      let run = {config: config};
      run.id = getUUID();
      //按照列表排序
      let ids = this.orderBySelectRows();
      run.ids = ids;
      run.projectId = this.projectId;
      run.condition = this.condition;
      this.$post(url, run, () => {
        this.runVisible = false;
        this.batchReportId = run.id;
      });
    },
    edit(row) {
      let data = JSON.parse(JSON.stringify(row));
      this.$emit('edit', data);
    },
    reductionApi(row) {
      this.$post("/api/automation/reduction", [row.id], response => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    handleBatchRestore() {
      let ids = this.$refs.scenarioTable.selectIds;

      let params = {};
      this.buildBatchParam(params);
      params.ids = ids;

      this.$post("/api/automation/id/all", params, response => {
        let idParams = response.data;
        this.$post("/api/automation/reduction", idParams, response => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      });
    },
    handleDeleteBatch(row) {
      if (this.trashEnable) {
        //let ids = Array.from(this.selectRows).map(row => row.id);
        let param = {};
        this.buildBatchParam(param);
        this.result.loading = true;
        this.$post('/api/automation/deleteBatchByCondition/', param, () => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        }, (error) => {
          this.search();
        });
        return;
      }else {
        let param = {};
        this.buildBatchParam(param);
        this.$post('/api/automation/checkBeforeDelete/', param, response => {

          let checkResult = response.data;
          let alertMsg = this.$t('api_test.definition.request.delete_confirm') + " ？";
          if(!checkResult.deleteFlag){
            alertMsg = "";
            checkResult.checkMsg.forEach(item => {
              alertMsg+=item+";";
            });
            if(alertMsg === ""){
              alertMsg = this.$t('api_test.definition.request.delete_confirm') + " ？";
            } else {
              alertMsg += this.$t('api_test.is_continue') + " ？";
            }
          }

          this.$alert(alertMsg, '', {
            confirmButtonText: this.$t('commons.confirm'),
            cancelButtonText: this.$t('commons.cancel'),
            callback: (action) => {
              if (action === 'confirm') {
                this.$post('/api/automation/removeToGcByBatch/', param, () => {
                  this.$success(this.$t('commons.delete_success'));
                  this.search();
                });
              }
            }
          });
        });
      }
    },

    execute(row) {
      this.infoDb = false;
      this.scenarioId = row.id;
      let url = "/api/automation/run";
      let run = {};
      let scenarioIds = [];
      scenarioIds.push(row.id);
      run.id = getUUID();
      run.projectId = this.projectId;
      run.ids = scenarioIds;
      this.$post(url, run, response => {
        this.runVisible = true;
        this.reportId = run.id;
      });
    },
    copy(row) {
      let rowParam = JSON.parse(JSON.stringify(row));
      rowParam.copy = true;
      rowParam.name = 'copy_' + rowParam.name;
      rowParam.customNum = '';
      this.$emit('edit', rowParam);
    },
    showReport(row) {
      this.showReportVisible = true;
      this.infoDb = true;
      this.reportId = row.reportId;
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    isSelectThissWeekData() {
      let dataRange = this.$route.params.dataSelectRange;
      this.selectDataRange = dataRange;
    },
    changeSelectDataRangeAll() {
      this.$emit("changeSelectDataRangeAll");
    },
    remove(row) {
      if (this.trashEnable) {
        this.$get('/api/automation/delete/' + row.id, () => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        });
        return;
      }else {
        let param = {};
        this.buildBatchParam(param);
        param.ids = [row.id];
        this.$post('/api/automation/checkBeforeDelete/', param, response => {
          let checkResult = response.data;
          let alertMsg = this.$t('api_test.definition.request.delete_confirm') +" ？";
          if(!checkResult.deleteFlag){
            alertMsg = "";
            checkResult.checkMsg.forEach(item => {
              alertMsg+=item+";";
            });
            if(alertMsg === ""){
              alertMsg = this.$t('api_test.definition.request.delete_confirm') +" ？";
            } else {
              alertMsg += this.$t('api_test.is_continue') + " ？";
            }
          }
          this.$alert(alertMsg, '', {
            confirmButtonText: this.$t('commons.confirm'),
            cancelButtonText: this.$t('commons.cancel'),
            callback: (action) => {
              if (action === 'confirm') {
                this.$post('/api/automation/removeToGcByBatch/', param, () => {
                  this.$success(this.$t('commons.delete_success'));
                  this.search();
                });
              }
            }
          });
        });
      }
    },
    openScenario(item) {
      this.$emit('openScenario', item);
    },
    exportApi(nodeTree) {
      let param = {};
      this.projectId = getCurrentProjectID();
      this.$get('project/get/' + this.projectId, response => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
          this.buildBatchParam(param);
          if (param.ids === undefined || param.ids.length < 1) {
            this.$warning(this.$t("api_test.automation.scenario.check_case"));
            return;
          }
          this.result.loading = true;
          this.result = this.$post("/api/automation/export", param, response => {
            this.result.loading = false;
            let obj = response.data;
            obj.nodeTree = nodeTree;
            downloadFile("Metersphere_Scenario_" + this.projectName + ".json", JSON.stringify(obj));
          });
        }
      });
    },
    exportJmx() {
      let param = {};
      this.buildBatchParam(param);
      if (param.ids === undefined || param.ids.length < 1) {
        this.$warning(this.$t("api_test.automation.scenario.check_case"));
        return;
      }
      this.result.loading = true;
      this.result = this.$post("/api/automation/export/jmx", param, response => {
        this.result.loading = false;
        let obj = response.data;
        if (obj && obj.length > 0) {
          obj.forEach(item => {
            downloadFile(item.name + ".jmx", item.jmx);
          });
        }
      });
    },
    getConditions() {
      return this.condition;
    },
    needRefreshModule() {
      if (this.initApiTableOpretion === '0') {
        return true;
      } else {
        this.$emit('updateInitApiTableOpretion', '0');
        return false;
      }
    },
    callBackSelectAll(selection) {
      this.$emit('selection', selection);
    },
    callBackSelect(selection) {
      this.$emit('selection', selection);
    },
    batchCreatePerformance() {
      this.$alert(this.$t('api_test.definition.request.batch_to_performance_confirm') + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.infoDb = false;
            let param = {};
            this.buildBatchParam(param);
            this.$post('/api/automation/batchGenPerformanceTestJmx/', param, response => {
              let returnDataList = response.data;
              let jmxObjList = [];
              returnDataList.forEach(item => {
                let jmxObj = {};
                jmxObj.name = item.name;
                jmxObj.xml = item.xml;
                jmxObj.attachFiles = item.attachFiles;
                jmxObj.attachByteFiles = item.attachByteFiles;
                jmxObj.scenarioId = item.id;
                jmxObjList.push(jmxObj);
              });
              this.$store.commit('setScenarioJmxs', {
                name: 'Scenarios',
                jmxs: jmxObjList
              });
              this.$router.push({
                path: "/performance/test/create"
              });
            });
          }
        }
      });
    },
    batchCopy() {
      this.$alert(this.$t('api_test.definition.request.batch_copy_confirm') + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.infoDb = false;
            let param = {};
            this.buildBatchParam(param);
            this.$post('/api/automation/batchCopy', param, response => {
              this.$success(this.$t('api_test.definition.request.batch_copy_end'));
              this.search();
            });
          }
        }
      });
    },
    saveSortField(key, orders) {
      saveLastTableSortField(key, JSON.stringify(orders));
    },
    getSortField() {
      let orderJsonStr = getLastTableSortField(this.tableHeaderKey);
      let returnObj = null;
      if (orderJsonStr) {
        try {
          returnObj = JSON.parse(orderJsonStr);
        } catch (e) {
          return null;
        }
      }
      return returnObj;
    }
  }
};
</script>

<style scoped>
/deep/ .el-drawer__header {
  margin-bottom: 0px;
}

/deep/ .el-table__fixed-body-wrapper {
  z-index: auto !important;
}

/deep/ .el-table__fixed-right {
  height: 100% !important;
}

/deep/ .el-card__header {
  padding: 10px;
}
</style>
