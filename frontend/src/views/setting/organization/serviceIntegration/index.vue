<template>
  <MsCard class="mb-[16px]" :title="t('system.config.parameterConfig')" simple auto-height>
    <div class="h-30">
      <a-collapse :bordered="false" expand-icon-position="right" @change="changeHandler">
        <a-collapse-item key="1" class="font-medium" :header="t('organization.service.headerTip')">
          <template #expand-icon="{ active }">
            <span v-if="active" class="float-right -mr-4 text-[rgb(var(--primary-6))]">
              {{ t('organization.service.packUp') }}
            </span>
            <span v-else class="float-right -mr-4 text-[rgb(var(--primary-6))]">
              {{ t('organization.service.expand') }}
            </span>
          </template>
          <div class="flex w-[100%] flex-row justify-between pb-1 text-sm font-normal">
            <div v-for="(item, index) in cardContent" :key="item.id" class="item mt-4" :class="`ms-item-${index}`">
              <span class="mr-3">
                <svg-icon width="64px" height="46px" :name="getItemIcon(item)" />
              </span>
              <div class="flex h-[100%] flex-1 flex-col justify-between">
                <div class="flex items-center justify-between">
                  <span class="font-normal">{{ t(item.title) }}</span>
                  <span class="flex items-center justify-end">
                    <div v-for="links of item.skipTitle" :key="links.name">
                      <a-tooltip
                        v-if="links.active"
                        :content="
                          isHasSystemPermission
                            ? t('organization.service.jumpPlugin')
                            : t('organization.service.noPermissionsTip')
                        "
                        position="left"
                        size="small"
                      >
                        <a-button
                          size="mini"
                          class="ml-3 px-0 text-sm"
                          type="text"
                          :disabled="links.disabled"
                          @click.stop="jumpHandler(links)"
                        >
                          {{ t(links.name) }}
                        </a-button>
                      </a-tooltip>
                      <a-button
                        v-else
                        size="mini"
                        class="ml-3 px-0 text-sm"
                        type="text"
                        :disabled="links.disabled"
                        @click.stop="jumpHandler(links)"
                      >
                        {{ t(links.name) }}
                      </a-button>
                    </div>
                  </span>
                </div>
                <div class="text-[12px] text-[var(--color-text-4)]">
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
  /**
   * @description 系统管理-组织-服务集成
   */
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import ServiceList from './components/serviceList.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { openWindow } from '@/utils/index';
  import { hasAnyPermission } from '@/utils/permission';

  import type { SkipTitle, StepListType } from '@/models/setting/serviceIntegration';
  import { SettingRouteEnum } from '@/enums/routeEnum';

  const isHasSystemPermission = computed(() => {
    return hasAnyPermission(['SYSTEM_PLUGIN:READ']);
  });

  const { t } = useI18n();
  const router = useRouter();
  const appStore = useAppStore();
  const cardContent = ref<StepListType[]>([
    {
      id: '1001',
      brightIcon: 'downloadplugin',
      darkIcon: 'downloadplugin-dark',
      title: 'organization.service.downloadPluginOrDev',
      skipTitle: [
        {
          name: 'organization.service.developmentDoc',
          src: 'https://github.com/metersphere/metersphere-platform-plugin/wiki/%E6%8F%92%E4%BB%B6%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97%E2%80%90V3',
          active: false,
          disabled: false,
        },
        {
          name: 'organization.service.downPlugin',
          src: 'https://github.com/metersphere/metersphere-platform-plugin',
          active: false,
          disabled: false,
        },
      ],
      step: '@/assets/images/ms_plugindownload.jpg',
      description: 'organization.service.description',
      bg: '@/assets/svg/service_01.svg',
    },
    {
      id: '1002',
      brightIcon: 'configplugin',
      darkIcon: 'configplugin-dark',
      title: 'organization.service.configPlugin',
      skipTitle: [
        {
          name: 'organization.service.jumpPlugin',
          src: '',
          active: true,
          disabled: !isHasSystemPermission.value,
        },
      ],
      step: '@/assets/images/ms_configplugin.jpg',
      description: 'organization.service.configDescription',
      bg: '@/assets/svg/service_02.svg',
    },
  ]);
  const isCollapse = ref<boolean>(false);
  const collapseHeight = ref<string>('56px');

  const changeHandler = (activeKey: (string | number)[]) => {
    isCollapse.value = activeKey.length > 0;
    collapseHeight.value = activeKey.length > 0 ? '152px' : '56px';
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

  function getItemIcon(item: StepListType) {
    const key = appStore.isDarkTheme ? 'darkIcon' : 'brightIcon';
    return item[key];
  }
</script>

<style scoped lang="less">
  :deep(.arco-icon-hover::before) {
    display: none;
  }
  :deep(.arco-scrollbar-container) {
    width: 100% !important;
  }
  :deep(.arco-collapse-item-header-right + .arco-collapse-item-content) {
    padding: 0;
  }
  .item {
    padding: 16px;
    width: calc(50% - 10px);
    height: 78px;
    border: 1px solid var(--color-text-n8);
    box-shadow: 0 0 7px rgb(120 56 135 / 10%);
    @apply flex items-center rounded-md;
  }
  .ms-item-0 {
    background: url('@/assets/svg/service_01.svg') no-repeat;
    background-position: center;
    background-size: cover;
  }
  .ms-item-1 {
    background: url('@/assets/svg/service_02.svg') no-repeat;
    background-position: center;
    background-size: cover;
  }
</style>
