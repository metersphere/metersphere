import { CommentParams } from '@/components/business/ms-comment/types';

import MSR from '@/api/http/index';
import * as bugURL from '@/api/requrls/bug-management';

import { BugEditFormObject, BugExportParams, BugListItem, CreateOrUpdateComment } from '@/models/bug-management';
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
/** 删除单个文件 */
export function deleteSingleBug(data: TableQueryParams) {
  return MSR.get({ url: `${bugURL.getDeleteBugUrl}${data.id}` });
}
/** 批量删除文件 */
export function deleteBatchBug(data: TableQueryParams) {
  return MSR.post({ url: bugURL.postBatchDeleteBugUrl, data });
}
/** 获取模板 Option */
export function getTemplateOption(projectId: string) {
  return MSR.get<TemplateOption[]>({ url: `${bugURL.getTemplateOption}/${projectId}` });
}
/** 获取模板详情 */
export function getTemplateById(data: TableQueryParams) {
  return MSR.post({ url: bugURL.getTemplateUrl, data });
}
// 获取导出字段配置
export function getExportConfig(projectId: string) {
  return MSR.get({ url: `${bugURL.getExportConfigUrl}${projectId}` });
}
// 获取模版详情
export function getTemplateDetailInfo(data: { id: string; projectId: string }) {
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

// 关注/取消关注 缺陷
export function followBug(id: string, isFollow: boolean) {
  if (isFollow) {
    return MSR.get({ url: `${bugURL.getUnFollowBugUrl}${id}` });
  }
  return MSR.get({ url: `${bugURL.getFollowBugUrl}${id}` });
}

// 创建评论
export function createOrUpdateComment(data: CommentParams) {
  if (data.id) {
    return MSR.post({ url: bugURL.postUpdateCommentUrl, data });
  }
  return MSR.post({ url: bugURL.postCreateCommentUrl, data });
}
// 获取评论列表
export function getCommentList(bugId: string) {
  return MSR.get({ url: `${bugURL.getCommentListUrl}${bugId}` });
}
// 删除评论
export function deleteComment(commentId: string) {
  return MSR.get({ url: `${bugURL.getDeleteCommentUrl}${commentId}` });
}
