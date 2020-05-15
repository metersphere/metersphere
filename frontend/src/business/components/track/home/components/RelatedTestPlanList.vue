<template>
  <home-base-component :title="'我的计划'">

    <el-table
      :data="tableData"
      @row-click="intoPlan">
      <el-table-column
        prop="name"
        fixed
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>

      <el-table-column
        prop="status"
        :label="$t('test_track.plan.plan_status')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <plan-status-table-item :value="scope.row.status"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="projectName"
        :label="'通过率'"
        show-overflow-tooltip>
        20%
      </el-table-column>

      <el-table-column
        prop="projectName"
        :label="'已测用例'"
        show-overflow-tooltip>
        14/16
      </el-table-column>

      <el-table-column
        prop="projectName"
        :label="'测试进度'"
        min-width="120"
        show-overflow-tooltip>
        <el-progress :percentage="50"></el-progress>
      </el-table-column>

      <el-table-column
        prop="stage"
        :label="$t('test_track.plan.plan_stage')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <plan-stage-table-item :stage="scope.row.stage"/>
        </template>
      </el-table-column>
      <el-table-column
        prop="projectName"
        :label="$t('test_track.plan.plan_project')"
        show-overflow-tooltip>
      </el-table-column>


    </el-table>

  </home-base-component>
</template>

<script>
    import HomeBaseComponent from "./HomeBaseComponent";
    import PlanStatusTableItem from "../../common/tableItems/plan/PlanStatusTableItem";
    import PlanStageTableItem from "../../common/tableItems/plan/PlanStageTableItem";
    import MsTableOperator from "../../../common/components/MsTableOperator";
    export default {
      name: "RelatedTestPlanList",
      components: {MsTableOperator, PlanStageTableItem, PlanStatusTableItem, HomeBaseComponent},
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
          this.result = this.$post('/test/plan/list/all/relate', this.condition, response => {
            this.tableData = response.data;
          });
        },
        intoPlan(row, event, column) {
          this.$router.push('/track/plan/view/' + row.id);
        }
      }
    }
</script>

<style scoped>
</style>
