import MSR from '@/api/http/index';
import {
  AddModuleUrl,
  AddRepositoryFileUrl,
  AddRepositoryUrl,
  BatchDownloadFileUrl,
  BatchMoveFileUrl,
  ConnectRepositoryUrl,
  DeleteFileUrl,
  DeleteModuleUrl,
  DownloadFileUrl,
  FilePageUrl,
  GetAssociationListUrl,
  GetFileDetailUrl,
  GetFileHistoryListUrl,
  GetFileTypesUrl,
  GetModuleCountUrl,
  GetModuleUrl,
  GetRepositoryFileTypesUrl,
  GetRepositoryFileUrl,
  GetRepositoryInfoUrl,
  MoveModuleUrl,
  ReuploadFileUrl,
  ToggleJarFileUrl,
  UpdateFileUrl,
  UpdateModuleUrl,
  UpdateRepositoryFileUrl,
  UpdateRepositoryUrl,
  UpgradeAssociationUrl,
  UploadFileUrl,
} from '@/api/requrls/project-management/fileManagement';

import type { CommonList, ModuleTreeNode } from '@/models/common';
import type {
  AddModuleParams,
  AddRepositoryFileParams,
  AddRepositoryParams,
  AssociationItem,
  BatchFileApiParams,
  FileDetail,
  FileHistoryItem,
  FileItem,
  FileListQueryParams,
  ModuleCount,
  MoveModuleParams,
  Repository,
  RepositoryInfo,
  ReuploadFileParams,
  TestRepositoryConnectParams,
  UpdateFileParams,
  UpdateModuleParams,
  UpdateRepositoryParams,
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

// 获取模块文件类型集合
export function getFileTypes(id: string) {
  return MSR.get<string[]>({ url: GetFileTypesUrl, params: id });
}

// 获取文件详情
export function getFileDetail(id: string) {
  return MSR.get<FileDetail>({ url: GetFileDetailUrl, params: id });
}

// 获取文件历史版本列表
export function getFileHistoryList(params: { id: string }) {
  return MSR.get<FileHistoryItem[]>({ url: GetFileHistoryListUrl, params: params.id });
}

// jar文件启用禁用
export function toggleJarFileStatus(id: string, status: boolean) {
  return MSR.get({ url: `${ToggleJarFileUrl}/${id}/${status}` });
}

// 批量移动文件
export function batchMoveFile(data: BatchFileApiParams) {
  return MSR.post({ url: BatchMoveFileUrl, data });
}

// 查找存储库
export function getRepositories(id: string) {
  return MSR.get<Repository[]>({ url: GetRepositoryFileUrl, params: id });
}

// 获取存储库文件类型集合
export function getRepositoryFileTypes(id: string) {
  return MSR.get<string[]>({ url: GetRepositoryFileTypesUrl, params: id });
}

// 添加存储库
export function addRepository(data: AddRepositoryParams) {
  return MSR.post({ url: AddRepositoryUrl, data });
}

// 测试存储库连接
export function connectRepository(data: TestRepositoryConnectParams) {
  return MSR.post({ url: ConnectRepositoryUrl, data });
}

// 修改存储库信息
export function updateRepository(data: UpdateRepositoryParams) {
  return MSR.post({ url: UpdateRepositoryUrl, data });
}

// 添加存储库文件
export function addRepositoryFile(data: AddRepositoryFileParams) {
  return MSR.post({ url: AddRepositoryFileUrl, data });
}

// 更新存储库文件
export function updateRepositoryFile(id: string) {
  return MSR.get<string>({ url: UpdateRepositoryFileUrl, params: id });
}

// 获取存储库信息
export function getRepositoryInfo(id: string) {
  return MSR.get<RepositoryInfo>({ url: GetRepositoryInfoUrl, params: id });
}

// 更新关联用例文件
export function upgradeAssociation(projectId: string, id: string) {
  return MSR.get({ url: `${UpgradeAssociationUrl}/${projectId}`, params: id });
}

// 获取文件关联用例列表
export function getAssociationList(params: { id: string }) {
  return MSR.get<AssociationItem[]>({ url: GetAssociationListUrl, params: params.id });
}
