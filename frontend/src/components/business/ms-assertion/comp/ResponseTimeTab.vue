<template>
  <div class="flex flex-col gap-[8px]">
    <div class="flex flex-row items-center gap-[8px]">
      <span class="text-[var(--color-text-1)]">{{ t('ms.assertion.responseTime') }}</span>
      <span class="text-[var(--color-text-4)]">(ms)</span>
    </div>
    <a-input-number v-model:model-value="innerParams" :step="100" mode="button" />
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  interface ResponseTimeTabProps {
    responseTime: number;
  }
  const props = defineProps<{
    value: ResponseTimeTabProps;
  }>();

  const innerParams = ref(props.value.responseTime);
  const emit = defineEmits<{
    (e: 'change', val: ResponseTimeTabProps): void; // 数据发生变化
  }>();
  watchEffect(() => {
    emit('change', { responseTime: innerParams.value });
  });
</script>

<style lang="less" scoped></style>
