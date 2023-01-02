<template>

  <div class="case-main-layout">

    <div class="case-main-layout-left" style="float: left; display: inline-block">
      <ms-table-count-bar :count-content="$t('table.all_case_content') + '(' + page.total + ')'"></ms-table-count-bar>
    </div>

    <div class="case-main-layout-right" style="float: right; display: inline-block">
      <!-- 简单搜索框 -->
      <ms-new-ui-search :condition.sync="condition" @search="search" style="float: left" />

      <!-- 版本切换组件 -->
      <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" />

      <!-- 高级搜索框  -->
      <ms-table-adv-search :condition.sync="condition" @search="search" ref="advanceSearch"/>

      <!-- 表头自定义显示Popover  -->
      <ms-table-header-custom-popover :fields.sync="fields" :custom-fields="testCaseTemplate.customFields"
                                      :field-key="tableHeaderKey" @reload="reloadTable" />
    </div>

    <ms-table
      v-loading="loading"
      operator-width="170px"
      row-key="id"
      :data="page.data"
      :condition="condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :operators="operators"
      :screen-height="screenHeight"
      :batch-operators="batchButtons"
      :remember-order="true"
      :enable-order-drag="enableOrderDrag"
      :row-order-group-id="projectId"
      :row-order-func="editTestCaseOrder"
      :fields.sync="fields"
      :disable-header-config="true"
      :field-key="tableHeaderKey"
      :custom-fields="testCaseTemplate.customFields"
      @handlePageChange="initTableData"
      @order="initTableData"
      @filter="search"
      @callBackSelect="callBackSelect"
      @callBackSelectAll="callBackSelectAll"
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
          sortable
          :label="$t('commons.id')"
          min-width="80">
          <template v-slot:default="scope">
            <a style="cursor:pointer" @click="handleEdit(scope.row)"> {{ scope.row.num }} </a>
          </template>
        </ms-table-column>

        <ms-table-column
          v-if="item.id === 'num' && customNum"
          :fields-width="fieldsWidth"
          :column-key="'customNum'"
          prop="customNum"
          sortable
          :label="$t('commons.id')"
          min-width="80">
          <template v-slot:default="scope">
            <a style="cursor:pointer" @click="handleEdit(scope.row)"> {{ scope.row.customNum }} </a>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="name"
          sortable
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.name')"
          min-width="120"
        />

        <ms-table-column :label="$t('test_track.case.case_desc')" prop="desc" :field="item" min-width="100px">
          <template v-slot:default="scope">
            <el-link @click.stop="getCase(scope.row.id)" style="color:#783887;">{{ $t('commons.preview') }}</el-link>
          </template>
        </ms-table-column>

        <ms-table-column
          sortable
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
          :label="$t('commons.tag')"
          min-width="80">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :show-tooltip="scope.row.tags.length===1&&itemName.length*12<=80"
                    :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
            <span/>
          </template>
        </ms-table-column>

        <ms-table-column
          v-if="versionEnable"
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
          prop="nodePath"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.case.module')"
          min-width="150px">
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
                  :value="getCustomFieldValue(scope.row, field, scope.row.priority)"/>
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

    <ms-table-batch-operator-group v-if="selectCounts > 0" :batch-operators="batchButtons" :select-counts="selectCounts"/>

    <home-pagination v-if="page.data.length > 0 && selectCounts == 0" :change="initTableData" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                     :total="page.total" layout="total, prev, pager, next, sizes, jumper" style="margin-top: 16px"/>


