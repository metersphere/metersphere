<template>
  <ms-container v-if="renderComponent" v-loading="loading">

    <ms-aside-container>
      <test-case-node-tree
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setTreeNodes="setTreeNodes"
        @exportTestCase="exportTestCase"
        @saveAsEdit="editTestCase"
        @refreshAll="refreshAll"
        :type="'edit'"
        ref="nodeTree"
      />
    </ms-aside-container>

    <ms-main-container>
      <el-tabs v-model="activeName" @tab-click="addTab" @tab-remove="removeTab">
        <el-tab-pane name="default" :label="$t('api_test.definition.case_title')">
          <ms-tab-button
            :active-dom.sync="activeDom"
            :left-tip="$t('api_test.definition.case_title')"
            :left-content="'CASE'"
            :right-tip="$t('test_track.case.minder')"
            :right-content="$t('test_track.case.minder')"
            :middle-button-enable="false">
          <test-case-list
            v-if="activeDom === 'left'"
            :checkRedirectID="checkRedirectID"
            :module-options="moduleOptions"
            :select-node-ids="selectNodeIds"
            :isRedirectEdit="isRedirectEdit"
            :select-parent-nodes="selectParentNodes"
            :tree-nodes="treeNodes"
            @testCaseEdit="editTestCase"
            @testCaseCopy="copyTestCase"
            @testCaseDetail="showTestCaseDetail"
            @refresh="refresh"
            @refreshAll="refreshAll"
            @setCondition="setCondition"
            ref="testCaseList">
          </test-case-list>
          <test-case-minder
            :tree-nodes="treeNodes"
            :project-id="projectId"
            v-if="activeDom === 'right'"
            ref="testCaseList"/>
          </ms-tab-button>
        </el-tab-pane>
        <el-tab-pane
          :key="item.name"
          v-for="(item) in tabs"
          :label="item.label"
          :name="item.name"
          closable>
          <div class="ms-api-scenario-div">
            <test-case-edit
              :currentTestCaseInfo="item.testCaseInfo"
              @refresh="refreshTable"
              @setModuleOptions="setModuleOptions"
              :read-only="testCaseReadOnly"
              :tree-nodes="treeNodes"
              :select-node="selectNode"
              :select-condition="condition"
              :type="type"
              @addTab="addTab"
              ref="testCaseEdit">
            </test-case-edit>
          </div>
        </el-tab-pane>
        <el-tab-pane name="add">
          <template v-slot:label>
            <el-dropdown @command="handleCommand" v-tester>
              <el-button type="primary" plain icon="el-icon-plus" size="mini" v-tester/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="ADD">{{ $t('test_track.case.create') }}</el-dropdown-item>
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

import NodeTree from '../common/NodeTree';
import TestCaseEdit from './components/TestCaseEdit';
import TestCaseList from "./components/TestCaseList";
import SelectMenu from "../common/SelectMenu";
import MsContainer from "../../common/components/MsContainer";
import MsAsideContainer from "../../common/components/MsAsideContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import {checkoutTestManagerOrTestUser, getCurrentProjectID, getUUID} from "../../../../common/js/utils";
import TestCaseNodeTree from "../common/TestCaseNodeTree";

import MsTabButton from "@/business/components/common/components/MsTabButton";
import TestCaseMinder from "@/business/components/track/common/minder/TestCaseMinder";

