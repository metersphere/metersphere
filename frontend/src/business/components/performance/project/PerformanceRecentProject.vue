<template>
  <el-menu router menu-trigger="click" :default-active="$route.path">
    <div class="recent-text">
      <i class="el-icon-time"/>
      {{$t('project.recent')}}
    </div>

    <el-menu-item :key="p.id" v-for="p in recentProjects"
                  :index="'/performance/plan/' + p.id" :route="{name:'perPlan', params:{projectId:p.id, projectName:p.name}}">
      {{ p.name }}
    </el-menu-item>
  </el-menu>
</template>

<script>

  import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "../../../../common/constants";
  import {hasRoles} from "../../../../common/utils";

  export default {
    name: "PerformanceRecentProject",
    mounted() {

      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.$get('/project/recent/5', (response) => {
          this.recentProjects = response.data;
        });
      }
    },
    methods: {},
    data() {
      return {
        recentProjects: [],
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
