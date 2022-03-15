<template>
  <div>
    <el-tabs v-model="activeName" type="card" class="api-info-tab" @tab-click="tabChange">
      <el-tab-pane label="API" name="api">
        <api-information :api-info="apiInfo" :api="currentApi"  style="margin:5px 5px 5px 5px">
          <template v-slot:headerRight>
            <div style="float: right;margin-right: 20px; position: unset" class="ms-opt-btn">
              <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
                <i class="el-icon-star-off"
                   style="color: #783987; font-size: 25px; margin-right: 5px; position: relative; top: 5px; cursor: pointer "
                   @click="saveFollow"/>
              </el-tooltip>
              <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
                <i class="el-icon-star-on"
                   style="color: #783987; font-size: 28px; margin-right: 5px; position: relative; top: 5px; cursor: pointer "
                   @click="saveFollow"/>
              </el-tooltip>
              <el-link type="primary" style="margin-right: 5px" @click="openHis" v-if="apiInfo.id">
                {{ $t('operating_log.change_history') }}
              </el-link>
              <!--  版本历史 -->
              <ms-version-history v-xpack
                                  ref="versionHistory"
                                  :version-data="versionData"
                                  :current-id="apiInfo.id"
                                  :test-users="maintainerOptions" :use-external-users="true"
                                  @compare="compare" @checkout="checkout" @create="create" @del="del"/>
              <el-button type="primary" size="small" @click="editApi" title="ctrl + s"
                         v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']">{{ $t('commons.edit') }}
              </el-button>
            </div>
          </template>
        </api-information>
      </el-tab-pane>
      <el-tab-pane label="TEST" name="test">
        <div style="margin:5px 5px 5px 5px">
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
            v-else-if="currentProtocol==='TCP'"
          />
          <ms-run-test-sql-page
            :syncTabs="syncTabs"
            :currentProtocol="currentProtocol"
            :api-data="currentApi"
            :project-id="projectId"
            @saveAsApi="editApi"
            @saveAsCase="saveAsCase"
            @refresh="refresh"
            v-else-if="currentProtocol==='SQL'"
          />
          <ms-run-test-dubbo-page
            :syncTabs="syncTabs"
            :currentProtocol="currentProtocol"
            :api-data="currentApi"
            :project-id="projectId"
            @saveAsApi="editApi"
            @saveAsCase="saveAsCase"
            @refresh="refresh"
            v-else-if="currentProtocol==='DUBBO'"
          />
        </div>
      </el-tab-pane>
      <el-tab-pane label="CASE" name="testCase">
        <!--测试用例列表-->
        <api-case-simple-list
          class="api-case-simple-list"
          :apiDefinitionId="currentApi.id"
          :apiDefinition="currentApi"
          :current-version="currentApi.versionId"
          :trash-enable="false"
          :test-users="maintainerOptions" :use-external-users="true"
          @changeSelectDataRangeAll="changeSelectDataRangeAll"
          @handleCase="handleCase"
          @refreshTable="refresh"
          @showExecResult="showExecResult" style="margin:5px 5px 5px 5px"
          ref="trashCaseList"/>
      </el-tab-pane>
      <el-tab-pane label="MOCK" name="mock" v-if="currentProtocol === 'HTTP' || currentProtocol === 'TCP'">
        <div style="margin:5px 5px 5px 5px">
          <mock-tab :base-mock-config-data="baseMockConfigData" @redirectToTest="redirectToTest"
                    :version-name="currentApi.versionName" :api-id="apiInfo.id"
                    :is-tcp="currentProtocol === 'TCP'"/>
        </div>
      </el-tab-pane>
    </el-tabs>
    <ms-change-history ref="changeHistory"/>
    <!-- 加载用例 -->
    <ms-api-case-list
      :createCase="createCase"
      :test-users="maintainerOptions" :use-external-users="true"
      :currentApi="api"
      @reLoadCase="reLoadCase"
      ref="caseList"/>
  </div>