<!--    <ms-table-pagination :change="initTableData" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"-->
<!--                         :total="page.total" style="margin-top: 16px"/>-->

    <batch-edit ref="batchEdit" @batchEdit="batchEdit"
                :typeArr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')"/>

    <batch-move @refresh="refresh" @moveSave="moveSave" ref="testBatchMove"/>

    <relate-demand ref="relateDemand" @batchRelate="_batchRelateDemand"/>

    <test-case-preview ref="testCasePreview" :loading="rowCaseResult.loading"/>

    <relationship-graph-drawer v-xpack :graph-data="graphData" ref="relationshipGraph"/>

    <!--  删除接口提示  -->
    <list-item-delete-confirm ref="apiDeleteConfirm" @handleDelete="_handleDeleteVersion"/>
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
import {OPERATORS, TEST_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import BatchEdit from "./BatchEdit";
import {TEST_CASE_LIST} from "metersphere-frontend/src/utils/constants";
import MsTag from "metersphere-frontend/src/components/MsTag";
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
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getUUID, operationConfirm, parseTag} from "metersphere-frontend/src/utils"
import {hasLicense} from "metersphere-frontend/src/utils/permission"
import {getTestTemplate} from "@/api/custom-field-template";
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
import RelationshipGraphDrawer from "metersphere-frontend/src/components/graph/MxRelationshipGraphDrawer";
import MsNewUiSearch from "metersphere-frontend/src/components/new-ui/MsSearch";
import {mapState} from "pinia";
import {useStore} from "@/store"
import {getProject} from "@/api/project";
import {getVersionFilters} from "@/business/utils/sdk-utils";
import {getProjectApplicationConfig} from "@/api/project-application";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import TestCaseReviewStatusTableItem from "@/business/common/tableItems/TestCaseReviewStatusTableItem";
import RelateDemand from "@/business/case/components/RelateDemand";
import TestPlanCaseStatusTableItem from "@/business/common/tableItems/TestPlanCaseStatusTableItem";
import {
  generateColumnKey,
  getCustomFieldValueForTrack,
  getProjectMemberOption
} from "@/business/utils/sdk-utils";
import {initTestCaseConditionComponents} from "@/business/case/test-case";


