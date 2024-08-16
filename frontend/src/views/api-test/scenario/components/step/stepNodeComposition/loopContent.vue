<template>
  <div class="flex items-center gap-[4px]" draggable="false">
    <a-input-group>
      <a-select
        v-model:model-value="innerData.loopType"
        :options="loopOptions"
        size="mini"
        class="w-[85px] px-[8px]"
        :disabled="props.disabled"
        @change="handleInputChange"
      />
      <a-tooltip
        v-if="innerData.loopType === ScenarioStepLoopTypeEnum.LOOP_COUNT"
        :content="innerData.msCountController.loops?.toString()"
        :disabled="!innerData.msCountController.loops"
      >
        <a-input
          v-model:model-value="innerData.msCountController.loops"
          class="w-[80px] px-[8px]"
          size="mini"
          hide-button
          model-event="input"
          :disabled="props.disabled"
          @blur="handleInputChange"
        >
          <template #prefix>
            <div class="text-[12px] text-[var(--color-text-4)]">{{ t('apiScenario.num') }}:</div>
          </template>
        </a-input>
      </a-tooltip>
    </a-input-group>
    <template v-if="innerData.loopType === ScenarioStepLoopTypeEnum.FOREACH">
      <a-tooltip :content="innerData.forEachController.variable" :disabled="!innerData.forEachController.variable">
        <a-input
          v-model:model-value="innerData.forEachController.variable"
          size="mini"
          class="w-[110px] px-[8px]"
          :max-length="255"
          :placeholder="t('apiScenario.variable')"
          :disabled="props.disabled"
          @change="handleInputChange"
        >
        </a-input>
      </a-tooltip>
      <div class="font-medium">in</div>
      <a-tooltip :content="innerData.forEachController.value" :disabled="!innerData.forEachController.value">
        <a-input
          v-model:model-value="innerData.forEachController.value"
          size="mini"
          class="w-[110px] px-[8px]"
          :placeholder="t('apiScenario.valuePrefix')"
          :max-length="255"
          :disabled="props.disabled"
          @change="handleInputChange"
        >
        </a-input>
      </a-tooltip>
    </template>
    <template v-else-if="innerData.loopType === ScenarioStepLoopTypeEnum.WHILE">
      <a-select
        v-model:model-value="innerData.whileController.conditionType"
        :options="whileOptions"
        size="mini"
        class="w-[75px] px-[8px]"
        :disabled="props.disabled"
        @change="handleInputChange"
      />
      <template v-if="innerData.whileController.conditionType === WhileConditionType.CONDITION">
        <a-tooltip
          :content="innerData.whileController.msWhileVariable.variable"
          :disabled="!innerData.whileController.msWhileVariable.variable"
        >
          <a-input
            v-model:model-value="innerData.whileController.msWhileVariable.variable"
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
          v-model:model-value="innerData.whileController.msWhileVariable.condition"
          size="mini"
          class="w-[90px] px-[8px]"
          :disabled="props.disabled"
          @change="handleInputChange"
        >
          <a-option v-for="opt of conditionOptions" :key="opt.value" :value="opt.value">
            {{ t(opt.label) }}
          </a-option>
        </a-select>
        <a-tooltip
          :content="innerData.whileController.msWhileVariable.value"
          :disabled="!innerData.whileController.msWhileVariable.value"
        >
          <a-input
            :id="stepId"
            v-model:model-value="innerData.whileController.msWhileVariable.value"
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
                @click.stop="handleQuickInput"
              />
            </template>
          </a-input>
        </a-tooltip>
      </template>
      <a-tooltip
        v-else
        :content="innerData.whileController.msWhileScript.scriptValue"
        :disabled="!innerData.whileController.msWhileScript.scriptValue"
      >
        <a-input
          :id="stepId"
          v-model:model-value="innerData.whileController.msWhileScript.scriptValue"
          size="mini"
          class="w-[200px] px-[8px]"
          :placeholder="t('apiScenario.expression')"
          :disabled="props.disabled"
          @change="handleInputChange"
        >
        </a-input>
      </a-tooltip>
      <a-tooltip
        :content="innerData.whileController.timeout?.toString()"
        :disabled="!innerData.whileController.timeout"
      >
        <a-input-number
          v-model:model-value="innerData.whileController.timeout"
          class="w-[120px] px-[8px]"
          size="mini"
          :step="100"
          :min="0"
          :max="600000"
          hide-button
          :precision="0"
          model-event="input"
          :disabled="props.disabled"
          @blur="handleInputChange"
        >
          <template #prefix>
            <div class="text-[12px] text-[var(--color-text-4)]">{{ t('apiScenario.timeout') }}:</div>
          </template>
        </a-input-number>
      </a-tooltip>
    </template>
    <a-tooltip
      v-if="innerData.loopType !== ScenarioStepLoopTypeEnum.WHILE"
      :content="innerData.forEachController.loopTime?.toString()"
      :disabled="!innerData.forEachController.loopTime"
    >
      <a-input-number
        v-if="innerData.loopType === ScenarioStepLoopTypeEnum.FOREACH"
        v-model:model-value="innerData.forEachController.loopTime"
        size="mini"
        :step="100"
        :min="0"
        :precision="0"
        hide-button
        :max="600000"
        class="w-[110px] px-[8px]"
        model-event="input"
        :disabled="props.disabled"
        @blur="handleInputChange"
      >
        <template #prefix>
          <div class="text-[12px] text-[var(--color-text-4)]">{{ t('apiScenario.space') }}:</div>
        </template>
      </a-input-number>
      <a-input-number
        v-else-if="innerData.loopType === ScenarioStepLoopTypeEnum.LOOP_COUNT"
        v-model:model-value="innerData.msCountController.loopTime"
        size="mini"
        :step="100"
        :min="0"
        :precision="0"
        :max="3600000"
        hide-button
        class="w-[110px] px-[8px]"
        model-event="input"
        :disabled="props.disabled"
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

  import { LoopStepDetail } from '@/models/apiTest/scenario';
  import { ScenarioStepLoopTypeEnum, WhileConditionType } from '@/enums/apiEnum';

  import { conditionOptions } from '@/views/api-test/scenario/components/config';

  const props = defineProps<{
    data: LoopStepDetail;
    stepId: string | number;
    disabled: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'change', innerData: LoopStepDetail): void;
    (e: 'quickInput', dataKey: string): void;
  }>();

  const { t } = useI18n();

  const innerData = ref(props.data);
  const loopOptions = [
    {
      value: ScenarioStepLoopTypeEnum.LOOP_COUNT,
      label: t('apiScenario.num'),
    },
    {
      value: ScenarioStepLoopTypeEnum.WHILE,
      label: 'while',
    },
    {
      value: ScenarioStepLoopTypeEnum.FOREACH,
      label: 'forEach',
    },
  ];
  const whileOptions = [
    {
      value: WhileConditionType.CONDITION,
      label: t('apiScenario.condition'),
    },
    {
      value: WhileConditionType.SCRIPT,
      label: t('apiScenario.expression'),
    },
  ];

  function handleQuickInput() {
    emit(
      'quickInput',
      innerData.value.whileController.conditionType === WhileConditionType.CONDITION
        ? 'msWhileVariableValue'
        : 'msWhileVariableScriptValue'
    );
  }

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
