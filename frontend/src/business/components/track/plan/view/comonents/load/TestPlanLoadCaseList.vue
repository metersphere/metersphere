<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">
      <template v-slot:header>
        <test-plan-load-case-list-header
          :condition="condition"
          :plan-id="planId"
          @refresh="initTable"
          @relevanceCase="$emit('relevanceCase')"
        />
      </template>

      <el-table v-loading="result.loading"
                border
                :data="tableData" row-key="id" class="test-content adjust-table"
                @select-all="handleSelectAll"
                @filter-change="filter"
                @sort-change="sort"
                @select="handleSelectionChange" :height="screenHeight">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore && !isReadOnly" :buttons="buttons" :size="selectRows.size"/>
          </template>
        </el-table-column>

<!--        <el-table-column prop="num" label="ID" show-overflow-tooltip/>-->
        <el-table-column
          prop="caseName"
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>
<!--        <el-table-column-->
<!--          prop="projectName"-->
<!--          :label="$t('load_test.project_name')"-->
<!--          width="150"-->
<!--          show-overflow-tooltip>-->
<!--        </el-table-column>-->
        <el-table-column
          prop="userName"
          :label="$t('load_test.user_name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          sortable
          prop="createTime"
          :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          sortable
          prop="updateTime"
          :label="$t('commons.update_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          column-key="status"
          :filters="statusFilters"
          :label="$t('commons.status')">
          <template v-slot:default="{row}">
            <ms-performance-test-status :row="row"/>
          </template>
        </el-table-column>
        <el-table-column v-if="!isReadOnly" :label="$t('commons.operating')" align="center">
          <template v-slot:default="scope">
            <ms-table-operator-button class="run-button" :is-tester-permission="true" :tip="$t('api_test.run')" icon="el-icon-video-play"
                                      @exec="run(scope.row)" v-tester/>
            <ms-table-operator-button :is-tester-permission="true" :tip="$t('test_track.plan_view.cancel_relevance')"
                                      icon="el-icon-unlock" type="danger" @exec="handleDelete(scope.row)" v-tester/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>
  </div>
</template>

<script>
import TestPlanLoadCaseListHeader from "@/business/components/track/plan/view/comonents/load/TestPlanLoadCaseListHeader";
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import {_filter, _sort} from "@/common/js/utils";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsPerformanceTestStatus from "@/business/components/performance/test/PerformanceTestStatus";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
export default {
  name: "TestPlanLoadCaseList",
  components: {
    TestPlanLoadCaseListHeader,
    ShowMoreBtn,
    MsTablePagination,
    MsPerformanceTestStatus,
    MsTableOperatorButton
  },
  data() {
    return {
      condition: {},
      result: {},
      tableData: [],
      selectRows: new Set(),
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: document.documentElement.clientHeight - 330,//屏幕高度
      buttons: [
        {
          name: "批量编辑用例", handleClick: this.handleBatchEdit
        },
        {
          name: "批量取消关联", handleClick: this.handleDeleteBatch
        },
        {
          name: "批量执行用例", handleClick: this.handleRunBatch
        }
      ],
      statusFilters: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
    }
  },
  props: {
    selectNodeIds: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    planId: String
  },
  created() {
    this.initTable();
  },
  watch: {
    selectNodeIds() {
      this.initTable();
    },
    planId() {
      this.initTable();
    }
  },
  methods: {
    initTable() {
      this.$post("/test/plan/load/case/list/" + this.currentPage + "/" + this.pageSize, {testPlanId: this.planId}, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
      })
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
        })
      }
    },
    handleSelectionChange(selection, row) {
      if (this.selectRows.has(row)) {
        this.$set(row, "showMore", false);
        this.selectRows.delete(row);
      } else {
        this.$set(row, "showMore", true);
        this.selectRows.add(row);
      }
    },
    handleBatchEdit() {

    },
    handleDeleteBatch() {

    },
    handleRunBatch() {

    },
    run() {

    },
    handleDelete(loadCase) {
      this.$get('/test/plan/load/case/delete/' + loadCase.id, () => {
        this.$success(this.$t('test_track.cancel_relevance_success'));
        this.$emit('refresh');
        this.initTable();
      });
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTableData();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
  }
}
</script>

<style scoped>
/deep/ .run-button {
  background-color: #409EFF;
  border-color: #409EFF;
}
</style>
