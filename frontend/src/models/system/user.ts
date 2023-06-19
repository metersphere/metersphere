export interface UserRoleListItem {
  id: string;
  name: string;
  description: string;
  internal: boolean;
  type: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  scopeId: string;
  pos: number;
}

export interface OrganizationList {
  id: string;
  num: number;
  name: string;
  description: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  updateUser: string;
  deleted: boolean;
  deleteUser: string;
  deleteTime: number;
  enable: boolean;
}

export interface UserListItem {
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
  organizationList: OrganizationList[];
  userRoleList: UserRoleListItem[];
}

export interface Filter {
  [key: string]: any;
}

export interface Sort {
  [key: string]: any;
}

// 创建用户模型
export interface CreateUserInfo {
  name: string;
  email: string;
  phone?: string;
}

export interface CreateUserParams {
  userInfoList: CreateUserInfo[];
  userRoleIdList: string[];
}
