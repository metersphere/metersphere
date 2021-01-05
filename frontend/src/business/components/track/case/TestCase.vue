<template>
  <ms-container>

    <ms-aside-container>
      <test-case-node-tree
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setTreeNodes="setTreeNodes"
        :type="'edit'"
        ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
      <test-case-list
        :select-node-ids="selectNodeIds"
        :select-parent-nodes="selectParentNodes"
        @testCaseEdit="editTestCase"
        @testCaseCopy="copyTestCase"
        @testCaseDetail="showTestCaseDetail"
        @batchMove="batchMove"
        @refresh="refresh"
        @refreshAll="refreshAll"
        @moveToNode="moveToNode"
        ref="testCaseList">
      </test-case-list>
    </ms-main-container>

    <test-case-edit
      @refresh="refreshTable"
      :read-only="testCaseReadOnly"
      :tree-nodes="treeNodes"
      :select-node="selectNode"
      ref="testCaseEditDialog">
    </test-case-edit>

    <test-case-move @refresh="refresh" ref="testCaseMove"/>

    <batch-move @refresh="refresh" ref="testBatchMove"/>

  </ms-container>

</template>

<script>

import NodeTree from '../common/NodeTree';
import TestCaseEdit from './components/TestCaseEdit';
import TestCaseList from "./components/TestCaseList";
import SelectMenu from "../common/SelectMenu";
import TestCaseMove from "./components/TestCaseMove";
import MsContainer from "../../common/components/MsContainer";
import MsAsideContainer from "../../common/components/MsAsideContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import {checkoutTestManagerOrTestUser, getCurrentProjectID, hasRoles} from "../../../../common/js/utils";
import BatchMove from "./components/BatchMove";
import TestCaseNodeTree from "../common/TestCaseNodeTree";

export default {
  name: "TestCase",
  components: {
    TestCaseNodeTree,
    MsMainContainer,
    MsAsideContainer, MsContainer, TestCaseMove, TestCaseList, NodeTree, TestCaseEdit, SelectMenu, BatchMove
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
    }
  },
  mounted() {
    this.init(this.$route);
  },
  watch: {
    '$route'(to, from) {
      this.init(to);
    },
  },
  methods: {
    init(route) {
      let path = route.path;
      if (path.indexOf("/track/case/edit") >= 0 || path.indexOf("/track/case/create") >= 0) {
        this.testCaseReadOnly = false;
        if (!checkoutTestManagerOrTestUser()) {
          this.testCaseReadOnly = true;
        }
        let caseId = this.$route.params.caseId;
        if (!getCurrentProjectID()) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        this.openRecentTestCaseEditDialog(caseId);
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
      this.testCaseReadOnly = false;
      if (this.treeNodes.length < 1) {
        this.$warning(this.$t('test_track.case.create_module_first'));
        return;
      }
      this.$refs.testCaseEditDialog.open(testCase);
    },
    copyTestCase(testCase) {
      this.testCaseReadOnly = false;
      let item = {};
      Object.assign(item, testCase);
      item.name = '';
      item.isCopy = true;
      this.$refs.testCaseEditDialog.open(item);
    },
    showTestCaseDetail(testCase) {
      this.testCaseReadOnly = true;
      this.$refs.testCaseEditDialog.open(testCase);
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
            this.$refs.testCaseEditDialog.open(response.data);
          }
        });
      } else {
        this.$refs.testCaseEditDialog.open();
      }
    },

    moveToNode(selectIds) {
      if (selectIds.size < 1) {
        this.$warning(this.$t('test_track.plan_view.select_manipulate'));
        return;
      }
      this.$refs.testCaseEditDialog.getModuleOptions();
      this.$refs.testCaseMove.open(this.$refs.testCaseEditDialog.moduleOptions, selectIds);
    },
    batchMove(selectIds) {
      this.$refs.testBatchMove.open(this.treeNodes, selectIds, this.$refs.testCaseEditDialog.moduleOptions);
    },
    setTreeNodes(data) {
      this.treeNodes = data;
    }
  }
}
</script>

<style scoped>

.el-main {
  padding: 15px;
}

</style>
