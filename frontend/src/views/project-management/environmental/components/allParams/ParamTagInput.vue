<template>
  <a-popover position="tl" class="ms-params-input-popover">
    <template #content>
      <div class="param-popover-title">
        {{ t('project.environmental.tag') }}
      </div>
      <div class="param-popover-value">
        <MsTagsGroup is-string-tag :tag-list="props.modelValue" :show-num="1" class="param-input" />
      </div>
    </template>
    <MsTagsInput ref="inputRef" v-model:model-value="innerValue" :max-tag-count="1" class="param-input" />
  </a-popover>
</template>

<script setup lang="ts">
  import { useEventListener, useVModel } from '@vueuse/core';

  import MsTagsGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    modelValue: string[];
  }>();
  const emit = defineEmits<{
    (e: 'update:modelValue', val: string[]): void;
    (e: 'input', val: string): void;
    (e: 'change', val: string): void;
    (e: 'dblclick'): void;
  }>();

  const { t } = useI18n();

  const innerValue = useVModel(props, 'modelValue', emit);

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
  .param-popover-subtitle {
    margin-bottom: 2px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-4);
  }
  .param-popover-value {
    min-width: 100px;
    max-width: 280px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-1);
  }
</style>
