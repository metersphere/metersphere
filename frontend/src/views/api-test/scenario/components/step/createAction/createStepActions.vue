<template>
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
  import { TriggerPopupTranslate } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import { ScenarioStepItem } from '../stepTree.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { findNodeByKey, getGenerateId } from '@/utils';

  import { CreateStepAction } from '@/models/apiTest/scenario';
  import { ScenarioAddStepActionType, ScenarioStepType } from '@/enums/apiEnum';

  import useCreateActions from './useCreateActions';
  import { defaultStepItemCommon } from '@/views/api-test/scenario/components/config';
  import { DropdownPosition } from '@arco-design/web-vue/es/dropdown/interface';

  const props = defineProps<{
    position?: DropdownPosition;
    popupTranslate?: TriggerPopupTranslate;
    createStepAction?: CreateStepAction;
  }>();
  const emit = defineEmits<{
    (e: 'close');
    (
      e: 'otherCreate',
      type:
        | ScenarioAddStepActionType.IMPORT_SYSTEM_API
        | ScenarioAddStepActionType.CUSTOM_API
        | ScenarioAddStepActionType.SCRIPT_OPERATION,
      step?: ScenarioStepItem
    );
  }>();

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

  const { handleCreateStep } = useCreateActions();

  /**
   * 处理创建步骤操作
   * @param val 创建步骤类型
   */
  function handleCreateActionSelect(val: ScenarioAddStepActionType) {
    switch (val) {
      case ScenarioAddStepActionType.LOOP_CONTROL:
        if (step.value && props.createStepAction) {
          handleCreateStep(
            {
              type: ScenarioStepType.LOOP_CONTROL,
              name: t('apiScenario.loopControl'),
            } as ScenarioStepItem,
            step.value,
            steps.value,
            props.createStepAction,
            selectedKeys.value
          );
        } else {
          steps.value.push({
            ...cloneDeep(defaultStepItemCommon),
            stepId: getGenerateId(),
            order: steps.value.length + 1,
            type: ScenarioStepType.LOOP_CONTROL,
            name: t('apiScenario.loopControl'),
          });
        }
        break;
      case ScenarioAddStepActionType.CONDITION_CONTROL:
        if (step.value && props.createStepAction) {
          handleCreateStep(
            {
              type: ScenarioStepType.CONDITION_CONTROL,
              name: t('apiScenario.conditionControl'),
            } as ScenarioStepItem,
            step.value,
            steps.value,
            props.createStepAction,
            selectedKeys.value
          );
        } else {
          steps.value.push({
            ...cloneDeep(defaultStepItemCommon),
            stepId: getGenerateId(),
            order: steps.value.length + 1,
            type: ScenarioStepType.CONDITION_CONTROL,
            name: t('apiScenario.conditionControl'),
          });
        }
        break;
      case ScenarioAddStepActionType.ONLY_ONCE_CONTROL:
        if (step.value && props.createStepAction) {
          handleCreateStep(
            {
              type: ScenarioStepType.ONLY_ONCE_CONTROL,
              name: t('apiScenario.onlyOnceControl'),
            } as ScenarioStepItem,
            step.value,
            steps.value,
            props.createStepAction,
            selectedKeys.value
          );
        } else {
          steps.value.push({
            ...cloneDeep(defaultStepItemCommon),
            stepId: getGenerateId(),
            order: steps.value.length + 1,
            type: ScenarioStepType.ONLY_ONCE_CONTROL,
            name: t('apiScenario.onlyOnceControl'),
          });
        }
        break;
      case ScenarioAddStepActionType.WAIT_TIME:
        if (step.value && props.createStepAction) {
          handleCreateStep(
            {
              type: ScenarioStepType.WAIT_TIME,
              name: t('apiScenario.waitTime'),
            } as ScenarioStepItem,
            step.value,
            steps.value,
            props.createStepAction,
            selectedKeys.value
          );
        } else {
          steps.value.push({
            ...cloneDeep(defaultStepItemCommon),
            stepId: getGenerateId(),
            order: steps.value.length + 1,
            type: ScenarioStepType.WAIT_TIME,
            name: t('apiScenario.waitTime'),
          });
        }
        break;
      case ScenarioAddStepActionType.IMPORT_SYSTEM_API:
      case ScenarioAddStepActionType.CUSTOM_API:
      case ScenarioAddStepActionType.SCRIPT_OPERATION:
        if (step.value) {
          const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.value.stepId, 'stepId');
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
    window.open('https://zhuanlan.zhihu.com/p/597905464?utm_id=0', '_blank');
  }
</script>

<style lang="less" scoped></style>
