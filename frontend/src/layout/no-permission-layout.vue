<template>
  <DefaultLayout
    :logo="pageConfig.logoPlatform[0]?.url || defaultPlatformLogo"
    :name="pageConfig.platformName"
    class="overflow-hidden"
    hide-right
  >
    <template #page>
      <div class="page">
        <div class="content-wrapper">
          <div class="content">
            <div :class="`${appStore.isDarkTheme ? 'no-permission-svg-dark' : 'no-permission-svg-bright'}`"></div>
            <div class="title">
              <span>{{ props.isProject ? t('common.noProject') : t('common.noResource') }}</span>
            </div>
            <slot></slot>
          </div>
        </div>
      </div>
    </template>
  </DefaultLayout>
</template>

<script lang="ts" setup>
  import DefaultLayout from './default-layout.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const defaultPlatformLogo = `${import.meta.env.BASE_URL}images/MeterSphere-logo.svg`;
  const appStore = useAppStore();
  const pageConfig = ref({ ...appStore.pageConfig });

  const { t } = useI18n();
  const props = defineProps<{
    isProject?: boolean;
  }>();
</script>

<style lang="less" scoped>
  .page {
    @apply h-full;

    background-color: var(--color-text-fff);
    .content-wrapper {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100%;
      background-color: var(--color-text-fff);
      .content {
        .title {
          display: flex;
          justify-content: center;
          font-size: 16px;
          color: var(--color-text-1);
          .user {
            margin-left: 16px;
          }
        }
      }
    }
  }
</style>
