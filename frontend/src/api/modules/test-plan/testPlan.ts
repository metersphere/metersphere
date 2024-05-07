import MSR from '@/api/http/index';
import {
  addTestPlanModuleUrl,
  DeleteTestPlanModuleUrl,
  GetTestPlanListUrl,
  GetTestPlanModuleCountUrl,
  GetTestPlanModuleUrl,
  MoveTestPlanModuleUrl,
  updateTestPlanModuleUrl,
} from '@/api/requrls/test-plan/testPlan';

import type { CreateOrUpdateModule, UpdateModule } from '@/models/caseManagement/featureCase';
import type { CommonList, MoveModules, TableQueryParams } from '@/models/common';
import { ModuleTreeNode } from '@/models/common';
import type { TestPlanItem } from '@/models/testPlan/testPlan';

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
export function getPlanModulesCounts(data: TableQueryParams) {
  return MSR.post({ url: GetTestPlanModuleCountUrl, data });
}

// 获取计划列表
export function getTestPlanList(data: TableQueryParams) {
  return MSR.post<CommonList<TestPlanItem>>({ url: GetTestPlanListUrl, data });
}
