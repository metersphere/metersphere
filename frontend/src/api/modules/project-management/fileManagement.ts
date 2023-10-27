import MSR from '@/api/http/index';
import {
  AddModuleUrl,
  BatchDownloadFileUrl,
  DeleteFileUrl,
  DeleteModuleUrl,
  DownloadFileUrl,
  FilePageUrl,
  GetModuleCountUrl,
  GetModuleUrl,
  MoveModuleUrl,
  ReuploadFileUrl,
  UpdateFileUrl,
  UpdateModuleUrl,
  UploadFileUrl,
} from '@/api/requrls/project-management/fileManagement';

import type { CommonList } from '@/models/common';
import type {
  AddModuleParams,
  BatchFileApiParams,
  FileItem,
  FileListQueryParams,
  ModuleCount,
  ModuleTreeNode,
  MoveModuleParams,
  ReuploadFileParams,
  UpdateFileParams,
  UpdateModuleParams,
  UploadFileParams,
} from '@/models/projectManagement/file';

// 获取文件列表
export function getFileList(data: FileListQueryParams) {
  return MSR.post<CommonList<FileItem>>({ url: FilePageUrl, data });
}

// 获取文件列表
export function getModulesCount(data: FileListQueryParams) {
  return MSR.post<ModuleCount>({ url: GetModuleCountUrl, data });
}

// 上传文件
export function uploadFile(data: UploadFileParams) {
  return MSR.uploadFile({ url: UploadFileUrl }, { request: data.request, fileList: [data.file] });
}

// 更新文件信息
export function updateFile(data: UpdateFileParams) {
  return MSR.post({ url: UpdateFileUrl, data });
}

// 重新上传文件
export function reuploadFile(data: ReuploadFileParams) {
  return MSR.uploadFile({ url: ReuploadFileUrl }, { request: data.request, fileList: [data.file] });
}

// 删除文件
export function deleteFile(data: BatchFileApiParams) {
  return MSR.post({ url: DeleteFileUrl, data });
}

// 下载文件
export function downloadFile(id: string) {
  return MSR.get({ url: DownloadFileUrl, params: id, responseType: 'blob' }, { isTransformResponse: false });
}

// 批量下载文件
export function batchDownloadFile(data: BatchFileApiParams) {
  return MSR.post({ url: BatchDownloadFileUrl, data, responseType: 'blob' }, { isTransformResponse: false });
}

// 更新模块
export function updateModule(data: UpdateModuleParams) {
  return MSR.post({ url: UpdateModuleUrl, data });
}

// 移动模块
export function moveModule(data: MoveModuleParams) {
  return MSR.post({ url: MoveModuleUrl, data });
}

// 添加模块
export function addModule(data: AddModuleParams) {
  return MSR.post({ url: AddModuleUrl, data });
}

// 查找模块
export function getModules(id: string) {
  return MSR.get<ModuleTreeNode[]>({ url: GetModuleUrl, params: id });
}

// 删除模块
export function deleteModule(id: string) {
  return MSR.get({ url: DeleteModuleUrl, params: id });
}
