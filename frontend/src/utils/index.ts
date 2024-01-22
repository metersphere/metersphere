import JSEncrypt from 'jsencrypt';

import { codeCharset } from '@/config/apiTest';

import { isObject } from './is';

type TargetContext = '_self' | '_parent' | '_blank' | '_top';

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
  const formattedSize = size.toFixed(2);
  const unit = units[unitIndex];

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
export function characterLimit(str?: string): string {
  if (!str) return '';
  if (str.length <= 20) {
    return str;
  }
  return `${str.slice(0, 20 - 3)}...`;
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
  [key: string]: any;
  children?: TreeNode<T>[];
}

/**
 * 递归遍历树形数组或树
 * @param tree 树形数组或树
 * @param customNodeFn 自定义节点函数
 * @param customChildrenKey 自定义子节点的key
 * @returns 遍历后的树形数组
 */
export function mapTree<T>(
  tree: TreeNode<T> | TreeNode<T>[] | T | T[],
  customNodeFn: (node: TreeNode<T>) => TreeNode<T> | null = (node) => node,
  customChildrenKey = 'children'
): T[] {
  if (!Array.isArray(tree)) {
    tree = [tree];
  }

  return tree
    .map((node: TreeNode<T>) => {
      const newNode = typeof customNodeFn === 'function' ? customNodeFn(node) : node;

      if (newNode && newNode[customChildrenKey] && newNode[customChildrenKey].length > 0) {
        newNode[customChildrenKey] = mapTree(newNode[customChildrenKey], customNodeFn, customChildrenKey);
      }

      return newNode;
    })
    .filter(Boolean);
}

/**
 * 根据属性 key 查找树形数组中匹配的某个节点
 * @param trees 属性数组
 * @param targetKey 需要匹配的属性值
 * @param customKey 默认为 key，可自定义需要匹配的属性名
 * @returns 匹配的节点/null
 */
export function findNodeByKey<T>(trees: TreeNode<T>[], targetKey: string, customKey = 'key'): TreeNode<T> | T | null {
  for (let i = 0; i < trees.length; i++) {
    const node = trees[i];
    if (node[customKey] === targetKey) {
      return node; // 如果当前节点的 key 与目标 key 匹配，则返回当前节点
    }

    if (Array.isArray(node.children) && node.children.length > 0) {
      const _node = findNodeByKey(node.children, targetKey, customKey); // 递归在子节点中查找
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

/**
 * 生成 id 序列号
 * @returns
 */
export const getGenerateId = () => {
  const timestamp = new Date().getTime().toString();
  const randomDigits = Math.floor(Math.random() * 10000)
    .toString()
    .padStart(4, '0');
  const generateId = timestamp + randomDigits;
  return generateId.substring(0, 16);
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
 * 转换字符串的字符集编码
 * @param str 需要转换的字符串
 * @param charset 字符集编码
 */
export function decodeStringToCharset(str: string, charset = 'UTF-8') {
  const encoder = new TextEncoder();
  const decoder = new TextDecoder(charset);
  return decoder.decode(encoder.encode(str));
}
