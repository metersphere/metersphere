<template>
  <span>
    <span>
      <ms-search
        v-if="visibleSearch"
        :condition.sync="condition"
        :base-search-tip="$t('commons.search_by_id_name_tag_path')"
        :base-search-width="260"
        @search="search">
      </ms-search>

      <ms-table
        :data="tableData"
        :select-node-ids="selectNodeIds"
        :condition="condition"
        :page-size="pageSize"
        :total="total"
        enableSelection
        :batch-operators="trashEnable ? trashButtons : buttons"
        :screen-height="screenHeight"
        :operators="tableOperatorButtons"
        operator-width="200px"
        :remember-order="true"
        @refresh="initTable"
        :fields.sync="fields"
        :row-order-func="editApiDefinitionOrder"
        :row-order-group-id="condition.projectId"
        :table-is-loading="this.result"
        :field-key="tableHeaderKey"
        :enable-order-drag="enableOrderDrag"
        row-key="id"
        ref="table">
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
          min-width="120"/>
        <span v-for="item in fields" :key="item.key">
          <ms-table-column prop="num" label="ID" :field="item" min-width="100px" :fields-width="fieldsWidth" sortable>
            <template slot-scope="scope" v-if="!trashEnable">
              <el-tooltip content="编辑">
                <a style="cursor: pointer" @click="editApi(scope.row)">
                  {{ scope.row.num }}
                </a>
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
            prop="status"
            :sortable="trashEnable ? false : 'custom'"
            :filters="!trashEnable ? statusFilters : null"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('api_test.definition.api_status')">
            <template v-slot:default="scope">
              <span class="el-dropdown-link">
                <api-status :value="scope.row.status"/>
              </span>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="method"
            sortable="custom"
            :field="item"
            :filters="methodFilters"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="getApiRequestTypeName">
            <template v-slot:default="scope" class="request-method">
              <el-tag
                size="mini"
                :style="{
                  'background-color': getColor(true, scope.row.method),
                  border: getColor(true, scope.row.method),
                }"
                class="api-el-tag">
                {{ scope.row.method }}
              </el-tag>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="userName"
            sortable="custom"
            :filters="userFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="100px"
            :label="$t('api_test.definition.request.responsible')"/>
          <ms-table-column
            prop="path"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="100px"
            :label="$t('api_test.definition.api_path')"/>

          <ms-table-column
            prop="tags"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="80px"
            :show-overflow-tooltip="false"
            :label="$t('commons.tag')">
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
                    style="margin-left: 0px; margin-right: 2px"/>
                </div>
              </el-tooltip>
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
            :label="$t('api_test.definition.api_last_time')"
            :field="item"
            :fields-width="fieldsWidth"
            sortable="custom"
            min-width="140px"
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

          <ms-table-column
            prop="caseTotal"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="100px"
            :label="$t('api_test.definition.api_case_number')"/>

          <ms-table-column
            :field="item"
            prop="caseStatus"
            :fields-width="fieldsWidth"
            min-width="100px"
            :label="$t('api_test.definition.api_case_status')">
            <template v-slot:default="{ row }">
              <ms-api-report-status :status="row.caseStatus" style="text-align: left;"/>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="casePassingRate"
            :field="item"
            min-width="120px"
            :fields-width="fieldsWidth"
            :label="$t('api_test.definition.api_case_passing_rate')"/>

          <ms-table-column
            prop="description"
            :field="item"
            min-width="120px"
            :fields-width="fieldsWidth"
            :label="$t('commons.description')"/>
        </span>
        <template v-if="!trashEnable" v-slot:opt-behind="scope">
          <table-extend-btns
            :dropdown-items="dropdownItems"
            :row="scope.row"/>
        </template>
      </ms-table>
      <ms-table-pagination
        :change="initTable"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"/>
    </span>
    <ms-api-case-list @refresh="initTable" @showExecResult="showExecResult" :currentApi="selectApi" ref="caseList"/>
    <!--批量编辑-->
    <ms-batch-edit
      ref="batchEdit"
      @batchEdit="batchEdit"
      :data-count="$refs.table ? $refs.table.selectDataCounts : 0"
      :typeArr="typeArr"
      :value-arr="valueArr"/>
    <!--从指定版本复制数据-->
    <version-selector @handleSave="handleCopyDataFromVersion" ref="versionSelector"/>
    <!--高级搜索-->
    <ms-table-adv-search-bar :condition.sync="condition" :showLink="false" ref="searchBar" @search="search"/>
    <!--查看引用-->
    <ms-show-reference ref="viewRef" :show-plan="false" :is-has-ref="false" api-type="API"/>
    <case-batch-move @refresh="initTable" @moveSave="moveSave" ref="testCaseBatchMove"/>

    <relationship-graph-drawer :graph-data="graphData" ref="relationshipGraph"/>
    <!--  删除接口提示  -->
    <list-item-delete-confirm ref="apiDeleteConfirm" @handleDelete="_handleDelete"/>
  </span>
