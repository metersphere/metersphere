<template>
  <div>
    <el-button type="primary" @click="relateTestCase">{{$t('test_track.review_view.relevance_case')}}</el-button>

    <ms-table
      v-loading="result.loading"
      :enable-selection="false"
      :operators="operators"
      :data="tableData"
      :screen-height="null"
      @refresh="initTableData"
      ref="table">

      <ms-table-column
        :label="$t('commons.id')"
        prop="num">
      </ms-table-column>

      <ms-table-column
        :label="$t('commons.name')"
        prop="name">
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.case.priority')"
        prop="name">
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.priority" ref="priority"/>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.case.type')"
        prop="type">
        <template v-slot:default="scope">
          <type-table-item :value="scope.row.type"/>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.case.module')"
        prop="nodePath">
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.plan.plan_project')"
        prop="projectName">
      </ms-table-column>

    </ms-table>

    <test-case-relate-list
      @refresh="initTableData"
      @save="handleRelate"
      :test-case-contain-ids="testCaseContainIds"
      ref="testCaseRelevance"/>
  </div>

</template>

<script>
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "@/business/components/track/common/tableItems/planview/TypeTableItem";
import TestCaseRelateList from "@/business/components/track/issue/TestCaseRelateList";
export default {
  name: "TestCaseIssueList",
  components: {TestCaseRelateList, TypeTableItem, PriorityTableItem, MsTableColumn, MsTable},
  data() {
    return {
      result: {},
      tableData: [],
      operators: [
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete
        }
      ],
    };
  },
  props: {
    issuesId: String,
    testCaseContainIds: Set,
  },
  methods: {
    handleDelete(item, index) {
      this.testCaseContainIds.delete(item.id);
      this.tableData.splice(index, 1);
    },
    initTableData() {
      this.tableData = [];
      let condition = {
        issuesId: this.issuesId
      };
      if (this.issuesId) {
        this.result = this.$post('test/case/issues/list', condition, response => {
          this.tableData = response.data;
          this.tableData.forEach(item => {
            this.testCaseContainIds.add(item.id);
          });
          this.$refs.table.reloadTable();
        });
      }
    },
    relateTestCase() {
      this.$refs.testCaseRelevance.open();
    },
    handleRelate(selectRows) {
      let selectData = Array.from(selectRows);
      selectData.forEach(item => {
        if (item.id) {
          this.testCaseContainIds.add(item.id);
        }
      });
      this.tableData.push(...selectData);
    },
  }
}
</script>

<style scoped>

</style>
