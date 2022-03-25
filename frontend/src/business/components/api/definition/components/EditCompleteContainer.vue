<template>
  <el-card class="card-content" v-if="isShow && !loading">
    <el-button-group v-if="currentApi.id" style="z-index: 10; position: fixed;">
      <el-button class="item" plain :class="{active: showApiList}" @click="changeTab('api')" size="small">
        API
      </el-button>
      <el-button class="item" plain :class="{active: showTest}" @click="changeTab('test')" size="small">
        TEST
      </el-button>
      <el-button class="item" plain :class="{active: showTestCaseList}" @click="changeTab('testCase')" size="small">
        CASE
      </el-button>
      <el-button class="item" plain :class="{active: showMock}" @click="changeTab('mock')" size="small"
                 v-if="currentProtocol === 'HTTP' || currentProtocol === 'TCP'">
        MOCK
      </el-button>

    </el-button-group>
    <div style="height: 40px"></div>
    <template v-slot:header>
      <slot name="header"></slot>
    </template>
    <slot></slot>
    <div v-if="showApiList">
      <ms-api-config
        :syncTabs="syncTabs" ref="apiConfig"
        :current-api="currentApi"
        :project-id="projectId"
        :currentProtocol="currentProtocol"
        :moduleOptions="moduleOptions"
        @runTest="runTest"
        @saveApi="saveApi"
        @checkout="checkout"
        @changeTab="changeTab"
        @createRootModel="createRootModel"
      />
    </div>
    <div v-else-if="showTest">
      <ms-run-test-http-page
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="currentApi"
        :project-id="projectId"
        @saveAsApi="editApi"
        @saveAsCase="saveAsCase"
        @refresh="refresh"
        ref="httpTestPage"
        v-if="currentProtocol==='HTTP'"
      />
      <ms-run-test-tcp-page
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="currentApi"
        :project-id="projectId"
        @saveAsApi="editApi"
        @saveAsCase="saveAsCase"
        @refresh="refresh"
        ref="tcpTestPage"
        v-if="currentProtocol==='TCP'"
      />
      <ms-run-test-sql-page
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="currentApi"
        :project-id="projectId"
        @saveAsApi="editApi"
        @saveAsCase="saveAsCase"
        @refresh="refresh"
        v-if="currentProtocol==='SQL'"
      />
      <ms-run-test-dubbo-page
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="currentApi"
        :project-id="projectId"
        @saveAsApi="editApi"
        @saveAsCase="saveAsCase"
        @refresh="refresh"
        v-if="currentProtocol==='DUBBO'"
      />
    </div>

    <div v-if="showMock && (currentProtocol === 'HTTP' || currentProtocol === 'TCP')">
      <mock-tab :base-mock-config-data="baseMockConfigData" @redirectToTest="redirectToTest" :version-name="currentApi.versionName"
                :is-tcp="currentProtocol === 'TCP'"/>
    </div>
    <div v-if="showTestCaseList">
      <!--测试用例列表-->
      <api-case-simple-list
        class="api-case-simple-list"
        :apiDefinitionId="currentApi.id"
        :apiDefinition="currentApi"
        :current-version="currentApi.versionId"
        :trash-enable="false"
        @changeSelectDataRangeAll="changeSelectDataRangeAll"
        @handleCase="handleCase"
        @refreshTable="refresh"
        @showExecResult="showExecResult"
        ref="trashCaseList"/>
    </div>
    <!-- 加载用例 -->
    <ms-api-case-list
      :createCase="createCase"
      :currentApi="api"
      @reLoadCase="reLoadCase"
      ref="caseList"/>
  </el-card>
</template>

<script>
import MsApiConfig from "./ApiConfig";
import MsRunTestHttpPage from "./runtest/RunTestHTTPPage";
import MsRunTestTcpPage from "./runtest/RunTestTCPPage";
import MsRunTestSqlPage from "./runtest/RunTestSQLPage";
import MsRunTestDubboPage from "./runtest/RunTestDubboPage";
import MockTab from "@/business/components/api/definition/components/mock/MockTab";
import TcpMockConfig from "@/business/components/api/definition/components/mock/TcpMockConfig";
import ApiCaseSimpleList from "./list/ApiCaseSimpleList";
import MsApiCaseList from "./case/ApiCaseList";
import {getUUID} from "@/common/js/utils";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import _ from 'lodash';

