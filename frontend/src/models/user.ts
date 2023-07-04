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
