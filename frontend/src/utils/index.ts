import { Recordable } from '#/global';
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
  tree: TreeNode<T> | TreeNode<T>[],
  customNodeFn: (node: TreeNode<T>) => TreeNode<T> | null = (node) => node,
  customChildrenKey = 'children'
): TreeNode<T>[] {
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
