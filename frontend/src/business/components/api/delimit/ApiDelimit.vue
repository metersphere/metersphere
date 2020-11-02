<template>
  <ms-container>

    <ms-aside-container>
      <ms-node-tree></ms-node-tree>
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
            :select-node-ids="selectApi"
            :select-parent-nodes="selectParentNodes"
            @testCaseEdit="editTestCase"
            @handleCase="handleCase"
            @batchMove="batchMove"
            @refresh="refresh"
            @moveToNode="moveToNode"
            ref="testCaseList">
          </ms-api-list>

          <div v-else-if="item.type=== 'add'">
            <ms-api-config @runTest="runTest"></ms-api-config>
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
        editableTabs: [{
          title: this.$t('api_test.delimit.api_title'),
          name: '1',
          type: "list",
          closable: false
        }],
        tabIndex: 1,

        result: {},
        currentPage: 1,
        pageSize: 5,
        total: 0,
        projects: [],
        currentProject: null,
        treeNodes: [],
        selectApi: {},
        selectParentNodes: [],
        testCaseReadOnly: true,
        selectNode: {},
        nodeTreeDraggable: true,

      }
    },
    created() {

    },
    watch: {
      '$route': 'init',

    },

    methods: {
      handleCommand(e) {
        if (e === "add") {
          this.handleTabsEdit(this.$t('api_test.delimit.request.title'), e);
        } else if (e === "test") {
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
          let newTabName = ++this.tabIndex + '';
          this.editableTabs.push({
            title: targetName,
            name: newTabName,
            closable: true,
            type: action,
            content: MsApiConfig
          });
          this.editableTabsValue = newTabName;
        }
      },

      editTestCase(testCase) {
        this.handleTabsEdit(testCase.api_name, "add");
      },
      handleCase(testCase) {
        this.selectApi = testCase;
        this.isHide = false;
      },
      apiCaseClose() {
        this.isHide = true;
      },

      batchMove(selectIds) {
      },

      refresh() {
      },
      moveToNode() {
      },
      saveAs(data) {
        this.handleCommand("add");
      },
      runTest(data) {
        console.log(data);
        this.handleCommand("test");
      },
      search() {
          alert(1);
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
