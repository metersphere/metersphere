import MSR from '@/api/http/index';
import {
  systemBatchCloseTaskUrl,
  systemBatchDeleteTaskUrl,
  systemBatchOpenTaskUrl,
  systemBatchStopTaskDetailUrl,
  systemBatchStopTaskUrl,
  systemBatchTaskReportUrl,
  systemDeleteScheduleUrl,
  systemDeleteTaskUrl,
  systemEditCronUrl,
  systemExecuteTaskDetailListUrl,
  systemExecuteTaskListUrl,
  systemExecuteTaskStatisticsUrl,
  systemOrgOptionsUrl,
  systemProjectOptionsUrl,
  systemResourcePoolStatusUrl,
  systemScheduleListUrl,
  systemScheduleSwitchUrl,
  systemStopTaskDetailUrl,
  systemStopTaskUrl,
  systemTaskCenterResourcePoolsUrl,
  systemTaskOrderUrl,
  systemTaskRerunUrl,
} from '@/api/requrls/taskCenter';

import type { CommonList, TableQueryParams } from '@/models/common';
import type { ProjectListItem } from '@/models/setting/project';
import type { OrganizationListItem } from '@/models/setting/user';
import type {
  TaskCenterBatchParams,
  TaskCenterBatchTaskReportItem,
  TaskCenterResourcePoolItem,
  TaskCenterResourcePoolStatus,
  TaskCenterStatisticsItem,
  TaskCenterSystemTaskItem,
  TaskCenterTaskDetailItem,
  TaskCenterTaskItem,
} from '@/models/taskCenter';

// 系统任务-系统后台任务列表
export function getSystemScheduleList(data: TableQueryParams) {
  return MSR.post<CommonList<TaskCenterSystemTaskItem>>({ url: systemScheduleListUrl, data });
}

// 系统任务-开启/关闭任务
export function systemScheduleSwitch(id: string) {
  return MSR.get({ url: `${systemScheduleSwitchUrl}/${id}` });
}

// 系统任务-获取任务详情列表
export function getSystemExecuteTaskDetailList(data: TaskCenterBatchParams) {
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

// 系统任务-任务排队信息
export function systemTaskOrder(data: string[]) {
  return MSR.post<Record<string, any>>({ url: systemTaskOrderUrl, data });
}

// 系统任务-停止任务
export function systemStopTask(id: string) {
  return MSR.get({ url: `${systemStopTaskUrl}/${id}` });
}

// 系统任务-批量停止任务
export function systemBatchStopTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: systemBatchStopTaskUrl, data });
}

// 系统任务-用例任务详情-停止任务
export function systemStopTaskDetail(id: string) {
  return MSR.get({ url: `${systemStopTaskDetailUrl}/${id}` });
}

// 系统任务-用例任务详情-批量停止任务
export function systemBatchStopTaskDetail(data: TaskCenterBatchParams) {
  return MSR.post({ url: systemBatchStopTaskDetailUrl, data });
}

// 系统任务-删除任务
export function systemDeleteTask(id: string) {
  return MSR.get({ url: `${systemDeleteTaskUrl}/${id}` });
}

// 系统任务-批量删除任务
export function systemBatchDeleteTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: systemBatchDeleteTaskUrl, data });
}

// 系统任务-删除后台任务
export function systemDeleteSchedule(id: string) {
  return MSR.get({ url: `${systemDeleteScheduleUrl}/${id}` });
}

// 任务中心-资源池状态
export function getResourcePoolsStatus(data: string[]) {
  return MSR.post<TaskCenterResourcePoolStatus[]>({ url: systemResourcePoolStatusUrl, data });
}

// 系统任务-批量开启后台任务
export function systemBatchOpenTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: systemBatchOpenTaskUrl, data });
}

// 系统任务-批量关闭后台任务
export function systemBatchCloseTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: systemBatchCloseTaskUrl, data });
}

// 系统任务-编辑 cron 表达式
export function systemEditCron(cron: string, id: string) {
  return MSR.post({ url: systemEditCronUrl, data: { cron, id } });
}

// 系统任务-批量任务报告列表
export function systemBatchTaskReportList(data: TaskCenterBatchParams) {
  return MSR.post<CommonList<TaskCenterBatchTaskReportItem>>({ url: systemBatchTaskReportUrl, data });
}

// 系统任务-组织列表
export function systemOrgOptions() {
  return MSR.get<OrganizationListItem[]>({ url: systemOrgOptionsUrl });
}

// 系统任务-项目列表
export function systemProjectOptions() {
  return MSR.get<ProjectListItem[]>({ url: systemProjectOptionsUrl });
}

// 系统任务-重跑
export function systemTaskRerun(id: string) {
  return MSR.get({ url: `${systemTaskRerunUrl}/${id}` });
}
