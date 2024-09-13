import { MsUserSelectorOption } from '@/components/business/ms-user-selector/types';

import MSR from '@/api/http/index';
import * as orgUrl from '@/api/requrls/setting/organizationAndProject';

import { CommonList, TableQueryParams } from '@/models/common';
import {
  CreateOrUpdateOrgProjectParams,
  CreateOrUpdateSystemOrgParams,
  CreateOrUpdateSystemProjectParams,
  OrgProjectTableItem,
  SystemGetUserByOrgOrProjectIdParams,
} from '@/models/setting/system/orgAndProject';
import { AddUserToOrgOrProjectParams } from '@/models/setting/systemOrg';

// 组织与项目-公共
// 系统-组织及项目，获取管理员下拉选项
export function getAdminByOrgOrProject() {
  return MSR.get({ url: orgUrl.getAdminByOrgOrProjectUrl });
}

// 系统-组织

// 获取组织列表
export function postOrgTable(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postOrgTableUrl, data });
}

// 创建或修改组织
export function createOrUpdateOrg(data: CreateOrUpdateSystemOrgParams | CreateOrUpdateSystemProjectParams) {
  return MSR.post({ url: data.id ? orgUrl.postModifyOrgUrl : orgUrl.postAddOrgUrl, data });
}

// 修改组织名称
export function modifyOrgName(data: { id: string; name: string }) {
  return MSR.post({ url: orgUrl.postModifyOrgNameUrl, data });
}

// 删除组织
export function deleteOrg(id: string) {
  return MSR.get({ url: `${orgUrl.getDeleteOrgUrl}${id}` });
}

// 删除项目
export function deleteProject(id: string) {
  return MSR.get({ url: `${orgUrl.getDeleteProjectUrl}${id}` });
}

// 撤销删除组织
export function revokeDeleteOrg(id: string) {
  return MSR.get({ url: `${orgUrl.getRecoverOrgUrl}${id}` });
}

// 撤销删除项目
export function revokeDeleteProject(id: string) {
  return MSR.get({ url: `${orgUrl.getRevokeProjectUrl}${id}` });
}

// 启用或禁用组织
export function enableOrDisableOrg(id: string, isEnable = true) {
  return MSR.get({ url: `${isEnable ? orgUrl.getEnableOrgUrl : orgUrl.getDisableOrgUrl}${id}` });
}

// 系统-项目

// 系统-获取项目列表
export function postProjectTable(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postProjectTableUrl, data });
}

// 根据组织id获取项目列表
export function postProjectTableByOrgId(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postProjectTableByOrgUrl, data });
}

// 根据 orgId 或 projectId 获取用户列表
export function postUserTableByOrgIdOrProjectId(data: SystemGetUserByOrgOrProjectIdParams) {
  return MSR.post({ url: data.organizationId ? orgUrl.postOrgMemberUrl : orgUrl.postProjectMemberUrl, data });
}
// 给组织或项目添加成员
export function addUserToOrgOrProject(data: AddUserToOrgOrProjectParams) {
  return MSR.post({ url: data.projectId ? orgUrl.postAddProjectMemberUrl : orgUrl.postAddOrgMemberUrl, data });
}
// 获取用户下拉选项
export function getUserByOrganizationOrProject(sourceId: string, keyword: string) {
  return MSR.get({ url: `${orgUrl.getUserByOrgOrProjectUrl}${sourceId}`, params: { keyword } });
}
// 系统设置-系统-组织与项目-获取添加成员列表
export function getSystemMemberListPage(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.getMemberListPageUrl, data });
}

// 系统-获取管理员下拉选项
export function getAdminByOrganizationOrProject(keyword: string) {
  return MSR.get({ url: `${orgUrl.getAdminByOrgOrProjectUrl}`, params: { keyword } });
}
// 删除组织或项目成员
export function deleteUserFromOrgOrProject(sourceId: string, userId: string, isOrg = true) {
  return MSR.get({
    url: `${isOrg ? orgUrl.getDeleteOrgMemberUrl : orgUrl.getDeleteProjectMemberUrl}${sourceId}/${userId}`,
  });
}

