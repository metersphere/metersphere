import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';
import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';

import MSR from '@/api/http/index';
import {
  AddDemandUrl,
  AddDependOnRelationUrl,
  AssociatedDebuggerUrl,
  associatedProjectOptionsUrl,
  BatchAssociationDemandUrl,
  BatchCopyCaseUrl,
  BatchDeleteCaseUrl,
  BatchDeleteRecycleCaseListUrl,
  BatchEditCaseUrl,
  BatchMoveCaseUrl,
  CancelAssociatedDebuggerUrl,
  CancelAssociationDemandUrl,
  cancelDisassociate,
  cancelPreAndPostCaseUrl,
  CaseAiBatchSaveUrl,
  CaseAiChatUrl,
  CaseAiTransformUrl,
  CheckCaseExportTaskUrl,
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
  DownloadExcelTemplateUrl,
  DownloadFileUrl,
  DownloadXMindTemplateUrl,
  dragSortUrl,
  EditorUploadFileUrl,
  ExportExcelCaseUrl,
  exportExcelCheckUrl,
  ExportXMindCaseUrl,
  exportXMindCheckUrl,
  FollowerCaseUrl,
  GetAiConfigUrl,
  GetAssociatedCaseIdsUrl,
  GetAssociatedDebuggerUrl,
  GetAssociatedDrawerCaseUrl,
  GetAssociatedFilePageUrl,
  GetAssociatedTestPlanUrl,
  GetAssociationPublicCaseModuleCountUrl,
  GetAssociationPublicCasePageUrl,
  GetAssociationPublicModuleTreeUrl,
  GetCaseDownloadFileUrl,
  GetCaseExportConfigUrl,
  GetCaseListUrl,
  GetCaseMinderTreeUrl,
  GetCaseMinderUrl,
  GetCaseModulesCountUrl,
  GetCaseModuleTreeUrl,
  getChangeHistoryListUrl,
  GetCommentListUrl,
  GetDebugDrawerPageUrl,
  GetDefaultTemplateFieldsUrl,
  GetDemandListUrl,
  GetDependOnPageUrl,
  GetDependOnRelationUrl,
  GetDetailCaseReviewUrl,
  GetFileIsUpdateUrl,
  GetPlanExecuteCommentListUrl,
  GetRecycleCaseListUrl,
  GetRecycleCaseModulesCountUrl,
  GetReviewCommentListUrl,
  GetSearchCustomFieldsUrl,
  GetThirdDemandUrl,
  getTransferTreeUrl,
  GetTrashCaseModuleTreeUrl,
  importExcelCaseUrl,
  importXMindCaseUrl,
  MoveCaseModuleTreeUrl,
  PreviewEditorImageUrl,
  PreviewFileUrl,
  publicAssociatedCaseUrl,
  RecoverRecycleCaseListUrl,
  RestoreCaseListUrl,
  SaveAiConfigUrl,
  SaveCaseMinderUrl,
  StopCaseExportUrl,
  TransferFileUrl,
  UpdateCaseModuleTreeUrl,
  UpdateCaseUrl,
  UpdateCommentItemUrl,
  UpdateDemandUrl,
  UploadOrAssociationFileUrl,
} from '@/api/requrls/case-management/featureCase';

import type { AiCaseTransformResult, AiChatPrams, CaseAiBatchSaveParams, CaseAiChatConfig } from '@/models/ai';
import type { BugListItem } from '@/models/bug-management';
import type {
  AssociatedList,
  BatchMoveOrCopyType,
  CaseManagementTable,
  ChangeHistoryItem,
  CreateOrUpdateDemand,
  CreateOrUpdateModule,
  DeleteCaseType,
  DeleteDependencyParams,
  DemandItem,
  DragCase,
  FeatureCaseMinderUpdateParams,
  ImportExcelType,
  ModulesTreeType,
  OperationFile,
  PreviewImages,
  UpdateModule,
} from '@/models/caseManagement/featureCase';
import type { CommonList, ModuleTreeNode, MoveModules, TableQueryParams } from '@/models/common';
import { ProjectListItem } from '@/models/setting/project';
import { AssociateFunctionalCaseItem } from '@/models/testPlan/testPlan';

import type { Result } from '#/axios';

