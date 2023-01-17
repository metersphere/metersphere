<template>
  <div>
    <ms-table
      :table-is-loading="this.result"
      :data="tableData"
      :condition="condition"
      :page-size="pageSize"
      :total="total"
      :screen-height="screenHeight"
      :remember-order="true"
      :fields.sync="fields"
      :field-key="tableHeaderKey"
      :row-order-group-id="condition.projectId"
      :enable-order-drag="enableOrderDrag"
      :enable-selection="((isUpcoming===true||isFocus===true||isCreation===true) && isShowAllColumn === true)"
      :batch-operators="batchButtons"
      :operators="(isFinish===false && showColum===true) ?tableOperatorButtons:[]"
      :operator-width="(isFinish===false && showColum===true)?'140':'0'"
      row-key="id"
      :disable-header-config=true
      @refresh="initTable"
      ref="apiTable">

      <ms-table-column
        prop="deleteTime"
        sortable
        v-if="trashEnable===true&&isShowAllColumn===true"
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
        v-if="trashEnable&&isShowAllColumn===true"
        :label="$t('commons.delete_user')"
        min-width="120"/>

      <span v-for="(item) in fields" :key="item.key">
          <ms-table-column
            prop="num"
            label="ID"
            :field="item"
            min-width="100px"
            :fields-width="fieldsWidth"
            sortable>

            <template slot-scope="scope">
              <el-tooltip :content="$t('commons.edit')">
                <a style="cursor:pointer" @click="editApi(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

        <ms-table-column
          prop="name"
          :label="$t('api_test.definition.api_name')"
          sortable="custom"
          :fields-width="fieldsWidth"
          min-width="120"
          :field="item"/>

        <ms-table-column
          prop="method"
          sortable="custom"
          :field="item"
          :filters="methodFilters"
          :fields-width="fieldsWidth"
          min-width="120px"
          :label="getApiRequestTypeName">
          <template v-slot:default="scope" class="request-method">
            <el-tag size="mini"
                    :style="{'background-color': getColor(true, scope.row.method), border: getColor(true, scope.row.method)}"
                    class="api-el-tag">
              {{ scope.row.method }}
            </el-tag>
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
          prop="path"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100px"
          :label="$t('api_test.definition.api_path')"/>

        <ms-table-column
          prop="status"
          sortable="custom"
          :filters="statusFilters"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="130px"
          :label="$t('api_test.definition.api_status')">
          <template v-slot:default="scope">
            <span @click.stop="clickt = 'stop'" v-if="isShowAllColumn">
              <el-dropdown class="test-case-status" @command="statusChange">
                <span class="el-dropdown-link">
                   <api-status :value="scope.row.status"/>
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
                                    :command="{item: scope.row, status: 'Completed'}">
                    {{ $t('test_track.plan.plan_status_completed') }}
                  </el-dropdown-item>

                </el-dropdown-menu>
              </el-dropdown>
            </span>
            <span class="el-dropdown-link" v-else>
              <api-status :value="scope.row.status"/>
            </span>
          </template>

        </ms-table-column>

        <ms-table-column
          prop="userName"
          sortable="custom"
          :filters="userFilters"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="110px"
          :label="$t('commons.create_user')"/>

        <ms-table-column
          prop="tags"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100px"
          v-if="isShowAllColumn"
          :label="$t('commons.tag')">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :show-tooltip="scope.row.tags.length===1&&itemName.length*12<=100" :content="itemName"
                    style="margin-left: 0px; margin-right: 2px"/>
            <span/>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('api_test.definition.api_last_time')"
          :field="item"
          :fields-width="fieldsWidth"
          v-if="isShowAllColumn"
          sortable="custom"
          min-width="160px"
          prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column prop="createTime"
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
          prop="projectName"
          :label="$t('api_test.definition.api_project')"
          :fields-width="fieldsWidth"
          min-width="120"
          :field="item"/>

        <ms-table-column
          prop="caseTotal"
          :field="item"
          v-if="isShowAllColumn"
          :fields-width="fieldsWidth"
          min-width="100px"
          :label="$t('api_test.definition.api_case_number')"/>

        <ms-table-column
          :field="item"
          prop="caseStatus"
          :fields-width="fieldsWidth"
          v-if="isShowAllColumn"
          min-width="110px"
          :label="$t('api_test.definition.api_case_status')">
          <template v-slot:default="{row}">
            <ms-api-report-status :status="row.caseStatus"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="casePassingRate"
          :field="item"
          min-width="180px"
          v-if="isShowAllColumn"
          :fields-width="fieldsWidth"
          :label="$t('api_test.definition.api_case_passing_rate')"/>

        </span>
    </ms-table>
    <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <ms-batch-edit ref="batchEdit" @batchEdit="batchEdit"
                   :data-count="$refs.apiTable ? $refs.apiTable.selectDataCounts : 0"
                   :typeArr="(isFocus&&isShowAllColumn) ? focusArr:typeArr"
                   :value-arr="(isFocus&&isShowAllColumn)?focusValueArr:valueArr"/>

    <el-dialog :visible.sync="dialogVisible"
               :fullscreen="showFullscreen" :close-on-click-modal="false" @close="closeSyncCase">
      <template slot="title">
        <div class="syncTitle">
          <div>
            <span style="font-size: 18px;font-weight: bold">{{ $t('workstation.sync') + $t('commons.track') }}</span>
          </div>
          <div>
            <el-button size="mini" class="sync" @click="syncCase">{{ $t('workstation.sync') }}
            </el-button>
            <el-button size="mini" @click="ignoreCase">{{ $t('workstation.ignore') }}</el-button>
            <span class="fulls-screen-btn" style="margin-left: 5px">
              <font-awesome-icon v-if="!showFullscreen" class="alt-ico" :icon="['fa', 'expand-alt']" size="lg"
                                 @click="showFullscreen=true"/>
              <font-awesome-icon v-if="showFullscreen" class="alt-ico" :icon="['fa', 'compress-alt']" size="lg"
                                 @click="showFullscreen=false"/>
            </span>
          </div>
        </div>
      </template>
      <sync-settings v-if="dialogVisible" ref="synSetting"></sync-settings>
      <ms-search
        :condition.sync="caseCondition"
        :base-search-tip="$t('commons.search_by_id_name_tag')"
        @search="search">
      </ms-search>
      <base-api-case-table
        :version-filters="versionFilters"
        :table-data="caseTableData"
        :condition="caseCondition"
        :loading="caseResult.loading"
        :total="caseTotal"
        :api-definition-id="currentApiId"
        @openNewCase="openNewCase"
        @getCaseListById="getCaseListById"
        ref="caseTable"
      >
      </base-api-case-table>
    </el-dialog>

    <el-dialog :visible.sync="ignoreApiVisible">
      <span v-if="currentIgnoreType==='API'">{{
          $t('commons.confirm') + $t('workstation.ignore') + $t('commons.update') + $t('workstation.table_name.api_definition') + currentIgnoreName + "?"
        }}</span>
      <span v-else>{{
          $t('commons.confirm') + $t('workstation.ignore') + $t('commons.update') + $t('workstation.table_name.api_case') + "?"
        }}</span>
      <br/>
      <span style="color: red">{{ $t('workstation.sync_case_tips') }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="ignoreApiVisible = false">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="startIgnoreApi()">{{$t('commons.confirm')}}</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="batchSyncApiVisible" :title="$t('commons.batch')+$t('workstation.sync')">
      <span>{{ $t('workstation.sync') + $t('commons.setting') }}</span><br/>
      <sync-settings ref="synSetting"></sync-settings>
      <span style="color: red">{{ $t('workstation.batch_sync_api_tips') }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchSyncApiVisible = false">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="batchApiSync()">{{$t('commons.confirm')}}</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="batchIgnoreApiVisible" :title="$t('commons.batch')+$t('workstation.ignore')">
      <span>{{
          $t('commons.confirm') + $t('commons.batch') + $t('workstation.ignore') + $t('commons.update') + $t('workstation.table_name.api_definition') + '?'
        }}</span><br/>
      <span style="color: red">{{ $t('workstation.batch_ignore_case_tips') }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchIgnoreApiVisible = false">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="batchApiIgnore()">{{$t('commons.confirm')}}</el-button>
      </span>
    </el-dialog>


  </div>
</template>

<script>



import {getUUID} from "metersphere-frontend/src/utils";
import {getCurrentProjectID,getCurrentUserId, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasLicense,hasPermission} from "metersphere-frontend/src/utils/permission";
import {API_LIST} from 'metersphere-frontend/src/utils/constants';
import MsTableHeaderSelectPopover from "metersphere-frontend/src/components/table/MsTableHeaderSelectPopover";
import ApiStatus from "@/business/module/api/ApiStatus";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import {
  API_CASE_CONFIGS,
  API_DEFINITION_CONFIGS
} from "metersphere-frontend/src/components/search/search-components";
import MsTipButton from "metersphere-frontend/src/components/MsTipButton";
import CaseBatchMove from "@/business/module/api/BatchMove";
import {buildBatchParam, getCustomTableWidth, initCondition,} from "metersphere-frontend/src/utils/tableUtils";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {
  API_METHOD_COLOUR,
  API_STATUS,
  DUBBO_METHOD,
  REQ_METHOD,
  SQL_METHOD,
  TCP_METHOD,
} from "@/business/component/js/JsonData";
import {getCustomTableHeaderByXpack} from "@/business/component/js/table-head-util";
import MsSearch from "metersphere-frontend/src/components/search/MsSearch";
import BaseApiCaseTable from "@/business/component/BaseApiCaseTable";
import SyncSettings from "@/business/component/SyncSettings";
import {getUrl} from "@/business/component/js/urlhelper";
import PlanStatusTableItem from "@/business/module/plan/PlanStatusTableItem";
import {TYPE_TO_C} from "@/business/module/api/scenario/Setting";
import MsBatchEdit from "@/business/module/api/BatchEdit";
import {getProjectVersions} from "metersphere-frontend/src/api/version";
import {
  apiTestCasePage, batchEditByParams, batchIgnoreApi, batchIgnoreCase,
  batchSyncApi,
  batchSyncCase, editApiCaseByParam, getCaseById,
  getDefinitionById,
  getDefinitionPage
} from "@/api/api";
import {getOwnerProjectIds, getProject, getProjectApplication} from "@/api/project";
import {getProjectMember} from "@/api/user";
import MsApiReportStatus from "@/business/module/api/ApiReportStatus";


export default {
  name: "ApiDefinitionTableList",
  components: {
    SyncSettings,
    HeaderLabelOperate,
    CaseBatchMove,
    ApiStatus,
    MsTableHeaderSelectPopover,
    MsTableButton,
    MsTableOperatorButton,
    MsTableOperator,
    MsTableHeader,
    MsTablePagination,
    MsTag,
    MsContainer,
    MsTipButton,
    MsTableAdvSearchBar,
    MsTable,
    MsTableColumn,
    MsSearch,
    BaseApiCaseTable,
    PlanStatusTableItem,
    MsBatchEdit,
    MsApiReportStatus
  },
  data() {
    return {
      showTextColor: "showTextColor",
      unShowTextColor: "unShowTextColor",
      type: API_LIST,
      tableHeaderKey: "API_DEFINITION",
      fields: getCustomTableHeaderByXpack('API_DEFINITION_HEAD'),
      fieldsWidth: getCustomTableWidth('API_DEFINITION'),
      condition: {
        components: API_DEFINITION_CONFIGS,
      },
      result: false,
      moduleId: "",
      enableOrderDrag: true,
      selectDataRange: "all",
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'},
      ],
      methodFilters: [
        {text: 'GET', value: 'GET'},
        {text: 'POST', value: 'POST'},
        {text: 'PUT', value: 'PUT'},
        {text: 'PATCH', value: 'PATCH'},
        {text: 'DELETE', value: 'DELETE'},
        {text: 'OPTIONS', value: 'OPTIONS'},
        {text: 'HEAD', value: 'HEAD'},
        {text: 'CONNECT', value: 'CONNECT'},
        {text: 'DUBBO', value: 'DUBBO'},
        {text: 'dubbo://', value: 'dubbo://'},
        {text: 'SQL', value: 'SQL'},
        {text: 'TCP', value: 'TCP'},
      ],
      typeArr: [
        {id: 'status', name: this.$t('api_test.definition.api_status')},
        {id: 'method', name: this.$t('api_test.definition.api_type')},
        {id: 'userId', name: this.$t('api_test.definition.api_principal')},
        {id: 'tags', name: this.$t('commons.tag')}
      ],
      focusArr: [
        {id: 'follow', name: this.$t('commons.follow')},
      ],
      userFilters: [],
      versionFilters: [],
      valueArr: {
        status: API_STATUS,
        method: REQ_METHOD,
        userId: [],
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
      selectDataCounts: 0,
      projectName: "",
      batchButtons: [],
      syncButtons: [
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
        {
          name: this.$t('api_test.definition.request.batch_edit'),
          handleClick: this.handleEditBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        }
      ],
      commonButtons: [
        {
          name: this.$t('api_test.definition.request.batch_edit'),
          handleClick: this.handleEditBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        }
      ],
      tableOperatorButtons: [
        {
          tip: this.$t('commons.edit'),
          icon: "el-icon-edit",
          exec: this.editApi,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        },
        {
          tip: this.$t('workstation.sync'),
          icon: "el-icon-refresh",
          exec: this.syncApiAndCase,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        },
        {
          tip: this.$t('workstation.ignore'),
          icon: "el-icon-close",
          exec: this.ignoreApi,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        },
      ],
      isFinish: true,
      dialogVisible: false,
      caseTableData: [],
      caseResult: {},
      caseTotal: 0,
      caseCondition: {
        components: API_CASE_CONFIGS
      },
      currentApiId: '',
      syncFromData: {},
      ignoreApiVisible: false,
      currentIgnoreName: "",
      currentIgnoreType: 'API',
      batchSyncApiVisible: false,
      batchIgnoreApiVisible: false,
      hasEditPermission: false,
      showFullscreen: false,
      openUpdateRule: true,
      showColum: false
    };
  },
  props: {
    currentProtocol: String,
    selectNodeIds: Array,
    isSelectThisWeek: String,
    activeDom: String,
    initApiTableOpretion: String,
    currentVersion: String,
    visible: {
      type: Boolean,
      default: false,
    },
    isCaseRelevance: {
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
    isFocus: {
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
    isCreation: {
      type: Boolean,
      default: false,
    },
    isUpcoming: {
      type: Boolean,
      default: false,
    },
    screenHeight: {
      type: [Number, String],
      default() {
        return 'calc(100vh - 220px)';
      }
    }, //屏幕高度
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    getApiRequestTypeName() {
      if (this.currentProtocol === 'TCP') {
        return this.$t('api_test.definition.api_agreement');
      } else {
        return this.$t('api_test.definition.api_type');
      }
    }
  },
  created: function () {
    if (hasLicense()) {
      this.showColum = true;
    }
    if (!this.projectName || this.projectName === "") {
      this.getProjectName();
    }
    if (this.isFocus) {
      if (this.condition.filters) {
        delete this.condition.filters['user_id']
      }
      if (this.condition.userId) {
        delete this.condition.userId
      }
      this.condition.combine = {followPeople: {operator: "current user", value: "current user",}}
    } else
      if (this.isCreation) {
      if (this.condition.filters) {
        delete this.condition.filters['user_id']
      }
      this.condition.userId = getCurrentUserId();

    } else {
      this.condition.userId = getCurrentUserId();
      if (this.condition.filters) {
        this.condition.filters.status = ["Prepare", "Underway"];
      } else {
        this.condition.filters = {status: ["Prepare", "Underway"]};
      }
    }
    this.condition.versionId = this.currentVersion;
    this.initTable();
    this.getMaintainerOptions();
    this.getProjectApplication();

    // 通知过来的数据跳转到编辑
    if (this.$route.query.resourceId) {
      getDefinitionById(this.$route.query.resourceId).then((response) => {
        this.editApi(response.data);
      });
    }
    this.getVersionOptions();
    if (this.isFinish) {
      this.batchButtons = this.commonButtons
    } else {
      this.batchButtons = this.syncButtons
    }
    this.hasEditPermission = hasPermission('PROJECT_TRACK_PLAN:READ+EDIT');
  },
  watch: {
    trashEnable() {
      if (this.condition.filters) {
        this.condition.filters.status = ["Prepare", "Underway"];
      } else {
        this.condition.filters = {status: ["Prepare", "Underway"]};
      }
      initCondition(this.condition, false);
      this.initTable();
    },
    currentProtocol() {
      this.condition.protocol = this.currentProtocol;
      initCondition(this.condition, false);
      this.initTable();
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTable();
      // 选择了版本过滤，版本列上的checkbox也进行过滤
      this.getVersionOptions(this.currentVersion);
    },
    isFinish() {
      this.condition.toBeUpdated = !this.isFinish;
      if (!this.isFinish) {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
      } else {
        this.condition.filters = {status: ["Prepare", "Underway", "NO"]};
      }
      this.initTable();
    },
  },
  methods: {
    changeTabState(name) {
      this.$refs.apiTable.clearFilter();
      this.isFinish = name !== 'update';
    },
    getProjectName() {
      getProject(this.projectId).then(response => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
        }
      });
    },
    initTable(currentProtocol) {
      if (this.$refs.apiTable) {
        this.$refs.apiTable.clear();
      }
      initCondition(this.condition, this.condition.selectAll);
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
          if (this.condition.filters) {
            if (!this.condition.filters.status || this.condition.filters.status.length === 0) {
              this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
            }
          } else {
            this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
          }
          this.condition.combine = {creator: {operator: "current user", value: "current user",}}
      } else {
          if (this.isFinish === false && this.openUpdateRule === false) {
            this.tableData = [];
            this.total = 0;
            this.$message.warning(this.$t('workstation.apply_tip'))
            return;
          }
          this.buildCondition();
        }
      this.selectDataCounts = 0;
      if (!this.trashEnable) {
        this.condition.moduleIds = this.selectNodeIds;
      }
      if (this.isSelectAll === false) {
        this.condition.projectId = this.projectId;
      }

      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }

      this.enableOrderDrag = (this.condition.orders && this.condition.orders.length) <= 0;

      this.condition.selectThisWeedData = false;
      this.condition.apiCaseCoverage = null;
      if (currentProtocol) {
        this.condition.moduleIds = [];
      }
      this.condition.workspaceId = getCurrentWorkspaceId();

      this.result = getDefinitionPage(this.currentPage,this.pageSize,this.condition).then(response => {
        this.genProtocolFilter(this.condition.protocol);
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
        this.tableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
      });
    },
    buildCondition() {
      if (this.isFinish === true) {
        this.batchButtons = this.commonButtons
        if (!this.condition.filters || this.condition.filters.status === null) {
          this.condition.filters.status = ["Prepare", "Underway"];
        }
        if (this.condition.filters.status && this.condition.filters.status.length > 0) {
          for (let i = 0; i < this.condition.filters.status.length; i++) {
            if (this.condition.filters.status[i] === "Completed") {
              this.condition.filters.status[i] = "NO"
            }
          }
        }
        this.condition.combine = {creator: {operator: "current user", value: "current user",}}
        if (this.condition.toUpdate) {
          delete this.condition.toUpdate
        }
      } else {
        this.batchButtons = this.syncButtons
        if (this.condition.filters) {
          if (!this.condition.filters.status || this.condition.filters.status.length === 0) {
            this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
          }
        } else {
          this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
        }
        this.condition.combine = {creator: {operator: "current user", value: "current user",}}
      }
    },
    genProtocolFilter(protocolType) {
      if (protocolType === "HTTP") {
        this.methodFilters = [
          {text: 'GET', value: 'GET'},
          {text: 'POST', value: 'POST'},
          {text: 'PUT', value: 'PUT'},
          {text: 'PATCH', value: 'PATCH'},
          {text: 'DELETE', value: 'DELETE'},
          {text: 'OPTIONS', value: 'OPTIONS'},
          {text: 'HEAD', value: 'HEAD'},
          {text: 'CONNECT', value: 'CONNECT'},
        ];
      } else if (protocolType === "TCP") {
        this.methodFilters = [
          {text: 'TCP', value: 'TCP'},
        ];
      } else if (protocolType === "SQL") {
        this.methodFilters = [
          {text: 'SQL', value: 'SQL'},
        ];
      } else if (protocolType === "DUBBO") {
        this.methodFilters = [
          {text: 'DUBBO', value: 'DUBBO'},
          {text: 'dubbo://', value: 'dubbo://'},
        ];
      } else {
        this.methodFilters = [
          {text: 'GET', value: 'GET'},
          {text: 'POST', value: 'POST'},
          {text: 'PUT', value: 'PUT'},
          {text: 'PATCH', value: 'PATCH'},
          {text: 'DELETE', value: 'DELETE'},
          {text: 'OPTIONS', value: 'OPTIONS'},
          {text: 'HEAD', value: 'HEAD'},
          {text: 'CONNECT', value: 'CONNECT'},
          {text: 'DUBBO', value: 'DUBBO'},
          {text: 'dubbo://', value: 'dubbo://'},
          {text: 'SQL', value: 'SQL'},
          {text: 'TCP', value: 'TCP'},
        ];
      }
    },
    getMaintainerOptions() {
      getProjectMember().then(response => {
        this.valueArr.userId = response.data;
        if (this.isCreation) {
          response.data.map(u => {
            if (u.id === getCurrentUserId()) {
              let a = {text: u.name, value: u.id};
              this.userFilters.push(a);
            }
          });
        } else {
          this.userFilters = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
        }
      });
    },

    editApi(resource) {
      let workspaceId = getCurrentWorkspaceId();
      let isTurnSpace = true
      if (resource.projectId !== getCurrentProjectID()) {
        isTurnSpace = false;
        getProject(resource.projectId).then(response => {
          if (response.data) {
            workspaceId = response.data.workspaceId;
            isTurnSpace = true;
            this.checkPermission(resource, workspaceId, isTurnSpace,"API");
          }
        });
      } else {
        this.checkPermission(resource, workspaceId, isTurnSpace,"API");
      }
    },
    getColor(enable, method) {
      if (enable) {
        return this.methodColorMap.get(method);
      }
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
    search() {
      this.getCaseListById(this.currentApiId, 1, 10)
    },
    getCaseListById(apiId, page, pageSize) {
      if (!this.caseCondition.filters) {
        this.caseCondition.filters = {}
      }
      if (!this.caseCondition.filters.case_status) {
        this.caseCondition.filters.case_status = this.condition.filters.status;
      }
      this.caseCondition.apiDefinitionId = apiId;
      if (this.condition.versionId) {
        this.caseCondition.versionId = this.condition.versionId
      }
      if (!this.caseCondition.toBeUpdated) {
        this.caseCondition.toBeUpdated = true;
      }
      if (!page) {
        page = this.currentPage
      }
      if (!pageSize) {
        pageSize = this.pageSize
      }
      this.caseResult = apiTestCasePage(page,pageSize,this.caseCondition).then(response => {
        let data = response.data;
        this.caseTableData = data.listObject
        this.caseTableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
        this.caseTotal = data.itemCount;
        this.dialogVisible = true;
      });
    },
    syncApiAndCase(row) {
      this.currentApiId = row.id;
      this.getCaseListById(row.id, 1, 10)
    },
    ignoreApi(row) {
      this.currentApiId = row.id;
      this.currentIgnoreName = row.name;
      this.currentIgnoreType = "API"
      this.ignoreApiVisible = true;
    },
    startIgnoreApi() {
      if (this.currentIgnoreType === "API") {
        let ids = [];
        ids.push(this.currentApiId);
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
        fromData.ids = ids;
        this.condition.syncConfig = fromData;
        if (hasLicense()) {
          batchIgnoreApi(this.condition).then(response => {
            this.ignoreApiVisible = false;
            this.$message.success(this.$t('commons.save_success'));
            this.dialogVisible = false;
            this.initTable();
          });
        }
      } else {
        this.startIgnoreCase();
      }
    },
    syncCase() {
      if (hasLicense()) {
        let selectIds = [];
        selectIds = this.$refs.caseTable.getSelectIds();
        if (!selectIds || selectIds.length === 0) {
          this.$message.warning(this.$t('commons.please_select') + this.$t('commons.track'))
          return;
        }
        let fromData = this.$refs.synSetting.fromData;
        fromData.ids = selectIds;
        this.caseCondition.syncConfig = fromData
        batchSyncCase(this.caseCondition).then(response => {
          this.dialogVisible = false;
          this.$message.success(this.$t('commons.save_success'));
          this.initTable();
        });
      }
    },
    ignoreCase() {
      this.currentIgnoreType = "CASE";
      let selectIds = [];
      selectIds = this.$refs.caseTable.getSelectIds();
      if (!selectIds || selectIds.length === 0) {
        this.$message.warning(this.$t('commons.please_select') + this.$t('commons.track'))
        return;
      }
      this.ignoreApiVisible = true;

    },
    startIgnoreCase() {
      let selectIds = [];
      selectIds = this.$refs.caseTable.getSelectIds();
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
      this.caseCondition.syncConfig = fromData
      if (hasLicense()) {
        batchIgnoreCase(this.caseCondition).then(response => {
          this.ignoreApiVisible = false;
          this.$message.success(this.$t('commons.save_success'));
          this.dialogVisible = false;
          this.initTable();
        });
      }
    },
    openBatchSync() {
      this.batchSyncApiVisible = true;
    },
    batchApiSync() {
      let selectIds = this.$refs.apiTable.selectIds;
      let fromData = this.$refs.synSetting.fromData;
      fromData.ids = selectIds;
      this.condition.syncConfig = fromData;
      if (hasLicense()) {
        batchSyncApi(this.condition).then(response => {
          this.batchSyncApiVisible = false;
          this.$message.success(this.$t('commons.save_success'));
          this.initTable();
        });
      }
    },
    openBatchIgnore() {
      this.batchIgnoreApiVisible = true
    },
    batchApiIgnore() {
      if (hasLicense()) {
        let selectIds = this.$refs.apiTable.selectIds;
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
        this.condition.syncConfig = fromData;
        batchIgnoreApi(this.condition).then(response => {
          this.batchIgnoreApiVisible = false;
          this.$message.success(this.$t('commons.save_success'));
          this.initTable();
        });
      }
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
      let param = buildBatchParam(this, this.$refs.apiTable.selectIds);
      if (form.type === 'tags') {
        param.type = form.type;
        param.appendTag = form.appendTag;
        param.tagList = form.tags;
      } else {
        param[form.type] = form.value;
      }
      batchEditByParams(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.initTable();
      });
    },
    openNewCase(resource) {
      let workspaceId = getCurrentWorkspaceId();
      let isTurnSpace = true
      this.checkPermission(resource, workspaceId, isTurnSpace,"CASE");
    },
    checkPermission(resource, workspaceId, isTurnSpace,refType) {
      getOwnerProjectIds().then(res => {
        const project = res.data.find(p => p === getCurrentProjectID());
        if (!project) {
          this.$warning(this.$t('commons.no_permission'));
        } else {
          this.gotoTurn(resource, workspaceId, isTurnSpace,refType)
        }

      })
    },
    gotoTurn(resource, workspaceId, isTurnSpace,refType) {
      if (refType === 'CASE') {
        getCaseById(resource.id).then((response) => {
          if (response.data) {
            response.data.sourceId = resource.id;
            response.data.type = this.setResourceType(response.data.apiMethod);
            response.data.refType = 'CASE';
            response.data.workspaceId = workspaceId;
            if (isTurnSpace) {
              this.clickCase(response.data)
            }
          } else {
            this.$error(this.$t('api_test.case_jump_message'))
          }
        });
      } else {
        if (resource.protocol === 'dubbo://') {
          resource.protocol = 'DUBBO'
        }
        let definitionData = this.$router.resolve({
          path: '/api/definition/default/' + getUUID() + '/api/edit:' + resource.id + '/' + resource.projectId + '/' + resource.protocol + '/' + workspaceId,
        });
        if (isTurnSpace) {
          window.open(definitionData.href, '_blank');
        }
      }
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
      let resourceId = resource.sourceId;
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
    statusChange(dataSource) {
      let data = dataSource.item;
      data.status = dataSource.status;
      let newStatus = dataSource.status;
      let ids = [];
      ids.push(data.id);
      let param = buildBatchParam(this, ids);
      param['status'] = newStatus;
      batchEditByParams(param).then(() => {
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id === data.id) { //  手动修改当前状态后，前端结束时间先用当前时间，等刷新后变成后台数据（相等）
            this.tableData[i].status = newStatus;
            break;
          }
        }
        this.$success(this.$t('commons.save_success'));
      });
    },
    setParameters(data) {
      data.request.name = data.name;
      if (this.currentProtocol === "DUBBO" || this.currentProtocol === "dubbo://") {
        data.request.protocol = "dubbo://";
      } else {
        data.request.protocol = this.currentProtocol;
      }
      if (data.isCopy) {
        data.sourceId = data.id;
        data.id = getUUID();
        data.request.id = data.id;
      } else {
        if (data.id) {
          data.request.id = data.id;
        } else {
          data.id = data.request.id;
        }
      }
      if (!data.method) {
        data.method = this.currentProtocol;
      }
    },
    getProjectApplication(){
      getProjectApplication(this.projectId).then(res=>{
        if (res.data) {
          this.openUpdateRule = res.data.openUpdateRule !== false;
        }
      })
    },
    sort(stepArray) {
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
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
    closeSyncCase() {
      this.showFullscreen = false;
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
.syncBtn {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

.sync {
  border: solid 1px #6d317c !important;
  background-color: var(--primary_color) !important;
  color: #FFFFFF !important;
}

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

.ms-select-all >>> th:first-child {
  margin-top: 20px;
}

.ms-select-all >>> th:nth-child(2) {
  top: -2px;
}

.syncTitle {
  width: 97%;
  display: flex;
  justify-content: space-between;
}
</style>
