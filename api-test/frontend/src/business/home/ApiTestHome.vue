<template>
  <ms-container>
    <ms-main-container v-loading="result">
      <el-row :gutter="10" style="margin-bottom: 5px; margin-top: -5px;">
        <el-col :span="6" style="padding-left: 5px;padding-right: 0px">
          <api-count-card @redirectPage="redirectPage" :api-count-data="apiCountData"
                          :api-coverage="apiCoverage"/>
        </el-col>
        <el-col :span="6" style="padding-left: 5px;padding-right: 0px">
          <api-case-count-card @redirectPage="redirectPage" :test-case-count-data="testCaseCountData"
                               :is-xpack="isXpack"/>
        </el-col>
        <el-col :span="6" style="padding-left: 5px;padding-right: 0px">
          <scenario-count-card @redirectPage="redirectPage" :scene-count-data="sceneCountData" :is-xpack="isXpack"
                               :scenario-coverage="scenarioCoverage"/>
        </el-col>
        <el-col :span="6" style="padding-left: 5px;padding-right: 5px">
          <schedule-task-count-card @redirectPage="redirectPage" :schedule-task-count-data="scheduleTaskCountData"
                                    :is-xpack="isXpack"/>
        </el-col>
      </el-row>

      <el-row :gutter="10" style="margin-top: 0px; margin-bottom: 5px;">
        <el-col :span="12" style="padding-left: 5px;padding-right: 0px">
          <el-card class="table-card" v-loading="result" body-style="padding:10px;">
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
                        $t('api_test.home_page.failed_case_list.title')
                      }}
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
          <el-card class="table-card" v-loading="result" body-style="padding:10px;">
            <div v-if="testTitleSecond === 'caseTitle'">
              <ms-api-failure-test-case-list @redirectPage="redirectPage"/>
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
import {
  apiCaseCountByProjectId,
  apiCountByProjectId,
  countApiCoverageByProjectId,
  countScenarioCoverageByProjectId,
  scenarioCountByProjectId,
  scheduleTaskCountByProjectId
} from "@/api/home";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsApiInfoCard from "./components/ApiInfoCard";
import MsSceneInfoCard from "./components/SceneInfoCard";
import MsScheduleTaskInfoCard from "./components/ScheduleTaskInfoCard";
import MsTestCaseInfoCard from "./components/TestCaseInfoCard";

import MsApiFailureTestCaseList from "./components/ApiFailureTestCaseList";
import MsFailureTestCaseList from "./components/FailureTestCaseList";
import MsRunningTaskList from "./components/RunningTaskList";
import MsApiRunningTaskList from "./components/ApiRunningTaskList";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getUUID} from "metersphere-frontend/src/utils";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import MsApiNewTestCaseList from "./components/ApiNewTestCaseList";

import ApiCountCard from "@/business/home/components/card/ApiCountCard";
import ApiCaseCountCard from "@/business/home/components/card/ApiCaseCountCard";
import ScenarioCountCard from "@/business/home/components/card/ScenarioCountCard";
import ScheduleTaskCountCard from "@/business/home/components/card/ScheduleTaskCountCard";