// 获取模块树
export function getCaseModuleTree(params: TableQueryParams) {
  return MSR.get<ModuleTreeNode[]>({ url: `${GetCaseModuleTreeUrl}/${params.projectId}` });
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
// 获取默认模板自定义字段
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
  return MSR.uploadFile<Result>({ url: CreateCaseUrl }, { request: data.request, fileList: data.fileList }, '', true);
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
export function batchDeleteCase(data: TableQueryParams) {
  return MSR.post({ url: `${BatchDeleteCaseUrl}`, data });
}
// 批量编辑属性
export function batchEditAttrs(data: TableQueryParams) {
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

// 保存脑图
export function saveCaseMinder(data: FeatureCaseMinderUpdateParams) {
  return MSR.post({ url: `${SaveCaseMinderUrl}`, data });
}

// 获取脑图
export function getCaseMinder(data: { projectId: string; moduleId: string; current: number }) {
  return MSR.post<CommonList<MinderJsonNode>>({ url: `${GetCaseMinderUrl}`, data });
}

// 获取脑图模块树（包含文本节点）
export function getCaseMinderTree(data: { projectId: string; moduleId: string }) {
  return MSR.post<MinderJsonNode[]>({ url: `${GetCaseMinderTreeUrl}`, data });
}

// 回收站

// 回收站用例分页表
export function getRecycleListRequest(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetRecycleCaseListUrl, data });
}
// 获取回收站模块数量
export function getRecycleModulesCounts(data: TableQueryParams) {
  return MSR.post({ url: GetRecycleCaseModulesCountUrl, data });
}
// 获取全部用例模块数量
export function getCaseModulesCounts(data: TableQueryParams) {
  return MSR.post({ url: GetCaseModulesCountUrl, data });
}
// 批量恢复回收站用例表
export function restoreCaseList(data: TableQueryParams) {
  return MSR.post({ url: RestoreCaseListUrl, data });
}
// 批量彻底删除回收站用例表
export function batchDeleteRecycleCase(data: TableQueryParams) {
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
export function updateDemandReq(data: CreateOrUpdateDemand) {
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

// 获取三方关联需求列表
export function getThirdDemandList(data: TableQueryParams) {
  return MSR.post({ url: GetThirdDemandUrl, data });
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

// 获取评审评论
export function getReviewCommentList(caseId: string) {
  return MSR.get<CommentItem[]>({ url: `${GetReviewCommentListUrl}/${caseId}` });
}

// 创建评论
export function createCommentList(data: CommentParams) {
  return MSR.post({ url: CreateCommentItemUrl, data });
}

// 编辑评论
export function addOrUpdateCommentList(data: CommentParams) {
  if (data.fetchType === 'UPDATE') {
    return MSR.post({ url: UpdateCommentItemUrl, data });
  }
  return MSR.post({ url: CreateCommentItemUrl, data });
}

// 删除评论
export function deleteCommentList(commentId: string) {
  return MSR.get({ url: `${DeleteCommentItemUrl}/${commentId}` });
}

// 评审
export function getDetailCaseReviewPage(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetDetailCaseReviewUrl, data });
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
  return MSR.post<ModuleTreeNode[]>({ url: GetAssociationPublicModuleTreeUrl, data });
}
// 关联用例
export function associationPublicCase(data: TableQueryParams) {
  return MSR.post<ModulesTreeType[]>({ url: publicAssociatedCaseUrl, data });
}

// 获取前后置用例
export function getDependOnCase(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetDependOnPageUrl, data });
}
// 用例管理-功能用例-用例详情-前后置关系
export function getPrepositionRelation(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetDependOnRelationUrl, data });
}
// 添加前后置关系
export function addPrepositionRelation(data: TableQueryParams) {
  return MSR.post<ModulesTreeType[]>({ url: AddDependOnRelationUrl, data });
}
// 取消依赖关系
export function cancelPreOrPostCase(data: DeleteDependencyParams) {
  return MSR.post({ url: cancelPreAndPostCaseUrl, data });
}
// 获取抽屉详情已关联用例列表
export function getAssociatedCasePage(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetAssociatedDrawerCaseUrl, data });
}
// 获取用例未关联抽屉缺陷列表
export function getDrawerDebugPage(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetDebugDrawerPageUrl, data });
}
// 关联缺陷
export function associatedDebug(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: AssociatedDebuggerUrl, data });
}

// 取消关联缺陷
export function cancelAssociatedDebug(id: string) {
  return MSR.get({ url: `${CancelAssociatedDebuggerUrl}/${id}` });
}
// 取消关联用例
export function cancelAssociatedCase(data: TableQueryParams) {
  return MSR.post({ url: `${cancelDisassociate}`, data });
}

// 获取已关联缺陷列表
export function getLinkedCaseBugList(data: TableQueryParams) {
  return MSR.post<CommonList<BugListItem>>({ url: GetAssociatedDebuggerUrl, data });
}

