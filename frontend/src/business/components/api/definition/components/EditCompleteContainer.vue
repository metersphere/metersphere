<template>
  <el-card v-if="isShow && !loading" class="ms-api-card">
    <ms-container v-loading="loading">
      <ms-aside-container :height="leftHeight">
        <api-base-info
          ref="apiBaseInfo"
          :api-template="apiTemplate"
          :currentProtocol="currentProtocol"
          :custom-field-form="customFieldForm"
          :custom-field-rules="customFieldRules"
          :form="currentApi"
          :is-form-alive="isFormAlive"
          :isloading="loading"
          :maintainer-options="maintainerOptions"
          :module-options="moduleOptions"
        />
      </ms-aside-container>
      <ms-main-container class="ms-api-main-container">
        <el-button-group v-if="currentApi.id" style="z-index: 10; position: fixed;">
          <el-button :class="{active: showApiList}" class="item" plain size="small" @click="changeTab('api')">
            API
          </el-button>
          <el-button :class="{active: showTest}" class="item" plain size="small" @click="changeTab('test')">
            TEST
          </el-button>
          <el-button :class="{active: showTestCaseList}" class="item" plain size="small" @click="changeTab('testCase')">
            CASE
          </el-button>
          <el-button v-if="currentProtocol === 'HTTP' || currentProtocol === 'TCP'" :class="{active: showMock}"
                     class="item" plain size="small"
                     @click="changeTab('mock')">
            MOCK
          </el-button>

        </el-button-group>
        <div v-if='currentApi.id' style="height: 40px"></div>
        <template v-slot:header>
          <slot name="header"></slot>
        </template>
        <slot></slot>
        <div v-if="showApiList">
          <ms-api-config
            ref="apiConfig" :api-template="apiTemplate"
            :current-api="currentApi"
            :currentProtocol="currentProtocol"
            :moduleOptions="moduleOptions"
            :project-id="projectId"
            :syncTabs="syncTabs"
            :validateTrue.sync="validateTrue"
            @changeTab="changeTab"
            @checkout="checkout"
            @createRootModel="createRootModel"
            @runTest="runTest"
            @saveApi="saveApi"
            @validateBasic="validateBasic"
          />
        </div>
        <div v-else-if="showTest">
          <ms-run-test-http-page
            v-if="currentProtocol==='HTTP'"
            ref="httpTestPage"
            :api-data="currentApi"
            :currentProtocol="currentProtocol"
            :project-id="projectId"
            :syncTabs="syncTabs"
            @refresh="refresh"
            @saveAsApi="editApi"
            @saveAsCase="saveAsCase"
          />
          <ms-run-test-tcp-page
            v-if="currentProtocol==='TCP'"
            ref="tcpTestPage"
            :api-data="currentApi"
            :currentProtocol="currentProtocol"
            :project-id="projectId"
            :syncTabs="syncTabs"
            @refresh="refresh"
            @saveAsApi="editApi"
            @saveAsCase="saveAsCase"
          />
          <ms-run-test-sql-page
            v-if="currentProtocol==='SQL'"
            :api-data="currentApi"
            :currentProtocol="currentProtocol"
            :project-id="projectId"
            :syncTabs="syncTabs"
            @refresh="refresh"
            @saveAsApi="editApi"
            @saveAsCase="saveAsCase"
          />
          <ms-run-test-dubbo-page
            v-if="currentProtocol==='DUBBO'"
            :api-data="currentApi"
            :currentProtocol="currentProtocol"
            :project-id="projectId"
            :syncTabs="syncTabs"
            @refresh="refresh"
            @saveAsApi="editApi"
            @saveAsCase="saveAsCase"
          />
        </div>

        <div v-if="showMock && (currentProtocol === 'HTTP' || currentProtocol === 'TCP')">
          <mock-tab :base-mock-config-data="baseMockConfigData" :form="currentApi"
                    :is-tcp="currentProtocol === 'TCP'"
                    :version-name="currentApi.versionName" @redirectToTest="redirectToTest"/>
        </div>
        <div v-if="showTestCaseList">
          <!--测试用例列表-->
          <api-case-simple-list
            ref="trashCaseList"
            :apiDefinition="currentApi"
            :apiDefinitionId="currentApi.id"
            :current-version="currentApi.versionId"
            :trash-enable="false"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            @handleCase="handleCase"
            @refreshTable="refresh"
            @showExecResult="showExecResult"/>
        </div>
        <!-- 加载用例 -->
        <ms-api-case-list
          ref="caseList"
          :createCase="createCase"
          :currentApi="api"
          @reLoadCase="reLoadCase"/>
      </ms-main-container>
    </ms-container>
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
import MsContainer from "@/business/components/common/components/MsContainer";
import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import ApiBaseInfo from "@/business/components/api/definition/components/complete/ApiBaseInfo";
import {getProjectMemberOption} from "@/network/user";
import {buildCustomFields, getApiFieldTemplate, parseCustomField} from "@/common/js/custom_field";

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
    MsApiCaseList,
    MsContainer, MsAsideContainer, MsMainContainer,
    ApiBaseInfo
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
      createCase: '',
      api: {},
      maintainerOptions: [],
      isFormAlive: true,
      validateTrue: '',
      apiTemplate: {},
      customFieldRules: {},
      customFieldForm: null,
      currentValidateName: '',
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
  computed: {
    leftHeight() {
      return 'calc(100vh - 100px)';
    }
  },
  created() {
    this.refreshButtonActiveClass(this.activeDom);
    getApiFieldTemplate(this)
      .then((template) => {
        this.apiTemplate = template;
        this.$store.commit('setApiTemplate', this.apiTemplate);
        this.customFieldForm = parseCustomField(this.currentApi, this.apiTemplate, this.customFieldRules);
      });
    if (this.currentApi.id && (this.currentProtocol === "HTTP" || this.currentProtocol === "TCP")) {
      this.mockSetting();
    }
    this.formatApi();
    this.getMaintainerOptions();
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
    reloadForm() {
      this.isFormAlive = false;
      this.$nextTick(() => (this.isFormAlive = true));
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
          if (stepArray[i].type === 'Assertions' && !stepArray[i].document) {
            stepArray[i].document = {
              type: 'JSON',
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
      if (data != null && data.tags !== 'null' && data.tags !== undefined) {
        if (Object.prototype.toString.call(data.tags) === "[object String]") {
          data.tags = JSON.parse(data.tags);
        }
      }
      Object.assign(this.currentApi, data);
      this.currentApi.isCopy = false;
      this.mockSetting();
      this.$emit("saveApi", data);
      this.reload();

    },
    validateBasic(data) {
      let baseInfoValidate = this.$refs.apiBaseInfo.validateForm();
      if (!baseInfoValidate) {
        return false;
      }
      let customValidate = this.$refs.apiBaseInfo.validateCustomForm();
      if (!customValidate) {
        let customFieldFormFields = this.$refs.apiBaseInfo.getCustomFields();
        for (let i = 0; i < customFieldFormFields.length; i++) {
          let customField = customFieldFormFields[i];
          if (customField.validateState === 'error') {
            if (this.currentValidateName) {
              this.currentValidateName = this.currentValidateName + "," + customField.label
            } else {
              this.currentValidateName = customField.label
            }
          }
        }
        this.$warning(this.currentValidateName + this.$t('commons.cannot_be_null'));
        this.currentValidateName = '';
        return false;
      }
      buildCustomFields(this.currentApi, data, this.apiTemplate);
      this.$refs.apiConfig.saveApi(data);
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
    beforeChangeTab() {
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
          this.$refs.httpTestPage.setRequestParam(requestParam, true);
        } else if (this.currentProtocol === "TCP" && this.$refs.tcpTestPage) {
          this.$refs.tcpTestPage.setRequestParam(param, true);
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
    },
    getMaintainerOptions() {
      getProjectMemberOption(data => {
        this.maintainerOptions = data;
      });
    },
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

/deep/ .ms-opt-btn {
  position: fixed;
  right: 50px;
  z-index: 1;
  top: 90px;
  float: right;
  margin-right: 20px;
  margin-top: 5px;
}

.ms-api-main-container {
  height: calc(100vh - 100px);
}

</style>
