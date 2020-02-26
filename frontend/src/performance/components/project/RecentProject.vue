<template>
  <div>
    <div class="recent-text">
      <i class="el-icon-time"/>
      {{$t('project.recent')}}
    </div>
    <el-menu-item :key="recentProject.id" v-for="recentProject in recentProjects" @click="goTest(recentProject)">
      {{ recentProject.name }}
    </el-menu-item>
  </div>
</template>

<script>
  export default {
    name: "RecentProject",
    mounted() {
      this.$get('/project/recent/5', (response) => {
        this.recentProjects = response.data;
      });
    },
    methods: {
      goTest(project) {
        window.console.log(project);
        // this.$router.push({path: '/allTest', params: {projectId: project.id, projectName: project.name}});
      }
    },
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
