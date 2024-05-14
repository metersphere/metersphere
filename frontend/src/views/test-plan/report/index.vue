<template>
  <MsCard simple no-content-padding>
    <!-- 报告列表-->
    <ReportList />
  </MsCard>
</template>

<script setup lang="ts">
  import MsCard from '@/components/pure/ms-card/index.vue';
  import ReportList from './component/reportList.vue';
</script>

<style scoped lang="less">
  :deep(.arco-tabs-content) {
    padding-top: 0;
  }
</style>
