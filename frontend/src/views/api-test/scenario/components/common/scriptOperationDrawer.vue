<template>
  <MsDrawer
    v-model:visible="visible"
    :title="t('apiScenario.scriptOperation')"
    :width="960"
    no-content-padding
    disabled-width-drag
  >
    waiting scriptOperation
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
</script>

<style lang="less" scoped></style>
