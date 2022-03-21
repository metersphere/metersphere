<template>
  <div v-if="visible">
    <ms-drawer :size="60" @close="apiCaseClose" direction="bottom" ref="testCaseDrawer">
      <template v-slot:header>
        <api-case-header
          :api="api"
          @setEnvironment="setEnvironment"
          @addCase="addCase"
          @saveCase="saveCase(apiCaseList[0])"
          :condition="condition"
          :priorities="priorities"
          :project-id="projectId"
          :useEnvironment="environment"
          :is-case-edit="isCaseEdit"
          ref="header"/>
      </template>

      <el-container v-if="!result.loading">
        <el-main>
          <api-case-item
            :loading="singleLoading && singleRunId === apiCaseList[0].id || batchLoadingIds.indexOf(apiCaseList[0].id) > -1"
            @refresh="refresh"
            @singleRun="singleRun"
            @stop="stop"
            @refreshModule="refreshModule"
            @copyCase="copyCase"
            @showExecResult="showExecResult"
            @showHistory="showHistory"
            @reLoadCase="reLoadCase"
            :environment="environment"
            :is-case-edit="isCaseEdit"
            :api="api"
            :currentApi="currentApi"
            :loaded="loaded"
            :runResult="runResult"
            :maintainerOptions="maintainerOptions"
            :api-case="apiCaseList[0]" ref="apiCaseItem"/>
        </el-main>
      </el-container>
    </ms-drawer>

    <!-- 执行组件 -->
    <ms-run :debug="false" :reportId="reportId" :run-data="runData" :env-map="envMap"
            @runRefresh="runRefresh" @errorRefresh="errorRefresh" ref="runTest"/>
    <ms-task-center ref="taskCenter" :show-menu="false"/>
  </div>
</template>
<script>

import ApiCaseHeader from "./ApiCaseHeader";
import {getCurrentProjectID, getUUID} from "@/common/js/utils";
import MsDrawer from "../../../../common/components/MsDrawer";
import {CASE_ORDER} from "../../model/JsonData";
import {API_CASE_CONFIGS} from "@/business/components/common/components/search/search-components";

