<template>
  <div class="flex items-center gap-[4px]" draggable="false">
    <a-input-group>
      <a-select
        v-model:model-value="innerData.loopType"
        :options="loopOptions"
        size="mini"
        class="w-[85px] px-[8px]"
        @change="handleInputChange"
      />
      <a-tooltip
        v-if="innerData.loopType === 'num'"
        :content="innerData.loopNum.toString()"
        :disabled="!innerData.loopNum"
      >
        <a-input-number
          v-model:model-value="innerData.loopNum"
          class="w-[80px] px-[8px]"
          size="mini"
          :step="1"
          :min="0"
          hide-button
          :precision="0"
          model-event="input"
          @blur="handleInputChange"
        >
          <template #prefix>
            <div class="text-[12px] text-[var(--color-text-4)]">{{ t('apiScenario.num') }}:</div>
          </template>
        </a-input-number>
      </a-tooltip>
    </a-input-group>
    <template v-if="innerData.loopType === 'forEach'">
      <a-tooltip :content="innerData.variableName" :disabled="!innerData.variableName">
        <a-input
          v-model:model-value="innerData.variableName"
          size="mini"
          class="w-[110px] px-[8px]"
          :max-length="255"
          :placeholder="t('apiScenario.variableName')"
          @change="handleInputChange"
        >
        </a-input>
      </a-tooltip>
      <div class="font-medium">in</div>
      <a-tooltip :content="innerData.variablePrefix" :disabled="!innerData.variablePrefix">
        <a-input
          v-model:model-value="innerData.variablePrefix"
          size="mini"
          class="w-[110px] px-[8px]"
          :placeholder="t('apiScenario.variablePrefix')"
          :max-length="255"
          @change="handleInputChange"
        >
        </a-input>
      </a-tooltip>
    </template>
    <template v-else-if="innerData.loopType === 'while'">
      <a-select
        v-model:model-value="innerData.loopWhileType"
        :options="whileOptions"
        size="mini"
        class="w-[75px] px-[8px]"
        @change="handleInputChange"
      />
      <template v-if="innerData.loopWhileType === 'condition'">
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
            :id="innerData.id"
            v-model:model-value="innerData.variableVal"
            size="mini"
            class="w-[110px] px-[8px]"
            :placeholder="t('apiScenario.variableVal')"
            @change="handleInputChange"
          >
          </a-input>
        </a-tooltip>
      </template>
      <a-tooltip v-else :content="innerData.expression" :disabled="!innerData.expression">
        <a-input
          :id="innerData.id"
          v-model:model-value="innerData.expression"
          size="mini"
          class="w-[200px] px-[8px]"
          :placeholder="t('apiScenario.expression')"
          @change="handleInputChange"
        >
        </a-input>
      </a-tooltip>
      <a-tooltip :content="innerData.overTime.toString()" :disabled="!innerData.overTime">
        <a-input-number
          v-model:model-value="innerData.overTime"
          class="w-[100px] px-[8px]"
          size="mini"
          :step="1"
          :min="0"
          hide-button
          :precision="0"
          model-event="input"
          @blur="handleInputChange"
        >
          <template #prefix>
            <div class="text-[12px] text-[var(--color-text-4)]">{{ t('apiScenario.overTime') }}:</div>
          </template>
        </a-input-number>
      </a-tooltip>
    </template>
    <a-tooltip
      v-if="innerData.loopType !== 'while'"
      :content="innerData.loopSpace.toString()"
      :disabled="!innerData.loopSpace"
    >
      <a-input-number
        v-model:model-value="innerData.loopSpace"
        size="mini"
        :step="1"
        :min="0"
        :precision="0"
        hide-button
        class="w-[110px] px-[8px]"
        model-event="input"
        @blur="handleInputChange"
      >
        <template #prefix>
          <div class="text-[12px] text-[var(--color-text-4)]">{{ t('apiScenario.space') }}:</div>
        </template>
      </a-input-number>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioStepLoopType, ScenarioStepLoopWhileType } from '@/models/apiTest/scenario';
  import { ScenarioStepType } from '@/enums/apiEnum';

  import { conditionOptions } from '@/views/api-test/scenario/components/config';

  export interface LoopContentProps {
    id: string | number;
    num: number;
    name: string;
    type: ScenarioStepType;
    loopNum: number;
    loopType: ScenarioStepLoopType;
    loopSpace: number;
    variableName: string;
    variablePrefix: string;
    loopWhileType: ScenarioStepLoopWhileType;
    variableVal: string;
    condition: string;
    overTime: number;
    expression: string;
  }

  const props = defineProps<{
    data: LoopContentProps;
  }>();
  const emit = defineEmits<{
    (e: 'change', innerData: LoopContentProps): void;
    (e: 'quickInput', dataKey: keyof LoopContentProps): void;
  }>();

  const { t } = useI18n();

  const innerData = ref(props.data);
  const loopOptions = [
    {
      value: 'num',
      label: t('apiScenario.num'),
    },
    {
      value: 'while',
      label: 'while',
    },
    {
      value: 'forEach',
      label: 'forEach',
    },
  ];
  const whileOptions = [
    {
      value: 'condition',
      label: t('apiScenario.condition'),
    },
    {
      value: 'expression',
      label: t('apiScenario.expression'),
    },
  ];

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
      if ((dbClick?.value.e?.target as Element).parentNode?.id.includes(innerData.value.id)) {
        emit('quickInput', innerData.value.loopWhileType === 'condition' ? 'variableVal' : 'expression');
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
