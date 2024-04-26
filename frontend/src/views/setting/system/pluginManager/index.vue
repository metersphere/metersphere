<template>
  <MsCard simple>
    <div class="flex h-full flex-col overflow-hidden">
      <div class="wrapper">
        <a-alert :closable="true" class="mb-4">
          <div>
            {{ t('system.plugin.alertDescribe') }}
            <span class="mx-1 text-[rgb(var(--primary-5))]" @click="openLink">{{ t('system.plugin.viewTable') }}</span>
          </div>
        </a-alert>
        <pluginTable />
      </div>
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

<style lang="less" scoped>
  a {
    color: rgb(var(--primary-5));
  }
  .wrapper {
    @apply flex-1 overflow-y-auto overflow-x-hidden;
    .ms-scroll-bar();

    min-height: 500px;
  }
</style>
