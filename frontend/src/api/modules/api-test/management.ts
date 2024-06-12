import MSR from '@/api/http/index';
import {
  AddCaseUrl,
  AddDefinitionScheduleUrl,
  AddDefinitionUrl,
  AddMockUrl,
  AddModuleUrl,
  BatchCleanOutApiUrl,
  BatchDeleteCaseUrl,
  BatchDeleteDefinitionUrl,
  BatchDeleteMockUrl,
  BatchDeleteRecycleCaseUrl,
  BatchEditCaseUrl,
  BatchEditMockUrl,
  BatchExecuteCaseUrl,
  BatchMoveDefinitionUrl,
  BatchRecoverApiUrl,
  BatchRecoverCaseUrl,
  BatchUpdateDefinitionUrl,
  CasePageUrl,
  CheckDefinitionScheduleUrl,
  CopyMockUrl,
  DebugCaseUrl,
  DebugDefinitionUrl,
  DefinitionMockPageUrl,
  DefinitionPageUrl,
  DefinitionReferenceUrl,
  DeleteCaseUrl,
  DeleteDefinitionScheduleUrl,
  DeleteDefinitionUrl,
  DeleteMockUrl,
  DeleteModuleUrl,
  DeleteRecycleApiUrl,
  DeleteRecycleCaseUrl,
  ExecuteCaseUrl,
  GetCaseDetailUrl,
  GetCaseReportByIdUrl,
  GetCaseReportDetailUrl,
  GetChangeHistoryUrl,
  GetDefinitionDetailUrl,
  GetDefinitionScheduleUrl,
  GetDependencyUrl,
  GetEnvModuleUrl,
  GetExecuteHistoryUrl,
  GetMockUrlUrl,
  GetModuleCountUrl,
  GetModuleOnlyTreeUrl,
  GetModuleTreeUrl,
  GetPoolId,
  GetPoolOptionUrl,
  GetTrashModuleCountUrl,
  GetTrashModuleTreeUrl,
  ImportDefinitionUrl,
  MockDetailUrl,
  MoveModuleUrl,
  OperationHistoryUrl,
  RecoverCaseUrl,
  RecoverDefinitionUrl,
  RecoverOperationHistoryUrl,
  RecycleCasePageUrl,
  RunCaseUrl,
  SaveOperationHistoryUrl,
  SortCaseUrl,
  SortDefinitionUrl,
  SwitchDefinitionScheduleUrl,
  ToggleFollowCaseUrl,
  ToggleFollowDefinitionUrl,
  TransferFileCaseUrl,
  TransferFileModuleOptionCaseUrl,
  TransferFileModuleOptionUrl,
  TransferFileUrl,
  TransferMockFileModuleOptionUrl,
  TransferMockFileUrl,
  UpdateCasePriorityUrl,
  UpdateCaseStatusUrl,
  UpdateCaseUrl,
  UpdateDefinitionScheduleUrl,
  UpdateDefinitionUrl,
  UpdateMockStatusUrl,
  UpdateMockUrl,
  UpdateModuleUrl,
  UploadTempFileCaseUrl,
  UploadTempFileUrl,
  UploadTempMockFileUrl,
} from '@/api/requrls/api-test/management';

import { ApiCaseReportDetail, ExecuteRequestParams } from '@/models/apiTest/common';
import {
  AddApiCaseParams,
  ApiCaseBatchEditParams,
  ApiCaseBatchExecuteParams,
  ApiCaseBatchParams,
  ApiCaseChangeHistoryParams,
  ApiCaseDependencyParams,
  ApiCaseDetail,
  ApiCaseExecuteHistoryItem,
  ApiCaseExecuteHistoryParams,
  ApiCasePageParams,
  ApiDefinitionBatchDeleteParams,
  ApiDefinitionBatchMoveParams,
  ApiDefinitionBatchUpdateParams,
  ApiDefinitionCreateParams,
  ApiDefinitionDeleteParams,
  ApiDefinitionDetail,
  ApiDefinitionGetEnvModuleParams,
  ApiDefinitionGetModuleParams,
  ApiDefinitionMockDetail,
  ApiDefinitionMockPageParams,
  ApiDefinitionPageParams,
  ApiDefinitionUpdateModuleParams,
  ApiDefinitionUpdateParams,
  BatchRecoverApiParams,
  CheckScheduleParams,
  CreateImportApiDefinitionScheduleParams,
  DefinitionHistoryItem,
  DefinitionHistoryPageParams,
  DefinitionReferencePageParams,
  Environment,
  EnvModule,
  ImportApiDefinitionParams,
  mockParams,
  RecoverDefinitionParams,
  UpdateScheduleParams,
} from '@/models/apiTest/management';
import type { BatchEditMockParams, MockDetail, MockParams, UpdateMockParams } from '@/models/apiTest/mock';
import {
  AddModuleParams,
  type BatchApiParams,
  CommonList,
  DragSortParams,
  ModuleTreeNode,
  MoveModules,
  TransferFileParams,
} from '@/models/common';
import { ResourcePoolItem } from '@/models/setting/resourcePool';

