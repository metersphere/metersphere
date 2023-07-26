<template>

  <div class="case-main-layout">

    <div class="case-main-layout-left" style="float: left; display: inline-block">
      <!-- 表头统计内容  -->
      <ms-table-count-bar :count-content="$t('case.all_case_content') + ' (' + page.total + ')'"></ms-table-count-bar>
    </div>

    <div class="case-main-layout-right" style="float: right; display: flex">
      <!-- 简单搜索框 -->
      <ms-new-ui-search :condition.sync="condition" @search="search" style="float: left" />

      <!-- 版本切换组件 -->
      <version-select v-xpack :project-id="projectId" :default-version="defaultVersion" @changeVersion="changeVersion" />

      <!-- 高级搜索框  -->
      <ms-table-adv-search :condition.sync="condition" @search="search" ref="advanceSearch"/>

      <!-- 表头自定义显示Popover  -->
      <ms-table-header-custom-popover :fields.sync="fields" :custom-fields="testCaseTemplate.customFields"
                                      :field-key="tableHeaderKey" :drag-key="tableHeaderDragKey" @reload="reloadTable" />
    </div>

    <!-- table -->
    <ms-table
      v-loading="loading"
      operator-width="180px"
      row-key="id"
      :data="page.data"
      :condition="condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :operators="operators"
      :screen-height="screenHeight"
      :max-height="maxHeight"
      :batch-operators="batchButtons"
      :remember-order="true"
      :enable-header-drag="true"
      :enable-order-drag="enableOrderDrag"
      :row-order-group-id="projectId"
      :row-order-func="editTestCaseOrder"
      :fields.sync="fields"
      :disable-header-config="true"
      :field-key="tableHeaderKey"
      :custom-fields="testCaseTemplate.customFields"
      :highlight-current-row="true"
      :refresh-by-search.sync="refreshBySearch"
      @handlePageChange="initTableData"
      @order="initTableData"
      @filter="search"
      @callBackSelect="callBackSelect"
      @callBackSelectAll="callBackSelectAll"
      @clearTableSelect="clearTableSelect"
      ref="table">

      <ms-table-column
        prop="deleteTime"
        sortable="custom"
        v-if="this.trashEnable"
        :fields-width="fieldsWidth"
        :label="$t('commons.delete_time')"
        min-width="150px">
        <template v-slot:default="scope">
          <span>{{ scope.row.deleteTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="deleteUserId"
        :fields-width="fieldsWidth"
        v-if="this.trashEnable"
        :label="$t('commons.delete_user')"
        min-width="120"/>

      <span v-for="(item, index) in fields" :key="index">

        <ms-table-column
          v-if="!customNum"
          :field="item"
          :fields-width="fieldsWidth"
          :column-key="'num'"
          :prop="'num'"
          sortable="custom"
          :label="$t('commons.id')"
          min-width="80">
          <template v-slot:default="scope">
            <a style="cursor:pointer"> {{ scope.row.num }} </a>
          </template>
        </ms-table-column>

        <ms-table-column
          v-if="item.id === 'num' && customNum"
          :fields-width="fieldsWidth"
          :column-key="'customNum'"
          prop="customNum"
          sortable="custom"
          :label="$t('commons.id')"
          min-width="80">
          <template v-slot:default="scope">
            <a style="cursor:pointer"> {{ scope.row.customNum }} </a>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="name"
          sortable="custom"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.case.name')"
          min-width="120">
          <template v-slot:default="scope">
            <a style="cursor:pointer"> {{ scope.row.name }} </a>
          </template>
        </ms-table-column>

        <ms-table-column :label="$t('test_track.case.case_desc')" prop="desc" :field="item" min-width="100px">
          <template v-slot:default="scope">
            <el-link @click.stop="getCase(scope.row.id)" style="color:#783887;">{{ $t('commons.preview') }}</el-link>
          </template>
        </ms-table-column>

        <ms-table-column
          sortable="custom"
          prop="createUser"
          min-width="120"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.create_user')"
          :filters="userFilter">
           <template v-slot:default="scope">
            {{ getCreateUserName(scope.row.createUser) }}
          </template>
        </ms-table-column>

        <test-case-review-status-table-item
          :min-width="130"
          :field="item"
          :fields-width="fieldsWidth"/>

        <test-plan-case-status-table-item
          prop="lastExecuteResult"
          :min-width="130"
          :field="item"
          :fields-width="fieldsWidth"/>

        <ms-table-column
          prop="tags"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="180"
          :show-overflow-tooltip="false"
          :label="$t('commons.tag')">
          <template v-slot:default="scope">
              <el-tooltip class="item" effect="dark" placement="top">
                <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
                <div class="oneLine">
                  <ms-single-tag
                    v-for="(itemName, index) in parseColumnTag(scope.row.tags)"
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
          v-if="enableVersionColumn"
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
          prop="nodePath"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.case.module')"
          min-width="150px">
          <template v-slot:default="scope">
            <span>{{ nodePathMap.get(scope.row.nodeId) }}</span>
          </template>
        </ms-table-column>

        <ms-update-time-column :field="item"
                               :fields-width="fieldsWidth"/>

        <ms-create-time-column :field="item"
                               :fields-width="fieldsWidth"/>

        <ms-table-column v-for="field in testCaseTemplate.customFields" :key="field.id"
                         :filters="getCustomFieldFilter(field)"
                         :field="item"
                         :fields-width="fieldsWidth"
                         :label="field.system ? $t(systemFiledMap[field.name]) :field.name"
                         :min-width="120"
                         :column-key="field.columnKey ? field.columnKey : generateColumnKey(field)"
                         :prop="field.name">
          <template v-slot="scope">
            <span v-if="field.name === '用例等级'">
                <priority-table-item
                  :value="getCustomFieldValue(scope.row, field, scope.row.priority)" :priority-options="priorityOptions"/>
            </span>
            <span v-else-if="field.name === '用例状态'">
              <case-status-table-item :value="getCustomFieldValue(scope.row, field, scope.row.status)"></case-status-table-item>
            </span>
            <span v-else>
              {{ getCustomFieldValue(scope.row, field) }}
            </span>
          </template>
        </ms-table-column>
      </span>

    </ms-table>

    <!-- 批量操作按钮  -->
    <ms-table-batch-operator-group v-if="selectCounts > 0" :batch-operators="batchButtons" :select-counts="selectCounts" @clear="clearTableSelect"/>

    <!-- 分页组件 -->
    <home-pagination v-if="page.data.length > 0 && selectCounts === 0" :change="initTableData" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                     :total="page.total" layout="total, prev, pager, next, sizes, jumper" style="margin: 26px 0 9px 0"/>

    <!-- dialog -->
    <batch-edit ref="batchEdit" @batchEdit="batchEdit"
                :typeArr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')"/>

    <batch-move @refresh="refresh" @moveSave="moveSave" ref="testBatchMove"/>

    <relate-demand ref="relateDemand" @batchRelate="_batchRelateDemand"/>

    <test-case-preview ref="testCasePreview" :loading="rowCaseResult.loading"/>

    <relationship-graph-drawer :graph-data="graphData" ref="relationshipGraph"/>

    <!--  删除接口提示  -->
    <list-item-delete-confirm ref="apiDeleteConfirm" @handleDelete="_handleDeleteVersion"/>

    <test-case-export-to-excel @exportTestCase="exportTestCase" ref="exportExcel" class="export-case-layout"/>

  </div>

</template>

<script>
import MsTableBatchOperatorGroup from "metersphere-frontend/src/components/new-ui/MsTableBatchOperatorGroup";
import MsTableAdvSearch from "metersphere-frontend/src/components/new-ui/MsTableAdvSearch";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import MsTableHeaderCustomPopover from 'metersphere-frontend/src/components/new-ui/MsTableHeaderCustomPopover'
import CaseStatusTableItem from "@/business/common/tableItems/planview/CaseStatusTableItem";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import ReviewStatus from "@/business/case/components/ReviewStatus";
import TestCaseImport from './import/TestCaseImport';
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import HomePagination from '@/business/home/components/pagination/HomePagination';
import MsTableCountBar from 'metersphere-frontend/src/components/table/MsTableCountBar';
import PriorityTableItem from "../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../common/tableItems/planview/TypeTableItem";
import {TEST_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import BatchEdit from "./BatchEdit";
import {TEST_CASE_LIST} from "metersphere-frontend/src/utils/constants";
import MsSingleTag from "metersphere-frontend/src/components/new-ui/MsSingleTag";
import {
  buildBatchParam,
  getCustomFieldBatchEditOption, getCustomFieldFilter,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  getPageInfo,
  getSelectDataCounts,
  getTableHeaderWithCustomFields,
  initCondition, parseCustomFilesForList,
} from "metersphere-frontend/src/utils/tableUtils";
import PlanStatusTableItem from "@/business/common/tableItems/plan/PlanStatusTableItem";
import {getCurrentProjectID, getCurrentWorkspaceId, setCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {parseTag} from "metersphere-frontend/src/utils"
import {hasLicense} from "metersphere-frontend/src/utils/permission"
import {getTestTemplateForList} from "@/api/custom-field-template";
import {getProjectMember, getProjectMemberUserFilter} from "@/api/user";
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import BatchMove from "@/business/case/components/BatchMove";
import {SYSTEM_FIELD_NAME_MAP} from "metersphere-frontend/src/utils/table-constants";
import TestCasePreview from "@/business/case/components/TestCasePreview";
import {
  deleteTestCaseVersion,
  editTestCaseOrder,
  getTestCase,
  getTestCaseStep,
  getTestCaseVersions, testCaseBatchCopy,
  testCaseBatchDelete,
  testCaseBatchDeleteToGc, testCaseBatchEdit, testCaseBatchRelateDemand,
  testCaseDelete,
  testCaseDeleteToGc, testCaseList,
  testCaseReduction
} from "@/api/testCase";
import {getGraphByCondition} from "@/api/graph";
import ListItemDeleteConfirm from "metersphere-frontend/src/components/ListItemDeleteConfirm";
import RelationshipGraphDrawer from "metersphere-frontend/src/components/graph/RelationshipGraphDrawer";
import MsNewUiSearch from "metersphere-frontend/src/components/new-ui/MsSearch";
import {mapState} from "pinia";
import {useStore} from "@/store"
import {getProject, versionEnableByProjectId} from "@/api/project";
import {getProjectApplicationConfig} from "@/api/project-application";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import TestCaseReviewStatusTableItem from "@/business/common/tableItems/TestCaseReviewStatusTableItem";
import RelateDemand from "@/business/case/components/RelateDemand";
import TestPlanCaseStatusTableItem from "@/business/common/tableItems/TestPlanCaseStatusTableItem";
import {
  fileDownloadPost,
  generateColumnKey,
  getCustomFieldValueForTrack,
  getProjectMemberOption
} from "@/business/utils/sdk-utils";
import {getTagToolTips, initTestCaseConditionComponents, openCaseEdit, parseColumnTag} from "@/business/case/test-case";
import TestCaseExportToExcel from "@/business/case/components/export/TestCaseExportToExcel";

const store = useStore();

export default {
  name: "TestCaseList",
  components: {
    TestCaseExportToExcel,
    TestPlanCaseStatusTableItem, RelateDemand, TestCaseReviewStatusTableItem,
    MsCreateTimeColumn, MsUpdateTimeColumn, MsNewUiSearch,
    ListItemDeleteConfirm,
    TestCasePreview,
    BatchMove,
    MsTableColumn,
    MsTable,
    PlanStatusTableItem,
    TypeTableItem,
    PriorityTableItem,
    TestCaseImport,
    MsTablePagination,
    HomePagination,
    BatchEdit,
    MsSingleTag,
    RelationshipGraphDrawer,
    MsTableCountBar,
    ReviewStatus,
    StatusTableItem,
    CaseStatusTableItem,
    MsTableHeaderCustomPopover,
    'VersionSelect': MxVersionSelect,
    MsTableAdvSearch,
    MsTableBatchOperatorGroup
  },
  data() {
    return {
      currentVersion: null,
      addPublic: false,
      projectName: "",
      type: TEST_CASE_LIST,
      tableHeaderKey: "TRACK_TEST_CASE",
      tableHeaderDragKey: "TRACK_TEST_CASE_DRAG",
      screenHeight: 'calc(100vh - 185px)',
      maxHeight: 'calc(100vh - 285px)',
      enableOrderDrag: true,
      isMoveBatch: true,
      loading: false,
      condition: {
        components: TEST_CASE_CONFIGS,
        filters: {},
        custom: false,
      },
      graphData: {},
      batchButtons: [],
      simpleButtons: [
        {
          name: this.$t('test_track.case.batch_edit_btn'),
          handleClick: this.handleBatchEdit,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_EDIT']
        },
        {
          name: this.$t('test_track.case.batch_move_btn'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_MOVE']
        },
        {
          name: this.$t('test_track.case.batch_copy_btn'),
          handleClick: this.handleBatchCopy,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_COPY']
        },
        {
          name: this.$t('test_track.case.batch_link_demand_btn'),
          handleClick: this.openRelateDemand,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_LINK_DEMAND']
        },
        {
          name: this.$t('test_track.case.generate_dependencies'),
          handleClick: this.generateGraph,
          permissions: ['PROJECT_TRACK_CASE:READ+GENERATE_DEPENDENCIES']
        },
        {
          name: this.$t('test_track.case.batch_add_public_btn'),
          isXPack: true,
          handleClick: this.handleBatchAddPublic,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_ADD_PUBLIC'],
        },
        {
          name: this.$t('test_track.case.import.case_export'),
          permissions: ['PROJECT_TRACK_CASE:READ+EXPORT'],
          children: [
            {
              name: this.$t('test_track.case.export.export_to_excel'),
              tips: this.$t('test_track.case.export.export_to_excel_tips'),
              handleClick: this.handleBatchExportToExcel,
              permissions: ['PROJECT_TRACK_CASE:READ+EXPORT'],
            },
            {
              name: this.$t('test_track.case.export.export_to_xmind'),
              tips: this.$t('test_track.case.export.export_to_xmind_tips'),
              handleClick: this.handleBatchExportToXmind,
              permissions: ['PROJECT_TRACK_CASE:READ+EXPORT'],
            }
          ]
        },
        {
          name: this.$t('test_track.case.batch_delete_btn'),
          handleClick: this.handleDeleteBatchToGc,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE'],
          isDivide: true,
          isActive: true
        }
      ],
      trashButtons: [
        {
          name: this.$t('commons.recover'),
          handleClick: this.batchReduction,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_REDUCTION']
        }, {
          name: this.$t('test_track.case.batch_delete_case'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_DELETE'],
          isDelete: true
        }
      ],
      operators: [],
      simpleOperators: [
        {
          tip: this.$t('commons.view'),
          isTextButton: true,
          exec: this.handleEdit,
          permissions: ['PROJECT_TRACK_CASE:READ']
        },
        {
          tip: this.$t('commons.copy'),
          isTextButton: true,
          exec: this.handleCopy,
          permissions: ['PROJECT_TRACK_CASE:READ+COPY']
        },
        {
          tip: this.$t('commons.delete'),
          isTextButton: true,
          exec: this.handleDeleteToGc,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE']
        }
      ],
      trashOperators: [
        {
          tip: this.$t('commons.recover'),
          isTextButton: true,
          exec: this.reduction,
          permissions: ['PROJECT_TRACK_CASE:READ+RECOVER']
        },
        {
          isTextButton: true,
          tip: this.$t('test_track.case.batch_delete_case'),
          exec: this.handleDelete,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_DELETE']
        }
      ],
      typeArr: [],
      valueArr: {},
      selectDataRange: "all",
      testCaseTemplate: {},
      members: [],
      page: getPageInfo(),
      fields: getCustomTableHeader('TRACK_TEST_CASE'),
      fieldsWidth: getCustomTableWidth('TRACK_TEST_CASE'),
      memberMap: new Map(),
      rowCase: {},
      rowCaseResult: {loading: false},
      userFilter: [],
      advanceSearchShow: false,
      selectCounts: 0,
      refreshBySearch: false,
      enableVersionColumn: false,
      projectId: '',
      priorityOptions: []
    };
  },
  props: {
    treeNodes: {
      type: Array
    },
    trashEnable: {
      type: Boolean,
      default: false,
    },
    versionEnable: {
      type: Boolean,
      default: false
    },
    defaultVersion: String
  },
  computed: {
    routeProjectId() {
      return this.$route.query.projectId;
    },
    systemFiledMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    editTestCaseOrder() {
      return editTestCaseOrder;
    },
    ...mapState(useStore, {
      selectNodeIds: 'testCaseSelectNodeIds',
      selectNode: 'testCaseSelectNode',
      moduleOptions: 'testCaseModuleOptions',
      customNum: 'currentProjectIsCustomNum'
    }),
    nodePathMap() {
      let map = new Map();
      if (this.moduleOptions) {
        this.moduleOptions.forEach((item) => {
          map.set(item.id, item.path);
        });
      }
      return map;
    }
  },
  created: function () {
    this.currentVersion = this.defaultVersion || null;
    this.checkCurrentProject();

    getProjectMemberUserFilter((data) => {
      this.userFilter = data;
    });
    this.getTemplateField();
    this.$emit('setCondition', this.condition);
    this.initTableData();
    let redirectParam = this.$route.query.dataSelectRange;
    this.checkRedirectEditPage(redirectParam);
    // 切换tab之后版本查询
    this.condition.versionId = this.currentVersion;
    if (this.trashEnable) {
      this.operators = this.trashOperators;
      this.batchButtons = this.trashButtons;
    } else {
      this.operators = this.simpleOperators;
      this.batchButtons = this.simpleButtons;
    }
    if (!this.projectName || this.projectName === "") {
      this.getProjectName();
    }

    // 通知过来的数据跳转到编辑
    if (this.$route.query.resourceId) {
      this.$get('test/case/get/' + this.$route.query.resourceId, response => {
        let testCase = response.data;
        testCase.label = 'redirect';
        this.$emit('testCaseEdit', testCase);
      });
    }
  },
  watch: {
    '$route'(to) {
      if (to.path.indexOf("/track/case/all") >= 0) {
        if (to.query.moduleId) {
          // 点击模块导致路由变更不刷新，避免刷新两次
          return;
        }
        this.getProject();
        this.getTemplateField();
        let ids = this.$route.params.ids;
        if (ids) {
          this.condition.ids = ids;
        }
        let dataSelectRange = this.$route.params.dataSelectRange;
        if (!dataSelectRange) {
          delete this.condition.filters.review_status
        }
        this.initTableData();
        this.condition.ids = null;
      }
    },
    selectNodeIds() {
      this.clearTableSelect();
      this.page.currentPage = 1;
      initCondition(this.condition, false);
      this.initTableData();
    },
    condition() {
      this.$emit('setCondition', this.condition);
    },
    trashEnable() {
      if (this.trashEnable) {
        //更改表格按钮
        this.operators = this.trashOperators;
        this.batchButtons = this.trashButtons;
        //更改查询条件
        this.condition.filters = {status: ["Trash"]};
        this.condition.moduleIds = [];
        initCondition(this.condition, false);
        this.initTableData();
      } else {
        //更改各种按钮
        this.operators = this.simpleOperators;
        this.batchButtons = this.simpleButtons;
        this.condition.filters.status = [];
      }
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTableData();
    },
  },
  methods: {
    async getTemplateField() {
      this.loading = true;
      // 防止第一次渲染版本字段展示顺序错乱
      await this.checkVersionEnable();
      let p1 = getProjectMember()
        .then((response) => {
          this.members = response.data;
          this.members.forEach(item => {
            this.memberMap.set(item.id, item.name);
          });
        });
      let p2 = getTestTemplateForList();
      Promise.all([p1, p2]).then((data) => {
        this.loading = false;
        let template = data[1];
        this.testCaseTemplate = template;
        this.fields = getTableHeaderWithCustomFields(this.tableHeaderKey, this.testCaseTemplate.customFields, this.members);
        this.initConditionComponents()
        this.setTestCaseDefaultValue(template);
        this.typeArr = [];
        this.typeArr.push({
          id: "tags",
          name: this.$t('commons.tag')
        })
        getCustomFieldBatchEditOption(template.customFields, this.typeArr, this.valueArr, this.members);
        this.$nextTick(() => {
          if (this.$refs.table) {
            this.$refs.table.resetHeader(() => {
              this.loading = false;
            });
          } else {
            this.loading = false;
          }
        });
      });
    },
    checkCurrentProject() {
      // 带了 routeProjectId 校验是否是当前项目
      if (this.routeProjectId) {
        if (getCurrentProjectID() !== this.routeProjectId) {
          setCurrentProjectID(this.routeProjectId);
          location.reload();
          return;
        } else {
          // 切换项目，url会重写为新的项目ID，也走这里
          this.projectId = this.routeProjectId;
        }
      } else {
        // 没带 routeProjectId 则使用当前项目
        this.projectId = getCurrentProjectID();
      }
    },
    getTagToolTips(tags) {
      return getTagToolTips(tags);
    },
    parseColumnTag(tags) {
      return parseColumnTag(tags);
    },
    initConditionComponents() {
      this.condition.components = initTestCaseConditionComponents(this.condition, this.testCaseTemplate.customFields, this.trashEnable);
    },
    setTestCaseDefaultValue(template) {
      let testCaseDefaultValue = {};
      template.customFields.forEach(item => {
        if (item.system) {
          if (item.defaultValue) {
            testCaseDefaultValue[item.name] = JSON.parse(item.defaultValue);
          } else {
            testCaseDefaultValue[item.name] = "";
          }
        }
        if (item.name === '用例等级') {
          item.columnKey = 'priority';
          this.priorityOptions = item.options;
        } else if (item.name === '责任人') {
          item.columnKey = 'maintainer';
        } else if (item.name === '用例状态') {
          item.columnKey = 'status';
        }
      });
      store.testCaseDefaultValue = testCaseDefaultValue;
    },
    getCreateUserName(userId) {
      let user = this.userFilter.filter(item => item.value === userId);
      return user.length > 0 ? user[0].text : "";
    },
    getCustomFieldValue(row, field, defaultVal = '') {
      let value = getCustomFieldValueForTrack(row, field, this.members);
      if (field.name === '用例等级') {
        return row.priority;
      } else if (field.name === '责任人') {
        return row.maintainerName;
      } else if (field.name === '用例状态') {
        value = value === 'Trash' ? this.$t('test_track.plan.plan_status_trash') : value
      }
      return value ? value : defaultVal;
    },
    getCustomFieldFilter(field) {
      if (field.name === '用例状态') {
        let option = null;
        if (!this.trashEnable) {
          option = [];
          field.options.forEach((item) => {
            option.push({
              text: this.$t(item.text),
              value: item.value
            })
          });
        }
        return option;
      }
      return getCustomFieldFilter(field, this.userFilter);
    },
    checkRedirectEditPage(redirectParam) {
      if (redirectParam != null) {
        getTestCase(redirectParam)
          .then(r => {
            let testCase = r.data;
            testCase.label = "redirect";
            this.$emit('testCaseEdit', testCase);
          });
      }
    },
    getProjectName() {
      getProject(this.projectId)
        .then(r => {
          let project = r.data;
          if (project) {
            this.projectName = project.name;
          }
        });
    },
    getSelectDataRange() {
      this.selectDataRange = 'all';
      let dataRange = this.$route.params.dataSelectRange;
      this.selectDataRange = dataRange;
    },
    initTableData(nodeIds) {
      this.condition.planId = "";
      this.condition.nodeIds = [];
      initCondition(this.condition, this.condition.selectAll);
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
      this.condition.versionId = this.currentVersion || null;
      this.enableOrderDrag = this.condition.orders.length > 0 ? false : true;

      if (this.planId) {
        this.condition.planId = this.planId;
      }

      if (!this.trashEnable) {
        if (this.selectNodeIds && this.selectNodeIds.length > 0) {
          if (!this.selectNode || this.selectNode.data.id !== 'root') {
            // 优化：如果当前选中节点是root节点，则不添加过滤条件
            this.condition.nodeIds = this.selectNodeIds;
          }
        }
      }
      // todo 优化参数传递方式
      if (nodeIds && Array.isArray(nodeIds) && nodeIds.length > 0) {
        this.condition.nodeIds = nodeIds;
        this.condition.workspaceId = getCurrentWorkspaceId();
      }
      this.getData();
    },
    getData() {
      this.getSelectDataRange();
      this.condition.selectThisWeedData = false;
      this.condition.selectThisWeedRelevanceData = false;
      this.condition.caseCoverage = null;
      if (this.selectDataRange && this.selectDataRange.indexOf("single") > -1) {
        this.condition.ids = [this.selectDataRange.substring(7)];
      }
      switch (this.selectDataRange) {
        case 'thisWeekCount':
          this.condition.selectThisWeedData = true;
          break;
        case 'thisWeekRelevanceCount':
          this.condition.selectThisWeedRelevanceData = true;
          break;
        case 'uncoverage':
          this.condition.caseCoverage = 'uncoverage';
          break;
        case 'coverage':
          this.condition.caseCoverage = 'coverage';
          break;
        case 'notReviewed':
          this.condition.filters.review_status = ['Prepare', 'Underway', 'Again'];
          break
        case 'reviewed':
          this.condition.filters.review_status = ['UnPass', 'Pass'];
          break
        case 'reviewSuccess':
          this.condition.filters.review_status = ['Pass'];
          break;
        case 'reviewFail':
          this.condition.filters.review_status = ['UnPass'];
          break;
      }
      if (this.trashEnable) {
        //支持回收站查询版本
        let versionIds = this.condition.filters.version_id;
        this.condition.filters.status = ["Trash"];
        if (versionIds) {
          this.condition.filters.version_id = versionIds;
        }
      }
      if (this.projectId) {
        this.condition.projectId = this.projectId;
        this.$emit('setCondition', this.condition);
        this.loading = true;
        testCaseList({pageNum: this.page.currentPage, pageSize: this.page.pageSize}, this.condition)
          .then(response => {
            this.loading = false;
            let data = response.data;
            this.page.total = data.itemCount;
            this.page.data = data.listObject;
            parseCustomFilesForList(this.page.data);
            parseTag(this.page.data);
            this.page.data.forEach(item => {
              if (item.customFields) {
                item.customFields = JSON.parse(item.customFields);
              }
            });
          });
        this.$emit("getTrashList");
        this.$emit("getPublicList")
      }
    },
    search() {
      this.refreshBySearch = true;
      // 添加搜索条件时，当前页设置成第一页
      this.page.currentPage = 1;
      this.initTableData();
      this.$emit('search');
    },
    callBackSelect(selection) {
      this.selectCounts = this.$refs.table.selectDataCounts;
    },
    callBackSelectAll(selection) {
      this.selectCounts = this.$refs.table.selectDataCounts;
    },
    changeVersion(currentVersion) {
      this.currentVersion = currentVersion || null;
      this.$emit('search',currentVersion);
    },
    toggleAdvanceSearch() {
      this.$refs.advanceSearch.toggle();
    },
    reloadTable() {
      this.$refs.table.resetHeader();
    },
    handleEdit(testCase) {
      openCaseEdit({caseId: testCase.id}, this);
    },
    getCase(id) {
      this.$refs.testCasePreview.open();
      this.rowCaseResult.loading = true;

      getTestCaseStep(id)
        .then(response => {
          this.rowCaseResult.loading = false;
          this.rowCase = response.data;
          this.rowCase.steps = JSON.parse(this.rowCase.steps);
          if (!this.rowCase.steps || this.rowCase.length < 1) {
            this.rowCase.steps = [{
              num: 1,
              desc: '',
              result: ''
            }];
          }
          if (!this.rowCase.stepModel) {
            this.rowCase.stepModel = "STEP";
          }
          this.$refs.testCasePreview.setData(this.rowCase);
        });
    },
    handleCopy(testCase) {
      openCaseEdit({caseId: testCase.id, type: 'copy', projectId: this.projectId}, this);
    },
    handleDelete(testCase) {
      let title = this.$t('test_track.case.case_delete_completely_confirm') + ": " + testCase.name + "?";
      this.$confirm(this.$t('test_track.case.batch_delete_tip'), title, {
          cancelButtonText: this.$t("commons.cancel"),
          confirmButtonText: this.$t("commons.delete"),
          customClass: 'custom-confirm-delete',
          callback: action => {
            if (action === "confirm") {
              this._handleDelete(testCase);
            }
          }
        }
      );
    },
    reduction(testCase) {
      let param = {};
      param.ids = [testCase.id];
      param.projectId = getCurrentProjectID();
      testCaseReduction(param)
        .then(() => {
          this.$emit('refresh');
          this.initTableData();
          this.$success(this.$t('commons.recover_success'), false);
        });
    },
    handleDeleteToGc(testCase) {
      getTestCaseVersions(testCase.id)
        .then(response => {
          if (hasLicense() && this.versionEnable && response.data.length > 1) {
            // 删除提供列表删除和全部版本删除
            this.$refs.apiDeleteConfirm.open(testCase, this.$t('test_track.case.delete_confirm'));
          } else {
            let title = this.$t('test_track.case.case_delete_confirm') + ": " + testCase.name + "?";
            this.$confirm(this.$t('test_track.case.batch_delete_soft_tip'), title, {
                cancelButtonText: this.$t("commons.cancel"),
                confirmButtonText: this.$t("commons.delete"),
                customClass: 'custom-confirm-delete',
                callback: action => {
                  if (action === "confirm") {
                    this._handleDeleteVersion(testCase, false);
                  }
                }
              }
            );
          }
        })
    },
    batchReduction() {
      let param = buildBatchParam(this, this.$refs.table.selectIds);
      testCaseReduction(param)
        .then(() => {
          this.$emit('refresh');
          this.initTableData();
          this.clearTableSelect();
          this.$success(this.$t('commons.recover_success'), false);
        });
    },
    handleDeleteBatch() {
      let title = this.$t('test_track.case.batch_delete_completely_confirm', [this.$refs.table.selectIds.length]);
      this.$confirm(this.$t('test_track.case.batch_delete_tip'), title, {
          cancelButtonText: this.$t("commons.cancel"),
          confirmButtonText: this.$t("commons.delete"),
          customClass: 'custom-confirm-delete',
          callback: action => {
            if (action === "confirm") {
              let param = buildBatchParam(this, this.$refs.table.selectIds);
              testCaseBatchDelete(param)
                .then(() => {
                  this.clearTableSelect();
                  this.$emit("refresh");
                  this.initTableData();
                  this.$success(this.$t('commons.delete_success'), false);
                });
            }
          }
        }
      );
    },
    generateGraph() {
      if (getSelectDataCounts(this.condition, this.$refs.table.selectDataCounts, this.$refs.table.selectRows) > 500) {
        this.$warning(this.$t('test_track.case.generate_dependencies_warning'));
        return;
      }
      getGraphByCondition('TEST_CASE', buildBatchParam(this, this.$refs.table.selectIds))
        .then((r) => {
          this.graphData = r.data;
          this.$refs.relationshipGraph.open();
        });
    },
    handleDeleteBatchToGc() {
      let title = this.$t('test_track.case.batch_delete_confirm', [this.selectCounts]);
      this.$confirm(this.$t('test_track.case.batch_delete_soft_tip'), title, {
          cancelButtonText: this.$t("commons.cancel"),
          confirmButtonText: this.$t("commons.delete"),
          customClass: 'custom-confirm-delete',
          callback: action => {
            if (action === "confirm") {
              let param = buildBatchParam(this, this.$refs.table.selectIds);
              testCaseBatchDeleteToGc(param)
                .then(() => {
                  this.clearTableSelect();
                  this.$emit("refresh");
                  this.$success(this.$t('commons.delete_success'), false);
                });
            }
          }
        }
      );
    },
    _handleDelete(testCase) {
      let testCaseId = testCase.id;
      testCaseDelete(testCaseId)
        .then(() => {
          this.$emit('refresh');
          this.clearTableSelect();
          this.initTableData();
          this.$success(this.$t('commons.delete_success'), false);
          this.$emit('decrease', testCase.nodeId);
        });
    },
    _handleDeleteToGc(testCase) {
      let testCaseId = testCase.id;
      testCaseDeleteToGc(testCaseId)
        .then(() => {
          this.$emit('refreshAll');
          this.initTableData();
          this.$success(this.$t('commons.delete_success'), false);
        });
    },
    clearTableSelect() {
      this.$refs.table.clear();
      this.selectCounts = 0;
    },
    refresh() {
      this.$refs.table.clear();
      this.selectCounts = 0;
      this.$emit('refreshAll');
    },
    refreshAll() {
      this.$refs.table.clear();
      this.selectCounts = 0;
      this.$emit('refreshAll');
    },
    importTestCase() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$refs.testCaseImport.open();
    },
    exportTestCase(exportType, fieldParam) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      let param = buildBatchParam(this, this.$refs.table.selectIds);
      Object.assign(param, fieldParam);
      let fileNameSuffix;
      let url;
      if (exportType === 'xmind') {
        url = '/test/case/export/testcase/xmind';
        fileNameSuffix = ".xmind";
      } else {
        url = '/test/case/export/testcase'
        fileNameSuffix = this.selectCounts > 1000 ? ".zip" : ".xlsx";
      }
      this.loading = true;
      store.isTestCaseExporting = true;
      fileDownloadPost(url, param, "Metersphere_case_" + this.projectName + fileNameSuffix)
        .then(() => {
          this.loading = false;
          this.$emit('closeExport');
          store.isTestCaseExporting = false;
        }).catch(() => {
          this.loading = false;
          store.isTestCaseExporting = false;
        });
    },
    batchEdit(form) {
      let ids = this.$refs.table.selectIds;
      let param = {};
      param.ids = ids;
      param.condition = this.condition;
      if (form.type.startsWith("custom")) {
        param.customTemplateFieldId = form.type.slice(6);
        param.customField = {
          fieldId: form.customField.id,
          name: form.customField.name,
        };
        if (form.customField.type && (form.customField.type === 'richText' || form.customField.type === 'textarea')) {
          param.customField.textValue = form.customField.defaultValue;
        } else {
          param.customField.value = JSON.stringify(form.customField.defaultValue ? form.customField.defaultValue : '');
        }
      } else if (form.type === 'tags') {
        param.type = form.type;
        param.appendTag = form.appendTag;
        param.tagList = form.tags;
      }
      testCaseBatchEdit(param)
        .then(() => {
          this.$success(this.$t('commons.save_success'), false);
          this.refresh();
        });
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open(this.condition.selectAll ? this.page.total : this.$refs.table.selectRows.size);
    },
    handleBatchAddPublic() {
      getProjectApplicationConfig('CASE_PUBLIC')
        .then(res => {
          let data = res.data;
          if (data && data.typeValue === 'true') {
            let param = {};
            param.ids = this.$refs.table.selectIds;
            param.casePublic = true;
            param.condition = this.condition;
            this.loading = true;
            testCaseBatchEdit(param)
              .then(() => {
                this.$success(this.$t('commons.save_success'), false);
                this.loading = false;
                this.refresh();
              });
          } else {
            this.$warning(this.$t('test_track.case.public_warning'), false);
          }
        });
    },
    handleBatchExportToExcel() {
      this.$refs.exportExcel.open(this.selectCounts, false);
    },
    handleBatchExportToXmind() {
      this.exportTestCase("xmind", {exportAll: false})
    },
    openRelateDemand() {
      this.$refs.relateDemand.open(this.condition.selectAll ? this.page.total : this.$refs.table.selectRows.size);
    },
    _batchRelateDemand(form) {
      if (form.demandId !== 'other') {
        form.demandName = '';
      }
      let ids = this.$refs.table.selectIds;
      let param = {};
      param.ids = ids;
      param.condition = this.condition;
      param.demandId = form.demandId;
      param.demandName = form.demandName;
      testCaseBatchRelateDemand(param)
        .then(() => {
          this.$success(this.$t('commons.save_success'), false);
          this.refresh();
        });
    },
    handleBatchMove() {
      this.isMoveBatch = true;
      let firstSelectRow = this.$refs.table.selectRows.values().next().value;
      this.$refs.testBatchMove.open(this.isMoveBatch, firstSelectRow.name, this.treeNodes, this.selectCounts, this.$refs.table.selectIds, this.moduleOptions);
    },
    handleBatchCopy() {
      this.isMoveBatch = false;
      let firstSelectRow = this.$refs.table.selectRows.values().next().value;
      this.$refs.testBatchMove.open(this.isMoveBatch, firstSelectRow.name, this.treeNodes, this.selectCounts, this.$refs.table.selectIds, this.moduleOptions);
    },
    _handleDeleteVersion(testCase, deleteCurrentVersion) {
      // 删除指定版本
      if (deleteCurrentVersion) {
        deleteTestCaseVersion(testCase.versionId, testCase.refId)
          .then(() => {
            this.$success(this.$t('commons.delete_success'), false);
            this.$refs.apiDeleteConfirm.close();
            this.$emit("refreshAll");
          });
      } else {
        // 删除全部版本
        this._handleDeleteToGc(testCase);
        this.$refs.apiDeleteConfirm.close();
      }
    },
    checkSelected() {
      let selectIds = this.$refs.table.selectIds;
      if (!selectIds || selectIds.length < 1) {
        this.$warning(this.$t("test_track.case.check_select"));
        return false;
      }
      return true;
    },
    getMaintainerOptions() {
      getProjectMemberOption()
        .then(response => {
          this.valueArr.maintainer = response.data;
        });
    },
    moveSave(param) {
      param.condition = this.condition;
      let func = testCaseBatchEdit;
      if (!this.isMoveBatch) {
        func = testCaseBatchCopy;
      }
      param.projectId = this.projectId;
      this.loading = true;
      func(param)
        .then(() => {
          this.$refs.testBatchMove.btnDisable = false;
          this.$success(this.$t('commons.save_success'), false);
          this.$refs.testBatchMove.close();
          this.refresh();
        });
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        versionEnableByProjectId(this.projectId)
          .then(response => {
            this.enableVersionColumn = response.data;
          });
      }
    },
    generateColumnKey,
    getProject() {
      getProjectApplicationConfig('CASE_CUSTOM_NUM')
        .then(result => {
          let data = result.data;
          if (data && data.typeValue === 'true') {
            store.currentProjectIsCustomNum = true;
          } else {
            store.currentProjectIsCustomNum = false;
          }
        });
    },
  }
};
</script>

<style scoped>
.el-tag {
  margin-left: 10px;
}

.oneLine {
  overflow: hidden;
  white-space: nowrap;
}

:deep(.el-table) {
  overflow: auto;
}

span.version-select {
  margin-left: 12px!important;
}

:deep(span.version-select input.el-input__inner) {
  position: relative;
  top: -1px;
  width: 140px!important;
}

.export-case-layout :deep(.el-button--small span) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: -5px;
}

:deep(button.el-button.el-button--default.el-button--mini) {
  box-sizing: border-box;
  width: 32px;
  height: 32px;
  background: #FFFFFF;
  border: 1px solid #BBBFC4;
  border-radius: 4px;
  flex: none;
  order: 5;
  align-self: center;
  flex-grow: 0;
  margin-left: 12px;
}

:deep(button.el-button.el-button--default.el-button--mini:hover) {
  color: #783887;
  border: 1px solid #783887;
}

:deep(button.el-button.el-button--default.el-button--mini:focus) {
  color: #783887;
  border: 1px solid #783887;
}
</style>
