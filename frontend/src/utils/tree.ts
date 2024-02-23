interface TreeData {
  id: number;
  groupId?: number;
  children?: TreeData[];
  [key: string]: any;
}

export function insertTreeByCurrentId(tree: TreeData[], currentId: number, newData: TreeData) {
  const stack: Array<{ node: TreeData; parent: TreeData | null }> = tree.map((node) => ({ node, parent: null }));

  while (stack.length > 0) {
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    const { node, parent } = stack.pop()!;

    if (parent && parent.children) {
      const index = parent.children.findIndex((child) => child.id === currentId);
      if (index !== -1) {
        // 在目标节点后面插入新数据
        parent.children.splice(index + 1, 0, newData);
        return true;
      }
    }

    if (node.children) {
      node.children.forEach((child) => {
        stack.push({ node: child, parent: node });
      });
    }
  }
  return false;
}

export function insertTreeByGroupId(tree: TreeData[], currentId: number, newData: TreeData) {
  const stack: Array<{ node: TreeData; parent: TreeData | null }> = tree.map((node) => ({ node, parent: null }));

  while (stack.length > 0) {
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    const { node, parent } = stack.pop()!;

    if (parent && parent.children) {
      const index = parent.children.findIndex((child) => child.groupId === currentId);
      if (index !== -1) {
        // 在目标节点后面插入新数据
        parent.children.splice(index + 1, 0, newData);
        return true;
      }
    }

    if (node.children) {
      node.children.forEach((child) => {
        stack.push({ node: child, parent: node });
      });
    }
  }

  return false;
}

export function findFirstByGroupId(tree: TreeData[], groupId: number): TreeData | null {
  const queue: TreeData[] = [...tree];
  while (queue.length > 0) {
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    const node = queue.shift()!; // 取出队列的第一个元素
    if (node.groupId === groupId) {
      return node; // 如果匹配，返回当前节点
    }
    if (node.children) {
      queue.push(...node.children);
    }
  }
  return null;
}
