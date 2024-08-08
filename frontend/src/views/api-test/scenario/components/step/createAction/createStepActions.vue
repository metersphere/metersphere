<template>
  <div>
    <a-dropdown
      v-model:popup-visible="visible"
      :position="props.position || 'bottom'"
      :popup-translate="props.popupTranslate"
      class="scenario-action-dropdown"
      @select="(val) => handleCreateActionSelect(val as ScenarioAddStepActionType)"
    >
      <slot></slot>
      <template #content>
        <a-dgroup :title="t('apiScenario.requestScenario')">
          <a-doption
            v-permission="['PROJECT_API_SCENARIO:READ+IMPORT']"
            :value="ScenarioAddStepActionType.IMPORT_SYSTEM_API"
          >
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
  </div>
</template>

<script setup lang="ts">
  import { TriggerPopupTranslate } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { findNodeByKey } from '@/utils';

  import { CreateStepAction, ScenarioStepItem } from '@/models/apiTest/scenario';
  import { ScenarioAddStepActionType, ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';

  import useCreateActions from './useCreateActions';
  import { defaultStepItemCommon } from '@/views/api-test/scenario/components/config';
  import { DropdownPosition } from '@arco-design/web-vue/es/dropdown/interface';

  const props = defineProps<{
    position?: DropdownPosition;
    popupTranslate?: TriggerPopupTranslate;
    createStepAction?: CreateStepAction;
  }>();
  const emit = defineEmits<{
    (e: 'close'): void;
    (
      e: 'otherCreate',
      type:
        | ScenarioAddStepActionType.IMPORT_SYSTEM_API
        | ScenarioAddStepActionType.CUSTOM_API
        | ScenarioAddStepActionType.SCRIPT_OPERATION,
      step?: ScenarioStepItem
    ): void;
    (e: 'addDone', newStep: ScenarioStepItem): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    default: false,
  });
  const steps = defineModel<ScenarioStepItem[]>('steps', {
    required: true,
  });
  const selectedKeys = defineModel<(string | number)[]>('selectedKeys', {
    required: true,
  });
  const step = defineModel<ScenarioStepItem>('step', {
    default: undefined,
  });

  const { handleCreateStep, buildInsertStepInfos } = useCreateActions();

  /**
   * 处理创建步骤操作
   * @param val 创建步骤类型
   */
  function handleCreateActionSelect(val: ScenarioAddStepActionType) {
    switch (val) {
      case ScenarioAddStepActionType.LOOP_CONTROL:
        const defaultLoopStep = buildInsertStepInfos(
          [cloneDeep(defaultStepItemCommon)],
          ScenarioStepType.LOOP_CONTROLLER,
          ScenarioStepRefType.DIRECT,
          steps.value.length + 1,
          appStore.currentProjectId
        )[0];
        if (step.value && props.createStepAction) {
          handleCreateStep(defaultLoopStep, step.value, steps.value, props.createStepAction, selectedKeys.value);
        } else {
          steps.value.push(defaultLoopStep);
        }
        emit('addDone', defaultLoopStep);
        break;
      case ScenarioAddStepActionType.CONDITION_CONTROL:
        const defaultConditionStep = buildInsertStepInfos(
          [cloneDeep(defaultStepItemCommon)],
          ScenarioStepType.IF_CONTROLLER,
          ScenarioStepRefType.DIRECT,
          steps.value.length + 1,
          appStore.currentProjectId
        )[0];
        if (step.value && props.createStepAction) {
          handleCreateStep(defaultConditionStep, step.value, steps.value, props.createStepAction, selectedKeys.value);
        } else {
          steps.value.push(defaultConditionStep);
        }
        emit('addDone', defaultConditionStep);
        break;
      case ScenarioAddStepActionType.ONLY_ONCE_CONTROL:
        const defaultOnlyOnceStep = buildInsertStepInfos(
          [cloneDeep(defaultStepItemCommon)],
          ScenarioStepType.ONCE_ONLY_CONTROLLER,
          ScenarioStepRefType.DIRECT,
          steps.value.length + 1,
          appStore.currentProjectId
        )[0];
        if (step.value && props.createStepAction) {
          handleCreateStep(defaultOnlyOnceStep, step.value, steps.value, props.createStepAction, selectedKeys.value);
        } else {
          steps.value.push(defaultOnlyOnceStep);
        }
        emit('addDone', defaultOnlyOnceStep);
        break;
      case ScenarioAddStepActionType.WAIT_TIME:
        const defaultWaitTimeStep = buildInsertStepInfos(
          [cloneDeep(defaultStepItemCommon)],
          ScenarioStepType.CONSTANT_TIMER,
          ScenarioStepRefType.DIRECT,
          steps.value.length + 1,
          appStore.currentProjectId
        )[0];
        if (step.value && props.createStepAction) {
          handleCreateStep(defaultWaitTimeStep, step.value, steps.value, props.createStepAction, selectedKeys.value);
        } else {
          steps.value.push(defaultWaitTimeStep);
        }
        emit('addDone', defaultWaitTimeStep);
        break;
      case ScenarioAddStepActionType.IMPORT_SYSTEM_API:
      case ScenarioAddStepActionType.CUSTOM_API:
      case ScenarioAddStepActionType.SCRIPT_OPERATION:
        if (step.value) {
          const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.value.uniqueId, 'uniqueId');
          if (realStep) {
            emit('otherCreate', val, realStep as ScenarioStepItem);
          }
        } else {
          emit('otherCreate', val);
        }
        break;
      default:
        break;
    }
    if (step.value) {
      emit('close');
    }
  }

  function openTutorial() {
    window.open('https://kb.fit2cloud.com/?p=247', '_blank');
  }
</script>

<style lang="less" scoped></style>
