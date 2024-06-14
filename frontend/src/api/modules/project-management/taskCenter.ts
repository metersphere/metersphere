import MSR from '@/api/http';
import {
  batchDisableScheduleOrgTaskUrl,
  batchDisableScheduleProTaskUrl,
  batchDisableScheduleSysTaskUrl,
  batchEnableScheduleOrgTaskUrl,
  batchEnableScheduleProTaskUrl,
  batchEnableScheduleSysTaskUrl,
  batchStopRealOrdApiUrl,
  batchStopRealProjectApiUrl,
  batchStopRealSystemApiUrl,
  deleteScheduleOrgTaskUrl,
  deleteScheduleProTaskUrl,
  deleteScheduleSysTaskUrl,
  enableSchedule,
  enableScheduleOrgTaskUrl,
  enableScheduleProTaskUrl,
  enableScheduleSysTaskUrl,
  orgRealTotal,
  orgScheduleTotal,
  projectRealTotal,
  projectScheduleTotal,
  scheduleOrgCenterListUrl,
  scheduleProCenterListUrl,
  scheduleSysCenterListUrl,
  stopRealOrdApiUrl, stopRealOrgPlanUrl,
  stopRealProjectApiUrl, stopRealProjectPlanUrl,
  stopRealSysApiUrl, stopRealSysPlanUrl,
  systemRealTotal,
  systemScheduleTotal,
  taskOrgPlanRealCenterListUrl,
  taskOrgRealCenterListUrl,
  taskProPlanRealCenterListUrl,
  taskProRealCenterListUrl,
  taskSysPlanRealCenterListUrl,
  taskSysRealCenterListUrl,
  updateScheduleOrgTaskUrl,
  updateScheduleProTaskUrl,
  updateScheduleSysTaskUrl,
} from '@/api/requrls/project-management/taskCenter';

import type { CommonList, TableQueryParams } from '@/models/common';
import type { RealTaskCenterApiCaseItem, TimingTaskCenterApiCaseItem } from '@/models/projectManagement/taskCenter';
import { TaskCenterEnum } from '@/enums/taskCenter';

// 实时任务
export function getRealSysApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: taskSysRealCenterListUrl, data });
}

export function batchStopRealSystemApi(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: batchStopRealSystemApiUrl, data });
}

export function getRealOrdApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: taskOrgRealCenterListUrl, data });
}

export function batchStopRealOrdApi(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: batchStopRealOrdApiUrl, data });
}

export function getRealProApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: taskProRealCenterListUrl, data });
}

export function batchStopRealProjectApi(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: batchStopRealProjectApiUrl, data });
}

export function stopRealSysApi(moduleType: keyof typeof TaskCenterEnum, id: string) {
  return MSR.get({ url: `${stopRealSysApiUrl}/${moduleType}/${id}` });
}

export function stopRealOrdApi(moduleType: keyof typeof TaskCenterEnum, id: string) {
  return MSR.get({ url: `${stopRealOrdApiUrl}/${moduleType}/${id}` });
}

export function stopRealProjectApi(moduleType: keyof typeof TaskCenterEnum, id: string) {
  return MSR.get({ url: `${stopRealProjectApiUrl}/${moduleType}/${id}` });
}

// 定时任务
export function getScheduleSysApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<TimingTaskCenterApiCaseItem>>({ url: scheduleSysCenterListUrl, data });
}

export function getScheduleOrgApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<TimingTaskCenterApiCaseItem>>({ url: scheduleOrgCenterListUrl, data });
}

export function getScheduleProApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<TimingTaskCenterApiCaseItem>>({ url: scheduleProCenterListUrl, data });
}

// 系统删除定时任务
export function deleteScheduleSysTask(moduleType: keyof typeof TaskCenterEnum, id: string) {
  return MSR.get({ url: `${deleteScheduleSysTaskUrl}/${moduleType}/${id}` });
}

// 组织删除定时任务
export function deleteScheduleOrgTask(moduleType: keyof typeof TaskCenterEnum, id: string) {
  return MSR.get({ url: `${deleteScheduleOrgTaskUrl}/${moduleType}/${id}` });
}

// 项目删除定时任务
export function deleteScheduleProTask(moduleType: keyof typeof TaskCenterEnum, id: string) {
  return MSR.get({ url: `${deleteScheduleProTaskUrl}/${moduleType}/${id}` });
}

