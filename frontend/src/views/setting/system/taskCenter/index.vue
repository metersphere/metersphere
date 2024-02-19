<template>
  <MsCard simple no-content-padding>
    <TaskCenter group="system" />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import TaskCenter from '@/views/project-management/taskCenter/component/taskCom.vue';
</script>

<style scoped lang="less">
  .box {
    display: flex;
    height: 100%;
    .left {
      padding: 24px;
      width: 252px;
      height: 100%;
      border-right: 1px solid var(--color-text-n8);
      .item {
        padding: 0 20px;
        height: 38px;
        font-size: 14px;
        line-height: 38px;
        border-radius: 4px;
        cursor: pointer;
        &.active {
          background: rgb(var(--primary-1));
        }
      }
    }
    .right {
      width: calc(100% - 300px);
      flex-grow: 1; /* 自适应 */
      height: 100%;
    }
  }
  .no-content {
    :deep(.arco-tabs-content) {
      padding-top: 0;
    }
  }
</style>