export default {
  name: 'ApiCaseList',
  components: {
    MsDrawer,
    ApiCaseHeader,
    MsRun: () => import("../Run"),
    ApiCaseItem: () => import("./ApiCaseItem"),
    MsTaskCenter: () => import("../../../../task/TaskCenter"),
  },
  props: {
    createCase: String,
    loaded: {
      type: Boolean,
      default: false
    },
    refreshSign: String,
    currentApi: {
      type: Object
    },
  },
  data() {
    return {
      result: {},
      environment: "",
      priorities: CASE_ORDER,
      apiCaseList: [],
      batchLoadingIds: [],
      singleLoading: false,
      singleRunId: "",
      runResult: {},
      runData: [],
      reportId: "",
      testCaseId: "",
      visible: false,
      condition: {
        components: API_CASE_CONFIGS
      },
      api: {},
      envMap: new Map,
      maintainerOptions: [],
    };
  },
  watch: {
    '$store.state.useEnvironment': function () {
      this.environment = this.$store.state.useEnvironment;
    },
    refreshSign() {
      this.api = this.currentApi;
      this.getApiTest();
    },
    createCase() {
      this.api = this.currentApi;
      this.sysAddition();
    },
  },
  created() {
    this.api = this.currentApi;
    if (this.createCase) {
      this.sysAddition();
    }
    if (!this.environment && this.$store.state.useEnvironment) {
      this.environment = this.$store.state.useEnvironment;
    }
    this.getMaintainerOptions();
  },
  computed: {
    isCaseEdit() {
      return this.testCaseId ? true : false;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.maintainerOptions = response.data;
      });
    },
    close(){
      if(this.$refs.testCaseDrawer){
        this.$refs.testCaseDrawer.close();
      }
    },
    open(api, testCaseId) {
      this.api = api;
      // testCaseId 不为空则为用例编辑页面
      this.testCaseId = testCaseId;
      this.condition = {components: API_CASE_CONFIGS};
      this.getApiTest(true,true);
      this.visible = true;
      this.$store.state.currentApiCase = undefined;

      //默认最大化
      this.$nextTick(() => {
        this.$refs.testCaseDrawer.setfullScreen();
      });
    },
    add(api) {
      this.api = api;
      this.api.source = "editCase";
      this.condition = {components: API_CASE_CONFIGS};
      this.sysAddition();
      this.visible = true;
      this.$nextTick(() => {
        this.$refs.testCaseDrawer.setfullScreen();
      });
    },
    copy(apiCase) {
      this.api.id = apiCase.apiDefinitionId;
      this.api.versionId = apiCase.versionId;
      if (apiCase && apiCase.request) {
        if (apiCase.request.type === "HTTPSamplerProxy") {
          this.api.protocol = "HTTP";
          this.api.source = "editCase";
        }
        this.api.method = apiCase.request.method
        this.api.name = apiCase.request.name;
        this.api.path = apiCase.request.path;
      }
      if (apiCase.tags) {
        apiCase.tags = JSON.parse(apiCase.tags);
      }
      this.condition = {components: API_CASE_CONFIGS};
      this.sysAddition(apiCase);
      this.visible = true;

      //默认最大化
      this.$nextTick(() => {
        this.$refs.testCaseDrawer.setfullScreen();
      });
    },
    runTestCase(api, testCaseId) {
      if (api && testCaseId) {
        this.api = api;
        this.testCaseId = testCaseId;
        this.condition = {components: API_CASE_CONFIGS};
        this.getTestCase().then(() => {
          this.singleRun(this.apiCaseList[0]);
        });
      }
      this.visible = true;
    },
    saveCase(item, hideAlert) {
      this.$refs.apiCaseItem.saveTestCase(item, hideAlert);
    },
    saveApiAndCase(api) {
      if (api && api.url) {
        api.url = undefined;
      }
      if (api && api.request && api.request.url) {
        api.request.url = undefined;
      }
      this.visible = true;
      this.api = api;
      this.currentApi = api;
      this.addCase();

      //默认最大化
      this.$nextTick(() => {
        this.$refs.testCaseDrawer.setfullScreen();
      });
    },
    setEnvironment(environment) {
      this.environment = environment;
    },
    sysAddition(apiCase) {
      this.condition.projectId = this.projectId;
      this.condition.apiDefinitionId = this.api.id;
      this.apiCaseList = [];
      if (apiCase) {
        this.copyCase(apiCase);
      } else {
        this.addCase();
      }
    },
    apiCaseClose() {
      this.apiCaseList = [];
      this.visible = false;
      if (this.$route.fullPath !== this.$route.path) {
        this.$router.replace({path: '/api/definition'});
      }
      this.$emit('refresh');
    },
    refreshModule() {
      this.$emit('refreshModule');
    },
    runRefresh() {
      this.batchLoadingIds = [];
      this.singleLoading = false;
      this.singleRunId = "";
      // 批量更新最后执行环境
      let obj = {envId: this.environment, show: true};
      this.batchEdit(obj);
      this.runResult = {testId: getUUID()};
      this.$refs.apiCaseItem.runLoading = false;
      this.$success(this.$t('organization.integration.successful_operation'));
      this.$store.state.currentApiCase = {refresh: true};
      this.getTestCase();
    },
    errorRefresh() {
      this.batchLoadingIds = [];
      this.singleLoading = false;
      this.singleRunId = "";
      this.$refs.apiCaseItem.runLoading = false;
    },
    refresh() {
      this.$emit('refresh');
    },
    reLoadCase() {
      this.$emit('reLoadCase');
    },
    formatCase(apiCase) {
      if (apiCase.tags && apiCase.tags.length > 0) {
        apiCase.tags = JSON.parse(apiCase.tags);
      }
      if (Object.prototype.toString.call(apiCase.request).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
        apiCase.request = JSON.parse(apiCase.request);
      }
      if (!apiCase.request.hashTree) {
        apiCase.request.hashTree = [];
      }
      const index = this.runData.findIndex(d => d.name === apiCase.id);
      if (index !== -1) {
        apiCase.active = true;
      } else {
        if (this.condition.id && this.condition.id != "") {
          apiCase.active = true;
        }
      }
      if (apiCase && apiCase.request && apiCase.request.useEnvironment) {
        this.environment = apiCase.request.useEnvironment;
      }
      if (apiCase.request && apiCase.request.hashTree) {
        apiCase.request.hashTree.forEach(item => {
          if (item.type === "Assertions" && !item.document) {
            item.document = {type: "JSON", data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}};
          }
        })
      }
    },
    getTestCase(openCase) {
      return new Promise((resolve) => {
        let commonUseEnvironment = this.$store.state.useEnvironment;
        this.environment = commonUseEnvironment ? commonUseEnvironment : "";
        this.result = this.$get("/api/testcase/findById/" + this.testCaseId, response => {
          let apiCase = response.data;
          if (apiCase) {
            this.formatCase(apiCase);
            if(openCase){
              apiCase.active = true;
            }
            this.apiCaseList = [apiCase];
          }
          resolve();
        });
      });
    },
    getApiLoadCase() {
      if (this.api) {
        this.condition.projectId = this.projectId;
        this.condition.apiDefinitionId = this.api.id;
        this.result = this.$post("/api/testcase/list", this.condition, response => {
          let data = response.data;
          if (data) {
            data.forEach(apiCase => {
              this.formatCase(apiCase);
            });
            this.apiCaseList = data;
          }
        });
      }
    },
    getApiTest(addCase,openCase) {
      if (this.loaded) {
        this.getApiLoadCase();
      } else {
        this.getTestCase(openCase).then(() => {
          if (addCase && !this.loaded && this.apiCaseList.length === 0) {
            this.addCase();
          }
        });
      }
    },
    addCase() {
      if (this.api && this.api.request) {
        // 初始化对象
        let request = {};
        if (this.api.request instanceof Object) {
          request = this.api.request;
        } else {
          request = JSON.parse(this.api.request);
        }
        if (!request.hashTree) {
          request.hashTree = [];
        }
        if (request.backScript) {
          request.hashTree.push(request.backScript);
        }
        let newUuid = getUUID();
        request.id = newUuid;
        let obj = {
          apiDefinitionId: this.api.id,
          name: '',
          priority: 'P0',
          active: true,
          tags: [],
          uuid: newUuid,
          caseStatus: "Underway"
        };
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
      if (this.currentApi.protocol !== "SQL" && this.currentApi.protocol !== "DUBBO" && this.currentApi.protocol !== "dubbo://" && !this.environment) {
        this.$warning(this.$t('api_test.environment.select_environment'));
        return;
      }
      this.runData = [];
      this.singleLoading = true;
      this.singleRunId = row.id;
      row.request.name = row.id;
      row.request.useEnvironment = this.environment;
      row.request.projectId = this.projectId;
      this.runData.push(row.request);
      /*触发执行操作*/
      this.reportId = getUUID().substring(0, 8);
      this.testCaseId = row.id ? row.id : row.request.id;
      this.$emit("refreshCase", this.testCaseId);
    },

    stop(id) {
      let obj = {type: "API", reportId: this.reportId};
      this.$post('/api/automation/stop/batch', [obj], response => {
        this.$emit("stop", id);
        this.singleLoading = false;
        this.$success(this.$t('report.test_stop_success'));
      });
    },
    batchEdit(form) {
      let param = {};
      if (form) {
        param[form.type] = form.value;
        param.ids = [this.testCaseId];
        param.projectId = this.projectId;
        param.envId = form.envId;
        if (this.api) {
          param.protocol = this.api.protocol;
        }
        param.selectAllDate = this.isSelectAllDate;
        param.unSelectIds = this.unSelection;
        param = Object.assign(param, this.condition);
      }
      this.$post('/api/testcase/batch/editByParam', param, () => {
        if (!form.show) {
          this.$success(this.$t('commons.save_success'));
        }
        this.apiCaseList[0].active = true;
      });
    },
    showHistory(id) {
      this.$refs.taskCenter.openHistory(id);
    }
  }
};
</script>

<style scoped>

.ms-drawer >>> .ms-drawer-body {
  margin-top: 40px;
}

</style>
