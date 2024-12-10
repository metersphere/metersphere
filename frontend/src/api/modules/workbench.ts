import { CascaderOption } from '@arco-design/web-vue';

import MSR from '@/api/http/index';

import type { ApiCaseDetail, ApiDefinitionDetail } from '@/models/apiTest/management';
import type { ApiScenarioTableItem } from '@/models/apiTest/scenario';
import type { BugListItem, BugOptionListItem } from '@/models/bug-management';
import type { ReviewItem } from '@/models/caseManagement/caseReview';
import type { CaseManagementTable } from '@/models/caseManagement/featureCase';
import type { CommonList, TableQueryParams } from '@/models/common';
import type { PassRateCountDetail, TestPlanItem } from '@/models/testPlan/testPlan';
import type {
  ApiCoverageData,
  OverViewOfProject,
  PassRateDataType,
  SelectedCardItem,
  WorkHomePageDetail,
  WorkTestPlanDetail,
  WorkTestPlanRageDetail,
} from '@/models/workbench/homePage';

import {
  EditDashboardLayoutUrl,
  GetDashboardLayoutUrl,
  WorkApiCaseCountDetailUrl,
  WorkApiChangeListUrl,
  WorkApiCountCoverRateUrl,
  WorkApiCountDetailUrl,
  WorkAssociateCaseDetailUrl,
  WorkbenchApiCaseListUrl,
  WorkbenchBugColumnOptionsUrl,
  WorkbenchBugCustomFieldUrl,
  WorkbenchBugListUrl,
  WorkbenchCaseListUrl,
  WorkbenchReviewListUrl,
  WorkbenchScenarioListUrl,
  WorkbenchTestPlanListUrl,
  WorkbenchTestPlanStatisticUrl,
  WorkBugByMeCreatedUrl,
  WorkBugCountDetailUrl,
  WorkBugHandleByMeUrl,
  WorkBugHandlerDetailUrl,
  WorkCaseCountDetailUrl,
  WorkCaseReviewDetailUrl,
  WorkHandleUserOptionsUrl,
  WorkMemberViewDetailUrl,
  WorkMyCreatedDetailUrl,
  WorkPlanLegacyBugUrl,
  WorkProjectMemberListUrl,
  WorkProOverviewDetailUrl,
  WorkReviewListUrl,
  WorkScenarioCaseCountDetailUrl,
  WorkTestPlanListUrl,
  WorkTestPlanOverviewUrl,
  WorkTestPlanRageUrl,
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

// 我的-缺陷列表-自定义字段
export function getCustomFieldHeader(projectId: string) {
  return MSR.get({ url: `${WorkbenchBugCustomFieldUrl}/${projectId}` });
}

// 我的-缺陷列表-表格筛选字段的数据查询
export function getCustomOptionHeader(projectId: string) {
  return MSR.get<BugOptionListItem>({ url: `${WorkbenchBugColumnOptionsUrl}/${projectId}` });
}

// 我的-接口用例列表
export function workbenchApiCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<ApiCaseDetail>>({ url: WorkbenchApiCaseListUrl, data });
}

// 工作台首页概览
export function workProOverviewDetail(data: WorkHomePageDetail) {
  return MSR.post<OverViewOfProject>({ url: WorkProOverviewDetailUrl, data }, { ignoreCancelToken: true });
}
// 我创建的
export function workMyCreatedDetail(data: WorkHomePageDetail) {
  return MSR.post<OverViewOfProject>({ url: WorkMyCreatedDetailUrl, data }, { ignoreCancelToken: true });
}
// 人员概览
export function workMemberViewDetail(data: WorkHomePageDetail) {
  return MSR.post<OverViewOfProject>({ url: WorkMemberViewDetailUrl, data }, { ignoreCancelToken: true });
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
  return MSR.post<PassRateDataType>({ url: WorkCaseCountDetailUrl, data }, { ignoreCancelToken: true });
}
// 工作台-首页-关联用例数
export function workAssociateCaseDetail(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkAssociateCaseDetailUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-用例评审数
export function workCaseReviewDetail(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkCaseReviewDetailUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-缺陷处理人
export function workBugHandlerDetail(data: WorkHomePageDetail) {
  return MSR.post<OverViewOfProject>({ url: WorkBugHandlerDetailUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-项目成员下拉
export function workProjectMemberOptions(projectId: string, keyword?: string) {
  return MSR.get({ url: `${WorkProjectMemberListUrl}/${projectId}`, params: { keyword } });
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

// 工作台-首页-缺陷数量
export function workBugCountDetail(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkBugCountDetailUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-我创建的缺陷
export function workBugByMeCreated(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkBugByMeCreatedUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-待我处理的缺陷
export function workBugHandleByMe(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkBugHandleByMeUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-接口数量
export function workApiCountDetail(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkApiCountDetailUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-接口用例数量
export function workApiCaseCountDetail(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkApiCaseCountDetailUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-场景用例数量
export function workScenarioCaseCountDetail(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkScenarioCaseCountDetailUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-缺陷处理人列表
export function workHandleUserOptions(projectId: string) {
  return MSR.get({ url: WorkHandleUserOptionsUrl, params: projectId }, { ignoreCancelToken: true });
}

// 工作台-首页-测试计划遗留缺陷
export function workPlanLegacyBug(data: WorkHomePageDetail) {
  return MSR.post<PassRateDataType>({ url: WorkPlanLegacyBugUrl, data }, { ignoreCancelToken: true });
}

// 工作台-首页-接口测试覆盖率
export function workApiCountCoverRage(projectId: string) {
  return MSR.get<ApiCoverageData>({ url: WorkApiCountCoverRateUrl, params: projectId }, { ignoreCancelToken: true });
}
// 工作台-首页-测试计划数量
export function workTestPlanRage(data: WorkTestPlanDetail) {
  return MSR.post<WorkTestPlanRageDetail>({ url: WorkTestPlanRageUrl, data }, { ignoreCancelToken: true });
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
// 待办-测试计划列表
export function getWorkTestPlanListUrl(projectId: string) {
  return MSR.get<CascaderOption[]>({ url: `${WorkTestPlanListUrl}/${projectId}` }, { ignoreCancelToken: true });
}

// 工作台-测试计划概览
export function workTestPlanOverviewDetail(data: WorkHomePageDetail) {
  return MSR.post<OverViewOfProject>({ url: WorkTestPlanOverviewUrl, data }, { ignoreCancelToken: true });
}
