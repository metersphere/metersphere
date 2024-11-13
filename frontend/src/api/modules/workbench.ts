import MSR from '@/api/http/index';

import type { ApiCaseDetail, ApiDefinitionDetail } from '@/models/apiTest/management';
import type { ApiScenarioTableItem } from '@/models/apiTest/scenario';
import type { BugListItem } from '@/models/bug-management';
import type { ReviewItem } from '@/models/caseManagement/caseReview';
import type { CaseManagementTable } from '@/models/caseManagement/featureCase';
import type { CommonList, TableQueryParams } from '@/models/common';
import type { PassRateCountDetail, TestPlanItem } from '@/models/testPlan/testPlan';
import type {
  OverViewOfProject,
  PassRateDataType,
  SelectedCardItem,
  WorkHomePageDetail,
} from '@/models/workbench/homePage';

import {
  EditDashboardLayoutUrl,
  GetDashboardLayoutUrl,
  WorkApiChangeListUrl,
  WorkAssociateCaseDetailUrl,
  WorkbenchApiCaseListUrl,
  WorkbenchBugListUrl,
  WorkbenchCaseListUrl,
  WorkbenchReviewListUrl,
  WorkbenchScenarioListUrl,
  WorkbenchTestPlanListUrl,
  WorkbenchTestPlanStatisticUrl,
  WorkBugHandlerDetailUrl,
  WorkCaseCountDetailUrl,
  WorkCaseReviewDetailUrl,
  WorkMemberViewDetailUrl,
  WorkMyCreatedDetailUrl,
  WorkProOverviewDetailUrl,
  WorkReviewListUrl,
  WorkTodoBugListUrl,
  WorkTodoPlanListUrl,
  WorkTodoReviewListUrl,
} from '../requrls/workbench';

// 我的-场景列表
export function workbenchScenarioList(data: TableQueryParams) {
  return MSR.post<CommonList<ApiScenarioTableItem>>({ url: WorkbenchScenarioListUrl, data });
}

// 我的-用例评审列表
export function workbenchReviewList(data: TableQueryParams) {
  return MSR.post<CommonList<ReviewItem>>({ url: WorkbenchReviewListUrl, data });
}

// 我的-测试计划列表
export function workbenchTestPlanList(data: TableQueryParams) {
  return MSR.post<CommonList<TestPlanItem>>({ url: WorkbenchTestPlanListUrl, data });
}

// 我的-测试计划统计
export function workbenchTestPlanStatistic(data: string[]) {
  return MSR.post<PassRateCountDetail[]>({ url: WorkbenchTestPlanStatisticUrl, data });
}

// 我的-用例列表
export function workbenchCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: WorkbenchCaseListUrl, data });
}

// 我的-缺陷列表
export function workbenchBugList(data: TableQueryParams) {
  return MSR.post<CommonList<BugListItem>>({ url: WorkbenchBugListUrl, data });
}

// 我的-接口用例列表
export function workbenchApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<ApiCaseDetail>>({ url: WorkbenchApiCaseListUrl, data });
}

// 工作台首页概览
export function workProOverviewDetail(data: WorkHomePageDetail) {
  return MSR.post<OverViewOfProject>({ url: WorkProOverviewDetailUrl, data });
}
// 我创建的
export function workMyCreatedDetail(data: WorkHomePageDetail) {
  return MSR.post<OverViewOfProject>({ url: WorkMyCreatedDetailUrl, data });
}
// 人员概览
export function workMemberViewDetail(data: WorkHomePageDetail) {
  return MSR.post<OverViewOfProject>({ url: WorkMemberViewDetailUrl, data });
}
// 获取用户布局
export function getDashboardLayout(orgId: string) {
  return MSR.get<SelectedCardItem[]>({ url: `${GetDashboardLayoutUrl}/${orgId}` });
}

// 获取用户布局
export function editDashboardLayout(data: SelectedCardItem[], orgId: string) {
  return MSR.post({ url: `${EditDashboardLayoutUrl}/${orgId}`, data });
}

// 工作台-首页-用例数
export function workCaseCountDetail(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkCaseCountDetailUrl, data });
}
// 工作台-首页-关联用例数
export function workAssociateCaseDetail(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkAssociateCaseDetailUrl, data });
}

// 工作台-首页-用例评审数
export function workCaseReviewDetail(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkCaseReviewDetailUrl, data });
}

// 工作台-首页-缺陷处理人
export function workBugHandlerDetail(data: WorkHomePageDetail) {
  return MSR.post<OverViewOfProject>({ url: WorkBugHandlerDetailUrl, data });
}

// 工作台-首页-接口变更
export function workApiChangeList(data: WorkHomePageDetail) {
  return MSR.post<CommonList<ApiDefinitionDetail>>(
    { url: WorkApiChangeListUrl, data },
    { ignoreCancelToken: true, errorMessageMode: 'none' }
  );
}

// 工作台-首页-接口变更
export function workReviewList(data: WorkHomePageDetail) {
  return MSR.post<CommonList<ReviewItem>>(
    { url: WorkReviewListUrl, data },
    { ignoreCancelToken: true, errorMessageMode: 'none' }
  );
}

// 待办-用例评审列表
export function workbenchTodoReviewList(data: TableQueryParams) {
  return MSR.post<CommonList<ReviewItem>>({ url: WorkTodoReviewListUrl, data });
}

// 待办-缺陷列表
export function workbenchTodoBugList(data: TableQueryParams) {
  return MSR.post<CommonList<BugListItem>>({ url: WorkTodoBugListUrl, data });
}

// 待办-测试计划列表
export function workbenchTodoTestPlanList(data: TableQueryParams) {
  return MSR.post<CommonList<TestPlanItem>>({ url: WorkTodoPlanListUrl, data });
}
