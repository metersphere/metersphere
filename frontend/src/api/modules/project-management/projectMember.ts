import MSR from '@/api/http/index';
import {
  GetProjectMemberListUrl,
  AddMemberToProjectUrl,
  EditProjectMemberUrl,
  BatchAddUserGroup,
  BatchRemoveMemberUrl,
  RemoveProjectMemberUrl,
  ProjectUserGroupUrl,
  ProjectMemberOptions,
} from '@/api/requrls/project-management/projectMember';
import type { ProjectMemberItem, ActionProjectMember } from '@/models/projectManagement/projectAndPermission';
import type { TableQueryParams, CommonList } from '@/models/common';

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

// 添加项目成员到用户组
export function addProjectUserGroup(data: ActionProjectMember) {
  return MSR.post({ url: BatchAddUserGroup, data });
}

// 批量移除项目成员
export function batchRemoveMember(data: ActionProjectMember) {
  return MSR.post({ url: BatchRemoveMemberUrl, data });
}

// 移除项目成员
export function removeProjectMember(projectId: string, userId: string) {
  return MSR.get({ url: RemoveProjectMemberUrl, params: `${projectId}/${userId}` });
}

// 获取用户组下拉
export function getProjectUserGroup(projectId: string) {
  return MSR.get({ url: ProjectUserGroupUrl, params: projectId });
}

// 项目成员下拉选项
export function getProjectMemberOptions(projectId: string) {
  return MSR.get({ url: ProjectMemberOptions, params: projectId });
}
