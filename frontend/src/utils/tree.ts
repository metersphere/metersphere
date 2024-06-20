import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

interface Tree {
  id?: string | number;
  groupId?: number;
  children?: Tree[];
  [key: string]: any;
}
// 插入节点
export function insertNode(tree: Tree[], currentNode: Tree, nodeToInsert: Tree): boolean {
  // 查找并插入节点的辅助函数
  function findAndInsert(node: Tree, parentNode: Tree | null = null): boolean {
    if (node.id === currentNode.id) {
      // 如果 parentNode 为 null，说明当前节点是根节点
      if (parentNode === null) {
        const index = tree.findIndex((n) => n.id === currentNode.id);
        // 根据 currentNode 的 groupId 是否存在，决定插入位置
        if (currentNode.groupId !== undefined) {
          // 插入到当前节点的后面
          tree.splice(index + 1, 0, nodeToInsert);
        } else {
          // 插入到同级节点的末尾
          tree.push(nodeToInsert);
        }
      } else {
        // 当前节点不是根节点
        const index = parentNode.children?.findIndex((n) => n.id === currentNode.id) ?? -1;
        // 根据 currentNode 的 groupId 是否存在，决定插入位置
        if (currentNode.groupId !== undefined) {
          // 插入到当前节点的后面
          parentNode.children?.splice(index + 1, 0, nodeToInsert);
        } else {
          // 插入到同级节点的末尾
          parentNode.children?.push(nodeToInsert);
        }
      }
      return true; // 插入成功
    }

    // 递归搜索子节点
    node.children?.forEach((child) => {
      if (findAndInsert(child, node)) {
        return true; // 如果在子树中插入成功，提前结束
      }
    });

    return false; // 未找到匹配的节点或插入失败
  }
  // 从根节点开始搜索并尝试插入
  return tree.some((node) => findAndInsert(node));
}

// 根据 groupId 查找第一个匹配的节点
export function findFirstByGroupId(tree: Tree[], groupId: number): Tree | null {
  const foundNode = tree.find((node) => node.groupId === groupId); // Use array.find() method to find the first node with matching groupId
  if (foundNode) {
    return foundNode;
  }

  tree.forEach((node) => {
    if (node.children) {
      const found = findFirstByGroupId(node.children, groupId);
      if (found) {
        return found;
      }
    }
  });

  return null;
}

// 根据id删除node
export function deleteNodeById(tree: Tree[], nodeId: number): void {
  // 查找并删除节点的辅助函数
  function findAndDelete(node: Tree, parentNode: Tree | null = null): boolean {
    // 如果当前节点就是要删除的节点
    if (node.id === nodeId) {
      if (parentNode) {
        // 如果存在父节点，从父节点的 children 中删除当前节点
        parentNode.children = parentNode.children?.filter((child) => child.id !== nodeId) ?? [];
      } else {
        // 如果不存在父节点，说明当前节点是根节点，直接从树中删除
        const index = tree.findIndex((n) => n.id === nodeId);
        if (index !== -1) {
          tree.splice(index, 1);
        }
      }
      return true; // 删除成功
    }

    // 递归搜索子节点
    if (node.children) {
      for (let i = 0; i < node.children.length; i++) {
        if (findAndDelete(node.children[i], node)) {
          return true; // 如果在子树中删除成功，提前结束
        }
      }
    }

    return false; // 未找到匹配的节点或删除失败
  }
  // 从根节点开始搜索并尝试删除
  tree.slice().forEach((rootNode) => {
    findAndDelete(rootNode);
  });
}

// 统计整棵树的节点数
export function countNodes(tree: Tree[]): number {
  // 统计节点数的辅助函数
  function count(node: Tree): number {
    // 计算当前节点的子节点数
    let childCount = 0;
    if (node.children) {
      childCount = node.children.reduce((acc, child) => acc + count(child), 0);
    }
    // 返回当前节点加上子节点的总数
    return 1 + childCount; // 当前节点自身也算一个节点
  }

  // 对树的每个根节点调用 count 函数，并累加结果
  return tree.reduce((acc, rootNode) => acc + count(rootNode), 0);
}

// 根据 groupId 统计元素数量
export function countNodesByGroupId(tree: Tree[], targetGroupId: number): number {
  // 统计符合条件的节点数的辅助函数
  function count(node: Tree): number {
    // 检查当前节点的 groupId 是否符合条件
    const matches = node.groupId === targetGroupId ? 1 : 0;
    // 计算当前节点的子节点中符合条件的节点数
    let childMatches = 0;
    if (node.children) {
      childMatches = node.children.reduce((acc, child) => acc + count(child), 0);
    }
    // 返回当前节点和子节点中符合条件的节点总数
    return matches + childMatches;
  }

  // 对树的每个根节点调用 count 函数，并累加结果
  return tree.reduce((acc, rootNode) => acc + count(rootNode), 0);
}

// 根据 groupId 删除所有符合条件的节点
export function deleteNodesByGroupId(tree: Tree[], targetGroupId: number): void {
  // 递归删除符合条件的节点的辅助函数
  function deleteMatchingNodes(nodes: Tree[]): Tree[] {
    return nodes
      .map((node) => {
        // 先处理子节点
        if (node.children) {
          node.children = deleteMatchingNodes(node.children);
        }
        return node;
      })
      .filter((node) => node.groupId !== targetGroupId); // 过滤掉符合条件的节点
  }

  // 对树的每个根节点调用 deleteMatchingNodes 函数，并更新树
  tree.splice(0, tree.length, ...deleteMatchingNodes(tree));
}

// 找到树结构最顶层的父节点id
export function getNodeParentId(node: MsTreeNodeData): string {
  while (node?.parent) {
    node = node.parent;
  }
  return node.id;
}
