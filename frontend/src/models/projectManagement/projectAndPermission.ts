export interface ProjectTreeData {
  key: string;
  title: string;
  children?: ProjectTreeData[];
}

export interface UserGroupItem {
  id: string;
  name: string;
  description: string;
  internal: boolean;
  type: string;
  createTime: number | string;
  updateTime: number | string;
  createUser: string;
  scopeId: string;
}
export interface ProjectMemberItem {
  id?: string;
  name: string;
  email: string;
  password: string;
  enable: boolean; // 是否启用
  createTime: number | string;
  updateTime: number | string;
  language: string;
  lastOrganizationId: string; // 组织id
  phone: string;
  source: string;
  lastProjectId: string; // 项目id
  createUser: string;
  updateUser: string;
  deleted: true; // 是否被删除
  userRoles: UserGroupItem[];
  showUserSelect?: boolean; // 是否可以选择
  selectUserList?: string[]; // 已选择项目用户组
}

export interface ActionProjectMember {
  userId?: string;
  projectId?: string; // 项目ID
  userIds?: (string | number)[] | string; // 用户ID集合
  roleIds?: (string | number)[] | string; // 用户组ID集合
}

export interface ProjectUserOption {
  id: string;
  name: string;
}

export interface SearchParams {
  filter: {
    roleIds: string[];
  };
  projectId: string;
  keyword: '';
}

export interface AddProjectMember {
  projectId?: string;
  userIds: string[];
  roleIds: string[] | string;
}

export interface InviteMemberParams {
  inviteEmails: string[];
  userRoleIds: string[];
  organizationId: string;
  projectId: string;
}
