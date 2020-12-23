<template>
  <div v-if="visible">
    <ms-drawer :size="60" @close="apiCaseClose" direction="bottom">
      <template v-slot:header>
        <api-case-header
          :api="api"
          @getApiTest="getApiTest"
          @setEnvironment="setEnvironment"
          @addCase="addCase"
          @batchRun="batchRun"
          :condition="condition"
          :priorities="priorities"
          :apiCaseList="apiCaseList"
          :is-read-only="isReadOnly"
          :project-id="projectId"
          :is-case-edit="isCaseEdit"
        />
      </template>

      <el-container v-loading="result.loading">
        <el-main v-loading="batchLoading">
          <div v-for="(item,index) in apiCaseList" :key="index">
            <api-case-item v-loading="singleLoading && singleRunId === item.id"
                           @refresh="refresh"
                           @singleRun="singleRun"
                           @copyCase="copyCase"
                           @showExecResult="showExecResult"
                           :is-case-edit="isCaseEdit"
                           :api="api"
                           :api-case="item" :index="index"/>
          </div>
        </el-main>
      </el-container>
    </ms-drawer>

    <!-- 执行组件 -->
    <ms-run :debug="false" :environment="environment" :reportId="reportId" :run-data="runData"
            @runRefresh="runRefresh" ref="runTest"/>

  </div>

</template>
<script>

  import ApiCaseHeader from "./ApiCaseHeader";
  import ApiCaseItem from "./ApiCaseItem";
  import MsRun from "../Run";
  import {downloadFile, getUUID, getCurrentProjectID} from "@/common/js/utils";
  import MsDrawer from "../../../../common/components/MsDrawer";
  import {PRIORITY} from "../../model/JsonData";

  export default {
    name: 'ApiCaseList',
    components: {
      MsDrawer,
      MsRun,
      ApiCaseHeader,
      ApiCaseItem,

    },
    props: {
      createCase: String,
      loaded: Boolean,
      refreshSign: String,
      currentApi: {
        type: Object
      },
    },
    data() {
      return {
        result: {},
        grades: [],
        environment: {},
        isReadOnly: false,
        selectedEvent: Object,
        priorities: PRIORITY,
        apiCaseList: [],
        batchLoading: false,
        singleLoading: false,
        singleRunId: "",
        runData: [],
        reportId: "",
        projectId: "",
        testCaseId: "",
        checkedCases: new Set(),
        visible: false,
        condition: {},
        api: {}
      }
    },
    watch: {
      refreshSign() {
        this.api = this.currentApi;
        this.getApiTest();
      },
      createCase() {
        this.api = this.currentApi;
        this.sysAddition();
      }
    },
    created() {
      this.api = this.currentApi;
      this.projectId = getCurrentProjectID();
      if (this.createCase) {
        this.sysAddition();
      } else {
        this.getApiTest();
      }
    },
    computed: {
      isCaseEdit() {
        return this.testCaseId ? true : false;
      }
    },
    methods: {
      open(api, testCaseId) {
        this.api = api;
        // testCaseId 不为空则为用例编辑页面
        this.testCaseId = testCaseId;
        this.getApiTest();
        this.visible = true;
      },
      setEnvironment(environment) {
        this.environment = environment;
      },
      sysAddition() {
        this.condition.projectId = this.projectId;
        this.condition.apiDefinitionId = this.api.id;
        this.$post("/api/testcase/list", this.condition, response => {
          for (let index in response.data) {
            let test = response.data[index];
            test.request = JSON.parse(test.request);
          }
          this.apiCaseList = response.data;
          this.addCase();
        });
      },

      apiCaseClose() {
        this.apiCaseList = [];
        this.visible = false;
      },

      runRefresh(data) {
        this.batchLoading = false;
        this.singleLoading = false;
        this.singleRunId = "";
        this.$success(this.$t('schedule.event_success'));
        this.getApiTest();
        this.$emit('refresh');
      },

      refresh(data) {
        this.getApiTest();
        this.$emit('refresh');
      },

      getApiTest() {
        if (this.api) {
          this.condition.projectId = this.projectId;
          if (this.isCaseEdit) {
            this.condition.id = this.testCaseId;
          } else {
            this.condition.apiDefinitionId = this.api.id;
          }
          this.result = this.$post("/api/testcase/list", this.condition, response => {
            for (let index in response.data) {
              let test = response.data[index];
              test.request = JSON.parse(test.request);
              if (!test.request.hashTree) {
                test.request.hashTree = [];
              }
            }
            this.apiCaseList = response.data;
            if (this.apiCaseList.length == 0 && !this.loaded) {
              this.addCase();
            }
          });
        }
      },
      addCase() {
        if (this.api.request) {
          // 初始化对象
          let request = {};
          if (this.api.request instanceof Object) {
            request = this.api.request;
          } else {
            request = JSON.parse(this.api.request);
          }
          let obj = {apiDefinitionId: this.api.id, name: '', priority: 'P0', active: true};
          obj.request = request;
          this.apiCaseList.unshift(obj);
        }
      },

      copyCase(data) {
        this.apiCaseList.unshift(data);
      },

      handleClose() {
        this.visible = false;
      },
      showExecResult(row) {
        this.visible = false;
        this.$emit('showExecResult', row);
      },

      singleRun(row) {
        if (!this.environment || !this.environment) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        this.runData = [];
        this.singleLoading = true;
        this.singleRunId = row.id;
        row.request.name = row.id;
        row.request.useEnvironment = this.environment.id;
        this.runData.push(row.request);
        /*触发执行操作*/
        this.reportId = getUUID().substring(0, 8);
      },

      batchRun() {
        if (!this.environment) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        if (this.apiCaseList.length > 0) {
          this.apiCaseList.forEach(item => {
            if (item.id) {
              item.request.name = item.id;
              item.request.useEnvironment = this.environment.id;
              this.runData.push(item.request);
            }
          })
          if (this.runData.length > 0) {
            this.batchLoading = true;
            /*触发执行操作*/
            this.reportId = getUUID().substring(0, 8);
          } else {
            this.$warning("没有可执行的用例！");
          }
        } else {
          this.$warning("没有可执行的用例！");
        }
      }
    }
  }
</script>

<style scoped>

  .ms-drawer >>> .ms-drawer-body {
    margin-top: 80px;
  }

</style>
