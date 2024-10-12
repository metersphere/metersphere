<template>
  <MsDrawer
    v-model:visible="visible"
    :title="t('settings.navbar.task')"
    width="100%"
    class="ms-task-center-drawer"
    :footer="false"
  >
    <TaskCenter type="project" />
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import TaskCenter from '@/views/taskCenter/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
</script>

<style lang="less">
  .ms-task-center-drawer {
    .arco-drawer-body {
      padding-top: 0;
      .arco-tabs-tab {
        margin-left: 0;
      }
    }
  }
</style>
