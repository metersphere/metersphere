import { cloneDeep } from 'lodash-es';
import JSEncrypt from 'jsencrypt';

import { BatchActionQueryParams, MsTableColumnData } from '@/components/pure/ms-table/type';
import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

import { BugEditCustomField, CustomFieldItem } from '@/models/bug-management';

import { isObject } from './is';

type TargetContext = '_self' | '_parent' | '_blank' | '_top';
const multipleExcludes = ['MULTIPLE_SELECT', 'CHECKBOX', 'MULTIPLE_MEMBER'];
const selectExcludes = ['MEMBER', 'RADIO', 'SELECT'];

/**
 * 打开新窗口
 * @param url 页面地址
 * @param opts 配置
 */
export const openWindow = (url: string, opts?: { target?: TargetContext; [key: string]: any }) => {
  const { target = '_blank', ...others } = opts || {};
  window.open(
    url,
    target,
    Object.entries(others)
      .reduce((preValue: string[], curValue) => {
        const [key, value] = curValue;
        return [...preValue, `${key}=${value}`];
      }, [])
      .join(',')
  );
};

/**
 * url 正则校验
 */
export const regexUrl = new RegExp(
  '^(?!mailto:)(?:(?:http|https|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))|localhost)(?::\\d{2,5})?(?:(/|\\?|#)[^\\s]*)?$',
  'i'
);

/**
 * 遍历对象属性并一一添加到 url 地址参数上
 * @param baseUrl 需要添加参数的 url
 * @param obj 参数对象
 * @returns 拼接后的 url
 */
export function setObjToUrlParams(baseUrl: string, obj: any): string {
  let parameters = '';
  Object.keys(obj).forEach((key) => {
    parameters += `${key}=${encodeURIComponent(obj[key])}&`;
  });
  parameters = parameters.replace(/&$/, '');
  return /\?$/.test(baseUrl) ? baseUrl + parameters : baseUrl.replace(/\/?$/, '?') + parameters;
}

/**
 * 递归深度合并
 * @param src 源对象
 * @param target 待合并的目标对象
 * @returns 合并后的对象
 */
export const deepMerge = <T = any>(src: any = {}, target: any = {}): T => {
  Object.keys(target).forEach((key) => {
    src[key] = isObject(src[key]) ? deepMerge(src[key], target[key]) : (src[key] = target[key]);
  });
  return src;
};

/**
 * rgb 颜色转换为 hex 颜色
 * @param rgb rgb颜色字符串
 * @returns HEX 6位颜色字符串
 */
export const rgbToHex = (rgb: string) => {
  const matches = rgb.match(/^(\d+),\s*(\d+),\s*(\d+)$/);
  if (!matches) {
    return rgb;
  }
  const r = parseInt(matches[1], 10).toString(16).padStart(2, '0');
  const g = parseInt(matches[2], 10).toString(16).padStart(2, '0');
  const b = parseInt(matches[3], 10).toString(16).padStart(2, '0');
  return `#${r}${g}${b}`;
};

/**
 * 字符串内容文件下载
 * @param type 文件类型
 * @param content 文件内容
 * @param fileName 文件名
 */
export const downloadStringFile = (type: string, content: string, fileName: string) => {
  const fileContent = `data:${type};charset=utf-8,${encodeURIComponent(content)}`;
  const link = document.createElement('a');
  link.setAttribute('href', fileContent);
  link.setAttribute('download', fileName);
  link.style.display = 'none';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

/**
 * 休眠
 * @param ms 睡眠时长，单位毫秒
 * @returns
 */
export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(() => resolve(), ms);
  });
}

/**
 *
 * 返回文件的大小
 * @param fileSize file文件的大小size
 * @returns
 */
export function formatFileSize(fileSize: number): string {
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let size = fileSize;
  let unitIndex = 0;

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024;
    unitIndex++;
  }
  const unit = units[unitIndex];
  if (size) {
    const formattedSize = size.toFixed(2);
    return `${formattedSize} ${unit}`;
  }
  const formattedSize = 0;
  return `${formattedSize} ${unit}`;
}

/**
 * 字符串脱敏
 * @param str 需要脱敏的字符串
 * @returns 脱敏后的字符串
 */
export function desensitize(str: string): string {
  if (!str || typeof str !== 'string') {
    return '';
  }

  return str.replace(/./g, '*');
}

/**
 * 对话框标题动态内容字符限制
 * @param str 标题的动态内容
 * @returns 转化后的字符串
 */
