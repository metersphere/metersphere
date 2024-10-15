<template>
  <div
    class="text-nowrap rounded-[0_999px_999px_0] border border-solid px-[8px] py-[2px] text-[12px] leading-[16px]"
    :style="{
      borderColor: type.color,
      color: type.color,
    }"
  >
    {{ t(type.label) }}
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioStepItem } from '@/models/apiTest/scenario';
  import { ScenarioStepType } from '@/enums/apiEnum';

  import getStepType from './utils';

  const props = defineProps<{
    step: ScenarioStepItem;
  }>();

  const { t } = useI18n();

  // 场景步骤类型映射
  const scenarioStepMap: Record<string, any> = {
    [ScenarioStepType.CONSTANT_TIMER]: { label: 'apiScenario.waitTime', color: 'rgb(var(--warning-6))' },
    [ScenarioStepType.LOOP_CONTROLLER]: { label: 'apiScenario.loopControl', color: 'rgba(167, 98, 191, 1)' },
    [ScenarioStepType.IF_CONTROLLER]: { label: 'apiScenario.conditionControl', color: 'rgba(238, 80, 163, 1)' },
    [ScenarioStepType.ONCE_ONLY_CONTROLLER]: { label: 'apiScenario.onlyOnceControl', color: 'rgba(211, 68, 0, 1)' },
    [ScenarioStepType.SCRIPT]: { label: 'apiScenario.scriptOperation', color: 'rgba(20, 225, 198, 1)' },
    [ScenarioStepType.CUSTOM_REQUEST]: { label: 'apiScenario.customApi', color: 'rgb(var(--link-4))' },
    [ScenarioStepType.JMETER_COMPONENT]: { label: 'apiScenario.jmeterComponent', color: 'rgba(211, 68, 0, 1)' },
  };

  const type = computed(() => {
    let config = scenarioStepMap[props.step.stepType];
    const stepType = getStepType(props.step);
    if (!config) {
      if (stepType.isQuoteApi) {
        config = { label: 'apiScenario.quoteApi', color: 'rgb(var(--link-7))' };
      } else if (stepType.isCopyApi) {
        config = { label: 'apiScenario.copyApi', color: 'rgb(var(--link-7))' };
      } else if (stepType.isQuoteCase) {
        config = { label: 'apiScenario.quoteCase', color: 'rgb(var(--success-7))' };
      } else if (stepType.isCopyCase) {
        config = { label: 'apiScenario.copyCase', color: 'rgb(var(--success-7))' };
      } else if (stepType.isQuoteScenario) {
        config = { label: 'apiScenario.quoteScenario', color: 'rgb(var(--primary-7))' };
      } else if (stepType.isCopyScenario) {
        config = { label: 'apiScenario.copyScenario', color: 'rgb(var(--primary-7))' };
      }
    }
    return {
      border: `1px solid ${config?.color}`,
      color: config?.color,
      label: t(config?.label),
    };
  });
</script>

<style lang="less" scoped></style>
