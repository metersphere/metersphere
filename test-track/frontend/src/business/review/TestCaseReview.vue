<template>
  <ms-container>
    <ms-aside-container :enable-remember-width="true" max-width="600px" :enable-aside-hidden.sync="enableAsideHidden" class="review-aside">
      <test-case-review-node-tree ref="reviewNodeTree" :review-condition="condition" @setTreeNodes="setTreeNodes"
                                  @nodeSelectEvent="handleCaseNodeSelect" @refreshTable="refreshCaseReviewList"/>
    </ms-aside-container>

    <ms-main-container>
      <test-case-review-list
        @openCaseReviewEditDialog="openCaseReviewEditDialog"
        @caseReviewEdit="openCaseReviewEditDialog"
        @refreshTree="refreshTreeByCondition"
        @setCondition="setCondition"
        :tree-nodes="treeNodes"
        :current-node="currentNode"
        :current-select-nodes="currentSelectNodes"
        ref="caseReviewList"/>
    </ms-main-container>

    <test-case-review-edit ref="caseReviewEditDialog" @refresh="refreshCaseReviewList"/>
  </ms-container>
</template>

<script>
import TestCaseReviewNodeTree from "@/business/module/TestCaseReviewNodeTree";
import TestCaseReviewList from "./components/TestCaseReviewList";
import TestCaseReviewEdit from "./components/TestCaseReviewEdit";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: "TestCaseReview",
  components: {
    MsMainContainer,
    MsContainer,
    TestCaseReviewList,
    TestCaseReviewEdit,
    MsAsideContainer,
    TestCaseReviewNodeTree
  },
  data() {
    return {
      condition: {},
      currentNode: null,
      currentSelectNodes: [],
      enableAsideHidden: true,
      treeNodes: []
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  mounted() {
    if (this.$route.path.indexOf("/track/review/create") >= 0){
      this.openCaseReviewEditDialog();
      this.$router.push('/track/review/all');
    }
  },
  watch: {
    '$route'(to) {
      if (to.path.indexOf("/track/review/create") >= 0){
        if (!this.projectId) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        this.openCaseReviewEditDialog();
        this.$router.push('/track/review/all');
      } else if (to.path.indexOf("/track/review/all") >= 0) {
        // 清空模块树相关参数
        this.currentNode = null;
        this.currentSelectNodes = [];
        this.$refs.reviewNodeTree.currentNode = {};
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
    openCaseReviewEditDialog(data) {
      this.$refs.caseReviewEditDialog.openCaseReviewEditDialog(data, this.currentNode);
    },
    refreshCaseReviewList(nodeIds) {
      this.$refs.caseReviewList.condition = {};
      this.$refs.caseReviewList.initTableData(nodeIds ? nodeIds : this.currentSelectNodes);
    },
    refreshTreeByCondition() {
      this.$refs.reviewNodeTree.list();
    },
    handleCaseNodeSelect(node, nodeIds, pNodes) {
      this.currentNode = node;
      this.currentSelectNodes = nodeIds;
      this.$refs.caseReviewList.initTableData(nodeIds);
    }
  }
}
</script>

<style>
.review-aside .hiddenBottom {
  top: 300px!important;
}

.review-aside .el-icon-arrow-left:before {
  font-size: 17px;
}

.review-aside .el-icon-arrow-right:before {
  font-size: 17px;
}

.review-aside .hiddenBottom i {
  margin-left: -4px;
  margin-top: 18px;
}
</style>