export function characterLimit(str?: string, length?: number): string {
  if (!str) return '';
  if (str.length <= (length || 20)) {
    return str;
  }
  return `${str.slice(0, length || 20 - 3)}...`;
}

/**
 * 递归计算树形数组的最大深度
 * @param node 树形数组
 * @param depth 深度
 * @returns 最大深度
 */
export interface Node {
  children?: Node[];
  [key: string]: any;
}

export function calculateMaxDepth(arr?: Node[], depth = 0) {
  if (!arr || arr.length === 0) {
    return depth;
  }

  let maxDepth = depth;
  Object.values(arr).forEach((item) => {
    const childDepth = calculateMaxDepth(item.children, depth + 1);
    maxDepth = Math.max(maxDepth, childDepth);
  });

  return maxDepth;
}

export interface TreeNode<T> {
  children?: TreeNode<T>[];
  [key: string]: any;
}

/**
 * 递归遍历树形数组或树
 * @param tree 树形数组或树
 * @param customNodeFn 自定义节点函数
 * @param customChildrenKey 自定义子节点的key
 * @param continueCondition 继续递归的条件，某些情况下需要无需递归某些节点的子孙节点，可传入该条件
 */
export function traverseTree<T>(
  tree: TreeNode<T> | TreeNode<T>[] | T | T[],
  customNodeFn: (node: TreeNode<T>) => void,
  continueCondition?: (node: TreeNode<T>) => boolean,
  customChildrenKey = 'children'
) {
  if (!Array.isArray(tree)) {
    tree = [tree];
  }
  for (let i = 0; i < tree.length; i++) {
    const node = (tree as TreeNode<T>[])[i];
    if (typeof customNodeFn === 'function') {
      customNodeFn(node);
    }
    if (node[customChildrenKey] && Array.isArray(node[customChildrenKey]) && node[customChildrenKey].length > 0) {
      if (typeof continueCondition === 'function' && !continueCondition(node)) {
        // 如果有继续递归的条件，则判断是否继续递归
        break;
      }
      traverseTree(node[customChildrenKey], customNodeFn, continueCondition, customChildrenKey);
    }
  }
}

/**
 * 递归遍历树形数组或树，返回新的树
 * @param tree 树形数组或树
 * @param customNodeFn 自定义节点函数
 * @param customChildrenKey 自定义子节点的key
 * @param parent 父节点
 * @param parentPath 父节点路径
 * @param level 节点层级
 * @returns 遍历后的树形数组
 */
export function mapTree<T>(
  tree: TreeNode<T> | TreeNode<T>[] | T | T[],
  customNodeFn: (node: TreeNode<T>, path: string, _level: number) => TreeNode<T> | null = (node) => node,
  customChildrenKey = 'children',
  parentPath = '',
  level = 0,
  parent: TreeNode<T> | null = null
): T[] {
  let cloneTree = cloneDeep(tree);
  if (!Array.isArray(cloneTree)) {
    cloneTree = [cloneTree];
  }

  function mapFunc(
    _tree: TreeNode<T> | TreeNode<T>[] | T | T[],
    _parentPath = '',
    _level = 0,
    _parent: TreeNode<T> | null = null
  ): T[] {
    if (!Array.isArray(_tree)) {
      _tree = [_tree];
    }
    return _tree
      .map((node: TreeNode<T>, i: number) => {
        const fullPath = node.path ? `${_parentPath}/${node.path}`.replace(/\/+/g, '/') : '';
        node.sort = i + 1; // sort 从 1 开始
        node.parent = _parent || undefined; // 没有父节点说明是树的第一层
        const newNode = typeof customNodeFn === 'function' ? customNodeFn(node, fullPath, _level) : node;
        if (newNode) {
          newNode.level = _level;
          if (newNode[customChildrenKey] && newNode[customChildrenKey].length > 0) {
            newNode[customChildrenKey] = mapFunc(newNode[customChildrenKey], fullPath, _level + 1, newNode);
          }
        }
        return newNode;
      })
      .filter((node: TreeNode<T> | null) => node !== null);
  }
  return mapFunc(cloneTree, parentPath, level, parent);
}

/**
 * 过滤树形数组或树
 * @param tree 树形数组或树
 * @param customNodeFn 自定义节点函数
 * @param customChildrenKey 自定义子节点的key
 * @returns 遍历后的树形数组
 */
