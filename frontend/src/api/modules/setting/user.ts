import MSR from '@/api/http/index';
import {
  GetUserListUrl,
  CreateUserUrl,
  UpdateUserUrl,
  DeleteUserUrl,
  ImportUserUrl,
  EnableUserUrl,
  GetSystemRoleUrl,
  ResetPasswordUrl,
  BatchAddUserGroupUrl,
  BatchAddOrgUrl,
  BatchAddProjectUrl,
  GetOrgsUrl,
  GetProjectsUrl,
} from '@/api/requrls/setting/user';
import type {
  UserListItem,
  CreateUserParams,
  UpdateUserInfoParams,
  UpdateUserStatusParams,
  DeleteUserParams,
  ImportUserParams,
  SystemRole,
  ImportResult,
  BatchAddParams,
  ResetUserPasswordParams,
  OrgsItem,
} from '@/models/setting/user';
import type { CommonList, TableQueryParams } from '@/models/common';

// 获取用户列表
export function getUserList(data: TableQueryParams) {
  return MSR.post<CommonList<UserListItem>>({ url: GetUserListUrl, data });
}

// 批量创建用户
export function batchCreateUser(data: CreateUserParams) {
  return MSR.post({ url: CreateUserUrl, data });
}

// 更新用户信息
export function updateUserInfo(data: UpdateUserInfoParams) {
  return MSR.post({ url: UpdateUserUrl, data });
}

// 更新用户启用/禁用状态
export function toggleUserStatus(data: UpdateUserStatusParams) {
  return MSR.post({ url: EnableUserUrl, data });
}

// 删除用户
export function deleteUserInfo(data: DeleteUserParams) {
  return MSR.post({ url: DeleteUserUrl, data });
}

// 导入用户
export function importUserInfo(data: ImportUserParams) {
  return MSR.uploadFile<ImportResult>({ url: ImportUserUrl }, data);
}

// 获取系统用户组
export function getSystemRoles() {
  return MSR.get<SystemRole[]>({ url: GetSystemRoleUrl });
}

// 重置用户密码
export function resetUserPassword(data: ResetUserPasswordParams) {
  return MSR.post({ url: ResetPasswordUrl, data });
}

// 批量添加用户到多个用户组
export function batchAddUserGroup(data: BatchAddParams) {
  return MSR.post({ url: BatchAddUserGroupUrl, data });
}

// 批量添加用户到多个项目
export function batchAddProject(data: BatchAddParams) {
  return MSR.post({ url: BatchAddProjectUrl, data });
}

// 批量添加用户到多个组织
export function batchAddOrg(data: BatchAddParams) {
  return MSR.post({ url: BatchAddOrgUrl, data });
}

// 获取系统组织组
export function getSystemOrgs() {
  return MSR.get<OrgsItem[]>({ url: GetOrgsUrl });
}

// 获取系统项目
export function getSystemProjects() {
  return MSR.get<OrgsItem[]>({ url: GetProjectsUrl });
}
