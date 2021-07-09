<template>
  <div class="card-container">
    <ms-table-header :tester-permission="true" :condition.sync="condition" @search="initTableData"
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
    <el-table
        v-loading="result.loading"
        class="test-content adjust-table ms-select-all-fixed"
        border
        @select-all="handleSelectAll"
        @filter-change="filter"
        @sort-change="sort"
        @select="handleSelectionChange"
        row-key="id"
        :height="screenHeight"
        style="margin-top: 5px"
        @row-click="showDetail"
        ref="caseTable"
        :data="tableData">

      <el-table-column width="50" type="selection"/>

      <ms-table-header-select-popover v-show="total>0"
                                      :page-size="pageSize > total ? total : pageSize"
                                      :total="total"
                                      :table-data-count-in-page="tableData.length"
                                      @selectPageAll="isSelectDataAll(false)"
                                      @selectAll="isSelectDataAll(true)"/>

      <el-table-column width="40" :resizable="false" align="center">
        <template v-slot:default="scope">
          <show-more-btn :is-show-tool="scope.row.showTool" :is-show="scope.row.showMore" :buttons="buttons"
                         :size="selectDataCounts"/>
        </template>
      </el-table-column>
      <template v-for="(item, index) in tableLabel">
        <el-table-column
            v-if="item.id == 'num'"
            prop="customNum"
            sortable="custom"
            min-width="100"
            :label="$t('commons.id')"
            show-overflow-tooltip
            :key="index"
        >
        </el-table-column>
        <el-table-column
            v-if="item.id == 'name'"
            prop="name"
            min-width="100"
            :label="$t('commons.name')"
            show-overflow-tooltip
            :key="index"
        >
        </el-table-column>
        <el-table-column
            v-if="item.id == 'priority'"
            prop="priority"
            :filters="priorityFilters"
            column-key="priority"
            sortable="custom"
            min-width="120px"
            :label="$t('test_track.case.priority')"
            :key="index">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority" ref="priority"/>
          </template>
        </el-table-column>

        <el-table-column
          v-if="item.id == 'type'"
          prop="type"
          :filters="typeFilters"
          column-key="type"
          min-width="100"
          :label="$t('test_track.case.type')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <type-table-item :value="scope.row.type"/>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'maintainer'"
          prop="maintainer"
          :label="$t('custom_field.case_maintainer')"
          show-overflow-tooltip
          :key="index"
          min-width="120"
        >
        </el-table-column>
        <el-table-column
          v-if="item.id=='nodePath'"
          prop="nodePath"
          min-width="180"
          :label="$t('test_track.case.module')"
          show-overflow-tooltip
          :key="index"
        >
        </el-table-column>

        <el-table-column
            v-if="item.id=='projectName'"
            prop="projectName"
            min-width="180"
            :label="$t('test_track.review.review_project')"
            show-overflow-tooltip
            :key="index">
        </el-table-column>

        <el-table-column
            v-if="item.id=='reviewerName'"
            prop="reviewerName"
            min-width="80"
            :label="$t('test_track.review.reviewer')"
            show-overflow-tooltip
            :key="index"
        >
        </el-table-column>

        <el-table-column
            v-if="item.id=='reviewStatus'"
            :filters="statusFilters"
            column-key="status"
            min-width="100"
            :label="$t('test_track.review_view.execute_result')"
            :key="index">
          <template v-slot:default="scope">
            <span class="el-dropdown-link">
              <review-status :value="scope.row.reviewStatus"/>
            </span>
          </template>
        </el-table-column>

        <el-table-column
            v-if="item.id=='updateTime'"
            sortable
            prop="updateTime"
            :label="$t('commons.update_time')"
            show-overflow-tooltip
            min-width="120"
            :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </template>
      <el-table-column
          min-width="100"
          fixed="right"
          :label="$t('commons.operating')"
      >
        <template slot="header">
          <header-label-operate @exec="customHeader"/>
        </template>
        <template v-slot:default="scope">
          <div>
            <ms-table-operator-button v-permission="['PROJECT_TRACK_CASE:READ+EDIT']" :tip="$t('commons.edit')"
                                      icon="el-icon-edit"
                                      @exec="handleEdit(scope.row)"/>
            <ms-table-operator-button v-permission="['PROJECT_TRACK_REVIEW:READ+RELEVANCE_OR_CANCEL']"
                                      :tip="$t('test_track.plan_view.cancel_relevance')"
                                      icon="el-icon-unlock" type="danger" @exec="handleDelete(scope.row)"/>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <header-custom ref="headerCustom" :initTableData="initTableData" :optionalFields=headerItems
                   :type=type></header-custom>

    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <test-review-test-case-edit
        ref="testReviewTestCaseEdit"
        :search-param="condition"
        @refresh="initTableData"
        :is-read-only="isReadOnly"
        @refreshTable="search"/>


    <batch-edit ref="batchEdit" @batchEdit="batchEdit"
                :type-arr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')"/>
  </div>
