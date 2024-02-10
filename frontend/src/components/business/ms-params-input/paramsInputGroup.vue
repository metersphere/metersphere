<template>
  <a-form-item v-for="(group, index) of props.inputGroup" :key="index" :label="t(group.label || '')" class="mb-[16px]">
    <a-select
      v-if="group.type === 'select'"
      v-model:model-value="innerForm[`${paramKey}${index + 1}`]"
      :options="group.options"
      :placeholder="t(group.placeholder || '')"
    />
    <a-input
      v-else-if="group.type === 'input'"
      v-model:model-value="innerForm[`${paramKey}${index + 1}`]"
      :placeholder="t(group.placeholder || '')"
      :max-length="255"
    />
    <a-radio-group
      v-else-if="group.type === 'radio'"
      v-model:model-value="innerForm[`${paramKey}${index + 1}`]"
      type="button"
    >
      <a-radio v-for="(option, i) of group.options || []" :key="`option${i}`" :value="option.value">
        {{ t(option.label || '') }}
      </a-radio>
    </a-radio-group>
    <a-date-picker
      v-else-if="group.type === 'date'"
      v-model:model-value="innerForm[`${paramKey}${index + 1}`]"
      :placeholder="t(group.placeholder || '')"
    />
    <a-input-number
      v-else-if="group.type === 'number'"
      v-model:model-value="innerForm[`${paramKey}${index + 1}`]"
      :placeholder="t(group.placeholder || '')"
      model-event="input"
    />
    <a-input
      v-else-if="group.type === 'inputAppendSelect'"
      v-model:model-value="innerForm[`${paramKey}${index + 1}`]"
      :placeholder="t(group.placeholder || '')"
      class="ms-params-input-inputAppendSelect"
      :max-length="255"
    >
      <template #prepend>
        <a-select
          v-model:model-value="innerForm[`${paramKey}${index + 1}`]"
          :options="group.options"
          class="select-input-prepend !w-[70px]"
        />
      </template>
    </a-input>
  </a-form-item>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  import type { MockParamInputGroupItem } from './types';

  const props = withDefaults(
    defineProps<{
      paramForm: Record<string, any>;
      type?: 'var' | 'func';
      inputGroup: MockParamInputGroupItem[];
    }>(),
    {
      type: 'var',
    }
  );
  const emit = defineEmits<{
    (e: 'update:paramForm', value: Record<string, any>): void;
  }>();

  const { t } = useI18n();
  const innerForm = useVModel(props, 'paramForm', emit);

  const paramKey = computed(() => {
    return props.type === 'var' ? 'param' : 'funcParam';
  });

  watch(
    () => props.inputGroup,
    (arr) => {
      arr.forEach((e, i) => {
        if (innerForm.value[`${paramKey.value}${i + 1}`] === '') {
          innerForm.value[`${paramKey.value}${i + 1}`] = e.value;
        }
      });
    },
    {
      immediate: true,
    }
  );
</script>

<style lang="less" scoped>
  .ms-input-group--prepend();
</style>
