<template>
  <a-select
    v-model:model-value="method"
    :placeholder="t('common.pleaseSelect')"
    :disabled="props.disabled"
    @change="(val) => emit('change', val as string)"
  >
    <template #label="{ data }">
      <apiMethodName :method="data.value" class="inline-block" />
    </template>
    <a-option v-for="item of RequestMethods" :key="item" :value="item">
      <apiMethodName :method="item" />
    </a-option>
  </a-select>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestMethods } from '@/enums/apiEnum';

  const props = defineProps<{
    modelValue: string;
    disabled?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'update:modelValue', value: string): void;
    (e: 'change', value: string): void;
  }>();

  const { t } = useI18n();

  const method = useVModel(props, 'modelValue', emit);
</script>

<style lang="less" scoped></style>
