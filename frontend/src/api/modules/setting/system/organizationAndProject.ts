import MSR from '@/api/http/index';
import * as orgUrl from '@/api/requrls/setting/system/organizationAndProject';
import { TableQueryParams } from '@/models/common';

// 获取组织列表
export function postOrgTable(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postOrgTableUrl, data });
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
