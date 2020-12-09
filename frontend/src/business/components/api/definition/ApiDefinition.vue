<template>
  <ms-container>
    <ms-aside-container>
      <ms-node-tree @selectModule="selectModule" @getApiModuleTree="initTree" @changeProject="changeProject" @changeProtocol="changeProtocol"
                    @refresh="refresh" @saveAsEdit="editApi" @debug="debug" @exportAPI="exportAPI"/>
    </ms-aside-container>

    <ms-main-container>
      <el-dropdown size="small" split-button type="primary" class="ms-api-buttion" @click="handleCommand('add')"
                   @command="handleCommand">
        {{$t('commons.add')}}
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="debug">{{$t('api_test.definition.request.fast_debug')}}</el-dropdown-item>
          <el-dropdown-item command="add">{{$t('api_test.definition.request.title')}}</el-dropdown-item>
          <el-dropdown-item command="closeAll">{{$t('api_test.definition.request.close_all_label')}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <!-- 主框架列表 -->
      <el-tabs v-model="apiDefaultTab" @edit="handleTabsEdit">
        <el-tab-pane
          :key="item.name"
          v-for="(item) in apiTabs"
          :label="item.title"
          :closable="item.closable"
          :name="item.name">
          <!-- 列表集合 -->
          <ms-api-list
            v-if="item.type === 'list'"
            :current-project="currentProject"
            :current-protocol="currentProtocol"
            :current-module="currentModule"
            @editApi="editApi"
            @handleCase="handleCase"
            :visible="visible"
            :currentRow="currentRow"
            ref="apiList"/>

          <!-- 添加测试窗口-->
          <div v-else-if="item.type=== 'add'" class="ms-api-div">
            <ms-api-config @runTest="runTest" @saveApi="saveApi" :current-api="currentApi"
                           :currentProject="currentProject"
                           :currentProtocol="currentProtocol"
                           :moduleOptions="moduleOptions" ref="apiConfig"/>
          </div>

          <!-- 快捷调试 -->
          <div v-else-if="item.type=== 'debug'" class="ms-api-div">
            <ms-debug-http-page :currentProtocol="currentProtocol" @saveAs="editApi" v-if="currentProtocol==='HTTP'"/>
            <ms-debug-jdbc-page :currentProtocol="currentProtocol" :currentProject="currentProject" @saveAs="editApi" v-if="currentProtocol==='SQL'"/>
            <ms-debug-tcp-page :currentProtocol="currentProtocol" :currentProject="currentProject" @saveAs="editApi" v-if="currentProtocol==='TCP'"/>
            <ms-debug-dubbo-page :currentProtocol="currentProtocol" :currentProject="currentProject" @saveAs="editApi" v-if="currentProtocol==='DUBBO'"/>
          </div>

          <!-- 测试-->
          <div v-else-if="item.type=== 'test'" class="ms-api-div">
            <ms-run-test-http-page :currentProtocol="currentProtocol" :api-data="runTestData" @saveAsApi="editApi" :currentProject="currentProject" v-if="currentProtocol==='HTTP'"/>
            <ms-run-test-tcp-page :currentProtocol="currentProtocol" :api-data="runTestData" @saveAsApi="editApi" :currentProject="currentProject" v-if="currentProtocol==='TCP'"/>
            <ms-run-test-sql-page :currentProtocol="currentProtocol" :api-data="runTestData" @saveAsApi="editApi" :currentProject="currentProject" v-if="currentProtocol==='SQL'"/>
            <ms-run-test-dubbo-page :currentProtocol="currentProtocol" :api-data="runTestData" @saveAsApi="editApi" :currentProject="currentProject" v-if="currentProtocol==='DUBBO'"/>
          </div>
        </el-tab-pane>

      </el-tabs>
    </ms-main-container>


  </ms-container>

</template>

<script>
  import MsNodeTree from './components/ApiModule';
  import MsApiList from './components/ApiList';
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

  import {downloadFile, getCurrentUser, getUUID} from "@/common/js/utils";

  export default {
    name: "ApiDefinition",
    components: {
      MsNodeTree,
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
        isHide: true,
        apiDefaultTab: 'default',
        currentProject: null,
        currentProtocol: null,
        currentModule: null,
        currentApi: {},
        moduleOptions: {},
        runTestData: {},
        apiTabs: [{
          title: this.$t('api_test.definition.api_title'),
          name: 'default',
          type: "list",
          closable: false
        }],
      }
    },
    watch: {
      currentProtocol() {
        this.handleCommand("closeAll");
      }
    },
    methods: {
      handleCommand(e) {
        if (e === "add") {
          this.currentApi = {status: "Underway", method: "GET", userId: getCurrentUser().id, url: "", protocol: this.currentProtocol};
          this.handleTabsEdit(this.$t('api_test.definition.request.title'), e);
        }
        else if (e === "test") {
          this.handleTabsEdit(this.$t("commons.api"), e);
        }
        else if (e === "closeAll") {
          let tabs = this.apiTabs[0];
          this.apiTabs = [];
          this.apiDefaultTab = tabs.name;
          this.apiTabs.push(tabs);
          this.refresh();
        }
        else {
          this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug");
        }
      },
      handleTabsEdit(targetName, action) {
        if (action === 'remove') {
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
        } else {
          if (targetName === undefined || targetName === null) {
            targetName = this.$t('api_test.definition.request.title');
          }
          let newTabName = getUUID().substring(0, 8);
          this.apiTabs.push({
            title: targetName,
            name: newTabName,
            closable: true,
            type: action
          });
          this.apiDefaultTab = newTabName;
        }
      },
      debug() {
        this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug");
      },
      editApi(row) {
        this.currentApi = row;
        this.handleTabsEdit(this.$t('api_test.definition.request.edit_api') + "-" + row.name, "add");
      },
      handleCase(testCase) {
        this.currentApi = testCase;
        this.isHide = false;
      },
      apiCaseClose() {
        this.isHide = true;
      },
      selectModule(data) {
        this.currentModule = data;
      },
      exportAPI() {
        if (!this.$refs.apiList[0].tableData) {
          return;
        }
        let obj = {projectName: this.currentProject.name, protocol: this.currentProtocol, data: this.$refs.apiList[0].tableData}
        downloadFile("导出API.json", JSON.stringify(obj));
      },
      refresh(data) {
        this.$refs.apiList[0].initApiTable(data);
      },
      setTabTitle(data) {
        for (let index in this.apiTabs) {
          let tab = this.apiTabs[index];
          if (tab.name === this.apiDefaultTab) {
            tab.title = this.$t('api_test.definition.request.edit_api') + "-" + data.name;
            break;
          }
        }
        this.runTestData = data;
      },
      runTest(data) {
        this.setTabTitle(data);
        this.handleCommand("test");
      },
      saveApi(data) {
        this.setTabTitle(data);
        this.$refs.apiList[0].initApiTable(data);
      },
      initTree(data) {
        this.moduleOptions = data;
      },
      changeProject(data) {
        this.currentProject = data;
      },
      changeProtocol(data) {
        this.currentProtocol = data;
      }
    }
  }
</script>

<style scoped>
  .ms-api-buttion {
    position: absolute;
    top: 100px;
    right: 4px;
    padding: 0;
    background: 0 0;
    border: none;
    outline: 0;
    cursor: pointer;
    margin-right: 10px;
    font-size: 16px;
  }

  .ms-api-div {
    overflow-y: auto;
    height: calc(100vh - 155px)
  }

  /deep/ .el-tabs__header {
    margin: 0 0 5px;
    width: calc(100% - 90px);
  }

  /deep/ .el-main {
    overflow: hidden;
  }
</style>