// 更新模块
export function updateModule(data: ApiDefinitionUpdateModuleParams) {
  return MSR.post({ url: UpdateModuleUrl, data });
}

// 获取模块树
export function getModuleTree(data: ApiDefinitionGetModuleParams) {
  return MSR.post<ModuleTreeNode[]>({ url: GetModuleTreeUrl, data });
}

// 获取模块树-只包含模块
export function getModuleTreeOnlyModules(data: ApiDefinitionGetModuleParams) {
  return MSR.post<ModuleTreeNode[]>({ url: GetModuleOnlyTreeUrl, data });
}

// 移动模块
export function moveModule(data: MoveModules) {
  return MSR.post({ url: MoveModuleUrl, data });
}

// 获取环境的模块树
export function getEnvModules(data: ApiDefinitionGetEnvModuleParams) {
  return MSR.post<EnvModule>({ url: GetEnvModuleUrl, data });
}

// 获取模块统计数量
export function getModuleCount(data: ApiDefinitionGetModuleParams) {
  return MSR.post({ url: GetModuleCountUrl, data });
}

// 添加模块
export function addModule(data: AddModuleParams) {
  return MSR.post({ url: AddModuleUrl, data });
}

// 删除模块
export function deleteModule(id: string) {
  return MSR.get({ url: DeleteModuleUrl, params: id });
}

// 获取接口定义列表
export function getDefinitionPage(data: ApiDefinitionPageParams) {
  return MSR.post<CommonList<ApiDefinitionDetail>>({ url: DefinitionPageUrl, data });
}

// 添加接口定义
export function addDefinition(data: ApiDefinitionCreateParams) {
  return MSR.post({ url: AddDefinitionUrl, data });
}

// 更新接口定义
export function updateDefinition(data: ApiDefinitionUpdateParams) {
  return MSR.post({ url: UpdateDefinitionUrl, data });
}

// 获取接口定义详情
export function getDefinitionDetail(id: string | number) {
  return MSR.get<ApiDefinitionDetail>({ url: GetDefinitionDetailUrl, params: id });
}

// 文件转存
export function transferFile(data: TransferFileParams) {
  return MSR.post({ url: TransferFileUrl, data });
}

// 文件转存目录
export function getTransferOptions(projectId: string) {
  return MSR.get<ModuleTreeNode[]>({ url: TransferFileModuleOptionUrl, params: projectId });
}

// 上传文件
export function uploadTempFile(file: File) {
  return MSR.uploadFile({ url: UploadTempFileUrl }, { fileList: [file] }, 'file');
}

// 删除定义
export function deleteDefinition(id: string) {
  return MSR.get({ url: DeleteDefinitionUrl, params: id });
}

// 批量删除定义
export function batchDeleteDefinition(data: ApiDefinitionBatchDeleteParams) {
  return MSR.post({ url: BatchDeleteDefinitionUrl, data });
}

// 导入定义
export function importDefinition(params: ImportApiDefinitionParams) {
  return MSR.uploadFile({ url: ImportDefinitionUrl }, { fileList: [params.file], request: params.request }, 'file');
}

// 拖拽定义节点
export function sortDefinition(data: DragSortParams) {
  return MSR.post({ url: SortDefinitionUrl, data });
}

// 批量更新定义
export function batchUpdateDefinition(data: ApiDefinitionBatchUpdateParams) {
  return MSR.post({ url: BatchUpdateDefinitionUrl, data });
}

// 批量移动定义
export function batchMoveDefinition(data: ApiDefinitionBatchMoveParams) {
  return MSR.post({ url: BatchMoveDefinitionUrl, data });
}

// 更新定时同步
export function updateDefinitionSchedule(data: UpdateScheduleParams) {
  return MSR.post({ url: UpdateDefinitionScheduleUrl, data });
}

// 定时同步-检查 url 是否存在
export function checkDefinitionSchedule(data: CheckScheduleParams) {
  return MSR.post({ url: CheckDefinitionScheduleUrl, data });
}

// 添加定时同步
export function createDefinitionSchedule(data: CreateImportApiDefinitionScheduleParams) {
  return MSR.post({ url: AddDefinitionScheduleUrl, data });
}

// 定时同步-开启关闭
export function switchDefinitionSchedule(id: string) {
  return MSR.get({ url: SwitchDefinitionScheduleUrl, params: id });
}

// 查询定时同步详情
export function getDefinitionSchedule(id: string) {
  return MSR.get({ url: GetDefinitionScheduleUrl, params: id });
}

