<template>
  <el-menu router menu-trigger="click" :default-active="$route.path">
    <div class="recent-text">
      <i class="el-icon-time"/>
      {{$t('load_test.recent')}}
    </div>
    <el-menu-item :key="t.id" v-for="t in recentTestPlans" :index="'/api/test/edit/' + t.id">
      {{ t.name }}
    </el-menu-item>
  </el-menu>
</template>

<script>
  import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "../../../../common/js/constants";
  import {hasRoles} from "../../../../common/js/utils";

  export default {
    name: "ApiRecentTest",
    mounted() {

      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.$get('/api/recent/5', (response) => {
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
