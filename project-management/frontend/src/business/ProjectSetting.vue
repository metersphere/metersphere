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


import ProjectHeaderMenus from "@/business/head/ProjectHeaderMenus";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";

export default {
  name: "ProjectSetting",
  components: {MsContainer, ProjectHeaderMenus, MsMainContainer},
  data() {
    return {
      baseUrl: "project"
    }
  },
}
</script>


<style scoped>

</style>