export function filterTree<T>(
  tree: TreeNode<T> | TreeNode<T>[] | T | T[],
  filterFn: (node: TreeNode<T>, nodeIndex: number, parent?: TreeNode<T> | null) => boolean,
  customChildrenKey = 'children',
  parentNode: TreeNode<T> | null = null
): TreeNode<T>[] {
  if (!Array.isArray(tree)) {
    tree = [tree];
  }
  const filteredTree: TreeNode<T>[] = [];
  for (let i = 0; i < tree.length; i++) {
    const node = (tree as TreeNode<T>[])[i];
    // 如果节点满足过滤条件，则保留该节点，并递归过滤子节点
    if (filterFn(node, i, parentNode)) {
      const newNode = cloneDeep({ ...node, [customChildrenKey]: [] });
      if (node[customChildrenKey] && node[customChildrenKey].length > 0) {
        // 递归过滤子节点，并将过滤后的子节点添加到当前节点中
        newNode[customChildrenKey] = filterTree(node[customChildrenKey], filterFn, customChildrenKey, node);
      } else {
        newNode[customChildrenKey] = [];
      }
      filteredTree.push(newNode);
    }
  }
  return filteredTree;
}
/**
 * 根据属性 key 查找树形数组中匹配的某个节点
 * @param trees 属性数组
 * @param targetKey 需要匹配的属性值
 * @param customKey 默认为 key，可自定义需要匹配的属性名
 * @returns 匹配的节点/null
 */
export function findNodeByKey<T>(
  trees: TreeNode<T>[],
  targetKey: string | number,
  customKey = 'key',
  dataKey: string | undefined = undefined
): TreeNode<T> | T | null {
  for (let i = 0; i < trees.length; i++) {
    const node = trees[i];
    if (dataKey ? node[dataKey]?.[customKey] === targetKey : node[customKey] === targetKey) {
      return node; // 如果当前节点的 key 与目标 key 匹配，则返回当前节点
    }

    if (Array.isArray(node.children) && node.children.length > 0) {
      const _node = findNodeByKey(node.children, targetKey, customKey, dataKey); // 递归在子节点中查找
      if (_node) {
        return _node; // 如果在子节点中找到了匹配的节点，则返回该节点
      }
    }
  }

  return null; // 如果在整个树形数组中都没有找到匹配的节点，则返回 null
}

/**
 * 根据属性 alias 查找树形数组中匹配的某个节点
 * @param trees 属性数组
 * @param targetKey 需要匹配的属性值
 * @param customKey 默认为 key，可自定义需要匹配的属性名
 * @returns 匹配的节点/null
 */
export function findNodeByAlias<T>(
  trees: TreeNode<T>[],
  targetKey: string,
  customKey = 'alias'
): TreeNode<T> | T | null {
  for (let i = 0; i < trees.length; i++) {
    const node = trees[i];
    if (node[customKey] === targetKey) {
      return node; // 如果当前节点的 key 与目标 key 匹配，则返回当前节点
    }

    if (Array.isArray(node.children) && node.children.length > 0) {
      const _node = findNodeByAlias(node.children, targetKey, customKey); // 递归在子节点中查找
      if (_node) {
        return _node; // 如果在子节点中找到了匹配的节点，则返回该节点
      }
    }
  }

  return null; // 如果在整个树形数组中都没有找到匹配的节点，则返回 null
}

/**
 * 根据 key 遍历树，并返回找到的节点路径和节点
 */
export function findNodePathByKey<T>(
  tree: TreeNode<T>[],
  targetKey: string,
  dataKey?: string,
  customKey = 'key'
): TreeNode<T> | null {
  for (let i = 0; i < tree.length; i++) {
    const node = tree[i];
    if (dataKey ? node[dataKey]?.[customKey] === targetKey : node[customKey] === targetKey) {
      return { ...node, treePath: [dataKey ? node[dataKey] : node] }; // 如果当前节点的 key 与目标 key 匹配，则返回当前节点
    }

    if (Array.isArray(node.children) && node.children.length > 0) {
      const result = findNodePathByKey(node.children, targetKey, dataKey, customKey); // 递归在子节点中查找
      if (result) {
        result.treePath.unshift(dataKey ? node[dataKey] : node);
        return result; // 如果在子节点中找到了匹配的节点，则返回该节点
      }
    }
  }

  return null;
}

/**
 * 根据customKey替换树节点
 */
export function replaceNodeInTree<T>(
  tree: TreeNode<T>[],
  targetKey: string,
  newNode: TreeNode<T>,
  dataKey?: string,
  customKey = 'key'
): boolean {
  for (let i = 0; i < tree.length; i++) {
    const node = tree[i];
    if (dataKey ? node[dataKey]?.[customKey] === targetKey : node[customKey] === targetKey) {
      // 找到目标节点，进行替换
      tree[i] = newNode;
      return true;
    }
    if (node.children && node.children.length > 0) {
      // 如果当前节点有子节点，递归查找
      if (replaceNodeInTree(node.children, targetKey, newNode, dataKey, customKey)) {
        return true;
      }
    }
  }
  return false;
}

