<template>

  <span>
    <ms-search
      :condition.sync="condition"
      @search="search">
    </ms-search>

    <ms-table
      v-loading="page.result.loading"
      :data="page.data"
      :condition="condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :operators="operators"
      operator-width="170px"
      :screen-height="screenHeight"
      :batch-operators="batchButtons"
      :remember-order="true"
      :enable-order-drag="enableOrderDrag"
      row-key="id"
      :row-order-group-id="projectId"
      :row-order-func="editTestCaseOrder"
      @handlePageChange="initTableData"
      @handleRowClick="handleEdit"
      :fields.sync="fields"
      :field-key="tableHeaderKey"
      @refresh="initTableData"
      :custom-fields="testCaseTemplate.customFields"
      ref="table">

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
        prop="deleteUserId"
        :fields-width="fieldsWidth"
        v-if="this.trashEnable"
        :label="$t('commons.delete_user')"
        min-width="120"/>

       <ms-table-column
         prop="projectName"
         :fields-width="fieldsWidth"
         :label="$t('test_track.case.project')"
         v-if="publicEnable"
         min-width="150px">
        </ms-table-column>

      <span v-for="(item, index) in fields" :key="index">
        <ms-table-column
          v-if="item.id === 'lastExecResult'"
          prop="lastExecuteResult"
          min-width="100px"
          :label="$t('test_track.plan_view.execute_result')">
        <template v-slot:default="scope">
          <span @click.stop="clickt = 'stop'">
              <span class="el-dropdown-link">
                  <status-table-item :value="scope.row.lastExecuteResult ? scope.row.lastExecuteResult : 'Prepare'"/>
              </span>
            </span>
        </template>
      </ms-table-column>

        <ms-table-column
          v-if="!customNum"
          :field="item"
          :fields-width="fieldsWidth"
          prop="num"
          sortable
          :label="$t('commons.id')"
          min-width="80"/>

        <ms-table-column
          v-if="item.id === 'num' && customNum"
          :fields-width="fieldsWidth"
          prop="customNum"
          sortable
          :label="$t('commons.id')"
          min-width="80"/>

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
          prop="createName"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.create_user')"
          min-width="120"/>

        <ms-table-column
          prop="reviewStatus"
          min-width="120px"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.case.status')">
          <template v-slot:default="scope">
            <span class="el-dropdown-link">
              <review-status :value="scope.row.reviewStatus"/>
            </span>
          </template>
        </ms-table-column>

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
          :filters="!publicEnable ? versionFilters : null"
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
          v-if="!publicEnable"
          min-width="150px">
        </ms-table-column>

        <ms-table-column
          prop="updateTime"
          sortable
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.update_time')"
          min-width="150px">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>
        <ms-table-column prop="createTime"
                         :field="item"
                         :fields-width="fieldsWidth"
                         :label="$t('commons.create_time')"
                         sortable
                         min-width="150px">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>

        <ms-table-column v-for="field in testCaseTemplate.customFields" :key="field.id"
                         :filters="getCustomFieldFilter(field)"
                         :field="item"
                         :fields-width="fieldsWidth"
                         :label="field.system ? $t(systemFiledMap[field.name]) :field.name"
                         :min-width="120"
                         :prop="field.name">
          <template v-slot="scope">
            <span v-if="field.name === '用例等级'">
                <priority-table-item
                  :value="getCustomFieldValue(scope.row, field) ? getCustomFieldValue(scope.row, field) : scope.row.priority"/>
            </span>
            <span v-else-if="field.name === '用例状态'">
                {{ getCustomFieldValue(scope.row, field) ? getCustomFieldValue(scope.row, field) : scope.row.status }}
            </span>
            <span v-else>
              {{ getCustomFieldValue(scope.row, field) }}
            </span>
          </template>
        </ms-table-column>

      </span>

    </ms-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                         :total="page.total"/>

    <batch-edit ref="batchEdit" @batchEdit="batchEdit"
                :typeArr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')"/>

    <batch-move @refresh="refresh" @moveSave="moveSave" ref="testBatchMove" :public-enable="publicEnable"
                @copyPublic="copyPublic"/>

    <test-case-preview ref="testCasePreview" :loading="rowCaseResult.loading"/>

    <relationship-graph-drawer :graph-data="graphData" ref="relationshipGraph"/>

    <!--  删除接口提示  -->
    <list-item-delete-confirm ref="apiDeleteConfirm" @handleDelete="_handleDeleteVersion"/>
  </span>

