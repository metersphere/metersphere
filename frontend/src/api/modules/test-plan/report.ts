import MSR from '@/api/http';
import * as reportUrl from '@/api/requrls/test-plan/report';

import { CommonList, TableQueryParams } from '@/models/common';
import { FeatureCaseItem, ReportBugItem, UpdateReportDetailParams } from '@/models/testPlan/report';

// 报告列表
export function reportList(data: TableQueryParams) {
  return MSR.post({ url: reportUrl.PlanReportListUrl, data });
}

// 删除报告
export function reportDelete(id: string) {
  return MSR.get({ url: `${reportUrl.PlanDeleteUrl}/${id}` });
}

// 重命名
export function reportRename(id: string, data: string) {
  return MSR.post({ url: `${reportUrl.PlanReportRenameUrl}/${id}`, data });
}

// 批量删除
export function reportBathDelete(data: TableQueryParams) {
  return MSR.post({ url: reportUrl.PlanBatchDeleteUrl, data });
}

// 测试计划-报告-详情-缺陷分页查询
export function getReportBugList(data: TableQueryParams) {
  return MSR.post<CommonList<ReportBugItem>>({ url: reportUrl.ReportBugListUrl, data });
}

// 测试计划-报告-详情-功能用例分页查询
export function getReportFeatureCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<FeatureCaseItem>>({ url: reportUrl.ReportFeatureCaseListUrl, data });
}

// 测试计划-报告-详情-报告内容更新
export function updateReportDetail(data: UpdateReportDetailParams) {
  return MSR.post({ url: reportUrl.UpdateReportDetailUrl, data });
}

export default {};
