<template>
  <div>
    <div class="recent-text">
      <i class="el-icon-time"/>
      <span>{{$t('project.recent')}}</span>
    </div>
    <el-menu-item :key="p.id" v-for="p in recentProjects"
                  :index="'/api/' + p.id" :route="{name:'fucPlan', params:{projectId:p.id, projectName:p.name}}">
      <span class="title">{{ p.name }}</span>
    </el-menu-item>
  </div>
</template>

<script>

  import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "../../../../common/js/constants";
  import {hasRoles} from "../../../../common/js/utils";

  export default {
    name: "ApiRecentProject",
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
