import MSR from '@/api/http';
import {
  AddVersion,
  DeleteVersion,
  EnableVersion,
  GetVersionOptions,
  GetVersionStatus,
  ToggleVersionStatus,
  UpdateVersion,
  UseLatestVersion,
  VersionPage,
} from '@/api/requrls/project-management/projectVersion';

import type { CommonList, TableQueryParams } from '@/models/common';
import type { ProjectCommon, ProjectItem, ProjectVersionOption } from '@/models/projectManagement/projectVersion';

// 更新版本
export function updateVersion(data: ProjectItem) {
  return MSR.post({ url: UpdateVersion, data });
}

// 获取版本列表
export function getVersionList(data: TableQueryParams) {
  return MSR.post<CommonList<ProjectItem>>({ url: VersionPage, data });
}

// 添加版本
export function addVersion(data: ProjectCommon) {
  return MSR.post({ url: AddVersion, data });
}

// 切换版本状态
export function toggleVersionStatus(id: string) {
  return MSR.get({ url: ToggleVersionStatus, params: id });
}

// 切换最新版本
export function useLatestVersion(id: string) {
  return MSR.get({ url: UseLatestVersion, params: id });
}

// 开启/关闭项目版本
export function toggleVersion(id: string) {
  return MSR.get({ url: EnableVersion, params: id });
}

// 获取项目版本列表选项
export function getVersionOptions(id: string) {
  return MSR.get<ProjectVersionOption[]>({ url: GetVersionOptions, params: id });
}

// 获取版本状态
export function getVersionStatus(id: string) {
  return MSR.get<boolean>({ url: GetVersionStatus, params: id });
}

// 删除版本
export function deleteVersion(id: string) {
  return MSR.get({ url: DeleteVersion, params: id });
}
