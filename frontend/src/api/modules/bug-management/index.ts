import { CommentParams } from '@/components/business/ms-comment/types';

import MSR from '@/api/http/index';
import * as bugURL from '@/api/requrls/bug-management';

import { BugEditFormObject, BugListItem, BugOptionListItem } from '@/models/bug-management';
import { AssociatedList, DemandItem, OperationFile } from '@/models/caseManagement/featureCase';
import { CommonList, TableQueryParams, TemplateOption } from '@/models/common';

/**
 * 校验缺陷是否存在
 *
 */
export function checkBugExist(id: string) {
  return MSR.get({ url: `${bugURL.checkBugExist}${id}` });
}

/**
 * 表格的查询
 * @param data
 * @returns
 */
export function getBugList(data: TableQueryParams) {
  return MSR.post<CommonList<BugListItem>>({ url: bugURL.postTableListUrl, data });
}

/**
 * 表格筛选字段的数据查询
 * @param data
 */
export function getCustomOptionHeader(projectId: string) {
  return MSR.get<BugOptionListItem>({ url: `${bugURL.getCustomOptionHeaderUrl}${projectId}` });
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

// 获取当前项目所属平台
export function getPlatform(projectId: string) {
  return MSR.get({ url: `${bugURL.getPlatform}${projectId}` });
}

// 同步缺陷开源
export function syncBugOpenSource(projectId: string) {
  return MSR.get({ url: bugURL.getSyncBugOpenSourceUrl + projectId });
}
// 同步缺陷企业版
export function syncBugEnterprise(data: { projectId: string; pre: boolean; createTime: number }) {
  return MSR.post({ url: bugURL.getSyncBugEnterpriseUrl, data });
}

// 获取同步状态
export function getSyncStatus(projectId: string) {
  return MSR.get<{ complete: boolean; msg: string }>({ url: bugURL.getSyncStatusUrl + projectId });
}

// 导出缺陷
export function exportBug(data: TableQueryParams) {
  return MSR.post<BlobPart>(
    { url: bugURL.postExportBugUrl, data, responseType: 'blob' },
    { isTransformResponse: false }
  );
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
  if (data.fetchType === 'UPDATE') {
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
export function updateFile(data: OperationFile) {
  return MSR.post({ url: bugURL.getFileIsUpdateUrl, data });
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

// --------------------回收站
// 获取回收站列表
export function getRecycleList(data: TableQueryParams) {
  return MSR.post<CommonList<BugListItem>>({ url: bugURL.getRecycleListUrl, data });
}
// 单个恢复
export function recoverSingleByRecycle(id: string) {
  return MSR.get({ url: `${bugURL.getRecoverSingleUrl}${id}` });
}
// 批量恢复
export function recoverBatchByRecycle(data: TableQueryParams) {
  return MSR.post({ url: bugURL.getBatchRecoverUrl, data });
}
// 删除
export function deleteSingleByRecycle(id: string) {
  return MSR.get({ url: `${bugURL.getDeleteSingleUrl}${id}` });
}
// 批量删除
export function deleteBatchByRecycle(data: TableQueryParams) {
  return MSR.post({ url: bugURL.getBatchDeleteUrl, data });
}

// ----------------------关联需求

// 已关联用例列表
export function getAssociatedList(data: TableQueryParams) {
  return MSR.post<CommonList<DemandItem[]>>({ url: bugURL.getDemandListUrl, data });
}

// 缺陷管理-关联用例-未关联用例-列表分页
export function getUnAssociatedList(data: TableQueryParams) {
  return MSR.post({ url: bugURL.getUnrelatedDemandListUrl, data });
}

// 未关联用例-模块树
export function getModuleTree(data: TableQueryParams) {
  return MSR.post({ url: `${bugURL.getUnrelatedModuleTreeUrl}`, data });
}

// 未关联用例-模块树-统计
export function getModuleTreeCounts(data: TableQueryParams) {
  return MSR.post({ url: `${bugURL.getUnrelatedModuleTreeCountUrl}`, data });
}

// 批量关联需求
export function batchAssociation(data: TableQueryParams) {
  return MSR.post({ url: bugURL.postAddDemandUrl, data });
}

// 取消关联
export function cancelAssociation(id: string) {
  return MSR.get({ url: `${bugURL.getCancelDemandUrl}/${id}` });
}

// 缺陷管理-变更历史-列表
export function getChangeHistoryList(data: TableQueryParams) {
  return MSR.post({ url: bugURL.getChangeHistoryListUrl, data });
}

// 校验跳转用例权限
export function checkCasePermission(projectId: string, caseType: string) {
  return MSR.get({ url: `${bugURL.checkCasePermissionUrl}/${projectId}/${caseType}` });
}
