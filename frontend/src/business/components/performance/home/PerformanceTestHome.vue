<template>
  <ms-container>
    <ms-main-container v-loading="result.loading">
      <el-row :gutter="20">
        <el-col :span="12">
          <ms-performance-test-recent-list/>
        </el-col>
        <el-col :span="12">
          <ms-performance-report-recent-list/>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <ms-test-heatmap :values="values"/>
        </el-col>
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
        result: {},
      }
    },
    mounted() {
      this.result = this.$get('/performance/dashboard/tests', response => {
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
