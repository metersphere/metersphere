<template>
  <MsDrawer
    v-model:visible="visible"
    unmount-on-close
    :title="t('caseManagement.featureCase.caseDetail')"
    :width="960"
    :footer="false"
    no-content-padding
  >
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });
</script>

<style lang="less" scoped></style>
