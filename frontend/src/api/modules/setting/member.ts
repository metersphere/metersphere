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
import type { MemberList, AddorUpdateMemberModel, BatchAddProjectModel } from '@/models/setting/member';
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
// 添加到用户
export function batchAddUserGroup(data: AddorUpdateMemberModel) {
  return MSR.post({ url: BatchAddUserGroupUrl, data });
}
// 移除成员
export function deleteMemberReq(organizationId: string, userId: string) {
  return MSR.get({ url: DeleteMemberUrl, params: `/${organizationId}/${userId}` });
}
// 获取全局用户组下拉
export function getGlobalUserGroup() {
  return MSR.get({ url: getUserGroupList });
}
// 获取用户下拉
export function getUser() {
  return MSR.get({ url: getUserList });
}
// 【暂用】获取组织下边的项目
export function getProjectList(data: any) {
  return MSR.post({ url: getProjectListUrl, data });
}
