<template>
  <div>
    <div class="recent-text">
      <i class="el-icon-time"/>
      <span>{{$t('load_test.recent')}}</span>
    </div>
    <el-menu-item :key="p.id" v-for="p in recentReports"
                  :index="'/api/report/view/' + p.id" :route="{path: '/api/report/view/' + p.id}">
      <span class="title">{{ p.name }}</span>
    </el-menu-item>
  </div>
</template>

<script>
  import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "../../../../common/constants";

  export default {
    name: "ApiRecentReport",
    mounted() {
      const rolesString = localStorage.getItem("roles");
      const roles = rolesString.split(',');

      if (roles.indexOf(ROLE_TEST_MANAGER) > -1 || roles.indexOf(ROLE_TEST_USER) > -1 || roles.indexOf(ROLE_TEST_VIEWER) > -1) {
        this.$get('/api/report/recent/5', (response) => {
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
    padding: 0 10px;
    margin-top: -5px;
    line-height: 36px;
    color: #777777;
    background-color: #F5F5F5;
  }

  .recent-text span {
    padding-left: 6px;
  }

  .title {
    padding-left: 20px;
  }
</style>
