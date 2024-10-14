import { cloneDeep } from 'lodash-es';

import { useI18n } from '@/hooks/useI18n';
import { getGenerateId, insertNodes, TreeNode } from '@/utils';

import { CreateStepAction, ScenarioStepItem } from '@/models/apiTest/scenario';
import { ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';

import {
  defaultConditionController,
  defaultLoopController,
  defaultScenarioStepConfig,
  defaultStepItemCommon,
  defaultTimeController,
} from '../../config';

export default function useCreateActions() {
  const { t } = useI18n();

  /**
   * 插入步骤时判断父节点是否选中，如果选中则需要把新节点也选中
   * @param selectedKeys 选中的步骤 uniqueId 集合
   * @param steps 需要判断的步骤
   * @param parent 需要判断的父节点
   */
  function selectedIfNeed(
    selectedKeys: (string | number)[],
    steps: (ScenarioStepItem | TreeNode<ScenarioStepItem>)[],
    parent?: TreeNode<ScenarioStepItem>
  ) {
    if (parent && selectedKeys.includes(parent.uniqueId)) {
      // 添加子节点时，当前节点已选中，则需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
      selectedKeys.push(...steps.map((item) => item.uniqueId));
    }
  }

  /**
   * 处理添加子步骤、插入步骤前/后操作-创建单个步骤
   * @param defaultStepInfo 创建传入的默认步骤信息
   * @param step 目标步骤
   * @param steps 顶层步骤列表
   * @param createStepAction 创建步骤操作类型
   * @param selectedKeys 选中的步骤 uniqueId 集合
   */
  function handleCreateStep(
    defaultStepInfo: Record<string, any>,
    step: ScenarioStepItem,
    steps: ScenarioStepItem[],
    createStepAction: CreateStepAction,
    selectedKeys: (string | number)[]
  ) {
    const id = getGenerateId();
    const newStep = {
      ...cloneDeep(defaultStepItemCommon),
      id,
      uniqueId: id,
      ...defaultStepInfo,
    };
    insertNodes<ScenarioStepItem>(
      step.parent?.children || steps,
      step.uniqueId,
      newStep,
      createStepAction,
      (newNode, parent) => selectedIfNeed(selectedKeys, [newNode], parent),
      'uniqueId'
    );
  }

  /**
   * 组装插入操作的步骤信息
   * @param newSteps 新步骤信息集合
   * @param stepType 需要插入的步骤类型
   * @param startOrder 步骤最开始的排序序号
   */
  function buildInsertStepInfos(
    newSteps: Record<string, any>[],
    stepType: ScenarioStepType,
    refType: ScenarioStepRefType,
    startOrder: number,
    projectId: string
  ): ScenarioStepItem[] {
    let name: string;
    switch (stepType) {
      case ScenarioStepType.LOOP_CONTROLLER:
        name = t('apiScenario.loopControl');
        break;
      case ScenarioStepType.IF_CONTROLLER:
        name = t('apiScenario.conditionControl');
        break;
      case ScenarioStepType.ONCE_ONLY_CONTROLLER:
        name = t('apiScenario.onlyOnceControl');
        break;
      case ScenarioStepType.CONSTANT_TIMER:
        name = t('apiScenario.waitTime');
        break;
      case ScenarioStepType.CUSTOM_REQUEST:
        name = t('apiScenario.customApi');
        break;
      case ScenarioStepType.SCRIPT:
        name = t('apiScenario.scriptOperation');
        break;
      default:
        break;
    }
    return newSteps.map((item, index) => {
      const id = getGenerateId();
      let resourceField = {};
      let config = {};
      if (stepType === ScenarioStepType.LOOP_CONTROLLER) {
        config = cloneDeep(defaultLoopController);
      } else if (stepType === ScenarioStepType.IF_CONTROLLER) {
        config = cloneDeep(defaultConditionController);
      } else if (stepType === ScenarioStepType.CONSTANT_TIMER) {
        config = cloneDeep(defaultTimeController);
      } else if (stepType === ScenarioStepType.API_SCENARIO) {
        config = cloneDeep(defaultScenarioStepConfig);
      }
      if (item.resourceId || item.id) {
        // 引用复制接口、用例、场景时的源资源信息
        resourceField = {
          resourceId: item.resourceId || item.id, // 场景会调接口获取信息，所以有resourceId，接口、用例没有，下同
          resourceNum: item.resourceNum || item.num,
          resourceName: item.resourceName || item.name,
        };
      }
      if (item.protocol || item.config?.protocol) {
        // 自定义请求、api、case 添加协议和方法
        config = {
          ...config,
          protocol: item.protocol || item.config?.protocol,
          method: item.method || item.config?.method,
        };
      }
      return {
        ...cloneDeep(defaultStepItemCommon),
        id: item.uniqueId || id,
        uniqueId: item.uniqueId || id, // 生成唯一 ID，避免重复引用的步骤无法读取正确的执行结果
        config: {
          ...defaultStepItemCommon.config,
          ...config,
        },
        draggable: stepType !== ScenarioStepType.API_SCENARIO ? !item.config?.isQuoteScenarioStep : true, // 引用场景下的任何子步骤不可拖拽，除了场景本身
        isQuoteScenarioStep: item.config?.isQuoteScenarioStep || false,
        isRefScenarioStep: item.config?.isRefScenarioStep || false,
        children: item.children || [],
        stepType,
        refType,
        csvIds: [],
        originProjectId: item.originProjectId,
        copyFromStepId: item.copyFromStepId,
        ...resourceField,
        name: name || item.name,
        sort: startOrder + index,
        projectId,
      };
    });
  }

  /**
   * 处理添加子步骤、插入步骤前/后操作-创建多个步骤
   * @param step 目标步骤
   * @param readyInsertSteps 待插入的步骤信息数组（需要先buildInsertStepInfos得到构建后的步骤信息）
   * @param steps 顶层步骤列表
   * @param createStepAction 创建步骤操作类型
   * @param selectedKeys 选中的步骤 id 集合
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
      step.uniqueId,
      readyInsertSteps,
      createStepAction,
      undefined,
      'uniqueId'
    );
    selectedIfNeed(selectedKeys, readyInsertSteps, step);
  }

  return {
    handleCreateStep,
    buildInsertStepInfos,
    handleCreateSteps,
    selectedIfNeed,
  };
}
