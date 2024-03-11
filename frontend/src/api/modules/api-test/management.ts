import MSR from '@/api/http/index';
import {
  AddDefinitionScheduleUrl,
  AddDefinitionUrl,
  AddModuleUrl,
  BatchDeleteDefinitionUrl,
  BatchMoveDefinitionUrl,
  BatchUpdateDefinitionUrl,
  CheckDefinitionScheduleUrl,
  DebugDefinitionUrl,
  DefinitionMockPageUrl,
  DefinitionPageUrl,
  DeleteDefinitionScheduleUrl,
  DeleteDefinitionUrl,
  DeleteMockUrl,
  DeleteModuleUrl,
  GetDefinitionDetailUrl,
  GetDefinitionScheduleUrl,
  GetEnvModuleUrl,
  GetModuleCountUrl,
  GetModuleOnlyTreeUrl,
  GetModuleTreeUrl,
  ImportDefinitionUrl,
  MoveModuleUrl,
  SortDefinitionUrl,
  SwitchDefinitionScheduleUrl,
  ToggleFollowDefinitionUrl,
  TransferFileModuleOptionUrl,
  TransferFileUrl,
  UpdateDefinitionScheduleUrl,
  UpdateDefinitionUrl,
  UpdateMockStatusUrl,
  UpdateModuleUrl,
  UploadTempFileUrl,
} from '@/api/requrls/api-test/management';

import { ExecuteRequestParams } from '@/models/apiTest/common';
import {
  ApiDefinitionBatchDeleteParams,
  ApiDefinitionBatchMoveParams,
  ApiDefinitionBatchUpdateParams,
  ApiDefinitionCreateParams,
  ApiDefinitionDetail,
  ApiDefinitionGetEnvModuleParams,
  ApiDefinitionGetModuleParams,
  ApiDefinitionMockDetail,
  ApiDefinitionMockPageParams,
  ApiDefinitionPageParams,
  ApiDefinitionUpdateModuleParams,
  ApiDefinitionUpdateParams,
  CheckScheduleParams,
  CreateImportApiDefinitionScheduleParams,
  EnvModule,
  ImportApiDefinitionParams,
  mockParams,
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
