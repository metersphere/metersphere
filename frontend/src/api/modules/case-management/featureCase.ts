import MSR from '@/api/http/index';
import {
  AddDemandUrl,
  AddDependOnRelationUrl,
  BatchAssociationDemandUrl,
  BatchCopyCaseUrl,
  BatchDeleteCaseUrl,
  BatchDeleteRecycleCaseListUrl,
  BatchEditCaseUrl,
  BatchMoveCaseUrl,
  CancelAssociationDemandUrl,
  cancelPreAndPostCaseUrl,
  checkFileIsUpdateUrl,
  CreateCaseModuleTreeUrl,
  CreateCaseUrl,
  CreateCommentItemUrl,
  DeleteCaseModuleTreeUrl,
  DeleteCaseUrl,
  DeleteCommentItemUrl,
  deleteFileOrCancelAssociationUrl,
  DeleteRecycleCaseListUrl,
  DetailCaseUrl,
  DownloadFileUrl,
  FollowerCaseUrl,
  GetAssociatedFilePageUrl,
  GetAssociationPublicCaseModuleCountUrl,
  GetAssociationPublicCasePageUrl,
  GetAssociationPublicModuleTreeUrl,
  GetCaseListUrl,
  GetCaseModulesCountUrl,
  GetCaseModuleTreeUrl,
  GetCommentListUrl,
  GetDefaultTemplateFieldsUrl,
  GetDemandListUrl,
  GetDependOnPageUrl,
  GetDependOnRelationUrl,
  GetDetailCaseReviewUrl,
  GetFileIsUpdateUrl,
  GetRecycleCaseListUrl,
  GetRecycleCaseModulesCountUrl,
  GetReviewerListUrl,
  GetSearchCustomFieldsUrl,
  getTransferTreeUrl,
  GetTrashCaseModuleTreeUrl,
  MoveCaseModuleTreeUrl,
  PreviewFileUrl,
  RecoverRecycleCaseListUrl,
  RestoreCaseListUrl,
  TransferFileUrl,
  UpdateCaseModuleTreeUrl,
  UpdateCaseUrl,
  UpdateCommentItemUrl,
  UpdateDemandUrl,
  UploadOrAssociationFileUrl,
} from '@/api/requrls/case-management/featureCase';

import type {
  AssociatedList,
  BatchDeleteType,
  BatchEditCaseType,
  BatchMoveOrCopyType,
  CaseManagementTable,
  CaseModuleQueryParams,
  CommentItem,
  CreateOrUpdate,
  CreateOrUpdateDemand,
  CreateOrUpdateModule,
  DeleteCaseType,
  DemandItem,
  ModulesTreeType,
  OperationFile,
  UpdateModule,
} from '@/models/caseManagement/featureCase';
import type { CommonList, MoveModules, TableQueryParams } from '@/models/common';
import type { UserListItem } from '@/models/setting/user';
// 获取模块树
export function getCaseModuleTree(params: TableQueryParams) {
  return MSR.get<ModulesTreeType[]>({ url: `${GetCaseModuleTreeUrl}/${params.projectId}` });
}

// 创建模块树
export function createCaseModuleTree(data: CreateOrUpdateModule) {
  return MSR.post({ url: CreateCaseModuleTreeUrl, data });
}

// 更新模块树
export function updateCaseModuleTree(data: UpdateModule) {
  return MSR.post({ url: UpdateCaseModuleTreeUrl, data });
}

// 移动模块树
export function moveCaseModuleTree(data: MoveModules) {
  return MSR.post({ url: MoveCaseModuleTreeUrl, data });
}

// 回收站-模块-获取模块树
export function getTrashCaseModuleTree(projectId: string) {
  return MSR.get<ModulesTreeType[]>({ url: `${GetTrashCaseModuleTreeUrl}/${projectId}` });
}

// 删除模块
export function deleteCaseModuleTree(id: string) {
  return MSR.get({ url: `${DeleteCaseModuleTreeUrl}/${id}` });
}

