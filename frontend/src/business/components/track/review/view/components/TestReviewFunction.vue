<template>
  <ms-test-plan-common-component>
    <template v-slot:aside>
      <ms-node-tree
        v-loading="result.loading"
        class="node-tree"
        local-suffix="test_case"
        default-label="未规划用例"
        :tree-nodes="treeNodes"
        :default-expand-all="true"
        :all-label="$t('commons.all_label.review')"
        @nodeSelectEvent="nodeChange"
        ref="nodeTree"/>
    </template>
    <template v-slot:main>
      <ms-tab-button
        :active-dom="activeDom"
        @update:activeDom="updateActiveDom"
        :left-tip="$t('test_track.case.list')"
        :left-content="$t('test_track.case.list')"
        :right-tip="$t('test_track.case.minder')"
        :right-content="$t('test_track.case.minder')"
        :middle-button-enable="false">
        <test-review-test-case-list
          class="table-list"
          v-if="activeDom === 'left'"
          :review-id="reviewId"
          :clickType="clickType"
          :current-version="currentVersion"
          :version-enable="versionEnable"
          @refresh="refresh"
          @setCondition="setCondition"
          @search="refreshTreeByCaseFilter"
          @openTestReviewRelevanceDialog="openTestReviewRelevanceDialog"
          ref="testPlanTestCaseList"/>
        <test-review-minder
          v-if="activeDom === 'right'"
          :tree-nodes="treeNodes"
          :project-id="projectId"
          :condition="condition"
          :review-id="reviewId"
          ref="minder"
        />
      </ms-tab-button>
    </template>
    <test-review-relevance
      @refresh="refresh"
      :review-id="reviewId"
      :version-enable="versionEnable"
      ref="testReviewRelevance"/>

    <is-change-confirm
      :version-enable="versionEnable"
      @confirm="changeConfirm"
      ref="isChangeConfirm"/>

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
import IsChangeConfirm from "@/business/components/common/components/IsChangeConfirm";
import {openMinderConfirm, saveMinderConfirm} from "@/business/components/track/common/minder/minderUtils";
import {getTestReviewCaseNodesByCaseFilter} from "@/network/testCase";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};

export default {
  name: "TestReviewFunction",
  components: {
    IsChangeConfirm,
    TestReviewMinder,
    MsTabButton,
    TestReviewTestCaseList,
    TestReviewRelevance, MsNodeTree, MsTestPlanCommonComponent,
    'VersionSelect': VersionSelect.default,
  },
  data() {
    return {
      result: {},
      testReviews: [],
      currentReview: {},
      treeNodes: [],
      isMenuShow: true,
      activeDom: 'left',
      condition: {},
      tmpActiveDom: null,
      tmpPath: null,
      currentVersion : null
    }
  },
  props: [
    'reviewId',
    'redirectCharType',
    'clickType',
    'versionEnable',
  ],
  mounted() {
    this.$store.commit('setTestReviewSelectNode', {});
    this.$store.commit('setTestReviewSelectNodeIds', []);
    this.getNodeTreeByReviewId();
  },
  activated() {
    this.getNodeTreeByReviewId();
  },
  watch: {
    reviewId() {
      this.getNodeTreeByReviewId();
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    setCondition(data) {
      this.condition = data;
    },
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
    getNodeTreeByReviewId(condition) {
      if (this.reviewId) {
        this.result = getTestReviewCaseNodesByCaseFilter(this.reviewId, condition, (data) => {
          this.treeNodes = data;
        });
      }
    },
    refreshTreeByCaseFilter() {
      this.getNodeTreeByReviewId(this.condition);
    },
    openTestReviewRelevanceDialog() {
      this.$refs.testReviewRelevance.openTestReviewRelevanceDialog();
    },
    updateActiveDom(activeDom) {
      openMinderConfirm(this, activeDom);
    },
    changeConfirm(isSave) {
      saveMinderConfirm(this, isSave);
    },
    handleBeforeRouteLeave(to) {
      if (this.$store.state.isTestCaseMinderChanged) {
        this.$refs.isChangeConfirm.open();
        this.tmpPath = to.path;
        return false;
      } else {
        return true;
      }
    },
    changeVersion(currentVersion) {
      this.currentVersion = currentVersion || null;
    }
  }
}
</script>

<style scoped>
.version-select {
  padding-left: 10px;
}
</style>
