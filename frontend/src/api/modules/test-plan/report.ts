import MSR from '@/api/http';
import * as reportUrl from '@/api/requrls/test-plan/report';

import type { GetShareId } from '@/models/apiTest/report';
import { CommonList, TableQueryParams } from '@/models/common';
import {
  ApiOrScenarioCaseItem,
  FeatureCaseItem,
  ReportBugItem,
  UpdateReportDetailParams,
} from '@/models/testPlan/report';
import { PlanReportDetail } from '@/models/testPlan/testPlanReport';

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

// 测试计划-报告-详情-缺陷分页查询 (分享)
export function getReportShareBugList(data: TableQueryParams) {
  return MSR.post<CommonList<ReportBugItem>>({ url: reportUrl.ReportShareBugListUrl, data });
}

// 测试计划-报告-详情-功能用例分页查询
export function getReportFeatureCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<FeatureCaseItem>>({ url: reportUrl.ReportFeatureCaseListUrl, data });
}

// 测试计划-报告-详情-功能用例分页查询 (分享)
export function getReportShareFeatureCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<FeatureCaseItem>>({ url: reportUrl.ReportShareFeatureCaseListUrl, data });
}

// 测试计划-报告-详情-富文本编辑器上传图片文件
export function editorUploadFile(data: { fileList: File[] }) {
  return MSR.uploadFile({ url: reportUrl.EditorUploadFileUrl }, { fileList: data.fileList }, '', false);
}

// 测试计划-报告-详情-报告内容更新
export function updateReportDetail(data: UpdateReportDetailParams) {
  return MSR.post({ url: reportUrl.UpdateReportDetailUrl, data });
}

// 测试计划-报告-详情
export function getReportDetail(id: string, shareId?: string) {
  if (shareId) {
    return MSR.get({ url: `${reportUrl.PlanReportShareDetailUrl}/${shareId}/${id}` });
  }
  return MSR.get({ url: `${reportUrl.PlanReportDetailUrl}/${id}` });
}

// 测试计划-报告-详情-分享
export function planReportShare(data: GetShareId) {
  return MSR.post({ url: reportUrl.PlanReportShareUrl, data });
}
// 测试计划-报告-分享详情查看
export function planReportShareDetail(shareId: string, reportId: string) {
  return MSR.get({ url: `${reportUrl.PlanReportShareDetailUrl}/${shareId}/${reportId}` });
}
// 测试计划-报告-获取分享链接
export function planGetShareHref(id: string) {
  return MSR.get({ url: `${reportUrl.PlanGetShareHrefDetailUrl}/${id}` });
}
// 测试计划-报告-获取分享链接时效
export function getShareValidity(id: string) {
  return MSR.get({ url: `${reportUrl.GetShareValidityUrl}/${id}` });
}
// 测试计划-独立报告-接口用例
export function getApiPage(data: TableQueryParams) {
  return MSR.post<CommonList<ApiOrScenarioCaseItem>>({ url: reportUrl.ReportIndependentApiUrl, data });
}
// 测试计划-报告-获取分享链接时效
export function getScenarioPage(data: TableQueryParams) {
  return MSR.post<CommonList<ApiOrScenarioCaseItem>>({ url: reportUrl.ReportIndependentScenarioUrl, data });
}
// 测试计划-独立报告-接口用例
export function getShareApiPage(data: TableQueryParams) {
  return MSR.post<CommonList<ApiOrScenarioCaseItem>>({ url: reportUrl.ReportShareApiUrl, data });
}
// 测试计划-报告-获取分享链接时效
export function getShareScenarioPage(data: TableQueryParams) {
  return MSR.post<CommonList<ApiOrScenarioCaseItem>>({ url: reportUrl.ReportShareScenarioUrl, data });
}
// 测试计划-聚合报告-报告明细
export function getReportDetailPage(data: TableQueryParams) {
  return MSR.post<CommonList<PlanReportDetail>>({ url: reportUrl.ReportDetailPageUrl, data });
}
// 测试计划-聚合报告-报告明细-分享
export function getReportDetailSharePage(data: TableQueryParams) {
  return MSR.post<CommonList<PlanReportDetail>>({ url: reportUrl.ReportDetailSharePageUrl, data });
}

export default {};
