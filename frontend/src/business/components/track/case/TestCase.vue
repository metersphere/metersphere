<template>
  <ms-container v-if="renderComponent" v-loading="loading">

    <ms-aside-container>
      <test-case-node-tree
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setTreeNodes="setTreeNodes"
        @exportTestCase="exportTestCase"
        @saveAsEdit="editTestCase"
        :show-operator="true"
        @createCase="handleCaseSimpleCreate($event, 'add')"
        @refreshAll="refreshAll"
        @enableTrash="enableTrash"
        :type="'edit'"
        ref="nodeTree"
      />
    </ms-aside-container>

    <ms-main-container>
      <el-tabs v-model="activeName" @tab-click="addTab" @tab-remove="removeTab">
        <el-tab-pane name="trash" v-if="trashEnable" :label="$t('commons.trash')">
          <test-case-list
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            :tree-nodes="treeNodes"
            :trash-enable="true"
            @refreshTable="refresh"
            @testCaseEdit="editTestCase"
            @testCaseCopy="copyTestCase"
            @testCaseDetail="showTestCaseDetail"
            @refresh="refresh"
            @refreshAll="refreshAll"
            @setCondition="setCondition"
            @decrease="decrease"
            :custom-num="custom_num"
            ref="testCaseList">
          </test-case-list>
        </el-tab-pane>
        <el-tab-pane name="default" :label="$t('api_test.definition.case_title')">
          <ms-tab-button
            :active-dom.sync="activeDom"
            :left-tip="$t('test_track.case.list')"
            :left-content="$t('test_track.case.list')"
            :right-tip="$t('test_track.case.minder')"
            :right-content="$t('test_track.case.minder')"
            :middle-button-enable="false">
            <test-case-list
              v-if="activeDom === 'left'"
              :checkRedirectID="checkRedirectID"
              :isRedirectEdit="isRedirectEdit"
              :tree-nodes="treeNodes"
              :trash-enable="false"
              @refreshTable="refresh"
              @testCaseEdit="editTestCase"
              @testCaseCopy="copyTestCase"
              @testCaseDetail="showTestCaseDetail"
              @refresh="refresh"
              @refreshAll="refreshAll"
              @setCondition="setCondition"
              @decrease="decrease"
              :custom-num="custom_num"
              ref="testCaseList">
            </test-case-list>
            <test-case-minder
              :tree-nodes="treeNodes"
              :project-id="projectId"
              :condition="condition"
              v-if="activeDom === 'right'"
              ref="minder"/>
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
              @caseEdit="handleCaseCreateOrEdit($event,'edit')"
              @caseCreate="handleCaseCreateOrEdit($event,'add')"
              :read-only="testCaseReadOnly"
              :tree-nodes="treeNodes"
              :select-node="selectNode"
              :select-condition="condition"
              :type="type"
              :custom-num="custom_num"
              @addTab="addTab"
              ref="testCaseEdit">
            </test-case-edit>
          </div>
        </el-tab-pane>
        <el-tab-pane name="add" v-if="hasPermission('PROJECT_TRACK_CASE:READ+CREATE')">
          <template v-slot:label>
            <el-dropdown @command="handleCommand" v-permission="['PROJECT_TRACK_CASE:READ+CREATE']">
              <el-button type="primary" plain icon="el-icon-plus" size="mini"/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="ADD" v-permission="['PROJECT_TRACK_CASE:READ+CREATE']">
                  {{ $t('test_track.case.create') }}
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

</template>

<script>

