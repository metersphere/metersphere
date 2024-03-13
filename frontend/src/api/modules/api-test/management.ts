import MSR from '@/api/http/index';
import {
  AddCaseUrl,
  AddDefinitionScheduleUrl,
  AddDefinitionUrl,
  AddModuleUrl,
  BatchCleanOutApiUrl,
  BatchDeleteCaseUrl,
  BatchDeleteDefinitionUrl,
  BatchEditCaseUrl,
  BatchMoveDefinitionUrl,
  BatchRecoverApiUrl,
  BatchUpdateDefinitionUrl,
  CasePageUrl,
  CheckDefinitionScheduleUrl,
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
  GetDefinitionDetailUrl,
  GetDefinitionScheduleUrl,
  GetEnvModuleUrl,
  GetModuleCountUrl,
  GetModuleOnlyTreeUrl,
  GetModuleTreeUrl,
  GetTrashModuleCountUrl,
  GetTrashModuleTreeUrl,
  ImportDefinitionUrl,
  MoveModuleUrl,
  OperationHistoryUrl,
  RecoverDefinitionUrl,
  RecoverOperationHistoryUrl,
  SaveOperationHistoryUrl,
  SortCaseUrl,
  SortDefinitionUrl,
  SwitchDefinitionScheduleUrl,
  ToggleFollowDefinitionUrl,
  TransferFileModuleOptionUrl,
  TransferFileUrl,
  UpdateCasePriorityUrl,
  UpdateCaseStatusUrl,
  UpdateDefinitionScheduleUrl,
  UpdateDefinitionUrl,
  UpdateMockStatusUrl,
  UpdateModuleUrl,
  UploadTempFileUrl,
} from '@/api/requrls/api-test/management';

import { ExecuteRequestParams } from '@/models/apiTest/common';
import {
  AddApiCaseParams,
  ApiCaseBatchEditParams,
  ApiCaseBatchParams,
  ApiCaseDetail,
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
  EnvModule,
  ImportApiDefinitionParams,
  mockParams,
  RecoverDefinitionParams,
  UpdateScheduleParams,
} from '@/models/apiTest/management';
import {
  AddModuleParams,
  CommonList,
  DragSortParams,
  ModuleTreeNode,
  MoveModules,
  TransferFileParams,
} from '@/models/common';

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
export function getDefinitionDetail(id: string) {
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
export function deleteDefinitionMockMock(data: mockParams) {
  return MSR.post({ url: DeleteMockUrl, data });
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

// 添加接口用例
export function addCase(data: AddApiCaseParams) {
  return MSR.post({ url: AddCaseUrl, data });
}
