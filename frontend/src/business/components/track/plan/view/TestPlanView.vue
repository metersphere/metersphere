<template>

  <ms-container>

    <ms-aside-container>
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
                 :draggable="false"
                 ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
        <test-plan-test-case-list
          class="table-list"
          @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
          @refresh="refresh"
          :plan-id="planId"
          :select-node-ids="selectNodeIds"
          :select-parent-nodes="selectParentNodes"
          ref="testPlanTestCaseList"/>
    </ms-main-container>

    <test-case-relevance
      @refresh="refresh"
      :plan-id="planId"
      ref="testCaseRelevance"/>

  </ms-container>

</template>

<script>

    import NodeTree from "../../common/NodeTree";
    import TestPlanTestCaseList from "./comonents/TestPlanTestCaseList";
    import TestCaseRelevance from "./comonents/TestCaseRelevance";
    import SelectMenu from "../../common/SelectMenu";
    import MsContainer from "../../../common/components/MsContainer";
    import MsAsideContainer from "../../../common/components/MsAsideContainer";
    import MsMainContainer from "../../../common/components/MsMainContainer";

    export default {
      name: "TestPlanView",
      components: {
        MsMainContainer,
        MsAsideContainer, MsContainer, NodeTree, TestPlanTestCaseList, TestCaseRelevance, SelectMenu},
      data() {
        return {
          result: {},
          testPlans: [],
          currentPlan: {},
          selectNodeIds: [],
          selectParentNodes: [],
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
        nodeChange(nodeIds, pNodes) {
          this.selectNodeIds = nodeIds;
          this.selectParentNodes = pNodes;
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
        },
        openTestCaseEdit(path) {
          if (path.indexOf("/plan/view/edit") >= 0){
            let caseId = this.$route.params.caseId;
            this.$get('/test/plan/case/get/' + caseId, response => {
              let testCase = response.data;
              if (testCase) {
                this.$refs.testPlanTestCaseList.handleEdit(testCase);
                this.$router.push('/track/plan/view/' + testCase.planId);
              }
            });
          }
        }
      }
    }
</script>

<style scoped>
</style>
