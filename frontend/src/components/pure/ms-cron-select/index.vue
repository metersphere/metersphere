<template>
  <a-select
    v-model:model-value="cron"
    :disabled="props.disabled"
    :class="props.class"
    allow-create
    @change="emit('change', $event)"
  >
    <template #label="{ data }">
      <div class="flex items-center">
        {{ data.value }}
        <div class="ml-[4px] text-[var(--color-text-4)]">{{ data.label.split('?')[1] }}</div>
      </div>
    </template>
    <a-option v-for="item of syncFrequencyOptions" :key="item.value" :value="item.value" class="block">
      <div class="flex w-full items-center justify-between">
        {{ item.value }}
        <div class="ml-[4px] text-[var(--color-text-4)]">{{ item.label }}</div>
      </div>
    </a-option>
  </a-select>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    class?: string;
    disabled?: boolean;
  }>();
  const emit = defineEmits<{
    (
      e: 'change',
      value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
    ): void;
  }>();

  const { t } = useI18n();

  const cron = defineModel<string>('modelValue', {
    required: true,
  });

  const syncFrequencyOptions = [
    { label: t('ms.cron.select.timeTaskHour'), value: '0 0 0/1 * * ?' },
    { label: t('ms.cron.select.timeTaskSixHour'), value: '0 0 0/6 * * ?' },
    { label: t('ms.cron.select.timeTaskTwelveHour'), value: '0 0 0/12 * * ?' },
    { label: t('ms.cron.select.timeTaskDay'), value: '0 0 0 * * ?' },
  ];
</script>

<style lang="less" scoped></style>
