import MSR from '@/api/http/index';
import {
  organizationDeleteTaskUrl,
  organizationExecuteTaskDetailListUrl,
  organizationExecuteTaskListUrl,
  organizationExecuteTaskStatisticsUrl,
  organizationScheduleListUrl,
  organizationStopTaskUrl,
  organizationTaskCenterResourcePoolsUrl,
  projectDeleteTaskUrl,
  projectExecuteTaskDetailListUrl,
  projectExecuteTaskListUrl,
  projectExecuteTaskStatisticsUrl,
  projectScheduleTaskListUrl,
  projectStopTaskUrl,
  projectTaskCenterResourcePoolsUrl,
  scheduleProCenterListUrl,
  systemDeleteTaskUrl,
  systemExecuteTaskDetailListUrl,
  systemExecuteTaskListUrl,
  systemExecuteTaskStatisticsUrl,
  systemResourcePoolStatusUrl,
  systemScheduleListUrl,
  systemStopTaskUrl,
  systemTaskCenterResourcePoolsUrl,
} from '@/api/requrls/taskCenter';

import type { CommonList, TableQueryParams } from '@/models/common';
import type { TimingTaskCenterApiCaseItem } from '@/models/projectManagement/taskCenter';
import type {
  TaskCenterResourcePoolItem,
  TaskCenterResourcePoolStatus,
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

// 项目任务-获取资源池列表
export function getProjectTaskCenterResourcePools() {
  return MSR.get<TaskCenterResourcePoolItem[]>({ url: projectTaskCenterResourcePoolsUrl });
}

// 项目任务-停止任务
export function projectStopTask(id: string) {
  return MSR.get({ url: `${projectStopTaskUrl}/${id}` });
}

// 项目任务-删除任务
export function projectDeleteTask(id: string) {
  return MSR.get({ url: `${projectDeleteTaskUrl}/${id}` });
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

// 系统任务-获取资源池列表
export function getSystemTaskCenterResourcePools() {
  return MSR.get<TaskCenterResourcePoolItem[]>({ url: systemTaskCenterResourcePoolsUrl });
}

// 系统任务-停止任务
export function systemStopTask(id: string) {
  return MSR.get({ url: `${systemStopTaskUrl}/${id}` });
}

// 系统任务-删除任务
export function systemDeleteTask(id: string) {
  return MSR.get({ url: `${systemDeleteTaskUrl}/${id}` });
}

// 任务中心-资源池状态
export function getResourcePoolsStatus(data: string[]) {
  return MSR.post<TaskCenterResourcePoolStatus[]>({ url: systemResourcePoolStatusUrl, data });
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

// 组织任务-获取资源池列表
export function getOrgTaskCenterResourcePools() {
  return MSR.get<TaskCenterResourcePoolItem[]>({ url: organizationTaskCenterResourcePoolsUrl });
}

// 组织任务-停止任务
export function organizationStopTask(id: string) {
  return MSR.get({ url: `${organizationStopTaskUrl}/${id}` });
}

// 组织任务-删除任务
export function organizationDeleteTask(id: string) {
  return MSR.get({ url: `${organizationDeleteTaskUrl}/${id}` });
}
