<template>
  <MsCard has-breadcrumb simple>
    <FieldSetting
      mode="organization"
      :delete-permission="['ORGANIZATION_TEMPLATE:READ+DELETE']"
      :update-permission="['ORGANIZATION_TEMPLATE:READ+UPDATE']"
      :create-permission="['ORGANIZATION_TEMPLATE:READ+ADD']"
  /></MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板管理-字段列表首页
   */

  import MsCard from '@/components/pure/ms-card/index.vue';
  import FieldSetting from '@/views/setting/organization/template/components/fieldSetting.vue';
</script>

<style scoped></style>
