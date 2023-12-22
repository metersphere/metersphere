import MSR from '@/api/http/index';
import * as bugURL from '@/api/requrls/bug-management';

import { BugEditFormObject, BugExportParams, BugListItem } from '@/models/bug-management';
import { AssociatedList } from '@/models/caseManagement/featureCase';
import { CommonList, TableQueryParams, TemplateOption } from '@/models/common';

/**
 * 表格的查询
 * @param data
 * @returns
 */
export function getBugList(data: TableQueryParams) {
  return MSR.post<CommonList<BugListItem>>({ url: bugURL.postTableListUrl, data });
}
/**
 * 更新Bug
 * @param data
 * @returns
 */
export function updateBug(data: { request: BugEditFormObject; fileList: File[] }) {
  return MSR.uploadFile({ url: bugURL.postUpdateBugUrl }, data, '', true);
}
/**
 * 批量更新
 * @param data
 * @returns
 */
export function updateBatchBug(data: TableQueryParams) {
  return MSR.post({ url: bugURL.postBatchUpdateBugUrl, data });
}
/**
 * 创建Bug
 * @param data
 * @returns
 */
export function createBug(data: { request: BugEditFormObject; fileList: File[] }) {
  return MSR.uploadFile({ url: bugURL.postCreateBugUrl }, data, '', true);
}
/**
 * 获取 bug 详情
 */
export function getBugDetail(id: string) {
  return MSR.get({ url: `${bugURL.getBugDetailUrl}${id}` });
}

export function deleteSingleBug(data: TableQueryParams) {
  return MSR.get({ url: `${bugURL.getDeleteBugUrl}${data.id}` });
}

export function deleteBatchBug(data: TableQueryParams) {
  return MSR.post({ url: bugURL.postBatchDeleteBugUrl, data });
}

export function getTemplageOption(params: { projectId: string }) {
  return MSR.get<TemplateOption[]>({ url: bugURL.getTemplageOption, params });
}

export function getTemplateById(data: TableQueryParams) {
  return MSR.post({ url: bugURL.getTemplateUrl, data });
}
// 获取导出字段配置
export function getExportConfig(projectId: string) {
  return MSR.get({ url: `${bugURL.getExportConfigUrl}${projectId}` });
}
// 获取模版详情
export function getTemplateDetailInfo(data: DefaultTemplate) {
  return MSR.post({ url: `${bugURL.getTemplateDetailUrl}`, data });
}

// 同步缺陷
export function syncBugOpenSource(params: { projectId: string }) {
  return MSR.get({ url: bugURL.getSyncBugOpenSourceUrl, params });
}

// 导出缺陷
export function exportBug(data: BugExportParams) {
  return MSR.post({ url: bugURL.postExportBugUrl, data });
}
// 获取关联文件列表
export function getAssociatedFileList(data: TableQueryParams) {
  return MSR.post<CommonList<AssociatedList>>({ url: bugURL.postAssociatedFileListUrl, data });
}