</template>

<script>
import {
  batchCopyByParams,
  batchEditByParams,
  copyDataByVersion,
  definitionReduction,
  delDefinition,
  delDefinitionByRefId,
  deleteBatchByParams,
  editApiDefinitionOrder,
  exportDefinition,
  getDefinitionById,
  getDefinitionPage,
  getDefinitionVersions,
  removeToGcByIds,
  removeToGcByParams,
} from '@/api/definition';
import {getMaintainer, getProject} from '@/api/project';
import {getProjectVersions, versionEnableByProjectId} from '@/api/xpack';
import MsTableHeader from 'metersphere-frontend/src/components/MsTableHeader';
import MsTableOperator from 'metersphere-frontend/src/components/MsTableOperator';
import MsTableOperatorButton from 'metersphere-frontend/src/components/MsTableOperatorButton';
import MsTableButton from 'metersphere-frontend/src/components/MsTableButton';
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import MsTable from 'metersphere-frontend/src/components/table/MsTable';
import MsTag from 'metersphere-frontend/src/components/MsTag';
import MsApiCaseList from '../case/EditApiCase';
import MsContainer from 'metersphere-frontend/src/components/MsContainer';
import MsTableColumn from 'metersphere-frontend/src/components/table/MsTableColumn';
import MsBottomContainer from '../BottomContainer';
import MsBatchEdit from '../basis/BatchEdit';
import {API_METHOD_COLOUR, API_STATUS, DUBBO_METHOD, REQ_METHOD, SQL_METHOD, TCP_METHOD} from '../../model/JsonData';
import {downloadFile, operationConfirm} from 'metersphere-frontend/src/utils';
import {getCurrentProjectID} from 'metersphere-frontend/src/utils/token';
import {hasLicense} from 'metersphere-frontend/src/utils/permission';
import {API_LIST} from 'metersphere-frontend/src/utils/constants';
import MsTableHeaderSelectPopover from 'metersphere-frontend/src/components/table/MsTableHeaderSelectPopover';
import ApiStatus from '@/business/definition/components/list/ApiStatus';
import MsTableAdvSearchBar from 'metersphere-frontend/src/components/search/MsTableAdvSearchBar';
import {API_DEFINITION_CONFIGS} from 'metersphere-frontend/src/components/search/search-components';
import {API_DEFINITION_CONFIGS_TRASH, getProtocolFilter} from '@/business/definition/api-definition';
import MsTipButton from 'metersphere-frontend/src/components/MsTipButton';
import CaseBatchMove from '@/business/definition/components/basis/BatchMove';
import {
  buildBatchParam,
  deepClone,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  getSelectDataCounts,
  initCondition,
} from 'metersphere-frontend/src/utils/tableUtils';
import HeaderLabelOperate from 'metersphere-frontend/src/components/head/HeaderLabelOperate';
import {Body} from '@/business/definition/model/ApiTestModel';
import {getGraphByCondition} from '@/api/graph';
import ListItemDeleteConfirm from 'metersphere-frontend/src/components/ListItemDeleteConfirm';
import MsSearch from 'metersphere-frontend/src/components/search/MsSearch';
import {buildNodePath} from 'metersphere-frontend/src/model/NodeTree';
import VersionSelector from '@/business/definition/components/version/VersionSelector';
import TableExtendBtns from "@/business/definition/components/complete/table/TableExtendBtns";
import MsShowReference from "@/business/definition/components/reference/ShowReference";
import {getApiTemplate} from "@/api/api-template";
import {getAdvSearchCustomField} from "metersphere-frontend/src/components/search/custom-component";

