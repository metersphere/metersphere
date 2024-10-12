import { mapTree } from '@/utils';

import { ModuleTreeNode } from '@/models/common';

export default function useCalculateTreeCount() {
  // 处理节点及其子节点的总数计算
  function processTreeData(nodes: ModuleTreeNode[]): ModuleTreeNode[] {
    const traverse = (node: any): number => {
      node.totalCount = node.count; // 设置当前节点的总数
      let totalChildrenCount = 0;

      if (node.children && node.children.length > 0) {
        totalChildrenCount = node.children.reduce((sum: any, child: any) => {
          return sum + traverse(child);
        }, 0);
        node.count -= totalChildrenCount; // 更新当前节点的 count 减去子节点的数量
      }

      return node.count + totalChildrenCount;
    };

    nodes.forEach((node: ModuleTreeNode) => traverse(node));
    return nodes;
  }

  // 计算树结构中的节点数量
  function calculateTreeCount(tree: ModuleTreeNode[], moduleCount: Record<string, any>): ModuleTreeNode[] {
    const folderTree = ref<ModuleTreeNode[]>([]);

    folderTree.value = mapTree<ModuleTreeNode>(tree, (node) => {
      return {
        ...node,
        count: moduleCount[node.id] ?? node.count, // 更新节点的 count
      };
    });

    // 处理节点总数
    const updatedModuleTreeCount = processTreeData(folderTree.value) as ModuleTreeNode[];

    // 再次映射树结构以更新 count
    folderTree.value = mapTree<ModuleTreeNode>(updatedModuleTreeCount, (node) => {
      return {
        ...node,
        count: node.count,
      };
    });

    return folderTree.value;
  }

  return {
    calculateTreeCount,
  };
}
