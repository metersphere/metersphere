<template>

  <div class="plan_container">
    <el-container>
      <el-aside class="node-tree" width="250px">
        <plan-node-tree
          :tree-nodes="treeNodes"
          @nodeSelectEvent="getPlanCases"
          ref="tree"></plan-node-tree>
      </el-aside>

      <el-main>
        <test-case-plan-list
          @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
          @editTestPlanTestCase="editTestPlanTestCase"
          :plan-id="planId"
          ref="testCasePlanList"></test-case-plan-list>
      </el-main>
    </el-container>

    <test-case-relevance
      @refresh="getPlanCases"
      :plan-id="planId"
      ref="testCaseRelevance"></test-case-relevance>

    <test-plan-test-case-edit
      ref="testPlanTestCaseEdit"
      @refresh="getPlanCases">
    </test-plan-test-case-edit>


  </div>

</template>

<script>

    import PlanNodeTree from "./components/PlanNodeTree";
    import TestCasePlanList from "./components/TestCasePlanList";
    import TestCaseRelevance from "./components/TestCaseRelevance";
    import TestPlanTestCaseEdit from "./components/TestPlanTestCaseEdit";

    export default {
      name: "TestPlanView",
      components: {PlanNodeTree, TestCasePlanList, TestCaseRelevance, TestPlanTestCaseEdit},
      data() {
        return {
          treeNodes: []
        }
      },
      created() {
        this.getNodeTreeByPlanId();
      },
      watch: {
        '$route'(to, from) {
          if (to.path.indexOf("/track/plan/view/") >= 0){
            this.getNodeTreeByPlanId();
          }
        }
      },
      computed: {
        planId: function () {
          return this.$route.params.planId;
        }
      },
      methods: {
        refresh() {

        },
        getPlanCases(nodeIds) {
          this.$refs.testCasePlanList.initTableData(nodeIds);
        },
        openTestCaseRelevanceDialog() {
          this.$refs.testCaseRelevance.openTestCaseRelevanceDialog(this.planId);
        },
        getNodeTreeByPlanId() {
          if(this.planId){
            this.$get("/case/node/list/plan/" + this.planId, response => {
              this.treeNodes = response.data;
            });
          }
        },
        editTestPlanTestCase(testCase) {
          let item = {};
          Object.assign(item, testCase);
          item.results = JSON.parse(item.results);
          this.$refs.testPlanTestCaseEdit.testCase = item;
          this.$refs.testPlanTestCaseEdit.dialog = true;
        }
      }
    }
</script>

<style scoped>

  .plan_container {
    background: white;
    height: 600px;
  }

  .node-tree {
    margin-top: 2%;
    margin-left: 15px;
  }

</style>
