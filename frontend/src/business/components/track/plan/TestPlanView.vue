<template>

  <div class="plan_container">
    <el-container>
      <el-aside width="250px">
        <plan-node-tree
          :plan-id="planId"
          class="node_tree"
          @nodeSelectEvent="get"
          ref="nodeTree"></plan-node-tree>
      </el-aside>

      <el-main>
        <test-case-plan-list
          @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
          ref="testCasePlanList"></test-case-plan-list>
      </el-main>
    </el-container>

    <test-case-relevance
      @refresh="getCaseByNodeIds"
      ref="testCaseRelevance"></test-case-relevance>

  </div>

</template>

<script>

    import PlanNodeTree from "./components/PlanNodeTree";
    import TestCasePlanList from "./components/TestCasePlanList";
    import TestCaseRelevance from "./components/TestCaseRelevance";

    export default {
      name: "TestPlanView",
      components: {PlanNodeTree, TestCasePlanList, TestCaseRelevance},
      data() {
        return {
          currentProject: {}
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
        get() {

        },
        openTestCaseRelevanceDialog(data) {
          this.$refs.testCaseRelevance.dialogFormVisible = true;
          this.$refs.testCaseRelevance.getCaseNames(this.planId);
        },
        getCaseByNodeIds() {

        }
      }
    }
</script>

<style scoped>

  .plan_container {
    background: white;
    height: 600px;
  }


</style>