</template>

<script>
import MsApiConfig from "@/business/components/api/definition/components/ApiConfig";
import MsRunTestHttpPage from "@/business/components/api/definition/components/runtest/RunTestHTTPPage";
import MsRunTestTcpPage from "@/business/components/api/definition/components/runtest/RunTestTCPPage";
import MsRunTestSqlPage from "@/business/components/api/definition/components/runtest/RunTestSQLPage";
import MsRunTestDubboPage from "@/business/components/api/definition/components/runtest/RunTestDubboPage";
import MockTab from "@/business/components/api/definition/components/mock/MockTab";
import TcpMockConfig from "@/business/components/api/definition/components/mock/TcpMockConfig";
import ApiCaseSimpleList from "@/business/components/api/definition/components/list/ApiCaseSimpleList";
import MsApiCaseList from "@/business/components/api/definition/components/case/ApiCaseList";
import {getCurrentProjectID, getUUID, hasLicense} from "@/common/js/utils";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import _ from 'lodash';
import ApiInformation from "@/business/components/api/definition/components/document/components/ApiInformation";
import MsVersionHistory from "@/business/components/xpack/version/VersionHistory";
import {createComponent} from "@/business/components/api/definition/components/jmeter/components";
import MsChangeHistory from "@/business/components/history/ChangeHistory";

