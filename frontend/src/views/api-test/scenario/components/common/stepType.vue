<template>
  <div
    class="text-nowrap rounded-[0_999px_999px_0] border border-solid px-[8px] py-[2px] text-[12px] leading-[16px]"
    :style="{
      borderColor: type.color,
      color: type.color,
    }"
  >
    {{ type.label }}
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioStepType } from '@/enums/apiEnum';

  const props = defineProps<{
    type: ScenarioStepType;
  }>();

  const { t } = useI18n();

  // 场景步骤类型映射
  const scenarioStepMap = {
    [ScenarioStepType.QUOTE_API]: { label: 'apiScenario.quoteApi', color: 'rgb(var(--link-7))' },
    [ScenarioStepType.COPY_API]: { label: 'apiScenario.copyApi', color: 'rgb(var(--link-7))' },
    [ScenarioStepType.QUOTE_CASE]: { label: 'apiScenario.quoteCase', color: 'rgb(var(--success-7))' },
    [ScenarioStepType.COPY_CASE]: { label: 'apiScenario.copyCase', color: 'rgb(var(--success-7))' },
    [ScenarioStepType.QUOTE_SCENARIO]: { label: 'apiScenario.quoteScenario', color: 'rgb(var(--primary-7))' },
    [ScenarioStepType.COPY_SCENARIO]: { label: 'apiScenario.copyScenario', color: 'rgb(var(--primary-7))' },
    [ScenarioStepType.WAIT_TIME]: { label: 'apiScenario.waitTime', color: 'rgb(var(--warning-6))' },
    [ScenarioStepType.LOOP_CONTROL]: { label: 'apiScenario.loopControl', color: 'rgba(167, 98, 191, 1)' },
    [ScenarioStepType.CONDITION_CONTROL]: { label: 'apiScenario.conditionControl', color: 'rgba(238, 80, 163, 1)' },
    [ScenarioStepType.ONLY_ONCE_CONTROL]: { label: 'apiScenario.onlyOnceControl', color: 'rgba(211, 68, 0, 1)' },
    [ScenarioStepType.SCRIPT_OPERATION]: { label: 'apiScenario.scriptOperation', color: 'rgba(20, 225, 198, 1)' },
    [ScenarioStepType.CUSTOM_API]: { label: 'apiScenario.customApi', color: 'rgb(var(--link-4))' },
  };

  const type = computed(() => {
    const config = scenarioStepMap[props.type];
    return {
      border: `1px solid ${config?.color}`,
      color: config?.color,
      label: t(config?.label),
    };
  });
</script>

<style lang="less" scoped></style>
