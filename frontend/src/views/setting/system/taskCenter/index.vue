<template>
  <MsCard simple no-content-padding>
    <TaskCenter type="system" />
  </MsCard>
</template>

<script setup lang="ts">
  import MsCard from '@/components/pure/ms-card/index.vue';
  import TaskCenter from '@/views/taskCenter/index.vue';
</script>

<style scoped lang="less">
  :deep(.ms-taskCenter-content) {
    padding: 0 16px;
  }
</style>
