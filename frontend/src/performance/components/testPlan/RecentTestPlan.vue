<template>
  <div>
    <div class="recent-text">
      <i class="el-icon-time"/>
      最近的测试
    </div>
    <el-menu-item v-bind:key="recentTestPlan.id" v-for="recentTestPlan in recentTestPlans">
      {{ recentTestPlan.name }}
    </el-menu-item>
  </div>
</template>

<script>
  export default {
    name: "RecentTestPlan",
    data() {
      return {
        recentTestPlans: [{
          id: 1,
          name: "测试1"
        }, {
          id: 2,
          name: "测试2"
        }, {
          id: 3,
          name: "测试3"
        }, {
          id: 4,
          name: "测试4"
        }]
      }
    }
  }
</script>

<style scoped>
  .recent-text {
    padding-left: 10%;
    color: #777777;
  }
</style>
