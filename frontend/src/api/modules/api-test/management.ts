import MSR from '@/api/http/index';
import {
  AddDefinitionUrl,
  AddModuleUrl,
  BatchDeleteDefinitionUrl,
  DefinitionPageUrl,
  DeleteDefinitionUrl,
  DeleteModuleUrl,
  GetDefinitionDetailUrl,
  GetEnvModuleUrl,
  GetModuleCountUrl,
  GetModuleOnlyTreeUrl,
  GetModuleTreeUrl,
  MoveModuleUrl,
  TransferFileModuleOptionUrl,
  TransferFileUrl,
  UpdateDefinitionUrl,
  UpdateModuleUrl,
  UploadTempFileUrl,
} from '@/api/requrls/api-test/management';

import {
  ApiDefinitionCreateParams,
  ApiDefinitionDetail,
  ApiDefinitionGetEnvModuleParams,
  ApiDefinitionGetModuleParams,
  ApiDefinitionPageParams,
  ApiDefinitionUpdateModuleParams,
  ApiDefinitionUpdateParams,
  EnvModule,
} from '@/models/apiTest/management';
import { AddModuleParams, CommonList, ModuleTreeNode, MoveModules, TransferFileParams } from '@/models/common';

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
  return MSR.get({ url: GetDefinitionDetailUrl, params: id });
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
export function batchDeleteDefinition(id: string) {
  return MSR.get({ url: BatchDeleteDefinitionUrl, params: id });
}
