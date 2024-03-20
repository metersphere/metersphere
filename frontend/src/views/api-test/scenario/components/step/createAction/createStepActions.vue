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
  import { findNodeByKey, getGenerateId, insertNode, TreeNode } from '@/utils';

  import { CreateStepAction } from '@/models/apiTest/scenario';
  import { ScenarioAddStepActionType, ScenarioStepType } from '@/enums/apiEnum';

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

  /**
   * 增加步骤时判断父节点是否选中，如果选中则需要把新节点也选中
   */
  function isParentSelected(parent?: TreeNode<ScenarioStepItem>) {
    if (parent && selectedKeys.value.includes(parent.id)) {
      // 添加子节点时，当前节点已选中，则需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
      selectedKeys.value.push(step.value.id);
    }
  }

  /**
   * 处理添加子步骤、插入步骤前/后操作
   */
  function handleCreateStep(defaultStepInfo: ScenarioStepItem) {
    switch (props.createStepAction) {
      case 'addChildStep':
        const id = getGenerateId();
        if (step.value?.children) {
          step.value.children.push({
            ...cloneDeep(defaultStepItemCommon),
            ...defaultStepInfo,
            id,
            order: step.value.children.length + 1,
          });
        } else {
          step.value.children = [
            {
              ...cloneDeep(defaultStepItemCommon),
              ...defaultStepInfo,
              id,
              order: 1,
            },
          ];
        }
        if (selectedKeys.value.includes(step.value.id)) {
          // 添加子节点时，当前节点已选中，则需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
          selectedKeys.value.push(id);
        }
        break;
      case 'insertBefore':
        insertNode<ScenarioStepItem>(
          step.value.children || steps.value,
          step.value.id,
          {
            ...cloneDeep(defaultStepItemCommon),
            ...defaultStepInfo,
            id: getGenerateId(),
            order: step.value.order,
          },
          'before',
          isParentSelected,
          'id'
        );
        break;
      case 'insertAfter':
        insertNode<ScenarioStepItem>(
          step.value.children || steps.value,
          step.value.id,
          {
            ...cloneDeep(defaultStepItemCommon),
            ...defaultStepInfo,
            id: getGenerateId(),
            order: step.value.order + 1,
          },
          'after',
          isParentSelected,
          'id'
        );
        break;
      default:
        break;
    }
  }

  /**
   * 处理创建步骤操作
   * @param val 创建步骤类型
   */
  function handleCreateActionSelect(val: ScenarioAddStepActionType) {
    switch (val) {
      case ScenarioAddStepActionType.LOOP_CONTROL:
        if (step.value) {
          handleCreateStep({
            type: ScenarioStepType.LOOP_CONTROL,
            name: t('apiScenario.loopControl'),
          } as ScenarioStepItem);
        } else {
          steps.value.push({
            ...cloneDeep(defaultStepItemCommon),
            id: getGenerateId(),
            order: steps.value.length + 1,
            type: ScenarioStepType.LOOP_CONTROL,
            name: t('apiScenario.loopControl'),
          });
        }
        break;
      case ScenarioAddStepActionType.CONDITION_CONTROL:
        if (step.value) {
          handleCreateStep({
            type: ScenarioStepType.CONDITION_CONTROL,
            name: t('apiScenario.conditionControl'),
          } as ScenarioStepItem);
        } else {
          steps.value.push({
            ...cloneDeep(defaultStepItemCommon),
            id: getGenerateId(),
            order: steps.value.length + 1,
            type: ScenarioStepType.CONDITION_CONTROL,
            name: t('apiScenario.conditionControl'),
          });
        }
        break;
      case ScenarioAddStepActionType.ONLY_ONCE_CONTROL:
        if (step.value) {
          handleCreateStep({
            type: ScenarioStepType.ONLY_ONCE_CONTROL,
            name: t('apiScenario.onlyOnceControl'),
          } as ScenarioStepItem);
        } else {
          steps.value.push({
            ...cloneDeep(defaultStepItemCommon),
            id: getGenerateId(),
            order: steps.value.length + 1,
            type: ScenarioStepType.ONLY_ONCE_CONTROL,
            name: t('apiScenario.onlyOnceControl'),
          });
        }
        break;
      case ScenarioAddStepActionType.WAIT_TIME:
        if (step.value) {
          handleCreateStep({
            type: ScenarioStepType.WAIT_TIME,
            name: t('apiScenario.waitTime'),
          } as ScenarioStepItem);
        } else {
          steps.value.push({
            ...cloneDeep(defaultStepItemCommon),
            id: getGenerateId(),
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
          const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.value.id, 'id');
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
      document.getElementById(step.value.id.toString())?.click();
    }
  }

  function openTutorial() {
    window.open('https://zhuanlan.zhihu.com/p/597905464?utm_id=0', '_blank');
  }
</script>

<style lang="less" scoped></style>
