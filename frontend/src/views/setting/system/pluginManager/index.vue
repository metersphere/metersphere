/* stylelint-disable order/properties-order */
<template>
  <MsCard simple>
    <div class="wrapper">
      <a-alert :closable="true" class="mb-4">
        <div>
          {{ t('system.plugin.alertDescribe') }}
          <a class="mx-1" href="javascript:;">{{ t('system.plugin.viewTable') }}</a
          >{{ t('system.plugin.downAddress') }}
          <a class="mx-1" href="https://github.com/metersphere" target="_blank">{{ t('system.plugin.goDownload') }} </a>
        </div>
      </a-alert>
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
  .wrapper {
    /* position: relative; */
    width: 100%;
    height: calc(100vh - 138px);
    min-height: 500px;
  }
</style>
