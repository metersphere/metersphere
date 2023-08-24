export interface UserGroupListItem {
  name: string;
  id: number;
  title?: string;
  authScope: string;
}
export interface UserOption {
  id: number;
  name: string;
  email: string;
}
export interface CustomMoreActionItem {
  eventKey: string;
  name: string;
}
export interface PopVisibleItem {
  [key: string]: boolean;
}

export type RenameType = 'rename' | 'auth';

export interface UserGroupItem {
  // 组ID
  id: string;
  // 组名称
  name: string;
  // 组描述
  description: string;
  // 是否是内置用户组
  internal: true;
  // 所属类型
  type: string;
  createTime: number;
  updateTime: number;
  // 创建人
  createUser: string;
  // 应用范围
  scopeId: string;
  // 自定义排序
  pos: number;
}

export interface SystemUserGroupParams {
  id?: string; // 组ID
  name?: string; // 名称
  scopeId?: string; // 组织ID
  type?: string; // 组类型：SYSTEM | PROJECT | ORGANIZATION
}
export interface OrgUserGroupParams {
  id?: string; // 组ID
  name: string;
  scopeId: string; // 组织ID
}

export interface UserGroupPermissionItem {
  id: string;
  name: string;
  enable: boolean;
  license: boolean;
}

export type AuthScopeType = 'SYSTEM' | 'PROJECT' | 'ORGANIZATION';

// 用户组对应的权限配置
export interface UserGroupAuthSetting {
  // 菜单项ID
  id: AuthScopeType;
  // 菜单所属类型
  type?: string;
  // 菜单项名称
  name: string;
  // 是否企业版
  license: boolean;
  // 是否全选
  enable: boolean;
  // 菜单下的权限列表
  permissions?: UserGroupPermissionItem[];
  // 子菜单
  children?: UserGroupAuthSetting[];
}

// 权限表格DataItem
export interface AuthTableItem {
  id: string;
  name?: string;
  enable: boolean;
  license: boolean;
  ability?: string | undefined;
  permissions?: UserGroupPermissionItem[];
  // 对应表格权限的复选框组的绑定值
  perChecked?: string[];
  operationObject?: string;
  isSystem?: boolean;
  isOrganization?: boolean;
  isProject?: boolean;
  indeterminate?: boolean;
}
export interface SavePermissions {
  id: string;
  enable: boolean;
}
export interface SaveGlobalUSettingData {
  userRoleId: string;
  permissions: SavePermissions[];
}

export interface UserTableItem {
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
