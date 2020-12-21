<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">
        {{$t('api_test.home_page.running_task_list.title')}}
      </span>
    </template>
    <el-table border :data="tableData" class="adjust-table table-content" height="300px">
      <el-table-column prop="index"  :label="$t('api_test.home_page.running_task_list.table_coloum.index')" width="80" show-overflow-tooltip/>
      <el-table-column prop="scenario"  :label="$t('api_test.home_page.running_task_list.table_coloum.scenario')" width="200" show-overflow-tooltip/>
      <el-table-column prop="rule"  :label="$t('api_test.home_page.running_task_list.table_coloum.run_rule')" width="120" show-overflow-tooltip/>
      <el-table-column width="100" :label="$t('api_test.home_page.running_task_list.table_coloum.task_status')">
        <template v-slot:default="scope">
          <el-switch @click.stop.native v-model="scope.row.taskStatus" @change="updateTask(scope.row)"/>
        </template>
      </el-table-column>
      <el-table-column width="170" :label="$t('api_test.home_page.running_task_list.table_coloum.next_execution_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.nextExecutionTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="creator"  :label="$t('api_test.home_page.running_task_list.table_coloum.create_user')" width="100" show-overflow-tooltip/>
      <el-table-column width="170" :label="$t('api_test.home_page.running_task_list.table_coloum.update_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>

    </el-table>
  </el-card>
</template>

<script>
import {getCurrentProjectID,getCurrentWorkspaceId} from "@/common/js/utils";
export default {
  name: "MsRunningTaskList",


  data() {
    return {
      value: '100',
      result: {},
      tableData: [],
      loading: false
    }
  },

  methods: {
    search() {
      let workSpaceID = getCurrentWorkspaceId();
      this.result = this.$get("/api/runningTask/"+workSpaceID, response => {
        this.tableData = response.data;
      });
    },

    updateTask(taskRow){

      this.result = this.$post('/api/schedule/updateEnableByPrimyKey', taskRow, response => {
        this.search();
      });
    }
  },

  created() {
    this.search();
  },
  activated() {
    this.search();
  }
}
</script>

<style scoped>

.el-table {
  cursor:pointer;
}
.el-card /deep/ .el-card__header {
  border-bottom: 0px solid #EBEEF5;
}

</style>
