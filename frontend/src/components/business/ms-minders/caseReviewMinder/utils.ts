import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';

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

/**
 * 获取脑图操作的参数
 * @param node 选中节点
 */
export function getMinderOperationParams(node: MinderJsonNode): BatchApiParams {
  if (node.data?.resource?.includes(t('common.module'))) {
    return {
      selectIds: [],
      selectAll: true,
      condition: {},
      moduleIds: node.data?.id === 'NONE' ? [] : [node.data?.id, ...getMinderOffspringIds(node)],
    };
  }
  return {
    selectIds: [node.data?.id as string],
    selectAll: false,
    condition: {},
    moduleIds: [],
  };
}
