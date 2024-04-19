export interface Option {
  label: string;
  value: string;
  children?: Option[];
}

// 递归函数，获取所有父级元素
export function findParents(data: Option[], targetId: string, parents: string[] = []): string[] | null {
  for (let i = 0; i < data.length; i++) {
    const current = data[i];
    if (current.value === targetId) {
      // 第一层级, 直接返回
      parents.push(current.value);
      return parents;
    }
    if (current.children && current.children.length > 0) {
      // 多层级, 递归查找
      parents.push(current.value);
      const result = findParents(current.children, targetId, parents);
      if (result) {
        // 子元素中有匹配的, 返回结果
        return result;
      }
      // 子元素中没有匹配的, 队尾元素删除, 继续查找相邻元素
      parents.pop();
    }
  }
  return null;
}
