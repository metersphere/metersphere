import MSR from '@/api/http/index';
import {
  AddCommonScriptUrl,
  ConnectionWebsocketUrl,
  DeleteCommonScriptUrl,
  getChangeHistoryUrl,
  GetCommonScriptDetailUrl,
  GetCommonScriptPageUrl,
  GetCommonScriptStatusUrl,
  getCommonScriptUrl,
  GetCustomFuncColumnsOptionUrl,
  GetFormApiImportModuleCountUrl,
  GetFormApiImportPageListUrl,
  GetFormApiImportUrl,
  GetInsertCommonScriptPageUrl,
  TestScriptUrl,
  UpdateCommonScriptStatusUrl,
  UpdateCommonScriptUrl,
} from '@/api/requrls/project-management/commonScript';

import type { CommonScriptInfo } from '@/models/apiTest/common';
import type { ModulesTreeType } from '@/models/caseManagement/featureCase';
import { CommonList, TableQueryParams } from '@/models/common';
import type {
  AddOrUpdateCommonScript,
  changeHistory,
  CommonScriptItem,
  TestScriptType,
} from '@/models/projectManagement/commonScript';
import { CustomFuncColumnOptionItem, updateCommonScriptStatus } from '@/models/projectManagement/commonScript';

// 获取公共脚本列表
export function getCommonScriptPage(data: TableQueryParams) {
  return MSR.post<CommonList<CommonScriptItem[]>>({ url: GetCommonScriptPageUrl, data });
}

// 添加公共脚本
export function addOrUpdateCommonScriptReq(data: AddOrUpdateCommonScript) {
  if (data.id) {
    return MSR.post({ url: UpdateCommonScriptUrl, data });
  }
  return MSR.post({ url: AddCommonScriptUrl, data });
}
// 更新公共脚本
export function updateCommonScript(data: AddOrUpdateCommonScript) {
  return MSR.post({ url: UpdateCommonScriptUrl, data });
}

// 更新公共脚本状态
export function updateStatusCommonScript(data: updateCommonScriptStatus) {
  return MSR.post({ url: UpdateCommonScriptStatusUrl, data });
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
// 获取公共脚本变更历史详情
export function getChangeHistory(data: TableQueryParams) {
  return MSR.post<CommonList<changeHistory[]>>({ url: getChangeHistoryUrl, data });
}

/**
 * 表格筛选字段的数据查询
 */
export function getCustomFuncColumnOption(projectId: string) {
  return MSR.get<CustomFuncColumnOptionItem>({ url: `${GetCustomFuncColumnsOptionUrl}${projectId}` });
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

// 测试脚本
export function testCommonScript(data: TestScriptType) {
  return MSR.post({ url: TestScriptUrl, data });
}
/**
 * apiSocket 建立连接
 * @param url 连接地址
 * @param host 连接主机
 */
export const apiSocket = (url: string, host?: string) => {
  let protocol = 'ws://';

  // 根据传入的 url 判断是否为 https 协议
  if (!host?.startsWith('http') && (window.location.protocol === 'https:' || host?.startsWith('https'))) {
    protocol = 'wss://';
  }
  const uri = protocol + (host?.split('://')[1] || window.location.host) + url;
  return new WebSocket(uri);
};

export function getSocket(reportId: string | number, socketUrl?: string, host?: string) {
  return apiSocket(`${socketUrl || ConnectionWebsocketUrl}/${reportId}`, host);
}

// 获取单个公共脚本详情
export function getCommonScript(scriptId: string) {
  return MSR.get<CommonScriptInfo>({ url: `${getCommonScriptUrl}/${scriptId}` });
}
