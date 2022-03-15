<template>
  <div>
    <ms-environment-select
      :project-id="projectId"
      :is-read-only="false"
      :useEnvironment='useEnvironment'
      @setEnvironment="setEnvironment"
      class="ms-api-button"
      ref="environmentSelect"/>
    <el-tabs v-model="apiDefaultTab" @edit="closeConfirm" class="api-base-view-tab" @tab-click="addTab">
      <el-tab-pane name="trash" :label="$t('commons.trash')" v-show="trashEnable">
        <api-test-base-container>
          <ms-api-module
            slot="aside"
            :show-operator="true"
            @nodeSelectEvent="nodeChange"
            @protocolChange="handleProtocolChange"
            @refreshTable="refresh"
            @exportAPI="exportAPI"
            @debug="debug"
            @saveAsEdit="editApi"
            @setModuleOptions="setModuleOptions"
            @setNodeTree="setNodeTree"
            @enableTrash="enableTrash"
            @schedule="handleTabsEdit($t('api_test.api_import.timing_synchronization'), 'SCHEDULE')"
            :type="'edit'"
            page-source="definition"
            :total='total'
            :current-version="currentVersion"
            ref="nodeTree"/>
          <div slot="mainContainer">
            <ms-tab-button
              v-if="this.trashTabInfo.type === 'list'"
              :active-dom.sync="trashActiveDom"
              :left-tip="$t('api_test.definition.api_title')"
              :right-tip="$t('api_test.definition.case_title')"
              :middle-button-enable="false"
              left-content="API"
              right-content="CASE"
            >
              <template v-slot:version>
                <version-select v-xpack :project-id="projectId" :version-id="trashVersion"
                                @changeVersion="changeVersion"/>
              </template>
              <!-- 列表集合 -->
              <ms-api-list
                v-if="trashActiveDom==='left'"
                @runTest="runTest"
                @refreshTree="refreshTree"
                @getTrashApi="getTrashApi"
                :module-tree="nodeTree"
                :module-options="moduleOptions"
                :current-protocol="currentProtocol"
                :current-version="currentVersion"
                :visible="visible"
                :currentRow="currentRow"
                :select-node-ids="selectNodeIds"
                :trash-enable="true"
                :queryDataType="queryDataType"
                :selectDataRange="selectDataRange"
                :is-read-only="isReadOnly"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @editApi="editApi"
                @handleCase="handleCase"
                @showExecResult="showExecResult"
                @refreshTable="refresh"
                :init-api-table-opretion="initApiTableOpretion"
                @updateInitApiTableOpretion="updateInitApiTableOpretion"
                ref="trashApiList"/>
              <!--测试用例列表-->
              <api-case-simple-list
                v-if="trashActiveDom==='right'"
                :current-protocol="currentProtocol"
                :current-version="currentVersion"
                :visible="visible"
                :currentRow="currentRow"
                :select-node-ids="selectNodeIds"
                :trash-enable="true"
                :queryDataType="queryDataType"
                :is-read-only="isReadOnly"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @handleCase="handleCase"
                @refreshTable="refresh"
                @showExecResult="showExecResult"
                ref="trashCaseList"/>
            </ms-tab-button>
          </div>
        </api-test-base-container>
      </el-tab-pane>

      <el-tab-pane name="default" :label="$t('api_test.definition.api_title')">
        <api-test-base-container>
          <ms-api-module
            slot="aside"
            :show-operator="true"
            @nodeSelectEvent="nodeChange"
            @protocolChange="handleProtocolChange"
            @refreshTable="refresh"
            @exportAPI="exportAPI"
            @debug="debug"
            @saveAsEdit="editApi"
            @setModuleOptions="setModuleOptions"
            @setNodeTree="setNodeTree"
            @enableTrash="enableTrash"
            @schedule="handleTabsEdit($t('api_test.api_import.timing_synchronization'), 'SCHEDULE')"
            :type="'edit'"
            page-source="definition"
            :total='total'
            :current-version="currentVersion"
            ref="nodeTree"/>
          <div slot="mainContainer">
            <ms-tab-button
              :active-dom.sync="activeDom"
              :left-tip="$t('api_test.definition.api_title')"
              :right-tip="$t('api_test.definition.doc_title')"
              :middle-tip="$t('api_test.definition.case_title')"
              left-content="API"
              middle-content="CASE"
              :right-content="$t('api_test.definition.doc_title')"
              :right-button-enable="currentProtocol === 'HTTP' "
            >
              <template v-slot:version>
                <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
              </template>
              <ms-api-list
                v-if="activeDom==='left'"
                @getTrashApi="getTrashApi"
                :module-tree="nodeTree"
                :module-options="moduleOptions"
                :current-protocol="currentProtocol"
                :current-version="currentVersion"
                :visible="visible"
                :currentRow="currentRow"
                :select-node-ids="selectNodeIds"
                :trash-enable="false"
                :queryDataType="queryDataType"
                :selectDataRange="selectDataRange"
                :is-read-only="isReadOnly"
                @runTest="runTest"
                @handleTestCase="handleTestCase"
                @refreshTree="refreshTree"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @editApi="editApi"
                @showApi="showApi"
                @copyApi="copyApi"
                @handleCase="handleCase"
                @showExecResult="showExecResult"
                @refreshTable="refresh"
                :init-api-table-opretion="initApiTableOpretion"
                @updateInitApiTableOpretion="updateInitApiTableOpretion"
                ref="apiDefList"/>
              <!--测试用例列表-->
              <api-case-simple-list
                v-if="activeDom==='middle'"
                :current-protocol="currentProtocol"
                :current-version="currentVersion"
                :visible="visible"
                :currentRow="currentRow"
                :select-node-ids="selectNodeIds"
                :trash-enable="false"
                :queryDataType="queryDataType"
                :is-read-only="isReadOnly"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @handleCase="handleCase"
                @refreshTable="refresh"
                @showExecResult="showExecResult"
                ref="caseList"/>
              <api-documents-page
                class="api-doc-page"
                v-if="activeDom==='right' && currentProtocol==='HTTP'"
                :project-id="projectId"
                :trash-enable="trashEnable"
                :version-id="currentVersion"
                :module-ids="selectNodeIds"
                ref="documentsPage"/>
            </ms-tab-button>
          </div>
        </api-test-base-container>
      </el-tab-pane>


      <el-tab-pane v-for="(item) in apiTabs"
                   :key="item.name"
                   :label="item.title"
                   :closable="item.closable"
                   :name="item.name">
        <template v-slot:version>
          <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
        </template>
        <!-- 列表集合 -->

        <!-- 添加/编辑测试窗口-->
        <div v-if="item.type=== 'ADD' ||item.type=== 'Case' ||item.type === 'TEST'||item.type === 'debug'"
             class="ms-api-div">
          <api-info-container :api-id="item.api.id" :current-api="item.api" :project-id="projectId"
                              :module-options="moduleOptions" :current-protocol="currentProtocol"
                              @saveApi="saveApi" @updateApiStatus="updateApiStatus" @saveApiAndCase="saveApiAndCase" @saveCaseCallback="saveCaseCallback"
                              :operation-type="item.api.operationType"></api-info-container>
        </div>
        <!-- 定时任务 -->
        <div v-if="item.type=== 'SCHEDULE'" class="ms-api-div">
          <api-schedule :param="param" :module-options="nodeTree" ref="apiSchedules"/>
        </div>
      </el-tab-pane>

      <el-tab-pane name="add" v-if="hasPermission('PROJECT_API_DEFINITION:READ+CREATE_API')">
        <template v-slot:label>
          <el-dropdown @command="handleCommand">
            <el-button type="primary" plain icon="el-icon-plus" size="mini"/>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="debug" v-permission="['PROJECT_API_DEFINITION:READ+DEBUG']">
                {{ $t('api_test.definition.request.fast_debug') }}
              </el-dropdown-item>
              <el-dropdown-item command="ADD" v-permission="['PROJECT_API_DEFINITION:READ+CREATE_API']">
                {{ $t('api_test.definition.request.title') }}
              </el-dropdown-item>
              <el-dropdown-item command="CLOSE_ALL">{{ $t('api_test.definition.request.close_all_label') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>

import MsApiModule from "@/business/components/api/definition/components/module/ApiModule";
import ApiTestBaseContainer from "@/business/components/common/layout/ApiTestBaseContainer";
import {getCurrentProjectID, getCurrentUser, getCurrentUserId, getUUID, hasPermission} from "@/common/js/utils";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsApiList from "@/business/components/api/definition/components/list/ApiList";
import MsTabButton from "@/business/components/common/components/MsTabButton";
import VersionSelect from "@/business/components/xpack/version/VersionSelect";
import MsEditCompleteContainer from "@/business/components/api/definition/components/EditCompleteContainer";
import ApiInfoContainer from "@/business/components/api/definition/components/apiinfo/ApiInfoContainer";
import ApiCaseSimpleList from "@/business/components/api/definition/components/list/ApiCaseSimpleList";
import MsEnvironmentSelect from "@/business/components/api/definition/components/case/MsEnvironmentSelect";

import MsDebugHttpPage from "./components/debug/DebugHttpPage";
import MsDebugJdbcPage from "./components/debug/DebugJdbcPage";
import MsDebugTcpPage from "./components/debug/DebugTcpPage";
import MsDebugDubboPage from "./components/debug/DebugDubboPage";
import MsRunTestHttpPage from "./components/runtest/RunTestHTTPPage";
import MsRunTestTcpPage from "./components/runtest/RunTestTCPPage";
import MsRunTestSqlPage from "./components/runtest/RunTestSQLPage";
import MsRunTestDubboPage from "./components/runtest/RunTestDubboPage";
import {createComponent} from "@/business/components/api/definition/components/jmeter/components";


export default {
  name: "ApiDefinitionBaseView",
  components: {
    MsApiModule, ApiTestBaseContainer, MsContainer, MsApiList, MsTabButton, VersionSelect, MsEditCompleteContainer,
    MsDebugHttpPage, MsDebugJdbcPage, MsDebugTcpPage, MsDebugDubboPage,
    MsRunTestHttpPage, MsRunTestTcpPage, MsRunTestSqlPage, MsRunTestDubboPage,
    ApiInfoContainer, ApiCaseSimpleList, MsEnvironmentSelect
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    currentRow: {
      type: Object,
    }
  },
  data() {
    return {
      redirectID: '',
      total: 0,
      renderComponent: true,
      selectDataRange: 'all',
      showCasePage: true,
      apiDefaultTab: 'default',
      currentProtocol: 'HTTP',
      currentModule: null,
      selectNodeIds: [],
      currentApi: {},
      moduleOptions: [],
      trashEnable: false,
      apiTabs: [],
      trashTabInfo: {
        title: this.$t('api_test.definition.api_title'),
        name: 'default',
        type: "list",
        closable: false
      },
      activeDom: "left",
      trashActiveDom: "left",
      syncTabs: [],
      nodeTree: [],
      currentModulePath: "",
      //影响API表格刷新的操作。 为了防止高频率刷新模块列表用。如果是模块更新而造成的表格刷新，则不回调模块刷新方法
      initApiTableOpretion: 'init',
      param: {},
      useEnvironment: String,
      activeTab: "api",
      currentVersion: null,
      trashVersion: null,
      project: null,
    };
  },
  activated() {
  },
  watch: {
    currentProtocol() {
      if (this.activeDom === 'right') {
        this.activeDom = 'left';
      }
      this.handleCommand("CLOSE_ALL");
    },
    selectNodeIds() {
      this.apiDefaultTab = "default";
    },
    redirectID() {
      this.renderComponent = false;
      this.$nextTick(() => {
        // 在 DOM 中添加 my-component 组件
        this.renderComponent = true;
      });
    },
    '$route'(to, from) {  //  路由改变时，把接口定义界面中的 ctrl s 保存快捷键监听移除
      if (to.path.indexOf('/api/definition') === -1) {
        if (this.$refs && this.$refs.apiConfig) {
          this.$refs.apiConfig.forEach(item => {
            item.removeListener();
          });
        }
      }
    },
  },
  created() {
    this.initBaseData();
  },
  mounted() {
  },
  computed: {
    queryDataType: function () {
      let routeParam = this.$route.params.dataType;
      let redirectIDParam = this.$route.params.redirectID;
      this.changeRedirectParam(redirectIDParam);
      return routeParam;
    },
    isReadOnly() {
      return false;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    initBaseData() {
      this.$get('/project/get/' + this.projectId, res => {
        let projectData = res.data;
        if (projectData) {
          this.project = projectData;
        }
      })
    },
    setEnvironment(data) {
      if (data) {
        this.useEnvironment = data.id;
        this.$store.state.useEnvironment = data.id;
        this.addEnv(data.id);
      }
    },
    addEnv(envId) {
      this.$post('/api/definition/env/create', {userId: getCurrentUserId(), envId: envId}, response => {
      });
    },
    getTrashApi() {
      this.$get("/api/module/trashCount/" + this.projectId + "/" + this.currentProtocol, response => {
        this.total = response.data;
      });
    },
    updateApiStatus(operationType, apiName) {
      for (let index in this.apiTabs) {
        let tab = this.apiTabs[index];
        if (tab.name === this.apiDefaultTab) {
          tab.api.operationType = operationType;
          if (operationType === 'edit') {
            tab.title = this.$t('api_test.definition.request.edit_api') + "-" + apiName;
          } else {
            tab.title = this.$t('api_test.definition.request.show_api') + "-" + apiName;
          }
          break;
        }
      }
    },
    getEnv() {
      this.$get("/api/definition/env/get/" + getCurrentUserId() + "/" + getCurrentProjectID(), response => {
        let env = response.data;
        if (env) {
          this.$store.state.useEnvironment = env.envId;
          this.useEnvironment = env.envId;
        } else {
          this.$store.state.useEnvironment = "";
          this.useEnvironment = "";
        }
      });
    },
    hasPermission,
    getPath(id, arr) {
      if (id === null) {
        return null;
      }
      if (arr) {
        arr.forEach(item => {
          if (item.id === id) {
            this.currentModulePath = item.path;
          }
          if (item.children && item.children.length > 0) {
            this.getPath(id, item.children);
          }
        });
      }
    },
    changeRedirectParam(redirectIDParam) {
      this.redirectID = redirectIDParam;
    },
    createApiStruct() {
      let request = {};
      let method = this.currentProtocol;
      let response = {};
      if (this.currentProtocol === 'HTTP') {
        method = "GET";
        request = createComponent("HTTPSamplerProxy");
        if (!request.path) {
          request.path = "";
        }
        response.type = "HTTP";
        response.headers = [];
        response.statusCode = [];
        response.body = {
          "binary": [],
          "json": false,
          "kV": false,
          "kvs": [],
          "oldKV": true,
          "type": "KeyValue",
          "valid": false,
          "xml": false
        };
      } else if (this.currentProtocol === 'TCP') {
        request = createComponent("TCPSampler");
      } else if (this.currentProtocol === 'JDBC' || this.currentProtocol === 'SQL') {
        request = createComponent("JDBCSampler");
      } else if (this.currentProtocol === 'DUBBO') {
        request = createComponent("DubboSampler");
      }
      let newApi = {
        id: getUUID(),
        operationType: 'debug',
        protocol: this.currentProtocol,
        environmentId: "",
        path: "",
        method: method,
        request: request,
        response: response,
      };
      return newApi;
    },
    addTab(tab) {
      if (tab.name === 'add') {
        if (this.project && this.project.apiQuick === 'api') {
          this.handleTabAdd("ADD");
        } else {
          let newApiStruct = this.createApiStruct();
          this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug", newApiStruct);
        }
      } else if (tab.name === 'trash') {
        if (this.$refs.trashApiList) {
          this.$refs.trashApiList.initTable();
        }
        if (this.$refs.trashCaseList) {
          this.$refs.trashCaseList.initTable();
        }
      } else if (tab.name === 'default') {
        this.refresh();
      }
      if (this.$refs.apiConfig) {
        this.$refs.apiConfig.forEach(item => {
          if (item) {
            item.removeListener();
          }
        }); //  删除所有tab的 ctrl + s 监听
        let tabs = this.apiTabs;
        let index = tabs.findIndex(item => item.name === tab.name); //  找到当前选中tab的index
        if (index !== -1 && this.$refs.apiConfig[index - 1]) {
          this.$refs.apiConfig[index - 1].addListener(); //  为选中tab添加 ctrl + s 监听（index-1的原因是要除去第一个固有tab）
        }
      }
    },
    handleCommand(e) {
      switch (e) {
        case "ADD":
          this.handleTabAdd(e);
          break;
        case "TEST":
          this.handleTabsEdit(this.$t("commons.api"), e);
          break;
        case "CLOSE_ALL":
          this.handleTabClose();
          break;
        default:
          this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug");
          break;
      }
    },
    handleApiAndCaseTabAdd(e, api) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.currentModulePath = "";
      if (this.nodeTree && this.nodeTree.length > 0) {
        api.moduleId = this.nodeTree[0].id;
        this.getPath(this.nodeTree[0].id, this.moduleOptions);
        api.modulePath = this.currentModulePath;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        api.moduleId = this.selectNodeIds[0];
        this.getPath(this.selectNodeIds[0], this.moduleOptions);
        api.modulePath = this.currentModulePath;
      }
      this.handleTabsEdit(this.$t('test_track.case.create_case'), e, api);
    },
    handleTabAdd(e) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }

      let request = {};
      let method = this.currentProtocol;
      let response = {};
      if (this.currentProtocol === 'HTTP') {
        method = "GET";
        request = createComponent("HTTPSamplerProxy");
        if (!request.path) {
          request.path = "";
        }
        response.type = "HTTP";
        response.headers = [];
        response.statusCode = [];
        response.body = {
          "binary": [],
          "json": false,
          "kV": false,
          "kvs": [],
          "oldKV": true,
          "type": "KeyValue",
          "valid": false,
          "xml": false
        };
      } else if (this.currentProtocol === 'TCP') {
        request = createComponent("TCPSampler");
      } else if (this.currentProtocol === 'JDBC' || this.currentProtocol === 'SQL') {
        request = createComponent("JDBCSampler");
      } else if (this.currentProtocol === 'DUBBO') {
        request = createComponent("DubboSampler");
      }
      let api = {
        status: "Underway",
        method: method,
        userId: getCurrentUser().id,
        url: "",
        id: getUUID(),
        protocol: this.currentProtocol,
        environmentId: "",
        remark: "",
        operationType: "create",
        moduleId: 'default-module',
        modulePath: "/" + this.$t("commons.module_title"),
        request: request,
        response: response
      };
      this.currentModulePath = "";
      if (this.nodeTree && this.nodeTree.length > 0) {
        api.moduleId = this.nodeTree[0].id;
        this.getPath(this.nodeTree[0].id, this.moduleOptions);
        api.modulePath = this.currentModulePath;
      }

      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        api.moduleId = this.selectNodeIds[0];
        this.getPath(this.selectNodeIds[0], this.moduleOptions);
        api.modulePath = this.currentModulePath;
      }
      this.handleTabsEdit(this.$t('api_test.definition.request.title'), e, api);
    },
    handleTabClose() {
      let message = "";
      let tab = this.apiTabs;
      delete tab[0];
      tab.forEach(t => {
        if (t.type === 'ADD' && t.api && this.$store.state.apiMap.has(t.api.id) && (this.$store.state.apiMap.get(t.api.id).get("responseChange") === true || this.$store.state.apiMap.get(t.api.id).get("requestChange") === true ||
          this.$store.state.apiMap.get(t.api.id).get("fromChange") === true)) {
          message += t.api.name + "，";
        }
      });
      if (message !== "") {
        this.$alert(this.$t('commons.api') + " [ " + message.substr(0, message.length - 1) + " ] " + this.$t('commons.confirm_info'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          callback: (action) => {
            if (action === 'confirm') {
              this.$store.state.apiMap.clear();
              this.apiTabs = [];
            }
          }
        });
      } else {
        this.apiTabs = [];
      }
      this.apiDefaultTab = "default";
    },
    closeConfirm(targetName) {
      let tab = this.apiTabs;
      tab.forEach(t => {
        if (t.name === targetName) {
          if (t.api && this.$store.state.apiMap.size > 0 && this.$store.state.apiMap.has(t.api.id)) {
            if (this.$store.state.apiMap.get(t.api.id).get("responseChange") === true || this.$store.state.apiMap.get(t.api.id).get("requestChange") === true ||
              this.$store.state.apiMap.get(t.api.id).get("fromChange") === true) {
              this.$alert(this.$t('commons.api') + " [ " + t.api.name + " ] " + this.$t('commons.confirm_info'), '', {
                confirmButtonText: this.$t('commons.confirm'),
                cancelButtonText: this.$t('commons.cancel'),
                callback: (action) => {
                  if (action === 'confirm') {
                    this.$store.state.apiMap.delete(t.api.id);
                    this.handleTabRemove(targetName);
                  }
                }
              });
            }
          } else {
            this.handleTabRemove(targetName);
          }
        }
      });
    },
    handleTabRemove(targetName) {
      let tabs = this.apiTabs;
      let activeName = this.apiDefaultTab;
      if (activeName === targetName) {
        tabs.forEach((tab, index) => {
          if (tab.name === targetName) {
            let nextTab = tabs[index + 1] || tabs[index - 1];
            if (nextTab) {
              activeName = nextTab.name;
            } else {
              activeName = "default";
            }
          }
        });
      }
      this.apiDefaultTab = activeName;
      this.apiTabs = tabs.filter(tab => tab.name !== targetName);
      this.refresh();
    },
    //创建左侧树的根目录模块
    createRootModel() {
      this.$refs.nodeTree.createRootModel();
    },
    handleMockTabsConfig(targetName, action, param) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (targetName === undefined || targetName === null) {
        targetName = this.$t('api_test.definition.request.title');
      }
      let newTabName = getUUID();
      this.apiTabs.push({
        title: targetName,
        name: newTabName,
        closable: true,
        type: action,
        mock: param,
      });
      this.apiDefaultTab = newTabName;
    },
    handleTabsEdit(targetName, action, api) {
      if (action === "debug" && !api) {
        api = this.createApiStruct();
      }
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (targetName === undefined || targetName === null) {
        targetName = this.$t('api_test.definition.request.title');
      }
      let newTabName = getUUID();
      if(action === "Case"){
        newTabName = api.id;
      }
      this.apiTabs.push({
        title: targetName,
        name: newTabName,
        closable: true,
        type: action,
        api: api,
      });
      if (action === "ADD") {
        this.activeTab = "api";
      }
      this.apiDefaultTab = newTabName;
    },
    debug(id) {
      this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug", id);
    },
    init() {
      let routeTestCase = this.$route.params.apiDefinition;
      if (routeTestCase) {
        this.editApi(routeTestCase);
      }
      let dataRange = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      if (dataRange) {
        let selectParamArr = dataRange.split("edit:");
        if (selectParamArr.length === 2) {
          let scenarioId = selectParamArr[1];
          if (dataType === 'api') {
            this.$get('/api/definition/get/' + scenarioId, (response) => {
              this.editApi(response.data);
            });
          }
        }
      }
    },
    showApi(row) {
      this.editApi(row, true);
    },
    editApi(row, isShowApi) {
      const index = this.apiTabs.find(p => p.api && p.api.id === row.id);
      if (!index) {
        let name = "";
        if (row.isCopy) {
          name = "copy" + "_" + row.name;
          row.name = "copy" + "_" + row.name;
        } else {
          if (row.name) {
            if (isShowApi) {
              name = this.$t('api_test.definition.request.show_api') + "-" + row.name;
            } else {
              name = this.$t('api_test.definition.request.edit_api') + "-" + row.name;
            }
          } else {
            name = this.$t('api_test.definition.request.title');
          }
        }
        this.activeTab = "api";
        if (row != null && row.tags != 'null' && row.tags != '' && row.tags != undefined) {
          if (Object.prototype.toString.call(row.tags).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object'
            && Object.prototype.toString.call(row.tags).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'array') {
            row.tags = JSON.parse(row.tags);
          }
        }
        if (isShowApi) {
          row.operationType = "show";
        } else {
          row.operationType = "edit";
        }
        this.handleTabsEdit(name, "ADD", row);
      } else {
        this.apiDefaultTab = index.name;
      }
    },
    copyApi(row) {
      let name = "";
      if (row.isCopy) {
        name = "copy" + "_" + row.name;
        row.name = "copy" + "_" + row.name;
      }
      this.activeTab = "api";
      if (row != null && row.tags != 'null' && row.tags != '' && row.tags != undefined) {
        if (Object.prototype.toString.call(row.tags).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object'
          && Object.prototype.toString.call(row.tags).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'array') {
          row.tags = JSON.parse(row.tags);
        }
      }
      this.handleTabsEdit(name, "ADD", row);
    },
    handleCase(api) {
      this.currentApi = api;
      this.showCasePage = false;
    },
    apiCaseClose() {
      this.showCasePage = true;
    },
    exportAPI(type, nodeTree) {
      if (this.activeDom !== 'left') {
        this.$warning('用例列表暂不支持导出，请切换成接口列表');
        return;
      }
      this.$refs.apiDefList[0].exportApi(type, nodeTree);
    },
    refreshModule() {
      this.$refs.nodeTree.list();
    },
    refresh(data) {
      if (this.$refs.caseList && this.$refs.caseList[0]) {
        this.$refs.caseList[0].initTable();
      }
      if (this.$refs.trashApiList) {
        this.$refs.trashApiList.initTable();
      }
      if (this.$refs.trashCaseList) {
        this.$refs.trashCaseList.initTable();
      }
      if (this.$refs.nodeTree) {
        this.$refs.nodeTree.list();
      }
      if (this.$refs.apiDefList) {
        if (this.$refs.apiDefList[0]) {
          this.$refs.apiDefList[0].initTable();
        } else {
          this.$refs.apiDefList.initTable();
        }
      }
      if (this.$refs.documentsPage && this.$refs.documentsPage[0]) {
        this.$refs.documentsPage[0].initApiDocSimpleList();
      }
      //this.$refs.nodeTree.list();
    },
    refreshTree() {
      this.$refs.nodeTree.list();
    },
    setTabTitle(data) {
      for (let index in this.apiTabs) {
        let tab = this.apiTabs[index];
        if (tab.name === this.apiDefaultTab) {
          tab.api.operationType = "show";
          tab.api.request = JSON.stringify(data.request);
          tab.api.response = JSON.stringify(data.response);
          tab.api.method = data.method;
          tab.title = this.$t('api_test.definition.request.show_api') + "-" + data.name;
          break;
        }
      }
    },
    runTest(data) {
      this.activeTab = "test";
      this.handleTabsEdit(this.$t("commons.api"), "TEST", data);
      this.setTabTitle(data);
    },
    handleTestCase(row) {
      this.activeTab = "testCase";
      let name = "";
      if (row.name) {
        name = this.$t('api_test.definition.request.edit_api') + "-" + row.name;
      } else {
        name = this.$t('api_test.definition.request.title');
      }
      if (row != null && row.tags != 'null' && row.tags != '' && row.tags != undefined) {
        if (Object.prototype.toString.call(row.tags).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object'
          && Object.prototype.toString.call(row.tags).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'array') {
          row.tags = JSON.parse(row.tags);
        }
      }
      this.handleTabsEdit(name, "TEST", row);
    },
    mockConfig(data) {
      let targetName = this.$t("commons.mock") + "-" + data.apiName;
      this.handleMockTabsConfig(targetName, "MOCK", data);
    },
    saveApi(data) {
      this.setTabTitle(data);
    },

    showExecResult(row) {
      this.debug(row);
    },
    nodeChange(node, nodeIds, pNodes) {
      this.initApiTableOpretion = "selectNodeIds";
      this.selectNodeIds = nodeIds;
    },
    handleProtocolChange(protocol) {
      this.initApiTableOpretion = "currentProtocol";
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    setNodeTree(data) {
      this.nodeTree = data;
    },
    changeSelectDataRangeAll(tableType) {
      this.$route.params.dataSelectRange = 'all';
    },
    enableTrash(data) {
      this.initApiTableOpretion = "trashEnable";
      this.trashEnable = data;
      this.trashVersion = this.currentVersion
      if (data) {
        this.apiDefaultTab = "trash";
      } else {
        this.apiDefaultTab = "default";
      }
    },
    updateInitApiTableOpretion(param) {
      this.initApiTableOpretion = param;
    },
    changeVersion(currentVersion) {
      this.trashVersion = null;
      this.currentVersion = currentVersion || null;
    },
    saveApiAndCase(apiStruct) {
      apiStruct.operationType = "addApiAndCase";
      apiStruct.id = getUUID();
      this.handleApiAndCaseTabAdd("Case", apiStruct);
    },
    saveCaseCallback(apiStruct){
      this.closeConfirm(apiStruct.id);
    }
  }
};
</script>

<style scoped>

.ms-api-div {
  overflow-y: auto;
  height: calc(100vh - 125px)
}

/deep/ .el-main {
  overflow: hidden;
}

/deep/ .api-base-view-tab > .el-tabs__header {
  width: calc(100% - 230px);
  margin: 0px 5px 0px 5px
}

/deep/ .api-base-view-tab > .el-tabs__nav-scroll {
  width: calc(100% - 10px);
}

/deep/ .el-card {
  border-top: none;
}

/deep/ .api-component {
  margin-top: 10px;
}

.ms-api-button {
  position: absolute;
  top: 86px;
  right: 10px;
  padding: 0;
  background: 0 0;
  border: none;
  outline: 0;
  cursor: pointer;
  margin-right: 10px;
  font-size: 16px;
  z-index: 1;
}

/deep/ .el-table__empty-block {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  padding-right: 100%;
}

.version-select {
  padding-left: 10px;
}
</style>
