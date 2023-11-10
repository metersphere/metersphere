<template>
  <MsCard has-breadcrumb simple>
    <WorkflowTable mode="project" />
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-模版-模版管理-工作流首页
   */
  import MsCard from '@/components/pure/ms-card/index.vue';
  import WorkflowTable from '@/views/setting/organization/template/components/workflowTable.vue';
</script>

<style scoped></style>
