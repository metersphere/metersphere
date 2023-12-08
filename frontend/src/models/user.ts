// 登录信息
export interface LoginData {
  username: string;
  password: string;
  authenticate: string;
}

export interface UserRole {
  id: string;
  createTime: number;
  createUser: string;
  roleId: string;
  sourceId: string;
  userId: string;
}

// 登录返回
export interface LoginRes {
  csrfToken: string;
  createTime: number;
  createUser: string;
  email: string;
  enabled: boolean;
  id: string;
  language: string;
  lastOrganizationId: string;
  lastProjectId: string;
  name: string;
  phone: string;
  platformInfo: string;
  seleniumServer: string;
  sessionId: string;
  source: string;
  updateTime: number;
  updateUser: string;
  userRolePermissions: UserRole[];
  userRoleRelations: UserRole[];
  userRoles: UserRole[];
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
  forever: boolean;
  expireTime: number;
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
export interface UserRolePermission {
  resource: Resource;
  permissions: Permission[];
  type: string;
  userRole: UserRole;
  userRolePermissions: Permission[];
}

export interface UserRoleRelation {
  id: string;
  userId: string;
  roleId: string;
  sourceId: string;
  organizationId: string;
  createTime: number;
  createUser: string;
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
