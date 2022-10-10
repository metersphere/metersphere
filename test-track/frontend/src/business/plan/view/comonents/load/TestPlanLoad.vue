<template>
  <ms-test-plan-common-component :show-aside="false">
    <template v-slot:main>
      <test-plan-load-case-list
        class="table-list"
        @refresh="refresh"
        :plan-id="planId"
        :plan-status="planStatus"
        :clickType="clickType"
        :select-project-id="selectProjectId"
        :select-parent-nodes="selectParentNodes"
        :version-enable="versionEnable"
        @relevanceCase="openTestCaseRelevanceDialog"
        ref="testPlanLoadCaseList"/>
    </template>

    <test-case-load-relevance
      @refresh="refresh"
      :plan-id="planId"
      :version-enable="versionEnable"
      ref="testCaseLoadRelevance"/>
  </ms-test-plan-common-component>
</template>

<script>

import MsTestPlanCommonComponent from "@/business/plan/view/comonents/base/TestPlanCommonComponent";
import NodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import TestPlanLoadCaseList from "@/business/plan/view/comonents/load/TestPlanLoadCaseList";
import TestCaseLoadRelevance from "@/business/plan/view/comonents/load/TestCaseLoadRelevance";

export default {
  name: "TestPlanLoad",
  components: {
    MsTestPlanCommonComponent,
    NodeTree,
    TestPlanLoadCaseList,
    TestCaseLoadRelevance,
  },
  data() {
    return {
      loading: false,
      selectNodeIds: [],
      selectParentNodes: [],
      selectProjectId: "",
      treeNodes: [],
    };
  },
  props: [
    'planId',
    'redirectCharType',
    'clickType',
    'versionEnable',
    'planStatus'
  ],
  methods: {
    refresh() {
      this.selectProjectId = '';
      this.selectParentNodes = [];
      this.$refs.testPlanLoadCaseList.initTable();
    },
    openTestCaseRelevanceDialog() {
      this.$refs.testCaseLoadRelevance.open();
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectProjectId = node.key;
      // 切换node后，重置分页数
      this.$refs.testPlanLoadCaseList.currentPage = 1;
      this.$refs.testPlanLoadCaseList.pageSize = 10;
    }
  }
};
</script>

<style scoped>

</style>
