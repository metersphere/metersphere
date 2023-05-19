<template>
  <ms-test-plan-common-component class="test-review-container">
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
import MsTestPlanCommonComponent from "@/business/plan/view/comonents/base/TestPlanCommonComponent";
import MsNodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import TestReviewRelevance from "./TestReviewRelevance";
import TestReviewTestCaseList from "./TestReviewTestCaseList";
import MsTabButton from "metersphere-frontend/src/components/MsTabButton";
import TestReviewMinder from "@/business/common/minder/TestReviewMinder";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import IsChangeConfirm from "metersphere-frontend/src/components/IsChangeConfirm";
import {openMinderConfirm, saveMinderConfirm} from "@/business/common/minder/minderUtils";
import {getTestReviewCaseNodesByCaseFilter} from "@/api/testCase";
import VersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import {useStore} from "@/store";

export default {
  name: "TestReviewFunction",
  components: {
    IsChangeConfirm,
    TestReviewMinder,
    MsTabButton,
    TestReviewTestCaseList,
    TestReviewRelevance, MsNodeTree, MsTestPlanCommonComponent,
    'VersionSelect': VersionSelect,
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
    useStore().testReviewSelectNode = {};
    useStore().testReviewSelectNodeIds = [];
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
      useStore().testReviewSelectNode = {};
      useStore().testReviewSelectNodeIds = [];
      this.$refs.testReviewRelevance.search();
      this.getNodeTreeByReviewId();
    },
    nodeChange(node, nodeIds, pNodes) {
      useStore().testReviewSelectNode = node;
      useStore().testReviewSelectNodeIds = nodeIds;
    },
    getNodeTreeByReviewId(condition) {
      if (this.reviewId) {
        getTestReviewCaseNodesByCaseFilter(this.reviewId, condition)
          .then((response) => {
            this.treeNodes = response.data;
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
      openMinderConfirm(this, activeDom, 'PROJECT_TRACK_REVIEW:READ+EDIT');
    },
    changeConfirm(isSave) {
      saveMinderConfirm(this, isSave);
    },
    handleBeforeRouteLeave(to) {
      if (useStore().isTestCaseMinderChanged) {
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

.test-review-container >>> .ms-main-container {
  height: calc(100vh - 100px) !important;
}
</style>
