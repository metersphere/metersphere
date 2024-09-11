// 项目下拉列表
export type ItemMap = {
  name: string;
  id: string;
};
// 成员项
export interface MemberItem {
  id: string;
  name: string;
  email: string;
  password: string;
  enable: boolean; // 状态
  createTime: string | number;
  updateTime: string | number;
  language: string;
  lastOrganizationId: string; // 选中组织
  phone: string;
  source: string;
  lastProjectId: string[]; // 选中项目
  createUser: string;
  updateUser: string;
  deleted: boolean;
  // 项目
  projectIdNameMap: ItemMap[];
  // 用户组
  userRoleIdNameMap: ItemMap[];
  // 编辑模式用户组
  showUserSelect?: boolean;
  // 编辑模式项目
  showProjectSelect?: boolean;
  // 添加到用户组下拉收集的用户组id 列表
  selectUserList: string[];
  // 添加到用户组下拉收集的项目id 列表
  selectProjectList: string[];
}
// 成员列表
export type MemberList = MemberItem[];
// 添加成员
export interface AddOrUpdateMemberModel {
  id?: string;
  organizationId?: string;
  memberIds?: string[];
  userRoleIds?: string[];
  projectIds?: string[];
  memberId?: string;
}
// 添加组织成员到项目
export interface BatchAddProjectModel {
  organizationId?: string;
  memberIds?: Array<string | number>;
  projectIds?: string[] | string;
  userRoleIds?: string[] | string;
}
// 用户组下拉列表

export interface LinkItem {
  id: string;
  name: string;
  disabled?: boolean;
}
export type LinkList = LinkItem[];

export interface InviteOrgMemberParams {
  inviteEmails: string[];
  userRoleIds: string[];
  organizationId: string;
}
