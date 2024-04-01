<template>
  <div class="flex h-[62px] flex-row items-end gap-[8px] text-[var(--color-text-1)]">
    <div>
      <div class="mb-[8px]">{{ t('ms.assertion.statusCode') }}</div>
      <a-select v-model="condition.condition" :disabled="props.disabled" class="w-[157px]" @change="clearExpectedValue">
        <a-option v-for="item in codeOptions" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-option>
      </a-select>
    </div>
    <a-input
      v-if="showInput"
      v-model="condition.expectedValue"
      :disabled="
        props.disabled ||
        condition.condition === 'UNCHECK' ||
        condition.condition === 'NOT_EMPTY' ||
        condition.condition === 'EMPTY'
      "
      hide-button
      class="w-[157px]"
      @change="
        emit('change', {
          ...condition,
        })
      "
    />
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  import { codeOptions } from './utils';

  const { t } = useI18n();
  interface Param {
    id: string;
    name: string;
    assertionType: string;
    condition: string;
    expectedValue: string;
  }

  const props = defineProps<{
    data: Param;
    disabled?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'change', data: Param): void;
  }>();
  const condition = useVModel(props, 'data', emit);
  const showInput = computed(() => condition.value.condition !== 'none' && condition.value.condition !== '');
  function clearExpectedValue() {
    if (
      condition.value.condition === 'UNCHECK' ||
      condition.value.condition === 'NOT_EMPTY' ||
      condition.value.condition === 'EMPTY'
    ) {
      condition.value.expectedValue = '';
    }
    emit('change', {
      ...condition.value,
    });
  }
</script>

<style lang="less" scoped></style>
