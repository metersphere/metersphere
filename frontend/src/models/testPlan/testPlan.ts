import { BatchApiParams } from '../common';

export type planStatusType = 'PREPARED' | 'UNDERWAY' | 'COMPLETED' | 'ARCHIVED';

// 计划分页
export interface TestPlanItem {
  id?: string;
  projectId: string;
  num: number;
  name: string;
  status: planStatusType;
  type: string;
  tags: string[];
  schedule: string; // 是否定时
  createUser: string;
  createTime: string;
  moduleName: string;
  moduleId: string;
  children: TestPlanItem[];
  childrenCount: number;
  groupId: string;
}

export interface AssociateFunctionalCaseItem {
  testPlanId: string;
  testPlanNum: number;
  testPlanName: string;
  projectName: string;
  planStatus: string;
}

export interface ResourcesItem {
  id: string;
  name: string;
  cpuRate: string;
  status: boolean;
}

export interface AssociateCaseRequest extends BatchApiParams {
  functionalSelectIds?: string[];
  apiSelectIds?: string[];
  apiCaseSelectIds?: string[];
  apiScenarioSelectIds?: string[];
  totalCount?: number;
}

export interface AddTestPlanParams {
  id?: string;
  name: string;
  projectId: string;
  groupId?: string;
  moduleId: string;
  cycle?: number[];
  plannedStartTime?: number;
  plannedEndTime?: number;
  tags: string[];
  description?: string;
  testPlanning: boolean; // 是否开启测试规划
  automaticStatusUpdate: boolean; // 是否自定更新功能用例状态
  repeatCase: boolean; // 是否允许重复添加用例
  passThreshold: number;
  type: string;
  baseAssociateCaseRequest: AssociateCaseRequest;
  groupOption?: boolean;
}

export interface SwitchListModel {
  key: 'repeatCase' | 'automaticStatusUpdate' | 'testPlanning';
  label: string;
  desc: string[];
  tooltipPosition: 'top' | 'tl' | 'tr' | 'bottom' | 'bl' | 'br' | 'left' | 'lt' | 'lb' | 'right' | 'rt' | 'rb';
}

// 获取统计数量
export interface UseCountType {
  id: string;
  passRate: string; // 通过率
  functionalCaseCount: number; // 功能用例数
  apiCaseCount: number; // 接口用例数
  apiScenarioCount: number; // 接口场景数
  bugCount: number; // Bug数量
  testProgress: string; // 测试进度
}

// TODO: 对后端
// 测试计划详情
export interface TestPlanDetail {
  id: string;
  name: string;
  num: number;
  status: planStatusType;
  followFlag: boolean;
  passRate: number;
  executedCount: number;
  caseCount: number;
  passCount: number;
  unPassCount: number;
  reReviewedCount: number;
  underReviewedCount: number;
}

export default {};
