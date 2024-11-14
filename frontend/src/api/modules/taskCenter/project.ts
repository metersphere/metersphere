import MSR from '@/api/http/index';
import {
  projectBatchCloseTaskUrl,
  projectBatchDeleteTaskUrl,
  projectBatchOpenTaskUrl,
  projectBatchStopTaskDetailUrl,
  projectBatchStopTaskUrl,
  projectBatchTaskReportUrl,
  projectDeleteScheduleUrl,
  projectDeleteTaskUrl,
  projectEditCronUrl,
  projectExecuteTaskDetailListUrl,
  projectExecuteTaskListUrl,
  projectExecuteTaskStatisticsUrl,
  projectScheduleSwitchUrl,
  projectScheduleTaskListUrl,
  projectStopTaskDetailUrl,
  projectStopTaskUrl,
  projectTaskCenterResourcePoolsUrl,
  projectTaskOrderUrl,
  projectTaskRerunUrl,
  scheduleProCenterListUrl,
} from '@/api/requrls/taskCenter';

import type { CommonList, TableQueryParams } from '@/models/common';
import type { TimingTaskCenterApiCaseItem } from '@/models/projectManagement/taskCenter';
import type {
  TaskCenterBatchParams,
  TaskCenterBatchTaskReportItem,
  TaskCenterResourcePoolItem,
  TaskCenterStatisticsItem,
  TaskCenterSystemTaskItem,
  TaskCenterTaskDetailItem,
  TaskCenterTaskItem,
} from '@/models/taskCenter';

// 项目任务-系统后台任务列表
export function getProjectScheduleList(data: TableQueryParams) {
  return MSR.post<CommonList<TaskCenterSystemTaskItem>>({ url: projectScheduleTaskListUrl, data });
}

// 项目任务-获取任务详情列表
export function getProjectExecuteTaskDetailList(data: TaskCenterBatchParams) {
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

// 项目任务-开启/关闭任务
export function projectScheduleSwitch(id: string) {
  return MSR.get({ url: `${projectScheduleSwitchUrl}/${id}` });
}

// 项目任务-任务排队信息
export function projectTaskOrder(data: string[]) {
  return MSR.post<Record<string, any>>({ url: projectTaskOrderUrl, data });
}

// 项目任务-批量停止任务
export function projectBatchStopTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: projectBatchStopTaskUrl, data });
}

// 项目任务-用例任务详情-停止任务
export function projectStopTaskDetail(id: string) {
  return MSR.get({ url: `${projectStopTaskDetailUrl}/${id}` });
}

// 项目任务-用例任务详情-批量停止任务
export function projectBatchStopTaskDetail(data: TaskCenterBatchParams) {
  return MSR.post({ url: projectBatchStopTaskDetailUrl, data });
}

// 项目任务-批量删除任务
export function projectBatchDeleteTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: projectBatchDeleteTaskUrl, data });
}

// 项目任务-删除后台任务
export function projectDeleteSchedule(id: string) {
  return MSR.get({ url: `${projectDeleteScheduleUrl}/${id}` });
}

// 项目任务-批量开启后台任务
export function projectBatchOpenTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: projectBatchOpenTaskUrl, data });
}

// 项目任务-批量关闭后台任务
export function projectBatchCloseTask(data: TaskCenterBatchParams) {
  return MSR.post({ url: projectBatchCloseTaskUrl, data });
}

// 项目任务-编辑 cron 表达式
export function projectEditCron(cron: string, id: string) {
  return MSR.post({ url: projectEditCronUrl, data: { cron, id } });
}

// 项目任务-批量任务报告列表
export function projectBatchTaskReportList(data: TaskCenterBatchParams) {
  return MSR.post<CommonList<TaskCenterBatchTaskReportItem>>({ url: projectBatchTaskReportUrl, data });
}

// 项目任务-重跑
export function projectTaskRerun(id: string) {
  return MSR.get({ url: `${projectTaskRerunUrl}/${id}` });
}
