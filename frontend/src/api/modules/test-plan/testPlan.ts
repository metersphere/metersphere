import MSR from '@/api/http/index';
import {
  addTestPlanModuleUrl,
  AddTestPlanUrl,
  ApiCaseReportDetailStepUrl,
  ApiCaseReportDetailUrl,
  ApiScenarioReportDetailStepUrl,
  ApiScenarioReportDetailUrl,
  archivedPlanUrl,
  AssociatedBugToApiCaseUrl,
  AssociatedBugToScenarioCaseUrl,
  BatchAddBugToApiCaseUrl,
  BatchAddBugToFunctionalCaseUrl,
  BatchAddBugToMinderCaseUrl,
  BatchAddBugToScenarioCaseUrl,
  batchArchivedPlanUrl,
  BatchAssociatedBugToCaseUrl,
  BatchAssociatedBugToMinderCaseUrl,
  BatchConfigScheduleUrl,
  batchCopyPlanUrl,
  batchDeletePlanUrl,
  BatchDisassociateApiCaseUrl,
  BatchDisassociateApiScenarioUrl,
  BatchDisassociateCaseUrl,
  BatchEditTestPlanUrl,
  BatchExecutePlanUrl,
  BatchLinkBugToApiCaseUrl,
  BatchLinkBugToScenarioCaseUrl,
  BatchMoveApiCaseUrl,
  BatchMoveApiScenarioUrl,
  BatchMoveFeatureCaseUrl,
  batchMovePlanUrl,
  BatchRunApiCaseUrl,
  BatchRunApiScenarioUrl,
  BatchRunCaseUrl,
  BatchUpdateCaseExecutorUrl,
  CancelBugFromApiCaseUrl,
  CancelBugFromScenarioCaseUrl,
  ConfigScheduleUrl,
  copyTestPlanUrl,
  deletePlanUrl,
  DeleteScheduleTaskUrl,
  DeleteTestPlanModuleUrl,
  DisassociateApiCaseUrl,
  DisassociateApiScenarioUrl,
  DisassociateCaseUrl,
  dragPlanOnGroupUrl,
  EditPlanMinderUrl,
  ExecuteHistoryUrl,
  ExecuteSinglePlanUrl,
  followPlanUrl,
  GenerateReportUrl,
  GetApiCaseModuleCountUrl,
  GetApiCaseModuleUrl,
  GetApiScenarioModuleCountUrl,
  GetApiScenarioModuleUrl,
  GetAssociatedBugUrl,
  GetFeatureCaseModuleCountUrl,
  GetFeatureCaseModuleUrl,
  GetPlanDetailApiCaseListUrl,
  GetPlanDetailApiScenarioListUrl,
  GetPlanDetailFeatureCaseListUrl,
  GetPlanMinderUrl,
  getStatisticalCountUrl,
  GetTestPlanCaseListUrl,
  GetTestPlanDetailUrl,
  GetTestPlanExecutorOptionsUrl,
  GetTestPlanListUrl,
  GetTestPlanListWithoutPageUrl,
  GetTestPlanModuleCountUrl,
  GetTestPlanModuleUrl,
  GetTestPlanUsersUrl,
  GetUnAssociatedApiBugUrl,
  GetUnAssociatedListUrl,
  GetUnAssociatedScenarioBugUrl,
  MoveTestPlanModuleUrl,
  planDetailBugPageUrl,
  PlanDetailExecuteHistoryUrl,
  planPassRateUrl,
  RunApiCaseUrl,
  RunApiScenarioUrl,
  RunFeatureCaseUrl,
  SortApiCaseUrl,
  SortApiScenarioUrl,
  SortFeatureCaseUrl,
  TaskResultUrl,
  TestPlanAndGroupCopyUrl,
  TestPlanApiAssociatedPageUrl,
  TestPlanAssociateBugUrl,
  TestPlanAssociationUrl,
  TestPlanCancelBugUrl,
  TestPlanCaseAssociatedPageUrl,
  TestPlanCaseDetailUrl,
  TestPlanGroupOptionsUrl,
  TestPlanScenarioAssociatedPageUrl,
  updateTestPlanModuleUrl,
  UpdateTestPlanUrl,
} from '@/api/requrls/test-plan/testPlan';

