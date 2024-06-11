import type { MinderNodePosition } from '@/store/modules/components/minder-editor/types';

import type { MinderJsonNode } from '../../props';

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
 * 将节点及其子节点id置为null，changed 标记为true
 * @param node
 */
export function resetNodes(nodes: MinderJsonNode[]) {
  if (nodes) {
    nodes.forEach((item: any) => {
      if (item.data) {
        item.data.id = null;
        item.data.contextChanged = true;
        item.data.changed = true;
        resetNodes(item.children);
      }
    });
  }
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
