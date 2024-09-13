import { TreeNodeData } from '@arco-design/web-vue';

import type { FilterResult } from '@/components/pure/ms-advance-filter/type';

import { RequestMethods } from '@/enums/apiEnum';

// 请求返回结构
export default interface CommonResponse<T> {
  code: number;
  message: string;
  messageDetail: string;
  data: T;
}

// 表格查询
export interface TableQueryParams {
  // 当前页
  current?: number;
  // 每页条数
  pageSize?: number;
  // 排序仅针对单个字段
  sort?: object;
  // 排序仅针对单个字段
  sortString?: string;
  // 表头筛选
  filter?: object;
  // 查询条件
  keyword?: string;
  [key: string]: any;
}
export interface CommonList<T> {
  [x: string]: any;
  pageSize: number;
  total: number;
  current: number;
  list: T[];
}

export interface TemplateOption {
  id: string;
  name: string;
  enableDefault: boolean;
}

export interface BatchApiParams {
  selectIds: string[]; // 已选 ID 集合，当 selectAll 为 false 时接口会使用该字段
  excludeIds?: string[]; // 需要忽略的用户 id 集合，当selectAll为 true 时接口会使用该字段
  selectAll: boolean; // 是否跨页全选，即选择当前筛选条件下的全部表格数据
  condition: Record<string, any>; // 当前表格查询的筛选条件
  currentSelectCount?: number; // 当前已选择的数量
  projectId?: string; // 项目 ID
  moduleIds?: (string | number)[]; // 模块 ID 集合
  versionId?: string; // 版本 ID
  refId?: string; // 版本来源
  protocols?: string[]; // 协议集合
  combineSearch?: FilterResult;
}

// 移动模块树
export interface MoveModules {
  dragNodeId: string; // 被拖拽的节点
  dropNodeId: string; // 放入的节点
  dropPosition: number; // 放入的位置（取值：-1，,0，,1。 -1：dropNodeId节点之前。 0:dropNodeId节点内。 1：dropNodeId节点后）
}
// 添加模块参数
export interface AddModuleParams {
  projectId: string;
  name: string;
  parentId: string;
}
// 模块树节点
export interface ModuleTreeNode extends TreeNodeData {
  id: string;
  name: string;
  type: 'MODULE' | 'API';
  children: ModuleTreeNode[];
  attachInfo: {
    method?: keyof typeof RequestMethods;
    protocol: string;
  }; // 附加信息
  count: 0;
  parentId: string;
  path: string;
}
// 拖拽排序
export type MoveMode = 'BEFORE' | 'AFTER' | 'APPEND';
export interface DragSortParams {
  projectId: string;
  targetId: string;
  moveMode: MoveMode; // 拖拽类型
  moveId: string;
  moduleId?: string;
}
// 文件转存入参
export interface TransferFileParams {
  projectId: string;
  sourceId?: string | number;
  fileName?: string;
  fileId: string;
  local: true;
  moduleId: string;
  originalName?: string;
  [x: string]: any;
}
