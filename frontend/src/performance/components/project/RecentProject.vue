<template>
  <div>
    <div class="recent-text">
      <i class="el-icon-time"/>
      最近的项目
    </div>
    <el-menu-item v-bind:key="recentTestPlan.id" v-for="recentTestPlan in recentTestPlans">
      {{ recentTestPlan.name }}
    </el-menu-item>
  </div>
</template>

<script>
  export default {
    name: "RecentProject",
    data() {
      return {
        recentTestPlans: [
          {
            id: 1,
            name: "项目1"
          },
          {
            id: 2,
            name: "项目2"
          },
          {
            id: 3,
            name: "项目3"
          },
          {
            id: 4,
            name: "项目4"
          }
        ]
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
