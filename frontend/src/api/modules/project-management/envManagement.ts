import { FileItem } from '@arco-design/web-vue';

import MSR from '@/api/http/index';
import * as envURL from '@/api/requrls/project-management/envManagement';

import type {
  EnvDetailItem,
  EnvGroupListItem,
  EnvGroupProjectListItem,
  EnvListItem,
  GlobalParams,
} from '@/models/projectManagement/environmental';
import { OptionsItem } from '@/models/setting/log';

export function updateOrAddEnv(data: EnvDetailItem) {
  return MSR.post<EnvDetailItem>({ url: data.id ? envURL.updateEnvUrl : envURL.addEnvUrl, data });
}
export function listEnv(data: { projectId: string; keyword: string }) {
  return MSR.post<EnvListItem[]>({ url: envURL.listEnvUrl, data });
}
export function importEnv(data: { request: EnvListItem; fileList: FileItem[] }) {
  return MSR.uploadFile({ url: envURL.importEnvUrl }, data, '', true);
}
export function getEntryEnv(data: EnvListItem) {
  return MSR.post<EnvListItem>({ url: envURL.getEntryEnvUrl, data });
}
export function exportEnv(id: string) {
  return MSR.get<EnvListItem>({ url: envURL.exportEnvUrl + id, responseType: 'blob' }, { isTransformResponse: false });
}
export function editPosEnv(data: EnvListItem) {
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
export function deleteEnv(data: EnvListItem) {
  return MSR.post<EnvListItem>({ url: envURL.deleteEnvUrl, data });
}
export function groupUpdateEnv(data: EnvListItem) {
  return MSR.post<EnvListItem>({ url: envURL.groupUpdateEnvUrl, data });
}
export function groupListEnv(data: EnvGroupListItem) {
  return MSR.post<EnvListItem>({ url: envURL.groupListEnvUrl, data });
}
export function groupEditPosEnv(data: EnvGroupListItem) {
  return MSR.post<EnvListItem>({ url: envURL.groupEditPosEnvUrl, data });
}
export function groupAddEnv(data: EnvGroupListItem) {
  return MSR.post<EnvListItem>({ url: envURL.groupAddEnvUrl, data });
}
export function groupDetailEnv(data: EnvListItem) {
  return MSR.post<EnvListItem>({ url: envURL.groupDetailEnvUrl, data });
}
export function groupDeleteEnv(data: EnvListItem) {
  return MSR.post<EnvListItem>({ url: envURL.groupDeleteEnvUrl, data });
}
export function groupProjectEnv(data: EnvGroupProjectListItem) {
  return MSR.post<EnvListItem>({ url: envURL.groupProjectEnvUrl, data });
}

/** 项目管理-环境-全局参数-更新or新增 */
export function updateOrAddGlobalParam(data: GlobalParams) {
  return MSR.post<EnvListItem>({ url: data.id ? envURL.updateGlobalParamUrl : envURL.addGlobalParamUrl, data });
}
/** 项目管理-环境-全局参数-导入 */
export function importGlobalParam(data: { request: any; fileList: FileItem[] }) {
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
