import MSR from '@/api/http/index';
import {
  addTestPlanModuleUrl,
  AddTestPlanUrl,
  archivedPlanUrl,
  batchCopyPlanUrl,
  batchDeletePlanUrl,
  batchMovePlanUrl,
  deletePlanUrl,
  DeleteTestPlanModuleUrl,
  getStatisticalCountUrl,
  GetTestPlanDetailUrl,
  GetTestPlanListUrl,
  GetTestPlanModuleCountUrl,
  GetTestPlanModuleUrl,
  MoveTestPlanModuleUrl,
  planDetailBugPageUrl,
  updateTestPlanModuleUrl,
  UpdateTestPlanUrl,
} from '@/api/requrls/test-plan/testPlan';

import type { CreateOrUpdateModule, UpdateModule } from '@/models/caseManagement/featureCase';
import type { CommonList, MoveModules, TableQueryParams } from '@/models/common';
import { ModuleTreeNode } from '@/models/common';
import type {
  AddTestPlanParams,
  PlanDetailBugItem,
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
// 计划详情缺陷管理列表
export function planDetailBugPage(data: TableQueryParams) {
  return MSR.post<CommonList<PlanDetailBugItem>>({ url: planDetailBugPageUrl, data });
}
