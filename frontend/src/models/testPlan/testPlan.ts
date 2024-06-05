import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';

import type { customFieldsItem } from '@/models/caseManagement/featureCase';
import type { TableQueryParams } from '@/models/common';
import { BatchApiParams, DragSortParams } from '@/models/common';
import { LastExecuteResults } from '@/enums/caseEnum';

export type planStatusType = 'PREPARED' | 'UNDERWAY' | 'COMPLETED' | 'ARCHIVED';

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
  testPlanId?: string;
}

export type AssociateCaseRequestType = Pick<AssociateCaseRequest, 'functionalSelectIds' | 'testPlanId'>;

export interface AddTestPlanParams {
  id?: string;
  name: string;
  groupId?: string;
  moduleId: string;
  plannedStartTime?: number;
  plannedEndTime?: number;
  tags: string[];
  description?: string;
  testPlanning: boolean; // 是否开启测试规划
  automaticStatusUpdate: boolean; // 是否自定更新功能用例状态
  repeatCase: boolean; // 是否允许重复添加用例
  passThreshold: number;
  type?: string;
  baseAssociateCaseRequest?: AssociateCaseRequest | null;
  groupOption?: boolean;
  cycle?: number[];
  projectId?: string;
  testPlanId?: string;
  functionalCaseCount?: number;
}

export interface TestPlanDetail extends AddTestPlanParams {
  num: number;
  groupName?: string;
  moduleName?: string;
  status: planStatusType;
  followFlag: boolean;
  passRate: number;
  executedCount: number;
  caseCount: number;
  passCount: number;
  unPassCount: number;
  reReviewedCount: number;
  underReviewedCount: number;
  functionalCaseCount?: number;
  bugCount?: number;
  apiCaseCount?: number;
  apiScenarioCount?: number;
}

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
export type TestPlanItemType = TestPlanItem & TestPlanDetail;

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

// 计划详情缺陷列表
export interface PlanDetailBugItem {
  id: string;
  num: string;
  title: string;
  relateCases: {
    id: string;
    bugId: string;
    name: string;
  }[];
  handleUser: string;
  status: string;
  createUser: string;
  createTime: number;
}

// 关注
export interface FollowPlanParams {
  userId: string; // 用户id
  testPlanId: string;
}

export interface TestPlanBaseParams {
  projectId: string;
  testPlanId: string;
}

export interface PlanDetailFeatureCaseItem {
  id: string;
  num: string;
  name: string;
  moduleId: string;
  versionName: string;
  createUser: string;
  createUserName: string;
  lastExecResult: LastExecuteResults;
  lastExecTime: number;
  executeUser: string;
  executeUserName: string;
  bugCount: number;
  customFields: customFieldsItem[]; // 自定义字段集合
  caseId: string;
  testPlanId: string;
  bugList: {
    bugId: string;
    id: string;
    title: string;
    type: string;
    caseId: string;
  }[];
}

export interface PlanDetailFeatureCaseListQueryParams extends TableQueryParams, TestPlanBaseParams {}
export interface DisassociateCaseParams {
  testPlanId: string;
  id: string;
}

export interface BatchFeatureCaseParams extends BatchActionQueryParams {
  testPlanId: string;
  moduleIds?: string[];
  projectId: string;
}

export interface ExecuteFeatureCaseFormParams {
  lastExecResult: LastExecuteResults;
  content?: string;
  commentIds?: string[];
  planCommentFileIds?: string[];
}

export interface RunFeatureCaseParams extends ExecuteFeatureCaseFormParams {
  projectId: string;
  id: string;
  testPlanId: string;
  caseId: string;
  notifier?: string;
}

export type ExecuteHistoryType = Pick<RunFeatureCaseParams, 'id' | 'testPlanId' | 'caseId'>;

export interface BatchExecuteFeatureCaseParams extends BatchFeatureCaseParams, ExecuteFeatureCaseFormParams {
  notifier?: string;
}

export interface BatchUpdateCaseExecutorParams extends BatchFeatureCaseParams {
  userId: string;
}

export interface SortFeatureCaseParams extends DragSortParams {
  testPlanId: string;
}

export interface PassRateCountDetail {
  id: string;
  passThreshold: number;
  passRate: number;
  executeRate: number;
  successCount: number;
  errorCount: number;
  fakeErrorCount: number;
  blockCount: number;
  pendingCount: number;
  caseTotal: number;
  functionalCaseCount: number;
  apiCaseCount: number;
  apiScenarioCount: number;
}

// 执行历史
export interface ExecuteHistoryItem {
  status: string;
  content: string;
  contentText: string;
  stepsExecResult: string;
  createUser: string;
  userName: string;
  userLogo: string;
  email: string;
  steps: string;
  createTime: string;
  deleted: boolean;
}

// TODO: 联调
export interface PlanDetailApiCaseItem {
  id: string;
  num: string;
  name: string;
  moduleId: string;
  versionName: string;
  createUser: string;
  createUserName: string;
  lastExecResult: LastExecuteResults;
  lastExecTime: number;
  executeUser: string;
  executeUserName: string;
  bugCount: number;
  customFields: customFieldsItem[]; // 自定义字段集合
  caseId: string;
  testPlanId: string;
  lastExecResultReportId: string;
}

// TODO: 联调
export interface PlanDetailApiScenarioItem {
  id: string;
  num: string;
  name: string;
  moduleId: string;
  versionName: string;
  createUser: string;
  createUserName: string;
  lastExecResult: LastExecuteResults;
  lastExecTime: number;
  executeUser: string;
  executeUserName: string;
  bugCount: number;
  customFields: customFieldsItem[]; // 自定义字段集合
  caseId: string;
  testPlanId: string;
  lastExecResultReportId: string;
}

// 执行历史 TODO 联调
export interface PlanDetailExecuteHistoryItem {
  id: string;
  num: string;
  name: string;
  operationUser: string;
  createUser: string;
  startTime: number;
  endTime: number;
  lastExecResult: LastExecuteResults;
  triggerMode: string;
}
export default {};
