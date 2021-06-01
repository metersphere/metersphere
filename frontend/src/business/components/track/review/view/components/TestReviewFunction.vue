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
      <ms-tab-button
        :active-dom.sync="activeDom"
        :left-tip="$t('test_track.case.list')"
        :left-content="$t('test_track.case.list')"
        :right-tip="$t('test_track.case.minder')"
        :right-content="$t('test_track.case.minder')"
        :middle-button-enable="false">
        <test-review-test-case-list
          class="table-list"
          v-if="activeDom === 'left'"
          @openTestReviewRelevanceDialog="openTestReviewRelevanceDialog"
          @refresh="refresh"
          :review-id="reviewId"
          :clickType="clickType"
          ref="testPlanTestCaseList"/>
        <test-review-minder
          :tree-nodes="treeNodes"
          :project-id="projectId"
          :review-id="reviewId"
          v-if="activeDom === 'right'"
        />
      </ms-tab-button>
    </template>
    <test-review-relevance
      @refresh="refresh"
      :review-id="reviewId"
      ref="testReviewRelevance"/>
  </ms-test-plan-common-component>
</template>

<script>
import MsTestPlanCommonComponent from "@/business/components/track/plan/view/comonents/base/TestPlanCommonComponent";
import MsNodeTree from "@/business/components/track/common/NodeTree";
import TestReviewRelevance from "@/business/components/track/review/view/components/TestReviewRelevance";
import TestReviewTestCaseList from "@/business/components/track/review/view/components/TestReviewTestCaseList";
import MsTabButton from "@/business/components/common/components/MsTabButton";
import TestReviewMinder from "@/business/components/track/common/minder/TestReviewMinder";
import {getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "TestReviewFunction",
  components: {
    TestReviewMinder,
    MsTabButton,
    TestReviewTestCaseList,
    TestReviewRelevance, MsNodeTree, MsTestPlanCommonComponent
  },
  data() {
    return {
      result: {},
      testReviews: [],
      currentReview: {},
      // selectNodeIds: [],
      // selectParentNodes: [],
      treeNodes: [],
      isMenuShow: true,
      activeDom: 'left',
    }
  },
  props: [
    'reviewId',
    'redirectCharType',
    'clickType',
  ],
  mounted() {
    this.getNodeTreeByReviewId()
  },
  activated() {
    this.getNodeTreeByReviewId()
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    refresh() {
      this.$store.commit('setTestReviewSelectNode', {});
      this.$store.commit('setTestReviewSelectNodeIds', []);
      this.$refs.testReviewRelevance.search();
      this.getNodeTreeByReviewId();
    },
    nodeChange(node, nodeIds, pNodes) {
      this.$store.commit('setTestReviewSelectNode', node);
      this.$store.commit('setTestReviewSelectNodeIds', nodeIds);
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
/deep/ .el-button-group>.el-button:first-child {
  padding: 4px 1px !important;
}
</style>
