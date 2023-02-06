<template>
  <div class="card-container">
    <ms-table-header :tester-permission="true" :condition.sync="condition" @search="search"
                     :show-create="false" :tip="$t('commons.search_by_name_or_id')">
      <template v-slot:button>
        <ms-table-button v-permission="['PROJECT_TRACK_REVIEW:READ+REVIEW']" icon="el-icon-video-play"
                         :content="$t('test_track.review_view.start_review')" @click="startReview"/>
        <ms-table-button v-permission="['PROJECT_TRACK_REVIEW:READ+RELEVANCE_OR_CANCEL']" icon="el-icon-connection"
                         :content="$t('test_track.review_view.relevance_case')"
                         @click="$emit('openTestReviewRelevanceDialog')"/>

      </template>
    </ms-table-header>

    <executor-edit ref="executorEdit" :select-ids="new Set(Array.from(this.selectRows).map(row => row.id))"
                   @refresh="initTableData"/>
    <status-edit ref="statusEdit" :plan-id="reviewId"
                 :select-ids="new Set(Array.from(this.selectRows).map(row => row.id))" @refresh="initTableData"/>
    <ms-table
      v-loading="result.loading"
      :field-key="tableHeaderKey"
      :data="tableData"
      :condition="condition"
      :total="total"
      :page-size.sync="pageSize"
      :operators="operators"
      :screen-height="screenHeight"
      :batch-operators="buttons"
      @handlePageChange="initTableData"
      @handleRowClick="showDetail"
      row-key="id"
      :fields.sync="fields"
      :remember-order="true"
      :enable-order-drag="enableOrderDrag"
      :row-order-func="editTestReviewTestCaseOrder"
      :row-order-group-id="reviewId"
      @order="initTableData"
      @filter="search"
      ref="table"
    >
      <span v-for="item in fields" :key="item.key">
        <ms-table-column
          v-if="item.id == 'num'"
          prop="customNum"
          sortable="custom"
          :fields-width="fieldsWidth"
          :label="$t('commons.id')"
          min-width="120px"/>

        <ms-table-column
          prop="name"
          :field="item"
          sortable="custom"
          :fields-width="fieldsWidth"
          :label="$t('commons.name')"
          min-width="120px"/>

        <ms-table-column
          v-if="versionEnable"
          prop="versionId"
          :field="item"
          :filters="versionFilters"
          :fields-width="fieldsWidth"
          :label="$t('commons.version')"
          min-width="120px">
          <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="priority"
          :field="item"
          :fields-width="fieldsWidth"
          :filters="priorityFilters"
          sortable="custom"
          min-width="120px"
          :label="$t('test_track.case.priority')">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority" ref="priority"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="maintainerName"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('custom_field.case_maintainer')"
          min-width="120px"/>

        <ms-table-column
          prop="nodePath"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.case.module')"
          min-width="120px"/>

        <ms-table-column
          prop="projectName"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.plan.plan_project')"
          min-width="120px"/>

        <ms-table-column
          prop="reviewerName"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.review.reviewer')"
          min-width="120px"/>

        <test-case-review-status-table-item
          :field="item"
          :fields-width="fieldsWidth"/>

        <ms-table-column
          sortable
          prop="updateTime"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.update_time')"
          min-width="120px">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>
      </span>

    </ms-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <test-review-test-case-edit
      ref="testReviewTestCaseEdit"
      :search-param="condition"
      :page-num="currentPage"
      :page-size="pageSize"
      :next-page-data="nextPageData"
      :pre-page-data="prePageData"
      :test-cases="tableData"
      :is-read-only="isReadOnly"
      :total="total"
      @nextPage="nextPage"
      @prePage="prePage"
      @refresh="initTableData"
      @refreshTable="initTableData"/>


    <batch-edit ref="batchEdit" @batchEdit="batchEdit"
                :type-arr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')"/>
  </div>
</template>

<script>