</template>

<script>

import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import TestCaseImport from './import/TestCaseImport';
import TestCaseExport from '../components/TestCaseExport';
import MsTablePagination from '../../../../components/common/pagination/TablePagination';
import NodeBreadcrumb from '../../common/NodeBreadcrumb';
import MsTableHeader from '../../../../components/common/components/MsTableHeader';
import PriorityTableItem from "../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../common/tableItems/planview/MethodTableItem";
import MsTableOperator from "../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
import MsTableButton from "../../../common/components/MsTableButton";
import {TEST_CASE_CONFIGS} from "../../../common/components/search/search-components";
import BatchEdit from "./BatchEdit";
import {TEST_CASE_LIST} from "@/common/js/constants";
import StatusTableItem from "@/business/components/track/common/tableItems/planview/StatusTableItem";
import TestCaseDetail from "./TestCaseDetail";
import ReviewStatus from "@/business/components/track/case/components/ReviewStatus";
import MsTag from "@/business/components/common/components/MsTag";
import ApiStatus from "@/business/components/api/definition/components/list/ApiStatus.vue";
import {
  buildBatchParam,
  deepClone,
  getCustomFieldBatchEditOption,
  getCustomFieldValue, getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  getPageInfo,
  getTableHeaderWithCustomFields,
  initCondition, parseCustomFilesForList,
} from "@/common/js/tableUtils";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import PlanStatusTableItem from "@/business/components/track/common/tableItems/plan/PlanStatusTableItem";
import {
  getCurrentProjectID,
  getCurrentUserId,
  getCurrentWorkspaceId,
  getUUID,
  hasLicense,
  parseTag
} from "@/common/js/utils";
import {getTestTemplate} from "@/network/custom-field-template";
import {getProjectMember} from "@/network/user";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import BatchMove from "@/business/components/track/case/components/BatchMove";
import {SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";
import TestCasePreview from "@/business/components/track/case/components/TestCasePreview";
import {editTestCaseOrder} from "@/network/testCase";
import {getGraphByCondition} from "@/network/graph";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import ListItemDeleteConfirm from "@/business/components/common/components/ListItemDeleteConfirm";
import {getAdvSearchCustomField} from "@/business/components/common/components/search/custom-component";
import MsSearch from "@/business/components/common/components/search/MsSearch";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const relationshipGraphDrawer = requireComponent.keys().length > 0 ? requireComponent("./graph/RelationshipGraphDrawer.vue") : {};

export default {
  name: "TestCaseList",
  components: {
    MsSearch,
    ListItemDeleteConfirm,
    MsTableAdvSearchBar,
    TestCasePreview,
    BatchMove,
    MsTableColumn,
    MsTable,
    PlanStatusTableItem,
    HeaderLabelOperate,
    MsTableHeaderSelectPopover,
    MsTableButton,
    MsTableOperatorButton,
    MsTableOperator,
    MethodTableItem,
    TypeTableItem,
    PriorityTableItem,
    TestCaseImport,
    TestCaseExport,
    MsTablePagination,
    NodeBreadcrumb,
    MsTableHeader,
    BatchEdit,
    StatusTableItem,
    TestCaseDetail,
    ReviewStatus,
    MsTag, ApiStatus,
    "relationshipGraphDrawer": relationshipGraphDrawer.default,
  },
  data() {
    return {
      addPublic: false,
      projectName: "",
      type: TEST_CASE_LIST,
      tableHeaderKey: "TRACK_TEST_CASE",
      screenHeight: 'calc(100vh - 228px)',
      tableLabel: [],
      deletePath: "/test/case/delete",
      enableOrderDrag: true,
      isMoveBatch: true,
      condition: {
        components: TEST_CASE_CONFIGS,
        filters: {},
        custom: true,
      },
      versionFilters: [],
      graphData: {},
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      methodFilters: [
        {text: this.$t('test_track.case.manual'), value: 'manual'},
        {text: this.$t('test_track.case.auto'), value: 'auto'}
      ],
      typeFilters: [
        {text: this.$t('commons.functional'), value: 'functional'},
        {text: this.$t('commons.performance'), value: 'performance'},
        {text: this.$t('commons.api'), value: 'api'}
      ],
      reviewStatusFilters: [
        {text: this.$t('test_track.review.prepare'), value: 'Prepare'},
        {text: this.$t('test_track.review.pass'), value: 'Pass'},
        {text: this.$t('test_track.review.un_pass'), value: 'UnPass'},
      ],
      statusFilters: [
        {text: this.$t('test_track.case.status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.case.status_running'), value: 'Underway'},
        {text: this.$t('test_track.case.status_finished'), value: 'Completed'},
      ],
      batchButtons: [],
      simpleButtons: [
        {
          name: this.$t('test_track.case.batch_edit_case'),
          handleClick: this.handleBatchEdit,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT']
        },
        {
          name: this.$t('test_track.case.batch_move_case'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT']
        },
        {
          name: this.$t('api_test.batch_copy'),
          handleClick: this.handleBatchCopy,
          permissions: ['PROJECT_TRACK_CASE:READ+COPY']
        },
        {
          name: this.$t('test_track.case.batch_delete_case'),
          handleClick: this.handleDeleteBatchToGc,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE']
        },
        {
          name: this.$t('test_track.case.generate_dependencies'),
          isXPack: true,
          handleClick: this.generateGraph,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        },
        {
          name: this.$t('test_track.case.batch_add_public'),
          isXPack: true,
          handleClick: this.handleBatchAddPublic,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API'],
        }
      ],
      publicButtons: [
        {
          name: this.$t('test_track.case.batch_copy'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT']
        }, {
          name: this.$t('test_track.case.batch_delete_case'),
          handleClick: this.handleDeleteBatchToPublic,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE'],
        },
      ],
      trashButtons: [
        {
          name: this.$t('commons.reduction'),
          handleClick: this.batchReduction,
          permissions: ['PROJECT_TRACK_CASE:READ+RECOVER']
        }, {
          name: this.$t('test_track.case.batch_delete_case'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE']
        }
      ],
      operators: [],
      simpleOperators: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT']
        },
        {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
          exec: this.handleCopy,
          permissions: ['PROJECT_TRACK_CASE:READ+COPY']
        },
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDeleteToGc,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE']
        }
      ],
      publicOperators: [
        {
          tip: this.$t('commons.view'), icon: "el-icon-view",
          exec: this.handleEditShow,
          permissions: ['PROJECT_TRACK_CASE:READ'],
        },
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEditPublic,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT'],
          isDisable: this.isPublic
        },
        {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
          exec: this.handleCopyPublic,
          permissions: ['PROJECT_TRACK_CASE:READ+COPY']
        },
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDeleteToGc,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE'],
          isDisable: this.isPublic
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
      rowCaseResult: {}
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
    publicEnable: {
      type: Boolean,
      default: false,
    },
    currentVersion: String,
    versionEnable: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    selectNodeIds() {
      return this.$store.state.testCaseSelectNodeIds;
    },
    moduleOptions() {
      return this.$store.state.testCaseModuleOptions;
    },
    customNum() {
      return this.$store.state.currentProjectIsCustomNum;
    },
    systemFiledMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    editTestCaseOrder() {
      return editTestCaseOrder;
    }
  },
  created: function () {
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
    } else if (this.publicEnable) {
      this.operators = this.publicOperators;
      this.batchButtons = this.publicButtons;
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
        this.initTableData();
        this.condition.ids = null;
        this.getVersionOptions();
      }
    },
    selectNodeIds() {
      this.page.currentPage = 1;
      if (!this.trashEnable) {
        this.condition.filters.status = [];
      }
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
    publicEnable() {
      if (this.publicEnable) {
        //更改表格按钮
        this.operators = this.publicOperators;
        this.batchButtons = this.publicButtons;
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
      this.page.result.loading = true;
      let p1 = getProjectMember((data) => {
        this.members = data;
        this.members.forEach(item => {
          this.memberMap.set(item.id, item.name);
        });
      });
      let p2 = getTestTemplate();
      Promise.all([p1, p2]).then((data) => {
        let template = data[1];
        this.testCaseTemplate = template;
        this.fields = getTableHeaderWithCustomFields('TRACK_TEST_CASE', this.testCaseTemplate.customFields);
        // todo 处理高级搜索自定义字段部分
        let comp = getAdvSearchCustomField(this.condition.components, this.testCaseTemplate.customFields);
        this.condition.components.push(...comp);
        this.setTestCaseDefaultValue(template);
        this.typeArr = [];
        getCustomFieldBatchEditOption(template.customFields, this.typeArr, this.valueArr, this.members);

        this.$nextTick(() => {
          if (this.$refs.table) {
            this.$refs.table.resetHeader();
          }
          this.page.result.loading = false;
        });
      });
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
      });
      this.$store.commit('setTestCaseDefaultValue', testCaseDefaultValue);
    },
    getCustomFieldValue(row, field) {
      let value = getCustomFieldValue(row, field, this.members);
      if (field.name === '用例等级') {
        return row.priority;
      } else if (field.name === '责任人') {
        return row.maintainer;
      } else if (field.name === '用例状态') {
        return row.status;
      }
      return value ? value : '';
    },
    getCustomFieldFilter(field) {
      if (field.name === '用例等级') {
        return this.priorityFilters;
      } else if (field.name === '用例状态') {
        return this.statusFilters;
      }
      return null;
    },
    checkRedirectEditPage(redirectParam) {
      if (redirectParam != null) {
        this.$get('test/case/get/' + redirectParam, response => {
          let testCase = response.data;
          testCase.label = "redirect";
          this.$emit('testCaseEdit', testCase);
        });
      }
    },
    getProjectName() {
      this.$get('project/get/' + this.projectId, response => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
        }
      });
    },
    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
    },
    getSelectDataRange() {
      let dataRange = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      this.selectDataRange = dataType === 'case' ? dataRange : 'all';
    },
    initTableData() {
      this.condition.planId = "";
      this.condition.nodeIds = [];
      //initCondition(this.condition);
      initCondition(this.condition, this.condition.selectAll);
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
      this.condition.versionId = this.currentVersion || null;
      this.enableOrderDrag = this.condition.orders.length > 0 ? false : true;

      if (this.planId) {
        // param.planId = this.planId;
        this.condition.planId = this.planId;
      }
      if (!this.trashEnable && !this.publicEnable) {
        if (this.selectNodeIds && this.selectNodeIds.length > 0) {
          // param.nodeIds = this.selectNodeIds;
          this.condition.nodeIds = this.selectNodeIds;
        }
      }
      this.getData();
    },
    getData() {
      this.getSelectDataRange();
      this.condition.selectThisWeedData = false;
      this.condition.selectThisWeedRelevanceData = false;
      this.condition.caseCoverage = null;
      this.condition.filters.reviewStatus = ["Prepare", "Pass", "UnPass"];
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
        case 'Prepare':
          this.condition.filters.review_status = [this.selectDataRange];
          break;
        case 'Pass':
          this.condition.filters.review_status = [this.selectDataRange];
          break;
        case 'UnPass':
          this.condition.filters.review_status = [this.selectDataRange];
          break;
      }
      this.condition.filters.priority = this.condition.filters['用例等级'];
      this.condition.filters.status = this.condition.filters['用例状态'];
      if (this.trashEnable) {
        //支持回收站查询版本
        let versionIds = this.condition.filters.version_id;
        this.condition.filters = {status: ["Trash"]};
        if (versionIds) {
          this.condition.filters.version_id = versionIds;
        }
      }
      if (this.projectId) {
        this.condition.projectId = this.projectId;
        this.$emit('setCondition', this.condition);
        let url = '/test/case/list';
        if (this.publicEnable) {
          url = '/test/case/publicList';
          this.condition.casePublic = true;
          this.condition.workspaceId = getCurrentWorkspaceId();
        }
        this.page.result = this.$post(this.buildPagePath(url), this.condition, response => {
          let data = response.data;
          this.page.total = data.itemCount;
          this.page.data = data.listObject;
          parseCustomFilesForList(this.page.data);
          parseTag(this.page.data);
        });
        this.$emit("getTrashList");
        this.$emit("getPublicList");
      }
    },
    search() {
      this.initTableData();
    },
    buildPagePath(path) {
      return path + "/" + this.page.currentPage + "/" + this.page.pageSize;
    },
    testCaseCreate() {
      this.$emit('testCaseEdit');
    },
    handleEdit(testCase, column) {
      if (column.label !== this.$t('test_track.case.case_desc')) {
        if (this.publicEnable) {
          return;
        } else {
          this.$get('test/case/get/' + testCase.id, response => {
            let testCase = response.data;
            testCase.trashEnable = this.trashEnable;
            this.$emit('testCaseEdit', testCase);
          });
        }
      }
    },
    handleEditPublic(testCase, column) {
      if (column.label !== this.$t('test_track.case.case_desc')) {
        this.$get('test/case/get/' + testCase.id, response => {
          let testCase = response.data;
          this.$emit('testCaseEdit', testCase);
        });
      }
    },
    handleEditShow(testCase, column) {
      if (column.label !== this.$t('test_track.case.case_desc')) {
        this.$get('test/case/get/' + testCase.id, response => {
          let testCase = response.data;
          this.$emit('testCaseEditShow', testCase);
        });
      }

    },
    isPublic(testCase) {
      if ((testCase.maintainer && testCase.maintainer === getCurrentUserId()) || (testCase.createUser && testCase.createUser === getCurrentUserId())) {
        return false;
      }
      return true;
    },
    getCase(id) {
      this.$refs.testCasePreview.open();
      this.rowCaseResult.loading = true;

      this.rowCaseResult = this.$get('test/case/get/step/' + id, response => {
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
    handleCopyPublic(testCase) {
      this.$refs.table.selectIds.push(testCase.id);
      this.$refs.testBatchMove.open(this.treeNodes, this.$refs.table.selectIds, this.moduleOptions);
    },
    handleCopy(testCase) {
      this.$get('test/case/get/' + testCase.id, response => {
        let testCase = response.data;
        testCase.name = 'copy_' + testCase.name;
        //复制的时候只复制当前版本
        testCase.id = getUUID();
        testCase.refId = null;
        testCase.versionId = null;
        this.$emit('testCaseCopy', testCase);
      });
    },
    handleDelete(testCase) {
      this.$alert(this.$t('test_track.case.delete_confirm') + '\'' + testCase.name + '\'' + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(testCase);
          }
        }
      });
    },
    reduction(testCase) {
      let param = {};
      param.ids = [testCase.id];
      param.projectId = getCurrentProjectID();
      this.$post('/test/case/reduction', param, () => {
        this.$emit('refreshTable');
        this.initTableData();
        this.$success(this.$t('commons.save_success'));
      });
    },
    handleDeleteToGc(testCase) {
      this.$get('/test/case/versions/' + testCase.id, response => {
        if (hasLicense() && this.versionEnable && response.data.length > 1) {
          // 删除提供列表删除和全部版本删除
          this.$refs.apiDeleteConfirm.open(testCase, this.$t('test_track.case.delete_confirm'));
        } else {
          this.$alert(this.$t('test_track.case.delete_confirm') + '\'' + testCase.name + '\'' + "？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                this._handleDeleteVersion(testCase, false);
              }
            }
          });
        }
      });
    },
    batchReduction() {
      let param = buildBatchParam(this, this.$refs.table.selectIds);
      this.$post('/test/case/reduction', param, () => {
        this.$emit('refreshTable');
        this.initTableData();
        this.$success(this.$t('commons.save_success'));
      });
    },
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.case.delete_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this, this.$refs.table.selectIds);
            this.$post('/test/case/batch/delete', param, () => {
              this.$refs.table.clear();
              this.$emit("refresh");
              this.$success(this.$t('commons.delete_success'));
            });
          }
        }
      });
    },
    generateGraph() {
      getGraphByCondition('TEST_CASE', buildBatchParam(this, this.$refs.table.selectIds), (data) => {
        this.graphData = data;
        this.$refs.relationshipGraph.open();
      });
    },
    handleDeleteBatchToGc() {
      this.$alert(this.$t('test_track.case.delete_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this, this.$refs.table.selectIds);
            this.$post('/test/case/batch/deleteToGc', param, () => {
              this.$refs.table.clear();
              this.$emit("refresh");
              this.$success(this.$t('commons.delete_success'));
            });
          }
        }
      });
    },
    _handleDelete(testCase) {
      let testCaseId = testCase.id;
      this.$post('/test/case/delete/' + testCaseId, {}, () => {
        this.initTableData();
        this.$success(this.$t('commons.delete_success'));
        this.$emit('decrease', testCase.nodeId);
      });
    },
    _handleDeleteToGc(testCase) {
      let testCaseId = testCase.id;
      this.$post('/test/case/deleteToGc/' + testCaseId, {}, () => {
        this.$emit('refreshTable');
        this.initTableData();
        this.$success(this.$t('commons.delete_success'));
      });
    },
    refresh() {
      this.$refs.table.clear();
      this.$emit('refresh');
    },
    refreshAll() {
      this.$refs.table.clear();
      this.$emit('refreshAll');
    },
    showDetail(row, event, column) {
      this.$emit('testCaseDetail', row);
    },
    importTestCase() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$refs.testCaseImport.open();
    },
    exportTestCase(exportType) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }

      let config = {};
      let fileNameSuffix = "";
      if (exportType === 'xmind') {
        config = {
          url: '/test/case/export/testcase/xmind',
          method: 'post',
          responseType: 'blob',
          data: buildBatchParam(this, this.$refs.table.selectIds)
        };
        fileNameSuffix = ".xmind";
      } else {
        config = {
          url: '/test/case/export/testcase',
          method: 'post',
          responseType: 'blob',
          data: buildBatchParam(this, this.$refs.table.selectIds)
        };
        fileNameSuffix = ".xlsx";
      }

      if (config.data.ids === undefined || config.data.ids.length < 1) {
        this.$warning(this.$t("test_track.case.check_select"));
        return;
      }

      this.page.result = this.$request(config).then(response => {
        const filename = "Metersphere_case_" + this.projectName + fileNameSuffix;
        const blob = new Blob([response.data]);
        if ("download" in document.createElement("a")) {
          let aTag = document.createElement('a');
          aTag.download = filename;
          aTag.href = URL.createObjectURL(blob);
          aTag.click();
          URL.revokeObjectURL(aTag.href);
        } else {
          navigator.msSaveBlob(blob, filename);
        }
      });
    },
    handleBatch(type) {
      if (this.$refs.selectRows.size < 1) {
        if (type === 'export') {
          this.handleExportTestCase();
          return;
        } else {
          this.$warning(this.$t('test_track.plan_view.select_manipulate'));
          return;
        }
      }
      if (type === 'move') {
        let ids = this.$refs.table.selectIds;
        this.$emit('moveToNode', ids);
      } else if (type === 'delete') {
        this.handleDeleteBatch();
      } else {
        this.handleExportTestCase();
      }
    },
    batchEdit(form) {
      let ids = this.$refs.table.selectIds;
      let param = {};
      param.ids = ids;
      param.customTemplateFieldId = form.type.slice(6);
      param.condition = this.condition;
      param.customField = {
        fieldId: form.customField.id,
        name: form.customField.name,
      };
      if (form.customField.type && (form.customField.type === 'richText' || form.customField.type === 'textarea')) {
        param.customField.textValue = form.customField.defaultValue;
      } else {
        param.customField.value = JSON.stringify(form.customField.defaultValue ? form.customField.defaultValue : '');
      }
      this.$post('/test/case/batch/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.refresh();
      });
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open(this.condition.selectAll ? this.page.total : this.$refs.table.selectRows.size);
    },
    handleBatchAddPublic() {
      this.$get('/project_application/get/config/' + getCurrentProjectID() + "/CASE_PUBLIC", res => {
        let data = res.data;
        if (data && data.casePublic) {
          let param = {};
          param.ids = this.$refs.table.selectIds;
          param.casePublic = true;
          param.condition = this.condition;
          this.page.result = this.$post('/test/case/batch/edit', param, () => {
            this.$success(this.$t('commons.save_success'));
            this.refresh();
          });
        } else {
          this.$warning(this.$t('test_track.case.public_warning'));
        }
      });

    },
    handleDeleteBatchToPublic() {
      this.$alert(this.$t('test_track.case.delete_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this, this.$refs.table.selectIds);
            this.$post('/test/case/batch/movePublic/deleteToGc', param, () => {
              this.$refs.table.clear();
              this.$emit("refresh");
              this.$success(this.$t('commons.delete_success'));
            });
          }
        }
      });
    },
    handleBatchMove() {
      this.isMoveBatch = true;
      this.$refs.testBatchMove.open(this.treeNodes, this.$refs.table.selectIds, this.moduleOptions);
    },
    handleBatchCopy() {
      this.isMoveBatch = false;
      this.$refs.testBatchMove.open(this.treeNodes, this.$refs.table.selectIds, this.moduleOptions);
    },
    _handleDeleteVersion(testCase, deleteCurrentVersion) {
      // 删除指定版本
      if (deleteCurrentVersion) {
        if (this.publicEnable) {
          this.$get('/test/case/deletePublic/' + testCase.versionId + '/' + testCase.refId, () => {
            this.$success(this.$t('commons.delete_success'));
            this.$refs.apiDeleteConfirm.close();
            this.$emit("refreshTable");
          });
        } else {
          this.$get('/test/case/delete/' + testCase.versionId + '/' + testCase.refId, () => {
            this.$success(this.$t('commons.delete_success'));
            this.$refs.apiDeleteConfirm.close();
            this.$emit("refreshTable");
          });
        }
      }
      // 删除全部版本
      else {
        if (this.publicEnable) {
          let param = buildBatchParam(this, this.$refs.table.selectIds);
          this.$post('/test/case/batch/movePublic/deleteToGc', param, () => {
            this.$success(this.$t('commons.delete_success'));
            // this.initTable();
            this.$refs.apiDeleteConfirm.close();
            this.$emit("refreshTable");

          });
        } else {
          this._handleDeleteToGc(testCase);
          this.$refs.apiDeleteConfirm.close();
        }

      }
    },
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.valueArr.maintainer = response.data;
      });
    },
    moveSave(param) {
      param.condition = this.condition;
      let url = '/test/case/batch/edit';
      if (!this.isMoveBatch)
        url = '/test/case/batch/copy';
      param.projectId = this.projectId;
      this.page.result = this.$post(url, param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.testBatchMove.close();
        this.refresh();
      });
    },
    copyPublic(param) {
      param.condition = this.condition;
      param.projectId = this.projectId;
      param.condition.projectId = null;
      param.condition.ids = null;
      this.page.result = this.$post('/test/case/batch/copy/public', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.testBatchMove.close();
        this.refresh();
      });
    },
    getVersionOptions() {
      if (hasLicense()) {
        this.$get('/project/version/get-project-versions/' + getCurrentProjectID(), response => {
          this.versionFilters = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
        });
      }
    },
  }
};
</script>

<style scoped>

.table-page {
  padding-top: 10px;
  margin-right: -9px;
  float: right;
}

.operate-button {
  float: right;
}

.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}

.search-input {
  float: right;
  width: 300px;
}

.search {
  margin-left: 10px;
  width: 240px;
}

.el-table {
  cursor: pointer;
}

.el-tag {
  margin-left: 10px;
}

/deep/ .el-table{
  overflow: auto;
}
</style>
