import { CommentParams } from '@/components/business/ms-comment/types';

import MSR from '@/api/http/index';
import * as bugURL from '@/api/requrls/bug-management';

import { BugEditFormObject, BugExportParams, BugListItem } from '@/models/bug-management';
import { AssociatedList, OperationFile } from '@/models/caseManagement/featureCase';
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
export function createOrUpdateBug(data: { request: BugEditFormObject; fileList: File[] }) {
  return MSR.uploadFile({ url: data.request.id ? bugURL.postUpdateBugUrl : bugURL.postCreateBugUrl }, data, '', true);
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
// 获取模板详情
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

export function getCustomFieldHeader(projectId: string) {
  return MSR.get({ url: `${bugURL.getCustomFieldHeaderUrl}${projectId}` });
}

// 附件

// 上传文件并关联用例
export function uploadOrAssociationFile(data: Record<string, any>) {
  return MSR.uploadFile({ url: bugURL.uploadOrAssociationFileUrl }, { request: data.request, fileList: [data.file] });
}
// 转存文件
export function transferFileRequest(data: OperationFile) {
  return MSR.post({ url: bugURL.transferFileUrl, data });
}
// 获取文件转存目录
export function getTransferFileTree(projectId: string) {
  return MSR.get({ url: `${bugURL.getTransferTreeUrl}/${projectId}` });
}

// 预览文件
export function previewFile(data: OperationFile) {
  return MSR.post({ url: bugURL.previewFileUrl, data, responseType: 'blob' }, { isTransformResponse: false });
}

// 下载文件
export function downloadFileRequest(data: OperationFile) {
  return MSR.post({ url: bugURL.downloadFileUrl, data, responseType: 'blob' }, { isTransformResponse: false });
}
// 检查文件是否更新
export function checkFileIsUpdateRequest(data: string[]) {
  return MSR.post({ url: bugURL.checkFileIsUpdateUrl, data });
}

// 更新文件
export function updateFile(projectId: string, id: string) {
  return MSR.get({ url: `${bugURL.getFileIsUpdateUrl}/${projectId}/${id}` });
}

// 删除文件或取消关联用例文件
export function deleteFileOrCancelAssociation(data: OperationFile) {
  return MSR.post({ url: bugURL.deleteFileOrCancelAssociationUrl, data });
}

// 获取文件列表
export function getAttachmentList(bugId: string) {
  return MSR.get({ url: `${bugURL.getAttachmentListUrl}${bugId}` });
}
// 富文本编辑器上传图片文件
export function editorUploadFile(data: { fileList: File[] }) {
  return MSR.uploadFile({ url: bugURL.editorUploadFileUrl }, { fileList: data.fileList }, '', false);
}
