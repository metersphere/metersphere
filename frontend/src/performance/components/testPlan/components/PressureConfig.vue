<style>

</style>

<template>
  <div>
    我是第二个子组件
    <el-button @click="testChange()" type="text" size="small">修改TestPlan值</el-button>
  </div>
</template>

<script>
  export default {
    name: "TestPlanPressureConfig",
    data() {
      return {
      }
    },
    methods: {
      testChange() {
        this._changeTestPlan(function (testPlan) {
          testPlan.pressureConifg = "1"
        })
      },
      _changeTestPlan(updateTestPlanFunc) {
        this.$emit('change-test-plan', updateTestPlanFunc);
      },
    }
  }
</script>
