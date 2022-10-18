<template>
  <div v-if="visible">
    <ms-drawer :size="60" @close="apiCaseClose" direction="bottom" ref="testCaseDrawer">
      <template v-slot:header>
        <api-case-header
          :api="api"
          @setEnvironment="setEnvironment"
          @addCase="addCase"
          @saveCase="saveCase"
          :condition="condition"
          :priorities="priorities"
          :project-id="projectId"
          :useEnvironment="environment"
          :is-case-edit="isCaseEdit"
          :button-text="saveButtonText"
          ref="header" v-if="refreshHeader"/>
      </template>

      <el-container>
        <el-main>
          <div v-for="item in apiCaseList" :key="item.id ? item.id : item.uuid">
            <api-case-item
              :loading="singleLoading && singleRunId ===item.id || batchLoadingIds.indexOf(item.id) > -1"
              @refresh="refresh"
              @singleRun="singleRun"
              @stop="stop"
              @refreshModule="refreshModule"
              @copyCase="copyCase"
              @showExecResult="showExecResult"
              @showHistory="showHistory"
              @reLoadCase="reLoadCase"
              :environment="environment"
              @setSelectedCaseId="setSelectedCaseId"
              :is-case-edit="isCaseEdit"
              :api="api"
              :currentApi="currentApi"
              :loaded="loaded"
              :maintainerOptions="maintainerOptions"
              :api-case="item" ref="apiCaseItem"/>
          </div>
        </el-main>
      </el-container>
    </ms-drawer>

    <!-- 执行组件 -->
    <ms-run
      :debug="false"
      :reportId="reportId"
      :run-data="runData"
      :env-map="envMap"
      :edit-case-request="true"
      @runRefresh="runRefresh"
      @errorRefresh="errorRefresh" ref="runTest"/>
    <ms-task-center ref="taskCenter" :show-menu="false"/>
  </div>
</template>
<script>

