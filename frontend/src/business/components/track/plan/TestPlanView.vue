<template>

  <div class="plan_container">
    <el-container>
      <el-aside class="node-tree" width="250px">
        <plan-node-tree
          :tree-nodes="treeNodes"
          :plan-id="planId"
          @nodeSelectEvent="getPlanCases"
          ref="tree"></plan-node-tree>
      </el-aside>

      <el-main>
        <test-case-plan-list
          @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
          @editTestPlanTestCase="editTestPlanTestCase"
          @refresh="refresh"
          :plan-id="planId"
          ref="testCasePlanList"></test-case-plan-list>
      </el-main>
    </el-container>

    <test-case-relevance
      @refresh="refresh"
      :plan-id="planId"
      ref="testCaseRelevance"></test-case-relevance>

    <test-plan-test-case-edit
      ref="testPlanTestCaseEdit"
      @refresh="refresh">
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
      computed: {
        planId: function () {
          return this.$route.params.planId;
        }
      },
      methods: {
        refresh() {
          this.getPlanCases();
          this.$refs.tree.initTree();
        },
        getPlanCases(nodeIds) {
          this.$refs.testCasePlanList.initTableData(nodeIds);
        },
        openTestCaseRelevanceDialog() {
          this.$refs.testCaseRelevance.openTestCaseRelevanceDialog();
        },
        editTestPlanTestCase(testCase) {
          let item = {};
          Object.assign(item, testCase);
          item.results = JSON.parse(item.results);
          item.steps = JSON.parse(item.steps);

          item.steptResults = [];
          for (let i = 0; i < item.steps.length; i++){
            if(item.results[i]){
              item.steps[i].actualResult = item.results[i].actualResult;
              item.steps[i].executeResult = item.results[i].executeResult;
            }
            item.steptResults.push(item.steps[i]);
          }
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
