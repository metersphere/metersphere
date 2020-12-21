<template>
  <ms-container>
    <el-header height="0">
      <div style="float: right">
        <span>
          😊 MeterSphere温馨提醒 —— 多喝热水哟！
        </span>
      </div>
    </el-header>
    <ms-main-container v-loading="result.loading">
      <el-row :gutter="0"></el-row>
      <el-row :gutter="10">
        <el-col :span="6"  >
          <ms-api-info-card :api-count-data="apiCountData"/>
        </el-col>
        <el-col :span="6" >
          <ms-test-case-info-card :test-case-count-data="testCaseCountData"/>
        </el-col>
        <el-col :span="6" >
          <ms-scene-info-card :scene-count-data="sceneCountData"/>
        </el-col>
        <el-col :span="6" >
          <ms-schedule-task-info-card :schedule-task-count-data="scheduleTaskCountData"/>
        </el-col>
      </el-row>

      <el-row :gutter="10" >
        <el-col :span="12"  >
          <ms-failure-test-case-list/>
        </el-col>
        <el-col  :span="12"  >
          <ms-running-task-list/>
        </el-col>
      </el-row>

    </ms-main-container>
  </ms-container>
</template>

<script>
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer  from "@/business/components/common/components/MsMainContainer";
import MsApiInfoCard from "./components/ApiInfoCard";
import MsSceneInfoCard from "./components/SceneInfoCard";
import MsScheduleTaskInfoCard from "./components/ScheduleTaskInfoCard";
import MsTestCaseInfoCard from "./components/TestCaseInfoCard";

// import MsApiDetailCard from "./components/ApiDetailCard";
// import MsSceneDetailCard from "./components/SceneDetailCard";
// import MsScheduleTaskDetailCard from "./components/ScheduleTaskDetailCard";
// import MsTestCaseDetailCard from "./components/TestCaseDetailCard";

import MsFailureTestCaseList from "./components/FailureTestCaseList";
import MsRunningTaskList from "./components/RunningTaskList"
import {getCurrentProjectID,getCurrentWorkspaceId} from "@/common/js/utils";

export default {
  name: "ApiTestHomePage",

  components: {
    MsApiInfoCard, MsSceneInfoCard, MsScheduleTaskInfoCard, MsTestCaseInfoCard,
    // MsApiDetailCard, MsSceneDetailCard, MsScheduleTaskDetailCard, MsTestCaseDetailCard,
    MsFailureTestCaseList,MsRunningTaskList,
    MsMainContainer, MsContainer
  },

  data() {
    return {
      values: [],
      apiCountData:{},
      sceneCountData:{},
      testCaseCountData:{},
      scheduleTaskCountData:{},
      result: {},
    }
  },
  // activated() {
  //   this.getValues();
  // },
  // mounted() {
  //   this.getValues();
  // },
  created() {
    this.search();
  },
  methods: {

    search() {
      let selectProjectId = getCurrentProjectID();
      this.$get("/api/apiCount/"+selectProjectId, response => {
        this.apiCountData = response.data;
      });

      this.$get("/api/testSceneInfoCount/"+selectProjectId, response => {
        this.sceneCountData = response.data;
      });

      this.$get("/api/testCaseInfoCount/"+selectProjectId, response => {
        this.testCaseCountData = response.data;
      });


      let workSpaceID = getCurrentWorkspaceId();
      this.$get("/api/scheduleTaskInfoCount/"+workSpaceID, response => {
        this.scheduleTaskCountData = response.data;
      });
    },
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
