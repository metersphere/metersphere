import MSR from '@/api/http/index';
import {
  updateUserGroupU,
  getUserGroupU,
  addUserGroupU,
  deleteUserGroupU,
  getGlobalUSettingUrl,
  editGlobalUSettingUrl,
  postUserByUserGroupUrl,
  deleteUserFromUserGroupUrl,
} from '@/api/requrls/setting/usergroup';
import { TableQueryParams, CommonList } from '@/models/common';
import { UserGroupItem, UserGroupAuthSeting, SaveGlobalUSettingData, UserTableItem } from '@/models/setting/usergroup';

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

export function getGlobalUSetting(id: string) {
  return MSR.get<UserGroupAuthSeting[]>({ url: `${getGlobalUSettingUrl}${id}` });
}

export function saveGlobalUSetting(data: SaveGlobalUSettingData) {
  return MSR.post<UserGroupAuthSeting[]>({ url: editGlobalUSettingUrl, data });
}

export function postUserByUserGroup(data: TableQueryParams) {
  return MSR.post<CommonList<UserTableItem[]>>({ url: postUserByUserGroupUrl, data });
}

export function deleteUserFromUserGroup(id: string) {
  return MSR.get<string>({ url: `${deleteUserFromUserGroupUrl}${id}` });
}
