<template>
  <ms-container>
    <ms-aside-container page-key="TEST_PLAN_LIST" max-width="600px" :enable-aside-hidden.sync="enableAsideHidden" class="plan-aside">
      <test-plan-node-tree ref="planNodeTree" :plan-condition="condition" @setTreeNodes="setTreeNodes"
                                  @nodeSelectEvent="handleCaseNodeSelect" @refreshTable="refreshTestPlanList"/>
    </ms-aside-container>

    <ms-main-container>
      <test-plan-list
        @openTestPlanEditDialog="openTestPlanEditDialog"
        @testPlanEdit="openTestPlanEditDialog"
        @refreshTree="refreshTreeByCondition"
        @setCondition="setCondition"
        :current-node="currentNode"
        :current-select-nodes="currentSelectNodes"
        :tree-nodes="treeNodes"
        ref="testPlanList"/>
    </ms-main-container>

    <test-plan-edit ref="testPlanEditDialog" @refresh="refreshTestPlanList"/>
  </ms-container>
</template>

<script>
import {TEST_PLAN_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import TestPlanNodeTree from "@/business/module/TestPlanNodeTree.vue";
import TestPlanList from './components/TestPlanList';
import TestPlanEdit from './components/TestPlanEdit';
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import TestCaseReviewList from "@/business/review/components/TestCaseReviewList.vue";

export default {
  name: "TestPlan",
  components: {
    TestCaseReviewList,
    TestPlanNodeTree, MsMainContainer, MsAsideContainer, MsContainer, TestPlanList, TestPlanEdit},
  data() {
    return {
      condition: {},
      currentNode: null,
      currentSelectNodes: [],
      enableAsideHidden: true,
      treeNodes: [],
    };
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  mounted() {
    if (this.$route.path.indexOf("/track/plan/create") >= 0) {
      this.openTestPlanEditDialog();
      this.$router.push('/track/plan/all');
    }
  },
  watch: {
    '$route'(to, from) {
      if (to.path.indexOf("/track/plan/create") >= 0) {
        if (!this.projectId) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        this.openTestPlanEditDialog();
        this.$router.push('/track/plan/all');
      } else if (to.path.indexOf("/track/plan/all") >= 0) {
        // 清空模块树相关参数
        this.currentNode = null;
        this.currentSelectNodes = [];
        this.$refs.planNodeTree.currentNode = {};
      }
    }
  },
  methods: {
    setTreeNodes(data) {
      this.treeNodes = data;
    },
    setCondition(data) {
      this.condition = data;
    },
    openTestPlanEditDialog(data) {
      this.$refs.testPlanEditDialog.openTestPlanEditDialog(data, this.currentNode);
    },
    refreshTestPlanList(nodeIds) {
      this.$refs.testPlanList.condition = {components: TEST_PLAN_CONFIGS};
      this.$refs.testPlanList.initTableData(nodeIds ? nodeIds : this.currentSelectNodes);
    },
    refreshTreeByCondition() {
      this.$refs.planNodeTree.list();
    },
    handleCaseNodeSelect(node, nodeIds, pNodes) {
      this.currentNode = node;
      this.currentSelectNodes = nodeIds;
      this.$refs.testPlanList.initTableData(nodeIds);
    }
  }
};
</script>

<style>
.plan-aside .hiddenBottom {
  top: 300px!important;
}

.plan-aside .el-icon-arrow-left:before {
  font-size: 17px;
}

.plan-aside .el-icon-arrow-right:before {
  font-size: 17px;
}

.plan-aside .hiddenBottom i {
  margin-left: -4px;
  margin-top: 18px;
}

.plan-aside .node-tree {
  height: 100%;
}
</style>
