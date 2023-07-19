import MSR from '@/api/http/index';
import { GetUserListUrl, CreateUserUrl, UpdateUserUrl } from '@/api/requrls/setting/user';
import type { UserListItem, CreateUserParams } from '@/models/setting/user';
import type { TableQueryParams } from '@/models/common';

export function getUserList(data: TableQueryParams) {
  return MSR.post<UserListItem[]>({ url: GetUserListUrl, data });
}

export function batchCreateUser(data: CreateUserParams) {
  return MSR.post({ url: CreateUserUrl, data });
}

export function updateUserInfo(data: UserListItem) {
  return MSR.post({ url: UpdateUserUrl, data });
}
