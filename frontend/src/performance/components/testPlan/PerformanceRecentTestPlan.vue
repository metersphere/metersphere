<template>
  <el-menu router menu-trigger="click" :default-active="$route.path">
    <div class="recent-text">
      <i class="el-icon-time"/>
      {{$t('load_test.recent')}}
    </div>
    <el-menu-item :key="t.id" v-for="t in recentTestPlans" :index="'/performance/plan/edit/' + t.id">
      {{ t.name }}
    </el-menu-item>
  </el-menu>
</template>

<script>
  import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "../../../common/constants";

  export default {
    name: "PerformanceRecentTestPlan",
    mounted() {
      const rolesString = localStorage.getItem("roles");
      const roles = rolesString.split(',');

      if (roles.indexOf(ROLE_TEST_MANAGER) > -1 || roles.indexOf(ROLE_TEST_USER) > -1 || roles.indexOf(ROLE_TEST_VIEWER) > -1) {
        this.$get('/testplan/recent/5', (response) => {
          this.recentTestPlans = response.data;
        });
      }

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
