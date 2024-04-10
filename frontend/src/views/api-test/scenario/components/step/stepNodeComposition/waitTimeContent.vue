<template>
  <div class="flex items-center gap-[4px]" draggable="false">
    <a-tooltip :content="innerData.delay?.toString()" :disabled="!innerData.delay">
      <a-input-number
        v-model:model-value="innerData.delay"
        class="max-w-[500px] px-[8px]"
        size="mini"
        :step="1000"
        :min="1"
        :max="600000"
        :precision="0"
        model-event="input"
        :disabled="props.disabled"
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
    disabled: boolean;
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
    if (!innerData.value.delay) {
      innerData.value.delay = 1000;
    }
    nextTick(() => {
      emit('change', innerData.value);
    });
  }
</script>

<style lang="less" scoped></style>
