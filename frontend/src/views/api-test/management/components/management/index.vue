<template>
  <a-tabs v-model:active-key="activeTab" animation lazy-load>
    <a-tab-pane key="api" title="API">
      <api
        :active-module="activeModule"
        :module="props.module"
        :all-count="props.allCount"
        :offspring-ids="props.offspringIds"
      />
    </a-tab-pane>
    <a-tab-pane key="case" title="CASE"> </a-tab-pane>
    <a-tab-pane key="mock" title="MOCK"> </a-tab-pane>
    <a-tab-pane key="doc" :title="t('apiTestManagement.doc')"> </a-tab-pane>
  </a-tabs>
</template>

<script setup lang="ts">
  import api from './api.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    module: string;
    allCount: number;
    activeModule: string;
    offspringIds: string[];
  }>();

  const { t } = useI18n();

  const activeTab = ref('api');
</script>

<style lang="less" scoped>
  :deep(.arco-tabs-nav) {
    border-bottom: 1px solid var(--color-text-n8);
  }
</style>
