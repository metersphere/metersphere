<template>
  <ms-test-plan-common-component>
    <template v-slot:aside>
      <ms-node-tree
        class="node-tree"
        :all-label="$t('commons.all_label.review')"
        v-loading="result.loading"
        @nodeSelectEvent="nodeChange"
        :tree-nodes="treeNodes"
        ref="nodeTree"/>
    </template>
    <template v-slot:main>
      <test-review-test-case-list
        class="table-list"
        @openTestReviewRelevanceDialog="openTestReviewRelevanceDialog"
        @refresh="refresh"
        :review-id="reviewId"
        :select-node-ids="selectNodeIds"
        :select-parent-nodes="selectParentNodes"
        :clickType="clickType"
        ref="testPlanTestCaseList"/>
    </template>
    <test-review-relevance
      @refresh="refresh"
      :review-id="reviewId"
      ref="testReviewRelevance"/>
  </ms-test-plan-common-component>

</template>

<script>
import MsTestPlanCommonComponent from "@/business/components/track/plan/view/comonents/base/TestPlanCommonComponent";
import FunctionalTestCaseList from "@/business/components/track/plan/view/comonents/functional/FunctionalTestCaseList";
import MsNodeTree from "@/business/components/track/common/NodeTree";
import TestReviewRelevance from "@/business/components/track/review/view/components/TestReviewRelevance";
import TestReviewTestCaseList from "@/business/components/track/review/view/components/TestReviewTestCaseList";

export default {
  name: "TestReviewLoad",
  components: {
    TestReviewTestCaseList,
    TestReviewRelevance, MsNodeTree, FunctionalTestCaseList, MsTestPlanCommonComponent
  },
  data() {
    return {
      result: {},
      testReviews: [],
      currentReview: {},
      selectNodeIds: [],
      selectParentNodes: [],
      treeNodes: [],
      isMenuShow: true,
    }
  },
  props: [
    'reviewId',
    'redirectCharType',
    'clickType'
  ],
  mounted() {
    this.getNodeTreeByReviewId()
  },
  activated() {
    this.getNodeTreeByReviewId()

  },
  methods: {
    refresh() {
      this.selectNodeIds = [];
      this.selectParentNodes = [];
      this.$refs.testReviewRelevance.search();
      this.getNodeTreeByReviewId();
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
      this.selectParentNodes = pNodes;
    },
    getNodeTreeByReviewId() {
      if (this.reviewId) {
        this.result = this.$get("/case/node/list/review/" + this.reviewId, response => {
          this.treeNodes = response.data;
        });
      }
    },
    openTestReviewRelevanceDialog() {
      this.$refs.testReviewRelevance.openTestReviewRelevanceDialog();
    },
  }
}
</script>

<style scoped>

</style>
