import { cloneDeep } from 'lodash-es';

import { ScenarioStepItem } from '../stepTree.vue';

import { useI18n } from '@/hooks/useI18n';
import { getGenerateId, insertNodes, TreeNode } from '@/utils';

import { CreateStepAction } from '@/models/apiTest/scenario';
import { ScenarioStepType } from '@/enums/apiEnum';

import { defaultStepItemCommon } from '../../config';

export default function useCreateActions() {
  const { t } = useI18n();

  /**
   * 插入步骤时判断父节点是否选中，如果选中则需要把新节点也选中
   * @param selectedKeys 选中的步骤 stepId 集合
   * @param steps 需要判断的步骤
   * @param parent 需要判断的父节点
   */
  function checkedIfNeed(
    selectedKeys: (string | number)[],
    steps: (ScenarioStepItem | TreeNode<ScenarioStepItem>)[],
    parent?: TreeNode<ScenarioStepItem>
  ) {
    if (parent && selectedKeys.includes(parent.stepId)) {
      // 添加子节点时，当前节点已选中，则需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
      selectedKeys.push(...steps.map((item) => item.stepId));
    }
  }

  /**
   * 处理添加子步骤、插入步骤前/后操作-创建单个步骤
   * @param defaultStepInfo 创建传入的默认步骤信息
   * @param step 目标步骤
   * @param steps 顶层步骤列表
   * @param createStepAction 创建步骤操作类型
   * @param selectedKeys 选中的步骤 stepId 集合
   */
  function handleCreateStep(
    defaultStepInfo: ScenarioStepItem,
    step: ScenarioStepItem,
    steps: ScenarioStepItem[],
    createStepAction: CreateStepAction,
    selectedKeys: (string | number)[]
  ) {
    const newStep = {
      ...cloneDeep(defaultStepItemCommon),
      ...defaultStepInfo,
      stepId: getGenerateId(),
    };
    switch (createStepAction) {
      case 'inside':
        newStep.order = step.children ? step.children.length : 0;
        break;
      case 'before':
        newStep.order = step.order;
        break;
      case 'after':
      default:
        newStep.order = step.order + 1;
        break;
    }
    insertNodes<ScenarioStepItem>(
      step.parent?.children || steps,
      step.stepId,
      newStep,
      createStepAction,
      (newNode, parent) => checkedIfNeed(selectedKeys, [newNode], parent),
      'stepId'
    );
  }

  /**
   * 组装插入操作的步骤信息
   * @param newSteps 新步骤信息集合
   * @param type 需要插入的步骤类型
   * @param startOrder 步骤最开始的排序序号
   */
  function buildInsertStepInfos(
    newSteps: Record<string, any>[],
    type: ScenarioStepType,
    startOrder: number,
    stepsDetailMap: Record<string, any>
  ): ScenarioStepItem[] {
    let name: string;
    switch (type) {
      case ScenarioStepType.LOOP_CONTROL:
        name = t('apiScenario.loopControl');
        break;
      case ScenarioStepType.CONDITION_CONTROL:
        name = t('apiScenario.conditionControl');
        break;
      case ScenarioStepType.ONLY_ONCE_CONTROL:
        name = t('apiScenario.onlyOnceControl');
        break;
      case ScenarioStepType.WAIT_TIME:
        name = t('apiScenario.waitTime');
        break;
      case ScenarioStepType.QUOTE_API:
        name = t('apiScenario.quoteApi');
        break;
      case ScenarioStepType.COPY_API:
        name = t('apiScenario.copyApi');
        break;
      case ScenarioStepType.QUOTE_CASE:
        name = t('apiScenario.quoteCase');
        break;
      case ScenarioStepType.COPY_CASE:
        name = t('apiScenario.copyCase');
        break;
      case ScenarioStepType.QUOTE_SCENARIO:
        name = t('apiScenario.quoteScenario');
        break;
      case ScenarioStepType.COPY_SCENARIO:
        name = t('apiScenario.copyScenario');
        break;
      case ScenarioStepType.CUSTOM_API:
        name = t('apiScenario.customApi');
        break;
      case ScenarioStepType.SCRIPT_OPERATION:
        name = t('apiScenario.scriptOperation');
        break;
      default:
        break;
    }
    return newSteps.map((item, index) => {
      const stepId = getGenerateId();
      stepsDetailMap[stepId] = item; // 导入系统请求的引用接口和 case 的时候需要先存储一下引用的接口/用例信息
      return {
        ...cloneDeep(defaultStepItemCommon),
        ...item,
        stepId,
        type,
        name,
        order: startOrder + index,
      };
    });
  }

  /**
   * 处理添加子步骤、插入步骤前/后操作-创建多个步骤
   * @param step 目标步骤
   * @param readyInsertSteps 待插入的步骤信息数组（需要先buildInsertStepInfos得到构建后的步骤信息）
   * @param steps 顶层步骤列表
   * @param createStepAction 创建步骤操作类型
   * @param type 需要插入的步骤类型
   * @param selectedKeys 选中的步骤 stepId 集合
   */
  function handleCreateSteps(
    step: ScenarioStepItem,
    readyInsertSteps: ScenarioStepItem[],
    steps: ScenarioStepItem[],
    createStepAction: CreateStepAction,
    selectedKeys: (string | number)[]
  ) {
    insertNodes<ScenarioStepItem>(
      step.parent?.children || steps,
      step.stepId,
      readyInsertSteps,
      createStepAction,
      undefined,
      'stepId'
    );
    checkedIfNeed(selectedKeys, readyInsertSteps, step);
  }

  return {
    handleCreateStep,
    buildInsertStepInfos,
    handleCreateSteps,
    checkedIfNeed,
  };
}
