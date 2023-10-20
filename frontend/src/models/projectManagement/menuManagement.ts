import { TableQueryParams } from '../common';

export interface MenuTableConfigItem {
  [key: string]: any;
}
export interface MenuTableListItem {
  module: string;
  moduleEnable: boolean;
  moduleDesc?: string;
  children?: MenuTableConfigItem[];
  type?: string;
}

export interface MenuTableListParams {
  projectId: string;
  type?: string;
  typeValue?: boolean | string;
}
export interface PoolOption {
  id: string;
  name: string;
}

export type SelectValue =
  | string
  | number
  | boolean
  | Record<string, any>
  | (string | number | boolean | Record<string, any>)[];

export interface FakeTableListItem {
  name: string;
  enable?: boolean;
  label: string[];
  rule: string;
  creator: string;
  updateDate: string;
  type: string | string[];
  id?: string;
  projectId?: string;
  typeList?: string[];
}
export interface FakeTableOperationParams {
  projectId: string;
  excludeIds?: string[]; // 排除的id
  selectIds?: string[]; // 选中的id
  selectAll: boolean; // 是否跨页全选
  params?: TableQueryParams; // 查询参数
  enable?: boolean; // 是否启用
  condition?: TableQueryParams; // 条件
}
