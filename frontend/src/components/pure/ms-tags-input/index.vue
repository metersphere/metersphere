<template>
  <a-input-tag
    v-model:model-value="innerModelValue"
    v-model:input-value="innerInputValue"
    :placeholder="t(props.placeholder || '')"
    :allow-clear="props.allowClear"
    :retain-input-value="props.retainInputValue"
    :unique-value="props.uniqueValue"
    @press-enter="tagInputEnter"
    @blur="tagInputBlur"
  >
    <template v-if="props.customPrefix" #prefix>
      <slot name="prefix"></slot>
    </template>
    <template v-if="props.customTag" #tag="{ data }">
      <slot name="tag" :data="data"></slot>
    </template>
    <template v-if="props.customSuffix" #suffix>
      <slot name="suffix"></slot>
    </template>
  </a-input-tag>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';

  const props = withDefaults(
    defineProps<{
      modelValue: string[]; // arco组件 BUG：回车添加第一个标签时，会触发表单的校验且状态为空数据错误，所以需要在a-form-item上设置 :validate-trigger="['blur', 'input']" 避免这种错误情况。
      inputValue?: string;
      placeholder?: string;
      retainInputValue?: boolean;
      uniqueValue?: boolean;
      allowClear?: boolean;
      tagsDuplicateText?: string;
      customPrefix?: boolean;
      customTag?: boolean;
      customSuffix?: boolean;
    }>(),
    {
      retainInputValue: true,
      uniqueValue: true,
      allowClear: true,
    }
  );
  const emit = defineEmits(['update:modelValue', 'update:inputValue']);

  const { t } = useI18n();

  const innerModelValue = ref(props.modelValue);
  const innerInputValue = ref(props.inputValue);
  const tagsLength = ref(0); // 记录每次回车或失去焦点前的tags长度，以判断是否有新的tag被添加，新标签添加时需要判断是否重复的标签

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

  watch(
    () => props.inputValue,
    (val) => {
      innerInputValue.value = val;
    }
  );

  watch(
    () => innerInputValue.value,
    (val) => {
      emit('update:inputValue', val);
    }
  );

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
    if (innerInputValue.value && innerInputValue.value.trim() !== '' && validateUniqueValue()) {
      innerModelValue.value.push(innerInputValue.value.trim());
      innerInputValue.value = '';
      tagsLength.value += 1;
    }
  }

  function tagInputEnter() {
    if (validateUniqueValue()) {
      innerInputValue.value = '';
      tagsLength.value += 1;
    }
  }
</script>

<style lang="less" scoped></style>
