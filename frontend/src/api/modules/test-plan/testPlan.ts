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
  batchMovePlanUrl,
  copyTestPlanUrl,
  deletePlanUrl,
  DeleteTestPlanModuleUrl,
  DisassociateCaseUrl,
  followPlanUrl,
  GetFeatureCaseModuleCountUrl,
  GetFeatureCaseModuleUrl,
  GetPlanDetailFeatureCaseListUrl,
  getStatisticalCountUrl,
  GetTestPlanDetailUrl,
  GetTestPlanListUrl,
  GetTestPlanModuleCountUrl,
  GetTestPlanModuleUrl,
  MoveTestPlanModuleUrl,
  planDetailBugPageUrl,
  planPassRateUrl,
  updateTestPlanModuleUrl,
  UpdateTestPlanUrl,
} from '@/api/requrls/test-plan/testPlan';

import type { CreateOrUpdateModule, UpdateModule } from '@/models/caseManagement/featureCase';
import type { CommonList, MoveModules, TableQueryParams } from '@/models/common';
import { ModuleTreeNode } from '@/models/common';
import type {
  AddTestPlanParams,
  AssociateCaseRequestType,
  BatchFeatureCaseParams,
  DisassociateCaseParams,
  FollowPlanParams,
  PassRateCountDetail,
  PlanDetailBugItem,
  PlanDetailFeatureCaseItem,
  PlanDetailFeatureCaseListQueryParams,
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
// 计划详情-功能用例列表-批量取消关联用例
export function batchDisassociateCase(data: BatchFeatureCaseParams) {
  return MSR.post({ url: BatchDisassociateCaseUrl, data });
}
