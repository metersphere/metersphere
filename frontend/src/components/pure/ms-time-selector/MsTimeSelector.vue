<template>
  <div class="ms-time-selector">
    <a-input-number
      v-model="current.value"
      class="w-[120px]"
      :min="0"
      :max="current.max"
      hide-button
      size="small"
      @blur="handleBlur"
    >
      <template #suffix>
        <a-select v-model="current.type" size="small" class="max-w-[64px]" :options="option"> </a-select>
      </template>
    </a-input-number>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{ modelValue: string }>();
  const emit = defineEmits<{
    (e: 'update:modelValue', value: string): void;
    (e: 'change', value: string): void;
  }>();

  function parseValue(v: string) {
    // 使用正则表达式匹配输入字符串，提取类型和值
    const match = v.match(/^(\d+)([MYHD])$/);
    if (match) {
      const value = parseInt(match[1], 10); // 提取值并将其转换为整数
      const type = match[2]; // 提取类型
      let max = 0;
      // 根据类型设置最大值
      switch (type) {
        case 'H':
          max = 24;
          break;
        case 'M':
          max = 12;
          break;
        default:
          max = Number.MAX_VALUE;
      }

      return { type, value, max };
    }
    // 如果输入字符串不匹配格式，可以抛出错误或返回一个默认值
    return { type: 'H', value: undefined, max: Number.MAX_VALUE };
  }
  const current = reactive(parseValue(props.modelValue));

  const handleBlur = () => {
    const result = current.value ? `${current.value}${current.type}` : '';
    emit('update:modelValue', current.value ? `${current.value}${current.type}` : '');
    emit('change', result);
  };
  const option = computed(() => [
    {
      label: t('msTimeSelector.hour'),
      value: 'H',
    },
    {
      label: t('msTimeSelector.day'),
      value: 'D',
    },
    {
      label: t('msTimeSelector.month'),
      value: 'M',
    },
    {
      label: t('msTimeSelector.year'),
      value: 'Y',
    },
  ]);
</script>

<style lang="less" scoped>
  .ms-time-selector {
    display: inline;
    :deep(.arco-input-wrapper) {
      padding-right: 0;
      :focus-within {
        border-color: rgb(var(--primary-7));
        border-top: none;
        border-bottom: none;
        border-left: none;
      }
      .arco-input.arco-input-size-small {
        padding: 0;
      }
    }
    :deep(.arco-input-wrapper:not(:disabled):hover) {
      border-color: rgb(var(--primary-7));
      background-color: var(--color-text-n10);
    }
    :deep(.arco-select) {
      border-top: 1px solid var(--color-text-n7);
      border-right: 1px solid var(--color-text-n7);
      border-bottom: 1px solid var(--color-text-n7);
      border-radius: 0 4px 4px 0;
      background: var(--color-text-n8);
    }
    :deep(.arco-select-focused) {
      border-color: rgb(var(--primary-7));
    }
    :deep(.arco-select-view-single) {
      padding: 5px 8px;
      .arco-select-view-value {
        padding-top: 0;
        padding-bottom: 0;
        height: 22px;
        min-height: 22px;
        line-height: 22px;
      }
    }
  }
</style>
