import type { MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';
import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';
import type { saveParams } from '@/components/business/ms-associate-case/types';

import type { customFieldsItem, DetailCase } from '@/models/caseManagement/featureCase';
import type { TableQueryParams } from '@/models/common';
import { BatchApiParams, DragSortParams } from '@/models/common';
import { CaseLinkEnum, LastExecuteResults } from '@/enums/caseEnum';
import type { ExecuteStatusEnum } from '@/enums/taskCenter';
import {
  type PlanMinderAssociateType,
  type PlanMinderCollectionType,
  type RunMode,
  testPlanTypeEnum,
} from '@/enums/testPlanEnum';

import type { TaskReportDetail } from '../apiTest/report';

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
  associateApiType?: string;
}

export interface AssociateCaseRequestParams {
  moduleMaps?: Record<string, saveParams>;
  syncCase: boolean;
  apiCaseCollectionId: string;
  apiScenarioCollectionId: string;
  selectAllModule: boolean;
  projectId: string;
  protocols: string[];
  totalCount?: number;
  associateApiType?: string;
  associateType?: string;
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
  isGroup?: boolean;
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
// 计划列表（不分页）
export interface TestPlanWithoutPageItem {
  id: string;
  num: number;
  groupId: string;
  projectId: string;
  moduleId: string;
  name: string;
  status: planStatusType;
  type: keyof typeof testPlanTypeEnum;
  tags: string[] | { id: string; name: string }[];
  createUser: string;
  createTime: string;
}

// 计划分页
export interface TestPlanItem {
  id: string;
  projectId: string;
  num: number;
  name: string;
  status: planStatusType;
  type: keyof typeof testPlanTypeEnum;
  tags: string[] | { id: string; name: string }[];
  schedule: string; // 是否定时
  createUser: string;
  createTime: string;
  moduleName: string;
  moduleId: string;
  children: TestPlanItem[];
  childrenCount: number;
  groupId: string;
  functionalCaseCount: number;
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

export interface RelateCasesType {
  id: string;
  bugId: string;
  name: string;
  projectId: string;
  type: CaseLinkEnum;
}

// 计划详情缺陷列表
export interface PlanDetailBugItem {
  id: string;
  num: string;
  title: string;
  relateCases: RelateCasesType[];
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
  projectId?: string;
  testPlanId: string;
  triggerMode?: string;
}

export interface CaseBugItem {
  bugId: string;
  id: string;
  title: string;
  type: string;
  caseId: string;
}

export interface PlanDetailFeatureCaseItem {
  id: string;
  num: string;
  name: string;
  moduleId: string;
  moduleName: string;
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
  testPlanCollectionName: string; // 测试集名称
  bugList: CaseBugItem[];
}

export interface PlanDetailFeatureCaseListQueryParams extends TableQueryParams, TestPlanBaseParams {}
export interface DisassociateCaseParams {
  testPlanId: string;
  id: string;
}

export interface BatchFeatureCaseParams extends BatchActionQueryParams {
  testPlanId: string;
  moduleIds?: string[];
  projectId?: string;
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

export type RunModeType = 'SERIAL' | 'PARALLEL';
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
  scheduleConfig: {
    resourceId: string;
    enable: boolean;
    cron: string;
    runConfig: {
      runMode: RunModeType;
    };
  };
  nextTriggerTime: number;
  status: planStatusType;
  pass: boolean; // 是否通过
}

// 执行历史
export interface ExecuteHistoryItem {
  id: string;
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
  caseEditType: string; // 类型是：步骤描述
  showResult: boolean;
}

export interface moduleForm {
  moveType: 'MODULE' | 'GROUP';
  targetId: string | number;
}

export interface BatchMoveParams extends TableQueryParams {
  moveType?: 'MODULE' | 'GROUP';
  targetId?: string | number;
}

// 计划详情-接口用例
export interface PlanDetailApiCaseQueryParams extends TableQueryParams, TestPlanBaseParams {
  apiDefinitionId?: string;
  protocols: string[];
  moduleIds?: string[];
  versionId?: string;
  refId?: string;
  collectionId?: string;
  treeType?: 'MODULE' | 'COLLECTION'; // 视图类型：模块是MODULE，测试集是COLLECTION
}

export interface PlanDetailApiCaseTreeParams {
  testPlanId: string;
  treeType: 'MODULE' | 'COLLECTION'; // 视图类型：模块是MODULE，测试集是COLLECTION
}

export interface PlanDetailApiCaseItem {
  id: string;
  num: number;
  name: string;
  moduleId: string;
  moduleName: string;
  createUser: string;
  createUserName: string;
  lastExecResult: LastExecuteResults;
  lastExecTime: number;
  lastExecReportId: string; // 报告id
  executeUser: string;
  executeUserName: string;
  priority: string;
  protocol: string;
  path: string;
  projectId: string;
  projectName: string;
  environmentId: string;
  environmentName: string;
  testPlanCollectionId: string; // 测试集id
  testPlanCollectionName: string; // 测试集名称
  apiTestCaseId: string; // 接口用例id
}

export interface BatchApiCaseParams extends BatchActionQueryParams {
  testPlanId: string;
  moduleIds?: string[];
  collectionId?: string; // 测试集id
  protocols?: string[]; // 接口用例传protocols 接口场景不传
}

export interface BatchMoveApiCaseParams extends BatchApiCaseParams {
  targetCollectionId: string; // 测试集id
}

export interface SortApiCaseParams extends DragSortParams {
  testCollectionId: string; // 测试集id
}

// 计划详情-接口场景
export interface PlanDetailApiScenarioQueryParams extends TableQueryParams, TestPlanBaseParams {
  scenarioId?: string;
  moduleIds?: string[];
  versionId?: string;
  refId?: string;
  collectionId?: string;
  treeType?: 'MODULE' | 'COLLECTION'; // 视图类型：模块是MODULE，测试集是COLLECTION
}

export interface PlanDetailApiScenarioItem {
  id: string;
  num: string;
  name: string;
  priority: string;
  projectId: string;
  projectName: string;
  environmentId: string;
  environmentName: string;
  moduleId: string;
  moduleName: string;
  createUser: string;
  createUserName: string;
  lastExecResult: LastExecuteResults;
  lastExecTime: number;
  executeUser: string;
  executeUserName: string;
  lastExecReportId: string; // 报告id
  testPlanCollectionId: string; // 测试集id
  testPlanCollectionName: string; // 测试集名称
  apiScenarioId: string; // 场景id
}

// 执行历史
export interface PlanDetailExecuteHistoryItem {
  id: string;
  num: string;
  triggerMode: string; // 执行方式
  execResult: ExecuteStatusEnum; // 执行结果
  operationUser: string;
  startTime: number;
  endTime: number;
  deleted: boolean;
}

export interface CreateTask {
  resourceId: string;
  enable: boolean;
  cron: string;
  runConfig: { runMode: 'SERIAL' | 'PARALLEL' };
}
export interface BatchConfigSchedule extends Partial<CreateTask>, BatchActionQueryParams {
  projectId: string;
}
export interface BatchExecutePlan {
  projectId?: string;
  executeIds?: string[];
  runMode: RunModeType;
  executionSource: string;
}

export interface ExecutePlan extends BatchExecutePlan {
  executeId: string;
}

export interface PlanMinderNodeData extends MinderJsonNodeData {
  id: string;
  pos: number;
  text: string;
  num: number; // 关联用例数量
  priority?: number; // 串行/并行
  executeMethod?: RunMode; // 串行/并行值
  type: PlanMinderCollectionType; // 测试集类型(功能：FUNCTIONAL_CASE/接口用例：API_CASE/场景：SCENARIO_CASE)
  extended: boolean;
  grouped: boolean; // 是否使用环境组
  environmentId: string;
  testResourcePoolId: string;
  retryOnFail: boolean;
  retryTimes: number;
  retryInterval: number;
  stopOnFail: boolean;
  tempCollectionNode?: boolean; // 关联用例到未保存的临时测试集节点标识
}
export interface PlanMinderNode extends MinderJsonNode {
  data: PlanMinderNodeData;
  children: PlanMinderNode[];
}

export interface PlanMinderAssociateDTO {
  ids: string[];
  associateType: PlanMinderAssociateType; // 关联关系的type(功能：FUNCTIONAL_CASE/接口定义：API/接口用例：API_CASE/场景：SCENARIO_CASE)
}
export interface PlanMinderEditListItem extends PlanMinderNodeData {
  name: string;
  associateDTOS: PlanMinderAssociateDTO[];
}

export interface PlanMinderEditParams {
  planId: string;
  editList: PlanMinderEditListItem[];
  deletedIds: string[];
}
export interface PlanExecuteResultExecuteCaseCount {
  success: number;
  error: number;
  fakeError: number;
  block: number;
  pending: number;
}
export interface PlanExecuteResult extends TaskReportDetail {
  taskName: string;
  reportId: string;
  childPlans: { id: string; name: string; apiCaseTotal: number; apiScenarioTotal: number }[]; // 子计划
  createUser: string;
  executeRate: string;
  executeCaseCount: PlanExecuteResultExecuteCaseCount;
  apiCaseTotal: number;
  apiScenarioTotal: number;
}

export interface TestPlanCaseDetail extends DetailCase {
  createUserName: string;
  createTime: number;
  lastExecuteResult: string;
  bugListCount: number;
  customFields: Record<string, any>[];
}