</template>

<script>

import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
import MsTableOperator from "../../../../common/components/MsTableOperator";
import MethodTableItem from "../../../common/tableItems/planview/MethodTableItem";
import TypeTableItem from "../../../common/tableItems/planview/TypeTableItem";
import StatusTableItem from "../../../common/tableItems/planview/StatusTableItem";
import PriorityTableItem from "../../../common/tableItems/planview/PriorityTableItem";
import StatusEdit from "../../../plan/view/comonents/StatusEdit";
import ExecutorEdit from "../../../plan/view/comonents/ExecutorEdit";
import MsTipButton from "../../../../common/components/MsTipButton";
import MsTableHeader from "../../../../common/components/MsTableHeader";
import NodeBreadcrumb from "../../../common/NodeBreadcrumb";
import MsTableButton from "../../../../common/components/MsTableButton";
import ShowMoreBtn from "../../../case/components/ShowMoreBtn";
import BatchEdit from "../../../case/components/BatchEdit";
import MsTablePagination from '../../../../common/pagination/TablePagination';
import {hasRoles} from "../../../../../../common/js/utils";
import {TEST_CASE_CONFIGS} from "../../../../common/components/search/search-components";
import {ROLE_TEST_MANAGER, ROLE_TEST_USER, TEST_CASE_REVIEW_CASE_LIST} from "../../../../../../common/js/constants";
import TestReviewTestCaseEdit from "./TestReviewTestCaseEdit";
import ReviewStatus from "@/business/components/track/case/components/ReviewStatus";
import {
  _filter,
  _handleSelect,
  _handleSelectAll,
  _sort,
  buildBatchParam, deepClone,
  getLabel,
  getSelectDataCounts,
  initCondition,
  setUnSelectIds,
  toggleAllSelection
} from "@/common/js/tableUtils";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import {Test_Case_Review_Case_List} from "@/business/components/common/model/JsonData";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";

