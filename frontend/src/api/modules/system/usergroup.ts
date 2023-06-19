import MSR from '@/api/http/index';
import { updateUserGroupU, getUserGroupU, addUserGroupU, deleteUserGroupU } from '@/api/requrls/usergroup';
// import { QueryParams, CommonList } from '@/models/common';
import { UserGroupItem } from '@/components/bussiness/usergroup/type';

export function updateOrAddUserGroup(data: Partial<UserGroupItem>) {
  return MSR.post<UserGroupItem>({
    url: data.id ? updateUserGroupU : addUserGroupU,
    data,
  });
}

export function updateSocpe(data: Partial<UserGroupItem>) {
  return MSR.post<UserGroupItem>({
    url: updateUserGroupU,
    data,
  });
}

export function getUserGroupList() {
  return MSR.get<UserGroupItem[]>({ url: getUserGroupU });
}

export function deleteUserGroup(id: string) {
  return MSR.get<string>({ url: `${deleteUserGroupU}${id}` });
}

export function getUsergroupInfo(id: string) {
  return MSR.get<UserGroupItem>({ url: `${getUserGroupU}${id}` });
}
