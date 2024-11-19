<template>
  <div class="flex w-full">
    <a-popover position="tl" :disabled="!modelValue || modelValue.trim() === ''" class="ms-params-input-popover">
      <template #content>
        <div v-if="props.title" class="ms-params-popover-title">
          {{ props.title }}
        </div>
        <div class="ms-params-popover-value">
          {{ props.popoverTitle || modelValue }}
        </div>
      </template>
      <a-input
        v-if="props.type === 'input'"
        ref="inputRef"
        v-model:model-value="modelValue"
        :class="props.class"
        :disabled="props.disabled"
        :size="props.size"
        :max-length="props.maxLength"
        :placeholder="props.placeholder"
        :trigger-props="{ contentClass: 'ms-form-table-input-trigger' }"
        :allow-clear="props.allowClear"
        @input="(val) => emit('input', val)"
        @change="(val) => emit('change', val)"
      />
      <a-textarea
        v-else
        ref="inputRef"
        v-model:model-value="modelValue"
        :class="props.class"
        :disabled="props.disabled"
        :size="props.size"
        :placeholder="props.placeholder"
        :max-length="props.maxLength"
        :auto-size="{ minRows: 1, maxRows: 1 }"
        :allow-clear="props.allowClear"
        @input="(val) => emit('input', val)"
        @change="(val) => emit('change', val)"
      />
    </a-popover>
  </div>
  <a-modal
    v-model:visible="showQuickInput"
    :title="props.title"
    :ok-text="t('common.save')"
    :ok-button-props="{ disabled: !quickInputValue || quickInputValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="480"
    title-align="start"
    @ok="applyQuickInputDesc"
    @close="clearQuickInputDesc"
  >
    <a-textarea
      v-model:model-value="quickInputValue"
      :placeholder="props.placeholder"
      :auto-size="{ minRows: 2 }"
      :max-length="props.maxLength"
    />
  </a-modal>
</template>

<script setup lang="ts">
  import { useEventListener } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  const props = withDefaults(
    defineProps<{
      type?: 'input' | 'textarea';
      title?: string;
      popoverTitle?: string;
      placeholder?: string;
      disabled?: boolean;
      size?: 'small' | 'large' | 'medium' | 'mini';
      maxLength?: number;
      class?: string;
      allowClear?: boolean;
    }>(),
    {
      type: 'input',
      title: '',
      disabled: false,
      size: 'medium',
    }
  );
  const emit = defineEmits<{
    (e: 'input', val: string): void;
    (e: 'change', val: string): void;
  }>();

  const { t } = useI18n();

  const modelValue = defineModel<string>('modelValue', {
    default: '',
  });

  const inputRef = ref<HTMLElement>();
  const showQuickInput = ref(false);
  const quickInputValue = ref('');

  function quickInputDesc() {
    showQuickInput.value = true;
    quickInputValue.value = modelValue.value;
  }

  function clearQuickInputDesc() {
    quickInputValue.value = '';
  }

  function applyQuickInputDesc() {
    modelValue.value = quickInputValue.value;
    emit('change', quickInputValue.value);
    clearQuickInputDesc();
    showQuickInput.value = false;
  }

  onMounted(() => {
    useEventListener(inputRef.value, 'dblclick', () => {
      quickInputDesc();
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
</style>
