export interface AdminList {
  id: string;
  name: string;
  email: string;
  password: string;
  enable: boolean;
  createTime: string;
  updateTime: number;
  language: string;
  lastOrganizationId: string;
  phone: string;
  source: string;
  lastProjectId: string;
  createUser: string;
  updateUser: string;
  deleted: boolean; // 是否删除
  adminFlag: boolean; // 是否组织/项目管理员
  memberFlag: boolean; // 是否组织/项目成员
  checkRoleFlag: boolean; // 是否属于用户组
  sourceId: string; // 资源id
}

export interface ProjectBasicInfoModel {
  id: string;
  num: number;
  organizationId: string;
  name: string;
  description: string;
  createTime: string;
  updateTime: number;
  updateUser: string;
  createUser: string;
  deleteTime: number;
  deleted: boolean;
  deleteUser: string;
  enable: boolean;
  moduleSetting: string; // 模块设置
  memberCount: number; // 项目成员数量
  organizationName: string;
  adminList: AdminList[]; // 管理员
  projectCreateUserIsAdmin: boolean; // 创建人是否是管理员
  moduleIds: string[];
  resourcePoolList: { name: string; id: string }[]; // 资源池列表
}

export interface UpdateProject {
  organizationId?: string;
  name: string;
  description: string;
  enable?: boolean;
  moduleIds?: string[]; // 模块设置
  id?: string;
  userIds?: string[]; // 成员数
}
