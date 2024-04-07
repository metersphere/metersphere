<template>
  <div class="single-logo-layout">
    <DefaultLayout
      :logo="pageConfig.logoPlatform[0]?.url || defaultPlatformLogo"
      :name="pageConfig.platformName"
      class="overflow-hidden"
      is-preview
    >
      <template #body>
        <div class="body">
          <div class="content-wrapper">
            <div class="content">
              <div class="icon">
                <div class="icon-svg">
                  <svg-icon width="232px" height="184px" name="no_resource" />
                </div>
                <div class="radius-box"></div>
              </div>
              <div class="title">
                <span>{{ props.isProject ? t('common.noProject') : t('common.noResource') }}</span>
              </div>
              <slot></slot>
            </div>
          </div>
        </div>
      </template>
    </DefaultLayout>
  </div>
</template>

<script setup lang="ts">
  import DefaultLayout from './default-layout.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const defaultPlatformLogo = `${import.meta.env.BASE_URL}images/MeterSphere-logo.svg`;
  const appStore = useAppStore();
  const pageConfig = ref({ ...appStore.pageConfig });

  const props = defineProps<{
    isProject?: boolean;
  }>();

  const { t } = useI18n();
</script>

<style lang="less" scoped>
  .single-logo-layout {
    @apply h-full;

    background-color: var(--color-text-n9);
    .body {
      margin-top: 56px;
      padding: 17px 14px 15px 18px;
      height: 100%;
      .content-wrapper {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100%;
        background-color: var(--color-text-fff);
        .content {
          .icon {
            position: relative;
            display: flex;
            align-items: center;
            margin-bottom: 40px;
            height: 218px;
            flex-direction: column;
            .icon-svg {
              z-index: 100;
            }
            .radius-box {
              position: relative;
              bottom: 83px;
              width: 355px;
              height: 117px;
              flex-shrink: 0;
              border-radius: 355px;
              background: linear-gradient(180deg, #ededf1 0%, rgb(255 255 255 / 0%) 100%);
            }
          }
          .title {
            font-size: 16px;
            color: var(--color-text-1);
            .user {
              margin-left: 16px;
            }
          }
        }
      }
    }
  }
</style>
