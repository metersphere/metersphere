<template>
  <MsCard simple no-content-padding>
    <TaskCenter type="org" />
  </MsCard>
</template>

<script setup lang="ts">
  import MsCard from '@/components/pure/ms-card/index.vue';
  import TaskCenter from '@/views/taskCenter/index.vue';
</script>

<style lang="less" scoped>
  :deep(.ms-taskCenter-content) {
    padding: 0 16px;
  }
</style>