// 获取已关联前后置用例ids
export function getAssociatedCaseIds(caseId: string) {
  return MSR.get<string[]>({ url: `${GetAssociatedCaseIdsUrl}/${caseId}` });
}

// 下载导入excel或xmind模板
export function downloadTemplate(projectId: string, type: 'Excel' | 'Xmind') {
  return MSR.get(
    {
      url: `${type === 'Excel' ? DownloadExcelTemplateUrl : DownloadXMindTemplateUrl}/${projectId}`,
      responseType: 'blob',
    },
    { isTransformResponse: false }
  );
}

// 导入excel或xmind文件检查
export function importExcelOrXMindChecked(
  data: { request: ImportExcelType; fileList: File[] },
  type: 'Excel' | 'Xmind'
) {
  return MSR.uploadFile(
    { url: type === 'Excel' ? exportExcelCheckUrl : exportXMindCheckUrl },
    { request: data.request, fileList: data.fileList },
    ''
  );
}

// 富文本编辑器上传图片文件
export function editorUploadFile(data: { fileList: File[] }) {
  return MSR.uploadFile({ url: EditorUploadFileUrl }, { fileList: data.fileList }, '', false);
}
// 富文本预览查看详情
export function editorPreviewImages(data: PreviewImages) {
  return MSR.post({ url: PreviewEditorImageUrl, data });
}
// 导入excel
export function importExcelOrXMindCase(data: { request: ImportExcelType; fileList: File[] }, type: 'Excel' | 'Xmind') {
  return MSR.uploadFile(
    { url: type === 'Excel' ? importExcelCaseUrl : importXMindCaseUrl },
    { request: data.request, fileList: data.fileList },
    ''
  );
}
// 导出excel
export function exportExcelCase(data: TableQueryParams) {
  return MSR.post({ url: ExportExcelCaseUrl, data });
}
// 导出XMind
export function exportXMindCase(data: TableQueryParams) {
  return MSR.post({ url: ExportXMindCaseUrl, data });
}
// 检查是否有导出任务
export function checkCaseExportTask() {
  return MSR.get({ url: CheckCaseExportTaskUrl });
}
// 获取导出的文件
export function getCaseDownloadFile(projectId: string, fileId: string) {
  return MSR.get(
    { url: `${GetCaseDownloadFileUrl}/${projectId}/${fileId}`, responseType: 'blob' },
    { isTransformResponse: false, isReturnNativeResponse: true }
  );
}
// 停止导出
export function stopCaseExport(taskId: string) {
  return MSR.get({ url: `${StopCaseExportUrl}/${taskId}` });
}
// 获取导出字段配置
export function getCaseExportConfig(projectId: string) {
  return MSR.get({ url: `${GetCaseExportConfigUrl}/${projectId}` });
}
// 拖拽排序
export function dragSort(data: DragCase) {
  return MSR.post({ url: dragSortUrl, data });
}

// 获取已关联缺陷列表
export function getChangeHistoryList(data: TableQueryParams) {
  return MSR.post<CommonList<ChangeHistoryItem>>({ url: getChangeHistoryListUrl, data });
}
// 获取已关联缺陷列表
export function getAssociatedProjectOptions(orgId: string, module: string) {
  return MSR.get<ProjectListItem[]>({ url: `${associatedProjectOptionsUrl}/${orgId}/${module}` });
}

// 获取已关联测试计划列表
export function getLinkedCaseTestPlanList(data: TableQueryParams) {
  return MSR.post<CommonList<AssociateFunctionalCaseItem>>({ url: GetAssociatedTestPlanUrl, data });
}

// 获取执行评论
export function getTestPlanExecuteCommentList(caseId: string) {
  return MSR.get<CommentItem[]>({ url: `${GetPlanExecuteCommentListUrl}/${caseId}` });
}

// 保存AI配置
export function saveAiConfig(data: CaseAiChatConfig) {
  return MSR.post({ url: SaveAiConfigUrl, data });
}

// 获取AI配置
export function getAiConfig() {
  return MSR.get<CaseAiChatConfig>({ url: GetAiConfigUrl });
}

// AI用例结构转换
export function caseAiTransform(data: AiChatPrams) {
  return MSR.post<AiCaseTransformResult>({ url: CaseAiTransformUrl, data });
}

// AI用例聊天
export function caseAiChat(data: AiChatPrams) {
  return MSR.post({ url: CaseAiChatUrl, data });
}

// AI用例批量保存
export function caseAiBatchSave(data: CaseAiBatchSaveParams) {
  return MSR.post({ url: CaseAiBatchSaveUrl, data });
}
