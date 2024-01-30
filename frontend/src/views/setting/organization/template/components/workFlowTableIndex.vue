<template>
  <MsCard has-breadcrumb simple>
    <WorkflowTable
      mode="organization"
      :delete-permission="['ORGANIZATION_TEMPLATE:READ+DELETE']"
      :update-permission="['ORGANIZATION_TEMPLATE:READ+UPDATE']"
      :create-permission="['ORGANIZATION_TEMPLATE:READ+ADD']"
    />
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织=模板-工作流首页
   */
  import MsCard from '@/components/pure/ms-card/index.vue';
  import WorkflowTable from './workflowTable.vue';
</script>

<style scoped></style>
