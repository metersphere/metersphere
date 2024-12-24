<template>
  <MsCard v-if="props.allScreen" simple :special-height="36">
    <div class="flex h-full w-full flex-col items-center justify-center">
      <div :class="`no-config-svg no-config-svg-${appStore.isDarkTheme ? 'dark' : 'bright'}`"></div>
      <div class="flex items-center">
        {{
          appStore.projectList.length
            ? t('workbench.homePage.workEmptyConfig')
            : t('workbench.homePage.workNoProjectTip')
        }}
        <MsButton
          v-if="appStore.projectList.length"
          type="text"
          class="ml-[8px] font-medium"
          @click="() => emit('config')"
        >
          {{ t('workbench.homePage.cardSetting') }}
        </MsButton>
      </div>
    </div>
  </MsCard>
  <div v-else-if="props.noResPermission || props.isDashboard" :class="`${props.height || 'h-full'} w-full`">
    <div class="no-card">
      <div :class="`${props.noResPermission ? noPermissionImg : noDataImg}`"></div>
      <div class="font-medium text-[var(--color-text-1)]">
        {{ props.noResPermission ? t('workbench.homePage.workNoProjectTip') : t('workbench.homePage.noCard') }}
      </div>
      <div v-if="!props.noResPermission" class="text-[var(--color-text-4)]">
        {{ t('workbench.homePage.noCardDesc') }}
      </div>
    </div>
  </div>
  <div v-else class="not-setting-data">
    {{ t(props.noPermissionText || 'workbench.homePage.noDataTemporarily') }}
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const props = defineProps<{
    allScreen?: boolean;
    isDashboard?: boolean; // 是否仪表盘
    noResPermission?: boolean; // 无资源权限
    height?: string;
    noPermissionText?: string;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const emit = defineEmits<{
    (e: 'config'): void;
  }>();

  const noDataImg = computed(() => {
    return appStore.isDarkTheme ? 'no-card-svg-dark' : 'no-card-svg-bright';
  });

  const noPermissionImg = computed(() => {
    return appStore.isDarkTheme ? 'no-permission-svg-dark' : 'no-permission-svg-bright';
  });
</script>

<style scoped lang="less">
  .not-setting-data {
    height: 76px;
    border: 1px solid var(--color-border-2);
    border-radius: 4px;
    color: var(--color-text-4);
    background-color: var(--color-text-fff);
    @apply flex w-full items-center justify-center;
  }
  .no-config-svg {
    margin: 0 auto 24px;
    width: 100px;
    height: 104px;
    background-size: cover;
  }
  .no-config-svg-bright {
    background: url('@/assets/svg/config-work.svg');
  }
  .no-config-svg-dark {
    background: url('@/assets/svg/config-work-dark.svg');
  }
  .no-card {
    @apply flex h-full w-full flex-col items-center justify-center gap-4;
    .no-card-svg-bright {
      margin: 0 auto;
      width: 160px;
      height: 90px;
      background: url('@/assets/svg/work-no-card.svg');
      background-size: cover;
    }
    .no-card-svg-dark {
      margin: 0 auto;
      width: 160px;
      height: 90px;
      background: url('@/assets/svg/work-no-card-dark.svg');
      background-size: cover;
    }
    .no-permission-svg-bright {
      margin: 0 auto;
      width: 160px;
      height: 90px;
      background: url('@/assets/svg/no_resource.svg');
      background-size: cover;
    }
    .no-permission-svg-dark {
      margin: 0 auto;
      width: 160px;
      height: 90px;
      background: url('@/assets/svg/no_resource-dark.svg');
      background-size: cover;
    }
  }
</style>
