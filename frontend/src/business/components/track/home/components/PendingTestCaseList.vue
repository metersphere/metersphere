<template>

  <home-base-component :title="'待测试'">

    <el-table
      row-key="id"
      :data="tableData">

      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>

      <el-table-column
        prop="priority"
        :label="$t('test_track.case.priority')">
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.priority" ref="priority"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="type"
        :label="$t('test_track.case.type')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <type-table-item :value="scope.row.type"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="status"
        :label="$t('test_track.plan_view.execute_result')">
        <template v-slot:default="scope">
          <status-table-item :value="scope.row.status"/>
        </template>
      </el-table-column>

    </el-table>

  </home-base-component>

</template>

<script>
    import HomeBaseComponent from "./HomeBaseComponent";
    import PriorityTableItem from "../../common/tableItems/planview/PriorityTableItem";
    import TypeTableItem from "../../common/tableItems/planview/TypeTableItem";
    import StatusTableItem from "../../common/tableItems/planview/StatusTableItem";
    export default {
      name: "PendingTestCaseList",
      components: {StatusTableItem, TypeTableItem, PriorityTableItem, HomeBaseComponent},
      data() {
        return {
          tableData: []
        }
      },
      mounted() {
        this.initTableData();
      },
      methods: {
        initTableData() {
          this.result = this.$post('/test/plan/case/pending/5', {}, response => {
            this.tableData = response.data;
          });
        },
      }
    }
</script>

<style scoped>

</style>
