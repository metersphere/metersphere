import MSR from '@/api/http';
import {
  deleteScheduleSysTaskUrl,
  scheduleOrgCenterListUrl,
  scheduleProCenterListUrl,
  scheduleSysCenterListUrl,
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

export function getRealOrdApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: taskOrgRealCenterListUrl, data });
}

export function getRealProApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<RealTaskCenterApiCaseItem>>({ url: taskProRealCenterListUrl, data });
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
