<template>
  <div>
    <ms-container v-if="renderComponent">
      <ms-aside-container>
        <ms-api-module
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
      </ms-aside-container>

      <ms-main-container>
        <ms-environment-select
          :project-id="projectId"
          :is-read-only="false"
          :useEnvironment='useEnvironment'
          @setEnvironment="setEnvironment"
          class="ms-api-button"
          ref="environmentSelect"/>
        <!-- 主框架列表 -->
        <el-tabs v-model="apiDefaultTab" @edit="closeConfirm" @tab-click="addTab">
          <el-tab-pane
            name="trash"
            :label="$t('commons.trash')" v-if="trashEnable">
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
                <version-select v-xpack :project-id="projectId" :version-id="trashVersion" @changeVersion="changeVersion"/>
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
          </el-tab-pane>

          <el-tab-pane v-for="(item) in apiTabs"
                       :key="item.name"
                       :label="item.title"
                       :closable="item.closable"
                       :name="item.name">
            <ms-tab-button
              v-if="item.type === 'list'"
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
              <!-- 列表集合 -->
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
            <!-- 添加/编辑测试窗口-->
            <div v-if="item.type=== 'ADD' ||item.type === 'TEST'" class="ms-api-div">
              <ms-edit-complete-container
                :syncTabs="syncTabs"
                @runTest="runTest"
                @saveApi="saveApi"
                @createRootModel="createRootModel"
                @editApi="editApi"
                @refresh="refresh"
                :current-api="item.api"
                :project-id="projectId"
                :currentProtocol="currentProtocol"
                :moduleOptions="moduleOptions"
                :activeDom="activeTab"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @handleCase="handleCase"
                @showExecResult="showExecResult"

                ref="apiConfig"
              />
            </div>
            <!-- 快捷调试 -->
            <div v-else-if="item.type=== 'debug'" class="ms-api-div">
              <ms-debug-http-page
                :currentProtocol="currentProtocol"
                :testCase="item.api"
                @saveAs="editApi"
                @refreshModule="refreshModule"
                v-if="currentProtocol==='HTTP'"/>
              <ms-debug-jdbc-page
                :currentProtocol="currentProtocol"
                :testCase="item.api"
                @saveAs="editApi"
                @refreshModule="refreshModule"
                v-if="currentProtocol==='SQL'"/>
              <ms-debug-tcp-page
                :currentProtocol="currentProtocol"
                :testCase="item.api"
                :scenario="false"
                @saveAs="editApi"
                @refreshModule="refreshModule"
                v-if="currentProtocol==='TCP'"/>
              <ms-debug-dubbo-page
                :currentProtocol="currentProtocol"
                :testCase="item.api"
                @saveAs="editApi"
                @refreshModule="refreshModule"
                v-if="currentProtocol==='DUBBO'"/>
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


      </ms-main-container>
    </ms-container>

  </div>
</template>
<script>
import MsApiList from './components/list/ApiList';
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import MsAsideContainer from "../../common/components/MsAsideContainer";
import MsDebugHttpPage from "./components/debug/DebugHttpPage";
import MsDebugJdbcPage from "./components/debug/DebugJdbcPage";
import MsDebugTcpPage from "./components/debug/DebugTcpPage";
import MsDebugDubboPage from "./components/debug/DebugDubboPage";

import MsRunTestHttpPage from "./components/runtest/RunTestHTTPPage";
import MsRunTestTcpPage from "./components/runtest/RunTestTCPPage";
import MsRunTestSqlPage from "./components/runtest/RunTestSQLPage";
import MsRunTestDubboPage from "./components/runtest/RunTestDubboPage";
import {getCurrentProjectID, getCurrentUser, getCurrentUserId, getUUID, hasPermission} from "@/common/js/utils";
import MsApiModule from "./components/module/ApiModule";
import ApiCaseSimpleList from "./components/list/ApiCaseSimpleList";

import ApiDocumentsPage from "@/business/components/api/definition/components/list/ApiDocumentsPage";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTabButton from "@/business/components/common/components/MsTabButton";

import MockConfig from "@/business/components/api/definition/components/mock/MockConfig";
import ApiSchedule from "@/business/components/api/definition/components/import/ApiSchedule";
import MsEditCompleteContainer from "./components/EditCompleteContainer";
import MsEnvironmentSelect from "./components/case/MsEnvironmentSelect";
import {PROJECT_ID, WORKSPACE_ID} from "@/common/js/constants";


const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};


