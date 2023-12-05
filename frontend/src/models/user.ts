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
