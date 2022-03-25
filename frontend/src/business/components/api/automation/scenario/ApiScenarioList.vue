<template>
  <div>
    <el-card class="table-card-nopadding" v-loading="result.loading">
      <slot name="version"></slot>

      <ms-table-search-bar :condition.sync="condition" @change="search" class="search-input"
                           :tip="$t('commons.search_by_id_name_tag')"/>
      <ms-table-adv-search-bar :condition.sync="condition" @search="search" class="adv-search-bar"/>

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
        :remember-order="true"
        operator-width="200"
        :enable-order-drag="enableOrderDrag"
        row-key="id"
        :row-order-group-id="condition.projectId"
        :row-order-func="editApiScenarioCaseOrder"
        @refresh="search(projectId)"
        @callBackSelectAll="callBackSelectAll"
        @callBackSelect="callBackSelect"
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
              <el-tooltip :content="$t('commons.edit')">
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
              <el-tooltip :content="$t('commons.edit')">
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
            :filters="apiscenariofilters.LEVEL_FILTERS"
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
                           :filters="apiscenariofilters.STATUS_FILTERS"
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
                      :content="itemName" :show-tooltip="scope.row.tags.length===1&&itemName.length*12<=120"
                      tooltip style="margin-left: 0px; margin-right: 2px"/>
              <span/>
            </template>
          </ms-table-column>

          <ms-table-column
            :label="$t('project.version.name')"
            :field="item"
            :fields-width="fieldsWidth"
            :filters="versionFilters"
            min-width="100px"
            prop="versionId">
          <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </ms-table-column>

          <ms-table-column prop="principalName"
                           min-width="120px"
                           :label="$t('api_test.definition.request.responsible')"
                           :filters="userFilters"
                           :field="item"
                           :fields-width="fieldsWidth"
                           sortable/>
          <ms-table-column prop="creatorName" min-width="120px"
                           :label="$t('api_test.automation.creator')"
                           :filters="userFilters"
                           :field="item"
                           :fields-width="fieldsWidth"
                           sortable="custom"/>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="environmentMap"
            :label="$t('commons.environment')"
            min-width="180">
            <template v-slot:default="{row}">
              <div v-if="row.environmentMap">
                <span v-for="(k, v, index) in row.environmentMap" :key="index">
                  <span v-if="index===0">
                    <span class="project-name" :title="v">{{ v }}</span>:
                    <el-tag type="success" size="mini" effect="plain">
                      <span class="project-env">{{ k }}</span>
                    </el-tag>
                    <br/>
                  </span>
                  <el-popover
                    placement="top"
                    width="350"
                    trigger="click">
                    <div v-for="(k, v, index) in row.environmentMap" :key="index">
                      <span class="plan-case-env">{{ v }}:
                        <el-tag type="success" size="mini" effect="plain">{{ k }}</el-tag><br/>
                      </span>
                    </div>
                    <el-link v-if="index === 1" slot="reference" type="info" :underline="false" icon="el-icon-more"/>
                  </el-popover>
                </span>
              </div>
            </template>
          </ms-table-column>

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
                           :filters="apiscenariofilters.RESULT_FILTERS"
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
              <el-link type="danger" style="color: #F6972A" @click="showReport(row)"
                       v-else-if="row.lastResult === 'errorReportResult'">
                {{ $t('error_report_library.option.name') }}
              </el-link>
            </template>
          </ms-table-column>

          <ms-table-column prop="passRate"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('api_test.automation.passing_rate')"
                           min-width="120px"/>
        </span>

        <template v-slot:opt-before="scope">
          <ms-table-operator-button v-permission=" ['PROJECT_API_SCENARIO:READ+RUN']"
                                    :tip="$t('api_test.automation.execute')" icon="el-icon-video-play"
                                    class="run-button"
                                    @exec="run(scope.row)" v-if="!scope.row.isStop && !trashEnable"
                                    style="margin-right: 10px;"/>
          <el-tooltip :content="$t('report.stop_btn')" placement="top" :enterable="false" v-else>
            <el-button v-if="!trashEnable" @click.once="stop(scope.row)" size="mini"
                       style="color:white;padding: 0;width: 28px;height: 28px;margin-right: 10px;" class="stop-btn"
                       circle>
              <div style="transform: scale(0.72)">
                <span style="margin-left: -3.5px;font-weight: bold">STOP</span>
              </div>
            </el-button>
          </el-tooltip>

        </template>

        <template v-slot:opt-behind="scope">
          <ms-scenario-extend-buttons v-if="!trashEnable" style="display: contents" @openScenario="openScenario"
                                      :row="scope.row"/>
        </template>

      </ms-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
      <div>
        <!-- 执行结果 -->
        <el-drawer :visible.sync="runVisible" :destroy-on-close="true" direction="ltr" :withHeader="true" :modal="false"
                   size="90%">
          <sysn-api-report-detail @refresh="search" :debug="true" :scenario="currentScenario" :scenarioId="scenarioId"
                                  :infoDb="infoDb" :report-id="reportId" :currentProjectId="projectId"/>
        </el-drawer>
        <!-- 执行结果 -->
        <el-drawer :visible.sync="showReportVisible" :destroy-on-close="true" direction="ltr" :withHeader="true"
                   :modal="false"
                   size="90%">
          <ms-api-report-detail @invisible="showReportVisible = false" @refresh="search" :infoDb="infoDb"
                                :show-cancel-button="false"
                                :report-id="showReportId" :currentProjectId="projectId"/>
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
    <ms-run-mode
      :request="runRequest"
      @close="search"
      @handleRunBatch="handleRunBatch"
      ref="runMode"/>
    <ms-run :debug="true" :environment="projectEnvMap"
            :reportId="reportId"
            :saved="true"
            :executeType="'Saved'"
            :environment-type="environmentType" :environment-group-id="envGroupId"
            :run-data="debugData"
            @runRefresh="runRefresh"
            ref="runTest"/>
    <ms-task-center ref="taskCenter" :show-menu="false"/>
    <relationship-graph-drawer :graph-data="graphData" ref="relationshipGraph"/>
    <!--  删除接口提示  -->
    <list-item-delete-confirm ref="apiDeleteConfirm" @handleDelete="_handleDelete"/>
  </div>
