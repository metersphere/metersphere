<template>
  <div class="flex flex-col gap-[8px]">
    <div class="flex flex-row items-center gap-[8px]">
      <span class="text-[var(--color-text-1)]">{{ t('ms.assertion.responseTime') }}</span>
      <span class="text-[var(--color-text-4)]">(ms)</span>
    </div>
    <a-input-number v-model:model-value="value" :step="100" mode="button" />
  </div>
</template>

<script setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const value = defineModel('modelValue', {
    default: 1000,
    type: Number,
  });
</script>

<style lang="less" scoped></style>