// 用例分页表
export function getCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetCaseListUrl, data });
}
// 删除用例
export function deleteCaseRequest(data: DeleteCaseType) {
  return MSR.post({ url: `${DeleteCaseUrl}`, data });
}
// 获取默认模版自定义字段
export function getCaseDefaultFields(projectId: string) {
  return MSR.get({ url: `${GetDefaultTemplateFieldsUrl}/${projectId}` });
}
// 获取关联文件列表
export function getAssociatedFileListUrl(data: TableQueryParams) {
  return MSR.post<CommonList<AssociatedList>>({ url: GetAssociatedFilePageUrl, data });
}
// 关注用例
export function followerCaseRequest(data: { userId: string; functionalCaseId: string }) {
  return MSR.post({ url: FollowerCaseUrl, data });
}
// 创建用例
export function createCaseRequest(data: Record<string, any>) {
  return MSR.uploadFile({ url: CreateCaseUrl }, { request: data.request, fileList: data.fileList }, '', true);
}
// 编辑用例
export function updateCaseRequest(data: Record<string, any>) {
  return MSR.uploadFile({ url: UpdateCaseUrl }, data, '', true);
}
// 用例详情
export function getCaseDetail(id: string) {
  return MSR.get({ url: `${DetailCaseUrl}/${id}` });
}
// 批量删除用例
export function batchDeleteCase(data: BatchDeleteType) {
  return MSR.post({ url: `${BatchDeleteCaseUrl}`, data });
}
// 批量编辑属性
export function batchEditAttrs(data: BatchEditCaseType) {
  return MSR.post({ url: `${BatchEditCaseUrl}`, data });
}
// 批量移动到模块
export function batchMoveToModules(data: BatchMoveOrCopyType) {
  return MSR.post({ url: `${BatchMoveCaseUrl}`, data });
}

// 批量复制到模块
export function batchCopyToModules(data: BatchMoveOrCopyType) {
  return MSR.post({ url: `${BatchCopyCaseUrl}`, data });
}

// 回收站

// 回收站用例分页表
export function getRecycleListRequest(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetRecycleCaseListUrl, data });
}
// 获取回收站模块数量
export function getRecycleModulesCounts(data: CaseModuleQueryParams) {
  return MSR.post({ url: GetRecycleCaseModulesCountUrl, data });
}
// 获取全部用例模块数量
export function getCaseModulesCounts(data: TableQueryParams) {
  return MSR.post({ url: GetCaseModulesCountUrl, data });
}
// 批量恢复回收站用例表
export function restoreCaseList(data: BatchMoveOrCopyType) {
  return MSR.post({ url: RestoreCaseListUrl, data });
}
// 批量彻底删除回收站用例表
export function batchDeleteRecycleCase(data: BatchMoveOrCopyType) {
  return MSR.post({ url: BatchDeleteRecycleCaseListUrl, data });
}
// 恢复回收站单个用例
export function recoverRecycleCase(id: string) {
  return MSR.get({ url: `${RecoverRecycleCaseListUrl}/${id}` });
}
// 删除回收站单个用例
export function deleteRecycleCaseList(id: string) {
  return MSR.get({ url: `${DeleteRecycleCaseListUrl}/${id}` });
}

// 关联需求

// 已关联需求列表
export function getDemandList(data: TableQueryParams) {
  return MSR.post<CommonList<DemandItem[]>>({ url: GetDemandListUrl, data });
}

// 添加需求
export function addDemandRequest(data: CreateOrUpdateDemand) {
  return MSR.post({ url: AddDemandUrl, data });
}

// 更新需求
export function updateDemand(data: CreateOrUpdateDemand) {
  return MSR.post({ url: UpdateDemandUrl, data });
}
// 批量关联需求
export function batchAssociationDemand(data: CreateOrUpdateDemand) {
  return MSR.post({ url: BatchAssociationDemandUrl, data });
}