export default {
  name: 'ApiList',
  components: {
    ListItemDeleteConfirm,
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
    MsApiCaseList,
    MsContainer,
    MsBottomContainer,
    MsBatchEdit,
    MsTipButton,
    MsTableAdvSearchBar,
    MsTable,
    MsTableColumn,
    MsSearch,
    VersionSelector,
    TableExtendBtns,
    MsShowReference,
    MsApiReportStatus: () => import('../../../automation/report/ApiReportStatus'),
    RelationshipGraphDrawer: () => import('metersphere-frontend/src/components/graph/RelationshipGraphDrawer'),
  },
  data() {
    return {
      type: API_LIST,
      tableHeaderKey: 'API_DEFINITION',
      fields: getCustomTableHeader('API_DEFINITION', undefined),
      fieldsWidth: getCustomTableWidth('API_DEFINITION'),
      condition: {
        components: this.trashEnable ? API_DEFINITION_CONFIGS_TRASH : API_DEFINITION_CONFIGS,
      },
      selectApi: {},
      result: false,
      moduleId: '',
      enableOrderDrag: true,
      selectDataRange: 'all',
      graphData: [],
      isMoveBatch: true,
      deletePath: '/test/case/delete',
      buttons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_API'],
        },
        {
          name: this.$t('api_test.definition.request.batch_edit'),
          handleClick: this.handleEditBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API'],
        },
        {
          name: this.$t('api_test.definition.request.batch_move'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API'],
        },
        {
          name: this.$t('api_test.batch_copy'),
          handleClick: this.handleBatchCopy,
          permissions: ['PROJECT_API_DEFINITION:READ+CREATE_API'],
        },
        {
          name: this.$t('api_definition.copy_data_from_other_version'),
          isXPack: true,
          handleClick: this.batchCopyDataFromVersion,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API'],
        },
        {
          name: this.$t('test_track.case.generate_dependencies'),
          handleClick: this.generateGraph,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API'],
        },
      ],
      trashButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_API'],
        },
        {
          name: this.$t('commons.batch_restore'),
          handleClick: this.handleBatchRestore,
        },
      ],
      tableOperatorButtons: [],
      tableUsualOperatorButtons: [
        {
          tip: this.$t('api_test.automation.execute'),
          icon: 'el-icon-video-play',
          exec: this.runApi,
          class: 'run-button',
          permissions: ['PROJECT_API_DEFINITION:READ+RUN'],
        },
        {
          tip: this.$t('commons.edit'),
          icon: 'el-icon-edit',
          exec: this.editApi,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API'],
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.handleDelete,
          icon: 'el-icon-delete',
          type: 'danger',
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_API'],
        },
        {
          tip: this.$t('commons.copy'),
          exec: this.handleCopy,
          icon: 'el-icon-document-copy',
          type: 'primary',
          permissions: ['PROJECT_API_DEFINITION:READ+COPY_API'],
        },
      ],
      tableTrashOperatorButtons: [
        {
          tip: this.$t('commons.reduction'),
          icon: 'el-icon-refresh-left',
          exec: this.reductionApi,
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.handleDelete,
          icon: 'el-icon-delete',
          type: 'danger',
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_API'],
        },
      ],
      dropdownItems: [
        {
          name: this.$t('api_test.automation.view_ref'),
          value: "ref",
          permissions: ['PROJECT_API_DEFINITION:READ'],
          exec: this.showCaseRef,
        },
        {
          name: this.$t('commons.view') + "CASE",
          value: "case",
          permissions: ['PROJECT_API_DEFINITION:READ'],
          exec: this.handleTestCase,
        }
      ],
      typeArr: [
        {id: 'status', name: this.$t('api_test.definition.api_status')},
        {id: 'method', name: this.$t('api_test.definition.api_type')},
        {id: 'userId', name: this.$t('api_test.definition.api_principal')},
        {id: 'tags', name: this.$t('commons.tag')},
      ],
      statusFilters: [
        {
          text: this.$t('test_track.plan.plan_status_prepare'),
          value: 'Prepare',
        },
        {
          text: this.$t('test_track.plan.plan_status_running'),
          value: 'Underway',
        },
        {
          text: this.$t('test_track.plan.plan_status_completed'),
          value: 'Completed',
        },
      ],

      statusFiltersTrash: [{text: this.$t('test_track.plan.plan_status_trash'), value: 'Trash'}],

      caseStatusFilters: [
        {
          text: this.$t('api_test.home_page.detail_card.unexecute'),
          value: '未执行',
        },
        {text: this.$t('test_track.review.pass'), value: '通过'},
        {text: this.$t('test_track.review.un_pass'), value: '未通过'},
      ],
      methodFilters: [],
      userFilters: [],
      versionFilters: [],
      valueArr: {
        status: API_STATUS,
        method: REQ_METHOD,
        userId: [],
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 180px)', //屏幕高度,
      environmentId: undefined,
      selectDataCounts: 0,
      projectName: '',
      versionEnable: false,
      isFirstInitTable: true,
      visibleSearch: true,
    };
  },
  props: {
    currentProtocol: String,
    currentVersion: String,
    selectNodeIds: Array,
    isSelectThisWeek: String,
    activeDom: String,
    initApiTableOpretion: String,
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
    },
    editApiDefinitionOrder() {
      return editApiDefinitionOrder;
    },
    moduleOptionsNew() {
      let moduleOptions = [];
      this.moduleOptions.forEach((node) => {
        buildNodePath(node, {path: ''}, moduleOptions);
      });
      return moduleOptions;
    },
  },
  created: function () {
    if (!this.projectName || this.projectName === '') {
      this.getProjectName();
    }
    if (this.trashEnable) {
      this.tableOperatorButtons = this.tableTrashOperatorButtons;
      this.condition.filters = {status: ['Trash']};
    } else {
      this.tableOperatorButtons = this.tableUsualOperatorButtons;
      this.condition.filters = {status: ['Prepare', 'Underway', 'Completed']};
    }
    this.condition.orders = getLastTableSortField(this.tableHeaderKey);
    // 切换tab之后版本查询
    this.condition.versionId = this.currentVersion;
    let protocol;
    if (this.$route && this.$route.params && this.$route.params.type) {
      protocol = this.$route.params.type;
    }
    this.initTable(protocol);
    this.getMaintainerOptions();
    this.getVersionOptions();
    this.checkVersionEnable();
    this.getProtocolFilter();
    //为了跳转的时候把参数传递到模块
    this.$EventBus.$emit('apiConditionBus', this.condition);

    // 通知过来的数据跳转到编辑
    if (this.$route.query.resourceId) {
      getDefinitionById(this.$route.query.resourceId).then((response) => {
        this.editApi(response.data);
      });
    }
    for (let i = 0; i < this.condition.components.length; i++) {
      if (this.condition.components[i].custom) {
        this.condition.components.splice(i, 1);
        break;
      }
    }
    this.setAdvSearchParam();
    getApiTemplate(this.projectId).then((template) => {
      let comp = getAdvSearchCustomField(this.condition, template.customFields);
      this.condition.components.push(...comp)
    });
  },
  watch: {
    selectNodeIds() {
      initCondition(this.condition, false);
      this.currentPage = 1;
      this.condition.moduleIds = [];
      this.condition.moduleIds.push(this.selectNodeIds);
      this.closeCaseModel();
      this.initTable();
    },
    currentProtocol() {
      this.getProtocolFilter();
      this.currentPage = 1;
      initCondition(this.condition, false);
      this.closeCaseModel();
      this.initTable(true);
      this.visibleSearch = false;
      this.$nextTick(() => (this.visibleSearch = true));
      this.setAdvSearchParam();
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTable();
      // 选择了版本过滤，版本列上的checkbox也进行过滤
      this.getVersionOptions(this.currentVersion);
    },
    trashEnable() {
      if (this.trashEnable) {
        this.tableOperatorButtons = this.tableTrashOperatorButtons;
        this.condition.filters = {status: ['Trash']};
        this.condition.moduleIds = [];
      } else {
        this.tableOperatorButtons = this.tableUsualOperatorButtons;
        this.condition.filters = {
          status: ['Prepare', 'Underway', 'Completed'],
        };
      }
      initCondition(this.condition, false);
      this.closeCaseModel();
      this.initTable();
    },
  },
  methods: {
    setAdvSearchParam() {
      let comp = this.condition.components.find((c) => c.key === 'moduleIds');
      if (comp) {
        comp.options.params = {protocol: this.currentProtocol};
      }
      let method = this.condition.components.find((c) => c.key === 'method');
      if (method) {
        method.options = getProtocolFilter(this.currentProtocol);
      }
    },
    getProtocolFilter() {
      this.methodFilters = getProtocolFilter(this.currentProtocol);
    },
    getProjectName() {
      getProject(this.projectId).then((response) => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
        }
      });
    },
    generateGraph() {
      if (getSelectDataCounts(this.condition, this.total, this.$refs.table.selectRows) > 100) {
        this.$warning(this.$t('test_track.case.generate_dependencies_warning'));
        return;
      }
      getGraphByCondition('API', buildBatchParam(this, this.$refs.table.selectIds)).then((data) => {
        this.graphData = data.data;
        this.$refs.relationshipGraph.open();
      });
    },
    handleBatchMove() {
      this.isMoveBatch = true;
      this.$refs.testCaseBatchMove.open(this.moduleTree, [], this.moduleOptionsNew);
    },
    handleBatchCopy() {
      this.isMoveBatch = false;
      this.$refs.testCaseBatchMove.open(this.moduleTree, this.$refs.table.selectIds, this.moduleOptionsNew);
    },
    batchCopyDataFromVersion() {
      if (this.$refs.versionSelector) {
        this.$refs.versionSelector.open(this.projectId);
      }
    },
    closeCaseModel() {
      //关闭案例弹窗
      if (this.$refs.caseList) {
        this.$refs.caseList.handleClose();
      }
    },
    initTableCondition(currentProtocol) {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
      initCondition(this.condition, this.condition.selectAll);
      this.selectDataCounts = 0;
      this.condition.moduleIds = this.selectNodeIds;
      this.condition.projectId = this.projectId;
      if (currentProtocol && ["HTTP", "DUBBO", "SQL", "TCP"].includes(currentProtocol)) {
        this.condition.protocol = currentProtocol;
      } else if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }
      this.enableOrderDrag = !(this.condition.orders && this.condition.orders.length > 0);

      //检查是否只查询本周数据
      this.getSelectDataRange();
      this.condition.selectThisWeedData = false;
      this.condition.apiCaseCoverage = null;
      this.condition.apiCoverage = null;
      this.condition.scenarioCoverage = null;
      switch (this.selectDataRange) {
        case 'thisWeekCount':
          this.condition.selectThisWeedData = true;
          break;
        case 'notCovered':
          this.condition.apiCoverage = 'unCovered';
          break;
        case 'covered':
          this.condition.apiCoverage = 'covered';
          break;
        case 'notCoveredTestCase':
          this.condition.apiCaseCoverage = 'unCovered';
          break;
        case 'coveredTestCase':
          this.condition.apiCaseCoverage = 'covered';
          break;
        case 'coveredScenario':
          this.condition.scenarioCoverage = 'covered';
          break;
        case 'notCoveredScenario':
          this.condition.scenarioCoverage = 'unCovered';
          break;
        case 'Prepare':
          this.condition.filters.status = [this.selectDataRange];
          break;
        case 'Completed':
          this.condition.filters.status = [this.selectDataRange];
          break;
        case 'Underway':
          this.condition.filters.status = [this.selectDataRange];
          break;
      }
      if (currentProtocol) {
        this.condition.moduleIds = [];
      }
      if (!this.condition.filters.status) {
        this.condition.filters = {
          status: ['Prepare', 'Underway', 'Completed'],
        };
      }
    },
    historyData() {
      this.tableData.forEach((item) => {
        if (item.tags && item.tags.length > 0) {
          item.tags = JSON.parse(item.tags);
        }
        item.caseTotal = parseInt(item.caseTotal);
        if (item.protocol === 'HTTP') {
          if (!item.response || item.response === 'null') {
            let response = {
              headers: [],
              body: new Body(),
              statusCode: [],
              type: 'HTTP',
            };
            item.response = JSON.stringify(response);
          }
          if (item.response) {
            item.response = JSON.parse(item.response);
            if (!item.response.body) {
              item.response.body = new Body();
            }
            if (!item.response.headers) {
              item.response.headers = [];
            }
            if (!item.response.statusCode) {
              item.response.statusCode = [];
            }
            item.response = JSON.stringify(item.response);
          }
          //request
          if (item.request) {
            item.request = JSON.parse(item.request);
            if (!item.request.body) {
              item.request.body = new Body();
            }
            if (!item.request.body.type) {
              this.$set(item.request.body, 'type', null);
            }
            if (!item.request.headers) {
              item.request.headers = [];
            }
            if (!item.request.body.kvs) {
              item.request.body.kvs = [];
            }
            item.request.body.kvs.forEach((i) => {
              if (!i.files) {
                i.files = [];
              }
            });

            if (!item.request.rest) {
              item.request.rest = [];
            }
            if (!item.request.arguments) {
              item.request.arguments = [
                {
                  contentType: 'text/plain',
                  enable: true,
                  file: false,
                  required: false,
                  type: 'text',
                  urlEncode: false,
                  valid: false,
                },
              ];
            }
            item.request = JSON.stringify(item.request);
          }
        }
      });
    },
    refreshTable() {
      this.initTableCondition();
      if (this.condition.projectId) {
        this.result = getDefinitionPage(this.currentPage, this.pageSize, this.condition).then((response) => {
          getProtocolFilter(this.condition.protocol);
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.historyData();
          if (this.$refs.table) {
            this.$refs.table.clearSelection();
          }
        });
      }
    },
    initTable(currentProtocol) {
      this.initTableCondition(currentProtocol);
      if (this.condition.projectId) {
        this.result = getDefinitionPage(this.currentPage, this.pageSize, this.condition).then((response) => {
          getProtocolFilter(this.condition.protocol);
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.historyData();
          this.$emit('getTrashApi');
          if (this.$refs.table) {
            this.$refs.table.clearSelection();
          }
        });
      }
      if (this.needRefreshModule()) {
        if (this.isFirstInitTable) {
          this.isFirstInitTable = false;
        } else {
          this.$emit('refreshTree');
        }
      }
    },
    getMaintainerOptions() {
      getMaintainer().then((response) => {
        this.valueArr.userId = response.data;
        this.userFilters = response.data.map((u) => {
          return {text: u.name, value: u.id};
        });
      });
    },
    getVersionOptions(currentVersion) {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then((response) => {
          if (currentVersion) {
            this.versionFilters = response.data
              .filter((u) => u.id === currentVersion)
              .map((u) => {
                return {text: u.name, value: u.id};
              });
          } else {
            this.versionFilters = response.data.map((u) => {
              return {text: u.name, value: u.id};
            });
          }
        });
      }
    },
    enterSearch() {
      this.$refs.inputVal.blur();
      this.search();
    },
    search() {
      this.$EventBus.$emit('apiConditionBus', this.condition);
      this.changeSelectDataRangeAll();
      this.initTable();
    },
    buildPagePath(path) {
      return path + '/' + this.currentPage + '/' + this.pageSize;
    },

    editApi(row) {
      this.$emit('editApiModule', row);
    },
    handleCopy(row) {
      let obj = JSON.parse(JSON.stringify(row));
      obj.isCopy = true;
      obj.sourceId = row.id;
      delete obj.id;
      this.$emit('copyApi', obj);
    },
    runApi(row) {
      let request;
      if (typeof row.request === 'string') {
        request = row ? JSON.parse(row.request) : {};
      } else {
        request = row ? row.request : {};
      }
      let response = '';
      if (row.response != null && row.response != 'null' && row.response != undefined) {
        if (
          Object.prototype.toString
            .call(row.response)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
          response = row.response;
        } else {
          try {
            response = JSON.parse(row.response);
          } catch (e) {
            response = {};
          }
        }
      } else {
        response = {
          headers: [],
          body: new Body(),
          statusCode: [],
          type: 'HTTP',
        };
      }
      if (response.body) {
        let body = new Body();
        Object.assign(body, response.body);
        if (!body.binary) {
          body.binary = [];
        }
        if (!body.kvs) {
          body.kvs = [];
        }
        if (!body.binary) {
          body.binary = [];
        }
        response.body = body;
      }
      row.request = request;
      row.response = response;
      this.$emit('runTest', row);
    },
    reductionApi(row) {
      let tmp = JSON.parse(JSON.stringify(row));
      let rows = {ids: [tmp.id]};
      rows.projectId = getCurrentProjectID();
      rows.protocol = this.currentProtocol;
      definitionReduction(rows).then(() => {
        this.$success(this.$t('commons.save_success'));
        // this.search();
        this.$emit('refreshTable');
      });
    },
    handleBatchRestore() {
      let batchParam = buildBatchParam(this, this.$refs.table.selectIds);
      batchParam.protocol = this.currentProtocol;
      definitionReduction(batchParam).then(() => {
        this.$success(this.$t('commons.save_success'));
        // this.search();
        this.$emit('refreshTable');
      });
    },
    handleDeleteBatch() {
      if (this.trashEnable) {
        operationConfirm(this, this.$t('api_test.definition.request.delete_confirm') + '？', () => {
          deleteBatchByParams(buildBatchParam(this, this.$refs.table.selectIds)).then(() => {
            this.$refs.table.clear();
            // this.initTable();
            this.$emit('refreshTable');
            this.$success(this.$t('commons.delete_success'));
          });
        });
      } else {
        operationConfirm(this, this.$t('api_test.definition.request.delete_confirm') + '？', () => {
          removeToGcByParams(buildBatchParam(this, this.$refs.table.selectIds)).then(() => {
            this.$refs.table.clear();
            this.$emit('refreshTable');
            this.$success(this.$t('commons.delete_success'));
            this.$refs.caseList.apiCaseClose();
          });
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
      let param = buildBatchParam(this, this.$refs.table.selectIds);
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
    moveSave(param) {
      let ids = this.$refs.table.selectIds;
      param.ids = ids;
      param.projectId = this.projectId;
      param.condition = this.condition;
      param.moduleId = param.nodeId;
      param.modulePath = param.nodePath;
      if (!this.isMoveBatch) {
        batchCopyByParams(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$refs.testCaseBatchMove.close();
          this.initTable();
        });
      } else {
        batchEditByParams(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$refs.testCaseBatchMove.close();
          this.initTable();
        });
      }
    },
    handleTestCase(api) {
      this.$emit('handleTestCase', api);
      // this.$refs.caseList.open(this.selectApi);
    },
    showCaseRef(row) {
      let param = {};
      Object.assign(param, row);
      param.moduleId = undefined;
      this.$refs.viewRef.open(param, 'API');
    },
    handleDelete(api) {
      if (this.trashEnable) {
        delDefinition(api.id).then(() => {
          this.$success(this.$t('commons.delete_success'));
          // this.initTable();
          this.$emit('refreshTable');
        });
        return;
      }
      // 检查api有几个版本
      getDefinitionVersions(api.id).then((response) => {
        if (hasLicense() && this.versionEnable && response.data.length > 1) {
          // 删除提供列表删除和全部版本删除
          this.$refs.apiDeleteConfirm.open(api, this.$t('api_test.definition.request.delete_confirm'));
        } else {
          operationConfirm(this, this.$t('api_test.definition.request.delete_confirm') + ' ' + api.name, () => {
            this._handleDelete(api, false);
          });
        }
      });
    },
    _handleDelete(api, deleteCurrentVersion) {
      // 删除指定版本
      if (deleteCurrentVersion) {
        delDefinitionByRefId(api.versionId, api.refId).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.$refs.apiDeleteConfirm.close();
          this.$emit('refreshTable');
        });
      }
      // 删除全部版本
      else {
        let ids = [api.id];
        removeToGcByIds(ids).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.$refs.apiDeleteConfirm.close();
          this.$emit('refreshTable');
          this.$refs.caseList.apiCaseClose();
        });
      }
    },
    getColor(enable, method) {
      if (enable) {
        return this.methodColorMap.get(method);
      }
    },
    showExecResult(row) {
      this.$emit('showExecResult', row);
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    getSelectDataRange() {
      let dataRange = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      let redirectVersionId = this.$route.params.versionId;
      if (redirectVersionId && redirectVersionId !== 'default') {
        this.condition.versionId = redirectVersionId;
      }
      this.selectDataRange = dataType === 'api' ? dataRange : 'all';
      if (
        this.selectDataRange &&
        Object.prototype.toString
          .call(this.selectDataRange)
          .match(/\[object (\w+)\]/)[1]
          .toLowerCase() !== 'object' &&
        this.selectDataRange.indexOf(':') !== -1
      ) {
        let selectParamArr = this.selectDataRange.split(':');
        if (selectParamArr.length === 2) {
          if (selectParamArr[0] === 'apiList') {
            this.condition.name = selectParamArr[1];
          }
        }
      }
    },
    changeSelectDataRangeAll() {
      this.$emit('changeSelectDataRangeAll', 'api');
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets);
      let ids = rowArray.map((s) => s.id);
      return ids;
    },
    exportApi(type, nodeTree) {
      let param = buildBatchParam(this, this.$refs.table.selectIds);
      param.protocol = this.currentProtocol;
      if (param.ids === undefined || param.ids.length < 1) {
        this.$warning(this.$t('api_test.definition.check_select'));
        return;
      }
      this.result = exportDefinition(type, param).then((response) => {
        let obj = response.data;
        if (type == 'MS') {
          obj.protocol = this.currentProtocol;
          obj.nodeTree = this.getExportNodeTree(nodeTree, obj.data);
          downloadFile('Metersphere_Api_' + this.projectName + '.json', JSON.stringify(obj));
        } else {
          downloadFile('Swagger_Api_' + this.projectName + '.json', JSON.stringify(obj));
        }
      });
    },
    getExportNodeTree(nodeTree, apis) {
      let idSet = new Set();
      apis.forEach((item) => {
        idSet.add(item.moduleId);
      });
      let exportTree = deepClone(nodeTree);
      for (let i = exportTree.length - 1; i >= 0; i--) {
        if (!this.cutDownTree(exportTree[i], idSet)) {
          exportTree.splice(i, 1);
        }
      }
      return exportTree;
    },
    // 去掉没有数据的模块再导出
    cutDownTree(nodeTree, nodeIdSet) {
      let hasData = false;
      if (nodeIdSet.has(nodeTree.id)) {
        hasData = true;
      }
      let children = nodeTree.children;
      if (children) {
        for (let i = children.length - 1; i >= 0; i--) {
          if (!this.cutDownTree(children[i], nodeIdSet)) {
            children.splice(i, 1);
          } else {
            hasData = true;
          }
        }
      }
      return hasData;
    },
    headerDragend(newWidth, oldWidth, column, event) {
      let finalWidth = newWidth;
      if (column.minWidth > finalWidth) {
        finalWidth = column.minWidth;
      }
      column.width = finalWidth;
      column.realWidth = finalWidth;
    },
    open() {
      this.$refs.searchBar.open();
    },
    needRefreshModule() {
      if (this.initApiTableOpretion === '0') {
        return true;
      } else {
        this.$emit('updateInitApiTableOpretion', '0');
        return false;
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
    //从指定版本拷贝数据
    handleCopyDataFromVersion(selectVersionId, copyCase, copyMock) {
      let copyParam = {};
      // let param = {};
      // if (vueObj.selectRows) {
      //   param.ids = selectIds ? selectIds : Array.from(vueObj.selectRows).map(row => row.id);
      // } else {
      //   param.ids = selectIds;
      // }
      // param.projectId = projectId ? projectId : getCurrentProjectID();
      // param.condition = vueObj.condition;
      // return param;

      // copyParam.versionId = selectVersionId;
      copyParam.copyCase = copyCase;
      copyParam.copyMock = copyMock;
      copyParam.versionId = selectVersionId;
      copyParam.condition = this.condition;
      copyParam.condition.ids = this.$refs.table.selectIds;
      copyParam.condition.protocol = this.currentProtocol;

      copyDataByVersion(copyParam).then(() => {
        this.$success(this.$t('commons.copy_success'));
        if (this.$refs.versionSelector) {
          this.$refs.versionSelector.handleClose();
        }
        this.initTable();
      });
    },
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
  color: #1e90ff;
}

.api-el-tag {
  color: white;
}

.search-input {
  float: right;
  width: 300px;
}

.el-tag {
  margin-left: 10px;
}

.ms-select-all :deep(th:first-child) {
  margin-top: 20px;
}

.ms-select-all :deep(th:nth-child(2) .el-icon-arrow-down) {
  top: -2px;
}

:deep(.el-table__empty-block) {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  padding-right: 100%;
}

.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