export default {
  name: "TestCaseList",
  components: {
    TestPlanCaseStatusTableItem,
    RelateDemand,
    TestCaseReviewStatusTableItem,
    MsCreateTimeColumn,
    MsUpdateTimeColumn,
    MsNewUiSearch,
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
    MsTag,
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
      screenHeight: 'calc(100vh - 185px)',
      enableOrderDrag: true,
      isMoveBatch: true,
      loading: false,
      condition: {
        components: TEST_CASE_CONFIGS,
        filters: {},
        custom: false,
      },
      versionFilters: [],
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
          name: this.$t('test_track.case.batch_delete_btn'),
          handleClick: this.handleDeleteBatchToGc,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_DELETE']
        },
        {
          name: this.$t('test_track.demand.batch_relate'),
          handleClick: this.openRelateDemand,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_LINK_DEMAND']
        },
        {
          name: this.$t('test_track.case.generate_dependencies'),
          isXPack: true,
          handleClick: this.generateGraph,
          permissions: ['PROJECT_TRACK_CASE:READ+GENERATE_DEPENDENCIES']
        },
        {
          name: this.$t('test_track.case.batch_add_public_btn'),
          isXPack: true,
          handleClick: this.handleBatchAddPublic,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_ADD_PUBLIC'],
        }
      ],
      trashButtons: [
        {
          name: this.$t('commons.reduction'),
          handleClick: this.batchReduction,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_REDUCTION']
        }, {
          name: this.$t('test_track.case.batch_delete_case'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_DELETE']
        }
      ],
      operators: [],
      simpleOperators: [
        {
          tip: this.$t('commons.edit'),
          isTextButton: true,
          exec: this.handleEdit,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT']
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
          tip: this.$t('commons.reduction'),
          icon: "el-icon-refresh-left",
          exec: this.reduction,
          permissions: ['PROJECT_TRACK_CASE:READ+RECOVER']
        },
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE']
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
      selectCounts: 0
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
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
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
    })
  },
  created: function () {
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
    this.getVersionOptions();
  },
  watch: {
    '$route'(to) {
      if (to.path.indexOf("/track/case/all") >= 0) {
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
        this.getVersionOptions();
      }
    },
    selectNodeIds() {
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
    getTemplateField() {
      this.loading = true;
      let p1 = getProjectMember()
        .then((response) => {
          this.members = response.data;
          this.members.forEach(item => {
            this.memberMap.set(item.id, item.name);
          });
        });
      let p2 = getTestTemplate();
      Promise.all([p1, p2]).then((data) => {
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
            this.$refs.table.resetHeader();
          }
          this.loading = false;
        });
      });
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
        } else if (item.name === '责任人') {
          item.columnKey = 'maintainer';
        } else if (item.name === '用例状态') {
          item.columnKey = 'status';
        }
      });
      useStore().testCaseDefaultValue = testCaseDefaultValue;
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
      this.condition.filters.reviewStatus = ["Prepare", "Pass", "UnPass"];
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
          this.condition.filters.review_status = ['Prepare'];
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
              let nodePath = item.nodePath;
              if (item.customFields) {
                item.customFields = JSON.parse(item.customFields);
              }
              if (nodePath.startsWith("/未规划用例", "0")) {
                item.nodePath = nodePath.replaceAll("/未规划用例", "/" + this.$t('api_test.unplanned_case'));
              }
            });
          });
        this.$emit("getTrashList");
        this.$emit("getPublicList");
      }
    },
    search() {
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
    },
    toggleAdvanceSearch() {
      this.$refs.advanceSearch.toggle();
    },
    reloadTable() {
      this.$refs.table.resetHeader();
    },
    testCaseCreate() {
      this.$emit('testCaseEdit');
    },
    handleEdit(testCase) {
      getTestCase(testCase.id)
        .then(r => {
          let testCase = r.data;
          testCase.trashEnable = this.trashEnable;
          this.$emit('testCaseEdit', testCase);
        });
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
      getTestCase(testCase.id)
        .then(r => {
          let testCase = r.data;
          testCase.name = 'copy_' + testCase.name;
          //复制的时候只复制当前版本
          testCase.id = getUUID();
          testCase.refId = null;
          testCase.versionId = null;
          this.$emit('testCaseCopy', testCase);
        });
    },
    handleDelete(testCase) {
      operationConfirm(this, this.$t('test_track.case.delete_confirm') + '\'' + testCase.name + '\'', () => {
        this._handleDelete(testCase);
      });
    },
    reduction(testCase) {
      let param = {};
      param.ids = [testCase.id];
      param.projectId = getCurrentProjectID();
      testCaseReduction(param)
        .then(() => {
          this.$emit('refresh');
          this.initTableData();
          this.$success(this.$t('commons.save_success'));
        });
    },
    handleDeleteToGc(testCase) {
      getTestCaseVersions(testCase.id)
        .then(response => {
          if (hasLicense() && this.versionEnable && response.data.length > 1) {
            // 删除提供列表删除和全部版本删除
            this.$refs.apiDeleteConfirm.open(testCase, this.$t('test_track.case.delete_confirm'));
          } else {
            operationConfirm(this, this.$t('test_track.case.delete_confirm') + '\'' + testCase.name + '\'', () => {
              this._handleDeleteVersion(testCase, false);
            });
          }
        })
    },
    batchReduction() {
      let param = buildBatchParam(this, this.$refs.table.selectIds);
      testCaseReduction(param)
        .then(() => {
          this.$emit('refresh');
          this.initTableData();
          this.$success(this.$t('commons.save_success'));
        });
    },
    handleDeleteBatch() {
      operationConfirm(this, this.$t('test_track.case.delete_confirm'), () => {
        let param = buildBatchParam(this, this.$refs.table.selectIds);
        testCaseBatchDelete(param)
          .then(() => {
            this.$refs.table.clear();
            this.$emit("refresh");
            this.initTableData();
            this.$success(this.$t('commons.delete_success'));
          });
      });
    },
    generateGraph() {
      if (getSelectDataCounts(this.condition, this.total, this.$refs.table.selectRows) > 100) {
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
      let title = this.$t('test_track.case.batch_delete_confirm', [this.$refs.table.selectIds.length]);
      this.$confirm(this.$t('test_track.case.batch_delete_tip'), title, {
          cancelButtonText: this.$t("commons.cancel"),
          confirmButtonText: this.$t("commons.confirm"),
          customClass: 'custom-confirm-delete',
          callback: action => {
            if (action === "confirm") {
              let param = buildBatchParam(this, this.$refs.table.selectIds);
              testCaseBatchDeleteToGc(param)
                .then(() => {
                  this.$refs.table.clear();
                  this.$emit("refresh");
                  this.$success(this.$t('commons.delete_success'));
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
          this.initTableData();
          this.$success(this.$t('commons.delete_success'));
          this.$emit('decrease', testCase.nodeId);
        });
    },
    _handleDeleteToGc(testCase) {
      let testCaseId = testCase.id;
      testCaseDeleteToGc(testCaseId)
        .then(() => {
          this.$emit('refreshAll');
          this.initTableData();
          this.$success(this.$t('commons.delete_success'));
        });
    },
    refresh() {
      this.$refs.table.clear();
      this.$emit('refreshAll');
    },
    refreshAll() {
      this.$refs.table.clear();
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
      let config = {};
      let fileNameSuffix = "";
      if (exportType === 'xmind') {
        config = {
          url: '/test/case/export/testcase/xmind',
          method: 'post',
          responseType: 'blob',
          data: param
        };
        fileNameSuffix = ".xmind";
      } else {
        config = {
          url: '/test/case/export/testcase',
          method: 'post',
          responseType: 'blob',
          data: param
        };
        fileNameSuffix = ".xlsx";
      }
      this.loading = true;
      this.$request(config).then(response => {
        this.loading = false;
        const filename = "Metersphere_case_" + this.projectName + fileNameSuffix;
        const blob = new Blob([response.data]);
        if ("download" in document.createElement("a")) {
          let aTag = document.createElement('a');
          aTag.download = filename;
          aTag.href = URL.createObjectURL(blob);
          aTag.click();
          URL.revokeObjectURL(aTag.href);
          this.$emit('closeExport');
        } else {
          navigator.msSaveBlob(blob, filename);
          this.$emit('closeExport');
        }
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
          this.$success(this.$t('commons.save_success'));
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
                this.$success(this.$t('commons.save_success'));
                this.loading = false;
                this.refresh();
              });
          } else {
            this.$warning(this.$t('test_track.case.public_warning'));
          }
        });
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
          this.$success(this.$t('commons.save_success'));
          this.refresh();
        });
    },
    handleBatchMove() {
      this.isMoveBatch = true;
      let firstSelectRow = this.$refs.table.selectRows.values().next().value;
      this.$refs.testBatchMove.open(this.isMoveBatch, firstSelectRow.name, this.treeNodes, this.$refs.table.selectIds, this.moduleOptions);
    },
    handleBatchCopy() {
      this.isMoveBatch = false;
      let firstSelectRow = this.$refs.table.selectRows.values().next().value;
      this.$refs.testBatchMove.open(this.isMoveBatch, firstSelectRow.name, this.treeNodes, this.$refs.table.selectIds, this.moduleOptions);
    },
    _handleDeleteVersion(testCase, deleteCurrentVersion) {
      // 删除指定版本
      if (deleteCurrentVersion) {
        deleteTestCaseVersion(testCase.versionId, testCase.refId)
          .then(() => {
            this.$success(this.$t('commons.delete_success'));
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
          this.$success(this.$t('commons.save_success'));
          this.$refs.testBatchMove.close();
          this.refresh();
        });
    },
    getVersionOptions() {
      if (hasLicense()) {
        getVersionFilters(getCurrentProjectID())
          .then(r =>  this.versionFilters = r.data);
      }
    },
    generateColumnKey
  }
};
</script>

<style scoped>

.el-table {
  cursor: pointer;
}

.el-tag {
  margin-left: 10px;
}

:deep(.el-table) {
  overflow: auto;
  position: relative;
  top: 16px;
}

span.version-select {
  margin-left: 12px!important;
}

:deep(span.version-select input.el-input__inner) {
  position: relative;
  top: -1px;
  width: 140px!important;
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
