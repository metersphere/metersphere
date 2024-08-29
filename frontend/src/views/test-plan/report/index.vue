<template>
  <MsCard simple no-content-padding>
    <!-- 报告列表-->
    <ReportList />
  </MsCard>
</template>

<script setup lang="ts" name="testPlanReport">
  import MsCard from '@/components/pure/ms-card/index.vue';
  import ReportList from './component/reportList.vue';

  import { RouteEnum } from '@/enums/routeEnum';

  defineOptions({
    name: RouteEnum.TEST_PLAN_REPORT,
  });
</script>

<style scoped lang="less">
  :deep(.arco-tabs-content) {
    padding-top: 0;
  }
</style>
