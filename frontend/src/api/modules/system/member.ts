import MSR from '@/api/http/index';
import { GetMemberListUrl, AddMemberUrl } from '@/api/requrls/system/member';
import type { UserListItem, CreateUserParams } from '@/models/system/user';
import type { TableQueryParams } from '@/models/common';

export function getMemberList(data: TableQueryParams) {
  return MSR.post<UserListItem[]>({ url: GetMemberListUrl, data });
}

export function batchCreateUser(data: CreateUserParams) {
  return MSR.post({ url: AddMemberUrl, data });
}
