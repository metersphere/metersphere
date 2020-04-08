<template>
  <el-col>
    <performance-header-menus/>
    <div>
      <transition>
        <keep-alive>
          <router-view :beaseUrl="beaseUrl"/>
        </keep-alive>
      </transition>
    </div>
  </el-col>
</template>

<script>

  import PerformanceHeaderMenus from "./head/PerformanceHeaderMenus";

  export default {
    name: "PerformanceTest",
    components: {PerformanceHeaderMenus},
    data() {
      return {
        beaseUrl: "performance"
      }
    }
  }

</script>

<style scoped>

</style>
