import { TableQueryParams } from '@/models/common';

export interface CreateOrUpdateSystemOrgParams {
  id?: string;
  name: string;
  description: string;
  memberIds: string[];
}
export interface CreateOrUpdateSystemProjectParams {
  id?: string;
  // 项目名称
  name: string;
  // 项目描述
  description: string;
  // 启用或禁用
  enable: boolean;
  // 项目成员
  userIds: string[];
  // 模块配置
  moduleIds?: string[];
  // 所属组织
  organizationId?: string;
  // 列表里的
}

export interface SystemOrgOption {
  id: string;
  name: string;
}
export interface SystemGetUserByOrgOrProjectIdParams extends TableQueryParams {
  projectId?: string;
  organizationId?: string;
}
