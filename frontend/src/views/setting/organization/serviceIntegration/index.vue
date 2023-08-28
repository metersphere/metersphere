<template>
  <MsCard class="mb-[16px]" :title="t('system.config.parameterConfig')" simple auto-height>
    <div class="h-30">
      <a-collapse :bordered="false" expand-icon-position="right" @change="changeHandler">
        <a-collapse-item key="1" class="font-medium" :header="t('organization.service.headerTip')">
          <template #expand-icon="{ active }">
            <span v-if="active" class="float-right -mr-4 text-[rgb(var(--primary-6))]">{{
              t('organization.service.packUp')
            }}</span>
            <span v-else class="float-right -mr-4 text-[rgb(var(--primary-6))]">{{
              t('organization.service.expand')
            }}</span>
          </template>
          <div class="flex w-[100%] flex-row justify-between text-sm font-normal">
            <div
              v-for="(item, index) in cardContent"
              :key="item.id"
              class="item mt-4 p-[16px]"
              :class="`ms-item-${index}`"
            >
              <span class="mr-3">
                <svg-icon width="64px" height="46px" :name="item.icon" />
              </span>
              <div class="flex h-[100%] flex-1 flex-col justify-between">
                <div class="flex items-center justify-between">
                  <span class="font-normal">{{ t(item.title) }}</span>
                  <span>
                    <a-button
                      v-for="links of item.skipTitle"
                      :key="links.name"
                      size="mini"
                      class="ml-3 px-0 text-sm"
                      type="text"
                      @click.stop="jumpHandler(links)"
                    >
                      {{ t(links.name) }}
                    </a-button>
                  </span>
                </div>
                <div class="text-xs text-[var(--color-text-4)]">
                  <div class="one-line-text w-[400px]"> {{ t(item.description) }}</div>
                </div>
              </div>
            </div>
          </div>
        </a-collapse-item>
      </a-collapse>
    </div>
  </MsCard>
  <ServiceList :collapse-height="collapseHeight" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import ServiceList from './components/serviceList.vue';
  import { useRouter } from 'vue-router';
  import type { StepListType, SkipTitle } from '@/models/setting/serviceIntegration';
  import { openWindow } from '@/utils/index';
  import { SettingRouteEnum } from '@/enums/routeEnum';
  import settings from '@/locale/zh-CN/settings';

  const { t } = useI18n();
  const router = useRouter();
  const cardContent = ref<StepListType[]>([
    {
      id: '1001',
      icon: 'configplugin',
      title: 'organization.service.downloadPluginOrDev',
      skipTitle: [
        {
          name: 'organization.service.developmentDoc',
          src: 'https://github.com/metersphere/metersphere-platform-plugin/wiki/%E6%8F%92%E4%BB%B6%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97',
          active: false,
        },
        {
          name: 'organization.service.downPlugin',
          src: 'https://github.com/metersphere/metersphere-platform-plugin',
          active: false,
        },
      ],
      step: '@/assets/images/ms_plugindownload.jpg',
      description: 'organization.service.description',
    },
    {
      id: '1002',
      icon: 'downloadplugin',
      title: 'organization.service.configPlugin',
      skipTitle: [
        {
          name: 'organization.service.jumpPlugin',
          src: '',
          active: true,
        },
      ],
      step: '@/assets/images/ms_configplugin.jpg',
      description: 'organization.service.configDescription',
    },
  ]);
  const isCollapse = ref<boolean>(false);
  const collapseHeight = ref<string>('72px');

  const changeHandler = (activeKey: (string | number)[]) => {
    isCollapse.value = activeKey.length > 0;
    collapseHeight.value = activeKey.length > 0 ? '158px' : '72px';
  };

  const jumpHandler = (links: SkipTitle) => {
    if (links.active)
      router.push({
        name: SettingRouteEnum.SETTING_SYSTEM_PLUGIN_MANAGEMENT,
      });

    if (links.src && !links.active) {
      openWindow(links.src);
    }
  };
</script>

<style scoped lang="less">
  :deep(.arco-icon-hover::before) {
    display: none;
  }
  :deep(.arco-scrollbar-container) {
    width: 100% !important;
  }
  .item {
    width: calc(50% - 10px);
    height: 78px;
    border: 1px solid #ffffff;
    box-shadow: 0 0 7px rgb(120 56 135 / 10%);
    @apply flex items-center rounded-md;
  }
  .ms-item-0 {
    background: url('@/assets/images/ms_plugindownload.jpg') no-repeat center / cover;
  }
  .ms-item-1 {
    background: url('@/assets/images/ms_configplugin.jpg') no-repeat center / cover;
  }
</style>
