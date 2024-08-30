<template>
  <a-trigger
    unmount-on-close
    trigger="hover"
    :disabled="props.isPreview"
    :popup-translate="props.popupTranslate || [100, -100]"
  >
    <slot></slot>
    <template #content>
      <div class="arco-table-filters-content px-[8px] py-[4px]">{{ t('report.detail.systemInternalTooltip') }}</div>
    </template>
  </a-trigger>
</template>

<script setup lang="ts">
  import { TriggerPopupTranslate } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    isPreview?: boolean;
    popupTranslate?: TriggerPopupTranslate | undefined;
  }>();
</script>

<style scoped></style>