// 取消关联
export function cancelAssociationDemand(id: string) {
  return MSR.get({ url: `${CancelAssociationDemandUrl}/${id}` });
}

// 附件

// 上传文件并关联用例
export function uploadOrAssociationFile(data: Record<string, any>) {
  return MSR.uploadFile({ url: UploadOrAssociationFileUrl }, { request: data.request, fileList: [data.file] });
}
// 转存文件
export function transferFileRequest(data: OperationFile) {
  return MSR.post({ url: TransferFileUrl, data });
}
// 获取文件转存目录
export function getTransferFileTree(projectId: string) {
  return MSR.get({ url: `${getTransferTreeUrl}/${projectId}` });
}

// 预览文件
export function previewFile(data: OperationFile) {
  return MSR.post({ url: PreviewFileUrl, data, responseType: 'blob' }, { isTransformResponse: false });
}

// 下载文件
export function downloadFileRequest(data: OperationFile) {
  return MSR.post({ url: DownloadFileUrl, data, responseType: 'blob' }, { isTransformResponse: false });
}
// 检查文件是否更新
export function checkFileIsUpdateRequest(data: string[]) {
  return MSR.post({ url: checkFileIsUpdateUrl, data });
}

// 更新文件
export function updateFile(projectId: string, id: string) {
  return MSR.get({ url: `${GetFileIsUpdateUrl}/${projectId}/${id}` });
}

// 删除文件或取消关联用例文件
export function deleteFileOrCancelAssociation(data: OperationFile) {
  return MSR.post({ url: deleteFileOrCancelAssociationUrl, data });
}
// 高级搜索的自定义字段
export function getCustomFieldsTable(projectId: string) {
  return MSR.get({ url: `${GetSearchCustomFieldsUrl}/${projectId}` });
}

// 评论列表
export function getCommentList(caseId: string) {
  return MSR.get<CommentItem[]>({ url: `${GetCommentListUrl}/${caseId}` });
}

// 创建评论
export function createCommentList(data: CreateOrUpdate) {
  return MSR.post({ url: CreateCommentItemUrl, data });
}

// 创建评论
export function updateCommentList(data: CreateOrUpdate) {
  return MSR.post({ url: UpdateCommentItemUrl, data });
}

// 删除评论
export function deleteCommentList(commentId: string) {
  return MSR.post({ url: `${DeleteCommentItemUrl}/${commentId}` });
}

// 评审
export function getDetailCaseReviewPage(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetDetailCaseReviewUrl, data });
}

// 获取评审人列表
export function getReviewerList(projectId: string, keyword: string) {
  return MSR.get<UserListItem[]>({ url: `${GetReviewerListUrl}/${projectId}`, params: { keyword } });
}

// 用例接口用例分页列表
export function getPublicLinkCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetAssociationPublicCasePageUrl, data });
}

// 获取用例详情接口用例模块数量
export function getPublicLinkCaseModulesCounts(data: TableQueryParams) {
  return MSR.post({ url: GetAssociationPublicCaseModuleCountUrl, data });
}

// 获取关联用例接口模块树
export function getPublicLinkModuleTree(data: TableQueryParams) {
  return MSR.post<ModulesTreeType[]>({ url: `${GetAssociationPublicModuleTreeUrl}`, data });
}

// 获取前后置用例
export function getDependOnCase(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: `${GetDependOnPageUrl}`, data });
}
// 用例管理-功能用例-用例详情-前后置关系
export function getPrepositionRelation(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: `${GetDependOnRelationUrl}`, data });
}
// 添加前后置关系
export function addPrepositionRelation(data: TableQueryParams) {
  return MSR.post<ModulesTreeType[]>({ url: `${AddDependOnRelationUrl}`, data });
}
// 取消依赖关系
export function cancelPreOrPostCase(id: string) {
  return MSR.get({ url: `${cancelPreAndPostCaseUrl}/${id}` });
}
export default {};
