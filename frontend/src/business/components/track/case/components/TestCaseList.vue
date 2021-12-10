<template>

  <div class="card-container">

    <ms-table-header :condition.sync="condition" @search="initTableData"
                     :tip="$t('commons.search_by_name_or_id')" title="" :show-create="false"/>

    <ms-table
      v-loading="page.result.loading"
      :data="page.data"
      :condition="condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :operators="operators"
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
        prop="lastExecuteResult"
        min-width="100px"
        :label="$t('test_track.plan_view.execute_result')">
        <template v-slot:default="scope">
          <span @click.stop="clickt = 'stop'">
              <span class="el-dropdown-link">
                  <status-table-item :value="scope.row.lastExecuteResult"/>
              </span>
            </span>
        </template>
      </ms-table-column>

      <span v-for="item in fields" :key="item.key">
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

        <ms-table-column :label="$t('test_track.case.case_desc')" prop="desc" :field="item">
          <template v-slot:default="scope">
            <el-link @click.stop="getCase(scope.row.id)" style="color:#783887;">{{ $t('commons.preview') }}</el-link>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="createUser"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.create_user')"
          min-width="120">
          <template v-slot:default="scope">
            {{ memberMap.get(scope.row.createUser) }}
          </template>
        </ms-table-column>

        <ms-table-column
          prop="reviewStatus"
          min-width="100px"
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
                    :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
            <span/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="nodePath"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.case.module')"
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
                         :min-width="90"
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

    <batch-move @refresh="refresh" @moveSave="moveSave" ref="testBatchMove"/>

    <test-case-preview ref="testCasePreview" :loading="rowCaseResult.loading"/>

    <relationship-graph-drawer :graph-data="graphData" ref="relationshipGraph"/>
  </div>

</template>

<script>

