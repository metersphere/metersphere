<template>
  <el-menu router menu-trigger="click" :default-active="$route.path">
    <div class="recent-text">
      <i class="el-icon-time"/>
      {{$t('project.recent')}}
    </div>

    <el-menu-item :key="p.id" v-for="p in recentProjects"
                  :index="'/loadtest/' + p.id" :route="{name:'loadtest', params:{projectId:p.id, projectName:p.name}}">
      {{ p.name }}
    </el-menu-item>
  </el-menu>
</template>

<script>
  export default {
    name: "MsRecentProject",
    mounted() {
      this.$get('/project/recent/5', (response) => {
        this.recentProjects = response.data;
      });
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
