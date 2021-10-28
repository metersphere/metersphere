<template>
  <el-col>
    <performance-header-menus/>
    <div>
      <transition>
        <keep-alive :exclude="excludeCaches">
          <router-view :baseUrl="baseUrl"/>
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
      baseUrl: "performance",
      // 不缓存的路由
      excludeCaches: ['PerformanceReportView'],
    };
  },
};

</script>

<style scoped>

</style>