/**
 * 在某个节点前/后插入单个新节点
 * @param treeArr 目标树
 * @param targetKey 目标节点唯一值
 * @param newNodes 新节点树/数组
 * @param position 插入位置
 * @param customKey 默认为 key，可自定义需要匹配的属性名
 */
export function insertNodes<T>(
  treeArr: TreeNode<T>[],
  targetKey: string | number,
  newNodes: TreeNode<T> | TreeNode<T>[],
  position: 'before' | 'after' | 'inside',
  customFunc?: (node: TreeNode<T>, parent?: TreeNode<T>) => void,
  customKey = 'key'
): void {
  function insertNewNodes(
    array: TreeNode<T>[],
    startIndex: number,
    parent: TreeNode<T> | undefined,
    startOrder: number
  ) {
    if (Array.isArray(newNodes)) {
      // 插入节点数组
      newNodes.forEach((newNode, index) => {
        newNode.parent = parent;
        newNode.sort = startOrder + index;
      });
      array.splice(startIndex, 0, ...newNodes);
    } else {
      // 插入单个节点
      newNodes.parent = parent;
      newNodes.sort = startOrder;
      array.splice(startIndex, 0, newNodes);
    }
    // 更新插入节点之后的节点的 sort
    const newLength = Array.isArray(newNodes) ? newNodes.length : 1;
    for (let j = startIndex + newLength; j < array.length; j++) {
      array[j].sort += newLength;
    }
  }

  function insertNodeInTree(tree: TreeNode<T>[], parent?: TreeNode<T>): boolean {
    for (let i = 0; i < tree.length; i++) {
      const node = tree[i];
      if (node[customKey] === targetKey) {
        // 如果当前节点的 customKey 与目标 customKey 匹配，则在当前节点前/后/内部插入新节点
        const parentChildren = parent ? parent.children || [] : treeArr; // 父节点没有 children 属性，说明是树的第一层，使用 treeArr
        const index = parentChildren.findIndex((item) => item[customKey] === node[customKey]);
        if (position === 'before') {
          insertNewNodes(parentChildren, index, parent || node.parent, node.sort);
        } else if (position === 'after') {
          insertNewNodes(parentChildren, index + 1, parent || node.parent, node.sort + 1);
        } else if (position === 'inside') {
          if (!node.children) {
            node.children = [];
          }
          insertNewNodes(node.children, node.children.length, node, node.children.length + 1);
        }
        if (typeof customFunc === 'function') {
          if (Array.isArray(newNodes)) {
            newNodes.forEach((newNode) => customFunc(newNode, position === 'inside' ? node : parent));
          } else {
            customFunc(newNodes, position === 'inside' ? node : parent);
          }
        }
        // 插入后返回 true
        return true;
      }
      if (Array.isArray(node.children) && insertNodeInTree(node.children, node)) {
        return true;
      }
    }
    return false;
  }
  insertNodeInTree(treeArr);
}

/**
 * 处理树结构之间拖拽节点
 * @param treeArr 整颗树
 * @param dragNode 拖动节点
 * @param dropNode 目标节点
 * @param dropPosition -1: before, 0: inside, 1: after
 * @param customKey 默认为 key，可自定义需要匹配的属性名
 */
export function handleTreeDragDrop<T>(
  treeArr: TreeNode<T>[],
  dragNode: TreeNode<T>,
  dropNode: TreeNode<T>,
  dropPosition: number,
  customKey = 'key'
): boolean {
  // 把 dragNode 从原来的位置删除
  const parentChildren = dragNode.parent?.children || treeArr;
  if (dragNode.parent?.[customKey] === dropNode[customKey] && dropPosition === 0) {
    // 如果拖动的节点释放到自己的父节点上，不做任何操作
    return false;
  }
  const index = parentChildren.findIndex((node: TreeNode<T>) => node[customKey] === dragNode[customKey]);
  if (index !== -1) {
    parentChildren.splice(index, 1);

    // 更新删除节点后的节点的 sort
    for (let i = index; i < parentChildren.length; i++) {
      parentChildren[i].sort -= 1;
    }
  }

  // 拖动节点插入到目标节点的 children 数组中
  if (dropPosition === 0) {
    insertNodes(dropNode.parent?.children || treeArr, dropNode[customKey], dragNode, 'inside', undefined, customKey);
  } else {
    // 拖动节点插入到目标节点的前/后
    insertNodes(
      dropNode.parent?.children || treeArr,
      dropNode[customKey],
      dragNode,
      dropPosition === -1 ? 'before' : 'after',
      undefined,
      customKey
    );
  }
  return true;
}