export default {
  name: "ApiDefinition",
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
  components: {
    'VersionSelect': VersionSelect.default,
    ApiSchedule,
    MsTabButton,
    MsTableButton,
    ApiCaseSimpleList,
    MsApiModule,
    MsApiList,
    MsMainContainer,
    MsContainer,
    MsAsideContainer,
    MsDebugHttpPage,
    MsRunTestHttpPage,
    MsDebugJdbcPage,
    MsDebugTcpPage,
    MsDebugDubboPage,
    MsRunTestTcpPage,
    MsRunTestSqlPage,
    MsRunTestDubboPage,
    ApiDocumentsPage,
    MockConfig,
    MsEditCompleteContainer,
    MsEnvironmentSelect
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
      apiTabs: [{
        title: this.$t('api_test.definition.api_title'),
        name: 'default',
        type: "list",
        closable: false
      }],
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
    };
  },
  activated() {
    this.selectNodeIds = [];
    let dataRange = this.$route.params.dataSelectRange;
    if (dataRange && dataRange.length > 0) {
      this.activeDom = 'middle';
    }
    let dataType = this.$route.params.dataType;
    if (dataType) {
      if (dataType === "api") {
        this.activeDom = 'left';
      } else {
        this.activeDom = 'middle';
      }
    }

    if (this.$route.params.dataSelectRange) {
      let item = JSON.parse(JSON.stringify(this.$route.params.dataSelectRange)).param;
      if (item !== undefined) {
        let type = item.taskGroup.toString();
        if (type === "SWAGGER_IMPORT") {
          this.handleTabsEdit(this.$t('api_test.api_import.timing_synchronization'), 'SCHEDULE');
          this.param = item;
        }
      }

    }
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
    let workspaceId = this.$route.params.workspaceId;
    if (workspaceId) {
      sessionStorage.setItem(WORKSPACE_ID, workspaceId);
    }else {
      if(this.$route.query.workspaceId){
        workspaceId = this.$route.query.workspaceId;
        sessionStorage.setItem(WORKSPACE_ID, workspaceId);
      }
    }
    let projectId = this.$route.params.projectId;
    if (projectId) {
      sessionStorage.setItem(PROJECT_ID, projectId);
    }else {
      if (this.$route.query.projectId) {
        projectId = this.$route.query.projectId;
        sessionStorage.setItem(PROJECT_ID, this.$route.query.projectId);
      }
    }
    this.getEnv();
    // 通知过来的数据跳转到编辑
    if (this.$route.query.caseId) {
      this.activeDom = 'middle';
    }
  },
  mounted() {
    this.init();
  },
  methods: {
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
    addTab(tab) {
      if (tab.name === 'add') {
        this.result = this.$get('/project_application/get/config/' + this.projectId +"/API_QUICK_MENU", res => {
          let projectData = res.data;
          if (projectData && projectData.apiQuickMenu === 'api') {
            this.handleTabAdd("ADD");
          } else {
            this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug");
          }
        })

      } else if (tab.name === 'trash') {
        if (this.$refs.trashApiList) {
          this.$refs.trashApiList.initTable();
        }
        if (this.$refs.trashCaseList) {
          this.$refs.trashCaseList.initTable();
        }
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
    handleTabAdd(e) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      let api = {
        status: "Underway",
        method: "GET",
        userId: getCurrentUser().id,
        url: "",
        protocol: this.currentProtocol,
        environmentId: "",
        remark: "",
        moduleId: 'default-module',
        modulePath: "/" + this.$t("commons.module_title")
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
      let tabs = this.apiTabs[0];
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
              this.apiDefaultTab = tabs.name;
              this.apiTabs.push(tabs);
            }
          }
        });
      } else {
        this.apiTabs = [];
        this.apiDefaultTab = tabs.name;
        this.apiTabs.push(tabs);
      }
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
    editApi(row) {
      const index = this.apiTabs.find(p => p.api && p.api.id === row.id);
      if (!index) {
        let name = "";
        if (row.isCopy) {
          name = "copy" + "_" + row.name;
          row.name = "copy" + "_" + row.name;
        } else {
          if (row.name) {
            name = this.$t('api_test.definition.request.edit_api') + "-" + row.name;
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
      if (this.$refs.apiDefList && this.$refs.apiDefList[0]) {
        this.$refs.apiDefList[0].initTable();
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
          tab.title = this.$t('api_test.definition.request.edit_api') + "-" + data.name;
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
      this.refresh(data);
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

/deep/ .el-tabs__header {
  margin: 0 0 0px;
}

/deep/ .el-tabs__nav-scroll {
  width: calc(100% - 200px);
}

/deep/ .el-card {
  /*border: 1px solid #EBEEF5;*/
  /*border-style: none;*/
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
