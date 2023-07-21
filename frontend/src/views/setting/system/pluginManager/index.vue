/* stylelint-disable order/properties-order */
<template>
  <div>
    <a-alert :closable="true">
      <div>
        {{ t('system.plugin.alertDescribe') }}
        <a class="mx-1" href="javascript:;">{{ t('system.plugin.viewTable') }}</a
        >{{ t('system.plugin.downAddress') }}
        <a class="mx-1" href="javascript:;">{{ t('system.plugin.goDownload') }} </a>
      </div>
    </a-alert>
    <div class="mt-4">
      <pluginTable />
    </div>
  </div>
</template>

<script setup lang="ts">
  import pluginTable from './components/pluginTable.vue';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
</script>

<style scoped>
  a {
    color: rgb(var(--primary-5));
  }
</style>