/**
 * 删除树形数组中的某个节点
 * @param treeArr 目标树
 * @param targetKey 目标节点唯一值
 */
export function deleteNode<T>(treeArr: TreeNode<T>[], targetKey: string | number, customKey = 'key'): void {
  function deleteNodeInTree(tree: TreeNode<T>[]): void {
    for (let i = 0; i < tree.length; i++) {
      const node = tree[i];
      if (node[customKey] === targetKey) {
        tree.splice(i, 1); // 直接删除当前节点
        // 重新调整剩余子节点的 sort 序号
        for (let j = i; j < tree.length; j++) {
          tree[j].sort = j + 1;
        }
        return;
      }
      if (Array.isArray(node.children)) {
        deleteNodeInTree(node.children); // 递归删除子节点
      }
    }
  }

  deleteNodeInTree(treeArr);
}

/**
 * 删除树形数组中的多个节点
 * @param treeArr 目标树
 * @param targetKeys 目标节点唯一值的数组
 */
export function deleteNodes<T>(
  treeArr: TreeNode<T>[],
  targetKeys: (string | number)[],
  deleteCondition?: (node: TreeNode<T>, parent?: TreeNode<T>) => boolean,
  deleteCallBack?: (node: TreeNode<T>) => void,
  customKey = 'key'
): boolean {
  let hasDeleted = false;
  const targetKeysSet = new Set(targetKeys);
  function deleteNodesInTree(tree: TreeNode<T>[]): void {
    for (let i = tree.length - 1; i >= 0; i--) {
      const node = tree[i];
      if (targetKeysSet.has(node[customKey])) {
        if (deleteCondition && deleteCondition(node, node.parent)) {
          if (deleteCallBack) {
            deleteCallBack(node);
          }
          tree.splice(i, 1); // 直接删除当前节点
          hasDeleted = true;
          targetKeysSet.delete(node[customKey]); // 删除后从集合中移除
          // 重新调整剩余子节点的 sort 序号
          for (let j = i; j < tree.length; j++) {
            tree[j].sort = j + 1;
          }
        }
      } else if (Array.isArray(node.children)) {
        deleteNodesInTree(node.children); // 递归删除子节点
      }
    }
  }

  deleteNodesInTree(treeArr);
  return hasDeleted;
}

/**
 * 找出俩数组之间的差异项并返回
 * @param targetMap 目标项
 * @param sourceMap 查找项
 * @returns 返回不存在项的新数组
 */
export function getFilterList(targetMap: Record<string, any>[], sourceMap: Record<string, any>[]) {
  const filteredData: Record<string, any>[] = [];
  targetMap.forEach((item) => {
    if (!sourceMap.some((it) => it.id === item.id)) {
      filteredData.push(item);
    }
  });
  return filteredData;
}

/**
 * 加密
 * @param input 输入的字符串
 * @param publicKey 公钥
 * @returns
 */
export function encrypted(input: string) {
  const publicKey = localStorage.getItem('salt') || '';
  const encrypt = new JSEncrypt({ default_key_size: '1024' });
  encrypt.setPublicKey(publicKey);

  return encrypt.encrypt(input);
}

/**
 * 链接文件下载
 * @param url 文件地址
 * @param fileName 文件名
 */
export const downloadUrlFile = (url: string, fileName: string) => {
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.style.display = 'none';
  link.click();
};

/**
 * 获取 URL 哈希参数
 * @returns 参数对象
 */
export const getHashParameters = (): Record<string, string> => {
  const query = window.location.hash.split('?')[1]; // 获取 URL 哈希参数部分
  const paramsArray = query?.split('&') || []; // 将哈希参数字符串分割成数组
  const params: Record<string, string> = {};

  // 遍历数组并解析参数
  paramsArray.forEach((param) => {
    const [key, value] = param.split('=');
    if (key && value) {
      params[key] = decodeURIComponent(value); // 解码参数值
    }
  });

  return params;
};

export function getQueryVariable(variable: string) {
  const urlString = window.location.href;
  const queryIndex = urlString.indexOf('?');
  if (queryIndex !== -1) {
    const query = urlString.substring(queryIndex + 1);

    // 分割查询参数
    const params = query.split('&');
    // 遍历参数，找到 _token 参数的值
    let variableValue;
    params.forEach((param) => {
      const equalIndex = param.indexOf('=');
      const variableName = param.substring(0, equalIndex);
      if (variableName === variable) {
        variableValue = param.substring(equalIndex + 1);
      }
    });
    return variableValue;
  }
}

