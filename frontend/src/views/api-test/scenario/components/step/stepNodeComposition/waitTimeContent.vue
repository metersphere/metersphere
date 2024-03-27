<template>
  <div class="flex items-center gap-[4px]" draggable="false">
    <a-tooltip :content="innerData.delay?.toString()" :disabled="!innerData.delay">
      <a-input-number
        v-model:model-value="innerData.delay"
        class="max-w-[500px] px-[8px]"
        size="mini"
        :step="1"
        :min="0"
        hide-button
        :precision="0"
        model-event="input"
        @blur="handleInputChange"
      >
        <template #prefix>
          <div class="text-[12px] text-[var(--color-text-4)]">{{ t('apiScenario.waitTimeMs') }}:</div>
        </template>
      </a-input-number>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  export interface WaitTimeContentProps {
    id: string | number;
    delay: number;
  }

  const props = defineProps<{
    data: WaitTimeContentProps;
  }>();
  const emit = defineEmits<{
    (e: 'change', innerData: WaitTimeContentProps): void;
  }>();

  const { t } = useI18n();

  const innerData = ref(props.data);

  watchEffect(() => {
    innerData.value = props.data;
  });

  function handleInputChange() {
    nextTick(() => {
      emit('change', innerData.value);
    });
  }
</script>

<style lang="less" scoped></style>
