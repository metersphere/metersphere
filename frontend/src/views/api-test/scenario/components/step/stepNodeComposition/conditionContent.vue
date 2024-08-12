<template>
  <div class="flex items-center gap-[4px]" draggable="false">
    <a-tooltip :content="innerData.variable" :disabled="!innerData.variable">
      <a-input
        v-model:model-value="innerData.variable"
        size="mini"
        class="w-[100px] px-[8px]"
        :max-length="255"
        :placeholder="t('apiScenario.variable', { suffix: '${var}' })"
        :disabled="props.disabled"
        @change="handleInputChange"
      >
      </a-input>
    </a-tooltip>
    <a-select
      v-model:model-value="innerData.condition"
      size="mini"
      class="w-[90px] px-[8px]"
      :disabled="props.disabled"
      @change="handleInputChange"
    >
      <a-option v-for="opt of conditionOptions" :key="opt.value" :value="opt.value">
        {{ t(opt.label) }}
      </a-option>
    </a-select>
    <a-tooltip :content="innerData.value" :disabled="!innerData.value">
      <a-input
        :id="props.stepId"
        v-model:model-value="innerData.value"
        size="mini"
        class="value-input w-[110px] px-[8px]"
        :placeholder="t('apiScenario.value')"
        :disabled="props.disabled"
        @change="handleInputChange"
      >
        <template #suffix>
          <MsIcon
            type="icon-icon_full_screen_one"
            class="input-suffix-icon ml-[8px]"
            @click.stop="emit('quickInput', 'conditionValue')"
          />
        </template>
      </a-input>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ConditionStepDetail } from '@/models/apiTest/scenario';

  import { conditionOptions } from '@/views/api-test/scenario/components/config';

  const props = defineProps<{
    data: ConditionStepDetail;
    stepId: string | number;
    disabled: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'change', innerData: ConditionStepDetail): void;
    (e: 'quickInput', dataKey: string): void;
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
