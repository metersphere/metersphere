<template>
  <MsCard has-breadcrumb simple>
    <FieldSetting
      mode="project"
      :delete-permission="['PROJECT_TEMPLATE:READ+DELETE']"
      :update-permission="['PROJECT_TEMPLATE:READ+UPDATE']"
      :create-permission="['PROJECT_TEMPLATE:READ+ADD']"
  /></MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-模板-字段设置-首页
   */

  import MsCard from '@/components/pure/ms-card/index.vue';
  import FieldSetting from '@/views/setting/organization/template/components/fieldSetting.vue';
</script>

<style scoped></style>
