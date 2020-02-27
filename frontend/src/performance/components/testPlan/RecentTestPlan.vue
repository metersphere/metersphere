<template>
  <el-menu router menu-trigger="click" :default-active="$route.path">
    <div class="recent-text">
      <i class="el-icon-time"/>
      最近的测试
    </div>
    <el-menu-item :key="t.id" v-for="t in recentTestPlans" :index="'/editTest/' + t.id">
      {{ t.name }}
    </el-menu-item>
  </el-menu>
</template>

<script>
  export default {
    name: "MsRecentTestPlan",
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
