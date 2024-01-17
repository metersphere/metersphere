<template>
  <div class="flex h-[62px] flex-row items-end gap-[8px] text-[var(--color-text-1)]">
    <div>
      <div class="mb-[8px]">{{ t('ms.assertion.statusCode') }}</div>
      <a-select v-model="selectValue" class="w-[157px]">
        <a-option v-for="item in statusCodeOptions" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-option>
      </a-select>
    </div>
    <a-input-number v-if="showInput" v-model:modelValue="statusCode" hide-button class="w-[157px]" />
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  import { statusCodeOptions } from '@/components/pure/ms-advance-filter/index';

  import { useI18n } from '@/hooks/useI18n';

  const selectValue = defineModel<string>('selectValue');
  const statusCode = defineModel<number>('statusCode');
  const showInput = computed(() => selectValue.value !== 'none' && selectValue.value !== '');

  const { t } = useI18n();
</script>

<style lang="less" scoped></style>
