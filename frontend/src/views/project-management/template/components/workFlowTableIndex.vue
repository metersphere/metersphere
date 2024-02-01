<template>
  <MsCard has-breadcrumb simple>
    <WorkflowTable
      mode="project"
      :delete-permission="['PROJECT_TEMPLATE:READ+DELETE']"
      :update-permission="['PROJECT_TEMPLATE:READ+UPDATE']"
      :create-permission="['PROJECT_TEMPLATE:READ+ADD']"
    />
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-模板-模板管理-工作流首页
   */
  import MsCard from '@/components/pure/ms-card/index.vue';
  import WorkflowTable from '@/views/setting/organization/template/components/workflowTable.vue';
</script>

<style scoped></style>
