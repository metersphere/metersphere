<template>
  <el-card class="table-card" v-loading="loading">
    <template v-slot:header>
      <span class="title">{{ $t('schedule.running_task') }}</span>
    </template>
    <el-table height="289" border :data="tableData" class="adjust-table table-content" @row-click="link">
      <el-table-column prop="resourceName" :label="$t('schedule.test_name')" width="150" show-overflow-tooltip/>
      <el-table-column prop="value" :label="$t('schedule.running_rule')" width="150" show-overflow-tooltip/>
      <el-table-column width="100px" :label="$t('schedule.job_status')">
        <template v-slot:default="scope">
          <el-switch @click.stop.native v-model="scope.row.enable" @change="update(scope.row)"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('schedule.next_execution_time')">
        <template v-slot:default="scope">
          <crontab-result :enable-simple-mode="true" :ex="scope.row.value" ref="crontabResult"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('report.user_name')" prop="userName"/>
      <el-table-column :label="$t('commons.update_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import CrontabResult from "metersphere-frontend/src/components/cron/CrontabResult";
import {listSchedule, updateSchedule} from "@/api/performance";

export default {
  name: "MsScheduleList",
  components: {CrontabResult},
  data() {
    return {
      result: {},
      tableData: [],
      loading: false,
      operators: {}
    };
  },
  props: {
    group: String
  },
  methods: {
    search() {
      let data = {
        workspaceId: getCurrentWorkspaceId(),
        projectId: getCurrentProjectID(),
        group: this.group
      };
      this.loading = listSchedule(data)
        .then(response => {
          this.tableData = response.data;
        })
    },
    link(row) {
      this.$router.push({
        path: `/performance/test/edit/${row.resourceId}`,
      });
    },
    update(schedule) {
      this.loading = updateSchedule(schedule)
        .then(() => {
          this.search();
        })
    },
    init() {
      this.search();
    }
  },

  created() {
    this.init();
  },
  activated() {
    this.init();
  }
};
</script>

<style scoped>
.el-table {
  cursor: pointer;
}
</style>
