import { cloneDeep } from 'lodash-es';

import { ScenarioStepItem } from '../stepTree.vue';

import { getGenerateId, insertNode, TreeNode } from '@/utils';

import { CreateStepAction } from '@/models/apiTest/scenario';

import { defaultStepItemCommon } from '../../config';
import steps from '@arco-design/web-vue/es/steps';

export default function useCreateActions() {
  /**
   * 增加步骤时判断父节点是否选中，如果选中则需要把新节点也选中
   */
  function isParentSelected(
    selectedKeys: (string | number)[],
    step: ScenarioStepItem,
    parent?: TreeNode<ScenarioStepItem>
  ) {
    if (parent && selectedKeys.includes(parent.id)) {
      // 添加子节点时，当前节点已选中，则需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
      selectedKeys.push(step.id);
    }
  }

  /**
   * 处理添加子步骤、插入步骤前/后操作
   */
  function handleCreateStep(
    defaultStepInfo: ScenarioStepItem,
    step: ScenarioStepItem,
    createStepAction: CreateStepAction,
    selectedKeys: (string | number)[]
  ) {
    switch (createStepAction) {
      case 'addChildStep':
        const id = getGenerateId();
        if (step.children) {
          step.children.push({
            ...cloneDeep(defaultStepItemCommon),
            ...defaultStepInfo,
            id,
            order: step.children.length + 1,
          });
        } else {
          step.children = [
            {
              ...cloneDeep(defaultStepItemCommon),
              ...defaultStepInfo,
              id,
              order: 1,
            },
          ];
        }
        if (selectedKeys.includes(step.id)) {
          // 添加子节点时，当前节点已选中，则需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
          selectedKeys.push(id);
        }
        break;
      case 'insertBefore':
        insertNode<ScenarioStepItem>(
          step.children || steps.value,
          step.id,
          {
            ...cloneDeep(defaultStepItemCommon),
            ...defaultStepInfo,
            id: getGenerateId(),
            order: step.order,
          },
          'before',
          (parent) => isParentSelected(selectedKeys, step, parent),
          'id'
        );
        break;
      case 'insertAfter':
        insertNode<ScenarioStepItem>(
          step.children || steps.value,
          step.id,
          {
            ...cloneDeep(defaultStepItemCommon),
            ...defaultStepInfo,
            id: getGenerateId(),
            order: step.order + 1,
          },
          'after',
          (parent) => isParentSelected(selectedKeys, step, parent),

          'id'
        );
        break;
      default:
        break;
    }
  }

  return {
    handleCreateStep,
    isParentSelected,
  };
}
