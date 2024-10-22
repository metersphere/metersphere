<template>
  <div class="ms-time-selector">
    <a-input-number
      v-model:model-value="numberValue"
      class="w-[120px]"
      :min="1"
      :max="1000"
      hide-button
      size="small"
      :disabled="props.disabled"
      @press-enter="changeNumber"
      @blur="changeNumber"
    >
      <template #suffix>
        <a-select
          v-model:model-value="typeValue"
          size="small"
          class="max-w-[64px]"
          :disabled="props.disabled"
          :options="option"
          :trigger-props="{ autoFitPopupMinWidth: true }"
          @change="(val) => changeType(val as string)"
        />
      </template>
    </a-input-number>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    defaultValue?: string;
    disabled?: boolean;
    allowEmpty?: boolean; // 允许为空
  }>();
  const emit = defineEmits<{
    (e: 'change', value: string): void;
  }>();

  const modelValue = defineModel<string>('modelValue', {
    default: '',
  });

  function parseValue(v?: string) {
    // 使用正则表达式匹配输入字符串，提取类型和值
    if (!v) {
      return { type: 'H', value: props.allowEmpty ? '' : 0 };
    }
    const match = v.match(/^(\d+(\.\d+)?)([MYHD])$/);
    if (match) {
      const value = parseInt(match[1], 10); // 提取值并将其转换为整数
      const type = match[3]; // 提取类型
      return { type, value };
    }
    // 如果输入字符串不匹配格式，可以抛出错误或返回一个默认值
    return { type: 'H', value: props.allowEmpty ? '' : 0 };
  }
  const numberValue = ref();
  const typeValue = ref('H');

  function initNumberAndType() {
    const { value, type } = parseValue(modelValue.value);
    numberValue.value = value;
    typeValue.value = type;
  }

  function changeNumber() {
    const result =
      numberValue.value === undefined ? props.defaultValue || '' : `${numberValue.value}${typeValue.value}`;
    modelValue.value = result;
    nextTick(() => {
      initNumberAndType();
      emit('change', modelValue.value);
    });
  }

  const option = [
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
  ];

  function changeType(val: string) {
    const result = numberValue.value === undefined ? props.defaultValue || '' : `${numberValue.value}${val}`;
    modelValue.value = result;
    nextTick(() => {
      initNumberAndType();
    });
  }

  watch(
    () => modelValue.value,
    (val) => {
      if (!props.allowEmpty) {
        numberValue.value = 0;
        initNumberAndType();
      } else if (val && Number(val) !== 0) {
        initNumberAndType();
      }
    },
    {
      immediate: true,
    }
  );
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
