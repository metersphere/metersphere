<template>
  <MsCard
    class="mb-[16px]"
    :title="t('system.config.parameterConfig')"
    hide-back
    hide-footer
    auto-height
    no-content-padding
  >
    <a-tabs v-model:active-key="activeTab" class="no-content">
      <a-tab-pane key="baseConfig" :title="t('system.config.baseConfig')" />
      <a-tab-pane key="pageConfig" :title="t('system.config.pageConfig')" />
      <a-tab-pane key="authConfig" :title="t('system.config.authConfig')" />
    </a-tabs>
  </MsCard>
  <baseConfig v-show="activeTab === 'baseConfig'" />
  <pageConfig v-show="activeTab === 'pageConfig'" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import { useI18n } from '@/hooks/useI18n';
  import baseConfig from './components/baseConfig.vue';
  import pageConfig from './components/pageConfig.vue';

  const { t } = useI18n();

  const activeTab = ref('pageConfig');
</script>

<style lang="less" scoped>
  :deep(.no-content) {
    .arco-tabs-content {
      display: none;
    }
  }
</style>
