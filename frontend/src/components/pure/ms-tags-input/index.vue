<template>
  <a-tooltip
    :content="allTagText"
    :disabled="props.noTooltip || (innerModelValue || []).length === 0"
    :mouse-enter-delay="300"
  >
    <div :class="`flex w-full items-center ${props.class}`">
      <a-input-tag
        v-model:model-value="innerModelValue"
        v-model:input-value="innerInputValue"
        :error="isError"
        :placeholder="t(props.placeholder || 'ms.tagsInput.tagsInputPlaceholder')"
        :allow-clear="props.allowClear"
        :retain-input-value="props.retainInputValue"
        :unique-value="props.uniqueValue"
        :max-tag-count="props.maxTagCount"
        :readonly="props.readonly"
        :class="props.inputClass"
        :size="props.size"
        :disabled="props.disabled"
        @press-enter="tagInputEnter"
        @blur="tagInputBlur"
        @change="(value) => emit('change', value)"
        @clear="emit('clear')"
        @click="emit('click')"
      >
        <template v-if="$slots.prefix" #prefix>
          <slot name="prefix"></slot>
        </template>
        <template v-if="$slots.tag" #tag="{ data }">
          <slot name="tag" :data="data"></slot>
        </template>
        <template v-if="$slots.suffix" #suffix>
          <slot name="suffix"></slot>
        </template>
      </a-input-tag>
    </div>
    <div
      v-if="isError && (!props.emptyPriorityHighest || innerModelValue.length)"
      class="ml-[1px] mr-[4px] flex justify-start text-[12px] text-[rgb(var(--danger-6))]"
    >
      {{ t('common.tagInputMaxLength', { number: props.maxLength }) }}
    </div>
  </a-tooltip>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { Message, TagData } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = withDefaults(
    defineProps<{
      modelValue: (string | number | TagData)[]; // arco组件 BUG：回车添加第一个标签时，会触发表单的校验且状态为空数据错误，所以需要在a-form-item上设置 :validate-trigger="['blur', 'input']" 避免这种错误情况。
      inputValue?: string;
      placeholder?: string;
      retainInputValue?: boolean;
      uniqueValue?: boolean;
      allowClear?: boolean;
      tagsDuplicateText?: string;
      maxTagCount?: number;
      maxLength?: number;
      readonly?: boolean;
      class?: string;
      inputClass?: string;
      size?: 'small' | 'large' | 'medium' | 'mini';
      disabled?: boolean;
      noTooltip?: boolean;
      inputValidator?: (value: string) => boolean;
      emptyPriorityHighest?: boolean; // 是否是空优先级最高
    }>(),
    {
      retainInputValue: true,
      uniqueValue: true,
      allowClear: true,
      maxLength: 64,
      class: '',
      inputClass: '',
      size: 'medium',
      noTooltip: false,
    }
  );
  const emit = defineEmits(['update:modelValue', 'update:inputValue', 'change', 'clear', 'blur', 'click']);

  const { t } = useI18n();

  const innerModelValue = ref(props.modelValue);
  const innerInputValue = defineModel<string>('inputValue', {
    default: '',
  });
  const tagsLength = ref((props.modelValue || []).length); // 记录每次回车或失去焦点前的tags长度，以判断是否有新的tag被添加，新标签添加时需要判断是否重复的标签

  const isError = computed(
    () =>
      innerInputValue.value.length > props.maxLength ||
      (innerModelValue.value || []).some((item) => item.toString().length > props.maxLength)
  );
  const allTagText = computed(() => {
    return (innerModelValue.value || []).join('、');
  });

  watch(
    () => props.modelValue,
    (val) => {
      innerModelValue.value = val;
      tagsLength.value = val.length;
    }
  );

  watch(
    () => innerModelValue.value,
    (val) => {
      if (val.length < tagsLength.value) {
        // 输入框内标签长度变化且比记录的 tagsLength 小，说明是删除标签，此时需要同步 tagsLength 与标签长度
        tagsLength.value = val.length;
      }
      emit('update:modelValue', val);
    }
  );

  function validateTagsCountEnter() {
    if (innerModelValue.value.length > 10) {
      innerModelValue.value.pop();
      Message.warning(t('common.tagCountMax'));
      return false;
    }
    return true;
  }

  function validateTagsCountBlur() {
    if (tagsLength.value >= 10) {
      Message.warning(t('common.tagCountMax'));
      return false;
    }
    return true;
  }

  function validateUniqueValue() {
    if (
      props.uniqueValue &&
      innerInputValue.value &&
      tagsLength.value === innerModelValue.value.length &&
      innerModelValue.value.includes(innerInputValue.value.trim())
    ) {
      // 当输入框值存在且记录的 tagsLength 与输入框内标签数量相等时，才进行重复校验判断，因为回车触发事件是在 innerModelValue 的值更新后，所以需要判断长度确保重复的标签不是刚增加的导致重复校验逻辑错误
      Message.warning(t(props.tagsDuplicateText || 'ms.tagsInput.tagsDuplicateText'));
      return false;
    }
    return true;
  }

  function tagInputBlur() {
    if (
      innerInputValue.value &&
      innerInputValue.value.trim() !== '' &&
      validateUniqueValue() &&
      validateTagsCountBlur() &&
      (innerInputValue.value || '').trim().length <= props.maxLength
    ) {
      if (props.inputValidator && !props.inputValidator(innerInputValue.value.trim())) {
        innerModelValue.value.splice(-1, 1);
        emit('update:modelValue', innerModelValue.value);
      } else {
        innerModelValue.value.push(innerInputValue.value.trim());
        innerInputValue.value = '';
        tagsLength.value += 1;
        emit('update:modelValue', innerModelValue.value);
        emit('change', innerModelValue.value);
      }
    }
    emit('blur');
  }

  function tagInputEnter() {
    if (
      validateTagsCountEnter() &&
      validateUniqueValue() &&
      innerInputValue.value &&
      innerInputValue.value.trim().length <= props.maxLength
    ) {
      if (props.inputValidator && !props.inputValidator(innerInputValue.value.trim())) {
        innerModelValue.value.splice(-1, 1);
        emit('update:modelValue', innerModelValue.value);
      } else {
        innerInputValue.value = '';
        tagsLength.value += 1;
      }
    } else {
      innerModelValue.value = innerModelValue.value.filter((item: any) => item.length <= props.maxLength);
      innerInputValue.value = '';
    }
  }
</script>

<style lang="less" scoped>
  // arco-input-tag-mirror会导致表格拖拽问题，故需要加上relative
  :deep(.arco-input-tag) {
    position: relative;
  }
</style>
