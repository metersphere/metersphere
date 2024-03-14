import MSR from '@/api/http/index';
import {
  AddApiDebugUrl,
  AddDebugModuleUrl,
  DeleteDebugModuleUrl,
  DeleteDebugUrl,
  DragDebugUrl,
  ExecuteApiDebugUrl,
  GetApiDebugDetailUrl,
  GetDebugModuleCountUrl,
  GetDebugModulesUrl,
  MoveDebugModuleUrl,
  TestMockUrl,
  TransferFileUrl,
  TransferOptionsUrl,
  UpdateApiDebugUrl,
  UpdateDebugModuleUrl,
  UploadTempFileUrl,
} from '@/api/requrls/api-test/debug';

import { ExecuteRequestParams } from '@/models/apiTest/common';
import {
  AddDebugModuleParams,
  DebugDetail,
  SaveDebugParams,
  UpdateDebugModule,
  UpdateDebugParams,
} from '@/models/apiTest/debug';
import { DragSortParams, ModuleTreeNode, MoveModules, TransferFileParams } from '@/models/common';

// 获取模块树
export function getDebugModules() {
  return MSR.get<ModuleTreeNode[]>({ url: GetDebugModulesUrl });
}

// 删除模块
export function deleteDebugModule(deleteId: string) {
  return MSR.get({ url: DeleteDebugModuleUrl, params: deleteId });
}

// 添加模块
export function addDebugModule(data: AddDebugModuleParams) {
  return MSR.post({ url: AddDebugModuleUrl, data });
}

// 移动模块
export function moveDebugModule(data: MoveModules) {
  return MSR.post({ url: MoveDebugModuleUrl, data });
}

// 更新模块
export function updateDebugModule(data: UpdateDebugModule) {
  return MSR.post({ url: UpdateDebugModuleUrl, data });
}

// 模块数量统计
export function getDebugModuleCount(data: { keyword: string }) {
  return MSR.post({ url: GetDebugModuleCountUrl, data });
}

// 拖拽调试节点
export function dragDebug(data: DragSortParams) {
  return MSR.post({ url: DragDebugUrl, data });
}

// 执行调试
export function executeDebug(data: ExecuteRequestParams) {
  return MSR.post<ExecuteRequestParams>({ url: ExecuteApiDebugUrl, data });
}

// 新增调试
export function addDebug(data: SaveDebugParams) {
  return MSR.post({ url: AddApiDebugUrl, data });
}

// 更新调试
export function updateDebug(data: UpdateDebugParams) {
  return MSR.post({ url: UpdateApiDebugUrl, data });
}

// 获取接口调试详情
export function getDebugDetail(id: string) {
  return MSR.get<DebugDetail>({ url: GetApiDebugDetailUrl, params: id });
}

// 删除接口调试
export function deleteDebug(id: string) {
  return MSR.get({ url: DeleteDebugUrl, params: id });
}

// 测试mock
export function testMock(data: string) {
  return MSR.post({ url: TestMockUrl, data });
}

// 上传文件
export function uploadTempFile(file: File) {
  return MSR.uploadFile({ url: UploadTempFileUrl }, { fileList: [file] }, 'file');
}

// 文件转存
export function transferFile(data: TransferFileParams) {
  return MSR.post({ url: TransferFileUrl, data });
}

// 文件转存目录
export function getTransferOptions(projectId: string) {
  return MSR.get<ModuleTreeNode[]>({ url: TransferOptionsUrl, params: projectId });
}
