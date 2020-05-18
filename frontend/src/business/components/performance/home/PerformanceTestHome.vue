<template>
  <ms-container>
    <ms-main-container>
      <el-row :gutter="20">
        <el-col :span="12">
          <ms-performance-report-recent-list/>
        </el-col>
        <el-col :span="12">
          <ms-performance-test-recent-list/>
        </el-col>
      </el-row>
      <el-row>
        <ms-test-heatmap :values="values"></ms-test-heatmap>
      </el-row>
    </ms-main-container>
  </ms-container>

</template>

<script>
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";
  import MsPerformanceTestRecentList from "./PerformanceTestRecentList"
  import MsPerformanceReportRecentList from "./PerformanceReportRecentList"
  import MsTestHeatmap from "../../common/components/MsTestHeatmap";

  export default {
    name: "PerformanceTestHome",
    components: {
      MsTestHeatmap,
      MsMainContainer,
      MsContainer,
      MsPerformanceTestRecentList,
      MsPerformanceReportRecentList
    },
    data() {
      return {
        values: [],
      }
    },
    mounted() {
      this.$get('/performance/dashboard/tests', response => {
        this.values = response.data;
      });
    },
  }
</script>

<style scoped>
  .el-row {
    padding-bottom: 20px;
  }
</style>
