import MSR from '@/api/http/index';
import {
  AddMemberUrl,
  BatchAddProjectUrl,
  BatchAddUserGroupUrl,
  DeleteMemberUrl,
  GetMemberListUrl,
  GetOrganizationMemberListPageUrl,
  getProjectListUrl,
  getUserGroupList,
  getUserList,
  inviteOrgMemberUrl,
  UpdateMemberUrl,
  UpdateSystemOrganizationMemberUrl,
} from '@/api/requrls/setting/member';

import type { CommonList, TableQueryParams } from '@/models/common';
import type {
  AddOrUpdateMemberModel,
  BatchAddProjectModel,
  InviteOrgMemberParams,
  LinkItem,
  MemberItem,
} from '@/models/setting/member';
// 获取成员列表
export function getMemberList(data: TableQueryParams) {
  return MSR.post<CommonList<MemberItem>>({ url: GetMemberListUrl, data });
}
// 添加成员
export function addOrUpdate(data: AddOrUpdateMemberModel, type: string) {
  if (type === 'add') {
    return MSR.post({ url: AddMemberUrl, data });
  }
  return MSR.post({ url: UpdateMemberUrl, data });
}
// 系统设置-系统-组织与项目-组织-成员-更新成员用户组
export function updateSystemOrganizationMember(data: AddOrUpdateMemberModel) {
  return MSR.post({ url: UpdateSystemOrganizationMemberUrl, data });
}
// 添加到项目
export function batchAddProject(data: BatchAddProjectModel) {
  return MSR.post({ url: BatchAddProjectUrl, data });
}
// 添加到用户组
export function batchAddUserGroup(data: BatchAddProjectModel) {
  return MSR.post({ url: BatchAddUserGroupUrl, data });
}
// 移除成员
export function deleteMemberReq(organizationId: string, userId: string) {
  return MSR.get({ url: DeleteMemberUrl, params: `${organizationId}/${userId}` });
}
// 获取用户组下拉
export function getGlobalUserGroup(organizationId: string) {
  return MSR.get({ url: getUserGroupList, params: organizationId });
}
// 获取系统用户下拉
export function getUser(organizationId: string, keyword: string) {
  return MSR.get<LinkItem[]>({ url: `${getUserList}/${organizationId}`, params: { keyword } });
}
// 获取组织下边的项目
export function getProjectList(organizationId: string, keyword?: string) {
  return MSR.get<LinkItem[]>({ url: `${getProjectListUrl}/${organizationId}`, params: { keyword } });
}
// 添加到用户组
export function inviteOrgMember(data: InviteOrgMemberParams) {
  return MSR.post({ url: inviteOrgMemberUrl, data }, { isReturnNativeResponse: true });
}
// 系统设置-组织-项目-分页获取成员列表
export function getOrganizationMemberListPage(data: TableQueryParams) {
  return MSR.post({ url: GetOrganizationMemberListPageUrl, data });
}
