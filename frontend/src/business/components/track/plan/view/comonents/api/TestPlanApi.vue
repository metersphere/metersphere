<template>

  <ms-test-plan-common-component>
    <template v-slot:aside>
      <node-tree class="node-tree"
                 v-loading="result.loading"
                 @nodeSelectEvent="nodeChange"
                 @refresh="refresh"
                 :tree-nodes="treeNodes"
                 :draggable="false"
                 ref="nodeTree"/>
    </template>

    <template v-slot:main>
      <test-plan-api-case-list
        class="table-list"
        @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
        @refresh="refresh"
        :plan-id="planId"
        :select-node-ids="selectNodeIds"
        :select-parent-nodes="selectParentNodes"
        ref="testPlanTestCaseList"/>
    </template>

    <test-case-api-relevance
      @refresh="refresh"
      :plan-id="planId"
      ref="testCaseRelevance"/>

  </ms-test-plan-common-component>

</template>

<script>
    import NodeTree from "../../../../common/NodeTree";
    import TestPlanTestCaseList from "../functional/FunctionalTestCaseList";
    import TestCaseRelevance from "../functional/TestCaseFunctionalRelevance";
    import MsTestPlanCommonComponent from "../base/TestPlanCommonComponent";
    import TestPlanApiCaseList from "./TestPlanApiCaseList";
    import TestCaseApiRelevance from "./TestCaseApiRelevance";

    export default {
      name: "TestPlanApi",
      components: {
        TestCaseApiRelevance,
        TestPlanApiCaseList,
        MsTestPlanCommonComponent,
        TestCaseRelevance,
        TestPlanTestCaseList,
        NodeTree,
      },
      data() {
        return {
          result: {},
          selectNodeIds: [],
          selectParentNodes: [],
          treeNodes: [],
        }
      },
      props: [
        'planId'
      ],
      mounted() {
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
          this.selectParentNodes = [];
          this.$refs.testCaseRelevance.search();
          this.getNodeTreeByPlanId();
        },
        initData() {
          this.getNodeTreeByPlanId();
        },
        openTestCaseRelevanceDialog() {
          this.$refs.testCaseRelevance.open();
        },
        nodeChange(nodeIds, pNodes) {
          this.selectNodeIds = nodeIds;
          this.selectParentNodes = pNodes;
          // 切换node后，重置分页数
          this.$refs.testPlanTestCaseList.currentPage = 1;
          this.$refs.testPlanTestCaseList.pageSize = 10;
        },
        getNodeTreeByPlanId() {
          if (this.planId) {
            this.result = this.$get("/case/node/list/plan/" + this.planId, response => {
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
    }

</script>

<style scoped>

</style>
