<template>
  <ms-container>
    <ms-main-container v-loading="result.loading">
      <el-row :gutter="10" style="margin-bottom: 5px; margin-top: -5px;">
        <el-col :span="6" style="padding-left: 5px;padding-right: 0px">
          <ms-api-info-card @redirectPage="redirectPage" :api-count-data="apiCountData"
                            :interface-coverage="apiCoverage"/>
        </el-col>
        <el-col :span="6" style="padding-left: 5px;padding-right: 0px">
          <ms-test-case-info-card @redirectPage="redirectPage" :test-case-count-data="testCaseCountData"/>
        </el-col>
        <el-col :span="6" style="padding-left: 5px;padding-right: 0px">
          <ms-scene-info-card @redirectPage="redirectPage" :scene-count-data="sceneCountData"
                              :interface-coverage="interfaceCoverage"/>
        </el-col>
        <el-col :span="6" style="padding-left: 5px;padding-right: 5px">
          <ms-schedule-task-info-card :schedule-task-count-data="scheduleTaskCountData"/>
        </el-col>
      </el-row>

      <el-row :gutter="10" style="margin-top: 0px; margin-bottom: 5px;">
        <el-col :span="12" style="padding-left: 5px;padding-right: 0px">
          <el-card class="table-card" v-loading="result.loading" body-style="padding:10px;">
            <template v-slot:header>
              <el-col :span="22">
            <span class="title">
            {{ cardTitle.name }}
            </span>
              </el-col>
              <el-dropdown @command="handleCommand">
                <el-col :span="2" :offset="22">
                  <el-link type="primary" :underline="false">
                    <el-icon class="el-icon-more"></el-icon>
                  </el-link>
                </el-col>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="caseTitle">
                    <el-radio v-model="testTitleFirst" label="caseTitle">{{
                        $t('api_test.home_page.failed_case_list.title')}}
                    </el-radio>
                  </el-dropdown-item>
                  <el-dropdown-item command="taskTitle">
                    <el-radio v-model="testTitleFirst" label="taskTitle">
                      {{ $t('api_test.home_page.running_task_list.title') }}
                    </el-radio>
                  </el-dropdown-item>
                  <el-dropdown-item command="newApiTitle">
                    <el-radio v-model="testTitleFirst" label="newApiTitle">
                      {{ $t('api_test.home_page.new_case_list.title') }}
                    </el-radio>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
            <div v-if="testTitleFirst === 'caseTitle'">
              <ms-api-failure-test-case-list @redirectPage="redirectPage"/>
            </div>
            <div v-if="testTitleFirst === 'taskTitle'">
              <ms-api-running-task-list :call-from="'api_test'" @redirectPage="redirectPage"/>
            </div>
            <div v-if="testTitleFirst === 'newApiTitle'">
              <ms-api-new-test-case-list @redirectPage="redirectPage"/>
            </div>
          </el-card>
        </el-col>

        <el-col :span="12" style="padding-left: 5px;padding-right: 5px">
          <el-card class="table-card" v-loading="result.loading" body-style="padding:10px;">
            <div v-if="testTitleSecond === 'caseTitle'">
              <ms-api-failure-test-case-list @redirectPage="redirectPage" />
            </div>
            <div v-if="testTitleSecond === 'taskTitle'">
              <ms-api-running-task-list :call-from="'api_test'" @redirectPage="redirectPage"/>
            </div>
            <div v-if="testTitleSecond === 'newApiTitle'">
              <ms-api-new-test-case-list @redirectPage="redirectPage"/>
            </div>
            <template v-slot:header>
              <el-col :span="22">
            <span class="title">
            {{ cardTitleSecond.name }}
            </span>
              </el-col>
              <el-dropdown @command="handleCommandSecond">
                <el-col :span="2" :offset="22">
                  <el-link type="primary" :underline="false">
                    <el-icon class="el-icon-more"></el-icon>
                  </el-link>
                </el-col>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="caseTitle">
                    <el-radio v-model="testTitleSecond" label="caseTitle">{{
                        $t('api_test.home_page.failed_case_list.title')
                      }}
                    </el-radio>
                  </el-dropdown-item>
                  <el-dropdown-item command="taskTitle">
                    <el-radio v-model="testTitleSecond" label="taskTitle">
                      {{ $t('api_test.home_page.running_task_list.title') }}
                    </el-radio>
                  </el-dropdown-item>
                  <el-dropdown-item command="newApiTitle">
                    <el-radio v-model="testTitleSecond" label="newApiTitle">
                      {{ $t('api_test.home_page.new_case_list.title') }}
                    </el-radio>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>

          </el-card>
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

import MsApiFailureTestCaseList from "./components/ApiFailureTestCaseList";
import MsFailureTestCaseList from "./components/FailureTestCaseList";
import MsRunningTaskList from "./components/RunningTaskList";
import MsApiRunningTaskList from "./components/ApiRunningTaskList";
import {getCurrentProjectID, getUUID} from "@/common/js/utils";
import MsApiNewTestCaseList from "./components/ApiNewTestCaseList";

