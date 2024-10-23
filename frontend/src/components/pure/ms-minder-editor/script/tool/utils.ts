import type { MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';

import { useI18n } from '@/hooks/useI18n';
import type { MinderNodePosition } from '@/store/modules/components/minder-editor/types';
import { findNodeByKey, getGenerateId, mapTree } from '@/utils';

const { t } = useI18n();
const moduleTag = t('common.module');

export function isDisableNode(minder: any) {
  let node: MinderJsonNode;
  if (minder && minder.getSelectedNode) {
    node = minder.getSelectedNode();
    if (node && node.data?.disabled === true) {
      return true;
    }
  }
  return false;
}

export function isDeleteDisableNode(minder: any) {
  let node: MinderJsonNode;
  if (minder && minder.getSelectedNode) {
    node = minder.getSelectedNode();
    if (node && node.data?.disabled === true && !node.data.allowDelete) {
      return true;
    }
  }
  return false;
}

export function isTagEnableNode(node: MinderJsonNode) {
  if (node.data && (node.data.tagEnable === true || node.data.allowDisabledTag === true)) {
    return true;
  }
  return false;
}

export function isTagEnable(minder: any) {
  let node: MinderJsonNode;
  if (minder && minder.getSelectedNode) {
    node = minder.getSelectedNode();
    if (isTagEnableNode(node)) {
      return true;
    }
  }
  return false;
}

export function markChangeNode(node: MinderJsonNode) {
  if (node.data) {
    node.data.changed = true;
  }
}

function markDelNode(node: MinderJsonNode, deleteChild: MinderJsonNode) {
  deleteChild.push(node.data);
  if (node.children) {
    node.children.forEach((child: MinderJsonNode) => {
      markDelNode(child, deleteChild);
    });
  }
}

// 在父节点记录删除的节点
export function markDeleteNode(minder: any) {
  if (minder) {
    const nodes: MinderJsonNode[] = minder.getSelectedNodes();
    nodes.forEach((node: MinderJsonNode) => {
      if (node.parent?.data) {
        const pData = node.parent.data;
        if (!pData.deleteChild) {
          pData.deleteChild = [];
        }
        markDelNode(node, pData.deleteChild);
      }
    });
  }
}

export function isPriority(e: any) {
  if (e.getAttribute('text-rendering') === 'geometricPrecision' && e.getAttribute('text-anchor') === 'middle') {
    return true;
  }
  return false;
}

export function setPriorityView(priorityStartWithZero: boolean, priorityPrefix: string) {
  // 手动将优先级前面加上P显示
  const items = document.getElementsByTagName('text');
  if (items) {
    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (isPriority(item)) {
        let content = item.innerHTML;
        if (content.indexOf(priorityPrefix) < 0) {
          if (priorityStartWithZero) {
            content = `${parseInt(content, 10) - 1}`;
          }
          item.innerHTML = priorityPrefix + content;
        }
      }
    }
  }
}

/**
 * 设置自定义优先级文本
 * @param valueMap 优先级数字与文本映射
 */
export function setCustomPriorityView(valueMap: Record<any, string>) {
  const items = document.getElementsByTagName('text');
  if (items) {
    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (isPriority(item)) {
        const content = item.innerHTML;
        if (valueMap[content]) {
          // 检查当前节点内优先级文本是否在映射中，如果在则替换；否则代表已经被替换过了，不再处理
          item.innerHTML = valueMap[content];
        }
      }
    }
  }
}

/**
 * 重置节点为新节点
 * @param node
 */
export function resetNodes(nodes: MinderJsonNode[]) {
  return mapTree(nodes, (node) => {
    if (node.data && node.data?.id !== 'fakeNode' && node.data?.type !== 'tmp') {
      node.data = {
        ...node.data,
        isNew: true,
        id: getGenerateId(),
        count:
          node.children?.filter((e: MinderJsonNode) => e.data?.id !== 'fakeNode' && e.data?.type !== 'tmp').length || 0,
        type: 'ADD',
        changed: false,
      };
      return node;
    }
    return null;
  });
}

export function isDisableForNode(node: MinderJsonNode) {
  if (node && node.data?.disabled === true) {
    return true;
  }
  return false;
}

/**
 * 判断脑图节点是否在脑图视图框内
 * @param node 目标节点
 */
