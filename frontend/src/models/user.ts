import { UserState } from '@/store/modules/user/types';

// 登录信息
export interface LoginData {
  username: string;
  password: string;
  authenticate: string;
}

// 登录返回
export interface LoginRes extends UserState {
  csrfToken: string;
  sessionId: string;
  token: string;
}
// 企业微信对接信息
export interface WecomInfo {
  corpId?: string;
  agentId?: string;
  state?: string;
  callBack?: string;
}

// 企业微信对接信息
export interface DingInfo {
  appKey?: string;
  state?: string;
  callBack?: string;
}

// 飞书对接信息
export interface LarkInfo {
  agentId?: string;
  state?: string;
  callBack?: string;
}

// 更新本地执行配置
export interface UpdateLocalConfigParams {
  id: string;
  userUrl: string;
}
// 本地执行配置类型
export type LocalConfigType = 'API' | 'UI';
// 添加本地执行
export interface AddLocalConfigParams {
  userUrl: string;
  type: LocalConfigType;
}
// 本地执行配置
export interface LocalConfig {
  id: string;
  userUrl: string;
  enable: boolean;
  type: LocalConfigType;
  createUser?: string;
}
// 更新 APIKEY
export interface UpdateAPIKEYParams {
  id: string;
  forever?: boolean;
  expireTime?: number;
  description: string;
}
// APIKEY
export interface APIKEY {
  id: string;
  createUser: string;
  accessKey: string;
  secretKey: string;
  createTime: number;
  enable: boolean;
  forever: boolean;
  expireTime: number;
  description: string;
}
// 更新密码入参
export interface UpdatePswParams {
  id: string;
  oldPassword: string;
  newPassword: string;
}

export interface Permission {
  id: string;
  roleId: string;
  permissionId: string;
}

export interface Resource {
  id: string;
  name: string;
  license: boolean;
}
// 个人信息
export interface PersonalOrganization {
  id: string;
  num: number;
  organizationId: string;
  name: string;
  description: string;
  createTime: number;
  updateTime: number;
  updateUser: string;
  createUser: string;
  deleteTime: number;
  deleted: boolean;
  deleteUser: string;
  enable: boolean;
  moduleSetting: string;
}
export interface PersonalProject {
  projectId: string;
  projectName: string;
}
export interface OrganizationProjectListItem {
  orgId: string;
  orgName: string;
  projectList: PersonalProject[];
}
export interface PersonalInfo {
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
  avatar: string;
  orgProjectList: OrganizationProjectListItem[];
}
export interface UpdateBaseInfo {
  id: string;
  username: string;
  phone: string;
  email: string;
  avatar: string;
}

export interface UpdateLanguage {
  language: string;
}

export interface OrgOptionItem {
  id: string;
  name: string;
}
