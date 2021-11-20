<template>
  <el-col>
    <report-statistics-header-menus/>
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

import ReportStatisticsHeaderMenus from "@/business/components/reportstatistics/header/ReportStatisticsHeaderMenus";

export default {
  name: "ReportStatistics",
  components: {ReportStatisticsHeaderMenus},
  data() {
    return {
      baseUrl: "report"
    }
  }
}

</script>

<style scoped>

</style>