export default {
  name: "EditCompleteContainer",
  components: {
    MsApiConfig,
    MsRunTestHttpPage,
    MsRunTestTcpPage,
    MsRunTestSqlPage,
    MsRunTestDubboPage,
    MockTab,
    TcpMockConfig,
    ApiCaseSimpleList,
    MsApiCaseList
  },
  data() {
    return {
      isShow: true,
      showApiList: true,
      showTest: false,
      showMock: false,
      showTestCaseList: false,
      baseMockConfigData: {},
      loading: false,
      createCase: "",
      api: {},
    };
  },
  props: {
    activeDom: String,
    isShowChangeButton: {
      type: Boolean,
      default: true
    },
    currentApi: {},
    moduleOptions: {},
    currentProtocol: String,
    syncTabs: Array,
    projectId: String,
    selectNodeIds: Array,
    visible: {
      type: Boolean,
      default: false,
    },
  },
  created() {
    this.refreshButtonActiveClass(this.activeDom);
    if (this.currentApi.id && (this.currentProtocol === "HTTP" || this.currentProtocol === "TCP")) {
      this.mockSetting();
    }
    this.formatApi();
  },
  watch: {
    showMock() {
      this.mockSetting();
    },
    '$store.state.currentApiCase.case'() {
      if (this.$store.state.currentApiCase && this.$store.state.currentApiCase.api) {
        this.refreshButtonActiveClass("testCase");
      }
    },
    '$store.state.currentApiCase.mock'() {
      this.mockSetting();
      this.refreshButtonActiveClass("mock");
    }
  },
  methods: {
    reLoadCase() {
      this.$refs.trashCaseList.initTable();
    },
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
            stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {
              type: "JSON",
              data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}
            };
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
    formatApi() {
      if (this.currentApi.response != null && this.currentApi.response != 'null' && this.currentApi.response != undefined) {
        if (Object.prototype.toString.call(this.currentApi.response).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
          this.currentApi.response = JSON.parse(this.currentApi.response);
        }
      }
      if (this.currentApi.request != null && this.currentApi.request != 'null' && this.currentApi.request != undefined) {
        if (Object.prototype.toString.call(this.currentApi.request).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
          this.currentApi.request = JSON.parse(this.currentApi.request);
          if (this.currentApi.request.body && !this.currentApi.request.body.type) {
            let tempRequest = _.cloneDeep(this.currentApi.request);
            tempRequest.body = {type: null};
            this.currentApi.request = tempRequest;
          }
        }
      }
      if (this.currentApi && this.currentApi.request && !this.currentApi.request.hashTree) {
        this.currentApi.request.hashTree = [];
      }
      if (this.currentApi && this.currentApi.request && this.currentApi.request.body && !this.currentApi.request.body.binary) {
        this.currentApi.request.body.binary = [];
      }
      if (this.currentApi.request) {
        this.currentApi.request.clazzName = TYPE_TO_C.get(this.currentApi.request.type);
        this.sort(this.currentApi.request.hashTree);
      }
    },
    mockSetting() {
      let mockParam = {};
      mockParam.projectId = this.projectId;
      if (this.currentApi.id) {
        mockParam.apiId = this.currentApi.id;
        this.$post('/mockConfig/genMockConfig', mockParam, response => {
          let mockConfig = response.data;
          mockConfig.apiName = this.currentApi.name;
          mockConfig.versionName = this.currentApi.versionName;
          this.baseMockConfigData = mockConfig;
        });
      }
    },
    runTest(data) {
      this.$emit("runTest", data);
    },
    saveApi(data) {
      this.$emit("saveApi", data);
      if (data != null && data.tags !== 'null' && data.tags !== undefined) {
        if (Object.prototype.toString.call(data.tags) === "[object String]") {
          data.tags = JSON.parse(data.tags);
        }
      }
      Object.assign(this.currentApi, data);
      this.currentApi.isCopy = false;
      this.mockSetting();
      this.reload();
    },
    createRootModel() {
      this.$emit("createRootModel");
    },
    editApi(data) {
      this.$emit("editApi", data);
    },
    refresh() {
      this.$emit("refresh");
    },
    checkout(data) {
      Object.assign(this.currentApi, data);
      this.reload();
    },
    changeTab(tabType) {
      this.beforeChangeTab();
      this.refreshButtonActiveClass(tabType);
    },
    beforeChangeTab(){
      //关闭接口用例弹窗
      this.$refs.caseList.close();
    },
    redirectToTest(param) {
      this.refreshButtonActiveClass("test");
      this.$nextTick(() => {
        if (this.currentProtocol === "HTTP" && this.$refs.httpTestPage) {
          let requestParam = null;
          if (param.params) {
            requestParam = param.params;
          }
          this.$refs.httpTestPage.setRequestParam(requestParam,true);
        } else if (this.currentProtocol === "TCP" && this.$refs.tcpTestPage) {
          this.$refs.tcpTestPage.setRequestParam(param,true);
        }
      });
    },
    removeListener() {
      if (this.$refs && this.$refs.apiConfig) {
        this.$refs.apiConfig.removeListener();
      }
    },
    changeSelectDataRangeAll() {
      this.$emit("changeSelectDataRangeAll");
    },
    handleCase(api) {
      this.$emit("handleCase", api);
    },
    showExecResult(data) {
      this.$emit("showExecResult", data);
    },
    addListener() {
      if (this.$refs && this.$refs.apiConfig) {
        this.$refs.apiConfig.addListener();
      }
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    saveAsCase(api) {
      this.showApiList = false;
      this.showTestCaseList = true;
      this.showTest = false;
      this.showMock = false;
      this.createCase = getUUID();
      this.api = api;
      this.$refs.caseList.open();
    },
    refreshButtonActiveClass(tabType) {
      if (tabType === "testCase") {
        this.showApiList = false;
        this.showTestCaseList = true;
        this.showTest = false;
        this.showMock = false;
        this.$store.state.currentApiCase = {case: true};
      } else if (tabType === "test") {
        this.showApiList = false;
        this.showTestCaseList = false;
        this.showTest = true;
        this.showMock = false;
        this.$store.state.currentApiCase = undefined;
      } else if (tabType === "mock") {
        this.showApiList = false;
        this.showTestCaseList = false;
        this.showTest = false;
        this.showMock = true;
        this.$store.state.currentApiCase = undefined;
      } else {
        this.syncApi();
      }
    },
    changeApi() {
      this.showApiList = true;
      this.showTestCaseList = false;
      this.showTest = false;
      this.showMock = false;
      this.$store.state.currentApiCase = undefined;
    },
    syncApi() {
      if (this.syncTabs && this.syncTabs.length > 0 && this.syncTabs.includes(this.currentApi.id)) {
        // 标示接口在其他地方更新过，当前页面需要同步
        let url = "/api/definition/get/";
        this.$get(url + this.currentApi.id, response => {
          if (response.data) {
            let request = JSON.parse(response.data.request);
            let index = this.syncTabs.findIndex(item => {
              if (item === this.currentApi.id) {
                return true;
              }
            });
            this.syncTabs.splice(index, 1);
            this.currentApi.request = request;
            this.changeApi();
          }
        });
      } else {
        this.changeApi();
      }
    }
  }
};
</script>

<style scoped>
.active {
  border: solid 1px #6d317c !important;
  background-color: var(--primary_color) !important;
  color: #FFFFFF !important;
}

.case-button {
  border-left: solid 1px var(--primary_color);
}

.item {
  border: solid 1px var(--primary_color);
}


.api-case-simple-list >>> .el-table {
  height: calc(100vh - 262px) !important;
}

/deep/ .ms-opt-btn {
  position: fixed;
  right: 50px;
  z-index: 1;
  top: 128px;
  float: right;
  margin-right: 20px;
  margin-top: 5px;
}
</style>
