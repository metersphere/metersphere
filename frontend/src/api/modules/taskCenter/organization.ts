import MSR from '@/api/http/index';
import {
  organizationBatchCloseTaskUrl,
  organizationBatchDeleteTaskUrl,
  organizationBatchOpenTaskUrl,
  organizationBatchStopTaskDetailUrl,
  organizationBatchStopTaskUrl,
  organizationBatchTaskReportUrl,
  organizationDeleteScheduleUrl,
  organizationDeleteTaskUrl,
  organizationEditCronUrl,
  organizationExecuteTaskDetailListUrl,
  organizationExecuteTaskListUrl,
  organizationExecuteTaskStatisticsUrl,
  organizationProjectOptionsUrl,
  organizationScheduleListUrl,
  organizationScheduleSwitchUrl,
  organizationStopTaskDetailUrl,
  organizationStopTaskUrl,
  organizationTaskCenterResourcePoolsUrl,
  organizationTaskOrderUrl,
  organizationTaskRerunUrl,
} from '@/api/requrls/taskCenter';

import type { CommonList, TableQueryParams } from '@/models/common';
import type { ProjectListItem } from '@/models/setting/project';
import type {
  TaskCenterBatchParams,
  TaskCenterBatchTaskReportItem,
  TaskCenterResourcePoolItem,
  TaskCenterStatisticsItem,
  TaskCenterSystemTaskItem,
  TaskCenterTaskDetailItem,
  TaskCenterTaskItem,
} from '@/models/taskCenter';

// 组织任务-系统后台任务列表
export function getOrganizationScheduleList(data: TableQueryParams) {
  return MSR.post<CommonList<TaskCenterSystemTaskItem>>({ url: organizationScheduleListUrl, data });
}

// 组织任务-获取任务详情列表
export function getOrganizationExecuteTaskDetailList(data: TaskCenterBatchParams) {
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

// 组织任务-开启/关闭任务
export function organizationScheduleSwitch(id: string) {
  return MSR.get({ url: `${organizationScheduleSwitchUrl}/${id}` });
}

// 组织任务-任务排队信息
export function organizationTaskOrder(data: string[]) {
  return MSR.post<Record<string, any>>({ url: organizationTaskOrderUrl, data });
}

// 组织任务-批量停止任务
export function organizationBatchStopTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: organizationBatchStopTaskUrl, data });
}

// 组织任务-用例任务详情-停止任务
export function organizationStopTaskDetail(id: string) {
  return MSR.get({ url: `${organizationStopTaskDetailUrl}/${id}` });
}

// 组织任务-用例任务详情-批量停止任务
export function organizationBatchStopTaskDetail(data: TaskCenterBatchParams) {
  return MSR.post({ url: organizationBatchStopTaskDetailUrl, data });
}

// 组织任务-批量删除任务
export function organizationBatchDeleteTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: organizationBatchDeleteTaskUrl, data });
}

// 组织任务-删除后台任务
export function organizationDeleteSchedule(id: string) {
  return MSR.get({ url: `${organizationDeleteScheduleUrl}/${id}` });
}

// 组织任务-批量开启后台任务
export function organizationBatchOpenTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: organizationBatchOpenTaskUrl, data });
}

// 组织任务-批量关闭后台任务
export function organizationBatchCloseTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: organizationBatchCloseTaskUrl, data });
}

// 组织任务-编辑 cron 表达式
export function organizationEditCron(cron: string, id: string) {
  return MSR.post({ url: organizationEditCronUrl, data: { cron, id } });
}

// 组织任务-批量任务报告列表
export function organizationBatchTaskReportList(data: TaskCenterBatchParams) {
  return MSR.post<CommonList<TaskCenterBatchTaskReportItem>>({ url: organizationBatchTaskReportUrl, data });
}

// 组织任务-项目列表
export function organizationProjectOptions() {
  return MSR.get<ProjectListItem[]>({ url: organizationProjectOptionsUrl });
}

// 组织任务-重跑
export function organizationTaskRerun(id: string) {
  return MSR.get({ url: `${organizationTaskRerunUrl}/${id}` });
}
