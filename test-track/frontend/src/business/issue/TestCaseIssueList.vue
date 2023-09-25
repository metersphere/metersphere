<template>
  <div>
    <el-button type="primary" @click="relateTestCase">{{
      $t("test_track.review_view.relevance_case")
    }}</el-button>

    <ms-table
      v-loading="result.loading"
      :enable-selection="false"
      :operators="operators"
      :data="tableData"
      :screen-height="null"
      @refresh="initTableData"
      ref="table"
    >
      <ms-table-column :label="$t('commons.id')" prop="num"> </ms-table-column>

      <ms-table-column :label="$t('commons.name')" prop="name">
      </ms-table-column>

      <ms-table-column :label="$t('test_track.case.priority')" prop="name">
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.priority" ref="priority" />
        </template>
      </ms-table-column>

      <ms-table-column :label="$t('test_track.case.type')" prop="type">
        <template v-slot:default="scope">
          <type-table-item :value="scope.row.type" />
        </template>
      </ms-table-column>

      <ms-table-column :label="$t('test_track.case.module')" prop="nodePath">
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.plan.plan_project')"
        prop="projectName"
      >
      </ms-table-column>
    </ms-table>

    <test-case-relate-list
      :test-case-contain-ids="testCaseContainIds"
      @refresh="initTableData"
      @save="handleRelate"
      ref="testCaseRelevance"
    />
  </div>
</template>

<script>
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "@/business/common/tableItems/planview/TypeTableItem";
import TestCaseRelateList from "@/business/issue/TestCaseRelateList";
import { getTestCaseIssueList } from "@/api/testCase";

export default {
  name: "TestCaseIssueList",
  components: {
    TestCaseRelateList,
    TypeTableItem,
    PriorityTableItem,
    MsTableColumn,
    MsTable,
  },
  data() {
    return {
      result: {
        loading: false,
      },
      tableData: [],
      deleteIds: new Set(),
      addIds: new Set(),
      testCaseContainIds: new Set(),
      operators: [
        {
          tip: this.$t("commons.delete"),
          icon: "el-icon-delete",
          type: "danger",
          exec: this.handleDelete,
        },
      ],
      cacheAddRows: [], // 缓存关联用例信息
    };
  },
  props: {
    issuesId: String,
  },
  watch: {
    issuesId: {
      handler(val) {
        if (val) {
          this.initTableData();
        }
      },
      immediate: true,
    },
  },
  methods: {
    handleDelete(item, index) {
      this.testCaseContainIds.delete(item.id);
      this.tableData.splice(index, 1);
      this.deleteIds.add(item.id);
      this.cacheAddRows.splice(index, 1);
    },
    clear() {
      this.addIds.clear();
      this.deleteIds.clear();
    },
    initTableData() {
      this.tableData = [];
      let condition = {
        issuesId: this.issuesId,
      };
      if (this.issuesId) {
        this.result.loading = true;
        getTestCaseIssueList(condition).then((response) => {
          this.tableData = response.data;
          this.tableData.forEach((item) => {
            this.testCaseContainIds.add(item.id);
          });
          this.$refs.table.reloadTable();
          if (this.cacheAddRows.length > 0) {
            this.tableData.push(...this.cacheAddRows);
          }
          this.result.loading = false;
        });
      }
    },
    relateTestCase() {
      this.$refs.testCaseRelevance.open();
    },
    handleRelate(selectRows) {
      let selectData = Array.from(selectRows);
      selectRows.forEach((i) => {
        if (i.id) {
          this.testCaseContainIds.add(i.id);
        }
        this.deleteIds.delete(i.id);
        this.addIds.add(i.id);
      });
      this.tableData.push(...selectData);
      this.cacheAddRows.push(...selectData);
    },
  },
};
</script>

<style scoped></style>
