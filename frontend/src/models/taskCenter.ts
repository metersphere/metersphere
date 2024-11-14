import type { ExecuteResultEnum, ExecuteStatusEnum, ExecuteTaskType, ExecuteTriggerMode } from '@/enums/taskCenter';

import type { TableQueryParams } from './common';

export interface TaskCenterSystemTaskItem {
  organizationName: string; // 所属组织名称
  projectName: string; // 所属项目名称
  projectId: string; // 项目ID
  organizationId: string; // 组织ID
  id: string;
  reportId: string;
  taskName: string;
  resourceId: string; // 资源ID
  num: number;
  resourceType: string; // 资源类型
  resourceNum: number; // 资源num
  value: string;
  nextTime: number;
  enable: boolean;
  createUserId: string;
  createUserName: string;
  createTime: number;
  [key: string]: any;
}

export interface TaskCenterBatchParams extends TableQueryParams {
  taskId?: string;
  batchType?: string;
  resourcePoolIds?: string[];
  resourcePoolNodes?: string[];
}

export interface TaskCenterTaskItem {
  id: string;
  reportId: string;
  num: number;
  taskName: string;
  status: string; // 执行状态
  caseCount: number;
  result: ExecuteResultEnum; // 执行结果
  taskType: ExecuteTaskType; // 任务类型
  resourceId: string;
  triggerMode: ExecuteTriggerMode; // 执行方式
  projectId: string;
  organizationId: string;
  createTime: number;
  createUser: string;
  startTime: number;
  endTime: number;
  organizationName: string; // 所属组织名称
  projectName: string; // 所属项目名称
  createUserName: string; // 创建人
  [key: string]: any;
}

export interface TaskCenterTaskDetailItem {
  id: string;
  reportId: string;
  taskId: string; // 任务ID
  resourceId: string;
  resourceName: string;
  taskOrigin: string; // 任务来源
  status: ExecuteStatusEnum; // 执行状态
  result: ExecuteResultEnum; // 执行结果
  resourcePoolId: string; // 资源池ID
  resourcePoolNode: string; // 资源池节点
  resourceType: string; // 资源类型
  projectId: string;
  organizationId: string;
  threadId: string; // 线程ID
  startTime: number;
  endTime: number;
  executor: string;
  taskName: string;
  userName: string;
  resourcePoolName: string;
  triggerMode: string; // 触发方式
  lineNum: number | string;
}

export interface TaskCenterStatisticsItem {
  id: string;
  executeRate: number; // 执行率
  successCount: number; // 成功数
  errorCount: number; // 失败数
  fakeErrorCount: number; // 误报数
  pendingCount: number; // 待执行数
  caseTotal: number; // 用例总数
}

export interface TaskCenterResourcePoolStatus {
  id: string;
  status: boolean; // 状态, true: 正常, false: 异常
}

export interface TaskCenterResourcePoolItem {
  id: string;
  name: string;
  children: TaskCenterResourcePoolItem[];
}

export interface TaskCenterBatchTaskReportItem {
  id: string;
  source: string;
  integrated: boolean; // 是否集合报告
  name: string;
  status: string;
  execResult: string;
  triggerMode: ExecuteTriggerMode;
  createUser: string;
  createTime: number;
}
