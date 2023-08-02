import MSR from '@/api/http/index';
import {
  GetMemberListUrl,
  AddMemberUrl,
  UpdateMemberUrl,
  BatchAddProjectUrl,
  BatchAddUserGroupUrl,
  DeleteMemberUrl,
  getUserGroupList,
  getUserList,
  getProjectListUrl,
} from '@/api/requrls/setting/member';
import type { MemberList, AddorUpdateMemberModel, BatchAddProjectModel, LinkItem } from '@/models/setting/member';
import type { TableQueryParams } from '@/models/common';
// 获取成员列表
export function getMemberList(data: TableQueryParams) {
  return MSR.post<MemberList>({ url: GetMemberListUrl, data });
}
// 添加成员
export function addOrUpdate(data: AddorUpdateMemberModel, type: string) {
  if (type === 'add') {
    return MSR.post({ url: AddMemberUrl, data });
  }
  return MSR.post({ url: UpdateMemberUrl, data });
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
export function getUser(organizationId: string) {
  return MSR.get<LinkItem[]>({ url: getUserList, params: organizationId });
}
// 获取组织下边的项目
export function getProjectList(organizationId: string) {
  return MSR.get<LinkItem[]>({ url: getProjectListUrl, params: organizationId });
}
