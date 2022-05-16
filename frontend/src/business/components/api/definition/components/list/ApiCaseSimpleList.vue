<template>
  <span>
    <span>
      <div class="ms-opt-btn" v-if="apiDefinitionId && versionEnable">
          {{ $t('project.version.name') }}:  {{ apiDefinition.versionName }}
      </div>
      <ms-search
        :condition.sync="condition"
        :base-search-tip="$t('commons.search_by_id_name_tag')"
        @search="search">
      </ms-search>
      <el-button type="primary" style="float: right;margin-right: 10px" icon="el-icon-plus" size="small"
                 @click="addTestCase" v-if="apiDefinitionId">{{ $t('commons.add') }}
      </el-button>
      <ms-table
        v-loading="result.loading"
        :data="tableData"
        :select-node-ids="selectNodeIds"
        :condition="condition"
        :page-size="pageSize"
        :total="total"
        :operators="operators"
        :batch-operators="buttons"
        :screenHeight="screenHeight"
        :fields.sync="fields"
        :field-key="tableHeaderKey"
        :remember-order="true"
        :row-order-group-id="condition.projectId"
        :row-order-func="editApiTestCaseOrder"
        :enable-order-drag="enableOrderDrag"
        row-key="id"
        operator-width="190px"
        @refresh="initTable"
        ref="caseTable"
      >
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

          <ms-table-column
            prop="num"
            label="ID"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="80px"
            sortable>
            <template slot-scope="scope">
              <!-- 判断为只读用户的话不可点击ID进行编辑操作 -->
              <span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.num }} </span>
              <el-tooltip v-else :content="$t('commons.edit')">
                <a style="cursor:pointer" @click="handleTestCase(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="name"
            sortable="custom"
            min-width="160px"
            :label="$t('test_track.case.name')"/>

          <ms-table-column
            prop="priority"
            :filters="priorityFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            sortable
            :label="$t('test_track.case.priority')">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.priority"/>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="caseStatus"
            :filters="STATUS_FILTERS"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('commons.status')">
            <template v-slot:default="scope">
              <plan-status-table-item :value="scope.row.caseStatus"/>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="execResult"
            :filters="statusFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('test_track.plan_view.execute_result')">
            <template v-slot:default="scope">
              <div v-if="scope.row.status === 'Running'">
                  <i class="el-icon-loading ms-running"/>
                  <el-link :class="getStatusClass(scope.row.status)">
                    {{ getStatusTitle(scope.row.status) }}
                  </el-link>
              </div>
              <el-link v-else @click="getExecResult(scope.row)" :class="getStatusClass(scope.row.execResult)">
                {{ getStatusTitle(scope.row.execResult) }}
              </el-link>
            </template>
          </ms-table-column>

           <ms-table-column
             prop="passRate"
             :field="item"
             :fields-width="fieldsWidth"
             min-width="100px"
             :label="$t('commons.pass_rate')">
           </ms-table-column>

          <ms-table-column
            sortable="custom"
            prop="path"
            min-width="180px"
            :field="item"
            :fields-width="fieldsWidth"
            :label="'API'+ $t('api_test.definition.api_path')"/>

          <ms-table-column v-if="item.id=='tags'" prop="tags" width="120px" :label="$t('commons.tag')">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                      :show-tooltip="scope.row.tags.length===1&&itemName.length*12<=120"
                      :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
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

          <ms-table-column
            prop="environment"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.environment')"
          >
          </ms-table-column>

          <ms-table-column
            prop="createUser"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.create_user')"/>

          <ms-table-column
            sortable="updateTime"
            min-width="160px"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('api_test.definition.api_last_time')"
            prop="updateTime">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </ms-table-column>
          <ms-table-column
            prop="createTime"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.create_time')"
            sortable
            min-width="180px">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </ms-table-column>
        </span>

        <template v-if="!trashEnable" v-slot:opt-behind="scope">
          <ms-api-case-table-extend-btns
            @showCaseRef="showCaseRef"
            @showEnvironment="showEnvironment"
            @createPerformance="createPerformance"
            @showHistory="openHis"
            :row="scope.row"/>
        </template>

      </ms-table>

      <ms-table-pagination
        :change="initTable"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"/>
    </span>

    <api-case-list @showExecResult="showExecResult" @refreshCase="setRunning" :currentApi="selectCase" ref="caseList"
                   @stop="stop" @reLoadCase="initTable"/>
    <!--批量编辑-->
    <ms-batch-edit ref="batchEdit" :data-count="$refs.caseTable ? $refs.caseTable.selectDataCounts : 0"
                   @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr"/>
    <!--选择环境(当创建性能测试的时候)-->
    <ms-set-environment ref="setEnvironment" :testCase="clickRow" @createPerformance="createPerformance"/>
    <!--查看引用-->
    <ms-reference-view ref="viewRef"/>

    <ms-task-center ref="taskCenter" :show-menu="false"/>

    <ms-api-case-run-mode-with-env
      :project-id="projectId"
      @handleRunBatch="runBatch"
      @close="initTable"
      ref="batchRun"/>

    <el-dialog :close-on-click-modal="false" :title="$t('test_track.plan_view.test_result')" width="60%"
               :visible.sync="resVisible" class="api-import" destroy-on-close @close="resVisible=false">
      <ms-request-result-tail :response="response" ref="debugResult"/>
    </el-dialog>
  </span>

