import MSR from '@/api/http/index';
import {
  addTestPlanModuleUrl,
  AddTestPlanUrl,
  archivedPlanUrl,
  associationCaseToPlanUrl,
  batchArchivedPlanUrl,
  batchCopyPlanUrl,
  batchDeletePlanUrl,
  BatchDisassociateCaseUrl,
  BatchEditTestPlanUrl,
  batchMovePlanUrl,
  BatchRunCaseUrl,
  BatchUpdateCaseExecutorUrl,
  copyTestPlanUrl,
  deletePlanUrl,
  DeleteTestPlanModuleUrl,
  DisassociateCaseUrl,
  ExecuteHistoryUrl,
  followPlanUrl,
  GenerateReportUrl,
  GetAssociatedBugUrl,
  GetFeatureCaseModuleCountUrl,
  GetFeatureCaseModuleUrl,
  GetPlanDetailFeatureCaseListUrl,
  getStatisticalCountUrl,
  GetTestPlanCaseListUrl,
  GetTestPlanDetailUrl,
  GetTestPlanListUrl,
  GetTestPlanModuleCountUrl,
  GetTestPlanModuleUrl,
  GetTestPlanUsersUrl,
  MoveTestPlanModuleUrl,
  planDetailBugPageUrl,
  PlanDetailExecuteHistoryUrl,
  planPassRateUrl,
  RunFeatureCaseUrl,
  SortFeatureCaseUrl,
  TestPlanAssociateBugUrl,
  TestPlanCancelBugUrl,
  TestPlanCaseDetailUrl,
  updateTestPlanModuleUrl,
  UpdateTestPlanUrl,
} from '@/api/requrls/test-plan/testPlan';

import { ReviewUserItem } from '@/models/caseManagement/caseReview';
import type { CaseManagementTable, CreateOrUpdateModule, UpdateModule } from '@/models/caseManagement/featureCase';
import type { CommonList, MoveModules, TableQueryParams } from '@/models/common';
import { ModuleTreeNode } from '@/models/common';
import type {
  AddTestPlanParams,
  AssociateCaseRequestType,
  BatchExecuteFeatureCaseParams,
  BatchFeatureCaseParams,
  BatchUpdateCaseExecutorParams,
  DisassociateCaseParams,
  ExecuteHistoryItem,
  ExecuteHistoryType,
  FollowPlanParams,
  PassRateCountDetail,
  PlanDetailApiCaseItem,
  PlanDetailApiScenarioItem,
  PlanDetailBugItem,
  PlanDetailExecuteHistoryItem,
  PlanDetailFeatureCaseItem,
  PlanDetailFeatureCaseListQueryParams,
  RunFeatureCaseParams,
  SortFeatureCaseParams,
  TestPlanBaseParams,
  TestPlanDetail,
  TestPlanItem,
  UseCountType,
} from '@/models/testPlan/testPlan';

// 获取模块树
export function getTestPlanModule(params: TableQueryParams) {
  return MSR.get<ModuleTreeNode[]>({ url: `${GetTestPlanModuleUrl}/${params.projectId}` });
}

// 创建模块树
export function createPlanModuleTree(data: CreateOrUpdateModule) {
  return MSR.post({ url: addTestPlanModuleUrl, data });
}

// 更新模块树
export function updatePlanModuleTree(data: UpdateModule) {
  return MSR.post({ url: updateTestPlanModuleUrl, data });
}

// 移动模块树
export function moveTestPlanModuleTree(data: MoveModules) {
  return MSR.post({ url: MoveTestPlanModuleUrl, data });
}

// 批量编辑测试计划
export function batchEditTestPlan(data: TableQueryParams) {
  return MSR.post({ url: BatchEditTestPlanUrl, data });
}

// 删除模块
export function deletePlanModuleTree(id: string) {
  return MSR.get({ url: `${DeleteTestPlanModuleUrl}/${id}` });
}

// 获取模块数量
export function getPlanModulesCount(data: TableQueryParams) {
  return MSR.post({ url: GetTestPlanModuleCountUrl, data });
}

// 获取计划列表
export function getTestPlanList(data: TableQueryParams) {
  return MSR.post<CommonList<TestPlanItem>>({ url: GetTestPlanListUrl, data });
}

// 创建测试计划
export function addTestPlan(data: AddTestPlanParams) {
  return MSR.post({ url: AddTestPlanUrl, data });
}
// 功能用例列表
export function getTestPlanCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetTestPlanCaseListUrl, data });
}
// 创建测试计划
export function copyTestPlan(data: AddTestPlanParams) {
  return MSR.post({ url: copyTestPlanUrl, data });
}

// 获取测试计划详情
export function getTestPlanDetail(id: string) {
  return MSR.get<TestPlanDetail>({ url: `${GetTestPlanDetailUrl}/${id}` });
}