import { ApiCaseDetail, ApiDefinitionDetail } from '@/models/apiTest/management';
import type { ReportDetail, ReportStepDetail } from '@/models/apiTest/report';
import { BugEditFormObject } from '@/models/bug-management';
import { ReviewUserItem } from '@/models/caseManagement/caseReview';
import type { CaseManagementTable, CreateOrUpdateModule, UpdateModule } from '@/models/caseManagement/featureCase';
import type { CommonList, MoveModules, TableQueryParams } from '@/models/common';
import { DragSortParams, ModuleTreeNode } from '@/models/common';
import type {
  AddTestPlanParams,
  BatchApiCaseParams,
  BatchConfigSchedule,
  BatchExecuteFeatureCaseParams,
  BatchExecutePlan,
  BatchFeatureCaseParams,
  BatchMoveApiCaseParams,
  BatchUpdateCaseExecutorParams,
  CreateTask,
  DisassociateCaseParams,
  ExecuteHistoryItem,
  ExecuteHistoryType,
  ExecutePlan,
  FollowPlanParams,
  PassRateCountDetail,
  PlanDetailApiCaseItem,
  PlanDetailApiCaseQueryParams,
  PlanDetailApiCaseTreeParams,
  PlanDetailApiScenarioItem,
  PlanDetailApiScenarioQueryParams,
  PlanDetailBugItem,
  PlanDetailExecuteHistoryItem,
  PlanDetailFeatureCaseItem,
  PlanDetailFeatureCaseListQueryParams,
  PlanExecuteResult,
  PlanMinderEditParams,
  PlanMinderNode,
  RunFeatureCaseParams,
  SortApiCaseParams,
  TestPlanBaseParams,
  TestPlanCaseDetail,
  TestPlanDetail,
  TestPlanItem,
  TestPlanWithoutPageItem,
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

// 获取计划列表(无分页)
export function getTestPlanListWithoutPage(projectId: string) {
  return MSR.get<TestPlanWithoutPageItem[]>({ url: `${GetTestPlanListWithoutPageUrl}/${projectId}` });
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
export function getFeatureCaseModule(data: PlanDetailApiCaseTreeParams) {
  return MSR.post<ModuleTreeNode[]>({ url: GetFeatureCaseModuleUrl, data }, { ignoreCancelToken: true });
}
// 计划详情-功能用例列表-取消关联用例
export function disassociateCase(data: DisassociateCaseParams) {
  return MSR.post({ url: DisassociateCaseUrl, data });
}
// 计划详情-功能用例列表-拖拽排序
export const sortFeatureCase = (data: SortApiCaseParams) => {
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
// 计划详情-功能用例列表-批量移动
export function batchMoveFeatureCase(data: BatchMoveApiCaseParams) {
  return MSR.post({ url: BatchMoveFeatureCaseUrl, data });
}
// 计划详情-功能用例-执行
export function runFeatureCase(data: RunFeatureCaseParams) {
  return MSR.post({ url: RunFeatureCaseUrl, data });
}
// 计划详情-功能用例-详情
export function getCaseDetail(id: string) {
  return MSR.get<TestPlanCaseDetail>({ url: `${TestPlanCaseDetailUrl}/${id}` });
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
// 计划详情-接口用例列表
export function getPlanDetailApiCaseList(data: PlanDetailApiCaseQueryParams) {
  return MSR.post<CommonList<PlanDetailApiCaseItem>>({ url: GetPlanDetailApiCaseListUrl, data });
}
// 计划详情-接口用例模块树
export function getApiCaseModule(data: PlanDetailApiCaseTreeParams) {
  return MSR.post<ModuleTreeNode[]>({ url: GetApiCaseModuleUrl, data }, { ignoreCancelToken: true });
}
// 计划详情-接口用例-获取模块数量
export function getApiCaseModuleCount(data: PlanDetailApiCaseQueryParams) {
  return MSR.post({ url: GetApiCaseModuleCountUrl, data });
}
// 计划详情-接口用例列表-拖拽排序
export const sortApiCase = (data: SortApiCaseParams) => {
  return MSR.post({ url: SortApiCaseUrl, data });
};
// 计划详情-接口用例列表-取消关联用例
export function disassociateApiCase(data: DisassociateCaseParams) {
  return MSR.post({ url: DisassociateApiCaseUrl, data });
}
// 计划详情-接口用例列表-执行
export function runApiCase(id: string, reportId?: string) {
  return MSR.get({ url: `${RunApiCaseUrl}/${id}`, params: reportId });
}
// 计划详情-接口用例列表-批量取消关联用例
export function batchDisassociateApiCase(data: BatchApiCaseParams) {
  return MSR.post({ url: BatchDisassociateApiCaseUrl, data });
}
// 计划详情-接口用例列表-批量执行
export function batchRunApiCase(data: BatchApiCaseParams) {
  return MSR.post({ url: BatchRunApiCaseUrl, data });
}
// 计划详情-接口用例列表-批量移动
export function batchMoveApiCase(data: BatchMoveApiCaseParams) {
  return MSR.post({ url: BatchMoveApiCaseUrl, data });
}
// 计划详情-接口用例列表-获取报告
export function getApiCaseReport(reportId: string) {
  return MSR.get<ReportDetail>({ url: `${ApiCaseReportDetailUrl}/${reportId}` });
}
// 计划详情-接口用例列表-获取报告-步骤详情
export function getApiCaseReportStep(reportId: string, stepId: string) {
  return MSR.get<ReportStepDetail[]>({ url: `${ApiCaseReportDetailStepUrl}/${reportId}/${stepId}` });
}
// 计划详情-接口场景列表
export function getPlanDetailApiScenarioList(data: PlanDetailApiScenarioQueryParams) {
  return MSR.post<CommonList<PlanDetailApiScenarioItem>>({ url: GetPlanDetailApiScenarioListUrl, data });
}
// 计划详情-接口场景模块树
export function getApiScenarioModule(data: PlanDetailApiCaseTreeParams) {
  return MSR.post<ModuleTreeNode[]>({ url: GetApiScenarioModuleUrl, data }, { ignoreCancelToken: true });
}
// 计划详情-接口场景-获取模块数量
export function getApiScenarioModuleCount(data: PlanDetailApiScenarioQueryParams) {
  return MSR.post({ url: GetApiScenarioModuleCountUrl, data });
}
// 计划详情-接口场景列表-拖拽排序
export const sortApiScenario = (data: SortApiCaseParams) => {
  return MSR.post({ url: SortApiScenarioUrl, data });
};
// 计划详情-接口场景列表-执行
export function runApiScenario(id: string, reportId?: string) {
  return MSR.get({ url: `${RunApiScenarioUrl}/${id}`, params: reportId });
}
// 计划详情-接口场景列表-取消关联用例
export function disassociateApiScenario(data: DisassociateCaseParams) {
  return MSR.post({ url: DisassociateApiScenarioUrl, data });
}
// 计划详情-接口场景列表-批量取消关联用例
export function batchDisassociateApiScenario(data: BatchApiCaseParams) {
  return MSR.post({ url: BatchDisassociateApiScenarioUrl, data });
}
// 计划详情-接口场景列表-批量执行
export function batchRunApiScenario(data: BatchApiCaseParams) {
  return MSR.post({ url: BatchRunApiScenarioUrl, data });
}
// 计划详情-接口场景列表-批量移动
export function batchMoveApiScenario(data: BatchMoveApiCaseParams) {
  return MSR.post({ url: BatchMoveApiScenarioUrl, data });
}
// 计划详情-接口场景列表-获取报告
export function getApiScenarioReport(reportId: string) {
  return MSR.get<ReportDetail>({ url: `${ApiScenarioReportDetailUrl}/${reportId}` });
}
// 计划详情-接口用例列表-获取报告-步骤详情
export function getApiScenarioReportStep(reportId: string, stepId: string) {
  return MSR.get<ReportStepDetail[]>({ url: `${ApiScenarioReportDetailStepUrl}/${reportId}/${stepId}` });
}
// 计划详情-执行历史
export function getPlanDetailExecuteHistory(data: PlanDetailFeatureCaseListQueryParams) {
  return MSR.post<CommonList<PlanDetailExecuteHistoryItem>>({ url: PlanDetailExecuteHistoryUrl, data });
}

// 功能用例-关联用例-接口用例-API
export function getTestPlanAssociationApiList(data: TableQueryParams) {
  return MSR.post<CommonList<ApiDefinitionDetail>>({ url: TestPlanApiAssociatedPageUrl, data });
}
// 功能用例-关联用例-接口用例-CASE
export function getTestPlanAssociationCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<ApiCaseDetail>>({ url: TestPlanCaseAssociatedPageUrl, data });
}
// 功能用例-关联用例-场景用例
export function getPlanScenarioAssociatedList(data: TableQueryParams) {
  return MSR.post<CommonList<ApiCaseDetail>>({ url: TestPlanScenarioAssociatedPageUrl, data });
}
// 测试计划-复制测试计划&测试计划组
export function testPlanAndGroupCopy(id: string) {
  return MSR.get({ url: `${TestPlanAndGroupCopyUrl}/${id}` });
}
// 测试计划-测试计划组下拉列表
export function getPlanGroupOptions(projectId: string) {
  return MSR.get({ url: `${TestPlanGroupOptionsUrl}/${projectId}` });
}
// 测试计划-测试计划组内拖拽
export function dragPlanOnGroup(data: DragSortParams) {
  return MSR.post({ url: dragPlanOnGroupUrl, data });
}
// 测试计划-配置定时任务
export function configSchedule(data: CreateTask) {
  return MSR.post({ url: ConfigScheduleUrl, data });
}

// 测试计划-批量配置定时任务
export function batchConfigSchedule(data: BatchConfigSchedule) {
  return MSR.post({ url: BatchConfigScheduleUrl, data });
}

// 测试计划-计划&计划组-执行
export function executeSinglePlan(data: ExecutePlan) {
  return MSR.post({ url: ExecuteSinglePlanUrl, data });
}
// 测试计划-计划&计划组-执行&批量执行
export function executePlanOrGroup(data: BatchExecutePlan) {
  return MSR.post({ url: BatchExecutePlanUrl, data });
}
// 测试计划-计划&计划组-执行&批量执行
export function deleteScheduleTask(testPlanId: string) {
  return MSR.get({ url: `${DeleteScheduleTaskUrl}/${testPlanId}` });
}
// 获取测试规划脑图
export function getPlanMinder(testPlanId: string) {
  return MSR.get<PlanMinderNode[]>({ url: GetPlanMinderUrl, params: testPlanId });
}
// 更新测试规划脑图
export function editPlanMinder(data: PlanMinderEditParams) {
  return MSR.post({ url: EditPlanMinderUrl, data });
}
// 测试计划关联模块count
export function testPlanAssociateModuleCount(data: TableQueryParams) {
  return MSR.post({ url: TestPlanAssociationUrl, data });
}
// 获取执行人下拉选项(空选项)
export function getExecuteUserOption(projectId: string, keyword?: string) {
  return MSR.get({ url: `${GetTestPlanExecutorOptionsUrl}/${projectId}`, params: { keyword } });
}
// 获取测试计划-功能用例-未关联抽屉缺陷列表
export function getTestPlanBugPage(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetUnAssociatedListUrl, data });
}
// 获取测试计划-接口用例-未关联抽屉缺陷列表
export function getTestPlanApiBugPage(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetUnAssociatedApiBugUrl, data });
}
// 获取测试计划-接口用例-未关联抽屉缺陷列表
export function getTestPlanScenarioBugPage(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetUnAssociatedScenarioBugUrl, data });
}
// 测试计划-用例详情-关联缺陷
export function associateBugToApiCase(data: TableQueryParams) {
  return MSR.post({ url: AssociatedBugToApiCaseUrl, data });
}
// 测试计划-用例详情-关联缺陷
export function associateBugToScenarioCase(data: TableQueryParams) {
  return MSR.post({ url: AssociatedBugToScenarioCaseUrl, data });
}
// 测试计划-接口用例-取消关联联缺陷
export function cancelBugFromApiCase(id: string) {
  return MSR.get({ url: `${CancelBugFromApiCaseUrl}/${id}` });
}
// 测试计划-场景用例-取消关联联缺陷
export function cancelBugFromScenarioCase(id: string) {
  return MSR.get({ url: `${CancelBugFromScenarioCaseUrl}/${id}` });
}
// 测试计划-详情-批量关联缺陷
export function batchAssociatedBugToCase(data: TableQueryParams) {
  return MSR.post({ url: BatchAssociatedBugToCaseUrl, data });
}
// 测试计划-详情-功能用例-批量新建缺陷
export function batchAddBugToFunctionCase(data: { request: BugEditFormObject; fileList: File[] }) {
  return MSR.uploadFile({ url: BatchAddBugToFunctionalCaseUrl }, data, '', true);
}
// 测试计划-详情-接口用例-批量新建缺陷
export function batchAddBugToApiCase(data: { request: BugEditFormObject; fileList: File[] }) {
  return MSR.uploadFile({ url: BatchAddBugToApiCaseUrl }, data, '', true);
}
// 测试计划-详情-场景用例-批量新建缺陷
export function batchAddBugToScenarioCase(data: { request: BugEditFormObject; fileList: File[] }) {
  return MSR.uploadFile({ url: BatchAddBugToScenarioCaseUrl }, data, '', true);
}
// 测试计划-详情-接口用例-批量关联缺陷
export function batchLinkBugToApiCase(data: TableQueryParams) {
  return MSR.post({ url: BatchLinkBugToApiCaseUrl, data });
}
// 测试计划-详情-接口用例-批量关联缺陷
export function batchLinkBugToScenarioCase(data: TableQueryParams) {
  return MSR.post({ url: BatchLinkBugToScenarioCaseUrl, data });
}
// 测试计划-详情-脑图批量关联缺陷
export function batchAssociatedBugToMinderCase(data: TableQueryParams) {
  return MSR.post({ url: BatchAssociatedBugToMinderCaseUrl, data });
}
// 测试计划-详情-脑图批量新建缺陷
export function batchAddBugToMinderCase(data: { request: BugEditFormObject; fileList: File[] }) {
  return MSR.uploadFile({ url: BatchAddBugToMinderCaseUrl }, data, '', true);
}

// 测试计划/组-执行结果
export function getTaskResult(id: string) {
  return MSR.get<PlanExecuteResult>({ url: `${TaskResultUrl}/${id}` });
}
