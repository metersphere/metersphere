<template>
  <a-popover position="tl" :disabled="!props.desc || props.desc.trim() === ''" class="ms-params-input-popover">
    <template #content>
      <div class="param-popover-title">
        {{ t('apiTestDebug.desc') }}
      </div>
      <div class="param-popover-value">
        {{ props.desc }}
      </div>
    </template>
    <a-input
      ref="inputRef"
      v-model:model-value="innerValue"
      :disabled="props.disabled"
      :size="props.size"
      :placeholder="t('ms.paramsInput.commonPlaceholder')"
      class="ms-form-table-input"
      :trigger-props="{ contentClass: 'ms-form-table-input-trigger' }"
      @input="(val) => emit('input', val)"
      @change="(val) => emit('change', val)"
    />
  </a-popover>
</template>

<script setup lang="ts">
  import { useEventListener, useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    desc: string;
    disabled?: boolean;
    size?: 'small' | 'large' | 'medium' | 'mini';
  }>();
  const emit = defineEmits<{
    (e: 'update:desc', val: string): void;
    (e: 'input', val: string): void;
    (e: 'change', val: string): void;
    (e: 'dblclick'): void;
  }>();

  const { t } = useI18n();

  const innerValue = useVModel(props, 'desc', emit);

  const inputRef = ref<HTMLElement>();

  onMounted(() => {
    useEventListener(inputRef.value, 'dblclick', () => {
      emit('dblclick');
    });
  });
</script>

<style lang="less" scoped>
  .param-input:not(.arco-input-focus) {
    &:not(:hover) {
      @apply bg-transparent;

      border-color: transparent;
    }
  }
  .param-popover-title {
    @apply font-medium;

    margin-bottom: 4px;
    font-size: 12px;
    font-weight: 500;
    line-height: 16px;
    color: var(--color-text-1);
  }
  .param-popover-value {
    min-width: 100px;
    max-width: 280px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-1);
  }
</style>
