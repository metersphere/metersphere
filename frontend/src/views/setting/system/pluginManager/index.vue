/* stylelint-disable order/properties-order */
<template>
  <MsCard simple>
    <a-alert :closable="true">
      <div>
        {{ t('system.plugin.alertDescribe') }}
        <a class="mx-1" href="javascript:;">{{ t('system.plugin.viewTable') }}</a
        >{{ t('system.plugin.downAddress') }}
        <a class="mx-1" href="https://github.com/metersphere" target="_blank">{{ t('system.plugin.goDownload') }} </a>
      </div>
    </a-alert>
    <div class="mt-4">
      <pluginTable />
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  import pluginTable from './components/pluginTable.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
</script>

<style scoped>
  a {
    color: rgb(var(--primary-5));
  }
</style>