export default {
  name: "TestCase",
  components: {
    TestCaseMinder,
    MsTabButton,
    TestCaseNodeTree,
    MsMainContainer,
    MsAsideContainer, MsContainer, TestCaseList, NodeTree, TestCaseEdit, SelectMenu
  },
  comments: {},
  data() {
    return {
      result: {},
      projects: [],
      treeNodes: [],
      selectNodeIds: [],
      selectParentNodes: [],
      testCaseReadOnly: true,
      selectNode: {},
      condition: {},
      moduleOptions: [],
      activeName: 'default',
      tabs: [],
      renderComponent:true,
      loading: false,
      type:'',
      activeDom: 'left',
      projectId: ""
    }
  },
  mounted() {
    this.init(this.$route);
    this.projectId = getCurrentProjectID();
  },
  watch: {
    redirectID() {
      this.renderComponent = false;
      this.$nextTick(() => {
        // 在 DOM 中添加 my-component 组件
        this.renderComponent = true;
      });
    },
    '$route'(to, from) {
      this.init(to);
      if (to.path.indexOf('/track/case/all') == -1) {
        if (this.$refs && this.$refs.autoScenarioConfig) {
          console.log(this.$refs.autoScenarioConfig);
          this.$refs.autoScenarioConfig.forEach(item => {
            /*item.removeListener();*/
          });
        }
      }
    },
  },
  computed: {
    checkRedirectID: function () {
      let redirectIDParam = this.$route.params.redirectID;
      this.changeRedirectParam(redirectIDParam);
      return redirectIDParam;
    },
    isRedirectEdit: function () {
      let redirectParam = this.$route.params.dataSelectRange;
      return redirectParam;
    }
  },
  methods: {
    handleCommand(e) {
      switch (e) {
        case "ADD":
          this.addTab({name: 'add'});
          break;
        case "CLOSE_ALL":
          this.handleTabClose();
          break;
        default:
          this.addTab({name: 'add'});
          break;
      }
    },
    changeRedirectParam(redirectIDParam) {
      this.redirectID = redirectIDParam;
      if (redirectIDParam != null) {
        if (this.redirectFlag == "none") {
          this.activeName = "default";
          this.addListener();
          this.redirectFlag = "redirected";
        }
      } else {
        this.redirectFlag = "none";
      }
    },
    addTab(tab) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (tab.name === 'add') {
        let label = this.$t('test_track.case.create');
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        this.type='add'
        this.tabs.push({label: label, name: name, testCaseInfo: {testCaseModuleId: "", id: getUUID()}});
      }
      if (tab.name === 'edit') {
        let label = this.$t('test_track.case.create');
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        label = tab.testCaseInfo.name;
        this.tabs.push({label: label, name: name, testCaseInfo: tab.testCaseInfo});
      }
      if (this.$refs && this.$refs.testCaseEdit) {
        this.$refs.testCaseEdit.forEach(item => {
         /* item.removeListener();*/
        });  //  删除所有tab的 ctrl + s 监听
        this.addListener();
      }
    },
    handleTabClose() {
      this.tabs = [];
      this.activeName = "default";
      this.refresh();
    },
    removeTab(targetName) {
      this.tabs = this.tabs.filter(tab => tab.name !== targetName);
      if (this.tabs.length > 0) {
        this.activeName = this.tabs[this.tabs.length - 1].name;
        this.addListener(); //  自动切换当前标签时，也添加监听
      } else {
        this.activeName = "default"
      }
    },
    exportTestCase(){
      this.$refs.testCaseList.exportTestCase()
    },
    addListener() {
      let index = this.tabs.findIndex(item => item.name === this.activeName); //  找到当前选中tab的index
      if (index != -1) {   //  为当前选中的tab添加监听
        this.$nextTick(() => {
/*
          this.$refs.testCaseEdit[index].addListener();
*/
        });
      }
    },
    init(route) {
      let path = route.path;
      if (path.indexOf("/track/case/edit") >= 0 || path.indexOf("/track/case/create") >= 0) {
        this.testCaseReadOnly = false;
        if (!checkoutTestManagerOrTestUser()) {
          this.testCaseReadOnly = true;
        }
        let caseId = this.$route.params.caseId;
        if (!this.projectId) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
/*
        this.openRecentTestCaseEditDialog(caseId);
*/
        this.$router.push('/track/case/all');
      }
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
      this.selectNode = node;
      this.selectParentNodes = pNodes;
    },
    refreshTable() {
      this.$refs.testCaseList.initTableData();
    },
    editTestCase(testCase) {
      this.type="edit"
      this.testCaseReadOnly = false;
      if (this.treeNodes.length < 1) {
        this.$warning(this.$t('test_track.case.create_module_first'));
        return;
      }
      this.addTab({name: 'edit', testCaseInfo: testCase});
    },

    copyTestCase(testCase) {
      this.type="copy"
      this.testCaseReadOnly = false;
      let item = {};
      testCase.isCopy = true;
      this.addTab({name: 'edit', testCaseInfo: testCase});
    },
    showTestCaseDetail(testCase) {
      this.testCaseReadOnly = true;
    },
    refresh() {
      this.selectNodeIds = [];
      this.selectParentNodes = [];
      this.selectNode = {};
      this.refreshTable();
    },
    refreshAll() {
      this.$refs.nodeTree.list();
      this.refresh();
    },
    openRecentTestCaseEditDialog(caseId) {
      if (caseId) {
        // this.getProjectByCaseId(caseId);
        this.$get('/test/case/get/' + caseId, response => {
          if (response.data) {
/*
            this.$refs.testCaseEditDialog.open(response.data);
*/
          }
        });
      } else {
/*
        this.$refs.testCaseEditDialog.open();
*/
      }
    },
    setTreeNodes(data) {
      this.treeNodes = data;
    },
    setCondition(data) {
      this.condition = data;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    }
  }
}
</script>

<style scoped>

.el-main {
  padding: 15px;
}

/deep/ .el-button-group>.el-button:first-child {
  padding: 4px 1px !important;
}

</style>
