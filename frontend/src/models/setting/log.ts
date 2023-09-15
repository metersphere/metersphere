import type { PathMapRoute } from '@/config/pathMap';

interface Sort {
  [key: string]: string;
}

interface Combine {
  [key: string]: any;
}

interface Filter {
  [key: string]: string[];
}

export interface LogListParams {
  keyword: string;
  filter: Filter;
  combine: Combine;
  current: number;
  pageSize: number;
  sort: Sort;
  operUser: string; // 操作人
  startTime: number;
  endTime: number;
  projectIds: string[]; // 项目 id 集合
  organizationIds: string[]; // 组织 id 集合
  type: string; // 操作类型
  module: string; // 操作对象
  content: string; // 操作名称
  level: string; // 系统/组织/项目级别
  sortString: string;
}

export interface OptionsItem {
  id: string;
  name: string;
}

export interface LogOptions {
  organizationList: OptionsItem[];
  projectList: OptionsItem[];
}

export interface LogItem {
  id: string;
  createUser: string;
  userName: string; // 操作人
  projectId: string;
  projectName: string;
  organizationId: string;
  organizationName: string;
  module: PathMapRoute; // 操作对象
  type: string; // 操作类型
  content: string; // 操作名称
  createTime: number;
  sourceId: string;
}

export interface UserItem {
  id: string;
  name: string;
  email: string;
  password: string;
  enable: boolean;
  createTime: number;
  updateTime: number;
  language: string;
  lastOrganizationId: string;
  phone: string;
  source: string;
  lastProjectId: string;
  createUser: string;
  updateUser: string;
  deleted: boolean;
}
