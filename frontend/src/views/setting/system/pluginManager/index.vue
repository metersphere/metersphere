/* stylelint-disable order/properties-order */
<template>
  <MsCard simple>
    <div class="wrapper">
      <a-alert :closable="true" class="mb-4">
        <div>
          {{ t('system.plugin.alertDescribe') }}
          <span class="mx-1 text-[rgb(var(--primary-5))]" @click="openLink">{{ t('system.plugin.viewTable') }}</span>
        </div>
      </a-alert>
      <pluginTable />
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-系统-插件管理
   */
  import MsCard from '@/components/pure/ms-card/index.vue';
  import pluginTable from './components/pluginTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  function openLink() {
    window.open('https://metersphere.io/docs/v3.x/plugin/', '_blank');
  }
</script>

<style scoped>
  a {
    color: rgb(var(--primary-5));
  }
  .wrapper {
    /* position: relative; */
    width: 100%;
    height: calc(100vh - 138px);
    min-height: 500px;
  }
</style>
