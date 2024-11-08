import MSR from '@/api/http/index';

import type { ApiCaseDetail } from '@/models/apiTest/management';
import type { ApiScenarioTableItem } from '@/models/apiTest/scenario';
import type { BugListItem } from '@/models/bug-management';
import type { ReviewItem } from '@/models/caseManagement/caseReview';
import type { CaseManagementTable } from '@/models/caseManagement/featureCase';
import type { CommonList, TableQueryParams } from '@/models/common';
import type { PassRateCountDetail, TestPlanItem } from '@/models/testPlan/testPlan';

import {
  WorkbenchApiCaseListUrl,
  WorkbenchBugListUrl,
  WorkbenchCaseListUrl,
  WorkbenchReviewListUrl,
  WorkbenchScenarioListUrl,
  WorkbenchTestPlanListUrl,
  WorkbenchTestPlanStatisticUrl,
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