import NodeTree from '../common/NodeTree';
import TestCaseEdit from './components/TestCaseEdit';
import TestCaseList from "./components/TestCaseList";
import SelectMenu from "../common/SelectMenu";
import MsContainer from "../../common/components/MsContainer";
import MsAsideContainer from "../../common/components/MsAsideContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import {getCurrentProjectID, getUUID, hasPermission} from "@/common/js/utils";
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
      testCaseReadOnly: true,
      trashEnable: false,
      condition: {},
      activeName: 'default',
      tabs: [],
      renderComponent: true,
      loading: false,
      type: '',
      activeDom: 'left',
      custom_num: false
    };
  },
  mounted() {
    this.getProject();
    this.init(this.$route);
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
          this.$refs.autoScenarioConfig.forEach(item => {
            /*item.removeListener();*/
          });
        }
      }
    },
    activeName(newVal, oldVal) {
      if (oldVal !== 'default' && newVal === 'default' && this.$refs.minder) {
        this.$refs.minder.refresh();
      }
    },
    activeDom(newVal, oldVal) {
      this.$nextTick(() => {
        if (oldVal !== 'left' && newVal === 'left' && this.$refs.testCaseList) {
          this.$refs.testCaseList.getTemplateField();
        }
      });
    },
    trashEnable(){
      if(this.trashEnable){
        this.activeName = 'trash';
      }else {
        this.activeName = 'default';
      }
    }
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
    },

    projectId() {
      return getCurrentProjectID();
    },
    selectNodeIds() {
      return this.$store.state.testCaseSelectNodeIds;
    },
    selectNode() {
      return this.$store.state.testCaseSelectNode;
    },
    moduleOptions() {
      return this.$store.state.testCaseModuleOptions;
    }
  },
  methods: {
    hasPermission,
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
        this.type = 'add';
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
        this.activeName = "default";
      }
    },
    exportTestCase() {
      if (this.activeDom !== 'left') {
        this.$warning(this.$t('test_track.case.export.export_tip'));
        return;
      }
      this.$refs.testCaseList.exportTestCase();
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
        let caseId = this.$route.params.caseId;
        if (!this.projectId) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        if (caseId) {
          this.$get('test/case/get/' + caseId, response => {
            let testCase = response.data;
            this.editTestCase(testCase);
          });
        } else {
          this.addTab({name: 'add'});
        }
        this.$router.push('/track/case/all');
      }
    },
    nodeChange(node) {
      this.condition.trashEnable = false;
      this.trashEnable = false;
      this.activeName = "default";
    },
    refreshTable(data) {
      if (this.$refs.testCaseList) {
        this.$refs.testCaseList.initTableData();
      }
      this.$refs.nodeTree.list();
      this.setTable(data);
    },
    increase(id) {
      this.$refs.nodeTree.increase(id);
    },
    decrease(id) {
      this.$refs.nodeTree.decrease(id);
    },
    editTestCase(testCase) {
      this.type = "edit";
      this.testCaseReadOnly = false;
      if (testCase.label !== "redirect") {
        if (this.treeNodes.length < 1) {
          this.$warning(this.$t('test_track.case.create_module_first'));
          return;
        }
      }
      let hasEditPermission = hasPermission('PROJECT_TRACK_CASE:READ+EDIT');
      this.$set(testCase, 'rowClickHasPermission', hasEditPermission);
      this.addTab({name: 'edit', testCaseInfo: testCase});
    },
    handleCaseCreateOrEdit(data, type) {
      if (this.$refs.minder) {
        this.$refs.minder.addCase(data, type);
      }
    },
    handleCaseSimpleCreate(data, type) {
      if ('default-module' === data.nodeId) {
        for (let i = 0; i < this.moduleOptions.length; i++) {
          let item = this.moduleOptions[i];
          if (item.path.indexOf('默认模块') > -1) {
            data.nodeId = item.id;
            break;
          }
        }
      }
      this.handleCaseCreateOrEdit(data, type);
      if (this.$refs.minder) {
        this.$refs.minder.refresh();
      }
    },
    copyTestCase(testCase) {
      this.type = "copy";
      this.testCaseReadOnly = false;
      testCase.isCopy = true;
      this.addTab({name: 'edit', testCaseInfo: testCase});
    },
    showTestCaseDetail(testCase) {
      this.testCaseReadOnly = true;
    },
    refresh(data) {
      this.$store.commit('setTestCaseSelectNode', {});
      this.$store.commit('setTestCaseSelectNodeIds', []);
      this.refreshTable(data);
    },
    setTable(data) {
      if (data) {
        for (let index in this.tabs) {
          let tab = this.tabs[index];
          if (tab.name === this.activeName) {
            tab.label = data.name;
            break;
          }
        }
      }
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
    getProject() {
      this.$get("/project/get/" + this.projectId, result => {
        let data = result.data;
        if (data) {
          this.custom_num = data.customNum;
        }
      });
    },
    enableTrash(data) {
      this.initApiTableOpretion = "trashEnable";
      this.trashEnable = data;
    },
  }
};
</script>

<style scoped>

.el-main {
  padding: 5px 10px;
}

/deep/ .el-button-group > .el-button:first-child {
  padding: 4px 1px !important;
}

/deep/ .el-tabs__header {
  margin: 0 0 0px;
  /*width: calc(100% - 90px);*/
}

</style>