export function getUrlParameterWidthRegExp(name: string) {
  const url = window.location.href;
  name = name.replace(/[[\]]/g, '\\$&');
  const regex = new RegExp(`[?&]${name}(=([^&#]*)|&|#|$)`);
  const results = regex.exec(url);
  if (!results) return null;
  if (!results[2]) return '';
  return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

let lastTimestamp = 0;
let sequence = 0;

/**
 * 生成 id 序列号
 * @returns
 */
export const getGenerateId = () => {
  let timestamp = new Date().getTime();
  if (timestamp === lastTimestamp) {
    sequence++;
    if (sequence >= 100000) {
      // 如果超过999，则重置为0，等待下一秒
      sequence = 0;
      while (timestamp <= lastTimestamp) {
        timestamp = new Date().getTime();
      }
    }
  } else {
    sequence = 0;
  }

  lastTimestamp = timestamp;

  return timestamp.toString() + sequence.toString().padStart(5, '0');
};

/**
 * 下载文件
 * @param byte 字节流
 * @param fileName 文件名
 */
export const downloadByteFile = (byte: BlobPart, fileName: string) => {
  // 创建一个Blob对象
  const blob = new Blob([byte], { type: 'application/octet-stream' });
  // 创建一个URL对象，用于生成下载链接
  const url = window.URL.createObjectURL(blob);
  // 创建一个虚拟的<a>标签来触发下载
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName; // 设置下载文件的名称
  document.body.appendChild(link);
  link.click();

  // 释放URL对象
  window.URL.revokeObjectURL(url);
  document.body.removeChild(link);
};

/**
 * 图片压缩
 * @param {*} img 图片对象
 * @param {*} type 图片类型
 * @param {*} maxWidth 图片最大宽度
 * @param {*} flag
 */

export function compress(img: ImageData, type: string, maxWidth: number, flag: boolean) {
  let canvas: HTMLCanvasElement | null = document.createElement('canvas');
  let ctx2: any = canvas.getContext('2d');

  const ratio = img.width / img.height;
  let { width } = img;
  let { height } = img;
  // 根据flag判断是否压缩图片
  if (flag && maxWidth <= width) {
    width = maxWidth;
    height = maxWidth / ratio; // 维持图片宽高比
  }
  canvas.width = width;
  canvas.height = height;

  ctx2.fillStyle = '#fff';
  ctx2.fillRect(0, 0, canvas.width, canvas.height);
  ctx2.drawImage(img, 0, 0, width, height);

  let base64Data = canvas.toDataURL(type, 0.75);

  if (type === 'image/gif') {
    const regx = /(?<=data:image).*?(?=;base64)/; // 正则表示时在用于replace时，根据浏览器的不同，有的需要为字符串
    base64Data = base64Data.replace(regx, '/gif');
  }
  canvas = null;
  ctx2 = null;
  return base64Data;
}

/**
 * 转换字符串的字符集编码
 * @param str 需要转换的字符串
 * @param charset 字符集编码
 */
export function decodeStringToCharset(str: string, charset = 'UTF-8') {
  const encoder = new TextEncoder();
  const decoder = new TextDecoder(charset);
  return decoder.decode(encoder.encode(str));
}

interface ParsedCurlOptions {
  url?: string;
  queryParameters?: { key: string; value: string }[];
  headers?: { key: string; value: string }[];
  method?: string;
}
/**
 * 解析 curl 脚本
 * @param curlScript curl 脚本
 */
export function parseCurlScript(curlScript: string): ParsedCurlOptions {
  const options: ParsedCurlOptions = {};

  // 提取 URL
  const [_, url] = curlScript.match(/curl\s+'([^']+)'/) || [];
  if (url) {
    options.url = url;
    // 提取 query 参数
    const queryParams = url
      .split('?')[1]
      ?.split('&')
      .map((param) => {
        const [key, value] = param.split('=');
        return { key, value };
      });
    options.queryParameters = queryParams || [];
  }

  // 提取请求方式
  const [, method] = curlScript.match(/-X\s+'([^']+)'/) || [];
  if (method) {
    options.method = method;
  }

  // 提取 header
  const headersMatch = curlScript.match(/-H\s+'([^']+)'/g);
  if (headersMatch) {
    const headers = headersMatch.map((header) => {
      const [, value] = header.match(/-H\s+'([^']+)'/) || [];
      const [key, rawValue] = value.split(':');
      const trimmedName = key.trim();
      const trimmedValue = rawValue ? rawValue.trim() : '';
      return { key: trimmedName, value: trimmedValue };
    });

    // 过滤常用的 HTTP header
    const commonHeaders = [
      'accept',
      'accept-language',
      'cache-control',
      'content-type',
      'origin',
      'pragma',
      'referer',
      'sec-ch-ua',
      'sec-ch-ua-mobile',
      'sec-ch-ua-platform',
      'sec-fetch-dest',
      'sec-fetch-mode',
      'sec-fetch-site',
      'user-agent',
      'connection',
      'host',
      'accept-encoding',
      'x-requested-with',
    ];
    options.headers = headers.filter((header) => !commonHeaders.includes(header.key.toLowerCase()));
  }

  return options;
}