import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import TestCaseImport from '../components/TestCaseImport';
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
import {PROJECT_NAME, TEST_CASE_LIST} from "@/common/js/constants";
import StatusTableItem from "@/business/components/track/common/tableItems/planview/StatusTableItem";
import TestCaseDetail from "./TestCaseDetail";
import ReviewStatus from "@/business/components/track/case/components/ReviewStatus";
import MsTag from "@/business/components/common/components/MsTag";
import ApiStatus from "@/business/components/api/definition/components/list/ApiStatus.vue";
import {
  buildBatchParam,
  deepClone,
  getCustomFieldBatchEditOption,
  getCustomFieldValue,
  getCustomTableWidth, getLastTableSortField,
  getPageInfo,
  getTableHeaderWithCustomFields,
  initCondition,
} from "@/common/js/tableUtils";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import PlanStatusTableItem from "@/business/components/track/common/tableItems/plan/PlanStatusTableItem";
import {getCurrentProjectID} from "@/common/js/utils";
import {getTestTemplate} from "@/network/custom-field-template";
import {getProjectMember} from "@/network/user";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import BatchMove from "@/business/components/track/case/components/BatchMove";
import {SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";
import TestCasePreview from "@/business/components/track/case/components/TestCasePreview";
import {editTestCaseOrder} from "@/network/testCase";
import {getGraphByCondition} from "@/network/graph";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const relationshipGraphDrawer = requireComponent.keys().length > 0 ? requireComponent("./graph/RelationshipGraphDrawer.vue") : {};

export default {
  name: "TestCaseList",
  components: {
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
      projectName: "",
      type: TEST_CASE_LIST,
      tableHeaderKey: "TRACK_TEST_CASE",
      screenHeight: 'calc(100vh - 258px)',
      tableLabel: [],
      deletePath: "/test/case/delete",
      enableOrderDrag: true,
      condition: {
        components: TEST_CASE_CONFIGS,
        filters: {}
      },
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
        {text: '未开始', value: 'Prepare'},
        {text: '进行中', value: 'Underway'},
        {text: '已完成', value: 'Completed'},
      ],
      batchButtons: [],
      simpleButtons: [
        {
          name: this.$t('test_track.case.batch_edit_case'),
          handleClick: this.handleBatchEdit,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT']
        }, {
          name: this.$t('test_track.case.batch_move_case'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT']
        }, {
          name: this.$t('test_track.case.batch_delete_case'),
          handleClick: this.handleDeleteBatchToGc,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE']
        },
        {
          name: this.$t('生成依赖关系'),
          isXPack: true,
          handleClick: this.generateGraph,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        }
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
      fields: [],
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
  activated() {
    this.getTemplateField();
    let ids = this.$route.params.ids;
    if (ids) {
      this.condition.ids = ids;
    }
    this.initTableData();
    this.condition.ids = null;
  },
  watch: {
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
    }
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
        this.page.result.loading = true;
        this.testCaseTemplate = template;
        this.fields = getTableHeaderWithCustomFields('TRACK_TEST_CASE', this.testCaseTemplate.customFields);
        this.page.result.loading = false;
        if (this.$refs.table) {
          this.$refs.table.reloadTable();
        }
        this.typeArr = [];
        getCustomFieldBatchEditOption(template.customFields, this.typeArr, this.valueArr, this.members);
      });
    },
    getCustomFieldValue(row, field) {
      let value = getCustomFieldValue(row, field, this.members);
      if (!value) {
        if (field.name === '用例等级') {
          return row.priority;
        }
        if (field.name === '责任人') {
          return row.maintainer;
        }
        if (field.name === '用例状态') {
          return row.status;
        }
      }
      return value;
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

      this.enableOrderDrag = this.condition.orders.length > 0 ? false : true;

      if (this.planId) {
        // param.planId = this.planId;
        this.condition.planId = this.planId;
      }
      if (!this.trashEnable) {
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
        this.condition.filters = {status: ["Trash"]};
      }
      if (this.projectId) {
        this.condition.projectId = this.projectId;
        this.$emit('setCondition', this.condition);
        this.page.result = this.$post(this.buildPagePath('/test/case/list'), this.condition, response => {
          let data = response.data;
          this.page.total = data.itemCount;
          this.page.data = data.listObject;
          this.page.data.forEach(item => {
            if (item.customFields) {
              item.customFields = JSON.parse(item.customFields);
            }
          });
          this.page.data.forEach((item) => {
            try {
              item.tags = JSON.parse(item.tags);
            } catch (e) {
              item.tags = [];
            }

          });
        });
        this.$emit("getTrashList");
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
        this.$get('test/case/get/' + testCase.id, response => {
          let testCase = response.data;
          this.$emit('testCaseEdit', testCase);
        });
      }

    },
    getCase(id) {
      this.$refs.testCasePreview.open();
      this.rowCaseResult.loading = true;
      if (this.rowCase && this.rowCase.id === id) {
        this.$refs.testCasePreview.setData(this.rowCase);
        this.rowCaseResult.loading = false;
        return;
      } else {
        this.rowCase = {};
        this.$refs.testCasePreview.setData({});
      }

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
    handleCopy(testCase) {
      this.$get('test/case/get/' + testCase.id, response => {
        let testCase = response.data;
        testCase.name = 'copy_' + testCase.name;
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
      this.$alert(this.$t('test_track.case.delete_confirm') + '\'' + testCase.name + '\'' + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDeleteToGc(testCase);
          }
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
        id: form.customField.id,
        name: form.customField.name,
        value: form.customField.defaultValue
      };
      this.$post('/test/case/batch/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.refresh();
      });
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open(this.$refs.table.selectRows.size);
    },
    handleBatchMove() {
      this.$refs.testBatchMove.open(this.treeNodes, this.$refs.table.selectIds, this.moduleOptions);
    },
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.valueArr.maintainer = response.data;
      });
    },
    moveSave(param) {
      param.condition = this.condition;
      this.page.result = this.$post('/test/case/batch/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.testBatchMove.close();
        this.refresh();
      });
    }
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
</style>