// 启用或禁用项目
export function enableOrDisableProject(id: string, isEnable = true) {
  return MSR.get({ url: `${isEnable ? orgUrl.getEnableProjectUrl : orgUrl.getDisableProjectUrl}${id}` });
}

// 获取组织下拉选项
export function getSystemOrgOption() {
  return MSR.post({ url: orgUrl.postOrgOptionsUrl }, { ignoreCancelToken: true });
}

// 创建或更新项目
export function createOrUpdateProject(data: Partial<OrgProjectTableItem>) {
  return MSR.post({ url: data.id ? orgUrl.postModifyProjectUrl : orgUrl.postAddProjectUrl, data });
}
// 修改项目名称
export function renameProject(data: { id: string; name: string; organizationId: string }) {
  return MSR.post({ url: orgUrl.postModifyProjectNameUrl, data });
}

// 创建项目或组织时获取所有用户
export function getAllUser() {
  return MSR.get({ url: orgUrl.getOrgOrProjectAdminUrl });
}

// 获取项目和组织的总数
export function getOrgAndProjectCount() {
  return MSR.get({ url: orgUrl.getOrgAndProjectCountUrl });
}

// 组织-项目
// 组织-获取项目列表
export function postProjectTableByOrg(data: TableQueryParams) {
  return MSR.post<CommonList<OrgProjectTableItem>>({ url: orgUrl.postProjectTableByOrgIdUrl, data });
}

// 组织-启用或禁用项目
export function enableOrDisableProjectByOrg(id: string, isEnable = true) {
  return MSR.get({ url: `${isEnable ? orgUrl.getEnableProjectByOrgUrl : orgUrl.getDisableProjectByOrgUrl}${id}` });
}

// 组织-删除项目
export function deleteProjectByOrg(id: string) {
  return MSR.get({ url: `${orgUrl.getDeleteProjectByOrgUrl}${id}` });
}

// 组织-撤销删除项目
export function revokeDeleteProjectByOrg(id: string) {
  return MSR.get({ url: `${orgUrl.getRecoverProjectByOrgUrl}${id}` });
}

// 组织-创建或更新项目
export function createOrUpdateProjectByOrg(data: CreateOrUpdateOrgProjectParams) {
  return MSR.post({ url: data.id ? orgUrl.postModifyProjectByOrgUrl : orgUrl.postAddProjectByOrgUrl, data });
}

// 修改项目名称
export function renameProjectByOrg(data: { id: string; name: string; organizationId: string }) {
  return MSR.post({ url: orgUrl.postModifyProjectNameByOrgUrl, data });
}

// 组织-获取项目下的成员列表
export function postProjectMemberByProjectId(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postProjectMemberByOrgIdUrl, data });
}

// 组织-移除项目成员
export function deleteProjectMemberByOrg(projectId: string, userId: string) {
  return MSR.get({ url: `${orgUrl.getDeleteProjectMemberByOrgUrl}${projectId}/${userId}` });
}

// 组织-添加项目成员
export function addProjectMemberByOrg(data: AddUserToOrgOrProjectParams) {
  return MSR.post({ url: orgUrl.postAddProjectMemberByOrgUrl, data });
}

// 组织-获取项目下的管理员选项
export function getAdminByProjectByOrg(organizationId: string, keyword: string) {
  return MSR.get<MsUserSelectorOption[]>({
    url: `${orgUrl.getAdminByOrganizationOrProjectUrl}${organizationId}`,
    params: { keyword },
  });
}

// 组织-获取成员下的成员选项
export function getUserByProjectByOrg(organizationId: string, projectId: string, keyword: string) {
  return MSR.get({
    url: `${orgUrl.getUserByOrganizationOrProjectUrl}${organizationId}/${projectId}`,
    params: { keyword },
  });
}

// 系统或组织-获取项目下的资源池options
export function getPoolOptionsByOrgOrSystem(modulesIds: string[], organizationId?: string) {
  return MSR.post({
    url: orgUrl.getProjectPoolByOrgOrSystemUrl,
    data: { organizationId, modulesIds },
  });
}
// 组织-获取项目下的资源池options
export function getPoolOptionsByOrg(modulesIds: string[], organizationId?: string) {
  return MSR.post({
    url: orgUrl.getProjectPoolByOrg,
    data: { organizationId, modulesIds },
  });
}
