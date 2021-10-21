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
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";

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
