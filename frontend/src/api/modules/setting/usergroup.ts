import MSR from '@/api/http/index';
import * as ugUrl from '@/api/requrls/setting/usergroup';

import { CommonList, TableQueryParams } from '@/models/common';
import {
  OrgUserGroupParams,
  SaveGlobalUSettingData,
  SystemUserGroupParams,
  UserGroupAuthSetting,
  UserGroupItem,
  UserTableItem,
} from '@/models/setting/usergroup';

// 系统-创建或修改用户组
export function updateOrAddUserGroup(data: SystemUserGroupParams) {
  return MSR.post<UserGroupItem>({
    url: data.id ? ugUrl.updateUserGroupU : ugUrl.addUserGroupU,
    data,
  });
}
// 组织-创建或修改用户组
export function updateOrAddOrgUserGroup(data: OrgUserGroupParams) {
  return MSR.post<UserGroupItem>({
    url: data.id ? ugUrl.updateOrgUserGroupU : ugUrl.addOrgUserGroupU,
    data,
  });
}

export function updateSocpe(data: Partial<SystemUserGroupParams>) {
  return MSR.post<UserGroupItem>({
    url: ugUrl.updateUserGroupU,
    data,
  });
}
// 系统-获取用户组列表
export function getUserGroupList() {
  return MSR.get<UserGroupItem[]>({ url: ugUrl.getUserGroupU });
}
// 组织-获取用户组列表
export function getOrgUserGroupList(organizationId: string) {
  return MSR.get<UserGroupItem[]>({ url: `${ugUrl.getOrgUserGroupU}${organizationId}` });
}
// 项目-获取用户组列表
export function getProjectUserGroupList(projectId: string) {
  return MSR.get<UserGroupItem[]>({ url: `${ugUrl.getProjectUserGroupU}${projectId}` });
}
// 系统-删除用户组
export function deleteUserGroup(id: string) {
  return MSR.get<string>({ url: `${ugUrl.deleteUserGroupU}${id}` });
}

// 组织-删除用户组
export function deleteOrgUserGroup(id: string) {
  return MSR.get<string>({ url: `${ugUrl.deleteOrgUserGroupU}${id}` });
}

export function getUsergroupInfo(id: string) {
  return MSR.get<UserGroupItem>({ url: `${ugUrl.getUserGroupU}${id}` });
}

// 系统-获取用户组对应的权限配置
export function getGlobalUSetting(id: string) {
  return MSR.get<UserGroupAuthSetting[]>({ url: `${ugUrl.getGlobalUSettingUrl}${id}` });
}
// 组织-获取用户组对应的权限配置
export function getOrgUSetting(id: string) {
  return MSR.get<UserGroupAuthSetting[]>({ url: `${ugUrl.getOrgUSettingUrl}${id}` });
}

// 系统-编辑用户组对应的权限配置
export function saveGlobalUSetting(data: SaveGlobalUSettingData) {
  return MSR.post<UserGroupAuthSetting[]>({ url: ugUrl.editGlobalUSettingUrl, data });
}

// 系统-获取需要关联的用户选项
export function getSystemUserGroupOption(id: string, keyword: string) {
  return MSR.get<UserTableItem[]>({ url: `${ugUrl.getSystemUserGroupOptionUrl}${id}`, params: { keyword } });
}

// 组织-获取需要关联的用户选项
export function getOrgUserGroupOption(organizationId: string, roleId: string, keyword: string) {
  return MSR.get<UserTableItem[]>({
    url: `${ugUrl.getOrgUserGroupOptionUrl}${organizationId}/${roleId}`,
    params: { keyword },
  });
}

// 组织-编辑用户组对应的权限配置
export function saveOrgUSetting(data: SaveGlobalUSettingData) {
  return MSR.post<UserGroupAuthSetting[]>({ url: ugUrl.editOrgUSettingUrl, data });
}

// 系统-获取用户组对应的用户列表
export function postUserByUserGroup(data: TableQueryParams) {
  return MSR.post<CommonList<UserTableItem[]>>({ url: ugUrl.postUserByUserGroupUrl, data });
}
// 组织-获取用户组对应的用户列表
export function postOrgUserByUserGroup(data: TableQueryParams) {
  return MSR.post<CommonList<UserTableItem[]>>({ url: ugUrl.postOrgUserByUserGroupUrl, data });
}

// 系统-删除用户组对应的用户
export function deleteUserFromUserGroup(id: string) {
  return MSR.get<string>({ url: `${ugUrl.deleteUserFromUserGroupUrl}${id}` });
}
// 组织-删除用户组对应的用户
export function deleteOrgUserFromUserGroup(data: { userRoleId: string; userIds: string[]; organizationId: string }) {
  return MSR.post<CommonList<UserTableItem[]>>({ url: ugUrl.deleteOrgUserFromUserGroupUrl, data });
}
// 系统-添加用户到用户组
export function addUserToUserGroup(data: { roleId: string; userIds: string[] }) {
  return MSR.post<string>({ url: ugUrl.addUserToUserGroupUrl, data });
}
// 组织-添加用户到用户组
export function addOrgUserToUserGroup(data: { userRoleId: string; userIds: string[]; organizationId: string }) {
  return MSR.post<string>({ url: ugUrl.addOrgUserToUserGroupUrl, data });
}
