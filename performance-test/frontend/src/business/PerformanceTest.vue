<template>
  <el-col>
    <performance-header-menus/>
    <div>
      <transition>
        <router-view :baseUrl="baseUrl"/>
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
      baseUrl: "performance",
    };
  },
};

</script>

<style scoped>

</style>