import {apiTestCaseList, editApiCaseByParam, getCaseById} from "@/api/api-test-case";
import {getMaintainer} from "@/api/project";
import ApiCaseHeader from "./ApiCaseHeader";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getUUID} from "metersphere-frontend/src/utils";
import MsDrawer from "metersphere-frontend/src/components/MsDrawer";
import {CASE_ORDER} from "../../model/JsonData";
import {API_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {parseEnvironment} from "@/business/environment/model/EnvironmentModel";
import {Body} from "@/business/definition/model/ApiTestModel";
import {getEnvironmentByProjectId} from "metersphere-frontend/src/api/environment";
import {execBatchStop} from "@/api/scenario";
import {useApiStore} from "@/store";

const store = useApiStore();
export default {
  name: 'EditApiCase',
  components: {
    MsDrawer,
    ApiCaseHeader,
    MsRun: () => import("../Run"),
    ApiCaseItem: () => import("./ApiCaseItem"),
    MsTaskCenter: () => import("@/business/history/task/ApiTaskCenter"),
  },
  props: {
    createCase: String,
    loaded: {
      type: Boolean,
      default: false
    },
    saveButtonText: String,
    refreshSign: String,
    currentApi: {
      type: Object
    },
  },
  data() {
    return {
      apiStore: useApiStore(),
      result: false,
      environment: "",
      priorities: CASE_ORDER,
      apiCaseList: [],
      batchLoadingIds: [],
      singleLoading: false,
      singleRunId: "",
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
      environments: [],
      refreshHeader: true
    };
  },
  watch: {
    'storeUseEnvironment': function () {
      this.setEnvironment(store.useEnvironment);
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
    if (!this.environment && store.useEnvironment) {
      this.environment = store.useEnvironment;
    }
    this.getMaintainerOptions();
  },
  computed: {
    storeUseEnvironment() {
      return store.useEnvironment;
    },
    isCaseEdit() {
      return this.testCaseId ? true : false;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    getMaintainerOptions() {
      getMaintainer().then(response => {
        this.maintainerOptions = response.data;
      });
    },
    close() {
      if (this.$refs.testCaseDrawer) {
        this.$refs.testCaseDrawer.close();
      }
    },
    open(api, testCaseId) {
      this.api = api;
      // testCaseId 不为空则为用例编辑页面
      this.testCaseId = testCaseId;
      this.condition = {components: API_CASE_CONFIGS};
      this.getApiTest(true, true);
      this.visible = true;
      store.currentApiCase = undefined;

      //默认最大化
      this.$nextTick(() => {
        if (this.$refs.testCaseDrawer) {
          this.$refs.testCaseDrawer.setfullScreen();
        }
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
        this.refreshHeader = false;
        this.$nextTick(() => {
          this.refreshHeader = true;
        });
        this.api = api;
        this.testCaseId = testCaseId;
        this.condition = {components: API_CASE_CONFIGS};
        this.getTestCase().then(() => {
          this.singleRun(this.apiCaseList[0]);
        });
      }
      this.visible = true;
    },
    setSelectedCaseId(caseId) {
      this.selectCaseId = caseId;
    },
    saveCase(hideAlert) {
      let index = 0;
      if (this.selectCaseId && this.selectCaseId !== '') {
        for (let i = 0; i < this.apiCaseList.length; i++) {
          if (this.apiCaseList[i].id === this.selectCaseId) {
            index = i;
          }
        }
      }
      if (this.apiCaseList && this.apiCaseList.length !== 0) {
        let item = this.apiCaseList[index];
        this.$refs.apiCaseItem[index].saveTestCase(item, hideAlert);
      }
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
      if (this.currentApi && api) {
        Object.assign(this.currentApi, api);
      }
      this.addCase();

      //默认最大化
      this.$nextTick(() => {
        this.$refs.testCaseDrawer.setfullScreen();
      });
    },
    setEnvironment(environment) {
      if (this.environment !== environment) {
        if (this.apiCaseList && this.apiCaseList.length > 0) {
          if (this.apiCaseList[0].request && this.apiCaseList[0].request.hashTree) {
            this.setOwnEnvironment(this.apiCaseList[0].request.hashTree, environment);
          }
        }
      }
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
      if (this.apiCaseList && this.apiCaseList.length > 0) {
        let message = "";
        if (store.apiCaseMap.has(this.apiCaseList[0].id) && store.apiCaseMap.get(this.apiCaseList[0].id) > 1) {
          message += this.apiCaseList[0].name + "，";
        }
        if (this.apiCaseList[0].type === 'AddCase') {
          message += this.apiCaseList[0].name + "，";
        }
        if (message !== "") {
          this.$alert(this.$t('commons.api_case') + " [ " + message.substr(0, message.length - 1) + " ] " + this.$t('commons.confirm_info'), '', {
            confirmButtonText: this.$t('commons.confirm'),
            cancelButtonText: this.$t('commons.cancel'),
            callback: (action) => {
              if (action === 'confirm') {
                store.apiCaseMap.delete(this.apiCaseList[0].id);
                this.apiCaseList = [];
                this.visible = false;
                if (this.$route.fullPath !== this.$route.path) {
                  this.$router.replace({path: '/api/definition'});
                }
                this.$emit('refresh');
              }
            }
          });
        } else {
          this.apiCaseList = [];
          this.visible = false;
          if (this.$route.fullPath !== this.$route.path) {
            this.$router.replace({path: '/api/definition'});
          }
          this.$emit('refresh');
        }
      } else {
        this.apiCaseList = [];
        this.visible = false;
        if (this.$route.fullPath !== this.$route.path) {
          this.$router.replace({path: '/api/definition'});
        }
        this.$emit('refresh');
      }

    },
    refreshModule() {
      this.$emit('refreshModule');
    },
    setNewSource(environment, obj) {
      if (environment.config && environment.config.databaseConfigs && environment.config.databaseConfigs.length > 0) {
        let dataSources = environment.config.databaseConfigs.filter(item => item.name === obj.targetDataSourceName);
        if (dataSources && dataSources.length > 0) {
          obj.dataSourceId = dataSources[0].id;
        } else {
          obj.dataSourceId = environment.config.databaseConfigs[0].id;
        }
      }
    },
    getEnvironments(obj, env) {
      if (this.environments.length === 0) {
        getEnvironmentByProjectId(this.projectId).then(response => {
          this.environments = response.data;
          // 获取原数据源名称
          if (env === obj.originalEnvironmentId && obj.originalDataSourceId) {
            obj.dataSourceId = obj.originalDataSourceId;
            obj.environmentId = env;
            this.getTargetSourceName(this.environments, obj);
          }
          if (!obj.targetDataSourceName) {
            this.getTargetSourceName(this.environments, obj);
          }

          // 设置新环境
          obj.environmentId = env;
          // 设置新数据源
          let envs = this.environments.filter(tab => tab.id === env);
          if (envs && envs.length > 0) {
            this.setNewSource(envs[0], obj);
          }
        });
      } else {
        // 获取原数据源名称
        if (env === obj.originalEnvironmentId && obj.originalDataSourceId) {
          obj.dataSourceId = obj.originalDataSourceId;
          obj.environmentId = env;
          this.getTargetSourceName(this.environments, obj);
        }
        if (!obj.targetDataSourceName) {
          this.getTargetSourceName(this.environments, obj);
        }
        // 设置新环境
        obj.environmentId = env;
        // 设置新数据源
        let envs = this.environments.filter(tab => tab.id === env);
        if (envs && envs.length > 0) {
          this.setNewSource(envs[0], obj);
        }
      }
    },
    getTargetSourceName(environments, obj) {
      environments.forEach(environment => {
        parseEnvironment(environment);
        // 找到原始环境和数据源名称
        if (environment.id === obj.environmentId) {
          if (environment.config && environment.config.databaseConfigs) {
            environment.config.databaseConfigs.forEach(item => {
              if (item.id === obj.dataSourceId) {
                obj.targetDataSourceName = item.name;
              }
            });
          }
        }
      });
    },
    setOwnEnvironment(scenarioDefinition, env) {
      for (let i in scenarioDefinition) {
        let typeArray = ["JDBCPostProcessor", "JDBCSampler", "JDBCPreProcessor"]
        if (typeArray.indexOf(scenarioDefinition[i].type) !== -1) {
          this.getEnvironments(scenarioDefinition[i], env);
        }
        if (scenarioDefinition[i].hashTree && scenarioDefinition[i].hashTree.length > 0) {
          this.setOwnEnvironment(scenarioDefinition[i].hashTree, env);
        }
      }
    },
    runRefresh(data) {
      this.batchLoadingIds = [];
      this.singleLoading = false;
      this.singleRunId = "";
      this.apiCaseList[0].active = true;
      if (data) {
        let status = data.status === 'FAKE_ERROR' ? data.status : data.error > 0 ? "Error" : "Success";
        this.apiCaseList[0].execResult = status;
        this.apiCaseList[0].responseData = data;
        this.$refs.apiCaseItem.runLoading = false;
        store.currentApiCase = {refresh: true, id: data.id, status: status};
      }
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
        if (apiCase.request.protocol === 'HTTP') {
          if (!apiCase.request.body) {
            apiCase.request.body = new Body();
          }
          if (!apiCase.request.headers) {
            apiCase.request.headers = [];
          }
          if (!apiCase.request.rest) {
            apiCase.request.rest = [];
          }
          if (!apiCase.request.arguments) {
            apiCase.request.arguments = [
              {
                contentType: "text/plain",
                enable: true,
                file: false,
                required: false,
                type: "text",
                urlEncode: false,
                valid: false
              }
            ];
          }
        }
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
        let commonUseEnvironment = store.useEnvironment;
        this.environment = commonUseEnvironment ? commonUseEnvironment : "";
        getCaseById(this.testCaseId).then(response => {
          let apiCase = response.data;
          if (apiCase) {
            this.formatCase(apiCase);
            if (openCase) {
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
        this.result = apiTestCaseList(this.condition).then(response => {
          let data = response.data;
          if (data && data.length > 0) {
            data.forEach(apiCase => {
              this.formatCase(apiCase);
            });
            this.apiCaseList = data;
          } else {
            this.$warning(this.$t('api_case.please_add_api_case'));
            return;
          }
        });
      }
    },
    getApiTest(addCase, openCase) {
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
          caseStatus: "Underway",
          type: 'AddCase'
        };
        request.projectId = getCurrentProjectID();
        obj.request = request;
        this.apiCaseList.unshift(obj);
      }
    },
    copyCase(data) {
      data.type = 'AddCase';
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
      let methods = ["SQL", "DUBBO", "dubbo://", "TCP"];
      if (row.apiMethod && methods.indexOf(row.apiMethod) === -1 && (!this.environment || this.environment === undefined)) {
        this.$warning(this.$t('api_test.environment.select_environment'));
        return;
      }
      this.runData = [];
      this.singleLoading = true;
      this.singleRunId = row.id;
      row.request.name = row.id;
      if (row.apiMethod !== "SQL" && row.apiMethod !== "DUBBO" && row.apiMethod !== "dubbo://") {
        row.request.useEnvironment = this.environment;
      } else {
        row.request.useEnvironment = row.request.environmentId;
      }

      row.request.projectId = this.projectId;
      row.request.id = row.id;
      this.runData.push(row.request);
      /*触发执行操作*/
      this.reportId = getUUID().substring(0, 8);
      this.testCaseId = row.id ? row.id : row.request.id;
      this.$emit("refreshCase", this.testCaseId);
    },

    stop(id) {
      let obj = {type: "API", reportId: this.reportId};
      execBatchStop([obj]).then(response => {
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
      editApiCaseByParam(param).then(() => {
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

.ms-drawer :deep(.ms-drawer-body) {
  margin-top: 40px;
}

</style>
