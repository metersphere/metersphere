import MSR from '@/api/http/index';
import {
  AddMemberToProjectUrl,
  BatchAddUserGroup,
  BatchRemoveMemberUrl,
  EditProjectMemberUrl,
  GetProjectMemberListUrl,
  ProjectMemberCommentOptions,
  ProjectMemberInviteUrl,
  ProjectMemberList,
  ProjectMemberOptions,
  ProjectUserGroupUrl,
  RemoveProjectMemberUrl,
  UpdateProjectMemberUrl,
} from '@/api/requrls/project-management/projectMember';

import { ReviewUserItem } from '@/models/caseManagement/caseReview';
import type { CommonList, TableQueryParams } from '@/models/common';
import type {
  ActionProjectMember,
  InviteMemberParams,
  ProjectMemberItem,
  ProjectUserOption,
} from '@/models/projectManagement/projectAndPermission';

// 获取项目成员列表
export function getProjectMemberList(data: TableQueryParams) {
  return MSR.post<CommonList<ProjectMemberItem>>({ url: GetProjectMemberListUrl, data });
}

// 添加项目成员&编辑项目成员
export function addOrUpdateProjectMember(data: ActionProjectMember) {
  if (data.userId) {
    return MSR.post({ url: EditProjectMemberUrl, data });
  }
  return MSR.post({ url: AddMemberToProjectUrl, data });
}
// 系统设置-系统-组织与项目-项目-更新成员用户组
export function updateProjectMember(data: ActionProjectMember) {
  return MSR.post({ url: UpdateProjectMemberUrl, data });
}

// 添加项目成员到用户组
export function addProjectUserGroup(data: ActionProjectMember) {
  return MSR.post({ url: BatchAddUserGroup, data });
}

// 批量移除项目成员
export function batchRemoveMember(data: TableQueryParams) {
  return MSR.post({ url: BatchRemoveMemberUrl, data });
}

// 移除项目成员
export function removeProjectMember(projectId: string, userId: string) {
  return MSR.get({ url: RemoveProjectMemberUrl, params: `${projectId}/${userId}` });
}

// 获取用户组下拉
export function getProjectUserGroup(projectId: string) {
  return MSR.get<ProjectUserOption[]>({ url: ProjectUserGroupUrl, params: projectId });
}

// 项目成员下拉选项
export function getProjectMemberOptions(projectId: string, keyword?: string) {
  return MSR.get({ url: `${ProjectMemberOptions}/${projectId}`, params: { keyword } });
}
// 项目成员下拉选项不包含组织
export function getProjectOptions(projectId: string, keyword?: string) {
  return MSR.get({ url: `${ProjectMemberList}/${projectId}`, params: { keyword } });
}

// 项目成员-@成员下拉选项
export function getProjectMemberCommentOptions(projectId: string, keyword?: string) {
  return MSR.get<ReviewUserItem[]>({
    url: `${ProjectMemberCommentOptions}/${projectId}`,
    params: { keyword },
  });
}

// 邀请成员
export function inviteMember(data: InviteMemberParams) {
  return MSR.post({ url: ProjectMemberInviteUrl, data }, { isReturnNativeResponse: true });
}