export default {
  name: "ApiDefinitionInformation",
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
    ApiInformation,
    MsVersionHistory,
    MsChangeHistory
  },
  data() {
    return {
      activeName: "api",
      isShow: true,
      baseMockConfigData: {},
      showFollow: false,
      versionData: [],
      maintainerOptions: [],
      httpForm: {environmentId: "", path: "", tags: []},
      loading: false,
      createCase: "",
      api: {},
    };
  },
  props: {
    apiId: String,
    activeDom: String,
    isShowChangeButton: {
      type: Boolean,
      default: true
    },
    apiInfo: {},
    moduleOptions: {},
    currentApi: {},
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

    this.getMaintainerOptions();
    if (!this.httpForm.environmentId) {
      this.httpForm.environmentId = "";
    }
    this.$get('/api/definition/follow/' + this.apiId, response => {
      this.httpForm.follows = response.data;
      for (let i = 0; i < response.data.length; i++) {
        if (response.data[i] === this.currentUser().id) {
          this.showFollow = true;
          break;
        }
      }
    });

    this.formatApi();
    if (hasLicense()) {
      this.getVersionHistory();
    }
  },
  watch: {
    activeName(){
      if(this.activeName === "mock"){
        this.mockSetting();
      }
    },
    currentProtocol() {
      this.initMockEnvironment();
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
    tabChange() {
      this.beforeChangeTab();
      this.refreshButtonActiveClass(this.activeName);
    },

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
      if (data != null && data.tags !== 'null' && data.tags !== undefined) {
        if (Object.prototype.toString.call(data.tags) === "[object String]") {
          data.tags = JSON.parse(data.tags);
        }
      }
      Object.assign(this.currentApi, data);
      this.currentApi.isCopy = false;
      this.setTabTitle(data);
      this.refresh(data);
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
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    saveAsCase(api) {
      this.createCase = getUUID();
      this.api = api;
      this.$refs.caseList.open();
    },
    refreshButtonActiveClass(tabType) {
      if (tabType === "testCase") {
        this.$store.state.currentApiCase = {case: true};
      } else if (tabType === "test") {
        this.$store.state.currentApiCase = undefined;
      } else if (tabType === "mock") {
        this.$store.state.currentApiCase = undefined;
      } else {
        this.syncApi();
      }
      if(tabType){
        this.activeName = tabType;
      }
    },
    changeApi() {
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
    getVersionHistory() {
      this.$get('/api/definition/versions/' + this.apiInfo.id, response => {
        if (this.apiInfo.isCopy) {
          this.versionData = response.data.filter(v => v.versionId === this.apiInfo.versionId);
        } else {
          this.versionData = response.data;
        }
      });
    },
    compare(row) {
      this.$get('/api/definition/get/' + row.id + "/" + this.apiInfo.refId, response => {
        this.$get('/api/definition/get/' + response.data.id, res => {
          if (res.data) {
            this.newData = res.data;
            this.dealWithTag(res.data);
            this.setRequest(res.data)
            if (!this.setRequest(res.data)) {
              this.oldRequest = createComponent("HTTPSamplerProxy");
              this.dialogVisible = true;
            }
            this.formatApi(res.data)
          }
        });
      });
    },
    checkoutVersion(row) {
      let api = this.versionData.filter(v => v.versionId === row.id)[0];
      if (api.tags && api.tags.length > 0) {
        api.tags = JSON.parse(api.tags);
      }
      this.$emit("checkout", api);
    },
    create(row) {
      // 创建新版本
      this.apiInfo.versionId = row.id;
      this.apiInfo.versionName = row.name;
      this.$set(this.apiInfo, 'newVersionRemark', !!this.apiInfo.remark);
      this.$set(this.apiInfo, 'newVersionDeps', this.$refs.apiOtherInfo.relationshipCount > 0);
      if (this.$refs.apiOtherInfo.relationshipCount > 0 || this.apiInfo.remark) {
        this.createNewVersionVisible = true;
      } else {
        this.saveApi();
      }
    },
    del(row) {
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$get('/api/definition/delete/' + row.id + '/' + this.apiInfo.refId, () => {
              this.$success(this.$t('commons.delete_success'));
              this.getVersionHistory();
            });
          }
        }
      });
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.httpForm.follows.length; i++) {
          if (this.httpForm.follows[i] === this.currentUser().id) {
            this.httpForm.follows.splice(i, 1);
            break;
          }
        }
        if (this.apiInfo.id) {
          this.$post("/api/definition/update/follows/" + this.apiInfo.id, this.httpForm.follows, () => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }
      } else {
        this.showFollow = true;
        if (!this.httpForm.follows) {
          this.httpForm.follows = [];
        }
        this.httpForm.follows.push(this.currentUser().id);
        if (this.apiInfo.id) {
          this.$post("/api/definition/update/follows/" + this.apiInfo.id, this.httpForm.follows, () => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.maintainerOptions = response.data;
      });
    },
    openHis() {
      this.$refs.changeHistory.open(this.httpForm.id, ["接口定义", "接口定義", "Api definition", "API_DEFINITION"]);
    },
    initMockEnvironment() {
      if (this.currentProtocol) {
        let protocol = this.currentProtocol;
        protocol = protocol.substring(0, protocol.indexOf(":"));
        let url = "/api/definition/getMockEnvironment/";
        this.$get(url + this.projectId, response => {
          this.mockEnvironment = response.data;
          let httpConfig = JSON.parse(this.mockEnvironment.config);
          if (httpConfig != null) {
            httpConfig = httpConfig.httpConfig;
            let httpType = httpConfig.defaultCondition;
            let conditions = httpConfig.conditions;
            conditions.forEach(condition => {
              if (condition.type === httpType) {
                this.mockBaseUrl = condition.protocol + "://" + condition.socket;
                this.newMockBaseUrl = this.mockBaseUrl;
              }
            });
          }
        });
      }
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

/*tab样式 start*/
/deep/ .api-info-tab > .el-tabs__header {
  background-color: #F4F6F9;
  border: none;
}

/deep/ .api-info-tab > .el-tabs__header .el-tabs__item.is-top {
  border-color: #F4F6F9;
  color: #8F9198;
}

/deep/ .api-info-tab > .el-tabs__header .el-tabs__item.is-active {
  background-color: #FFFFFF;
  color: var(--count_number);
  border: none;
}

/deep/ .api-info-tab > .el-tabs__header .el-tabs__nav.is-top {
  border-color: #F4F6F9;
}

/*tab样式 end*/
</style>

