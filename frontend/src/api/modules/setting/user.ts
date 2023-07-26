import MSR from '@/api/http/index';
import {
  GetUserListUrl,
  CreateUserUrl,
  UpdateUserUrl,
  DeleteUserUrl,
  ImportUserUrl,
  EnableUserUrl,
  GetSystemRoleUrl,
} from '@/api/requrls/setting/user';
import type {
  UserListItem,
  CreateUserParams,
  UpdateUserInfoParams,
  UpdateUserStausParams,
  DeleteUserParams,
  ImportUserParams,
  SystemRole,
  ImportResult,
} from '@/models/setting/user';
import type { TableQueryParams } from '@/models/common';

// 获取用户列表
export function getUserList(data: TableQueryParams) {
  return MSR.post<UserListItem[]>({ url: GetUserListUrl, data });
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
export function toggleUserStatus(data: UpdateUserStausParams) {
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
  return MSR.get<SystemRole>({ url: GetSystemRoleUrl });
}
