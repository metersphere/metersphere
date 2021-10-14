<template>
  <el-col>
    <project-header-menus/>
    <div>
      <transition>
        <keep-alive>
          <router-view :baseUrl="baseUrl"/>
        </keep-alive>
      </transition>
    </div>
  </el-col>
</template>

<script>


import ProjectHeaderMenus from "@/business/components/project/head/ProjectHeaderMenus";

export default {
  name: "ProjectSetting",
  components: {ProjectHeaderMenus},
  data() {
    return {
      baseUrl: "project"
    }
  },
  activated() {
    this.$refs.table.doLayout()
  }
}
</script>


<style>
.node-tree {
  margin: 3%;
}
</style>

<style scoped>


</style>
