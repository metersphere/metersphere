<template>
  <span>
    <span>
      <div class="ms-opt-btn" v-if="apiDefinitionId && versionEnable">
        {{ $t('project.version.name') }}: {{ apiDefinition.versionName }}
      </div>
      <ms-search :condition.sync="condition" :base-search-tip="$t('commons.search_by_id_name_tag')" @search="search">
      </ms-search>
      <el-button
        type="primary"
        style="float: right; margin-right: 10px"
        icon="el-icon-plus"
        size="small"
        v-permission="['PROJECT_API_DEFINITION:READ+CREATE_CASE']"
        @click="addTestCase"
        v-if="apiDefinitionId"
        >{{ $t('commons.add') }}
      </el-button>
      <ms-table
        v-loading="result"
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
        class="api-case-simple-list">
        <ms-table-column
          prop="deleteTime"
          sortable
          v-if="this.trashEnable"
          :fields-width="fieldsWidth"
          :label="$t('commons.delete_time')"
          min-width="150px">
          <template v-slot:default="scope">
            <span>{{ scope.row.deleteTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="deleteUser"
          :fields-width="fieldsWidth"
          v-if="this.trashEnable"
          :label="$t('commons.delete_user')"
          min-width="120" />
        <span v-for="item in fields" :key="item.key">
          <ms-table-column prop="num" label="ID" :field="item" :fields-width="fieldsWidth" min-width="80px" sortable>
            <template slot-scope="scope" v-if="!trashEnable">
              <!-- 判断为只读用户的话不可点击ID进行编辑操作 -->
              <span style="cursor: pointer" v-if="isReadOnly">
                {{ scope.row.num }}
              </span>
              <el-tooltip v-else :content="$t('commons.edit')">
                <a style="cursor: pointer" @click="handleTestCase(scope.row)">
                  {{ scope.row.num }}
                </a>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="name"
            sortable="custom"
            min-width="160px"
            :label="$t('test_track.case.name')" />

          <ms-table-column
            prop="priority"
            :filters="priorityFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            sortable
            :label="$t('test_track.case.priority')">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.priority" />
            </template>
          </ms-table-column>

          <ms-table-column
            v-if="!trashEnable"
            prop="caseStatus"
            :filters="STATUS_FILTERS"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('commons.status')">
            <template v-slot:default="scope">
              <plan-status-table-item :value="scope.row.caseStatus" />
            </template>
          </ms-table-column>

          <ms-table-column
            v-if="trashEnable"
            prop="caseStatus"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('commons.status')">
            <template v-slot:default="scope">
              <plan-status-table-item :value="scope.row.status" />
            </template>
          </ms-table-column>

          <ms-table-column
            prop="execResult"
            :filters="statusFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('test_track.plan_view.execute_result')"
            v-if="!trashEnable">
            <template v-slot:default="scope">
              <el-link
                @click="getExecResult(scope.row)"
                :disabled="!scope.row.execResult || scope.row.execResult === 'PENDING'">
                <ms-api-report-status :status="scope.row.execResult" />
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
            :show-overflow-tooltip="false"
            :label="'API' + $t('api_test.definition.api_path')" />

          <ms-table-column
            v-if="item.id == 'tags'"
            prop="tags"
            width="120px"
            :label="$t('commons.tag')"
            :show-overflow-tooltip="false">
            <template v-slot:default="scope">
              <el-tooltip class="item" effect="dark" placement="top">
                <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
                <div class="oneLine">
                  <ms-tag
                    v-for="(itemName, index) in scope.row.tags"
                    :key="index"
                    type="success"
                    effect="plain"
                    :show-tooltip="scope.row.tags.length === 1 && itemName.length * 12 <= 100"
                    :content="itemName"
                    style="margin-left: 0px; margin-right: 2px" />
                </div>
              </el-tooltip>
              <span />
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
            :filters="environmentsFilters"
            :fields-width="fieldsWidth"
            min-width="100px"
            :label="$t('commons.environment')">
          </ms-table-column>

          <ms-table-column
            prop="createUser"
            :field="item"
            :fields-width="fieldsWidth"
            :filters="userFilters"
            :label="$t('commons.create_user')" />

          <ms-table-column
            sortable="updateTime"
            min-width="160px"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('api_test.definition.api_last_time')"
            prop="updateTime">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | datetimeFormat }}</span>
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
              <span>{{ scope.row.createTime | datetimeFormat }}</span>
            </template>
          </ms-table-column>
        </span>

        <template v-if="!trashEnable" v-slot:opt-behind="scope">
          <ms-api-case-table-extend-btns
            @showCaseRef="showCaseRef"
            @showEnvironment="showEnvironment"
            @createPerformance="createPerformance"
            @showHistory="openHis"
            :row="scope.row" />
        </template>
      </ms-table>

      <ms-table-pagination
        :change="initTable"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total" />
    </span>

    <api-case-list
      @showExecResult="showExecResult"
      @refreshCase="setRunning"
      :currentApi="selectCase"
      ref="caseList"
      @stop="stop"
      @reLoadCase="initTable" />
    <!--批量编辑-->
    <ms-batch-edit
      ref="batchEdit"
      :data-count="$refs.caseTable ? $refs.caseTable.selectDataCounts : 0"
      @batchEdit="batchEdit"
      :typeArr="typeArr"
      :value-arr="valueArr" />
    <!--选择环境(当创建性能测试的时候)-->
    <ms-set-environment ref="setEnvironment" :testCase="clickRow" @createPerformance="createPerformance" />
    <!--查看引用-->
    <ms-show-reference ref="viewRef" />

    <ms-task-center ref="taskCenter" :show-menu="false" />

    <ms-api-run-mode
      :is-scenario="false"
      :project-id="projectId"
      :run-case-ids="runCaseIds"
      @handleRunBatch="runBatch"
      @close="initTable"
      ref="apiBatchRun" />

    <el-dialog
      :close-on-click-modal="false"
      :title="$t('test_track.plan_view.test_result')"
      width="60%"
      :visible.sync="resVisible"
      class="api-import"
      destroy-on-close
      @close="resVisible = false">
      <ms-request-result-tail :response="response" ref="debugResult" />
    </el-dialog>

    <el-dialog :visible.sync="batchSyncCaseVisible" :title="$t('commons.batch') + $t('workstation.sync')">
      <span>{{ $t('workstation.sync') + $t('commons.setting') }}</span
      ><br />
      <sync-setting ref="synSetting"></sync-setting>
      <span style="color: red">{{ $t('workstation.batch_sync_api_tips') }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchSyncCaseVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="batchSync()">{{ $t('commons.confirm') }}</el-button>
      </span>
    </el-dialog>
    <!--  删除接口提示  -->
    <api-delete-confirm
      :has-ref="hasRef"
      :show-case="showCase"
      @showCaseRef="showCaseRef"
      @handleDeleteCase="handleDeleteCase"
      ref="apiDeleteConfirm" />
  </span>
</template>

<script>
import {
  apiTestCasePage,
  checkDeleteData,
  delApiTestCase,
  delCaseBatchByParam,
  delCaseToGcByParam,
  deleteToGc,
  editApiCaseByParam,
  editApiTestCaseOrder,
  getApiCaseById,
  getCaseById,
  testCaseBatchRun,
  testCaseReduction,
  updateExecuteInfo,
} from '@/api/api-test-case';
import { getDefinitionById } from '@/api/definition';
import { getApiReportDetail } from '@/api/definition-report';
import { genPerformanceTestXml } from '@/api/home';
import { getMaintainer } from '@/api/project';
import { getProjectVersions, synCaseBatch, versionEnableByProjectId } from '@/api/xpack';
import MsTable from 'metersphere-frontend/src/components/table/MsTable';
import MsTableColumn from 'metersphere-frontend/src/components/table/MsTableColumn';
import MsTableOperator from 'metersphere-frontend/src/components/MsTableOperator';
import MsTableOperatorButton from 'metersphere-frontend/src/components/MsTableOperatorButton';
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import MsTag from 'metersphere-frontend/src/components/MsTag';
import MsApiCaseList from '../case/EditApiCase';
import ApiCaseList from '../case/EditApiCase';
import MsContainer from 'metersphere-frontend/src/components/MsContainer';
import MsBottomContainer from '../BottomContainer';
import ShowMoreBtn from '@/business/commons/ShowMoreBtn';
import MsBatchEdit from '../basis/BatchEdit';
import { getUUID } from 'metersphere-frontend/src/utils';
import {
  API_METHOD_COLOUR,
  CASE_PRIORITY,
  DUBBO_METHOD,
  REQ_METHOD,
  SQL_METHOD,
  TCP_METHOD,
} from '../../model/JsonData';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import { hasLicense } from 'metersphere-frontend/src/utils/permission';
import { getBodyUploadFiles } from '@/business/definition/api-definition';
import PriorityTableItem from '@/business/commons/PriorityTableItem';
import MsApiCaseTableExtendBtns from '../reference/ApiCaseTableExtendBtns';
import MsShowReference from '../reference/ShowReference';
import MsSetEnvironment from '@/business/definition/components/basis/SetEnvironment';
import TestPlan from '@/business/definition/components/jmeter/components/test-plan';
import ThreadGroup from '@/business/definition/components/jmeter/components/thread-group';
import { parseEnvironment } from '@/business/environment/model/EnvironmentModel';
import MsTableHeaderSelectPopover from 'metersphere-frontend/src/components/table/MsTableHeaderSelectPopover';
import MsTableAdvSearchBar from 'metersphere-frontend/src/components/search/MsTableAdvSearchBar';
import { API_CASE_CONFIGS, API_CASE_CONFIGS_TRASH } from 'metersphere-frontend/src/components/search/search-components';
import {
  _filter,
  _sort,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  handleRowDrop,
} from 'metersphere-frontend/src/utils/tableUtils';
import { API_CASE_LIST } from 'metersphere-frontend/src/utils/constants';
import HeaderLabelOperate from 'metersphere-frontend/src/components/head/HeaderLabelOperate';
import ApiCaseBatchRun from '@/business/definition/components/list/ApiCaseBatchRun';
import MsRequestResultTail from '@/business/definition/components/response/RequestResultTail';
import { TYPE_TO_C } from '@/business/automation/scenario/Setting';
import i18n from 'metersphere-frontend/src/i18n';
import MsSearch from 'metersphere-frontend/src/components/search/MsSearch';
import SyncSetting from '@/business/definition/util/SyncSetting';
import { getEnvironmentByProjectId } from 'metersphere-frontend/src/api/environment';
import { useApiStore, usePerformanceStore } from '@/store';
import { REPORT_STATUS } from '@/business/commons/js/commons';
import MsApiRunMode from '@/business/automation/scenario/common/ApiRunMode';
import ApiDeleteConfirm from '@/business/definition/components/list/ApiDeleteConfirm';

const performanceStore = usePerformanceStore();

const store = useApiStore();
export default {
  name: 'ApiCaseSimpleList',
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
    MsTableAdvSearchBar,
    MsTable,
    MsTableColumn,
    MsRequestResultTail,
    MsApiRunMode,
    MsSearch,
    SyncSetting,
    MsShowReference,
    ApiDeleteConfirm,
    MsApiReportStatus: () => import('../../../automation/report/ApiReportStatus'),
    PlanStatusTableItem: () => import('@/business/commons/PlanStatusTableItem'),
    MsTaskCenter: () => import('metersphere-frontend/src/components/task/TaskCenter'),
  },
  data() {
    return {
      type: API_CASE_LIST,
      tableHeaderKey: 'API_CASE',
      fields: getCustomTableHeader('API_CASE', undefined),
      fieldsWidth: getCustomTableWidth('API_CASE'),
      condition: {
        components: this.trashEnable ? API_CASE_CONFIGS_TRASH : API_CASE_CONFIGS,
      },
      selectCase: {},
      result: false,
      moduleId: '',
      selectDataRange: 'all',
      clickRow: {},
      buttons: [],
      enableOrderDrag: true,
      simpleButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteToGcBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE'],
        },
        {
          name: this.$t('api_test.definition.request.batch_edit'),
          handleClick: this.handleEditBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_CASE'],
        },
        {
          name: this.$t('api_test.automation.batch_execute'),
          handleClick: this.handleRunBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+RUN'],
        },
        {
          name: this.$t('commons.batch') + this.$t('workstation.sync'),
          handleClick: this.openBatchSync,
          permissions: ['PROJECT_TRACK_PLAN:READ+SCHEDULE'],
          isXPack: true,
        },
      ],
      trashButtons: [
        {
          name: this.$t('commons.reduction'),
          handleClick: this.handleBatchRestore,
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE'],
        },
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_CASE'],
        },
      ],
      operators: [],
      simpleOperators: [
        {
          tip: this.$t('api_test.automation.execute'),
          icon: 'el-icon-video-play',
          exec: this.runTestCase,
          class: 'run-button',
          permissions: ['PROJECT_API_DEFINITION:READ+RUN'],
        },
        {
          tip: this.$t('commons.edit'),
          icon: 'el-icon-edit',
          exec: this.handleTestCase,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_CASE'],
        },
        {
          tip: this.$t('commons.copy'),
          exec: this.handleCopy,
          icon: 'el-icon-document-copy',
          type: 'primary',
          permissions: ['PROJECT_API_DEFINITION:READ+COPY_CASE'],
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.deleteToGc,
          icon: 'el-icon-delete',
          type: 'danger',
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE'],
        },
      ],
      trashOperators: [
        {
          tip: this.$t('commons.reduction'),
          icon: 'el-icon-refresh-left',
          exec: this.reduction,
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.handleDelete,
          icon: 'el-icon-delete',
          type: 'danger',
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE'],
        },
      ],
      typeArr: [
        { id: 'priority', name: this.$t('test_track.case.priority') },
        { id: 'tags', name: this.$t('commons.tag') },
      ],
      priorityFilters: [
        { text: 'P0', value: 'P0' },
        { text: 'P1', value: 'P1' },
        { text: 'P2', value: 'P2' },
        { text: 'P3', value: 'P3' },
      ],
      STATUS_FILTERS: [
        {
          text: i18n.t('test_track.plan.plan_status_prepare'),
          value: 'Prepare',
        },
        {
          text: i18n.t('test_track.plan.plan_status_running'),
          value: 'Underway',
        },
        {
          text: i18n.t('test_track.plan.plan_status_completed'),
          value: 'Completed',
        },
      ],
      statusFilters: REPORT_STATUS,
      valueArr: {
        priority: CASE_PRIORITY,
        method: REQ_METHOD,
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 180px)', //屏幕高度
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
      userFilters: [],
      environmentsFilters: [],
      batchSyncCaseVisible: false,
      hasRef: false,
      showCase: false,
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
      default: false,
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
      },
    },
    planId: String,
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
    this.getVersionOptions();
    this.checkVersionEnable();
    this.getMaintainerOptions();
    this.showEnvironment();
  },
  mounted() {
    // 通知过来的数据跳转到编辑
    if (this.$route.query.caseId) {
      getCaseById(this.$route.query.caseId).then((response) => {
        if (!response.data) {
          this.$error(this.$t('api_test.case_jump_message'));
          return;
        }
        this.handleTestCase(response.data);
      });
    }
  },
  watch: {
    selectNodeIds() {
      this.selectAll = false;
      this.unSelection = [];
      this.currentPage = 1;
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
    storeCurrentApiCaseRefresh() {
      if (store.currentApiCase && store.currentApiCase.refresh) {
        this.setStatus(store.currentApiCase.id, store.currentApiCase.status, store.currentApiCase.passRate, store.currentApiCase.reportId);
      }
      store.currentApiCase = {};
    },
  },
  computed: {
    storeCurrentApiCaseRefresh() {
      return store.currentApiCase ? store.currentApiCase.refresh : '';
    },
    // 接口定义用例列表
    isApiModel() {
      return this.model === 'api';
    },
    projectId() {
      return getCurrentProjectID();
    },
    selectRows() {
      return this.$refs.caseTable ? this.$refs.caseTable.getSelectRows() : '';
    },
    editApiTestCaseOrder() {
      return editApiTestCaseOrder;
    },
  },
  methods: {
    getMaintainerOptions() {
      getMaintainer().then((response) => {
        this.userFilters = response.data.map((u) => {
          return { text: u.name, value: u.id };
        });
      });
    },
    openHis(row) {
      this.$refs.taskCenter.openHistory(row.id);
    },
    getExecResult(apiCase) {
      if (apiCase.lastResultId && apiCase.execResult) {
        getApiReportDetail(apiCase.lastResultId).then((response) => {
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
    handleRunBatch() {
      this.runCaseIds = Array.from(this.selectRows).map((row) => row.id);
      this.$nextTick(() => {
        this.$refs.apiBatchRun.open();
      });
    },
    runBatch(config) {
      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = this.selectAll;
      obj.unSelectIds = this.unSelection;
      obj.ids = Array.from(this.selectRows).map((row) => row.id);
      obj.config = config;
      obj.condition = this.condition;
      obj.condition.status = '';
      testCaseBatchRun(obj).then(() => {
        this.condition.ids = [];
        this.$refs.apiBatchRun.close();
        if (store.currentApiCase) {
          store.currentApiCase.case = true;
        } else {
          store.currentApiCase = { case: true };
        }
        this.$refs.taskCenter.open('API');
      });
    },
    customHeader() {
      this.$refs.caseTable.openCustomHeader();
    },
    setStatus(id, status, passRate, reportId) {
      this.tableData.forEach((item) => {
        if (id && id === item.id) {
          item.status = status;
          item.execResult = status;
          item.passRate = passRate;
          item.lastResultId = reportId;
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
        const index = this.condition.orders.findIndex((d) => d.name && d.name === 'case_path');
        if (index !== -1) {
          this.condition.orders.splice(index, 1);
        }
      }
      this.enableOrderDrag = this.condition.orders.length > 0 ? false : true;
      if (this.trashEnable) {
        this.condition.moduleIds = [];
        if (this.condition.filters) {
          if (this.condition.filters.status) {
            this.condition.filters.status = ['Trash'];
          } else {
            this.condition.filters = { status: ['Trash'] };
          }
        } else {
          this.condition.filters = {};
          this.condition.filters = { status: ['Trash'] };
        }
      }
      this.initCondition();
      let isNext = false;
      if (this.condition.projectId) {
        this.result = apiTestCasePage(this.currentPage, this.pageSize, this.condition).then((response) => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.tableData.forEach((item) => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
            if (id && id === item.id) {
              item.status = 'Running';
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
          });
          if (isNext) {
            this.refreshStatus();
          }
        });
      }
      if (this.$refs.caseTable) {
        this.$refs.caseTable.clearSelection();
      }
    },
    setRunning(id) {
      this.tableData.forEach((item) => {
        if (id && id === item.id) {
          item.status = 'Running';
        }
      });
    },
    initCondition() {
      if (this.apiDefinitionId) {
        this.condition.apiDefinitionId = this.apiDefinitionId;
      }
      this.condition.status = '';
      this.condition.moduleIds = this.selectNodeIds;
      if (this.condition.filters && !this.condition.filters.status) {
        delete this.condition.filters['status'];
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
      //检查是否有跳转数据筛选
      this.isRedirectFilter();
      this.condition.selectThisWeedData = false;
      this.condition.id = null;
      this.condition.redirectFilter = null;
      if (this.selectDataRange == 'createdInWeek') {
        this.condition.selectThisWeedData = true;
      } else if (
        this.selectDataRange === 'executionPassCount' ||
        this.selectDataRange === 'unexecuteCount' ||
        this.selectDataRange === 'executionFailedCount' ||
        this.selectDataRange === 'fakeErrorCount' ||
        this.selectDataRange === 'executedCount'
      ) {
        this.condition.redirectFilter = this.selectDataRange;
      } else if (this.selectDataRange != null) {
        let selectParamArr = this.selectDataRange.split(':');
        if (selectParamArr.length === 2) {
          if (selectParamArr[0] === 'single') {
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
        this.result = apiTestCasePage(this.currentPage, this.pageSize, this.condition).then((response) => {
          let isNext = false;
          let tableData = response.data.listObject;
          this.tableData.forEach((item) => {
            for (let i in tableData) {
              if (item.id === tableData[i].id) {
                item.status = tableData[i].status;
                item.lastResultId = tableData[i].lastResultId;
              }
            }
            if (id && id === item.id) {
              item.status = 'Running';
            }
            if (item.status === 'Running') {
              isNext = true;
            }
          });
          if (isNext && store.currentApiCase && store.currentApiCase.case && this.timeoutIndex < 12) {
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
      return path + '/' + this.currentPage + '/' + this.pageSize;
    },
    runTestCase(testCase) {
      getDefinitionById(testCase.apiDefinitionId).then((response) => {
        let api = response.data;
        let selectApi = api;
        let request = {};
        if (
          Object.prototype.toString
            .call(api.request)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
          request = api.request;
        } else {
          request = JSON.parse(api.request);
        }
        if (!request.hashTree) {
          request.hashTree = [];
        }
        selectApi.url = request.path;
        this.$refs.caseList.runTestCase(selectApi, testCase.id);
        let obj = { id: testCase.id };
        updateExecuteInfo(obj);
      });
    },
    handleTestCase(testCase) {
      getDefinitionById(testCase.apiDefinitionId).then((response) => {
        let api = response.data;
        let selectApi = api;
        let request = {};
        if (
          Object.prototype.toString
            .call(api.request)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
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
      getDefinitionById(this.apiDefinitionId).then((response) => {
        let api = response.data;
        let selectApi = api;
        let request = {};
        if (
          Object.prototype.toString
            .call(api.request)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
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
      getDefinitionById(row.apiDefinitionId).then((response) => {
        let api = response.data;
        if (api) {
          this.getCaseAndOpen(row.id, api.name, row.apiDefinitionId, true);
        }
      });
    },
    resetResourceId(hashTree) {
      hashTree.forEach((item) => {
        item.resourceId = getUUID();
        if (item.hashTree && item.hashTree.length > 0) {
          this.resetResourceId(item.hashTree);
        }
      });
    },
    getCaseAndOpen(id, apiName, apiId, isCopyCase) {
      getCaseById(id).then((response) => {
        let data = response.data;
        let uuid = getUUID();
        let apiCaseRequest = JSON.parse(data.request);
        apiCaseRequest.id = uuid;
        if (apiCaseRequest.type === 'TCPSampler') {
          apiCaseRequest.method = 'TCP';
        } else if (apiCaseRequest.type === 'JDBCSampler') {
          apiCaseRequest.method = 'SQL';
        }
        if (apiCaseRequest.hashTree && apiCaseRequest.hashTree.length > 0) {
          this.resetResourceId(apiCaseRequest.hashTree);
        }
        apiCaseRequest.name = apiName;
        let obj = {
          name: 'copy_' + data.name,
          apiDefinitionId: apiId,
          versionId: data.versionId,
          priority: data.priority,
          active: true,
          tags: data.tags,
          request: apiCaseRequest,
          url: apiCaseRequest.path,
          uuid: uuid,
        };
        if (isCopyCase) {
          obj.sourceIdByCopy = id;
        }
        this.$refs.caseList.copy(obj);
      });
    },
    handleDeleteBatch() {
      this.$alert(this.$t('api_test.definition.request.delete_case_confirm') + '？', '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let obj = {};
            obj.projectId = this.projectId;
            obj.selectAllDate = this.selectAll;
            obj.unSelectIds = this.unSelection;
            obj.ids = Array.from(this.selectRows).map((row) => row.id);
            obj = Object.assign(obj, this.condition);
            delCaseBatchByParam(obj).then(() => {
              this.$refs.caseTable.clearSelectRows();
              this.$emit('refreshTable');
              this.$success(this.$t('commons.delete_success'));
            });
          }
        },
      });
    },
    handleDeleteToGcBatch() {
      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = this.selectAll;
      obj.unSelectIds = this.unSelection;
      obj = Object.assign(obj, this.condition);
      obj.ids = Array.from(this.selectRows).map((row) => row.id);
      obj.type = 'batch';
      this.showCase = false;
      this.hasRef = false;
      checkDeleteData(obj).then((response) => {
        let checkResult = response.data;
        let alertMsg = this.$t('api_test.definition.request.delete_case_confirm') + ' ？';
        if (checkResult.deleteFlag) {
          alertMsg =
            this.$t('api_definition.case_is_referenced', [checkResult.refCount]) +
            ', ' +
            this.$t('api_test.is_continue') +
            ' ？';
          this.showCase = true;
        }
        this.$refs.apiDeleteConfirm.open(
          alertMsg,
          this.$t('permission.project_api_definition.delete_case'),
          obj,
          checkResult.checkMsg
        );
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
      let ids = arr.map((row) => row.id);
      let param = {};
      Object.assign(param, this.condition);
      if (form.type === 'tags') {
        param.type = form.type;
        param.appendTag = form.appendTag;
        param.tagList = form.tags;
      } else {
        param[form.type] = form.value;
      }
      param.ids = ids;
      param.projectId = this.projectId;
      param.selectAllDate = this.selectAll;
      param.unSelectIds = this.unSelection;
      editApiCaseByParam(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.initTable();
      });
    },
    openBatchSync() {
      this.batchSyncCaseVisible = true;
    },
    batchSync() {
      let selectIds = this.$refs.caseTable.selectIds;
      let fromData = this.$refs.synSetting.fromData;
      fromData.ids = selectIds;
      this.condition.syncConfig = fromData;
      if (hasLicense()) {
        synCaseBatch(this.condition).then((response) => {
          this.batchSyncCaseVisible = false;
          this.$message.success(this.$t('commons.save_success'));
          this.initTable();
        });
      }
    },
    handleDelete(apiCase) {
      this.$alert(this.$t('api_test.definition.request.delete_case_confirm') + ' ' + apiCase.name + ' ？', '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            delApiTestCase(apiCase.id).then(() => {
              this.$success(this.$t('commons.delete_success'));
              this.$emit('refreshTable');
            });
          }
        },
      });
      return;
    },
    handleDeleteCase(apiCase) {
      this.$refs.apiDeleteConfirm.close();
      if (apiCase.type === 'batch') {
        delCaseToGcByParam(apiCase).then(() => {
          this.$refs.caseTable.clearSelectRows();
          this.initTable();
          this.$success(this.$t('commons.delete_success'));
          this.$emit('refreshTable');
        });
      } else {
        deleteToGc(apiCase.id).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.initTable();
          this.$emit('refreshTree');
          this.$emit('refreshTable');
        });
      }
    },
    deleteToGc(apiCase) {
      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = false;
      obj.ids = [apiCase.id];
      obj = Object.assign(obj, this.condition);
      this.showCase = false;
      this.hasRef = false;
      checkDeleteData(obj).then((response) => {
        let checkResult = response.data;
        let alertMsg = this.$t('api_test.definition.request.delete_case_confirm') + '[' + apiCase.name + ']' + '?';
        if (checkResult.deleteFlag) {
          alertMsg =
            '[' +
            apiCase.name +
            '] ' +
            this.$t('api_definition.case_is') +
            (checkResult.scenarioCount > 0
              ? this.$t('api_definition.scenario_count', [checkResult.scenarioCount])
              : '') +
            (checkResult.planCount > 0 && checkResult.scenarioCount > 0 ? '、 ' : '') +
            (checkResult.planCount > 0 ? this.$t('api_definition.plan_count', [checkResult.planCount]) : '') +
            this.$t('api_test.scenario.reference') +
            ', ' +
            this.$t('api_test.is_continue') +
            ' ？';
          this.hasRef = true;
        }
        this.$refs.apiDeleteConfirm.open(
          alertMsg,
          this.$t('permission.project_api_definition.delete_case'),
          apiCase,
          null
        );
      });
    },
    reduction(row) {
      let tmp = JSON.parse(JSON.stringify(row));
      let rows = { ids: [tmp.id] };
      testCaseReduction(rows).then((response) => {
        let cannotReductionApiNameArr = response.data;
        if (cannotReductionApiNameArr.length > 0) {
          let apiNames = '';
          cannotReductionApiNameArr.forEach((item) => {
            if (apiNames === '') {
              apiNames += item;
            } else {
              apiNames += ';' + item;
            }
          });
          this.$error(
            this.$t('api_test.definition.case_reduction_error_text') +
              '[' +
              apiNames +
              ']' +
              this.$t('api_test.home_page.api_details_card.title')
          );
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
      obj.ids = Array.from(this.selectRows).map((row) => row.id);
      obj = Object.assign(obj, this.condition);
      testCaseReduction(obj).then((response) => {
        let cannotReductionApiNameArr = response.data;
        if (cannotReductionApiNameArr.length > 0) {
          let apiNames = '';
          cannotReductionApiNameArr.forEach((item) => {
            if (apiNames === '') {
              apiNames += item;
            } else {
              apiNames += ';' + item;
            }
          });
          this.$error(
            this.$t('api_test.definition.case_reduction_error_text') +
              '[' +
              apiNames +
              ']' +
              this.$t('api_test.home_page.api_details_card.title')
          );
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
      let allIDs = this.tableData.map((s) => s.id);
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
    isRedirectFilter() {
      this.selectDataRange = 'all';
      let routeParam = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      let redirectVersionId = this.$route.params.versionId;
      if (redirectVersionId && redirectVersionId !== 'default') {
        this.condition.versionId = redirectVersionId;
      }
      if (dataType === 'apiTestCase') {
        this.selectDataRange = routeParam;
      }
      if (this.$route.query && this.$route.params.dataSelectRange === 'ref') {
        if (this.$route.query.ids) {
          if (typeof this.$route.query.ids === 'string') {
            this.condition.ids = [this.$route.query.ids];
          } else {
            this.condition.ids = this.$route.query.ids;
          }
        }
      }
    },
    changeSelectDataRangeAll() {
      this.$emit('changeSelectDataRangeAll', 'testCase');
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets);
      let ids = rowArray.map((s) => s.id);
      return ids;
    },
    showCaseRef(row) {
      let param = {};
      Object.assign(param, row);
      param.moduleId = undefined;
      this.$refs.viewRef.open(param, 'API');
    },
    showEnvironment(row) {
      if (this.projectId) {
        getEnvironmentByProjectId(this.projectId).then((response) => {
          this.environments = response.data;
          this.environments.forEach((environment) => {
            parseEnvironment(environment);
          });
          this.environmentsFilters = response.data.map((u) => {
            return { text: u.name, value: u.id };
          });
        });
      } else {
        this.environment = undefined;
      }
      if (row) {
        this.clickRow = row;
        this.$refs.setEnvironment.open(row);
      }
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
          getApiCaseById(id).then((res) => {
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
          if (stepArray[i].type === 'Assertions' && !stepArray[i].document) {
            stepArray[i].document = {
              type: 'JSON',
              data: {
                xmlFollowAPI: false,
                jsonFollowAPI: false,
                json: [],
                xml: [],
              },
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
      let map = new Map();
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
      runData.forEach((item) => {
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
        environmentMap: new Map([[projectId, environment.id]]),
      };
      let bodyFiles = getBodyUploadFiles(reqObj, runData);
      reqObj.reportId = 'run';
      genPerformanceTestXml(null, bodyFiles, reqObj)
        .then((response) => {
          let jmxInfo = response.data.data.jmxInfoDTO;
          if (jmxInfo) {
            let projectEnvMap = response.data.projectEnvMap;
            let jmxObj = {};
            jmxObj.name = jmxInfo.name;
            jmxObj.xml = jmxInfo.xml;
            jmxObj.attachFiles = jmxInfo.attachFiles;
            jmxObj.attachByteFiles = jmxInfo.attachByteFiles;
            jmxObj.caseId = reqObj.id;
            jmxObj.scenarioId = null;
            jmxObj.version = row.version;
            jmxObj.envId = environment.id;
            jmxObj.projectEnvMap = projectEnvMap;
            performanceStore.$patch({ test: { name: row.name, jmx: jmxObj }, scenarioJmxs: null });
            this.$router.push({
              path: '/performance/test/create',
            });
          }
        })
        .catch(() => {
          this.$emit('runRefresh', {});
        });
    },
    getVersionOptions(currentVersion) {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then((response) => {
          if (currentVersion) {
            this.versionFilters = response.data
              .filter((u) => u.id === currentVersion)
              .map((u) => {
                return { text: u.name, value: u.id };
              });
          } else {
            this.versionFilters = response.data.map((u) => {
              return { text: u.name, value: u.id };
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
        versionEnableByProjectId(this.projectId).then((response) => {
          this.versionEnable = response.data;
          if (!response.data) {
            this.fields = this.fields.filter((f) => f.id !== 'versionId');
          }
        });
      }
    },
    getTagToolTips(tags) {
      try {
        let showTips = '';
        tags.forEach((item) => {
          showTips += item + ',';
        });
        return showTips.substr(0, showTips.length - 1);
      } catch (e) {
        return '';
      }
    },
  },
};
</script>

<style scoped>
.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}

.ms-select-all :deep(th:first-child) {
  margin-top: 20px;
}

.ms-select-all :deep(th:nth-child(2) .el-icon-arrow-down) {
  top: -2px;
}

.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.api-case-simple-list :deep(.el-table) {
  height: calc(100vh - 185px) !important;
}

.api-case-simple-list :deep(.el-loading-mask) {
  z-index: 999;
}
</style>