</template>

<script>

import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableOperator from "../../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
import MsTablePagination from "../../../../common/pagination/TablePagination";
import MsTag from "../../../../common/components/MsTag";
import MsApiCaseList from "../case/ApiCaseList";
import ApiCaseList from "../case/ApiCaseList";
import MsContainer from "../../../../common/components/MsContainer";
import MsBottomContainer from "../BottomContainer";
import ShowMoreBtn from "../../../../track/case/components/ShowMoreBtn";
import MsBatchEdit from "../basis/BatchEdit";
import MsApiCaseRunModeWithEnv from "./ApiCaseRunModeWithEnv";

import {API_METHOD_COLOUR, CASE_PRIORITY, DUBBO_METHOD, REQ_METHOD, SQL_METHOD, TCP_METHOD} from "../../model/JsonData";

import {getBodyUploadFiles, getCurrentProjectID, getUUID, hasLicense} from "@/common/js/utils";
import PriorityTableItem from "../../../../track/common/tableItems/planview/PriorityTableItem";
import MsApiCaseTableExtendBtns from "../reference/ApiCaseTableExtendBtns";
import MsReferenceView from "../reference/ReferenceView";
import MsSetEnvironment from "@/business/components/api/definition/components/basis/SetEnvironment";
import TestPlan from "@/business/components/api/definition/components/jmeter/components/test-plan";
import ThreadGroup from "@/business/components/api/definition/components/jmeter/components/thread-group";
import {parseEnvironment} from "@/business/components/api/test/model/EnvironmentModel";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import {API_CASE_CONFIGS} from "@/business/components/common/components/search/search-components";
import {
  _filter,
  _sort,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  handleRowDrop
} from "@/common/js/tableUtils";
import {API_CASE_LIST} from "@/common/js/constants";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import ApiCaseBatchRun from "@/business/components/api/definition/components/list/ApiCaseBatchRun";
import MsRequestResultTail from "../../../../api/definition/components/response/RequestResultTail";
import {editApiTestCaseOrder} from "@/network/api";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import i18n from "@/i18n/i18n";
import MsSearch from "@/business/components/common/components/search/MsSearch";