// 删除定时同步
export function deleteDefinitionSchedule(id: string) {
  return MSR.get({ url: DeleteDefinitionScheduleUrl, params: id });
}

// 接口定义调试
export function debugDefinition(data: ExecuteRequestParams) {
  return MSR.post({ url: DebugDefinitionUrl, data });
}

// 关注/取消关注接口定义
export function toggleFollowDefinition(id: string | number) {
  return MSR.get({ url: ToggleFollowDefinitionUrl, params: id });
}

// 接口定义-变更历史
export function operationHistory(data: DefinitionHistoryPageParams) {
  return MSR.post<CommonList<DefinitionHistoryItem>>({ url: OperationHistoryUrl, data });
}

// 接口定义-保存变更历史为指定版本
export function saveOperationHistory(data: ExecuteRequestParams) {
  return MSR.post({ url: SaveOperationHistoryUrl, data });
}

// 接口定义-恢复至指定变更历史
export function recoverOperationHistory(data: RecoverDefinitionParams) {
  return MSR.post({ url: RecoverOperationHistoryUrl, data });
}

// 接口定义-引用关系
export function getDefinitionReference(data: DefinitionReferencePageParams) {
  return MSR.post({ url: DefinitionReferenceUrl, data });
}

/**
 * Mock
 */
// 获取mock列表接口
export function getDefinitionMockPage(data: ApiDefinitionMockPageParams) {
  return MSR.post<CommonList<ApiDefinitionMockDetail>>({ url: DefinitionMockPageUrl, data });
}

// 更新mock状态接口
export function updateMockStatusPage(id: string) {
  return MSR.get({ url: UpdateMockStatusUrl, params: id });
}

// 刪除mock接口
export function deleteMock(data: mockParams) {
  return MSR.post({ url: DeleteMockUrl, data });
}

// 上传文件
export function uploadMockTempFile(file: File) {
  return MSR.uploadFile({ url: UploadTempMockFileUrl }, { fileList: [file] }, 'file');
}

// 文件转存
export function transferMockFile(data: TransferFileParams) {
  return MSR.post({ url: TransferMockFileUrl, data });
}

// 文件转存目录
export function getMockTransferOptions(projectId: string) {
  return MSR.get<ModuleTreeNode[]>({ url: TransferMockFileModuleOptionUrl, params: projectId });
}

// 更新 mock
export function updateMock(data: UpdateMockParams) {
  return MSR.post({ url: UpdateMockUrl, data });
}

// 获取 mock 详情
export function getMockDetail(data: { id: string; projectId: string }) {
  return MSR.post<MockDetail>({ url: MockDetailUrl, data });
}

// 复制 mock
export function copyMock(data: { id: string; projectId: string }) {
  return MSR.post({ url: CopyMockUrl, data });
}

// 批量编辑 mock
export function batchEditMock(data: BatchEditMockParams) {
  return MSR.post({ url: BatchEditMockUrl, data });
}

// 批量删除 mock
export function batchDeleteMock(data: BatchApiParams) {
  return MSR.post({ url: BatchDeleteMockUrl, data });
}

// 添加 mock
export function addMock(data: MockParams) {
  return MSR.post({ url: AddMockUrl, data });
}

// 获取 mock url
export function getMockUrl(id: string) {
  return MSR.get({ url: GetMockUrlUrl, params: id });
}
/**
 * 回收站
 */
// 回收站-恢复接口定义
export function recoverDefinition(data: ApiDefinitionDeleteParams) {
  return MSR.post({ url: RecoverDefinitionUrl, data });
}

// 回收站-彻底删除接口定义
export function deleteRecycleApiList(id: string) {
  return MSR.get({ url: DeleteRecycleApiUrl, params: id });
}

// 回收站-批量恢复接口定义
export function batchRecoverDefinition(data: BatchRecoverApiParams) {
  return MSR.post({ url: BatchRecoverApiUrl, data });
}

// 回收站-批量彻底删除接口定义
export function batchCleanOutDefinition(data: BatchRecoverApiParams) {
  return MSR.post({ url: BatchCleanOutApiUrl, data });
}

// 回收站-模块树
export function getTrashModuleTree(data: ApiDefinitionGetModuleParams) {
  return MSR.post<ModuleTreeNode[]>({ url: GetTrashModuleTreeUrl, data });
}

// 获取回收站模块统计数量
export function getTrashModuleCount(data: ApiDefinitionGetModuleParams) {
  return MSR.post({ url: GetTrashModuleCountUrl, data });
}

// --------------------用例
// 获取接口用例列表
export function getCasePage(data: ApiCasePageParams) {
  return MSR.post<CommonList<ApiCaseDetail>>({ url: CasePageUrl, data });
}

// 接口用例更新状态
export function updateCaseStatus(id: string, status: string) {
  return MSR.get({ url: `${UpdateCaseStatusUrl}/${id}/${status}` });
}

