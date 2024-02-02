import type { BatchApiParams } from '@/models/common';

// 用户所属用户组模型
export interface UserRoleListItem {
  id: string;
  name: string;
  description: string;
  internal: boolean; // 是否内置用户组
  type: string; // 所属类型 SYSTEM ORGANIZATION PROJECT
  createTime: number;
  updateTime: number;
  createUser: string;
  scopeId: string; // 应用范围
}
// 用户所属组织模型
export interface OrganizationListItem {
  id: string;
  num: number;
  name: string;
  description: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  updateUser: string;
  deleted: boolean; // 是否删除
  deleteUser: string;
  deleteTime: number;
  enable: boolean; // 是否启用
}

// 用户模型
export interface UserListItem {
  id: string;
  name: string;
  email: string;
  password: string;
  enable: boolean;
  createTime: number;
  updateTime: number;
  language: string; // 语言
  lastOrganizationId: string; // 当前组织ID
  phone: string;
  source: string; // 来源：LOCAL OIDC CAS OAUTH2
  lastProjectId: string; // 当前项目ID
  createUser: string;
  updateUser: string;
  organizationList: OrganizationListItem[]; // 用户所属组织
  userRoleList: UserRoleListItem[]; // 用户所属用户组
  userRoles?: UserRoleListItem[]; // 用户所属用户组
}

export interface Filter {
  [key: string]: any;
}

export interface Sort {
  [key: string]: any;
}

// 创建用户模型
export interface SimpleUserInfo {
  id?: string;
  name: string;
  email: string;
  phone?: string;
}

export interface UpdateUserInfoParams extends SimpleUserInfo {
  id: string;
  userRoleIdList: string[];
}

export interface CreateUserParams {
  userInfoList: SimpleUserInfo[];
  userRoleIdList: string[];
}
export interface UpdateUserStatusParams extends BatchApiParams {
  enable: boolean;
}

export interface ImportUserParams {
  fileList: (File | undefined)[];
}

export type DeleteUserParams = BatchApiParams;
export type ResetUserPasswordParams = BatchApiParams;

export interface SystemRole {
  id: string;
  name: string;
  selected: boolean; // 是否可选
  closeable: boolean; // 是否可取消
}

export interface ImportResult {
  importCount: number;
  successCount: number;
  errorMessages: Record<string, any>;
}

export interface BatchAddParams extends BatchApiParams {
  roleIds: string[]; // 用户组/项目/组织 id 集合
}

export interface OrgsItem {
  id: string;
  name: string;
  children?: OrgsItem[];
  leafNode: boolean;
}

export interface InviteUserParams {
  inviteEmails: string[];
  userRoleIds: string[];
}

export interface RegisterByInviteParams {
  inviteId: string;
  name: string;
  password: string;
  phone: string;
}

export interface CreateUserResult {
  errorEmails: Record<string, any>;
  successList: any[];
}