export default {
  name: "ApiTestHome",

  components: {
    MsApiInfoCard, MsSceneInfoCard, MsScheduleTaskInfoCard, MsTestCaseInfoCard,
    MsApiFailureTestCaseList, MsRunningTaskList,
    MsMainContainer, MsContainer, MsFailureTestCaseList, MsApiRunningTaskList, MsApiNewTestCaseList,
    ApiCountCard, ApiCaseCountCard, ScenarioCountCard, ScheduleTaskCountCard
  },

  data() {
    return {
      isXpack: false,
      values: [],
      apiCountData: {},
      sceneCountData: {},
      testCaseCountData: {},
      scheduleTaskCountData: {},
      apiCoverage: {
        rateOfCoverage: "waitting...",
      },
      scenarioCoverage: {
        rateOfCoverage: "waitting...",
      },
      result: false,
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
    } else {
      this.cardTitle.name = this.$t('api_test.home_page.failed_case_list.title');
    }
    this.testTitleSecond = "taskTitle";
    if (localStorage.getItem("cardTitleSecond")) {
      if (localStorage.getItem("titleSecond")) {
        this.testTitleSecond = localStorage.getItem("titleSecond");
      }
      this.cardTitleSecond.name = localStorage.getItem("cardTitleSecond");
    } else {
      this.cardTitleSecond.name = this.$t('api_test.home_page.running_task_list.title');
    }

  },
  methods: {
    handleCommand(cmd) {
      if (cmd) {
        switch (cmd) {
          case  "caseTitle":
            this.cardTitle.name = this.$t('api_test.home_page.failed_case_list.title');
            localStorage.setItem("cardTitle", this.cardTitle.name);
            localStorage.setItem("titleFirst", "caseTitle");
            break;
          case "taskTitle":
            this.cardTitle.name = this.$t('api_test.home_page.running_task_list.title');
            localStorage.setItem("cardTitle", this.cardTitle.name);
            localStorage.setItem("titleFirst", "taskTitle");
            break;
          case "newApiTitle":
            this.cardTitle.name = this.$t('api_test.home_page.new_case_list.title');
            localStorage.setItem("cardTitle", this.cardTitle.name);
            localStorage.setItem("titleFirst", "newApiTitle");
            break;
        }
      }
    },
    handleCommandSecond(cmd) {
      if (cmd) {
        switch (cmd) {
          case  "caseTitle":
            this.cardTitleSecond.name = this.$t('api_test.home_page.failed_case_list.title');
            localStorage.setItem("cardTitleSecond", this.cardTitleSecond.name);
            localStorage.setItem("titleSecond", "caseTitle");
            break;
          case "taskTitle":
            this.cardTitleSecond.name = this.$t('api_test.home_page.running_task_list.title');
            localStorage.setItem("cardTitleSecond", this.cardTitleSecond.name);
            localStorage.setItem("titleSecond", "taskTitle");
            break;
          case "newApiTitle":
            this.cardTitleSecond.name = this.$t('api_test.home_page.new_case_list.title');
            localStorage.setItem("cardTitleSecond", this.cardTitleSecond.name);
            localStorage.setItem("titleSecond", "newApiTitle");
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
      this.isXpack = hasLicense();
      this.searchData();
    },
    searchData() {
      let selectProjectId = getCurrentProjectID();
      apiCountByProjectId(selectProjectId).then(response => {
        this.apiCountData = response.data;
      });

      scenarioCountByProjectId(selectProjectId).then(response => {
        this.sceneCountData = response.data;
      });

      this.apiCoverage.rateOfCoverage = "waitting...";
      this.scenarioCoverage.rateOfCoverage = "waitting...";

      countScenarioCoverageByProjectId(selectProjectId).then(response => {
        let respData = response.data;
        this.scenarioCoverage.rateOfCoverage = respData.rateOfCoverage;
        this.scenarioCoverage.coverate = respData.coverate;
        this.scenarioCoverage.notCoverate = respData.notCoverate;
      });

      countApiCoverageByProjectId(selectProjectId).then(response => {
        let respData = response.data;
        this.apiCoverage.rateOfCoverage = respData.rateOfCoverage;
        this.apiCoverage.coverate = respData.coverate;
        this.apiCoverage.notCoverate = respData.notCoverate;
      });

      apiCaseCountByProjectId(selectProjectId).then(response => {
        this.testCaseCountData = response.data;
      });

      scheduleTaskCountByProjectId(selectProjectId).then(response => {
        this.scheduleTaskCountData = response.data;
      });
    },
    redirectPage(page, dataType, selectType, title) {
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      let home ;
      switch (page) {
        case "api":
          home = this.$router.resolve({
            name: 'ApiDefinitionWithQuery',
            params: {redirectID: uuid, dataType: dataType, dataSelectRange: selectType}
          });
          break;
        case "apiWithQuery":
          home = this.$router.resolve({
            name: 'ApiDefinitionWithQuery',
            params: {redirectID: uuid, dataType: dataType, dataSelectRange: selectType}
          });
          break;
        case "scenario":
          home = this.$router.resolve({
            name: 'ApiAutomationWithQuery',
            params: {redirectID: uuid, dataType: dataType, dataSelectRange: selectType}
          });
          break;
        case "scenarioWithQuery":
          home = this.$router.resolve({
            name: 'ApiAutomationWithQuery',
            params: {redirectID: uuid, dataType: dataType, dataSelectRange: selectType}
          });
          break;
        case "testPlanEdit":
          home = this.$router.resolve('/track/plan/view/' + selectType)
          break;
      }
      if (home) {
        window.open(home.href, '_blank');
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

:deep(.el-table__empty-block) {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  padding-right: 100%;
}

</style>