</template>

<script>
import {downloadFile, getCurrentProjectID, getUUID, hasLicense, objToStrMap, strMapToObj} from "@/common/js/utils";
import {API_SCENARIO_CONFIGS} from "@/business/components/common/components/search/search-components";
import {API_SCENARIO_LIST} from "../../../../../common/js/constants";

import {
  buildBatchParam,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField
} from "@/common/js/tableUtils";
import {API_SCENARIO_FILTERS} from "@/common/js/table-constants";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import {editApiScenarioCaseOrder} from "@/business/components/api/automation/api-automation";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import axios from "axios";
import {getGraphByCondition} from "@/network/graph";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import ListItemDeleteConfirm from "@/business/components/common/components/ListItemDeleteConfirm";
import {Message} from "element-ui";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const relationshipGraphDrawer = requireComponent.keys().length > 0 ? requireComponent("./graph/RelationshipGraphDrawer.vue") : {};

export default {
  name: "MsApiScenarioList",
  components: {
    ListItemDeleteConfirm,
    MsTableAdvSearchBar,
    MsTableSearchBar,
    MsTable,
    MsTableColumn,
    HeaderLabelOperate,
    "relationshipGraphDrawer": relationshipGraphDrawer.default,
    HeaderCustom: () => import("@/business/components/common/head/HeaderCustom"),
    BatchMove: () => import("../../../track/case/components/BatchMove"),
    EnvironmentSelect: () => import("../../definition/components/environment/EnvironmentSelect"),
    BatchEdit: () => import("../../../track/case/components/BatchEdit"),
    PlanStatusTableItem: () => import("../../../track/common/tableItems/plan/PlanStatusTableItem"),
    PriorityTableItem: () => import("../../../track/common/tableItems/planview/PriorityTableItem"),
    MsTableHeaderSelectPopover: () => import("@/business/components/common/components/table/MsTableHeaderSelectPopover"),
    MsTablePagination: () => import("@/business/components/common/pagination/TablePagination"),
    MsTableMoreBtn: () => import("./TableMoreBtn"),
    ShowMoreBtn: () => import("@/business/components/track/case/components/ShowMoreBtn"),
    MsTableHeader: () => import("@/business/components/common/components/MsTableHeader"),
    MsTag: () => import("../../../common/components/MsTag"),
    MsApiReportDetail: () => import("../report/ApiReportDetail"),
    SysnApiReportDetail: () => import("../report/SysnApiReportDetail"),
    MsScenarioExtendButtons: () => import("@/business/components/api/automation/scenario/ScenarioExtendBtns"),
    MsTestPlanList: () => import("./testplan/TestPlanList"),
    MsTableOperatorButton: () => import("@/business/components/common/components/MsTableOperatorButton"),
    MsRunMode: () => import("./common/RunMode"),
    MsTaskCenter: () => import("../../../task/TaskCenter"),
    MsRun: () => import("./DebugRun")
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
      screenHeight: 'calc(100vh - 220px)',//屏幕高度,
      condition: {
        components: API_SCENARIO_CONFIGS
      },
      scenarioId: "",
      isMoveBatch: true,
      currentScenario: {},
      schedule: {},
      tableData: [],
      selectDataRange: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: "",
      showReportId: "",
      projectEnvMap: new Map(),
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
      versionFilters: [],
      operators: [],
      selectRows: new Set(),
      isStop: false,
      enableOrderDrag: true,
      debugData: {},
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
          tip: this.$t('commons.edit'),
          icon: "el-icon-edit",
          exec: this.edit,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT']
        },
        {
          tip: this.$t('api_test.automation.copy'),
          icon: "el-icon-document-copy",
          exec: this.copy,
          permissions: ['PROJECT_API_SCENARIO:READ+COPY']
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
          name: this.$t('commons.batch_restore'),
          handleClick: this.handleBatchRestore
        },
      ],
      unTrashButtons: [
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
          name: this.$t('test_track.case.batch_move_case'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_API_SCENARIO:READ+MOVE_BATCH']
        },
        {
          name: this.$t('api_test.batch_copy'),
          handleClick: this.handleBatchCopy,
          permissions: ['PROJECT_API_SCENARIO:READ+BATCH_COPY']
        },
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
        {
          name: this.$t('test_track.case.generate_dependencies'),
          handleClick: this.generateGraph,
          isXPack: true,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT']
        },
        {
          name: this.$t('api_test.automation.batch_add_plan'),
          handleClick: this.handleBatchAddCase,
          permissions: ['PROJECT_API_SCENARIO:READ+MOVE_BATCH']
        },
        {
          name: this.$t('api_test.create_performance_test_batch'),
          handleClick: this.batchCreatePerformance,
          permissions: ['PROJECT_API_SCENARIO:READ+CREATE_PERFORMANCE_BATCH']
        },
      ],
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
      graphData: {},
      environmentType: "",
      envGroupId: "",
      apiscenariofilters: {},
      runRequest: {},
      versionEnable: false,
    };
  },
  created() {
    this.apiscenariofilters = API_SCENARIO_FILTERS();
    this.$EventBus.$on('hide', id => {
      this.hideStopBtn(id);
    });
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


    if (this.trashEnable) {
      this.condition.orders = [{"name": "delete_time", "type": "desc"}];
    } else {
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
    }

    this.search();
    this.getPrincipalOptions([]);
    this.getVersionOptions();

    if (this.isRelate) {
      this.checkVersionEnable(this.selectProjectId);
    } else {
      this.checkVersionEnable(this.projectId);
    }

    // 通知过来的数据跳转到编辑
    if (this.$route.query.resourceId) {
      this.$get('/api/automation/get/' + this.$route.query.resourceId, (response) => {
        this.edit(response.data);
      });
    }
  },
  beforeDestroy() {
    this.$EventBus.$off("hide");
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
    },
  },
  computed: {
    isNotRunning() {
      return "Running" !== this.report.status;
    },
    editApiScenarioCaseOrder() {
      return editApiScenarioCaseOrder;
    }
  },
  methods: {
    generateGraph() {
      getGraphByCondition('API_SCENARIO', buildBatchParam(this, this.$refs.scenarioTable.selectIds), (data) => {
        this.graphData = data;
        this.$refs.relationshipGraph.open();
      });
    },
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

      this.enableOrderDrag = this.condition.orders.length > 0 ? false : true;

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
      if (this.selectDataRange != null) {
        let selectParamArr = this.selectDataRange.split(":");
        if (selectParamArr.length === 2) {
          if (selectParamArr[0] === "list") {
            let ids = selectParamArr[1].split(",");
            this.condition.ids = ids;
          }
        }
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
      this.isMoveBatch = true;
      this.$refs.testBatchMove.open(this.moduleTree, [], this.moduleOptions);
    },
    handleBatchCopy() {
      this.isMoveBatch = false;
      this.$refs.testBatchMove.open(this.moduleTree, this.$refs.scenarioTable.selectIds, this.moduleOptions);
    },
    moveSave(param) {
      this.buildBatchParam(param);
      param.apiScenarioModuleId = param.nodeId;
      param.modulePath = param.nodePath;
      let url = '/api/automation/batch/edit';
      if (!this.isMoveBatch)
        url = '/api/automation/batch/copy';
      this.$post(url, param, () => {
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
        param.environmentType = form.environmentType;
        param.environmentGroupId = form.envGroupId;
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
    getVersionOptions(currentVersion) {
      if (hasLicense()) {
        this.$get('/project/version/get-project-versions/' + getCurrentProjectID(), response => {
          if (currentVersion) {
            this.versionFilters = response.data.filter(u => u.id === currentVersion).map(u => {
              return {text: u.name, value: u.id};
            });
          } else {
            this.versionFilters = response.data.map(u => {
              return {text: u.name, value: u.id};
            });
          }
        });
      }
    },
    checkVersionEnable(projectId) {
      if (!projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + projectId, response => {
          this.versionEnable = response.data;
          if (!response.data) {
            this.fields = this.fields.filter(f => f.id !== 'versionId');
          }
        });
      }
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
        this.$warning(this.$t('api_test.scenario.warning_context'));
      }

      this.planVisible = false;

      obj.mapping = strMapToObj(params[2]);
      obj.envMap = strMapToObj(params[1]);
      obj.environmentType = params[3];
      obj.envGroupId = params[4];

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
      let run = {};
      run.id = getUUID();
      //按照列表排序
      let ids = this.orderBySelectRows();
      run.ids = ids;
      run.projectId = this.projectId;
      run.condition = this.condition;
      this.runRequest = run;
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
      } else {
        let param = {};
        this.buildBatchParam(param);
        this.$post('/api/automation/checkBeforeDelete/', param, response => {

          let checkResult = response.data;
          let alertMsg = this.$t('load_test.delete_threadgroup_confirm') + " ？";
          if (!checkResult.deleteFlag) {
            alertMsg = "";
            checkResult.checkMsg.forEach(item => {
              alertMsg += item + ";";
            });
            if (alertMsg === "") {
              alertMsg = this.$t('load_test.delete_threadgroup_confirm') + " ？";
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
    getApiScenario(scenarioId) {
      return new Promise((resolve) => {
        this.result = this.$get("/api/automation/getApiScenario/" + scenarioId, response => {
          if (response.data) {
            this.currentScenario = response.data;
            this.currentScenario.clazzName = TYPE_TO_C.get("scenario");
            if (response.data.scenarioDefinition != null) {
              let obj = JSON.parse(response.data.scenarioDefinition);
              this.currentScenario.scenarioDefinition = obj;
              this.currentScenario.name = response.data.name;
              if (this.currentScenario.scenarioDefinition && this.currentScenario.scenarioDefinition.hashTree) {
                this.sort(this.currentScenario.scenarioDefinition.hashTree);
              }
              resolve();
            }
          }
        });
      });
    },
    sort(stepArray) {
      for (let i in stepArray) {
        stepArray[i].index = Number(i) + 1;
        if (!stepArray[i].resourceId) {
          stepArray[i].resourceId = getUUID();
        }
        if (!stepArray[i].clazzName) {
          stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
        }
        if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
          stepArray[i].document = {type: "JSON", data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}};
        }
        if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
          stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
        }
        if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
          this.sort(stepArray[i].hashTree);
        }
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
      run.executeType = "Saved";
      this.$post(url, run, response => {
        this.runVisible = true;
        this.$set(row, "isStop", true);
        this.reportId = run.id;
      }, () => {
        this.$set(row, "isStop", false);
      });
    },
    runRefresh(row) {
      this.$set(row, "isStop", false);
    },
    run(row) {
      this.scenarioId = row.id;
      this.getApiScenario(row.id).then(() => {
        let scenarioStep = this.currentScenario.scenarioDefinition;
        if (scenarioStep) {
          this.debugData = {
            id: this.currentScenario.id,
            name: this.currentScenario.name,
            type: "scenario",
            variables: scenarioStep.variables,
            referenced: 'Created',
            onSampleError: scenarioStep.onSampleError,
            enableCookieShare: scenarioStep.enableCookieShare,
            headers: scenarioStep.headers,
            environmentMap: this.currentScenario.environmentJson ? objToStrMap(JSON.parse(this.currentScenario.environmentJson)) : new Map,
            hashTree: scenarioStep.hashTree
          };
          if (this.currentScenario.environmentJson) {
            this.projectEnvMap = objToStrMap(JSON.parse(this.currentScenario.environmentJson));
          }
          this.environmentType = this.currentScenario.environmentType;
          this.envGroupId = this.currentScenario.environmentGroupId;

          this.$get("/api/automation/checkScenarioEnv/" + this.currentScenario.id, res => {
            let data = res.data;
            if (!data) {
              this.$warning(this.$t('workspace.env_group.please_select_env_for_current_scenario'));
              return false;
            }
            this.reportId = getUUID().substring(0, 8);
            this.runVisible = true;
            this.$set(row, "isStop", true);
          });
        }
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
      this.showReportId = row.reportId;
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
      } else {
        let param = {};
        this.buildBatchParam(param);
        param.ids = [row.id];
        this.$post('/api/automation/checkBeforeDelete/', param, response => {
          let checkResult = response.data;
          let alertMsg = this.$t('load_test.delete_threadgroup_confirm') + " [" + row.name + "] ?";
          if (!checkResult.deleteFlag) {
            alertMsg = "";
            checkResult.checkMsg.forEach(item => {
              alertMsg += item;
            });
            if (alertMsg === "") {
              alertMsg = this.$t('load_test.delete_threadgroup_confirm') + " [" + row.name + "] ?";
            } else {
              alertMsg += this.$t('api_test.is_continue');
            }
          }
          //
          this.$get('/api/automation/versions/' + row.id, response => {
            if (hasLicense() && this.versionEnable && response.data.length > 1) {
              // 删除提供列表删除和全部版本删除
              this.$refs.apiDeleteConfirm.open(row, alertMsg);
            } else {
              this.$alert(alertMsg, '', {
                confirmButtonText: this.$t('commons.confirm'),
                cancelButtonText: this.$t('commons.cancel'),
                callback: (action) => {
                  if (action === 'confirm') {
                    this._handleDelete(row, false);
                  }
                }
              });
            }
          });
        });
      }
    },
    _handleDelete(api, deleteCurrentVersion) {
      // 删除指定版本
      if (deleteCurrentVersion) {
        this.$get('/api/automation/delete/' + api.versionId + '/' + api.refId, () => {
          this.$success(this.$t('commons.delete_success'));
          this.$refs.apiDeleteConfirm.close();
          this.search();
        });
      }
      // 删除全部版本
      else {
        let param = {};
        this.buildBatchParam(param);
        param.ids = [api.id];
        this.$post('/api/automation/removeToGcByBatch/', param, () => {
          this.$success(this.$t('commons.delete_success'));
          this.$refs.apiDeleteConfirm.close();
          this.search();
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
    fileDownload(url, param) {
      axios.post(url, param, {responseType: 'blob'})
        .then(response => {
          let link = document.createElement("a");
          link.href = window.URL.createObjectURL(new Blob([response.data], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8"}));
          link.download = "场景JMX文件集.zip";
          this.result.loading = false;
          link.click();
        }, error => {
          this.result.loading = false;
          if (error.response && error.response.status === 509) {
            let reader = new FileReader();
            reader.onload = function (event) {
              let content = reader.result;
              Message.error({message: content, showClose: true});
            };
            reader.readAsText(error.response.data);
          } else {
            this.$error("导出JMX文件失败，请检查是否选择环境");
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
      this.fileDownload("/api/automation/export/zip", param);
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
                jmxObj.version = item.version;
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
    stop(row) {
      let url = "/api/automation/stop/" + this.reportId;
      this.$get(url, () => {
        this.$set(row, "isStop", false);
      });
    },
    hideStopBtn(scenarioId) {
      for (let data of this.tableData) {
        if (scenarioId && scenarioId === data.id) {
          this.$set(data, "isStop", false);
        }
      }
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

.stop-btn {
  background-color: #E62424;
  border-color: #dd3636;
  color: white;
}

.plan-case-env {
  display: inline-block;
  padding: 0 0;
  max-width: 350px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
  margin-left: 5px;
}

.project-name {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 80px;
  vertical-align: middle;
}

.project-env {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  width: 50px;
  text-overflow: ellipsis;
  vertical-align: middle;
}


.search-input {
  float: right;
  width: 250px;
}

.adv-search-bar {
  float: right;
  margin-top: 5px;
  margin-right: 10px;
}
</style>
