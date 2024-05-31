import type { MinderJsonNode } from '../../props';

export function isDisableNode(minder: any) {
  let node;
  if (minder && minder.getSelectedNode) {
    node = minder.getSelectedNode();
  }
  if (node && node.data.disable === true) {
    return true;
  }
  return false;
}

export function isDeleteDisableNode(minder: any) {
  let node;
  if (minder && minder.getSelectedNode) {
    node = minder.getSelectedNode();
  }
  if (node && node.data.disable === true && !node.data.allowDelete) {
    return true;
  }
  return false;
}

export function isTagEnableNode(node: MinderJsonNode) {
  if (node && (node.data.tagEnable === true || node.data.allowDisabledTag === true)) {
    return true;
  }
  return false;
}

export function isTagEnable(minder: any) {
  let node;
  if (minder && minder.getSelectedNode) {
    node = minder.getSelectedNode();
  }
  if (isTagEnableNode(node)) {
    return true;
  }
  return false;
}

export function markChangeNode(node: MinderJsonNode) {
  if (node.data) {
    node.data.changed = true;
  }
}

function markDelNode(node: MinderJsonNode, deleteChild: any) {
  deleteChild.push(node.data);
  if (node.children) {
    node.children.forEach((child: any) => {
      markDelNode(child, deleteChild);
    });
  }
}

// 在父节点记录删除的节点
export function markDeleteNode(minder: any) {
  if (minder) {
    const nodes = minder.getSelectedNodes();
    nodes.forEach((node: MinderJsonNode) => {
      if (node && node.parent) {
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
 * 将节点及其子节点id置为null，changed 标记为true
 * @param node
 */
export function resetNodes(nodes: any) {
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
  if (node && node.data.disable === true) {
    return true;
  }
  return false;
}