export default {
  name: "ApiCaseSimpleList",
  components: {
    ApiCaseBatchRun,
    HeaderLabelOperate,
    MsTableHeaderSelectPopover,
    MsSetEnvironment,
    ApiCaseList,
    PriorityTableItem,
    MsTableOperatorButton,
    MsTableOperator,
    MsTablePagination,
    MsTag,
    MsApiCaseList,
    MsContainer,
    MsBottomContainer,
    ShowMoreBtn,
    MsBatchEdit,
    MsApiCaseTableExtendBtns,
    MsReferenceView,
    MsTableAdvSearchBar,
    MsTable,
    MsTableColumn,
    MsRequestResultTail,
    MsApiCaseRunModeWithEnv,
    MsSearch,
    PlanStatusTableItem: () => import("../../../../track/common/tableItems/plan/PlanStatusTableItem"),
    MsTaskCenter: () => import("@/business/components/task/TaskCenter"),
  },
  data() {
    return {
      type: API_CASE_LIST,
      tableHeaderKey: "API_CASE",
      fields: getCustomTableHeader('API_CASE'),
      fieldsWidth: getCustomTableWidth('API_CASE'),
      condition: {
        components: API_CASE_CONFIGS
      },
      selectCase: {},
      result: {},
      moduleId: "",
      selectDataRange: "all",
      clickRow: {},
      buttons: [],
      enableOrderDrag: true,
      simpleButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteToGcBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE']
        },
        {
          name: this.$t('api_test.definition.request.batch_edit'),
          handleClick: this.handleEditBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_CASE']
        },
        {
          name: this.$t('api_test.automation.batch_execute'),
          handleClick: this.handleRunBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+RUN']
        },
      ],
      trashButtons: [
        {
          name: this.$t('commons.reduction'),
          handleClick: this.handleBatchRestore,
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE']
        },
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_CASE']
        },
      ],
      operators: [],
      simpleOperators: [
        {
          tip: this.$t('api_test.automation.execute'),
          icon: "el-icon-video-play",
          exec: this.runTestCase,
          class: "run-button",
          permissions: ['PROJECT_API_DEFINITION:READ+RUN']
        },
        {
          tip: this.$t('commons.edit'),
          icon: "el-icon-edit",
          exec: this.handleTestCase,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_CASE']
        },
        {
          tip: this.$t('commons.copy'),
          exec: this.handleCopy,
          icon: "el-icon-document-copy",
          type: "primary",
          permissions: ['PROJECT_API_DEFINITION:READ+COPY_CASE']
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.deleteToGc,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE']
        }
      ],
      trashOperators: [
        {tip: this.$t('commons.reduction'), icon: "el-icon-refresh-left", exec: this.reduction},
        {
          tip: this.$t('commons.delete'),
          exec: this.handleDelete,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE']
        },
      ],
      typeArr: [
        {id: 'priority', name: this.$t('test_track.case.priority')}
      ],
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      STATUS_FILTERS: [
        {text: i18n.t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: i18n.t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: i18n.t('test_track.plan.plan_status_completed'), value: 'Completed'}
      ],
      statusFilters: [
        {text: this.$t('api_test.automation.success'), value: 'success'},
        {text: this.$t('api_test.automation.fail'), value: 'error'},
        {text: this.$t('error_report_library.option.name'), value: 'errorReportResult'},
        {text: this.$t('report.stop_btn'), value: 'STOP'},
        {text: this.$t('api_test.home_page.detail_card.unexecute'), value: ''},
        {text: this.$t('commons.testing'), value: 'Running'}
      ],
      valueArr: {
        priority: CASE_PRIORITY,
        method: REQ_METHOD,
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 220px)',//屏幕高度
      environmentId: undefined,
      selectAll: false,
      unSelection: [],
      selectDataCounts: 0,
      environments: [],
      resVisible: false,
      response: {},
      timeoutIndex: 0,
      versionFilters: [],
      versionName: '',
      runCaseIds: [],
      versionEnable: false,
    };
  },
  props: {
    currentProtocol: String,
    currentVersion: String,
    apiDefinitionId: String,
    apiDefinition: Object,
    selectNodeIds: Array,
    activeDom: String,
    visible: {
      type: Boolean,
      default: false,
    },
    trashEnable: {
      type: Boolean,
      default: false,
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
    isCaseRelevance: {
      type: Boolean,
      default: false,
    },
    relevanceProjectId: String,
    model: {
      type: String,
      default() {
        'api';
      }
    },
    planId: String
  },
  created: function () {
    if (this.trashEnable) {
      this.operators = this.trashOperators;
      this.buttons = this.trashButtons;
    } else {
      this.operators = this.simpleOperators;
      this.buttons = this.simpleButtons;
    }
    // 切换tab之后版本查询
    this.condition.versionId = this.currentVersion;
    this.initTable();
    // 通知过来的数据跳转到编辑
    if (this.$route.query.caseId) {
      this.$get('/api/testcase/findById/' + this.$route.query.caseId, (response) => {
        this.handleTestCase(response.data);
      });
    }
    this.getVersionOptions();
    this.checkVersionEnable();
  },
  watch: {
    selectNodeIds() {
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;
      this.initTable();
    },
    currentProtocol() {
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;
      this.initTable();
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTable();
      // 选择了版本过滤，版本列上的checkbox也进行过滤
      this.getVersionOptions(this.currentVersion);
    },
    trashEnable() {
      if (this.trashEnable) {
        this.operators = this.trashOperators;
        this.buttons = this.trashButtons;
      } else {
        this.operators = this.simpleOperators;
        this.buttons = this.simpleButtons;
      }
      if (this.trashEnable) {
        this.selectAll = false;
        this.unSelection = [];
        this.selectDataCounts = 0;
        this.initTable();
      }
    },
    relevanceProjectId() {
      this.initTable();
    },
    '$store.state.currentApiCase.refresh'() {
      if (this.$store.state.currentApiCase.refresh) {
        this.setStatus(this.$store.state.currentApiCase.id, this.$store.state.currentApiCase.status);
      }
      this.$store.state.currentApiCase = {};
    }
  },
  computed: {
    // 接口定义用例列表
    isApiModel() {
      return this.model === 'api';
    },
    projectId() {
      return getCurrentProjectID();
    },
    selectRows() {
      return this.$refs.caseTable.getSelectRows();
    },
    editApiTestCaseOrder() {
      return editApiTestCaseOrder;
    }
  },
  methods: {
    openHis(row) {
      this.$refs.taskCenter.openHistory(row.id);
    },
    getExecResult(apiCase) {
      if (apiCase.lastResultId && apiCase.execResult) {
        let url = "/api/definition/report/get/" + apiCase.lastResultId;
        this.$get(url, response => {
          if (response.data) {
            try {
              let data = JSON.parse(response.data.content);
              this.response = data;
              this.resVisible = true;
            } catch (error) {
              this.resVisible = true;
            }
          } else {
            this.$warning(this.$t('commons.report_delete'));
          }
        });
      }
    },
    getStatusClass(status) {
      switch (status) {
        case "success":
          return "ms-success";
        case "error":
          return "ms-error";
        case "Running":
          return "ms-running";
        case "errorReportResult":
          return "ms-error-report-result";
        case "STOP":
          return "stop";
        default:
          return "ms-unexecute";
      }
    },
    getStatusTitle(status) {
      switch (status) {
        case "success":
          return this.$t('api_test.automation.success');
        case "error":
          return this.$t('api_test.automation.fail');
        case "Running":
          return this.$t('commons.testing');
        case "errorReportResult":
          return this.$t('error_report_library.option.name');
        case "STOP":
          return this.$t('report.stop_btn');
        default:
          return this.$t('api_test.home_page.detail_card.unexecute');
      }
    },
    handleRunBatch() {
      this.$refs.batchRun.open();
    },
    runBatch(config) {
      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = this.selectAll;
      obj.unSelectIds = this.unSelection;
      obj.ids = Array.from(this.selectRows).map(row => row.id);
      obj.config = config;
      obj.condition = this.condition;
      obj.condition.status = "";
      this.$post('/api/testcase/batch/run', obj, () => {
        this.condition.ids = [];
        this.$refs.batchRun.close();
        if (this.$store.state.currentApiCase) {
          this.$store.state.currentApiCase.case = true;
        } else {
          this.$store.state.currentApiCase = {case: true};
        }
        this.$refs.taskCenter.open();
      });
    },
    customHeader() {
      this.$refs.caseTable.openCustomHeader();
    },
    setStatus(id, status) {
      this.tableData.forEach(item => {
        if (id && id === item.id) {
          item.status = status;
          item.execResult = status;
        }
      });
    },
    initTable(id) {
      this.timeoutIndex = 0;
      if (this.$refs.caseTable) {
        this.$refs.caseTable.clearSelectRows();
      }
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
      if (this.condition.orders) {
        const index = this.condition.orders.findIndex(d => d.name && d.name === 'case_path');
        if (index !== -1) {
          this.condition.orders.splice(index, 1);
        }
      }
      this.enableOrderDrag = this.condition.orders.length > 0 ? false : true;
      if (this.trashEnable) {
        this.condition.moduleIds = [];
        if (this.condition.filters) {
          if (this.condition.filters.status) {
            this.condition.filters.status = ["Trash"];
          } else {
            this.condition.filters = {status: ["Trash"]};
          }
        } else {
          this.condition.filters = {};
          this.condition.filters = {status: ["Trash"]};
        }
      }
      this.initCondition();

      let isNext = false;
      if (this.condition.projectId) {
        this.result = this.$post('/api/testcase/list/' + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
            if (id && id === item.id) {
              item.status = "Running";
            }
            if (item.status === 'Running') {
              isNext = true;
            }
          });
          this.$nextTick(() => {
            if (this.$refs.caseTable) {
              this.$refs.caseTable.clear();
            }
            handleRowDrop(this.tableData, (param) => {
              param.groupId = this.condition.projectId;
              editApiTestCaseOrder(param);
            });
          })
          if (isNext) {
            this.refreshStatus();
          }
        });
      }
    },
    setRunning(id) {
      this.tableData.forEach(item => {
        if (id && id === item.id) {
          item.status = "Running";
        }
      });
    },
    initCondition() {
      if (this.apiDefinitionId) {
        this.condition.apiDefinitionId = this.apiDefinitionId;
      }
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;
      if (this.condition.filters && !this.condition.filters.status) {
        this.$delete(this.condition.filters, 'status');
      }
      if (!this.selectAll) {
        this.selectAll = false;
        this.unSelection = [];
        this.selectDataCounts = 0;
      }
      this.condition.projectId = this.projectId;
      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }
      //检查是否只查询本周数据
      this.isSelectThisWeekData();
      this.condition.selectThisWeedData = false;
      this.condition.id = null;
      if (this.selectDataRange == 'thisWeekCount') {
        this.condition.selectThisWeedData = true;
      } else if (this.selectDataRange != null) {
        let selectParamArr = this.selectDataRange.split(":");
        if (selectParamArr.length === 2) {
          if (selectParamArr[0] === "single") {
            this.condition.id = selectParamArr[1];
          } else {
            this.condition.apiDefinitionId = selectParamArr[1];
          }
        }
      }
    },
    refreshStatus(id) {
      this.initCondition();
      if (this.condition.projectId) {
        this.result = this.$post('/api/testcase/list/' + this.currentPage + "/" + this.pageSize, this.condition, response => {
          let isNext = false;
          let tableData = response.data.listObject;
          this.tableData.forEach(item => {
            for (let i in tableData) {
              if (item.id === tableData[i].id) {
                item.status = tableData[i].status;
                item.lastResultId = tableData[i].lastResultId;
              }
            }
            if (id && id === item.id) {
              item.status = "Running";
            }
            if (item.status === 'Running') {
              isNext = true;
            }
          });
          if (isNext && this.$store.state.currentApiCase && this.$store.state.currentApiCase.case && this.timeoutIndex < 12) {
            this.timeoutIndex++;
            setTimeout(() => {
              this.refreshStatus();
            }, 12000);
          }
        });
      }
    },
    open() {
      this.$refs.searchBar.open();
    },
    showExecResult(row) {
      this.visible = false;
      this.$emit('showExecResult', row);
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTable();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTable();
    },
    search() {
      this.changeSelectDataRangeAll();
      this.initTable();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    runTestCase(testCase) {
      this.$get('/api/definition/get/' + testCase.apiDefinitionId, (response) => {
        let api = response.data;
        let selectApi = api;
        let request = {};
        if (Object.prototype.toString.call(api.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          request = api.request;
        } else {
          request = JSON.parse(api.request);
        }
        if (!request.hashTree) {
          request.hashTree = [];
        }
        selectApi.url = request.path;
        this.$refs.caseList.runTestCase(selectApi, testCase.id);
        let obj = {id:testCase.id};
        this.$post('/api/testcase/updateExecuteInfo', obj);
      });
    },
    handleTestCase(testCase) {
      this.$get('/api/definition/get/' + testCase.apiDefinitionId, (response) => {
        let api = response.data;
        let selectApi = api;
        let request = {};
        if (Object.prototype.toString.call(api.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          request = api.request;
        } else {
          request = JSON.parse(api.request);
        }
        if (!request.hashTree) {
          request.hashTree = [];
        }
        selectApi.url = request.path;
        if (this.$refs.caseList) {
          this.$refs.caseList.open(selectApi, testCase.id);
        }
      });
    },
    addTestCase() {
      this.$get('/api/definition/get/' + this.apiDefinitionId, (response) => {
        let api = response.data;
        let selectApi = api;
        let request = {};
        if (Object.prototype.toString.call(api.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          request = api.request;
        } else {
          request = JSON.parse(api.request);
        }
        if (!request.hashTree) {
          request.hashTree = [];
        }
        selectApi.url = request.path;
        this.$refs.caseList.add(selectApi);
      });
    },
    handleCopy(row) {
      this.$get('/api/testcase/findById/' + row.id, (response) => {
        let data = response.data;
        let uuid = getUUID();
        let apiCaseRequest = JSON.parse(data.request);
        apiCaseRequest.id = uuid;
        let obj = {
          name: "copy_" + data.name,
          apiDefinitionId: row.apiDefinitionId,
          versionId: data.versionId,
          priority: data.priority,
          active: true,
          tags: data.tags,
          request: apiCaseRequest,
          url: apiCaseRequest.path,
          uuid: uuid
        };
        this.$refs.caseList.copy(obj);
      });
    },
    handleDeleteBatch() {
      this.$alert(this.$t('api_test.definition.request.delete_case_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let obj = {};
            obj.projectId = this.projectId;
            obj.selectAllDate = this.selectAll;
            obj.unSelectIds = this.unSelection;
            obj.ids = Array.from(this.selectRows).map(row => row.id);
            obj = Object.assign(obj, this.condition);
            this.$post('/api/testcase/deleteBatchByParam/', obj, () => {
              this.$refs.caseTable.clearSelectRows();
              this.$emit('refreshTable');
              this.$success(this.$t('commons.delete_success'));
            });
          }
        }
      });
    },
    handleDeleteToGcBatch() {
      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = this.selectAll;
      obj.unSelectIds = this.unSelection;
      obj = Object.assign(obj, this.condition);
      obj.ids = Array.from(this.selectRows).map(row => row.id);
      this.$post('/api/testcase/checkDeleteDatas/', obj, response => {
        let checkResult = response.data;
        let alertMsg = this.$t('api_test.definition.request.delete_case_confirm') + " ？";
        if (!checkResult.deleteFlag) {
          alertMsg = "";
          checkResult.checkMsg.forEach(item => {
            alertMsg += item + ";";
          });
          if (alertMsg === "") {
            alertMsg = this.$t('api_test.definition.request.delete_case_confirm') + " ？";
          } else {
            alertMsg += this.$t('api_test.is_continue') + " ？";
          }
        }

        this.$alert(alertMsg, '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {

              this.$post('/api/testcase/deleteToGcByParam/', obj, () => {
                this.$refs.caseTable.clearSelectRows();
                this.initTable();
                this.$success(this.$t('commons.delete_success'));
                this.$emit('refreshTable');
              });
            }
          }
        });
      });
    },
    handleEditBatch() {
      if (this.currentProtocol == 'HTTP') {
        this.valueArr.method = REQ_METHOD;
      } else if (this.currentProtocol == 'TCP') {
        this.valueArr.method = TCP_METHOD;
      } else if (this.currentProtocol == 'SQL') {
        this.valueArr.method = SQL_METHOD;
      } else if (this.currentProtocol == 'DUBBO') {
        this.valueArr.method = DUBBO_METHOD;
      }
      this.$refs.batchEdit.open();
    },
    batchEdit(form) {
      let arr = Array.from(this.selectRows);
      let ids = arr.map(row => row.id);
      let param = {};
      param[form.type] = form.value;
      param.ids = ids;
      param.projectId = this.projectId;
      param.selectAllDate = this.selectAll;
      param.unSelectIds = this.unSelection;
      param = Object.assign(param, this.condition);
      this.$post('/api/testcase/batch/editByParam', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.initTable();
      });
    },
    handleDelete(apiCase) {
      this.$alert(this.$t('api_test.definition.request.delete_case_confirm') + ' ' + apiCase.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$get('/api/testcase/delete/' + apiCase.id, () => {
              this.$success(this.$t('commons.delete_success'));
              this.$emit('refreshTable');
            });
          }
        }
      });
      return;
    },
    deleteToGc(apiCase) {
      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = false;
      obj.ids = [apiCase.id];
      obj = Object.assign(obj, this.condition);
      this.$post('/api/testcase/checkDeleteDatas/', obj, response => {
        let checkResult = response.data;
        let alertMsg = this.$t('api_test.definition.request.delete_case_confirm') + ' ' + apiCase.name + " ？";
        if (!checkResult.deleteFlag) {
          alertMsg = "";
          checkResult.checkMsg.forEach(item => {
            alertMsg += item + ";";
          });
          if (alertMsg === "") {
            alertMsg = this.$t('api_test.definition.request.delete_case_confirm') + ' ' + apiCase.name + " ？";
          } else {
            alertMsg += this.$t('api_test.is_continue') + " ？";
          }
        }

        this.$alert(alertMsg, '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this.$get('/api/testcase/deleteToGc/' + apiCase.id, () => {
                this.$success(this.$t('commons.delete_success'));
                this.initTable();
                this.$emit("refreshTree");
                this.$emit('refreshTable');
              });
            }
          }
        });
      });
    },
    reduction(row) {
      let tmp = JSON.parse(JSON.stringify(row));
      let rows = {ids: [tmp.id]};
      this.$post('/api/testcase/reduction/', rows, response => {
        let cannotReductionApiNameArr = response.data;
        if (cannotReductionApiNameArr.length > 0) {
          let apiNames = "";
          cannotReductionApiNameArr.forEach(item => {
            if (apiNames === "") {
              apiNames += item;
            } else {
              apiNames += ";" + item;
            }
          });
          this.$error(this.$t('api_test.definition.case_reduction_error_text') + "[" + apiNames + "]" + this.$t("api_test.home_page.api_details_card.title"));
        } else {
          this.$success(this.$t('commons.save_success'));
        }
        // this.search();
        this.$emit('refreshTable');
      });
    },
    handleBatchRestore() {
      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = this.selectAll;
      obj.unSelectIds = this.unSelection;
      obj.ids = Array.from(this.selectRows).map(row => row.id);
      obj = Object.assign(obj, this.condition);
      this.$post('/api/testcase/reduction/', obj, response => {
        let cannotReductionApiNameArr = response.data;
        if (cannotReductionApiNameArr.length > 0) {
          let apiNames = "";
          cannotReductionApiNameArr.forEach(item => {
            if (apiNames === "") {
              apiNames += item;
            } else {
              apiNames += ";" + item;
            }
          });
          this.$error(this.$t('api_test.definition.case_reduction_error_text') + "[" + apiNames + "]" + this.$t("api_test.home_page.api_details_card.title"));
        } else {
          this.$success(this.$t('commons.save_success'));
        }
        // this.search();
        this.$emit('refreshTable');
      });
    },
    setEnvironment(data) {
      this.environmentId = data.id;
    },
    selectRowsCount(selection) {
      let selectedIDs = this.getIds(selection);
      let allIDs = this.tableData.map(s => s.id);
      this.unSelection = allIDs.filter(function (val) {
        return selectedIDs.indexOf(val) === -1;
      });
      if (this.selectAll) {
        this.selectDataCounts = this.total - this.unSelection.length;
      } else {
        this.selectDataCounts = selection.size;
      }
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    isSelectThisWeekData() {
      this.selectDataRange = "all";
      let routeParam = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      if (dataType === 'apiTestCase') {
        this.selectDataRange = routeParam;
      }
    },
    changeSelectDataRangeAll() {
      this.$emit("changeSelectDataRangeAll", "testCase");
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets);
      let ids = rowArray.map(s => s.id);
      return ids;
    },
    showCaseRef(row) {
      let param = {};
      Object.assign(param, row);
      param.moduleId = undefined;
      this.$refs.viewRef.open(param);
    },
    showEnvironment(row) {
      if (this.projectId) {
        this.$get('/api/environment/list/' + this.projectId, response => {
          this.environments = response.data;
          this.environments.forEach(environment => {
            parseEnvironment(environment);
          });
        });
      } else {
        this.environment = undefined;
      }
      this.clickRow = row;
      this.$refs.setEnvironment.open(row);
    },
    headerDragend(newWidth, oldWidth, column, event) {
      let finalWidth = newWidth;
      if (column.minWidth > finalWidth) {
        finalWidth = column.minWidth;
      }
      column.width = finalWidth;
      column.realWidth = finalWidth;
    },
    stop(id) {
      for (let item of this.tableData) {
        if (id && id === item.id) {
          // 获取执行前结果
          this.$get('/api/testcase/get/' + id, res => {
            if (res) {
              item.status = res.data.status;
            }
          });
          break;
        }
      }
    },
    sortHashTree(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {
              type: "JSON",
              data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}
            };
          }
          if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
            stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sortHashTree(stepArray[i].hashTree);
          }
        }
      }
    },
    createPerformance(row, environment) {
      /**
       * 思路：调用后台创建性能测试的方法，把当前案例的hashTree在后台转化为jmx并文件创建性能测试。
       * 然后跳转到修改性能测试的页面
       *
       * 性能测试保存地址： performance/save
       *
       */
      if (!environment) {
        this.$warning(this.$t('api_test.environment.select_environment'));
        return;
      }
      let projectId = getCurrentProjectID();
      let runData = [];
      row.request = JSON.parse(row.request);
      row.request.name = row.id;
      row.request.useEnvironment = environment.id;
      let map = new Map;
      map.set(row.projectId, environment.id);
      row.environmentMap = map;
      runData.push(row.request);
      /*触发执行操作*/
      let testPlan = new TestPlan();
      testPlan.clazzName = TYPE_TO_C.get(testPlan.type);
      let threadGroup = new ThreadGroup();
      threadGroup.clazzName = TYPE_TO_C.get(threadGroup.type);
      threadGroup.hashTree = [];
      testPlan.hashTree = [threadGroup];
      runData.forEach(item => {
        item.projectId = projectId;
        if (!item.clazzName) {
          item.clazzName = TYPE_TO_C.get(item.type);
        }
        threadGroup.hashTree.push(item);
      });
      this.sortHashTree(testPlan.hashTree);
      let reqObj = {
        id: row.id,
        testElement: testPlan,
        clazzName: this.clazzName ? this.clazzName : TYPE_TO_C.get(this.type),
        name: row.name,
        projectId: this.projectId,
        environmentMap: new Map([
          [projectId, environment.id]
        ]),
      };
      let bodyFiles = getBodyUploadFiles(reqObj, runData);
      reqObj.reportId = "run";

      let url = "/api/genPerformanceTestXml";

      this.$fileUpload(url, null, bodyFiles, reqObj, response => {
        let jmxObj = {};
        jmxObj.name = response.data.name;
        jmxObj.xml = response.data.xml;
        jmxObj.attachFiles = response.data.attachFiles;
        jmxObj.attachByteFiles = response.data.attachByteFiles;
        jmxObj.caseId = reqObj.id;
        jmxObj.version = row.version;
        jmxObj.envId = environment.id;
        this.$store.commit('setTest', {
          name: row.name,
          jmx: jmxObj
        });
        this.$router.push({
          path: "/performance/test/create"
        });
      }, erro => {
        this.$emit('runRefresh', {});
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
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + this.projectId, response => {
          this.versionEnable = response.data;
          if (!response.data) {
            this.fields = this.fields.filter(f => f.id !== 'versionId');
          }
        });
      }
    }
  },
};
</script>

<style scoped>
.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}

.request-method {
  padding: 0 5px;
  color: #1E90FF;
}

.api-el-tag {
  color: white;
}

.search-input {
  float: right;
  width: 300px;
  /*margin-bottom: 20px;*/
  margin-right: 10px;
}

.ms-select-all >>> th:first-child {
  margin-top: 20px;
}

.ms-select-all >>> th:nth-child(2) .el-icon-arrow-down {
  top: -2px;
}

.ms-success {
  color: #67C23A;
}

.ms-error {
  color: #F56C6C;
}

.ms-error-report-result {
  color: #F6972A;
}

.ms-running {
  color: #6D317C;
}

.ms-unexecute {
}
</style>
