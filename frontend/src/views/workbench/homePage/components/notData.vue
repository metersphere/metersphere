<template>
  <MsCard v-if="props.allScreen" class="mb-[16px]" simple>
    <div class="flex h-full w-full flex-col items-center justify-center">
      <div class="no-config-svg"></div>
      <div class="flex items-center">
        {{ t('workbench.homePage.workEmptyConfig') }}
        <MsButton type="text" class="ml-[8px] font-medium">
          {{ t('workbench.homePage.configureWorkbench') }}
        </MsButton>
      </div>
    </div>
  </MsCard>
  <div v-else class="not-setting-data">
    {{ t('workbench.homePage.noDataTemporarily') }}
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    allScreen?: boolean;
  }>();

  const { t } = useI18n();
</script>

<style scoped lang="less">
  .not-setting-data {
    height: 76px;
    border: 1px solid var(--color-border-2);
    border-radius: 4px;
    color: var(--color-text-4);
    @apply flex items-center justify-center bg-white;
  }
  .no-config-svg {
    margin: 0 auto 24px;
    width: 100px;
    height: 104px;
    background: url('@/assets/svg/config-work.svg');
    background-size: cover;
  }
</style>
