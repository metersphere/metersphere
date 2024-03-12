import { FileItem } from '@arco-design/web-vue';

import MSR from '@/api/http/index';
import * as envURL from '@/api/requrls/project-management/envManagement';

import type {
  DragParam,
  EnvDetailItem,
  EnvListItem,
  EnvPluginListItem,
  GlobalParams,
  GroupItem,
  ProjectOptionItem,
} from '@/models/projectManagement/environmental';
import { OptionsItem } from '@/models/setting/log';

export function updateOrAddEnv(data: { request: EnvDetailItem; fileList: FileItem[] }) {
  return MSR.uploadFile<EnvDetailItem>(
    { url: data.request.id ? envURL.updateEnvUrl : envURL.addEnvUrl },
    data,
    '',
    true
  );
}
export function listEnv(data: { projectId: string; keyword: string }) {
  return MSR.post<EnvListItem[]>({ url: envURL.listEnvUrl, data });
}
export function importEnv(data: { request: any; fileList: File[] }) {
  return MSR.uploadFile({ url: envURL.importEnvUrl }, data, '', false);
}
export function getEntryEnv(data: EnvListItem) {
  return MSR.post<EnvListItem>({ url: envURL.getEntryEnvUrl, data });
}
export function exportEnv(selectIds: string[]) {
  return MSR.post<Blob>(
    { url: envURL.exportEnvUrl, data: { selectIds }, responseType: 'blob' },
    { isTransformResponse: false }
  );
}
export function editPosEnv(data: DragParam) {
  return MSR.post<EnvListItem>({ url: envURL.editPosEnvUrl, data });
}

/** 测试数据库连接 */
export function validateDatabaseEnv(data: object) {
  return MSR.post<EnvListItem>({ url: envURL.validateDatabaseEnvUrl, data });
}
/** 获取数据库驱动option */
export function driverOptionFun(organizationId: string) {
  return MSR.get<OptionsItem[]>({ url: envURL.driverOptionUrl + organizationId });
}

export function addEnv(data: EnvListItem) {
  return MSR.post<EnvListItem>({ url: envURL.addEnvUrl, data });
}
export function getDetailEnv(id: string) {
  return MSR.get<EnvDetailItem>({ url: envURL.detailEnvUrl + id });
}
export function deleteEnv(id: string) {
  return MSR.get<EnvListItem>({ url: envURL.deleteEnvUrl + id });
}
export function groupUpdateEnv(data: any) {
  return MSR.post<EnvListItem>({ url: envURL.groupUpdateEnvUrl, data });
}
export function groupListEnv(data: { projectId: string; keyword: string }) {
  return MSR.post<EnvListItem[]>({ url: envURL.groupListEnvUrl, data });
}
export function groupEditPosEnv(data: DragParam) {
  return MSR.post<EnvListItem>({ url: envURL.groupEditPosEnvUrl, data });
}
export function groupAddEnv(data: any) {
  return MSR.post<EnvListItem>({ url: envURL.groupAddEnvUrl, data });
}
export function deleteEnvGroup(id: string) {
  return MSR.get<EnvListItem>({ url: envURL.groupDeleteEnvUrl + id });
}
export function getEnvPlugin(projectId: string) {
  return MSR.get<EnvPluginListItem[]>({ url: envURL.getEnvPluginUrl + projectId });
}
// 项目管理-项目组-详情
export function getGroupDetailEnv(id: string) {
  return MSR.get<GroupItem>({ url: `${envURL.groupDetailEnvUrl}${id}` });
}
export function groupDeleteEnv(data: EnvListItem) {
  return MSR.post<EnvListItem>({ url: envURL.groupDeleteEnvUrl, data });
}
// 获取项目组的项目
export function groupProjectEnv(organizationId: string) {
  return MSR.get<ProjectOptionItem[]>({ url: envURL.groupProjectEnvUrl + organizationId });
}
// 获取项目组的项目
export function groupCategoryEnvList(projectId: string) {
  return MSR.get<ProjectOptionItem[]>({ url: `${envURL.getProjectEnvCategoryUrl}/${projectId}` });
}

/** 项目管理-环境-全局参数-更新or新增 */
export function updateOrAddGlobalParam(data: GlobalParams) {
  return MSR.post<EnvListItem>({ url: data.id ? envURL.updateGlobalParamUrl : envURL.addGlobalParamUrl, data });
}
/** 项目管理-环境-全局参数-导入 */
export function importGlobalParam(data: { request: any; fileList: File[] }) {
  return MSR.uploadFile<EnvListItem>({ url: envURL.importGlobalParamUrl }, data, '', false);
}
/** 项目管理-环境-全局参数-详情 */
export function getGlobalParamDetail(id: string) {
  return MSR.get<GlobalParams>({ url: envURL.detailGlobalParamUrl + id });
}
/** 项目管理-环境-全局参数-导出 */
export function exportGlobalParam(id: string) {
  return MSR.get<BlobPart>(
    { url: envURL.exportGlobalParamUrl + id, responseType: 'blob' },
    { isTransformResponse: false }
  );
}
