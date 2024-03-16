<template>
  <a-dropdown class="scenario-action-dropdown" @select="(val) => emit('select', val as ScenarioAddStepActionType)">
    <slot></slot>
    <template #content>
      <a-dgroup :title="t('apiScenario.requestScenario')">
        <a-doption :value="ScenarioAddStepActionType.IMPORT_SYSTEM_API">
          {{ t('apiScenario.importSystemApi') }}
        </a-doption>
        <a-doption :value="ScenarioAddStepActionType.CUSTOM_API">
          {{ t('apiScenario.customApi') }}
        </a-doption>
      </a-dgroup>
      <a-dgroup :title="t('apiScenario.logicControl')">
        <a-doption :value="ScenarioAddStepActionType.LOOP_CONTROL">
          <div class="flex w-full items-center justify-between">
            {{ t('apiScenario.loopControl') }}
            <MsButton type="text" @click="openTutorial">{{ t('apiScenario.tutorial') }}</MsButton>
          </div>
        </a-doption>
        <a-doption :value="ScenarioAddStepActionType.CONDITION_CONTROL">
          {{ t('apiScenario.conditionControl') }}
        </a-doption>
        <a-doption :value="ScenarioAddStepActionType.ONLY_ONCE_CONTROL">
          {{ t('apiScenario.onlyOnceControl') }}
        </a-doption>
      </a-dgroup>
      <a-dgroup :title="t('apiScenario.other')">
        <a-doption :value="ScenarioAddStepActionType.SCRIPT_OPERATION">
          {{ t('apiScenario.scriptOperation') }}
        </a-doption>
        <a-doption :value="ScenarioAddStepActionType.WAIT_TIME">{{ t('apiScenario.waitTime') }}</a-doption>
      </a-dgroup>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioAddStepActionType } from '@/enums/apiEnum';

  const emit = defineEmits<{
    (e: 'select', val: ScenarioAddStepActionType): void;
  }>();

  const { t } = useI18n();

  function openTutorial() {
    window.open('https://zhuanlan.zhihu.com/p/597905464?utm_id=0', '_blank');
  }
</script>

<style lang="less" scoped></style>
