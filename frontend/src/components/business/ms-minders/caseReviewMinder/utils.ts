import type { MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';

import { useI18n } from '@/hooks/useI18n';
import { mapTree } from '@/utils';

import { BatchApiParams } from '@/models/common';

const { t } = useI18n();

export function getMinderOffspringIds(node: MinderJsonNode): string[] {
  const offspringIds: string[] = [];
  mapTree(node.children || [], (e) => {
    if (e.data.resource?.includes(t('common.module')) && e.data.id !== 'fakeNode') {
      offspringIds.push(e.data.id);
    }
    return e;
  });
  return offspringIds;
}

// 模块或测试点
export function isModuleOrCollection(data?: MinderJsonNodeData) {
  return data?.isModuleOrCollection || data?.resource?.includes(t('common.module'));
}

/**
 * 获取脑图操作的参数
 * @param node 选中节点
 */
export function getMinderOperationParams(node: MinderJsonNode, isCollection = false): BatchApiParams {
  if (isModuleOrCollection(node.data)) {
    return {
      selectIds: [],
      selectAll: true,
      condition: {},
      ...(isCollection
        ? { collectionId: node.data?.id === 'NONE' ? '' : node.data?.id }
        : { moduleIds: (node.data?.id === 'NONE' ? [] : [node.data?.id, ...getMinderOffspringIds(node)]) as string[] }),
    };
  }
  return {
    selectIds: [node.data?.id as string],
    selectAll: false,
    condition: {},
    moduleIds: [],
  };
}
