<template>
  <MsCard class="mb-[16px]" :title="t('system.config.parameterConfig')" simple auto-height>
    <div class="h-30">
      <a-collapse :bordered="false" expand-icon-position="right" @change="changeHandler">
        <a-collapse-item key="1" class="font-medium" :header="t('organization.service.headerTip')">
          <template #expand-icon="{ active }">
            <span v-if="active" class="text-[rgb(var(--primary-6))]">{{ t('organization.service.packUp') }}</span>
            <span v-else class="text-[rgb(var(--primary-6))]">{{ t('organization.service.expand') }}</span>
          </template>
          <div class="flex w-[100%] flex-row justify-between text-sm font-normal">
            <div v-for="(item, index) in cardContent" :key="index" class="item" :class="`ms-item-${index}`">
              <span>
                <svg-icon width="64px" height="46px" :name="item.icon" />
              </span>
              <div class="flex h-[100%] flex-1 flex-col justify-between p-4">
                <div class="flex justify-between">
                  <span class="leading-6">{{ t(item.title) }}</span>
                  <span>
                    <a-button
                      v-for="links of item.skipTitle"
                      :key="links.name"
                      size="mini"
                      class="ml-3 px-0 text-sm"
                      type="text"
                      :href="links.src"
                      target="_blank"
                    >
                      {{ t(links.name) }}
                    </a-button>
                  </span>
                </div>
                <div class="text-xs text-[var(--color-text-4)]">
                  {{ t(item.description) }}
                </div>
              </div>
            </div>
          </div>
        </a-collapse-item>
      </a-collapse>
    </div>
  </MsCard>
  <ServiceList :list-height="listHeight" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import ServiceList from './components/serviceList.vue';

  const { t } = useI18n();
  const cardContent = ref([
    {
      icon: 'configplugin',
      title: 'organization.service.downloadPluginOrDev',
      skipTitle: [
        {
          name: 'organization.service.developmentDoc',
          src: '',
        },
        {
          name: 'organization.service.downPlugin',
          src: 'https://github.com/metersphere/metersphere-platform-plugin',
        },
      ],
      step: '@/assets/images/ms_plugindownload.jpg',
      description: 'organization.service.description',
    },
    {
      icon: 'downloadplugin',
      title: 'organization.service.configPlugin',
      skipTitle: [
        {
          name: 'organization.service.jumpPlugin',
          src: '',
        },
      ],
      step: '@/assets/images/ms_configplugin.jpg',
      description: 'organization.service.configDescription',
    },
  ]);
  const isCollapse = ref<boolean>(false);
  const listHeight = ref<string>('56px');

  const changeHandler = (activeKey: (string | number)[]) => {
    isCollapse.value = activeKey.length > 0;
    listHeight.value = activeKey.length > 0 ? '142px' : '56px';
  };
</script>

<style scoped lang="less">
  :deep(.arco-icon-hover::before) {
    display: none;
  }
  :deep(.arco-collapse) {
    padding: 15px;
  }
  :deep(.arco-collapse-item-header) {
    border-bottom: none;
  }
  :deep(.arco-collapse-item-content) {
    padding-top: 0;
    background: none;
  }
  :deep(.arco-collapse-item) {
    margin-bottom: 0 !important;
  }
  :deep(.arco-collapse-item-header-title) {
    font-weight: 500;
  }
  :deep(.ms-card) {
    padding: 0;
  }
  :deep(.arco-scrollbar-container) {
    width: 100% !important;
  }
  .item {
    width: calc(50% - 10px);
    border: 1px solid #ffffff;
    box-shadow: 0 0 7px rgb(120 56 135 / 10%);
    @apply flex h-20 items-center rounded-md;
  }
  .ms-item-0 {
    background: url('@/assets/images/ms_plugindownload.jpg') no-repeat center / cover;
  }
  .ms-item-1 {
    background: url('@/assets/images/ms_configplugin.jpg') no-repeat center / cover;
  }
</style>