// 系统启用定时任务
export function enableScheduleSysTask(moduleType: keyof typeof TaskCenterEnum, id: string) {
  return MSR.get({ url: `${enableScheduleSysTaskUrl}/${moduleType}/${id}` });
}

// 组织启用定时任务
export function enableScheduleOrgTask(moduleType: keyof typeof TaskCenterEnum, id: string) {
  return MSR.get({ url: `${enableScheduleOrgTaskUrl}/${moduleType}/${id}` });
}

// 项目启用定时任务
export function enableScheduleProTask(moduleType: keyof typeof TaskCenterEnum, id: string) {
  return MSR.get({ url: `${enableScheduleProTaskUrl}/${moduleType}/${id}` });
}

// 系统更新定时任务规则
export function updateRunRules(moduleType: keyof typeof TaskCenterEnum, id: string, data: string) {
  return MSR.post({ url: `${updateScheduleSysTaskUrl}/${moduleType}/${id}`, data });
}

// 组织更新定时任务规则
export function updateRunRulesOrg(moduleType: keyof typeof TaskCenterEnum, id: string, data: string) {
  return MSR.post({ url: `${updateScheduleOrgTaskUrl}/${moduleType}/${id}`, data });
}

// 项目更新定时任务规则
export function updateRunRulesPro(moduleType: keyof typeof TaskCenterEnum, id: string, data: string) {
  return MSR.post({ url: `${updateScheduleProTaskUrl}/${moduleType}/${id}`, data });
}

// 系统批量开启定时任务
export function batchEnableScheduleSysTask(data: TableQueryParams) {
  return MSR.post({ url: `${batchEnableScheduleSysTaskUrl}`, data });
}

// 组织批量开启定时任务
export function batchEnableScheduleOrgTask(data: TableQueryParams) {
  return MSR.post({ url: `${batchEnableScheduleOrgTaskUrl}`, data });
}

// 项目批量开启定时任务
export function batchEnableScheduleProTask(data: TableQueryParams) {
  return MSR.post({ url: `${batchEnableScheduleProTaskUrl}`, data });
}

// 系统批量关闭定时任务
export function batchDisableScheduleSysTask(data: TableQueryParams) {
  return MSR.post({ url: `${batchDisableScheduleSysTaskUrl}`, data });
}

// 组织批量关闭定时任务
export function batchDisableScheduleOrgTask(data: TableQueryParams) {
  return MSR.post({ url: `${batchDisableScheduleOrgTaskUrl}`, data });
}

// 项目批量关闭定时任务
export function batchDisableScheduleProTask(data: TableQueryParams) {
  return MSR.post({ url: `${batchDisableScheduleProTaskUrl}`, data });
}

export function switchSchedule(id: string) {
  return MSR.get({ url: `${enableSchedule}/${id}` });
}

export function getSystemScheduleTotal() {
  return MSR.get({ url: `${systemScheduleTotal}` });
}

export function getOrgScheduleTotal() {
  return MSR.get({ url: `${orgScheduleTotal}` });
}

export function getProjectScheduleTotal() {
  return MSR.get({ url: `${projectScheduleTotal}` });
}

export function getSystemRealTotal() {
  return MSR.get({ url: `${systemRealTotal}` });
}

export function getOrgRealTotal() {
  return MSR.get({ url: `${orgRealTotal}` });
}

export function getProjectRealTotal() {
  return MSR.get({ url: `${projectRealTotal}` });
}

// 实时任务 测试计划
export function getRealSysPlanList(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: taskSysPlanRealCenterListUrl, data });
}

export function getRealOrgPlanList(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: taskOrgPlanRealCenterListUrl, data });
}

export function getRealProPlanList(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: taskProPlanRealCenterListUrl, data });
}

export function stopRealSysPlan(id: string) {
  return MSR.get({ url: `${stopRealSysPlanUrl}/${id}` });
}

export function stopRealOrgPlan(id: string) {
  return MSR.get({ url: `${stopRealOrgPlanUrl}/${id}` });
}

export function stopRealProPlan(id: string) {
  return MSR.get({ url: `${stopRealProjectPlanUrl}/${id}` });
}

export function batchStopRealSysPlan(data: TableQueryParams) {
  return MSR.post({ url: `${stopRealSysPlanUrl}`, data });
}

export function batchStopRealOrgPlan(data: TableQueryParams) {
  return MSR.post({ url: `${stopRealOrgPlanUrl}`, data });
}

export function batchStopRealProPlan(data: TableQueryParams) {
  return MSR.post({ url: `${stopRealProjectPlanUrl}`, data });
}

export default {};
