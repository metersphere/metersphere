<template>
  <ms-container>
    <ms-main-container v-loading="loading">
      <el-row :gutter="5">
        <el-col :span="12">
          <ms-performance-test-recent-list/>
        </el-col>
        <el-col :span="12">
          <ms-performance-report-recent-list/>
        </el-col>
      </el-row>
      <el-row :gutter="5">
        <el-col :span="12">
          <ms-test-heatmap :values="values"/>
        </el-col>
        <el-col :span="12">
          <ms-schedule-list :group="'PERFORMANCE_TEST'" v-permission="['PROJECT_PERFORMANCE_TEST:READ+SCHEDULE']"/>
        </el-col>
      </el-row>
    </ms-main-container>
  </ms-container>

</template>

<script>
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsPerformanceTestRecentList from "./PerformanceTestRecentList";
import MsPerformanceReportRecentList from "./PerformanceReportRecentList";
import MsTestHeatmap from "./MsTestHeatmap";
import MsScheduleList from "./ScheduleList";
import {getDashboardHeatmap} from "@/api/performance";

export default {
  name: "PerformanceTestHome",
  components: {
    MsScheduleList,
    MsTestHeatmap,
    MsMainContainer,
    MsContainer,
    MsPerformanceTestRecentList,
    MsPerformanceReportRecentList
  },
  data() {
    return {
      values: [],
      loading: false,
    };
  },
  mounted() {
    this.getValues();
  },
  methods: {
    getValues() {
      this.loading = getDashboardHeatmap()
        .then(response => {
          this.values = response.data;
        });
    }
  }
};
</script>

<style scoped>
.el-row {
  padding-bottom: 5px;
}
</style>