/**
 * 转换手机号格式
 * @param phoneNumber 需要转换的手机号
 */
export function formatPhoneNumber(phoneNumber = '') {
  if (phoneNumber && phoneNumber.trim().length === 11) {
    const cleanedNumber = phoneNumber.replace(/\D/g, '');
    const formattedNumber = cleanedNumber.replace(/(\d{3})(\d{4})(\d{4})/, '$1 $2 $3');
    return formattedNumber;
  }
  return phoneNumber;
}

// 获取表头自定义字段过滤索引
export function getCustomFieldIndex(field: CustomFieldItem) {
  const { fieldId } = field;
  if (selectExcludes.includes(field.type)) {
    return `custom_single_${fieldId}`;
  }
  if (multipleExcludes.includes(field.type)) {
    return `custom_multiple_${fieldId}`;
  }
  return fieldId;
}

// 表格自定义字段转column
export function customFieldToColumns(customFields: CustomFieldItem[]) {
  return customFields.map((field) => {
    const { fieldName, fieldKey, fieldId, options, platformOptionJson, type } = field;
    const column: MsTableColumnData = {
      title: fieldName,
      dataIndex: ['handleUser', 'status'].includes(fieldId) ? fieldKey : getCustomFieldIndex(field),
      showTooltip: true,
      showDrag: true,
      showInTable: true,
      width: 200,
      options: options || JSON.parse(platformOptionJson),
      type,
      internal: field.internal,
    };
    return column;
  });
}

// 表格查询参数转请求参数
export function tableParamsToRequestParams(params: BatchActionQueryParams) {
  const { selectedIds, selectAll, excludeIds, condition } = params;
  return {
    selectIds: selectedIds,
    excludeIds,
    selectAll,
    condition,
  };
}

/**
 * 解析 URL 查询参数
 * @param url URL 地址
 */
interface QueryParam {
  key: string;
  value: string;
}
export function parseQueryParams(url: string): QueryParam[] {
  const queryParams: QueryParam[] = [];
  // 从 URL 中提取查询参数部分
  const queryString = url.split('?')[1];
  if (queryString) {
    const params = new URLSearchParams(queryString);
    // 遍历查询参数，将每个参数添加到数组中
    params.forEach((value, key) => {
      queryParams.push({ key, value });
    });
  }
  return queryParams;
}

export interface CascaderOption {
  children?: CascaderOption[];
  value: string;
  text: string;
}

/**
 * 解析级联选择器的值
 * @param arr 值
 * @param options 级联选项 (默认层级2)
 */
export function cascaderValueToLabel(arr: string[], options: CascaderOption[]) {
  if (!arr || !options || arr.length === 0) return '';
  let targetLabel = '';
  options.forEach((option) => {
    if (option.value === arr[0]) {
      targetLabel += option.text;
      if (arr.length === 1) return;
      option.children?.forEach((child) => {
        if (child.value === arr[1]) {
          targetLabel += `/${child.text}`;
        }
      });
    }
  });
  return targetLabel;
}

/**
 * 将表格数据里的自定义字段转换为表格数据二维变一维
 */
