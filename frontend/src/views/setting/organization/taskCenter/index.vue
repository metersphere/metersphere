<template>
  <MsCard simple no-content-padding>
    <TaskCenter group="organization" />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import TaskCenter from '@/views/project-management/taskCenter/component/taskCom.vue';
</script>

<style scoped></style>
