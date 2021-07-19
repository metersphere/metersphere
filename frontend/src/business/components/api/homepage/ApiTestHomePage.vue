<template>
  <ms-container>
    <ms-main-container v-loading="result.loading">
      <el-row :gutter="10">
        <el-col :span="6">
          <ms-api-info-card @redirectPage="redirectPage" :api-count-data="apiCountData"/>
        </el-col>
        <el-col :span="6">
          <ms-test-case-info-card @redirectPage="redirectPage" :test-case-count-data="testCaseCountData"/>
        </el-col>
        <el-col :span="6">
          <ms-scene-info-card @redirectPage="redirectPage"  :scene-count-data="sceneCountData" :interface-coverage="interfaceCoverage"/>
        </el-col>
        <el-col :span="6">
          <ms-schedule-task-info-card :schedule-task-count-data="scheduleTaskCountData"/>
        </el-col>
      </el-row>

      <el-row :gutter="10">
        <el-col :span="12">
          <ms-failure-test-case-list @redirectPage="redirectPage"/>
        </el-col>
        <el-col :span="12">
          <ms-running-task-list :call-from="'api_test'" @redirectPage="redirectPage"/>
        </el-col>
      </el-row>

    </ms-main-container>
  </ms-container>
</template>

<script>
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsApiInfoCard from "./components/ApiInfoCard";
import MsSceneInfoCard from "./components/SceneInfoCard";
import MsScheduleTaskInfoCard from "./components/ScheduleTaskInfoCard";
import MsTestCaseInfoCard from "./components/TestCaseInfoCard";

import MsFailureTestCaseList from "./components/FailureTestCaseList";
import MsRunningTaskList from "./components/RunningTaskList"
import {getCurrentProjectID,getUUID} from "@/common/js/utils";

export default {
  name: "ApiTestHomePage",

  components: {
    MsApiInfoCard, MsSceneInfoCard, MsScheduleTaskInfoCard, MsTestCaseInfoCard,
    MsFailureTestCaseList, MsRunningTaskList,
    MsMainContainer, MsContainer
  },

  data() {
    return {
      values: [],
      apiCountData: {},
      sceneCountData: {},
      testCaseCountData: {},
      scheduleTaskCountData: {},
      interfaceCoverage: "waitting...",
      result: {},
    }
  },
  activated() {
    this.search();
  },
  created() {
  },
  methods: {
    search() {
      let selectProjectId = getCurrentProjectID();

      this.$get("/api/apiCount/" + selectProjectId, response => {
        this.apiCountData = response.data;
      });

      this.$get("/api/testSceneInfoCount/" + selectProjectId, response => {
        this.sceneCountData = response.data;
      });
      this.interfaceCoverage = "waitting...";
      this.$get("/api/countInterfaceCoverage/" + selectProjectId, response => {
        this.interfaceCoverage = response.data;
      });

      this.$get("/api/testCaseInfoCount/" + selectProjectId, response => {
        this.testCaseCountData = response.data;
      });

      this.$get("/api/scheduleTaskInfoCount/" + selectProjectId, response => {
        this.scheduleTaskCountData = response.data;
      });
    },
    redirectPage(page,dataType,selectType){
      //api页面跳转
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      switch (page){
        case "api":
          this.$router.push({name:'ApiDefinition',params:{redirectID:uuid,dataType:dataType,dataSelectRange:selectType}});
          break;
        case "scenario":
          this.$router.push({name:'ApiAutomation',params:{redirectID:uuid,dataType:dataType,dataSelectRange:selectType}});
          break;
        case "testPlanEdit":
          this.$router.push('/track/plan/view/'+selectType)
          break;
      }
    }
  }
}
</script>

<style scoped>
.el-row {
  margin-bottom: 20px;
  margin-left: 20px;
  margin-right: 20px;
}
</style>
