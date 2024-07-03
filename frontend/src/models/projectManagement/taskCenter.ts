// 实时

import { testPlanTypeEnum } from '@/enums/testPlanEnum';

export interface RealTaskCenterApiCaseItem {
  organizationName: string; // 所属组织
  projectName: string;
  projectId: string;
  id: string;
  resourceId: string;
  resourceNum: number; // 资源业务id
  resourceName: string; // 资源名称 单独报告显示模块名称 集合报告显示报告名称
  triggerMode: string; // 触发模式（手动，定时，批量，测试计划）
  poolName: string; // 资源池名称
  status: string; // 执行状态/SUCCESS/ERROR
  operationName: string; // 操作人
  operationTime: string;
  integrated: boolean; // 是否为集合报告
}

export interface TestPlanTaskCenterItem extends RealTaskCenterApiCaseItem {
  children: TestPlanTaskCenterItem[];
  childrenCount: number;
  groupId: string;
  type: keyof typeof testPlanTypeEnum;
}
// 定时任务
export interface TimingTaskCenterApiCaseItem {
  organizationName: string;
  projectName: string;
  projectId: string;
  id: string;
  taskName: string; // 任务名称
  resourceId: string; // 资源Id
  resourceNum: number; // 资源业务id
  resourceName: string; // 资源名称
  resourceType: string; // 资源分类
  value: string;
  nextTime: string; // 下次执行时间
  enable: true; // 任务状态
  createUserName: string;
  createTime: string;
}