export default {
  name: "TestReviewTestCaseList",
  components: {
    HeaderLabelOperate,
    HeaderCustom,
    MsTableOperatorButton, MsTableOperator, MethodTableItem, TypeTableItem,
    StatusTableItem, PriorityTableItem, StatusEdit,
    ExecutorEdit, MsTipButton, TestReviewTestCaseEdit, MsTableHeader,
    NodeBreadcrumb, MsTableButton, ShowMoreBtn, BatchEdit, MsTablePagination, ReviewStatus, MsTableHeaderSelectPopover
  },
  data() {
    return {
      type: TEST_CASE_REVIEW_CASE_LIST,
      headerItems: Test_Case_Review_Case_List,
      screenHeight: 'calc(100vh - 270px)',
      tableLabel: [],
      result: {},
      condition: {},
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selectRows: new Set(),
      testReview: {},
      isReadOnly: false,
      isTestManagerOrTestUser: false,
      selectDataCounts: 0,
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
      statusFilters: [
        {text: this.$t('test_track.review.prepare'), value: 'Prepare'},
        {text: this.$t('test_track.review.pass'), value: 'Pass'},
        {text: this.$t('test_track.review.un_pass'), value: 'UnPass'},
      ],
      showMore: false,
      buttons: [
        {
          name: this.$t('test_track.case.batch_edit_case'), handleClick: this.handleEditBatch
        },
        {
          name: this.$t('test_track.case.batch_unlink'), handleClick: this.handleDeleteBatch
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
    };
  },
  props: {
    reviewId: {
      type: String
    },
  },
  watch: {
    reviewId() {
      this.refreshTableAndReview();
    },
    selectNodeIds() {
      this.search();
    }
  },
  computed: {
    selectNodeIds() {
      return this.$store.state.testReviewSelectNodeIds;
    }
  },
  mounted() {
    this.refreshTableAndReview();
    this.isTestManagerOrTestUser = true;
  },
  methods: {
    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
    },
    initTableData() {
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
      this.condition.nodeIds = this.selectNodeIds;
      if (this.reviewId) {
        this.result = this.$post(this.buildPagePath('/test/review/case/list'), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.selectRows.clear();

          this.$nextTick(function () {
            if (this.$refs.caseTable) {
              setTimeout(this.$refs.caseTable.doLayout, 200);
            }
            this.checkTableRowIsSelect();
          });
        });
      }
      getLabel(this, TEST_CASE_REVIEW_CASE_LIST);

    },
    checkTableRowIsSelect() {
      //如果默认全选的话，则选中应该选中的行
      if (this.condition.selectAll) {
        let unSelectIds = this.condition.unSelectIds;
        this.tableData.forEach(row => {
          if (unSelectIds.indexOf(row.id) < 0) {
            this.$refs.caseTable.toggleRowSelection(row, true);

            //默认全选，需要把选中对行添加到selectRows中。不然会影响到勾选函数统计
            if (!this.selectRows.has(row)) {
              this.$set(row, "showMore", true);
              this.selectRows.add(row);
            }
          } else {
            //不勾选的行，也要判断是否被加入了selectRow中。加入了的话就去除。
            if (this.selectRows.has(row)) {
              this.$set(row, "showMore", false);
              this.selectRows.delete(row);
            }
          }
        });
      }
    },
    showDetail(row, event, column) {
      this.isReadOnly = true;
      this.$refs.testReviewTestCaseEdit.openTestCaseEdit(row);
    },
    refresh() {
      this.condition = {components: TEST_CASE_CONFIGS};
      this.selectRows.clear();
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
      // this.$post('/test/case/review/edit', param);
    },
    search() {
      this.initTableData();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleEdit(testCase, index) {
      this.isReadOnly = false;
      this.$refs.testReviewTestCaseEdit.openTestCaseEdit(testCase);
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
            let param = buildBatchParam(this);
            param.reviewId = this.reviewId;
            this.$post('/test/review/case/batch/delete', param, () => {
              this.selectRows.clear();
              this.$emit("refresh");
              this.$success(this.$t('test_track.cancel_relevance_success'));
            });
          }
        }
      });
    },
    _handleDelete(testCase) {
      let testCaseId = testCase.id;
      this.$post('/test/review/case/delete', {id: testCaseId, reviewId: testCase.reviewId}, () => {
        this.$emit("refresh");
        this.$success(this.$t('test_track.cancel_relevance_success'));
      });
    },
    handleEditBatch() {
      this.$refs.batchEdit.open(this.selectRows.size);
    },
    batchEdit(form) {
      let reviewId = this.reviewId;
      let param = buildBatchParam(this);
      param[form.type] = form.value;
      param.ids = Array.from(this.selectRows).map(row => row.caseId);
      param.reviewId = reviewId;
      this.$post('/test/review/case/batch/edit/status', param, () => {
        this.selectRows.clear();
        this.status = '';
        this.$post('/test/case/review/edit/status/' + reviewId);
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
      });
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.tableData.forEach(item => {
          this.$set(item, "showMore", true);
          this.selectRows.add(item);
        });
      } else {
        this.selectRows.clear();
        this.tableData.forEach(row => {
          this.$set(row, "showMore", false);
        });
      }
    },
    handleSelectionChange(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      // if (this.selectRows.has(row)) {
      //   this.$set(row, "showMore", false);
      //   this.selectRows.delete(row);
      // } else {
      //   this.$set(row, "showMore", true);
      //   this.selectRows.add(row);
      // }
    },
    openTestReport() {
      this.$refs.testReportTemplateList.open(this.reviewId);
    },
    getTestReviewById() {
      if (this.reviewId) {
        this.$get('/test/case/review/get/' + this.reviewId, response => {
          this.testReview = response.data;
          this.refreshTestReviewRecent();
        });
      }
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTableData();
    },
    startReview() {
      if (this.tableData.length !== 0) {
        this.isReadOnly = false;
        this.$refs.testReviewTestCaseEdit.openTestCaseEdit(this.tableData[0]);
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

  }
};
</script>

<style scoped>
/deep/ .table-title {
  height: 0px;
  font-weight: bold;
  font-size: 0px;
}
</style>

