<template>
  <ms-container>

    <ms-aside-container>
      <ms-node-tree @selectModule="selectModule" @getApiModuleTree="initTree"
                    @changeProject="changeProject"></ms-node-tree>
    </ms-aside-container>

    <ms-main-container>
      <el-dropdown size="small" split-button type="primary" class="ms-api-buttion" @click="handleCommand('add')"
                   @command="handleCommand">
        {{$t('commons.add')}}
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="debug">{{$t('api_test.delimit.request.fast_debug')}}</el-dropdown-item>
          <el-dropdown-item command="add">{{$t('api_test.delimit.request.title')}}</el-dropdown-item>
          <el-dropdown-item command="closeAll">{{$t('api_test.delimit.request.close_all_label')}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>

      <el-tabs v-model="editableTabsValue" @edit="handleTabsEdit">

        <el-tab-pane
          :key="item.name"
          v-for="(item) in editableTabs"
          :label="item.title"
          :closable="item.closable"
          :name="item.name">
          <!-- 列表集合 -->
          <ms-api-list
            v-if="item.type === 'list'"
            :current-project="currentProject"
            :current-module="currentModule"
            @editApi="editApi"
            @handleCase="handleCase"
            ref="apiList">
          </ms-api-list>

          <div v-else-if="item.type=== 'add'">
            <ms-api-config @runTest="runTest" @saveApi="saveApi" :current-api="currentApi"
                           :currentProject="currentProject"
                           :moduleOptions="moduleOptions" ref="apiConfig"></ms-api-config>
          </div>
          <div v-else-if="item.type=== 'debug'">
            <ms-debug-http-page @saveAs="saveAs"></ms-debug-http-page>
          </div>
          <div v-else-if="item.type=== 'test'">
            <!-- 测试-->
            <ms-run-test-http-page></ms-run-test-http-page>
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
  import MsBottomContainer from "./components/BottomContainer";
  import MsApiConfig from "./components/ApiConfig";
  import MsDebugHttpPage from "./components/debug/DebugHttpPage";
  import MsRunTestHttpPage from "./components/runtest/RunTestHttpPage";
  import {getCurrentUser, getUUID} from "../../../../common/js/utils";

  export default {
    name: "TestCase",
    components: {
      MsNodeTree,
      MsApiList,
      MsMainContainer,
      MsContainer,
      MsAsideContainer,
      MsBottomContainer,
      MsApiConfig,
      MsDebugHttpPage,
      MsRunTestHttpPage
    },
    comments: {},
    data() {
      return {
        projectId: null,
        isHide: true,
        editableTabsValue: '1',
        tabIndex: 1,
        result: {},
        currentPage: 1,
        pageSize: 5,
        total: 0,
        currentProject: null,
        currentModule: null,
        currentApi: {},
        moduleOptions: {},
        editableTabs: [{
          title: this.$t('api_test.delimit.api_title'),
          name: '1',
          type: "list",
          closable: false
        }],
      }
    },
    methods: {
      handleCommand(e) {
        if (e === "add") {
          this.currentApi = {
            status: "Underway",
            path: "GET",
            userId: getCurrentUser().id,
          };
          this.handleTabsEdit(this.$t('api_test.delimit.request.title'), e);
        }
        else if (e === "test") {
          this.handleTabsEdit(this.$t("commons.api"), e);
        }
        else if (e === "closeAll") {
          let tabs = this.editableTabs[0];
          this.editableTabs = [];
          this.editableTabsValue = tabs.name;
          this.editableTabs.push(tabs);
        }
        else {
          this.handleTabsEdit(this.$t('api_test.delimit.request.fast_debug'), "debug");
        }
      },
      handleTabsEdit(targetName, action) {
        if (action === 'remove') {
          let tabs = this.editableTabs;
          let activeName = this.editableTabsValue;
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
          this.editableTabsValue = activeName;
          this.editableTabs = tabs.filter(tab => tab.name !== targetName);
        } else {
          if (targetName === undefined || targetName === null) {
            targetName = this.$t('api_test.delimit.request.title');
          }
          let newTabName = getUUID().substring(0, 8);
          this.editableTabs.push({
            title: targetName,
            name: newTabName,
            closable: true,
            type: action
          });
          this.editableTabsValue = newTabName;
        }
      },

      editApi(row) {
        this.currentApi = row;
        this.handleTabsEdit(row.name, "add");
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
      refresh(data) {
        this.$refs.apiList[0].initTableData(data);
      },
      saveAs(data) {
        this.handleCommand("add");
      },
      runTest(data) {
        this.handleCommand("test");
      },
      saveApi(data) {
        for (let index in this.editableTabs) {
          let tab = this.editableTabs[index];
          if (tab.name === this.editableTabsValue) {
            tab.title = data.name;
            break;
          }
        }
        this.$refs.apiList[0].initTableData(data);

      },
      initTree(data) {
        this.moduleOptions = data;
      },
      changeProject(data) {
        this.currentProject = data;
      }
    }
  }
</script>

<style scoped>
  .ms-api-buttion {
    position: absolute;
    top: 100px;
    right: 20px;
    padding: 0;
    background: 0 0;
    border: none;
    outline: 0;
    cursor: pointer;
    margin-right: 10px;
    font-size: 16px;
  }

  /deep/ .el-tabs__header {
    margin-right: 82px;
  }

</style>
