import MSR from '@/api/http/index';
import * as ugUrl from '@/api/requrls/project-management/usergroup';

import { CommonList, TableQueryParams } from '@/models/common';
import {
  SaveGlobalUSettingData,
  SystemUserGroupParams,
  UserGroupAuthSetting,
  UserGroupItem,
  UserTableItem,
} from '@/models/setting/usergroup';

// 项目-创建或修改用户组
export function updateOrAddProjectUserGroup(data: SystemUserGroupParams) {
  return MSR.post<UserGroupItem>({
    url: data.id ? ugUrl.updateUrl : ugUrl.addUrl,
    data,
  });
}

// 项目-获取用户组列表
export function postUserGroupList(data: TableQueryParams) {
  return MSR.post<CommonList<UserGroupItem>>({ url: ugUrl.listUserGroupUrl, data });
}

// 项目-删除用户组
export function deleteUserGroup(id: string) {
  return MSR.get<string>({ url: `${ugUrl.deleteUrl}${id}` });
}

// 项目-编辑用户组对应的权限配置
export function saveProjectUGSetting(data: SaveGlobalUSettingData) {
  return MSR.post<UserGroupAuthSetting[]>({ url: ugUrl.updatePermissionUrl, data });
}

// 项目-获取用户组对应的权限
export function getAuthByUserGroup(id: string) {
  return MSR.get<UserGroupAuthSetting[]>({ url: ugUrl.listPermissionUrl + id });
}

// 项目-获取用户组对应的用户列表
export function postUserByUserGroup(data: TableQueryParams) {
  return MSR.post<CommonList<UserTableItem[]>>({ url: ugUrl.listMemberUrl, data });
}

// 项目-删除用户组对应的用户
export function deleteUserFromUserGroup(data: { projectId: string; userRoleId: string; userIds: string[] }) {
  return MSR.post<CommonList<UserTableItem[]>>({ url: ugUrl.removeMemberUrl, data });
}

// 项目-获取需要关联的用户选项
export function getProjectUserGroupOptions(projectId: string, userRoleId: string, keyword: string) {
  return MSR.get<UserTableItem[]>({
    url: `${ugUrl.getMemberOptionsUrl}${projectId}/${userRoleId}`,
    params: { keyword },
  });
}

// 项目-添加用户到用户组
export function addUserToUserGroup(data: { projectId: string; userRoleId: string; userIds: string[] }) {
  return MSR.post<string>({ url: ugUrl.addMemberUrl, data });
}
