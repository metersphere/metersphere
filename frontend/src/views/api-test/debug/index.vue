<template>
  <MsCard simple no-content-padding>
    <MsSplitBox :size="0.25" :max="0.5">
      <template #first>
        <div class="border-b border-[var(--color-text-n8)] p-[24px_24px_16px_24px]">
          <a-button type="primary" class="mr-[12px]">{{ t('ms.apiTestDebug.createDebug') }}</a-button>
          <a-button type="outline">{{ t('common.import') }}</a-button>
        </div>
        <div class="px-[24px] py-[16px]">
          <moduleTree />
        </div>
      </template>
      <template #second>
        <div class="flex h-full flex-col">
          <debug />
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
</template>

<script lang="ts" setup>
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import debug from './components/debug/index.vue';
  import moduleTree from './components/moduleTree.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { registerCatchSaveShortcut, removeCatchSaveShortcut } from '@/utils/event';

  const { t } = useI18n();

  function saveDebug() {
    console.log('save');
  }

  onMounted(() => {
    registerCatchSaveShortcut(saveDebug);
  });

  onBeforeUnmount(() => {
    removeCatchSaveShortcut(saveDebug);
  });
</script>

<style lang="less" scoped></style>
