<template>
  <div>
    <div>
      <div class="changeTap" v-if="showTap">
        <span :class="isFinish ? showTextColor: unShowTextColor"
              @click="changeTabState('finish')">{{ $t('commons.to_be_completed') }}</span><span>｜</span><span
        @click="changeTabState('update')"
        :class="isFinish ? unShowTextColor: showTextColor">{{ $t('commons.pending_upgrade') }}</span>
      </div>
      <ms-table
        :table-is-loading="this.result"
        :data="tableData"
        :select-node-ids="selectNodeIds"
        :condition="condition"
        :page-size="pageSize"
        :total="total"
        :screen-height="screenHeight"
        :fields.sync="fields"
        :field-key="tableHeaderKey"
        :remember-order="true"
        :row-order-group-id="condition.projectId"
        :row-order-func="editApiTestCaseOrder"
        :batch-operators="batchButtons"
        :enable-selection="((isUpcoming===true||isFocus===true||isCreation===true) && isShowAllColumn === true)"
        :operators="(isFinish===false && isShowAllColumn===true && showColum===true)?simpleOperators:[]"
        :operator-width="(isFinish===false && showColum===true)?'180':'0'"
        :disable-header-config=true
        row-key="id"
        @refresh="initTable"
        ref="caseTable"
      >
        <span v-for="(item,index) in fields" :key="index">

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
            sortable
            min-width="160px"
            :label="$t('test_track.case.name')"/>

          <ms-table-column
            prop="priority"
            :filters="priorityFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('test_track.case.priority')">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.priority"/>
            </template>
          </ms-table-column>

          <ms-table-column
            :label="$t('project.version.name')"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="100px"
            prop="versionId">
          <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </ms-table-column>

          <ms-table-column
            sortable="custom"
            prop="path"
            min-width="180px"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('api_test.definition.api_path')"/>

          <ms-table-column
            prop="caseStatus"
            :field="item"
            :filters="STATUS_FILTERS"
            :fields-width="fieldsWidth"
            :label="$t('commons.status')">
            <template v-slot:default="scope">
              <span @click.stop="clickt = 'stop'" v-if="isShowAllColumn">
              <el-dropdown class="test-case-status" @command="statusChange">
                <span class="el-dropdown-link">
                  <plan-status-table-item :value="scope.row.caseStatus"/>
                </span>
                <el-dropdown-menu slot="dropdown" chang>
                  <el-dropdown-item :disabled="!hasEditPermission" :command="{item: scope.row, caseStatus: 'Prepare'}">
                    {{ $t('test_track.plan.plan_status_prepare') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!hasEditPermission"
                                    :command="{item: scope.row, caseStatus: 'Underway'}">
                    {{ $t('test_track.plan.plan_status_running') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!hasEditPermission"
                                    :command="{item: scope.row, caseStatus: 'Completed'}">
                    {{ $t('test_track.plan.plan_status_completed') }}
                  </el-dropdown-item>

                </el-dropdown-menu>
              </el-dropdown>
            </span>
              <plan-status-table-item :value="scope.row.caseStatus" v-else/>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="createUser"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.create_user')"/>

          <ms-table-column
            prop="execResult"
            v-if="!isFinish"
            :filters="statusFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('test_track.plan_view.execute_result')">
             <template v-slot:default="scope">
              <el-link :disabled="!scope.row.execResult || scope.row.execResult==='PENDING'">
                <ms-api-report-status :status="scope.row.execResult"/>
              </el-link>
            </template>
          </ms-table-column>

          <ms-table-column
            v-if="isFinish"
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

          <ms-table-column
            prop="passRate"
            v-if="isShowAllColumn"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="100px"
            :label="$t('commons.pass_rate')">
          </ms-table-column>

          <ms-table-column
            v-if="!isFinish"
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
            prop="projectName"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('report.project_name')"/>

          <ms-table-column v-if="item.id==='tags'&&isShowAllColumn" prop="tags" width="120px"
                           :label="$t('commons.tag')">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                      :show-tooltip="scope.row.tags.length===1&&itemName.length*12<=120"
                      :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
              <span/>
            </template>
          </ms-table-column>
        </span>


      </ms-table>

      <ms-table-pagination
        :change="initTable"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"/>
    </div>

    <ms-batch-edit ref="batchEdit" :data-count="$refs.caseTable ? $refs.caseTable.selectDataCounts : 0"
                   @batchEdit="batchEdit" :typeArr="(isFocus&&isShowAllColumn) ? focusArr:typeArr"
                   :value-arr="(isFocus&&isShowAllColumn)?focusValueArr:valueArr"/>

    <api-case-run-mode-with-env
      :project-id="projectId"
      @handleRunBatch="runBatch"
      @close="initTable"
      ref="batchRun"/>
    <ms-task-center ref="taskCenter" :show-menu="false"/>

    <el-dialog :visible.sync="syncCaseVisible" :title="$t('workstation.sync')+$t('commons.setting')">
      <sync-settings ref="synSetting"></sync-settings>
      <span slot="footer" class="dialog-footer">
        <el-button @click="syncCaseVisible = false">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="syncCase()">{{$t('commons.confirm')}}</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="ignoreCaseVisible">
      <span>{{
          $t('commons.confirm') + $t('workstation.ignore') + $t('commons.update') + $t('workstation.table_name.api_case') + currentCaseName + "?"
        }}</span>
      <br/>
      <span style="color: red">{{ $t('workstation.sync_case_tips') }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="ignoreCaseVisible = false">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="ignoreCase()">{{$t('commons.confirm')}}</el-button>
      </span>
    </el-dialog>


    <el-dialog :visible.sync="batchSyncCaseVisible" :title="$t('commons.batch')+$t('workstation.sync')">
      <span>{{ $t('workstation.sync') + $t('commons.setting') }}</span><br/>
      <sync-settings ref="synSetting"></sync-settings>
      <span style="color: red">{{ $t('workstation.batch_sync_api_tips') }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchSyncCaseVisible = false">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="batchSync()">{{$t('commons.confirm')}}</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="batchIgnoreCaseVisible" :title="$t('commons.batch')+$t('workstation.ignore')">
      <span>{{
          $t('commons.confirm') + $t('commons.batch') + $t('workstation.ignore') + $t('commons.update') + $t('workstation.table_name.api_definition') + '?'
        }}</span><br/>
      <span style="color: red">{{ $t('workstation.batch_ignore_case_tips') }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchIgnoreCaseVisible = false">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="batchIgnore()">{{$t('commons.confirm')}}</el-button>
      </span>
    </el-dialog>
  </div>

</template>

<script>

import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsBottomContainer from "@/components/BottomContainer";
import ShowMoreBtn from "metersphere-frontend/src/components/table/ShowMoreBtn";
import MsBatchEdit from "@/business/module/api/BatchEdit";
import {
  API_METHOD_COLOUR,
  API_STATUS,
  CASE_PRIORITY,
  DUBBO_METHOD,
  REQ_METHOD,
  SQL_METHOD,
  TCP_METHOD
} from "@/business/component/js/JsonData";

import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasLicense, hasPermission} from "metersphere-frontend/src/utils/permission";
import PriorityTableItem from "@/business/module/track/PriorityTableItem";
import MsApiCaseTableExtendBtns from "@/business/module/api/ApiCaseTableExtendBtns";
import MsReferenceView from "@/business/module/api/ReferenceView";
import {
  apiTestCasePage,
  batchIgnoreCase,
  batchSyncCase,
  editApiCaseByParam,
  editApiTestCaseOrder,
  getApiCaseById,
  getCaseById,
  testCaseBatchRun
} from "@/api/api";
import {getCustomTableHeaderByXpack, getCustomTableWidth} from "@/business/component/js/table-head-util";
import SyncSettings from "@/business/component/SyncSettings";
import {getProjectVersions} from "metersphere-frontend/src/api/version";
import PlanStatusTableItem from "@/business/module/plan/PlanStatusTableItem";
import ApiCaseRunModeWithEnv from "@/business/module/api/ApiCaseRunModeWithEnv";
import {API_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {getOwnerProjectIds, getProject, getProjectApplication} from "@/api/project";
import useApiStore from "@/store";
import {getUrl} from "@/business/component/js/urlhelper";
import MsApiReportStatus from "@/business/module/api/ApiReportStatus";
import {REPORT_STATUS} from "@/business/component/js/commons";

export default {
  name: "ApiCaseTableList",
  components: {
    PriorityTableItem,
    MsTableOperatorButton,
    MsTableOperator,
    MsTablePagination,
    MsTag,
    MsContainer,
    MsBottomContainer,
    ShowMoreBtn,
    MsBatchEdit,
    MsApiCaseTableExtendBtns,
    MsReferenceView,
    MsTable,
    MsTableColumn,
    SyncSettings,
    ApiCaseRunModeWithEnv,
    PlanStatusTableItem,
    MsApiReportStatus,
    MsTaskCenter: () => import("metersphere-frontend/src/components/task/TaskCenter"),
  },
  data() {
    return {
      showTextColor: "showTextColor",
      unShowTextColor: "unShowTextColor",
      isFinish: true,
      tableHeaderKey: "API_CASE",
      fields: getCustomTableHeaderByXpack('API_CASE_HEAD'),
      fieldsWidth: getCustomTableWidth('API_CASE'),
      condition: {
        components: API_CASE_CONFIGS
      },
      selectCase: {},
      result: false,
      moduleId: "",
      selectDataRange: "all",
      clickRow: {},
      buttons: [],
      batchButtons: [],
      syncButtons: [
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
        {
          name: this.$t('commons.batch') + this.$t('workstation.sync'),
          handleClick: this.openBatchSync,
          isXPack: true,
          permissions: ['PROJECT_TRACK_PLAN:READ+SCHEDULE']
        },
        {
          name: this.$t('commons.batch') + this.$t('workstation.ignore'),
          handleClick: this.openBatchIgnore,
          isXPack: true,
          permissions: ['PROJECT_TRACK_PLAN:READ+SCHEDULE']
        },
      ],
      commonButtons: [
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
      focusButtons: [
        {
          name: this.$t('api_test.definition.request.batch_edit'),
          handleClick: this.handleEditBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_CASE']
        },
      ],
      operators: [],
      versionFilters: [],
      simpleOperators: [
        {
          tip: this.$t('api_test.automation.execute'),
          icon: "el-icon-video-play",
          exec: this.handleTestCase,
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
          tip: this.$t('workstation.sync'),
          icon: "el-icon-refresh",
          exec: this.openSyncCase,
          isDisable: this.systemDisable,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        },
        {
          tip: this.$t('workstation.ignore'),
          icon: "el-icon-close",
          exec: this.openIgnoreCase,
          isDisable: this.systemDisable,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        },
      ],
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      statusFilters: REPORT_STATUS,
      STATUS_FILTERS: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'}
      ],
      typeArr: [
        {id: 'caseStatus', name: this.$t('api_test.definition.api_case_status')},
        {id: 'priority', name: this.$t('test_track.case.priority')},
        {id: 'tags', name: this.$t('commons.tag')},
      ],
      focusArr: [
        {id: 'follow', name: this.$t('commons.follow')},
      ],
      valueArr: {
        priority: CASE_PRIORITY,
        method: REQ_METHOD,
        caseStatus: API_STATUS,
      },
      focusValueArr: {
        follow: [
          {id: 'cancel', label: this.$t('commons.cancel') + this.$t('commons.follow')}
        ]
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      environmentId: undefined,
      selectAll: false,
      unSelection: [],
      selectDataCounts: 0,
      environments: [],
      resVisible: false,
      response: {},
      timeoutIndex: 0,
      currentCaseId: '',
      currentCaseName: '',
      syncCaseVisible: false,
      ignoreCaseVisible: false,
      batchSyncCaseVisible: false,
      batchIgnoreCaseVisible: false,
      hasEditPermission: false,
      store: {},
      openUpdateRule: true,
      showColum: false
    };
  },
  props: {
    currentProtocol: String,
    apiDefinitionId: String,
    selectNodeIds: Array,
    activeDom: String,
    currentVersion: String,
    visible: {
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
    isFocus: {
      type: Boolean,
      default: false,
    },
    isCreation: {
      type: Boolean,
      default: false,
    },
    isUpcoming: {
      type: Boolean,
      default: false,
    },
    isShowAllColumn: {
      type: Boolean,
      default: true,
    },
    isSelectAll: {
      type: Boolean,
      default: false,
    },
    showTap: {
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
    planId: String,
    screenHeight: {
      type: [Number, String],
      default() {
        return 'calc(100vh - 218px)';
      }
    }, //屏幕高度
  },
  created: function () {
    this.store = useApiStore();
    if (hasLicense()) {
      this.showColum = true;
    }
    // 切换tab之后版本查询
    this.condition.versionId = this.currentVersion;
    this.initTable();
    this.getProjectApplication();
    // 通知过来的数据跳转到编辑
    if (this.$route.query.caseId) {
      getCaseById(this.$route.query.caseId).then((response) => {
        this.handleTestCase(response.data);
      });
    }
    this.getVersionOptions();
    if (this.isFinish === true) {
      if (this.isFocus) {
        this.batchButtons = this.focusButtons
      } else {
        this.batchButtons = this.commonButtons
      }
    } else {
      this.batchButtons = this.syncButtons
    }
    this.hasEditPermission = hasPermission('PROJECT_TRACK_PLAN:READ+EDIT');
  },
  watch: {
    selectNodeIds() {
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;
      this.initTable();
    },
    currentProtocol() {
      this.condition.protocol = this.currentProtocol;
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;
      this.initTable();
    },
    relevanceProjectId() {
      this.initTable();
    },
    'store.currentApiCase.refresh'() {
      if (this.store.currentApiCase.refresh) {
        this.initTable();
      }
      this.store.currentApiCase = {};
    },
    isFinish() {
      this.condition.toBeUpdated = !this.isFinish;
      if (this.isFinish) {
        if (this.isFocus) {
          this.batchButtons = this.focusButtons;
        } else {
          this.batchButtons = this.commonButtons;
        }
        this.condition.filters = {case_status: ["Prepare", "Underway"]};
        this.condition.combine = {creator: {operator: "current user", value: "current user",}}
        if (this.condition.toUpdate) {
          delete this.condition.toUpdate
        }
      } else {
        if (this.condition.filters.case_status) {
          delete this.condition.filters.case_status
        }
        this.condition.combine = {creator: {operator: "current user", value: "current user",}}
        this.condition.toUpdate = true;
        this.batchButtons = this.syncButtons
      }

      this.initTable();
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTable();
      // 选择了版本过滤，版本列上的checkbox也进行过滤
      this.getVersionOptions(this.currentVersion);
    },
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
    getStatusClass(status) {
      switch (status) {
        case "success":
          return "ms-success";
        case "error":
          return "ms-error";
        case "Running":
          return "ms-running";
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
        default:
          return this.$t('api_test.home_page.detail_card.unexecute');
      }
    },
    initTable(id) {
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.timeoutIndex = 0;
      if (this.$refs.caseTable) {
        this.$refs.caseTable.clearSelectRows();
      }
      if (!this.selectAll) {
        this.selectAll = false;
        this.unSelection = [];
        this.selectDataCounts = 0;
      }

      if (this.apiDefinitionId) {
        this.condition.apiDefinitionId = this.apiDefinitionId;
      }
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;

      if (this.condition.filters && !this.condition.filters.status) {
        delete this.condition.filters.status
      }
      if (this.isSelectAll === false) {
        this.condition.projectId = this.projectId;
      }
      if (this.isFocus) {
        if (this.condition.toUpdate) {
          delete this.condition.toUpdate
        }
        if (this.condition.filters) {
          delete this.condition.filters['user_id']
        }
        this.condition.combine = {followPeople: {operator: "current user", value: "current user",}}
      } else if (this.isCreation) {
        if (this.condition.toUpdate) {
          delete this.condition.toUpdate
        }
        if (this.condition.filters) {
          delete this.condition.filters['user_id']
        }
        this.condition.combine = {creator: {operator: "current user", value: "current user",}}
      } else {
        if (this.isFinish) {
          if (!this.condition.filters) {
            this.condition.filters = {case_status: ["Prepare", "Underway"]};
          }
          if (!this.condition.filters.case_status) {
            this.condition.filters.case_status = ["Prepare", "Underway"];
          }
          if (this.condition.filters.case_status && this.condition.filters.case_status.length > 0) {
            for (let i = 0; i < this.condition.filters.case_status.length; i++) {
              if (this.condition.filters.case_status[i] === "Completed") {
                this.condition.filters.case_status[i] = "NO"
              }
            }
          }
          this.condition.combine = {creator: {operator: "current user", value: "current user",}}
          if (this.condition.toUpdate) {
            delete this.condition.toUpdate
          }
        } else {
          if (this.openUpdateRule === false) {
            this.tableData = [];
            this.total = 0;
            this.$message.warning(this.$t('workstation.apply_tip'))
            return;
          }
          this.condition.combine = {creator: {operator: "current user", value: "current user",}}
          this.condition.filters.status = [];
          this.condition.toUpdate = true;
          if (this.condition.filters.case_status === null || !this.condition.filters.case_status) {
            this.condition.filters.case_status = ["Prepare", "Underway", "Completed"]
          }
        }
      }
      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }

      let isNext = false;
      this.result = apiTestCasePage(this.currentPage,this.pageSize,this.condition).then(response => {
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
        if (!this.selectAll) {
          this.unSelection = response.data.listObject.map(s => s.id);
        }
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
        })
      });
    },

    handleTestCase(testCase) {
      //跳转到case编辑页
      this.clickResource(testCase);
    },
    clickResource(resource) {
      let workspaceId = getCurrentWorkspaceId();
      let isTurnSpace = true
      if (resource.projectId !== getCurrentProjectID()) {
        isTurnSpace = false;
        getProject(resource.projectId).then(response => {
          if (response.data) {
            workspaceId = response.data.workspaceId;
            isTurnSpace = true;
            this.checkPermission(resource, workspaceId, isTurnSpace);
          }
        });
      } else {
        this.checkPermission(resource, workspaceId, isTurnSpace);
      }
    },
    checkPermission(resource, workspaceId, isTurnSpace) {
      getOwnerProjectIds().then(res => {
        const project = res.data.find(p => p === resource.projectId);
        if (!project) {
          this.$warning(this.$t('commons.no_permission'));
        } else {
          this.gotoTurn(resource, workspaceId, isTurnSpace)
        }

      })
    },
    gotoTurn(resource, workspaceId, isTurnSpace) {
      resource.refType = 'CASE'
      getCaseById(resource.id).then(response => {
        if (response.data) {
          response.data.sourceId = resource.resourceId;
          response.data.type = this.setResourceType(response.data.request);
          response.data.refType = resource.refType;
          response.data.workspaceId = workspaceId;
          if (isTurnSpace) {
            this.clickCase(response.data)
          }
        } else {
          this.$error(this.$t('api_test.case_jump_message'))
        }
      });
    },
    setResourceType(request){
      let type="";
      if (typeof (request) === 'string') {
        let parse = JSON.parse(request);
        type = parse.type;
      } else {
        type = request.type;
      }
      return type;

    },
    clickCase(resource) {
      let uri = getUrl(resource);
      let resourceId = resource.id;
      if (resourceId && resourceId.startsWith("\"" || resourceId.startsWith("["))) {
        resourceId = JSON.parse(resource.sourceId);
      }
      if (resourceId instanceof Array) {
        resourceId = resourceId[0];
      }
      this.toPage(uri);
    },
    toPage(uri) {
      let id = "new_a";
      let a = document.createElement("a");
      a.setAttribute("href", uri);
      a.setAttribute("target", "_blank");
      a.setAttribute("id", id);
      document.body.appendChild(a);
      a.click();

      let element = document.getElementById(id);
      element.parentNode.removeChild(element);
    },

    stop(id) {
      for (let item of this.tableData) {
        if (id && id === item.id) {
          // 获取执行前结果
          getApiCaseById(id).then(res => {
            if (res) {
              item.status = res.data.status;
            }
          });
          break;
        }
      }
    },

    changeTabState(name) {
      this.$refs.caseTable.clearFilter();
      if (name === 'update') {
        this.isFinish = false;
      } else {
        this.isFinish = true;
      }
    },
    getProjectApplication(){
      getProjectApplication(this.projectId).then(res=>{
        if (res.data) {
          this.openUpdateRule = res.data.openUpdateRule !== false;
        }
      })
    },
    getVersionOptions(currentVersion) {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then(response => {
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
    statusChange(dataSource) {
      let data = dataSource.item;
      data.caseStatus = dataSource.caseStatus;
      let newStatus = dataSource.caseStatus;
      let ids = [];
      ids.push(data.id);
      let param = {};
      param.ids = ids;
      param.projectId = this.projectId;
      param = Object.assign(param, this.condition);
      param['caseStatus'] = newStatus;
      editApiCaseByParam(param).then(() => {
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id === data.id) { //  手动修改当前状态后，前端结束时间先用当前时间，等刷新后变成后台数据（相等）
            this.tableData[i].status = newStatus;
            break;
          }
        }
        this.$success(this.$t('commons.save_success'));
      });
    },

    handleEditBatch() {
      if (this.currentProtocol === 'HTTP') {
        this.valueArr.method = REQ_METHOD;
      } else if (this.currentProtocol === 'TCP') {
        this.valueArr.method = TCP_METHOD;
      } else if (this.currentProtocol === 'SQL') {
        this.valueArr.method = SQL_METHOD;
      } else if (this.currentProtocol === 'DUBBO') {
        this.valueArr.method = DUBBO_METHOD;
      }
      this.$refs.batchEdit.open();
    },
    batchEdit(form) {
      let arr = Array.from(this.selectRows);
      let ids = arr.map(row => row.id);
      let param = {};
      if (form.type === 'tags') {
        param.type = form.type;
        param.appendTag = form.appendTag;
        param.tagList = form.tags;
      } else {
        param[form.type] = form.value;
      }
      param[form.type] = form.value;
      param.ids = ids;
      param.projectId = this.projectId;
      param.selectAllDate = this.selectAll;
      param.unSelectIds = this.unSelection;
      param = Object.assign(param, this.condition);
      editApiCaseByParam(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.initTable();
      });
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
      testCaseBatchRun(obj).then(() => {
        this.condition.ids = [];
        this.$refs.batchRun.close();
        if (this.store.currentApiCase) {
          this.store.currentApiCase.case = true;
        } else {
          this.store.currentApiCase = {case: true};
        }
        this.$refs.taskCenter.open();
      });
    },
    openSyncCase(row) {
      this.currentCaseId = row.id;
      this.syncCaseVisible = true;
    },
    syncCase() {
      let ids = [];
      ids.push(this.currentCaseId);
      let fromData = this.$refs.synSetting.fromData;
      fromData.ids = ids;
      this.condition.syncConfig = fromData
      if (hasLicense()) {
        batchSyncCase(this.condition).then(response => {
          this.$message.success(this.$t('commons.save_success'));
          this.syncCaseVisible = false
          this.initTable();
        });
      }
    },
    openIgnoreCase(row) {
      this.currentCaseId = row.id;
      this.currentCaseName = row.name;
      this.ignoreCaseVisible = true;
    },
    ignoreCase() {
      let ids = [];
      ids.push(this.currentCaseId);
      let fromData = {
        protocol: true,
        method: true,
        path: true,
        headers: true,
        query: true,
        rest: true,
        body: true,
        delNotSame: true,
        runError: true,
        unRun: true,
      };
      fromData.ids = ids;
      this.condition.syncConfig = fromData
      if (hasLicense()) {
        batchIgnoreCase(this.condition).then(response => {
          this.$message.success(this.$t('commons.save_success'));
          this.ignoreCaseVisible = false
          this.initTable()
        });
      }
    },
    openBatchSync() {
      this.batchSyncCaseVisible = true;
    },
    batchSync() {
      let selectIds = this.$refs.caseTable.selectIds;
      let fromData = this.$refs.synSetting.fromData;
      fromData.ids = selectIds;
      this.condition.syncConfig = fromData
      if (hasLicense()) {
        batchSyncCase(this.condition).then(response => {
          this.batchSyncCaseVisible = false;
          this.$message.success(this.$t('commons.save_success'));
          this.initTable();
        });
      }
    },
    openBatchIgnore() {
      this.batchIgnoreCaseVisible = true;
    },
    batchIgnore() {
      if (hasLicense()) {
        let selectIds = this.$refs.caseTable.selectIds;
        let fromData = {
          protocol: true,
          method: true,
          path: true,
          headers: true,
          query: true,
          rest: true,
          body: true,
          delNotSame: true,
          runError: true,
          unRun: true,
        }
        fromData.ids = selectIds;
        this.condition.syncConfig = fromData
        batchIgnoreCase(this.condition).then(response => {
          this.batchIgnoreCaseVisible = false;
          this.$message.success(this.$t('commons.save_success'));
          this.initTable()
        });
      }
    },
    systemDisable(row) {
      return row.toBeUpdated !== true;
    }

  },
};
</script>
<style type="text/css" scoped>
.showTextColor {
  color: #783987;
  cursor: pointer;
}

.unShowTextColor {
  cursor: pointer;
}

.changeTap {
  margin: auto;
  width: 50%;
  text-align: center;
}
</style>
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

.ms-running {
  color: #783887;
}

.ms-unexecute {
}
</style>

