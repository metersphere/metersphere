<template>
  <div class="main-content">
    <el-container class="view-container">
      <el-aside class="tree-aside">
        <select-menu
          :data="testPlans"
          :current-data="currentPlan"
          :title="$t('test_track.plan_view.plan')"
          @dataChange="changePlan">
        </select-menu>

        <plan-node-tree
          class="node-tree"
          :plan-id="planId"
          @nodeSelectEvent="selectNodeIdsChange"
          ref="tree">
        </plan-node-tree>

      </el-aside>

      <el-main>
        <test-plan-test-case-list
          @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
          @refresh="refresh"
          :plan-id="planId"
          :select-node-ids="selectNodeIds"
          ref="testCasePlanList"></test-plan-test-case-list>
      </el-main>
    </el-container>

    <test-case-relevance
      @refresh="refresh"
      :plan-id="planId"
      ref="testCaseRelevance">
    </test-case-relevance>
  </div>

</template>

<script>

    import PlanNodeTree from "./components/PlanNodeTree";
    import TestPlanTestCaseList from "./components/TestPlanTestCaseList";
    import TestCaseRelevance from "./components/TestCaseRelevance";
    import SelectMenu from "../common/SelectMenu";

    export default {
      name: "TestPlanView",
      components: {PlanNodeTree, TestPlanTestCaseList, TestCaseRelevance, SelectMenu},
      data() {
        return {
          testPlans: [],
          currentPlan: {},
          selectNodeIds: []
        }
      },
      computed: {
        planId: function () {
          return this.$route.params.planId;
        }
      },
      created() {
        this.getTestPlans();
      },
      watch: {
        planId() {
          this.getTestPlans();
        }
      },
      methods: {
        refresh() {
          this.selectNodeIds = [];
          this.$refs.tree.initTree();
        },
        selectNodeIdsChange(nodeIds) {
          this.selectNodeIds = nodeIds;
        },
        openTestCaseRelevanceDialog() {
          this.$refs.testCaseRelevance.openTestCaseRelevanceDialog();
        },
        getTestPlans() {
          this.result = this.$post('/test/plan/list/all', {}, response => {
            this.testPlans = response.data;
            this.testPlans.forEach(plan => {
              if (this.planId && plan.id === this.planId) {
                this.currentPlan = plan;
              }
            });
          });
        },
        changePlan(plan) {
          this.currentPlan = plan;
          this.$router.push('/track/plan/view/' + plan.id);
        }
      }
    }
</script>

<style scoped>


  .node-tree {
    margin: 3%;
  }

  .view-container {
    height: calc(100vh - 150px);
    min-height: 600px;
  }

  .tree-aside {
    position: relative;
    border: 1px solid #EBEEF5;
    box-sizing: border-box;
    background: white;
  }

  .el-main {
    padding: 15px;
  }

</style>
