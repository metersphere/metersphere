<template>

  <ms-test-plan-common-component>
    <template v-slot:aside>
      <node-tree class="node-tree"
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
        <functional-test-case-list
          class="table-list"
          v-if="activeDom === 'left'"
          @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
          @refresh="refresh"
          :plan-id="planId"
          :clickType="clickType"
          :select-node-ids="selectNodeIds"
          ref="testPlanTestCaseList"/>
        <test-plan-minder
          :tree-nodes="treeNodes"
          :project-id="projectId"
          :plan-id="planId"
          v-if="activeDom === 'right'"
        />
      </ms-tab-button>
    </template>

    <test-case-functional-relevance
      @refresh="refresh"
      :plan-id="planId"
      ref="testCaseRelevance"/>
  </ms-test-plan-common-component>

</template>

<script>
import NodeTree from "../../../../common/NodeTree";
import TestCaseRelevance from "./TestCaseFunctionalRelevance";
import TestCaseFunctionalRelevance from "./TestCaseFunctionalRelevance";
import MsTestPlanCommonComponent from "../base/TestPlanCommonComponent";
import FunctionalTestCaseList from "./FunctionalTestCaseList";
import MsTabButton from "@/business/components/common/components/MsTabButton";
import TestPlanMinder from "@/business/components/track/common/minder/TestPlanMinder";
import {getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "TestPlanFunctional",
  components: {
    TestPlanMinder,
    MsTabButton,
    FunctionalTestCaseList,
    TestCaseFunctionalRelevance,
    MsTestPlanCommonComponent,
    TestCaseRelevance,
    NodeTree,
  },
  data() {
    return {
      result: {},
      selectNodeIds: [],
      treeNodes: [],
      activeDom: 'left',
      selectNode: {}
    };
  },
  props: [
    'planId',
    'redirectCharType',
    'clickType'
  ],
  mounted() {
    this.initData();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  activated() {
    this.initData();
    this.openTestCaseEdit(this.$route.path);
  },
  watch: {
    '$route'(to, from) {
      this.openTestCaseEdit(to.path);
    },
    planId() {
      this.initData();
    }
  },
  methods: {
    refresh() {
      this.selectNodeIds = [];
      this.$store.commit('setTestPlanViewSelectNode', {});
      this.$refs.testCaseRelevance.search();
      this.getNodeTreeByPlanId();
    },
    initData() {
      this.getNodeTreeByPlanId();
    },
    openTestCaseRelevanceDialog() {
      this.$refs.testCaseRelevance.open();
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
      this.$store.commit('setTestPlanViewSelectNode', node);
      // 切换node后，重置分页数
      if (this.$refs.testPlanTestCaseList) {
        this.$refs.testPlanTestCaseList.currentPage = 1;
        this.$refs.testPlanTestCaseList.pageSize = 10;
      }
    },
    getNodeTreeByPlanId() {
      if (this.planId) {
        let url = "/case/node/list/plan/" + this.planId;
        if (this.clickType) {
          url = url + "/" + this.clickType;
        }
        this.result = this.$get(url, response => {
          this.treeNodes = response.data;
        });
      }
    },
    openTestCaseEdit(path) {
      if (path.indexOf("/plan/view/edit") >= 0) {
        let caseId = this.$route.params.caseId;
        this.$get('/test/plan/case/get/' + caseId, response => {
          let testCase = response.data;
          if (testCase) {
            this.$refs.testPlanTestCaseList.handleEdit(testCase);
            this.$router.push('/track/plan/view/' + testCase.planId);
          }
        });
      }
    },
  }
};

</script>

<style scoped>
/deep/ .el-button-group > .el-button:first-child {
  padding: 4px 1px !important;
}
</style>
