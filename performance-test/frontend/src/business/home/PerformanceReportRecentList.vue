<template>
  <el-card class="table-card" v-loading="loading">
    <template v-slot:header>
      <span class="title">{{ $t('api_report.title') }}</span>
    </template>
    <el-table border :data="tableData" class="adjust-table table-content" @row-click="link" height="300px">
      <el-table-column prop="name" :label="$t('commons.name')" width="150" show-overflow-tooltip/>
      <el-table-column width="180" :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ datetimeFormat(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="triggerMode" width="150" :label="$t('commons.trigger_mode.name')">
        <template v-slot:default="scope">
          <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('commons.status')">
        <template v-slot:default="{row}">
          <ms-performance-report-status :row="row"/>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>

import MsPerformanceReportStatus from "../report/PerformanceReportStatus";
import ReportTriggerModeItem from "metersphere-frontend/src/components/tableItem/ReportTriggerModeItem";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getRecentReports} from "@/api/performance";
import {datetimeFormat} from "fit2cloud-ui/src/filters/time";

export default {
  name: "MsPerformanceReportRecentList",
  components: {ReportTriggerModeItem, MsPerformanceReportStatus},
  data() {
    return {
      loading: false,
      tableData: []
    };
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    search() {
      let condition = {
        projectId: this.projectId,
      };
      this.loading = getRecentReports(condition)
        .then(response => {
          this.tableData = response.data;
        })
    },
    link(row) {
      this.$router.push({
        path: '/performance/report/view/' + row.id,
      });
    },
    datetimeFormat,
  },

  created() {
    this.search();
  },
};
</script>

<style scoped>

.el-table {
  cursor: pointer;
}

</style>
