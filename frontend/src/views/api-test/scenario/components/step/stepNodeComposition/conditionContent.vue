<template>
  <div class="flex items-center gap-[4px]" draggable="false">
    <a-tooltip :content="innerData.variableName" :disabled="!innerData.variableName">
      <a-input
        v-model:model-value="innerData.variableName"
        size="mini"
        class="w-[100px] px-[8px]"
        :max-length="255"
        :placeholder="t('apiScenario.variableName', { suffix: '${var}' })"
        @change="handleInputChange"
      >
      </a-input>
    </a-tooltip>
    <a-select
      v-model:model-value="innerData.condition"
      size="mini"
      class="w-[90px] px-[8px]"
      @change="handleInputChange"
    >
      <a-option v-for="opt of conditionOptions" :key="opt.value" :value="opt.value">
        {{ t(opt.label) }}
      </a-option>
    </a-select>
    <a-tooltip :content="innerData.variableVal" :disabled="!innerData.variableVal">
      <a-input
        :id="innerData.stepId"
        v-model:model-value="innerData.variableVal"
        size="mini"
        class="w-[110px] px-[8px]"
        :placeholder="t('apiScenario.variableVal')"
        @change="handleInputChange"
      >
      </a-input>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { conditionOptions } from '@/views/api-test/scenario/components/config';

  export interface ConditionContentProps {
    stepId: string;
    variableName: string;
    condition: string;
    variableVal: string;
  }

  const props = defineProps<{
    data: ConditionContentProps;
  }>();
  const emit = defineEmits<{
    (e: 'change', innerData: ConditionContentProps): void;
    (e: 'quickInput', dataKey: keyof ConditionContentProps): void;
  }>();

  const { t } = useI18n();

  const innerData = ref(props.data);

  watchEffect(() => {
    innerData.value = props.data;
  });

  // 接收全局双击时间戳
  const dbClick = inject<
    Ref<{
      e: MouseEvent | null;
      timeStamp: number;
    }>
  >('dbClick');

  watch(
    () => dbClick?.value.timeStamp,
    () => {
      // @ts-ignore
      if ((dbClick?.value.e?.target as Element).parentNode?.id.includes(innerData.value.stepId)) {
        emit('quickInput', 'variableVal');
      }
    }
  );

  function handleInputChange() {
    nextTick(() => {
      emit('change', innerData.value);
    });
  }
</script>

<style lang="less" scoped></style>
