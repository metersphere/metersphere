<template>
  <div class="container">
    <div class="main-content">
      <test-plan-list
        @openTestPlanEditDialog="openTestPlanEditDialog"
        @testPlanEdit="openTestPlanEditDialog"
        ref="testPlanList"></test-plan-list>
      <test-plan-edit
        ref="testPlanEditDialog"
        @refresh="refreshTestPlanList"></test-plan-edit>
    </div>
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
    mounted() {
      if (this.$route.path.indexOf("/track/plan/create") >= 0){
        this.openTestPlanEditDialog();
        this.$router.push('/track/plan/all');
      }
    },
    watch: {
      '$route'(to, from) {
        if (to.path.indexOf("/track/plan/create") >= 0){
          this.openTestPlanEditDialog();
          this.$router.push('/track/plan/all');
        }
      }
    },
    methods: {
      openTestPlanEditDialog(data) {
        this.$refs.testPlanEditDialog.openTestPlanEditDialog(data);
      },
      refreshTestPlanList() {
        this.$refs.testPlanList.condition = {};
        this.$refs.testPlanList.initTableData();
      }
    }
  }
</script>

<style scoped>

</style>
