import MSR from '@/api/http';
import {
  batchStopRealOrdApiUrl,
  batchStopRealProjectApiUrl,
  batchStopRealSystemApiUrl,
  deleteScheduleSysTaskUrl,
  scheduleOrgCenterListUrl,
  scheduleProCenterListUrl,
  scheduleSysCenterListUrl,
  stopRealOrdApiUrl,
  stopRealProjectApiUrl,
  stopRealSysApiUrl,
  taskOrgRealCenterListUrl,
  taskProRealCenterListUrl,
  taskSysRealCenterListUrl,
} from '@/api/requrls/project-management/taskCenter';

import type { CommonList, TableQueryParams } from '@/models/common';
import type { RealTaskCenterApiCaseItem, TimingTaskCenterApiCaseItem } from '@/models/projectManagement/taskCenter';

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

export function stopRealSysApi(moduleType: string, id: string) {
  return MSR.get({ url: `${stopRealSysApiUrl}/${moduleType}/${id}` });
}
export function stopRealOrdApi(moduleType: string, id: string) {
  return MSR.get({ url: `${stopRealOrdApiUrl}/${moduleType}/${id}` });
}
export function stopRealProjectApi(moduleType: string, id: string) {
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

export function deleteScheduleSysTask(id: string) {
  return MSR.get({ url: `${deleteScheduleSysTaskUrl}/${id}` });
}
export default {};
