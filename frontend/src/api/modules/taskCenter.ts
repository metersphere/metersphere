import MSR from '@/api/http/index';
import {
  organizationExecuteTaskDetailListUrl,
  organizationExecuteTaskListUrl,
  organizationExecuteTaskStatisticsUrl,
  organizationScheduleListUrl,
  projectExecuteTaskDetailListUrl,
  projectExecuteTaskListUrl,
  projectExecuteTaskStatisticsUrl,
  projectScheduleTaskListUrl,
  scheduleProCenterListUrl,
  systemExecuteTaskDetailListUrl,
  systemExecuteTaskListUrl,
  systemExecuteTaskStatisticsUrl,
  systemScheduleListUrl,
} from '@/api/requrls/taskCenter';

import type { CommonList, TableQueryParams } from '@/models/common';
import type { TimingTaskCenterApiCaseItem } from '@/models/projectManagement/taskCenter';
import type {
  TaskCenterStatisticsItem,
  TaskCenterSystemTaskItem,
  TaskCenterTaskDetailItem,
  TaskCenterTaskDetailParams,
  TaskCenterTaskItem,
} from '@/models/taskCenter';

// 项目任务-系统后台任务列表
export function getProjectScheduleList(data: TableQueryParams) {
  return MSR.post<CommonList<TaskCenterSystemTaskItem>>({ url: projectScheduleTaskListUrl, data });
}

// 项目任务-获取任务详情列表
export function getProjectExecuteTaskDetailList(data: TaskCenterTaskDetailParams) {
  return MSR.post<CommonList<TaskCenterTaskDetailItem>>({ url: projectExecuteTaskDetailListUrl, data });
}

// 项目任务-获取任务列表
export function getProjectExecuteTaskList(data: TableQueryParams) {
  return MSR.post<CommonList<TaskCenterTaskItem>>({ url: projectExecuteTaskListUrl, data });
}

// 项目任务-获取任务统计
export function getProjectExecuteTaskStatistics(data: string[]) {
  return MSR.post<TaskCenterStatisticsItem[]>({ url: projectExecuteTaskStatisticsUrl, data });
}

// 接口测试-定时任务列表
export function getScheduleProApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<TimingTaskCenterApiCaseItem>>({ url: scheduleProCenterListUrl, data });
}

// 系统任务-系统后台任务列表
export function getSystemScheduleList(data: TableQueryParams) {
  return MSR.post<CommonList<TaskCenterSystemTaskItem>>({ url: systemScheduleListUrl, data });
}

// 系统任务-获取任务详情列表
export function getSystemExecuteTaskDetailList(data: TaskCenterTaskDetailParams) {
  return MSR.post<CommonList<TaskCenterTaskDetailItem>>({ url: systemExecuteTaskDetailListUrl, data });
}

// 系统任务-获取任务列表
export function getSystemExecuteTaskList(data: TableQueryParams) {
  return MSR.post<CommonList<TaskCenterTaskItem>>({ url: systemExecuteTaskListUrl, data });
}

// 系统任务-获取任务统计
export function getSystemExecuteTaskStatistics(data: string[]) {
  return MSR.post<TaskCenterStatisticsItem[]>({ url: systemExecuteTaskStatisticsUrl, data });
}

// 组织任务-系统后台任务列表
export function getOrganizationScheduleList(data: TableQueryParams) {
  return MSR.post<CommonList<TaskCenterSystemTaskItem>>({ url: organizationScheduleListUrl, data });
}

// 组织任务-获取任务详情列表
export function getOrganizationExecuteTaskDetailList(data: TaskCenterTaskDetailParams) {
  return MSR.post<CommonList<TaskCenterTaskDetailItem>>({ url: organizationExecuteTaskDetailListUrl, data });
}

// 组织任务-获取任务列表
export function getOrganizationExecuteTaskList(data: TableQueryParams) {
  return MSR.post<CommonList<TaskCenterTaskItem>>({ url: organizationExecuteTaskListUrl, data });
}

// 组织任务-获取任务统计
export function getOrganizationExecuteTaskStatistics(data: string[]) {
  return MSR.post<TaskCenterStatisticsItem[]>({ url: organizationExecuteTaskStatisticsUrl, data });
}
