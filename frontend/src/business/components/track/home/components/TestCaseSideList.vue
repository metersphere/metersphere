<template>

  <home-base-component :title="title">

    <el-table
      row-key="id"
      @row-click="editTestCase"
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
  import StatusTableItem from "../../common/tableItems/planview/StatusTableItem";
  import TypeTableItem from "../../common/tableItems/planview/TypeTableItem";
  import PriorityTableItem from "../../common/tableItems/planview/PriorityTableItem";
  export default {
    name: "TestCaseSideList",
    components: {PriorityTableItem, TypeTableItem, StatusTableItem, HomeBaseComponent},
    data() {
      return {
        tableData: [],
      }
    },
    props: {
      type: {
        type: String
      },
      title: {
        type: String
      },
    },
    mounted() {
      this.initTableData();
    },
    methods: {
      initTableData() {
        this.result = this.$post('/test/plan/case/' + this.type + '/5', {}, response => {
          this.tableData = response.data;
        });
      },
      editTestCase(row, event, column) {
        this.$router.push('/track/plan/view/edit/' + row.caseId)
      }
    }
  }
</script>

<style scoped>

</style>
