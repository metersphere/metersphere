import MSR from '@/api/http/index';
import {
  AddCommonScriptUrl,
  DeleteCommonScriptUrl,
  GetCommonScriptDetailUrl,
  GetCommonScriptPageUrl,
  GetCommonScriptStatusUrl,
  GetFormApiImportModuleCountUrl,
  GetFormApiImportPageListUrl,
  GetFormApiImportUrl,
  GetInsertCommonScriptPageUrl,
  UpdateCommonScriptUrl,
} from '@/api/requrls/project-management/commonScript';

import type { ModulesTreeType } from '@/models/caseManagement/featureCase';
import { CommonList, TableQueryParams } from '@/models/common';
import type { AddOrUpdateCommonScript, CommonScriptItem } from '@/models/projectManagement/commonScript';

// 获取公共脚本列表
export function getCommonScriptPage(data: TableQueryParams) {
  return MSR.post<CommonList<CommonScriptItem[]>>({ url: GetCommonScriptPageUrl, data });
}

// 添加公共脚本
export function addCommonScriptReq(data: AddOrUpdateCommonScript) {
  return MSR.post({ url: AddCommonScriptUrl, data });
}
// 更新公共脚本
export function updateCommonScript(data: AddOrUpdateCommonScript) {
  return MSR.post({ url: UpdateCommonScriptUrl, data });
}
// 获取公共脚本详情
export function getCommonScriptDetail(id: string) {
  return MSR.get<CommonScriptItem>({ url: `${GetCommonScriptDetailUrl}/${id}` });
}
// 删除公共脚本
export function deleteCommonScript(id: string) {
  return MSR.get({ url: `${DeleteCommonScriptUrl}/${id}` });
}
// 脚本更新状态
export function getCommonScriptStatus(data: AddOrUpdateCommonScript) {
  return MSR.post({ url: GetCommonScriptStatusUrl, data });
}

// 获取插入脚本列表
export function getInsertCommonScriptPage(data: TableQueryParams) {
  return MSR.post<CommonList<CommonScriptItem[]>>({ url: GetInsertCommonScriptPageUrl, data });
}

// 获取从Api导入
export function getFormApiImportModule(data: TableQueryParams) {
  return MSR.post<ModulesTreeType[]>({ url: GetFormApiImportUrl, data });
}
// 获取从api导入列表
export function getFormApiImportPageList(data: TableQueryParams) {
  return MSR.post<CommonList<any[]>>({ url: GetFormApiImportPageListUrl, data });
}
// 获取从api接口导入模块数量
export function getFormApiImportModuleCount(data: TableQueryParams) {
  return MSR.post<Record<string, any>>({ url: GetFormApiImportModuleCountUrl, data });
}
