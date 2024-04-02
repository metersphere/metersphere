<template>
  <div class="flex flex-col gap-[8px]">
    <div class="flex flex-row items-center gap-[8px]">
      <span class="text-[var(--color-text-1)]">{{ t('ms.assertion.responseTime') }}</span>
      <span class="text-[var(--color-text-4)]">(ms)</span>
    </div>
    <a-input-number
      v-model="condition.expectedValue"
      :disabled="props.disabled"
      :step="100"
      :min="0"
      :precision="0"
      mode="button"
      @blur="
        emit('change', {
          ...condition,
        })
      "
    />
  </div>
</template>

<script lang="ts" setup>
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteAssertion } from '../type';

  const { t } = useI18n();

  const props = defineProps<{
    data: ExecuteAssertion;
    disabled?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'change', data: ExecuteAssertion): void;
  }>();

  const condition = useVModel(props, 'data', emit);
</script>

<style lang="less" scoped></style>
