import type { ExecuteTriggerMode } from '@/enums/taskCenter';

import type { TableQueryParams } from './common';

export interface TaskCenterSystemTaskItem {
  organizationName: string; // 所属组织名称
  projectName: string; // 所属项目名称
  projectId: string; // 项目ID
  organizationId: string; // 组织ID
  id: string;
  taskName: string;
  resourceId: string; // 资源ID
  num: number;
  resourceType: string; // 资源类型
  value: string;
  nextTime: number;
  enable: boolean;
  createUserId: string;
  createUserName: string;
  createTime: number;
}

export interface TaskCenterTaskDetailParams extends TableQueryParams {
  taskId: string;
  resourcePoolId: string;
}

export interface TaskCenterTaskItem {
  id: string;
  num: number;
  taskName: string;
  status: string; // 执行状态
  caseCount: number;
  result: string; // 执行结果
  taskType: string; // 任务类型
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
  taskId: string; // 任务ID
  resourceId: string;
  resourceName: string;
  taskOrigin: string; // 任务来源
  status: string; // 执行状态
  result: string; // 执行结果
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
  lineNum: number;
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
