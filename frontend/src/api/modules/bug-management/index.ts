import MSR from '@/api/http/index';
import * as bugURL from '@/api/requrls/bug-management';

import { BugListItem } from '@/models/bug-management';
import { CommonList, TableQueryParams, TemplateOption } from '@/models/common';

/**
 * 表格的查询
 * @param data
 * @returns
 */
export function getBugList(data: TableQueryParams) {
  return MSR.post<CommonList<BugListItem>>({ url: bugURL.postTableListUrl, data });
}

export function updateBug(data: TableQueryParams) {
  return MSR.post({ url: bugURL.postUpdateBugUrl, data });
}

export function updateBatchBug(data: TableQueryParams) {
  return MSR.post({ url: bugURL.postBatchUpdateBugUrl, data });
}

export function createBug(data: TableQueryParams) {
  return MSR.post({ url: bugURL.postCreateBugUrl, data });
}

export function deleteBug(data: TableQueryParams) {
  return MSR.get({ url: bugURL.getDeleteBugUrl, data });
}

export function deleteBatchBug(data: TableQueryParams) {
  return MSR.post({ url: bugURL.postBatchDeleteBugUrl, data });
}

export function getTemplageOption(params: { projectId: string }) {
  return MSR.get<TemplateOption[]>({ url: bugURL.getTemplageOption, params });
}

export function getTemplateById(data: TableQueryParams) {
  return MSR.get({ url: bugURL.getTemplateUrl, data });
}
