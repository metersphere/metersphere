import MSR from '@/api/http/index';
import * as orgUrl from '@/api/requrls/setting/system/organizationAndProject';
import { TableQueryParams } from '@/models/common';
import { AddUserToOrgOrProjectParams } from '@/models/setting/systemOrg';
import {
  CreateOrUpdateSystemOrgParams,
  CreateOrUpdateSystemProjectParams,
} from '@/models/setting/system/orgAndProject';

// 获取组织列表
export function postOrgTable(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postOrgTableUrl, data });
}

// 创建或修改组织
export function createOrUpdateOrg(data: CreateOrUpdateSystemOrgParams | CreateOrUpdateSystemProjectParams) {
  return MSR.post({ url: data.id ? orgUrl.postModifyOrgUrl : orgUrl.postAddOrgUrl, data });
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

// 获取项目列表
export function postProjectTable(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postProjectTableUrl, data });
}

// 根据组织id获取项目列表
export function postProjectTableByOrgId(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postProjectTableByOrgUrl, data });
}

// 根据组织id获取用户列表
export function postUserTableByOrgId(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postOrgMemberUrl, data });
}
// 给组织或项目添加成员
export function addUserToOrgOrProject(data: AddUserToOrgOrProjectParams) {
  return MSR.post({ url: data.projectId ? orgUrl.postAddProjectMemberUrl : orgUrl.postAddOrgMemberUrl, data });
}
// 获取用户下拉选项
export function getUserByOrganizationOrProject(sourceId: string) {
  return MSR.get({ url: `${orgUrl.getUserByOrgOrProjectUrl}${sourceId}` });
}
// 删除组织或项目成员
export function deleteUserFromOrgOrProject(sourceId: string, userId: string, isOrg = true) {
  return MSR.get({
    url: `${isOrg ? orgUrl.getDeleteOrgMemberUrl : orgUrl.getDeleteProjectMemberUrl}${sourceId}/${userId}`,
  });
}

// TODO: 等待后端同学的接口 启用或禁用项目
export function enableOrDisableProject(id: string, isEnable = true) {
  return MSR.get({ url: `${isEnable ? orgUrl.getEnableProjectUrl : orgUrl.getDisableProjectUrl}${id}` });
}