import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MethodTableItem from "@/business/common/tableItems/planview/MethodTableItem";
import TypeTableItem from "@/business/common/tableItems/planview/TypeTableItem";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import StatusEdit from "@/business/plan/view/comonents/StatusEdit";
import ExecutorEdit from "@/business/plan/view/comonents/ExecutorEdit";
import MsTipButton from "metersphere-frontend/src/components/MsTipButton";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import NodeBreadcrumb from "@/business/common/NodeBreadcrumb";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import ShowMoreBtn from "metersphere-frontend/src/components/table/ShowMoreBtn";
import BatchEdit from "@/business/case/components/BatchEdit";
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import {TEST_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import TestReviewTestCaseEdit from "./TestReviewTestCaseEdit";
import ReviewStatus from "@/business/case/components/ReviewStatus";
import {
  _handleSelectAll, buildBatchParam, deepClone, getCustomTableWidth, getLastTableSortField, getSelectDataCounts,
  getTableHeaderWithCustomFields, initCondition, toggleAllSelection} from "metersphere-frontend/src/utils/tableUtils";
import HeaderCustom from "metersphere-frontend/src/components/head/HeaderCustom";
import {Test_Case_Review_Case_List} from "@/business/model/JsonData";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTableHeaderSelectPopover from "metersphere-frontend/src/components/table/MsTableHeaderSelectPopover";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import {editTestReviewTestCaseOrder, getTestReviewTestCase} from "@/api/testCase";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import TestCaseReviewStatusTableItem from "@/business/common/tableItems/TestCaseReviewStatusTableItem";
import {getProjectConfig} from "@/api/project";
import {
  batchDeleteTestReviewCase,
  batchEditTestReviewCaseStatus,
  deleteTestReviewCase, getTesReviewById
} from "@/api/test-review";
import {useStore} from "@/store";
import {getVersionFilters} from "@/business/utils/sdk-utils";

export default {
  name: "TestReviewTestCaseList",
  components: {
    TestCaseReviewStatusTableItem,
    HeaderLabelOperate,
    HeaderCustom,
    MsTableOperatorButton, MsTableOperator, MethodTableItem, TypeTableItem,
    StatusTableItem, PriorityTableItem, StatusEdit,
    ExecutorEdit, MsTipButton, TestReviewTestCaseEdit, MsTableHeader,
    NodeBreadcrumb, MsTableButton, ShowMoreBtn, BatchEdit,
    MsTablePagination, ReviewStatus, MsTableHeaderSelectPopover,
    MsTableColumn,
    MsTable,
  },
  data() {
    return {
      fields: [],
      fieldsWidth: getCustomTableWidth('TEST_CASE_REVIEW_FUNCTION_TEST_CASE'),
      headerItems: Test_Case_Review_Case_List,
      screenHeight: 'calc(100vh - 240px)',
      tableLabel: [],
      result: {},
      condition: {},
      tableData: [],
      nextPageData: null,
      prePageData: null,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      pageCount: 0,
      enableOrderDrag: true,
      selectRows: new Set(),
      testReview: {},
      isReadOnly: false,
      isTestManagerOrTestUser: false,
      selectDataCounts: 0,
      tableHeaderKey: 'TEST_CASE_REVIEW_FUNCTION_TEST_CASE',
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
      showMore: false,
      buttons: [
        {
          name: this.$t('test_track.case.batch_edit_case'),
          handleClick: this.handleEditBatch,
          permissions: ['PROJECT_TRACK_REVIEW:READ+EDIT']
        },
        {
          name: this.$t('test_track.case.batch_unlink'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_TRACK_REVIEW:READ+RELEVANCE_OR_CANCEL']
        }
      ],
      operators: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit,
          permissions: ['PROJECT_TRACK_REVIEW:READ+EDIT']
        },
        {
          tip: this.$t('test_track.plan_view.cancel_relevance'), icon: "el-icon-unlock", type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_TRACK_REVIEW:READ+RELEVANCE_OR_CANCEL']
        }
      ],
      typeArr: [
        {id: 'status', name: this.$t('test_track.review_view.execute_result')},
      ],
      valueArr: {
        status: [
          {name: this.$t('test_track.review.prepare'), id: 'Prepare'},
          {name: this.$t('test_track.review.pass'), id: 'Pass'},
          {name: this.$t('test_track.review.un_pass'), id: 'UnPass'},
        ]
      },
      versionFilters: []
    };
  },
  props: {
    reviewId: {
      type: String
    },
    currentVersion: {
      type: String
    },
    versionEnable: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    reviewId() {
      useStore().testReviewSelectNodeIds = [];
      this.refreshTableAndReview();
    },
    selectNodeIds() {
      this.initTableData();
    },
    condition() {
      this.$emit('setCondition', this.condition);
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTableData();
    },
    pageCount() {
      this.currentPage = 1;
    }
  },
  computed: {
    selectNodeIds() {
      return useStore().testReviewSelectNodeIds;
    },
    editTestReviewTestCaseOrder() {
      return editTestReviewTestCaseOrder;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  created() {
    this.condition.orders = getLastTableSortField(this.tableHeaderKey);
    this.pageCount = Math.ceil(this.total / this.pageSize);
  },
  mounted() {
    this.$emit('setCondition', this.condition);
    this.refreshTableAndReview();
    this.isTestManagerOrTestUser = true;
    this.initTableHeader();
    this.getVersionOptions();
    this.getProject();
  },
  methods: {
    nextPage() {
      this.currentPage++;
      this.initTableData(() => {
        this.$refs.testReviewTestCaseEdit.openTestCaseEdit(this.tableData[0], this.tableData);
      });
    },
    prePage() {
      this.currentPage--;
      this.initTableData(() => {
        this.$refs.testReviewTestCaseEdit.openTestCaseEdit(this.tableData[this.tableData.length - 1], this.tableData);
      });
    },
    initTableHeader() {
      this.fields = getTableHeaderWithCustomFields(this.tableHeaderKey, []);
    },
    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
    },
    initTableData(callback) {
      initCondition(this.condition, this.condition.selectAll);
      if (this.reviewId) {
        this.condition.reviewId = this.reviewId;
      }
      if (this.clickType) {
        if (this.status == 'default') {
          this.condition.status = this.clickType;
        } else {
          this.condition.status = null;
        }
        this.status = 'all';
      }
      this.enableOrderDrag = (this.condition.orders && this.condition.orders.length) > 0 ? false : true;

      this.condition.nodeIds = this.selectNodeIds;
      if (this.reviewId) {
        getTestReviewTestCase(this.currentPage, this.pageSize, this.condition)
          .then((response) => {
            this.total = response.data.itemCount;
            this.pageCount = Math.ceil(this.total / this.pageSize);
            this.tableData = response.data.listObject;
            this.getPreData();
            if (callback && callback instanceof Function) {
              callback();
            }
          });
        this.getNexPageData();
      }
    },
    getNexPageData() {
      getTestReviewTestCase(this.currentPage * this.pageSize + 1, 1, this.condition)
        .then((response) => {
          if (response.data.listObject && response.data.listObject.length > 0) {
            this.nextPageData = {
              name: response.data.listObject[0].name
            }
          } else {
            this.nextPageData = null;
          }
        });
    },
    getPreData() {
      // 如果不是第一页并且只有一条数据时，需要调用
      if (this.currentPage > 1 && this.tableData.length === 1) {
        getTestReviewTestCase((this.currentPage - 1) * this.pageSize, 1, this.condition)
          .then((response) => {
            if (response.data.listObject && response.data.listObject.length > 0) {
              this.prePageData = {
                name: response.data.listObject[0].name
              }
            } else {
              this.prePageData = null;
            }
          })
      }
    },
    showDetail(row, event, column) {
      this.isReadOnly = true;
      this.$refs.testReviewTestCaseEdit.openTestCaseEdit(row, this.tableData);
    },
    refresh() {
      this.condition = {components: TEST_CASE_CONFIGS};
      this.tableClear();
      this.$emit('refresh');
    },
    refreshTableAndReview() {
      this.getTestReviewById();
      this.initTableData();
    },
    refreshTestReviewRecent() {
      let param = {};
      param.id = this.reviewId;
      param.updateTime = Date.now();
    },
    search() {
      this.initTableData();
      this.$emit('search');
    },
    getProject() {
      getProjectConfig(this.projectId, "/CASE_CUSTOM_NUM")
        .then(result => {
          let store = useStore();
          let data = result.data;
          if (data && data.typeValue === 'true') {
            store.currentProjectIsCustomNum = true;
          } else {
            store.currentProjectIsCustomNum = false;
          }
        });
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleEdit(testCase, index) {
      this.isReadOnly = false;
      this.$refs.testReviewTestCaseEdit.openTestCaseEdit(testCase, this.tableData);
    },
    handleDelete(testCase) {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + ' ' + testCase.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(testCase);
          }
        }
      });
    },
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this, this.$refs.table.selectIds);
            param.ids = this.$refs.table.selectIds;
            param.reviewId = this.reviewId;
            batchDeleteTestReviewCase(param)
              .then(() => {
                this.tableClear();
                this.$emit("refresh");
                this.$success(this.$t('test_track.cancel_relevance_success'));
              });
          }
        }
      });
    },
    _handleDelete(testCase) {
      let testCaseId = testCase.id;
      deleteTestReviewCase({id: testCaseId, reviewId: testCase.reviewId})
        .then(() => {
          this.$emit("refresh");
          this.$success(this.$t('test_track.cancel_relevance_success'));
        });
    },
    handleEditBatch() {
      this.$refs.batchEdit.open(this.condition.selectAll ? this.total : this.$refs.table.selectRows.size);
    },
    batchEdit(form) {
      let reviewId = this.reviewId;
      let param = buildBatchParam(this, this.$refs.table.selectIds);
      param.ids = Array.from(this.$refs.table.selectRows).map(row => row.caseId);
      param[form.type] = form.value;
      param.reviewId = reviewId;
      batchEditTestReviewCaseStatus(param)
        .then(() => {
          this.tableClear();
          this.status = '';
          this.$post('/test/case/review/edit/status/' + reviewId);
          this.$success(this.$t('commons.save_success'));
          this.$emit('refresh');
        })
    },
    openTestReport() {
      this.$refs.testReportTemplateList.open(this.reviewId);
    },
    getTestReviewById() {
      if (this.reviewId) {
        getTesReviewById(this.reviewId)
          .then((response) => {
            this.testReview = response.data;
            this.refreshTestReviewRecent();
          })
      }
    },
    startReview() {
      if (this.tableData.length !== 0) {
        this.isReadOnly = false;
        this.$refs.testReviewTestCaseEdit.openTestCaseEdit(this.tableData[0], this.tableData);
      } else {
        this.$warning(this.$t('test_track.review.no_link_case'));
      }
    },
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      //设置勾选
      toggleAllSelection(this.$refs.caseTable, this.tableData, this.selectRows);
      //显示隐藏菜单
      _handleSelectAll(this, this.tableData, this.tableData, this.selectRows);
      //设置未选择ID(更新)
      this.condition.unSelectIds = [];
      //更新统计信息
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    tableClear() {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
    },
    getVersionOptions() {
      if (hasLicense()) {
        getVersionFilters(getCurrentProjectID())
          .then(r => this.versionFilters = r.data);
      }
    },
  }
};
</script>

<style scoped>
:deep(.table-title) {
  height: 0px;
  font-weight: bold;
  font-size: 0px;
}

.ms-table-header {
  margin-bottom: 10px;
}

</style>

