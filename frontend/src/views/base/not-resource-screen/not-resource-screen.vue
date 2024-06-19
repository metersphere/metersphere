<template>
  <div class="absolute left-[50%] top-[50%] ml-[-95px] mt-[-121px] text-center">
    <div class="page">
      <div class="content-wrapper">
        <div class="content">
          <div class="no-resource-svg"></div>
          <div class="title">
            <span>{{ resourceType === 'DELETE' ? t('common.resourceDeleted') : t('common.resourceExpired') }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useRoute } from 'vue-router';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const route = useRoute();

  const resourceType = ref<string>(route.query.type as string);
</script>

<style lang="less">
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
        .no-resource-svg {
          margin: 0 auto 24px;
          width: 160px;
          height: 98px;
          background: url('@/assets/svg/no_resource.svg');
          background-size: cover;
        }
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
