<template>
  <div class="main-content">
    <el-container class="view-container">
      <el-aside class="tree-aside">
        <select-menu
          :data="testPlans"
          :current-data="currentPlan"
          :title="$t('test_track.plan_view.plan')"
          @dataChange="changePlan"/>

        <node-tree class="node-tree"
                   v-loading="result.loading"
                   @nodeSelectEvent="nodeChange"
                   @refresh="refresh"
                   :tree-nodes="treeNodes"
                   ref="nodeTree"/>
      </el-aside>

      <el-main>
        <test-plan-test-case-list
          @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
          @refresh="refresh"
          :plan-id="planId"
          :select-node-ids="selectNodeIds"
          :select-node-names="selectNodeNames"
          ref="testCasePlanList"/>
      </el-main>
    </el-container>

    <test-case-relevance
      @refresh="refresh"
      :plan-id="planId"
      ref="testCaseRelevance"/>
  </div>

</template>

<script>

    import NodeTree from "../common/NodeTree";
    import TestPlanTestCaseList from "./components/TestPlanTestCaseList";
    import TestCaseRelevance from "./components/TestCaseRelevance";
    import SelectMenu from "../common/SelectMenu";

    export default {
      name: "TestPlanView",
      components: {NodeTree, TestPlanTestCaseList, TestCaseRelevance, SelectMenu},
      data() {
        return {
          result: {},
          testPlans: [],
          currentPlan: {},
          selectNodeIds: [],
          selectNodeNames: [],
          treeNodes: []
        }
      },
      computed: {
        planId: function () {
          return this.$route.params.planId;
        }
      },
      mounted() {
        this.initData();
      },
      watch: {
        planId() {
          this.initData();
        }
      },
      methods: {
        refresh() {
          this.selectNodeIds = [];
          this.selectNodeNames = [];
          this.getNodeTreeByPlanId();
        },
        initData() {
          this.getTestPlans();
          this.getNodeTreeByPlanId();
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
        nodeChange(nodeIds, nodeNames) {
          this.selectNodeIds = nodeIds;
          this.selectNodeNames = nodeNames;
        },
        changePlan(plan) {
          this.currentPlan = plan;
          this.$router.push('/track/plan/view/' + plan.id);
        },
        getNodeTreeByPlanId() {
          if(this.planId){
            this.result = this.$get("/case/node/list/plan/" + this.planId, response => {
              this.treeNodes = response.data;
            });
          }
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
