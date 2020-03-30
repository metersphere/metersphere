<template>

  <div>
    <test-plan-list
      @openTestPlanEditDialog="openTestPlanEditDialog"
      @testPlanEdit="openTestPlanEditDialog"
      ref="testPlanList"></test-plan-list>
    <test-plan-edit
      ref="testPlanEditDialog"
      @refresh="refreshTestPlanList"></test-plan-edit>
  </div>


</template>

<script>

  import TestPlanList from './components/TestPlanList';
  import TestPlanEdit from './components/TestPlanEdit';

  export default {
    name: "TestPlan",
    components: {TestPlanList, TestPlanEdit},
    data() {
      return {
      }
    },
    methods: {
      openTestPlanEditDialog(data) {
        this.$refs.testPlanEditDialog.openTestPlanEditDialog(data);
      },
      refreshTestPlanList() {
        this.$refs.testPlanList.initTableData();
      }
    }
  }
</script>

<style scoped>

</style>
