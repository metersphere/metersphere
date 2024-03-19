<template>
  <div class="condition-status" :style="getClass"> {{ t(scenarioStepMap[props.status].label) }} </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioStepType } from '@/enums/apiEnum';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
  }>();
  // 场景步骤类型映射
  const scenarioStepMap = {
    [ScenarioStepType.QUOTE_API]: { label: 'apiScenario.quoteApi', color: 'rgb(var(--link-7))' },
    [ScenarioStepType.COPY_API]: { label: 'apiScenario.copyApi', color: 'rgb(var(--link-7))' },
    [ScenarioStepType.QUOTE_CASE]: { label: 'apiScenario.quoteCase', color: 'rgb(var(--success-7))' },
    [ScenarioStepType.COPY_CASE]: { label: 'apiScenario.copyCase', color: 'rgb(var(--success-7))' },
    [ScenarioStepType.QUOTE_SCENARIO]: { label: 'apiScenario.quoteScenario', color: 'rgb(var(--primary-7))' },
    [ScenarioStepType.COPY_SCENARIO]: { label: 'apiScenario.copyScenario', color: 'rgb(var(--primary-7))' },
    [ScenarioStepType.WAIT_TIME]: { label: 'apiScenario.waitTime', color: 'rgb(var(--warning-6))' },
    [ScenarioStepType.LOOP_CONTROLLER]: { label: 'apiScenario.loopControl', color: 'rgba(167, 98, 191, 1)' },
    [ScenarioStepType.IF_CONTROLLER]: { label: 'apiScenario.conditionControl', color: 'rgba(238, 80, 163, 1)' },
    [ScenarioStepType.ONCE_ONLY_CONTROLLER]: { label: 'apiScenario.onlyOnceControl', color: 'rgba(211, 68, 0, 1)' },
    [ScenarioStepType.SCRIPT_OPERATION]: { label: 'apiScenario.scriptOperation', color: 'rgba(20, 225, 198, 1)' },
    [ScenarioStepType.CUSTOM_API]: { label: 'apiScenario.customApi', color: 'rgb(var(--link-4))' },
    [ScenarioStepType.API_CASE]: { label: 'report.detail.api.apiCase', color: 'rgb(var(--link-4))' },
    [ScenarioStepType.CUSTOM_REQUEST]: { label: 'report.detail.api.apiCase', color: 'rgb(var(--link-4))' },
  };

  const getClass = computed(() => {
    if (props.status) {
      return {
        color: scenarioStepMap[props.status].color,
        border: `1px solid ${scenarioStepMap[props.status].color}`,
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