export function isNodeInMinderView(node?: MinderJsonNode, nodePosition?: MinderNodePosition, offsetX?: number) {
  const containerWidth = document.querySelector('.ms-minder-container')?.clientWidth || 0;
  if (node) {
    nodePosition = (node || window.minder.getSelectedNode())?.getRenderBox();
  }
  if (nodePosition) {
    return nodePosition.x >= 0 && nodePosition.x + (offsetX || 0) <= containerWidth;
  }
  return false;
}

/**
 * 渲染其子节点
 * @param node 对应节点
 * @param renderNode 需要渲染的子节点
 */
export function handleRenderNode(node: MinderJsonNode, renderNode: MinderJsonNode) {
  if (!node.data) return;
  window.minder.renderNodeBatch(renderNode);
  node.layout();
  node.data.isLoaded = true;
}

/**
 * 展开节点和其全部子节点
 * @param node 对应节点
 */
export function expendNodeAndChildren(node: MinderJsonNode) {
  if (node.children?.length) {
    node.expand();
    node.renderTree();
    node.children?.forEach((child) => expendNodeAndChildren(child));
  }
}

/**
 * 移除占位的虚拟节点
 * @param node 对应节点
 * @param fakeNodeName 虚拟节点名称
 */
export function removeFakeNode(node: MinderJsonNode, fakeNodeName: string) {
  const fakeNode = node.children?.find((e: MinderJsonNode) => e.data?.id === fakeNodeName);
  if (fakeNode) {
    window.minder.removeNode(fakeNode);
  }
}

/**
 * 创建节点
 * @param data 节点数据
 * @param parentNode 父节点
 */
export function createNode(data?: MinderJsonNodeData, parentNode?: MinderJsonNode) {
  return window.minder.createNode(
    {
      ...data,
      text: data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
      expandState: 'collapse',
      disabled: true,
    },
    parentNode
  );
}

/**
 * 递归渲染子节点及其子节点
 * @param parentNode - 父节点
 * @param children - 子节点数组
 */
export function renderSubNodes(parentNode: MinderJsonNode, children?: MinderJsonNode[]) {
  return (
    children?.map((item: MinderJsonNode) => {
      const grandChild = createNode(
        { ...item.data, text: item.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '' } as MinderJsonNodeData,
        parentNode
      );
      const greatGrandChildren = renderSubNodes(grandChild, item.children);
      window.minder.renderNodeBatch(greatGrandChildren);
      return grandChild;
    }) || []
  );
}

// 清空选中状态
export function clearSelectedNodes() {
  const currentSelectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
  if (currentSelectedNodes && currentSelectedNodes.length > 0) {
    window.minder.toggleSelect(currentSelectedNodes);
  }
}

// 清空子节点，从后向前遍历时，删除节点不会影响到尚未遍历的节点
export function clearNodeChildren(node: MinderJsonNode) {
  if (!node.children?.length) return;
  for (let i = node.children.length - 1; i >= 0; i--) {
    window.minder.removeNode(node.children[i]);
  }
}

// 重新递归渲染子模块
export function renderSubModules(
  node: MinderJsonNode,
  importJsonRoot: MinderJsonNode,
  modulesCount: Record<string, any>
) {
  const waitingRenderNodes: MinderJsonNode[] = [];
  const createSubModules = (id: string) => {
    const curNode: MinderJsonNode | null = findNodeByKey(importJsonRoot.children as MinderJsonNode[], id, 'id', 'data');
    const minderNode = window.minder.getNodeById(id);
    if (curNode?.children) {
      const moduleNode = curNode.children.filter(
        (child) => child.data?.resource?.includes(moduleTag) || child.data?.type === 'tmpModule'
      );
      if (!moduleNode.length) {
        const newNode = createNode(
          {
            id: 'fakeNode',
            text: t('ms.minders.moreCase'),
            resource: [''],
          },
          minderNode
        );
        waitingRenderNodes.push(newNode);
      } else {
        moduleNode.forEach((childNode) => {
          if (!childNode.data) return;
          const newNode = createNode(
            {
              ...childNode.data,
              id: childNode.id || childNode.data?.id || '',
              text: childNode.name || childNode.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
              count: modulesCount[childNode.data.id],
              isLoaded: false,
            },
            minderNode
          );
          waitingRenderNodes.push(newNode);
          createSubModules(childNode.data.id);
        });
      }
    }
  };
  createSubModules(node.data?.id as string);
  handleRenderNode(node, waitingRenderNodes);
}
