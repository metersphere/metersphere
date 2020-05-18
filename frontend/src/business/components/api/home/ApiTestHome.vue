<template>
  <ms-container>
    <ms-main-container>
      <el-row :gutter="20">
        <el-col :span="12">
          <ms-api-test-recent-list/>
        </el-col>
        <el-col :span="12">
          <ms-api-report-recent-list/>
        </el-col>
      </el-row>
    </ms-main-container>
  </ms-container>

</template>

<script>
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";
  import MsApiTestRecentList from "./ApiTestRecentList";
  import MsApiReportRecentList from "./ApiReportRecentList";

  export default {
    name: "ApiTestHome",
    components: {MsApiReportRecentList, MsApiTestRecentList, MsMainContainer, MsContainer}
  }
</script>

<style scoped>

</style>
