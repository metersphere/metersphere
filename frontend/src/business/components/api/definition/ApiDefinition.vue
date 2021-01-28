<template>
  <ms-container v-if="renderComponent">
    <ms-aside-container>
      <ms-api-module
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @exportAPI="exportAPI"
        @debug="debug"
        @saveAsEdit="editApi"
        @setModuleOptions="setModuleOptions"
        @enableTrash="enableTrash"
        :type="'edit'"
        ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
      <!-- 主框架列表 -->
      <el-tabs v-model="apiDefaultTab" @edit="handleTabRemove" @tab-click="addTab">
        <el-tab-pane v-for="(item) in apiTabs"
                     :key="item.name"
                     :label="item.title"
                     :closable="item.closable"
                     :name="item.name">
          <!-- 列表集合 -->
          <ms-api-list
            v-if="item.type === 'list' && isApiListEnable"
            :current-protocol="currentProtocol"
            :visible="visible"
            :currentRow="currentRow"
            :select-node-ids="selectNodeIds"
            :trash-enable="trashEnable"
            :is-api-list-enable="isApiListEnable"
            :queryDataType="queryDataType"
            :selectDataRange="selectDataRange"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            @editApi="editApi"
            @handleCase="handleCase"
            @showExecResult="showExecResult"
            @isApiListEnableChange="isApiListEnableChange"
            ref="apiList"/>
          <!--测试用例列表-->
          <api-case-simple-list
            v-if="item.type === 'list' && !isApiListEnable"
            :current-protocol="currentProtocol"
            :visible="visible"
            :currentRow="currentRow"
            :select-node-ids="selectNodeIds"
            :trash-enable="trashEnable"
            :is-api-list-enable="isApiListEnable"
            :queryDataType="queryDataType"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            @isApiListEnableChange="isApiListEnableChange"
            @handleCase="handleCase"
            @showExecResult="showExecResult"
            ref="apiList"/>

          <!-- 添加/编辑测试窗口-->
          <div v-else-if="item.type=== 'ADD'" class="ms-api-div">
            <ms-api-config :syncTabs="syncTabs" @runTest="runTest" @saveApi="saveApi" @createRootModel="createRootModel" ref="apiConfig"
                           :current-api="item.api"
                           :project-id="projectId"
                           :currentProtocol="currentProtocol"
                           :moduleOptions="moduleOptions"/>
          </div>
          <!-- 快捷调试 -->
          <div v-else-if="item.type=== 'debug'" class="ms-api-div">
            <ms-debug-http-page :currentProtocol="currentProtocol" :testCase="item.api" @saveAs="editApi"
                                v-if="currentProtocol==='HTTP'"/>
            <ms-debug-jdbc-page :currentProtocol="currentProtocol" :testCase="item.api" @saveAs="editApi"
                                v-if="currentProtocol==='SQL'"/>
            <ms-debug-tcp-page :currentProtocol="currentProtocol" :testCase="item.api" @saveAs="editApi"
                               v-if="currentProtocol==='TCP'"/>
            <ms-debug-dubbo-page :currentProtocol="currentProtocol" :testCase="item.api" @saveAs="editApi"
                                 v-if="currentProtocol==='DUBBO'"/>
          </div>

          <!-- 测试-->
          <div v-else-if="item.type=== 'TEST'" class="ms-api-div">
            <ms-run-test-http-page :syncTabs="syncTabs" :currentProtocol="currentProtocol" :api-data="item.api" :project-id="projectId"
                                   @saveAsApi="editApi" @refresh="refresh" v-if="currentProtocol==='HTTP'"/>
            <ms-run-test-tcp-page :syncTabs="syncTabs" :currentProtocol="currentProtocol" :api-data="item.api" :project-id="projectId"
                                  @saveAsApi="editApi" @refresh="refresh" v-if="currentProtocol==='TCP'"/>
            <ms-run-test-sql-page :syncTabs="syncTabs" :currentProtocol="currentProtocol" :api-data="item.api" :project-id="projectId"
                                  @saveAsApi="editApi" @refresh="refresh" v-if="currentProtocol==='SQL'"/>
            <ms-run-test-dubbo-page :syncTabs="syncTabs" :currentProtocol="currentProtocol" :api-data="item.api" :project-id="projectId"
                                    @saveAsApi="editApi" @refresh="refresh" v-if="currentProtocol==='DUBBO'"/>
          </div>
        </el-tab-pane>

        <el-tab-pane name="add">
          <template v-slot:label>
            <el-dropdown @command="handleCommand" v-tester>
              <el-button type="primary" plain icon="el-icon-plus" size="mini" v-tester/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="debug">{{ $t('api_test.definition.request.fast_debug') }}</el-dropdown-item>
                <el-dropdown-item command="ADD">{{ $t('api_test.definition.request.title') }}</el-dropdown-item>
                <el-dropdown-item command="CLOSE_ALL">{{ $t('api_test.definition.request.close_all_label') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-tab-pane>
      </el-tabs>
    </ms-main-container>
  </ms-container>
</template>
<script>
  import MsApiList from './components/list/ApiList';
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";
  import MsAsideContainer from "../../common/components/MsAsideContainer";
  import MsApiConfig from "./components/ApiConfig";
  import MsDebugHttpPage from "./components/debug/DebugHttpPage";
  import MsDebugJdbcPage from "./components/debug/DebugJdbcPage";
  import MsDebugTcpPage from "./components/debug/DebugTcpPage";
  import MsDebugDubboPage from "./components/debug/DebugDubboPage";

  import MsRunTestHttpPage from "./components/runtest/RunTestHTTPPage";
  import MsRunTestTcpPage from "./components/runtest/RunTestTCPPage";
  import MsRunTestSqlPage from "./components/runtest/RunTestSQLPage";
  import MsRunTestDubboPage from "./components/runtest/RunTestDubboPage";
  import {downloadFile, getCurrentUser, getUUID, getCurrentProjectID} from "@/common/js/utils";
  import MsApiModule from "./components/module/ApiModule";
  import ApiCaseSimpleList from "./components/list/ApiCaseSimpleList";
  import {PROJECT_NAME} from "../../../../common/js/constants";

  export default {
    name: "ApiDefinition",
    computed: {
      queryDataType: function () {
        let routeParam = this.$route.params.dataType;
        let redirectIDParam = this.$route.params.redirectID;
        this.changeRedirectParam(redirectIDParam);
        if (routeParam === 'apiTestCase') {
          this.isApiListEnableChange(false);
        } else {
          this.isApiListEnableChange(true);
        }
        return routeParam;
      },
    },
    components: {
      ApiCaseSimpleList,
      MsApiModule,
      MsApiList,
      MsMainContainer,
      MsContainer,
      MsAsideContainer,
      MsApiConfig,
      MsDebugHttpPage,
      MsRunTestHttpPage,
      MsDebugJdbcPage,
      MsDebugTcpPage,
      MsDebugDubboPage,
      MsRunTestTcpPage,
      MsRunTestSqlPage,
      MsRunTestDubboPage
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
        renderComponent: true,
        selectDataRange: 'all',
        showCasePage: true,
        apiDefaultTab: 'default',
        currentProtocol: null,
        currentModule: null,
        selectNodeIds: [],
        currentApi: {},
        moduleOptions: {},
        trashEnable: false,
        apiTabs: [{
          title: this.$t('api_test.definition.api_title'),
          name: 'default',
          type: "list",
          closable: false
        }],
        isApiListEnable: true,
        syncTabs: [],
        projectId: ""
      }
    },
    mounted() {
      this.projectId = getCurrentProjectID();
    },
    watch: {
      currentProtocol() {
        this.handleCommand("CLOSE_ALL");
      },
      redirectID() {
        this.renderComponent = false;
        this.$nextTick(() => {
          // 在 DOM 中添加 my-component 组件
          this.renderComponent = true;
        });
      },
      '$route'(to, from) {  //  路由改变时，把接口定义界面中的 ctrl s 保存快捷键监听移除
        if (to.path.indexOf('/api/definition') == -1) {
          if (this.$refs && this.$refs.apiConfig) {
            this.$refs.apiConfig.forEach(item => {
              item.removeListener();
            });
          }
        }
      }
    },
    methods: {
      changeRedirectParam(redirectIDParam) {
        this.redirectID = redirectIDParam;
      },
      isApiListEnableChange(data) {
        this.isApiListEnable = data;
      },
      addTab(tab) {
        if (tab.name === 'add') {
          this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug");
        }
        if (this.$refs.apiConfig) {
          this.$refs.apiConfig.forEach(item => {
            item.removeListener();
          }); //  删除所有tab的 ctrl + s 监听
          let tabs = this.apiTabs;
          let index = tabs.findIndex(item => item.name === tab.name); //  找到当前选中tab的index
          if (index != -1 && this.$refs.apiConfig[index - 1]) {
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
          status: "Underway", method: "GET", userId: getCurrentUser().id,
          url: "", protocol: this.currentProtocol, environmentId: ""
        };
        this.handleTabsEdit(this.$t('api_test.definition.request.title'), e, api);
      },
      handleTabClose() {
        let tabs = this.apiTabs[0];
        this.apiTabs = [];
        this.apiDefaultTab = tabs.name;
        this.apiTabs.push(tabs);
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
        this.apiDefaultTab = newTabName;
      },
      debug(id) {
        this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug", id);
      },
      editApi(row) {
        let name = this.$t('api_test.definition.request.edit_api');
        if (row.name) {
          name = this.$t('api_test.definition.request.edit_api') + "-" + row.name;
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
      exportAPI() {
        if (!this.isApiListEnable) {
          this.$warning('用例列表暂不支持导出，请切换成接口列表');
          return;
        }
        let obj = {projectName: this.projectId, protocol: this.currentProtocol}
        if (this.$refs.apiList[0].selectRows && this.$refs.apiList[0].selectRows.size > 0) {
          let arr = Array.from(this.$refs.apiList[0].selectRows);
          obj.data = arr;
          this.buildApiPath(obj.data);
          downloadFile("Metersphere_Api_" + localStorage.getItem(PROJECT_NAME) + ".json", JSON.stringify(obj));
        } else {
          let condition = {};
          let url = "/api/definition/list/all";
          condition.filters = new Map(
            [
              ["status", ["Prepare", "Underway", "Completed"]],
            ]
          );
          condition.projectId = this.projectId;
          this.$post(url, condition, response => {
            obj.data = response.data;
            this.buildApiPath(obj.data);
            downloadFile("Metersphere_Api_" + localStorage.getItem(PROJECT_NAME) + ".json", JSON.stringify(obj));
          });
        }
      },
      buildApiPath(apis) {
        apis.forEach((api) => {
          this.moduleOptions.forEach(item => {
            if (api.moduleId === item.id) {
              api.modulePath = item.path;
            }
          });
        });
      },
      refresh(data) {
        this.$refs.apiList[0].initTable(data);
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
        this.setTabTitle(data);
        this.handleTabsEdit(this.$t("commons.api"), "TEST", data);
      },
      saveApi(data) {
        this.setTabTitle(data);
        this.refresh(data);
      },

      showExecResult(row) {
        this.debug(row);
      },
      nodeChange(node, nodeIds, pNodes) {
        this.selectNodeIds = nodeIds;
      },
      handleProtocolChange(protocol) {
        this.currentProtocol = protocol;
      },
      setModuleOptions(data) {
        this.moduleOptions = data;
      },
      changeSelectDataRangeAll(tableType) {
        this.$route.params.dataSelectRange = 'all';
      },
      enableTrash(data) {
        this.trashEnable = data;
      }
    }
  }
</script>

<style scoped>

  .ms-api-div {
    overflow-y: auto;
    height: calc(100vh - 155px)
  }

  /deep/ .el-main {
    overflow: hidden;
  }

  /deep/ .el-tabs__header {
    margin: 0 0 0px;
    /*width: calc(100% - 90px);*/
  }

  /deep/ .el-card {
    /*border: 1px solid #EBEEF5;*/
    /*border-style: none;*/
    border-top: none;
  }

  /deep/ .api-component {
    margin-top: 10px;
  }

</style>
