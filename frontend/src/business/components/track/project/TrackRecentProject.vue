<template>
  <el-menu router menu-trigger="click" :default-active="$route.path">
    <div class="recent-text">
      <i class="el-icon-time"/>
      {{$t('project.recent')}}
    </div>

    <el-menu-item :key="p.id" v-for="p in recentProjects"
                  @click="routeToTestCase(p)">
      {{ p.name }}
    </el-menu-item>
  </el-menu>
</template>
<script>

  import {CURRENT_PROJECT, ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "../../../../common/js/constants";
  import {hasRoles} from "../../../../common/js/utils";

  export default {
    name: "TrackRecentProject",
    mounted() {

      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.$get('/project/recent/5', (response) => {
          this.recentProjects = response.data;
        });
      }
    },
    data() {
      return {
        recentProjects: [],
      }
    },
    methods: {
      routeToTestCase(project) {
        localStorage.setItem(CURRENT_PROJECT, JSON.stringify(project));
        if(this.$router.currentRoute.path === '/track/case/all'){
          this.$router.go(0);
        } else {
          this.$router.push("/track/case/all");
        }
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
