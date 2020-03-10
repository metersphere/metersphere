<template>
  <el-menu router menu-trigger="click" :default-active="$route.path">
    <div class="recent-text">
      <i class="el-icon-time"/>
      {{$t('load_test.recent')}}
    </div>
    <el-menu-item :key="p.id" v-for="p in recentReports"
                  :index="'/reportView/' + p.id" :route="{path: '/reportView/' + p.id}">
      {{ p.name }}
    </el-menu-item>
  </el-menu>
</template>

<script>
  import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "../../../common/constants";
  import {hasRoles} from "../../../common/utils";

  export default {
    name: "MsRecentReport",
    mounted() {

      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.$get('/report/recent/5', (response) => {
          this.recentReports = response.data;
        });
      }
    },
    methods: {},
    data() {
      return {
        recentReports: [],
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
