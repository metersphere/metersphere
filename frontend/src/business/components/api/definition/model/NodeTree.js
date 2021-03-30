// 递归构建节点路径下拉框选项
export function buildNodePath(node, option, moduleOptions) {
  option.id = node.id;
  option.path = option.path + '/' + node.name;
  moduleOptions.push(option);
  if (node.children) {
    for (let i = 0; i < node.children.length; i++) {
      buildNodePath(node.children[i], {path: option.path}, moduleOptions);
    }
  }
}
// 构建树
export function buildTree(node, option) {
  option.id = node.id;
  option.name = node.name;
  option.path = option.path + '/' + node.name;
  node.path = option.path;
  if (node.children) {
    for (let i = 0; i < node.children.length; i++) {
      buildTree(node.children[i], {path: option.path});
    }
  }
}

