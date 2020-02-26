<template>
  <el-menu>
    <div class="recent-text">
      <i class="el-icon-time"/>
      最近的测试
    </div>
    <el-menu-item :key="recentTestPlan.id" v-for="recentTestPlan in recentTestPlans">
      {{ recentTestPlan.name }}
    </el-menu-item>
  </el-menu>
</template>

<script>
  export default {
    name: "RecentTestPlan",
    mounted() {
      this.$get('/testplan/recent/5', (response) => {
        this.recentTestPlans = response.data;
      });
    },
    data() {
      return {
        recentTestPlans: []
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
