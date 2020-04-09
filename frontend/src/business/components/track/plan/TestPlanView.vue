<template>
  <div class="container">
    <div class="main-content">
      <el-container>
        <el-aside class="aside-container" width="250px">
          <select-menu
            :data="testPlans"
            :current-data="currentPlan"
            :title="$t('test_track.plan')"
            @dataChange="changePlan">
          </select-menu>

          <plan-node-tree
            class="node-tree"
            :plan-id="planId"
            @nodeSelectEvent="getPlanCases"
            ref="tree">
          </plan-node-tree>

        </el-aside>

        <el-main>
          <test-plan-test-case-list
            @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
            @editTestPlanTestCase="editTestPlanTestCase"
            @refresh="refresh"
            :plan-id="planId"
            ref="testCasePlanList"></test-plan-test-case-list>
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
  </div>

</template>

<script>

    import PlanNodeTree from "./components/PlanNodeTree";
    import TestPlanTestCaseList from "./components/TestPlanTestCaseList";
    import TestCaseRelevance from "./components/TestCaseRelevance";
    import TestPlanTestCaseEdit from "./components/TestPlanTestCaseEdit";
    import SelectMenu from "../common/SelectMenu";

    export default {
      name: "TestPlanView",
      components: {PlanNodeTree, TestPlanTestCaseList, TestCaseRelevance, TestPlanTestCaseEdit, SelectMenu},
      data() {
        return {
          testPlans: [],
          currentPlan: {}
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

  .main-content {
    padding: 0px;
    background: white;
    height: 600px;
  }

  .container {
    padding: 0px;
  }

  .aside-container {
    margin-left: 15px;
  }

  .node-tree {
    margin: 3%;
  }

</style>
