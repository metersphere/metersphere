<template>
  <div class="container">
    <el-input
      v-model="keyword"
      :placeholder="$t('test_track.report.search_placeholder')"
      size="small"
      prefix-icon="el-icon-search"
      @keyup.enter.native="searchTestCase"
    />
    <el-table row-key="id" :data="currentPageTestCase" max-height="700">
      <el-table-column
        prop="num"
        :label="$t('commons.id')"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <span v-if="isTemplate || isShare">
            {{ row.isCustomNum ? row.customNum : row.num }}
          </span>
          <el-link
            v-else
            type="primary"
            @click="redirectFunctionCaseEditPage(row.caseId, row.projectId)"
          >
            {{ row.isCustomNum ? row.customNum : row.num }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip
      >
      </el-table-column>
      <el-table-column
        prop="priority"
        column-key="priority"
        :label="$t('test_track.case.priority')"
      >
        <template #default="scope">
          <priority-table-item :value="scope.row.priority" ref="priority" />
        </template>
      </el-table-column>

      <el-table-column
        prop="projectName"
        :label="$t('test_track.case.project_name')"
        show-overflow-tooltip
      >
      </el-table-column>

      <el-table-column
        prop="executorName"
        :label="$t('test_track.plan_view.executor')"
      >
      </el-table-column>

      <el-table-column
        prop="maintainerName"
        :label="$t('test_track.plan.plan_principal')"
      >
      </el-table-column>

      <el-table-column
        prop="status"
        column-key="status"
        :label="$t('test_track.plan_view.execute_result')"
      >
        <template #default="scope">
          <status-table-item :value="scope.row.status" />
        </template>
      </el-table-column>

      <el-table-column
        prop="updateTime"
        :label="$t('commons.update_time')"
        show-overflow-tooltip
      >
        <template #default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
    </el-table>
    <div style="float: right; padding-top: 10px">
      <el-pagination
        :current-page.sync="currentPage"
        :page-size="20"
        layout="prev, pager, next"
        :total="filterCases.length"
        background
        @current-change="handlePageChange"
        @prev-click="handlePageChange"
        @next-click="handlePageChange"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import { getEditSimpleTestCase } from "@/api/testCase";
import { openCaseEdit } from "@/business/case/test-case";

export default {
  name: "FunctionalCases",
  components: {
    StatusTableItem,
    MethodTableItem,
    TypeTableItem,
    PriorityTableItem,
  },
  props: {
    planId: String,
    isTemplate: Boolean,
    isShare: Boolean,
    report: {},
    shareId: String,
    isAll: Boolean,
    isDb: Boolean,
    filterStatus: String,
    allTestCase: {
      type: Array,
      default() {
        return [];
      },
    },
  },
  data() {
    return {
      keyword: "",
      filterCases: [],
      currentPage: 1,
      currentPageTestCase: [],
    };
  },
  watch: {
    allTestCase(val) {
      this.filterCases = val;
      this.handlePageChange(this.currentPage);
    },
  },
  methods: {
    redirectFunctionCaseEditPage(caseId, projectId) {
      getEditSimpleTestCase(caseId)
        .then((r) => {
          openCaseEdit({ caseId: caseId, projectId: projectId }, this);
        })
        .catch(() => {});
    },
    searchTestCase() {
      this.currentPage = 1;
      this.filterCases = this.allTestCase.filter(
        (e) =>
          e.name.includes(this.keyword) || e.customNum.includes(this.keyword)
      );
      this.handlePageChange(this.currentPage);
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.currentPageTestCase = this.filterCases.slice(
        (page - 1) * 20,
        page * 20
      );
    },
  },
};
</script>

<style scoped></style>