// 接口用例更新等级
export function updateCasePriority(id: string, priority: string) {
  return MSR.get({ url: `${UpdateCasePriorityUrl}/${id}/${priority}` });
}

// 删除接口用例
export function deleteCase(id: string) {
  return MSR.get({ url: DeleteCaseUrl, params: id });
}

// 批量删除接口用例
export function batchDeleteCase(data: ApiCaseBatchParams) {
  return MSR.post({ url: BatchDeleteCaseUrl, data });
}

// 批量编辑接口用例
export function batchEditCase(data: ApiCaseBatchEditParams) {
  return MSR.post({ url: BatchEditCaseUrl, data });
}

// 拖拽排序
export function dragSort(data: DragSortParams) {
  return MSR.post({ url: SortCaseUrl, data });
}

// 更新接口用例
export function updateCase(data: AddApiCaseParams) {
  return MSR.post({ url: UpdateCaseUrl, data });
}

// 接口用例调试
export function debugCase(data: ExecuteRequestParams) {
  return MSR.post({ url: DebugCaseUrl, data });
}

// 文件转存
export function transferFileCase(data: TransferFileParams) {
  return MSR.post({ url: TransferFileCaseUrl, data });
}

// 文件转存目录
export function getTransferOptionsCase(projectId: string) {
  return MSR.get<ModuleTreeNode[]>({ url: TransferFileModuleOptionCaseUrl, params: projectId });
}

// 上传文件
export function uploadTempFileCase(file: File) {
  return MSR.uploadFile({ url: UploadTempFileCaseUrl }, { fileList: [file] }, 'file');
}

// 获取接口用例详情
export function getCaseDetail(id: string) {
  return MSR.get<ApiCaseDetail>({ url: GetCaseDetailUrl, params: id });
}

// 关注/取消关注接口用例
export function toggleFollowCase(id: string | number) {
  return MSR.get({ url: ToggleFollowCaseUrl, params: id });
}

// 用例执行，传请求详情执行
export function runCase(data: ExecuteRequestParams) {
  return MSR.post({ url: RunCaseUrl, data });
}

/**
 * 接口用例回收站
 */
// 获取回收站接口用例列表
export function getRecycleCasePage(data: ApiCasePageParams) {
  return MSR.post<CommonList<ApiCaseDetail>>({ url: RecycleCasePageUrl, data });
}

// 恢复接口用例
export function recoverCase(id: string) {
  return MSR.get({ url: RecoverCaseUrl, params: id });
}

// 批量恢复接口用例
export function batchRecoverCase(data: ApiCaseBatchParams) {
  return MSR.post({ url: BatchRecoverCaseUrl, data });
}

// 彻底删除接口用例
export function deleteRecycleCase(id: string) {
  return MSR.get({ url: DeleteRecycleCaseUrl, params: id });
}

// 批量彻底删除接口用例
export function batchDeleteRecycleCase(data: ApiCaseBatchParams) {
  return MSR.post({ url: BatchDeleteRecycleCaseUrl, data });
}

// 添加接口用例
export function addCase(data: AddApiCaseParams) {
  return MSR.post({ url: AddCaseUrl, data });
}

// 执行接口用例
export function executeCase(id: string) {
  return MSR.get({ url: ExecuteCaseUrl, params: id });
}

// 批量执行接口用例
export function batchExecuteCase(data: ApiCaseBatchExecuteParams) {
  return MSR.post({ url: BatchExecuteCaseUrl, data });
}

// 获取接口用例-执行历史
export function getApiCaseExecuteHistory(data: ApiCaseExecuteHistoryParams) {
  return MSR.post<CommonList<ApiCaseExecuteHistoryItem>>({ url: GetExecuteHistoryUrl, data });
}

// 获取接口用例-变更历史
export function getApiCaseChangeHistory(data: ApiCaseChangeHistoryParams) {
  return MSR.post({ url: GetChangeHistoryUrl, data });
}

// 获取接口用例-依赖关系
export function getApiCaseDependency(data: ApiCaseDependencyParams) {
  return MSR.post({ url: GetDependencyUrl, data });
}

// 获取接口的资源池列表
export function getPoolOption(projectId: string) {
  return MSR.get<ResourcePoolItem[]>({ url: GetPoolOptionUrl, params: projectId });
}

export function getPoolId(projectId: string) {
  return MSR.get<string>({ url: GetPoolId + projectId });
}

export function getReportById(id: string) {
  return MSR.get<Record<string, any>>({ url: GetCaseReportByIdUrl + id });
}

export function getCaseReportDetail(reportId: string, stepId: string) {
  return MSR.get<ApiCaseReportDetail[]>({ url: `${GetCaseReportDetailUrl + reportId}/${stepId}` });
}
