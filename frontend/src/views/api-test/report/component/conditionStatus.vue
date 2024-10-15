<template>
  <div class="condition-status" :style="getClass"> {{ t(scenarioStepMap[props.status].label) }} </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioStepType } from '@/enums/apiEnum';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
  }>();
  // 场景步骤类型映射
  const scenarioStepMap: Record<string, any> = {
    [ScenarioStepType.LOOP_CONTROLLER]: { label: 'apiScenario.loopControl', color: 'rgba(167, 98, 191, 1)' },
    [ScenarioStepType.IF_CONTROLLER]: { label: 'apiScenario.conditionControl', color: 'rgba(238, 80, 163, 1)' },
    [ScenarioStepType.ONCE_ONLY_CONTROLLER]: { label: 'apiScenario.onlyOnceControl', color: 'rgba(211, 68, 0, 1)' },
    [ScenarioStepType.SCRIPT]: { label: 'apiScenario.scriptOperation', color: 'rgba(20, 225, 198, 1)' },
    [ScenarioStepType.API_CASE]: { label: 'report.detail.api.apiCase', color: 'rgb(var(--link-4))' },
    [ScenarioStepType.CUSTOM_REQUEST]: { label: 'report.detail.api.customRequest', color: 'rgb(var(--link-4))' },
    [ScenarioStepType.API]: { label: 'report.detail.api', color: 'rgb(var(--link-4))' },
    [ScenarioStepType.API_SCENARIO]: { label: 'report.detail.api_scenario', color: 'rgba(167, 98, 191, 1)' },
    [ScenarioStepType.CONSTANT_TIMER]: { label: 'apiScenario.waitTime', color: 'rgb(var(--warning-6))' },
  };

  const getClass = computed(() => {
    if (props.status) {
      return {
        color: scenarioStepMap[props.status]?.color,
        border: `1px solid ${scenarioStepMap[props.status]?.color}`,
      };
    }
  });
</script>

<style scoped lang="less">
  .condition-status {
    padding: 0 2px;
    font-size: 12px;
    line-height: 16px;
    border-radius: 0 12px 12px 0; /* 设置左半边为正常边框 */
  }
</style>