export default {
  name: "ApiTestHomePage",

  components: {
    MsApiInfoCard, MsSceneInfoCard, MsScheduleTaskInfoCard, MsTestCaseInfoCard,
    MsApiFailureTestCaseList, MsRunningTaskList,
    MsMainContainer, MsContainer,MsFailureTestCaseList,MsApiRunningTaskList,MsApiNewTestCaseList
  },

  data() {
    return {
      values: [],
      apiCountData: {},
      sceneCountData: {},
      testCaseCountData: {},
      scheduleTaskCountData: {},
      interfaceCoverage: "waitting...",
      apiCoverage: "waitting...",
      result: {},
      testTitleFirst: " ",
      testTitleSecond: " ",
      cardTitle: {},
      cardTitleSecond: {},
    }
  },
  activated() {
    this.search();
  },
  created() {
    this.testTitleFirst = "caseTitle";
    if (localStorage.getItem("cardTitle")) {
      if (localStorage.getItem("titleFirst")) {
        this.testTitleFirst = localStorage.getItem("titleFirst");
      }
      this.cardTitle.name = localStorage.getItem("cardTitle");
    } else{
      this.cardTitle.name=this.$t('api_test.home_page.failed_case_list.title');
    }
    this.testTitleSecond = "taskTitle";
    if (localStorage.getItem("cardTitleSecond")) {
      if (localStorage.getItem("titleSecond")) {
        this.testTitleSecond = localStorage.getItem("titleSecond");
      }
      this.cardTitleSecond.name = localStorage.getItem("cardTitleSecond");
    } else{
      this.cardTitleSecond.name=this.$t('api_test.home_page.running_task_list.title');
    }

  },
  methods: {
    handleCommand(cmd) {
      if(cmd){
        switch (cmd) {
          case  "caseTitle":
            this.cardTitle.name=this.$t('api_test.home_page.failed_case_list.title');
            localStorage.setItem("cardTitle" , this.cardTitle.name);
            localStorage.setItem("titleFirst" , "caseTitle");
            break;
          case "taskTitle":
            this.cardTitle.name = this.$t('api_test.home_page.running_task_list.title');
            localStorage.setItem("cardTitle" , this.cardTitle.name);
            localStorage.setItem("titleFirst" , "taskTitle");
            break;
          case "newApiTitle":
            this.cardTitle.name = this.$t('api_test.home_page.new_case_list.title');
            localStorage.setItem("cardTitle" , this.cardTitle.name);
            localStorage.setItem("titleFirst" , "newApiTitle");
            break;
        }
      }
    },
    handleCommandSecond(cmd) {
      if(cmd){
        switch (cmd) {
          case  "caseTitle":
            this.cardTitleSecond.name=this.$t('api_test.home_page.failed_case_list.title');
            localStorage.setItem("cardTitleSecond" , this.cardTitleSecond.name);
            localStorage.setItem("titleSecond" , "caseTitle");
            break;
          case "taskTitle":
            this.cardTitleSecond.name = this.$t('api_test.home_page.running_task_list.title');
            localStorage.setItem("cardTitleSecond" , this.cardTitleSecond.name);
            localStorage.setItem("titleSecond" , "taskTitle");
            break;
          case "newApiTitle":
            this.cardTitleSecond.name = this.$t('api_test.home_page.new_case_list.title');
            localStorage.setItem("cardTitleSecond" , this.cardTitleSecond.name);
            localStorage.setItem("titleSecond" , "newApiTitle");
            break;
        }
      }
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      });
    },
    search() {
      let selectProjectId = getCurrentProjectID();
      this.$get("/api/apiCount/" + selectProjectId, response => {
        this.apiCountData = response.data;
      });

      this.$get("/api/testSceneInfoCount/" + selectProjectId, response => {
        this.sceneCountData = response.data;
      });
      this.apiCoverage = "waitting...",
        this.interfaceCoverage = "waitting...";
      this.$get("/api/countInterfaceCoverage/" + selectProjectId, response => {
        this.interfaceCoverage = response.data;
      });

      this.$get("/api/countApiCoverage/" + selectProjectId, response => {
        this.apiCoverage = response.data;
      });

      this.$get("/api/testCaseInfoCount/" + selectProjectId, response => {
        this.testCaseCountData = response.data;
      });

      this.$get("/api/scheduleTaskInfoCount/" + selectProjectId, response => {
        this.scheduleTaskCountData = response.data;
      });
    },
    redirectPage(page, dataType, selectType, title) {
      //api页面跳转
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      switch (page) {
        case "api":
          this.$router.push({
            name: 'ApiDefinition',
            params: {redirectID: uuid, dataType: dataType, dataSelectRange: selectType}
          });
          break;
        case "scenario":
          this.$router.push({
            name: 'ApiAutomation',
            params: {redirectID: uuid, dataType: dataType, dataSelectRange: selectType}
          });
          break;
        case "testPlanEdit":
          this.$router.push('/track/plan/view/' + selectType)
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

/deep/ .el-table__empty-block {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  padding-right: 100%;
}

</style>
