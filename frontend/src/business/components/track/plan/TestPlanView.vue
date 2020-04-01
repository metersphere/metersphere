<template>

  <div class="plan_container">
    <el-container>
      <el-aside width="250px">
        <plan-node-tree
          :plan-id="planId"
          class="node_tree"
          @nodeSelectEvent="get"
          @refresh="refresh"
          ref="nodeTree"></plan-node-tree>
      </el-aside>

    </el-container>
  </div>

</template>

<script>

    import PlanNodeTree from "./components/PlanNodeTree";

    export default {
      name: "TestPlanView",
      components: {PlanNodeTree},
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