//  更新测试计划
export function updateTestPlan(data: AddTestPlanParams) {
  return MSR.post({ url: UpdateTestPlanUrl, data });
}
// 批量删除测试计划
export function batchDeletePlan(data: TableQueryParams) {
  return MSR.post({ url: batchDeletePlanUrl, data });
}
// 删除测试计划
export function deletePlan(id: string | undefined) {
  return MSR.get({ url: `${deletePlanUrl}/${id}` });
}
// 获取统计数量
export function getStatisticalCount(id: string) {
  return MSR.get<UseCountType>({ url: `${getStatisticalCountUrl}/${id}` });
}
// 归档
export function archivedPlan(id: string | undefined) {
  return MSR.get({ url: `${archivedPlanUrl}/${id}` });
}
// 批量复制测试计划
export function batchCopyPlan(data: TableQueryParams) {
  return MSR.post({ url: batchCopyPlanUrl, data });
}
// 批量移动测试计划
export function batchMovePlan(data: TableQueryParams) {
  return MSR.post({ url: batchMovePlanUrl, data });
}
// 批量移动测试计划
export function batchArchivedPlan(data: TableQueryParams) {
  return MSR.post({ url: batchArchivedPlanUrl, data });
}
// 计划详情缺陷管理列表
export function planDetailBugPage(data: TableQueryParams) {
  return MSR.post<CommonList<PlanDetailBugItem>>({ url: planDetailBugPageUrl, data });
}
// 生成报告
export function generateReport(data: TestPlanBaseParams) {
  return MSR.post({ url: GenerateReportUrl, data });
}
// 关注
export function followPlanRequest(data: FollowPlanParams) {
  return MSR.post({ url: followPlanUrl, data });
}
// 关联用例到测试计划
export function associationCaseToPlan(data: AssociateCaseRequestType) {
  return MSR.post({ url: associationCaseToPlanUrl, data });
}
// 测试计划通过率执行进度
export function getPlanPassRate(data: (string | undefined)[]) {
  return MSR.post<PassRateCountDetail[]>({ url: planPassRateUrl, data });
}
// 计划详情-功能用例列表
export function getPlanDetailFeatureCaseList(data: PlanDetailFeatureCaseListQueryParams) {
  return MSR.post<CommonList<PlanDetailFeatureCaseItem>>({ url: GetPlanDetailFeatureCaseListUrl, data });
}
// 计划详情-功能用例-获取模块数量
export function getFeatureCaseModuleCount(data: PlanDetailFeatureCaseListQueryParams) {
  return MSR.post({ url: GetFeatureCaseModuleCountUrl, data });
}
// 计划详情-功能用例模块树
export function getFeatureCaseModule(planId: string) {
  return MSR.get<ModuleTreeNode[]>({ url: `${GetFeatureCaseModuleUrl}/${planId}` });
}
// 计划详情-功能用例列表-取消关联用例
export function disassociateCase(data: DisassociateCaseParams) {
  return MSR.post({ url: DisassociateCaseUrl, data });
}
// 计划详情-功能用例列表-拖拽排序
export const sortFeatureCase = (data: SortFeatureCaseParams) => {
  return MSR.post({ url: SortFeatureCaseUrl, data });
};
// 计划详情-功能用例列表-批量取消关联用例
export function batchDisassociateCase(data: BatchFeatureCaseParams) {
  return MSR.post({ url: BatchDisassociateCaseUrl, data });
}
// 计划详情-功能用例列表-批量执行
export function batchExecuteCase(data: BatchExecuteFeatureCaseParams) {
  return MSR.post({ url: BatchRunCaseUrl, data });
}
// 计划详情-功能用例-获取用户列表
export const GetTestPlanUsers = (projectId: string, keyword: string) => {
  return MSR.get<ReviewUserItem[]>({ url: `${GetTestPlanUsersUrl}/${projectId}`, params: { keyword } });
};
// 计划详情-功能用例列表-批量更新执行人
export function batchUpdateCaseExecutor(data: BatchUpdateCaseExecutorParams) {
  return MSR.post({ url: BatchUpdateCaseExecutorUrl, data });
}
// 计划详情-功能用例-执行
export function runFeatureCase(data: RunFeatureCaseParams) {
  return MSR.post({ url: RunFeatureCaseUrl, data });
}
// 计划详情-功能用例-详情
export function getCaseDetail(id: string) {
  return MSR.get({ url: `${TestPlanCaseDetailUrl}/${id}` });
}
// 测试计划-用例详情-缺陷列表
export function associatedBugPage(data: TableQueryParams) {
  return MSR.post({ url: GetAssociatedBugUrl, data });
}
// 测试计划-用例详情-关联缺陷
export function associateBugToPlan(data: TableQueryParams) {
  return MSR.post({ url: TestPlanAssociateBugUrl, data });
}
// 测试计划-用例详情-关联缺陷
export function testPlanCancelBug(id: string) {
  return MSR.get({ url: `${TestPlanCancelBugUrl}/${id}` });
}
// 测试计划-用例详情-执行历史
export function executeHistory(data: ExecuteHistoryType) {
  return MSR.post<ExecuteHistoryItem[]>({ url: ExecuteHistoryUrl, data });
}
// 计划详情-接口用例列表 TODO 联调
export function getPlanDetailApiCaseList(data: PlanDetailFeatureCaseListQueryParams) {
  return MSR.post<CommonList<PlanDetailApiCaseItem>>({ url: GetPlanDetailFeatureCaseListUrl, data });
}
// 计划详情-接口场景列表 TODO 联调
export function getPlanDetailApiScenarioList(data: PlanDetailFeatureCaseListQueryParams) {
  return MSR.post<CommonList<PlanDetailApiScenarioItem>>({ url: GetPlanDetailFeatureCaseListUrl, data });
}
// 计划详情-执行历史 TODO 联调
export function getPlanDetailExecuteHistory(data: PlanDetailFeatureCaseListQueryParams) {
  return MSR.post<CommonList<PlanDetailExecuteHistoryItem>>({ url: PlanDetailExecuteHistoryUrl, data });
}
