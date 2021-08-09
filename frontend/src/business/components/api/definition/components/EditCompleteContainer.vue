<template>
  <el-card class="card-content" v-if="isShow && !loading">
    <el-button-group v-if="currentApi.id">
      <el-tooltip class="item" effect="dark" :content="$t('api_test.definition.api_title')" placement="left">
        <el-button plain :class="{active: showApiList}" @click="changeTab('api')" size="small">API</el-button>
      </el-tooltip>
      <el-tooltip class="item" effect="dark" :content="$t('commons.test')" placement="top">
        <el-button plain :class="{active: showTest}" @click="changeTab('test')" size="small">TEST</el-button>
      </el-tooltip>
      <el-tooltip class="item" effect="dark" :content="$t('api_test.definition.case_title')" placement="top">
        <el-button plain :class="{active: showTestCaseList}" @click="changeTab('testCase')" size="small">CASE</el-button>
      </el-tooltip>

      <el-tooltip class="item" effect="dark" content="Mock设置" placement="right" v-if="currentProtocol==='HTTP'">
        <el-button plain :class="{active: showMock}" @click="changeTab('mock')" size="small"> Mock</el-button>
      </el-tooltip>

    </el-button-group>

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
        @createRootModel="createRootModel"
      />
    </div>
    <div v-else-if="showTest" class="ms-api-div">
      <ms-run-test-http-page
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="currentApi"
        :project-id="projectId"
        @saveAsApi="editApi"
        @refresh="refresh"
        v-if="currentProtocol==='HTTP'"
      />
      <ms-run-test-tcp-page
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="currentApi"
        :project-id="projectId"
        @saveAsApi="editApi"
        @refresh="refresh"
        v-if="currentProtocol==='TCP'"
      />
      <ms-run-test-sql-page
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="currentApi"
        :project-id="projectId"
        @saveAsApi="editApi"
        @refresh="refresh"
        v-if="currentProtocol==='SQL'"
      />
      <ms-run-test-dubbo-page
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="currentApi"
        :project-id="projectId"
        @saveAsApi="editApi"
        @refresh="refresh"
        v-if="currentProtocol==='DUBBO'"
      />
    </div>

    <div v-if="showMock" class="ms-api-div">
      <mock-config :base-mock-config-data="baseMockConfigData"/>
    </div>
    <div v-if="showTestCaseList">
      <!--测试用例列表-->
      <api-case-simple-list
        :apiDefinitionId="currentApi.id"
        :trash-enable="false"
        @changeSelectDataRangeAll="changeSelectDataRangeAll"
        @handleCase="handleCase"
        @refreshTable="refresh"
        @showExecResult="showExecResult"
        ref="trashCaseList"/>
    </div>
  </el-card>
</template>

<script>
import MsApiConfig from "./ApiConfig";
import MsRunTestHttpPage from "./runtest/RunTestHTTPPage";
import MsRunTestTcpPage from "./runtest/RunTestTCPPage";
import MsRunTestSqlPage from "./runtest/RunTestSQLPage";
import MsRunTestDubboPage from "./runtest/RunTestDubboPage";
import MockConfig from "@/business/components/api/definition/components/mock/MockConfig";
import ApiCaseSimpleList from "./list/ApiCaseSimpleList";

export default {
  name: "EditCompleteContainer",
  components: {
    MsApiConfig,
    MsRunTestHttpPage,
    MsRunTestTcpPage,
    MsRunTestSqlPage,
    MsRunTestDubboPage,
    MockConfig,
    ApiCaseSimpleList
  },
  data() {
    return {
      isShow: true,
      showApiList: true,
      showTest: false,
      showMock: false,
      showTestCaseList: false,
      baseMockConfigData: {},
      loading: false
    }
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
    if (this.currentApi.id !== null && this.currentProtocol === "HTTP") {
      this.mockSetting();
    }
  },
  watch: {
    showMock() {
      this.mockSetting();
    }
  },
  methods: {
    mockSetting() {
      let mockParam = {};
      mockParam.projectId = this.projectId;
      mockParam.apiId = this.currentApi.id;
      this.$post('/mockConfig/genMockConfig', mockParam, response => {
        let mockConfig = response.data;
        mockConfig.apiName = this.currentApi.name;
        this.baseMockConfigData = mockConfig;
      });
    },
    runTest(data) {
      this.$emit("runTest", data);
    },
    saveApi(data) {
      this.$emit("saveApi", data);
      if (data != null && data.tags != 'null' && data.tags != undefined) {
        if (Object.prototype.toString.call(data.tags).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
          data.tags = JSON.parse(data.tags);
        }
      }
      Object.assign(this.currentApi, data);
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
    changeTab(tabType) {
      this.refreshButtonActiveClass(tabType);
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
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      });
    },
    refreshButtonActiveClass(tabType) {
      if (tabType === "testCase") {
        this.showApiList = false;
        this.showTestCaseList = true;
        this.showTest = false;
        this.showMock = false;
      } else if (tabType === "test") {
        this.showApiList = false;
        this.showTestCaseList = false;
        this.showTest = true;
        this.showMock = false;
      } else if (tabType === "mock") {
        this.showApiList = false;
        this.showTestCaseList = false;
        this.showTest = false;
        this.showMock = true;
      } else {
        this.showApiList = true;
        this.showTestCaseList = false;
        this.showTest = false;
        this.showMock = false;
      }
    }
  },
}
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
</style>