export function customFieldDataToTableData(customFieldData: Record<string, any>[], customFields: BugEditCustomField[]) {
  if (!customFieldData || !customFields) return {};

  const tableData: Record<string, any> = {};
  customFieldData.forEach((field) => {
    const customField = customFields.find((item) => item.fieldId === field.id);
    if (!customField) return;
    if (customField.platformOptionJson) {
      // 对jira模板字段做特殊处理
      field.options = JSON.parse(customField.platformOptionJson);
    } else if (customField.options && customField.options.length > 0) {
      field.options = customField.options;
    }
    // 后端返回来的数据这个字段没值
    field.type = customField.type;
    if (selectExcludes.includes(field.type) && Array.isArray(field.options)) {
      tableData[`custom_single_${field.id}`] = field.options.find((option) => option.value === field.value)?.text;
    } else if (field.type === 'MULTIPLE_INPUT' && field.value) {
      // 处理标签形式
      tableData[field.id] = JSON.parse(field.value).join('，') || '-';
    } else if (multipleExcludes.includes(field.type) && Array.isArray(field.options) && field.value) {
      // 多值的类型后端返回的是json字符串
      try {
        field.value = JSON.parse(field.value);
        tableData[`custom_multiple_${field.id}`] = field.value
          .map((val: string) => field.options.find((option: { value: string }) => option.value === val)?.text)
          .join(',');
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('multiple field value is not array!');
      }
    } else if (field.type === 'CASCADER' && field.value) {
      try {
        const arr = JSON.parse(field.value);
        if (arr.length > 1) {
          // 特殊的三方字段-级联选择器
          tableData[field.id] = cascaderValueToLabel(arr, field.options);
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('cascader field value is nor array!');
      }
    } else {
      tableData[field.id] = field.value;
    }
  });
  return tableData;
}

/**
 * 获取模块树多选节点目标Id对应的name列表
 */
/**
 * 获取模块树多选节点目标Id对应的name列表
 * @param trees 属性数组
 * @param targetIds 需要匹配的属性值
 */
export function findNodeNames<T>(trees: TreeNode<T>[], targetIds: string[]) {
  const result: string[] = [];
  // eslint-disable-next-line no-shadow
  function findNameRecursive(node: TreeNode<T>, targetIds: string[]) {
    if (targetIds.includes(node.id)) {
      result.push(node.name);
    }
    if (node.children) {
      node.children.forEach((child) => {
        findNameRecursive(child, targetIds);
      });
    }
  }

  trees.forEach((module) => {
    findNameRecursive(module, targetIds);
  });

  return result;
}

/**
 * 获取每三位使用逗号隔开数字格式
 * @param number 目标值
 */

export function addCommasToNumber(number: number) {
  if (number === 0 || number === undefined) {
    return '0';
  }
  // 将数字转换为字符串
  const numberStr = number.toString();

  // 分割整数部分和小数部分
  const parts = numberStr.split('.');
  const integerPart = parts[0];
  const decimalPart = parts[1] || ''; // 如果没有小数部分，则设为空字符串

  // 对整数部分添加逗号分隔
  const integerWithCommas = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ',');

  // 拼接整数部分和小数部分（如果有）
  const result = decimalPart ? `${integerWithCommas}.${decimalPart}` : integerWithCommas;

  return result;
}

/**
 * 给树添加深度
 * @param tree 目标树
 */
export function addLevelToTree<T>(tree: TreeNode<T>[], level = 0): TreeNode<T>[] {
  if (!tree || !Array.isArray(tree)) {
    return [];
  }

  return tree.map((node) => {
    const newNode = { ...node, level };

    if (newNode.children && newNode.children.length > 0) {
      newNode.children = addLevelToTree(newNode.children, level + 1);
    }

    return newNode;
  });
}
/**
 * 时间转换单位
 * @param ms 目标时间时间
 */
export function formatDuration(ms: number) {
  // 将毫秒转换为秒
  const seconds = ms / 1000;
  // 如果小于1秒，则直接返回毫秒
  if (seconds < 1) return `${ms}-ms`;
  // 如果小于60秒，则返回秒
  if (seconds < 60) return `${seconds}-sec`;
  // 将秒转换为分钟
  const minutes = seconds / 60;
  // 如果小于60分钟，则返回分钟
  if (minutes < 60) return `${minutes.toFixed(1)}-min`;
  // 将分钟转换为小时
  const hours = minutes / 60;
  // 返回小时
  return `${hours.toFixed(1)}-hr`;
}

export const operationWidth = (enWidth: number, zhWidth: number) =>
  localStorage.getItem('MS-locale') === 'en-US' ? enWidth : zhWidth;

/**
 * 下拉树查询检索
 * @param searchValue 搜索关键字
 * @param nodeData 树节点
 */
export function filterTreeNode(searchValue: string, nodeData: MsTreeNodeData, nameKey = 'name') {
  return nodeData[nameKey].toLowerCase().includes(searchValue.toLowerCase());
}

/**
 * 判断是否是mac系统
 */
export function isMacOs() {
  const platform = navigator.platform.toLowerCase();
  return platform.includes('mac');
}
