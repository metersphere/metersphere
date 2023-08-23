import type { RouteEnum } from '@/enums/routeEnum';

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
  module: typeof RouteEnum; // 操作对象
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
