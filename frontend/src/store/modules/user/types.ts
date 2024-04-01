export type RoleType = '' | '*' | 'admin' | 'user';
export type SystemScopeType = 'PROJECT' | 'ORGANIZATION' | 'SYSTEM';

export interface UserRole {
  createTime: number;
  updateTime: number;
  createUser: string;
  description?: string;
  id: string;
  name: string;
  scopeId: string; // 项目/组织/系统 id
  type: SystemScopeType;
}

export interface permissionsItem {
  id: string;
  permissionId: string;
  roleId: string;
}
export interface UserRoleRelation {
  id: string;
  userId: string;
  roleId: string;
  sourceId: string;
  organizationId: string;
  createTime: number;
  createUser: string;
  userRolePermissions: permissionsItem[];
  userRole: UserRole;
}

export interface UserRolePermissions {
  userRole: UserRole;
  userRolePermissions: permissionsItem[];
}
export interface UserState {
  name?: string;
  avatar?: string;
  job?: string;
  organization?: string;
  location?: string;
  email?: string;
  introduction?: string;
  personalWebsite?: string;
  jobName?: string;
  organizationName?: string;
  locationName?: string;
  phone?: string;
  registrationDate?: string;
  id?: string;
  certification?: number;
  role: RoleType;
  lastOrganizationId?: string;
  lastProjectId?: string;
  userRolePermissions?: UserRolePermissions[];
  userRoles?: UserRole[];
  userRoleRelations?: UserRoleRelation[];
  loginType: string[];
  hasLocalExec?: boolean; // 是否配置了api本地执行
  isPriorityLocalExec?: boolean; // 是否优先本地执行
  localExecuteUrl?: string;
}
