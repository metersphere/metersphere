import MSR from '@/api/http';
import * as reportUrl from '@/api/requrls/api-test/report';

import type { GetShareId, ReportDetail, ReportStepDetail } from '@/models/apiTest/report';
import type { TableQueryParams } from '@/models/common';
import { ReportEnum } from '@/enums/reportEnum';

// 报告列表
export function reportList(data: TableQueryParams) {
  if (data.moduleType === ReportEnum.API_SCENARIO_REPORT) {
    return MSR.post({ url: reportUrl.ScenarioReportListUrl, data });
  }
  return MSR.post({ url: reportUrl.ApiReportListUrl, data });
}

// 删除报告
export function reportDelete(moduleType: string, id: string) {
  if (moduleType === ReportEnum.API_SCENARIO_REPORT) {
    return MSR.get({ url: `${reportUrl.ScenarioDeleteUrl}/${id}` });
  }
  return MSR.get({ url: `${reportUrl.ApiDeleteUrl}/${id}` });
}

// 重命名
export function reportRename(moduleType: string, id: string, reportName: string) {
  if (moduleType === ReportEnum.API_SCENARIO_REPORT) {
    return MSR.get({ url: `${reportUrl.ScenarioReportRenameUrl}/${id}/${reportName}` });
  }
  return MSR.get({ url: `${reportUrl.ApiReportRenameUrl}/${id}/${reportName}` });
}

// 批量删除
export function reportBathDelete(moduleType: string, data: TableQueryParams) {
  if (moduleType === ReportEnum.API_SCENARIO_REPORT) {
    return MSR.post({ url: reportUrl.ScenarioBatchDeleteUrl, data });
  }
  return MSR.post({ url: reportUrl.ApiBatchDeleteUrl, data });
}

// 报告详情
export function reportScenarioDetail(reportId: string, shareId?: string | undefined) {
  if (shareId) {
    return MSR.get<ReportDetail>({ url: `${reportUrl.ScenarioReportShareDetailUrl}/${shareId}/${reportId}` });
  }
  return MSR.get<ReportDetail>({ url: `${reportUrl.ScenarioReportDetailUrl}/${reportId}` });
}
// 报告步骤详情
export function reportStepDetail(reportId?: string, stepId?: string, shareId?: string | undefined) {
  if (shareId) {
    return MSR.get<ReportStepDetail>({
      url: `${reportUrl.ScenarioReportShareDetailStepUrl}/${shareId}/${reportId}/${stepId}`,
    });
  }
  return MSR.get<ReportStepDetail>({ url: `${reportUrl.ScenarioReportDetailStepUrl}/${reportId}/${stepId}` });
}
// 用例报告详情
export function reportCaseDetail(reportId: string, shareId?: string | undefined) {
  if (shareId) {
    return MSR.get<ReportDetail>({ url: `${reportUrl.CaseReportShareDetailUrl}/${shareId}/${reportId}` });
  }
  return MSR.get<ReportDetail>({ url: `${reportUrl.CaseReportDetailUrl}/${reportId}` });
}

// 报告步骤详情
export function reportCaseStepDetail(reportId: string, stepId: string, shareId?: string | undefined) {
  if (shareId) {
    return MSR.get<ReportStepDetail[]>({
      url: `${reportUrl.CaseStepDetailShareStepUrl}/${shareId}/${reportId}/${stepId}`,
    });
  }
  return MSR.get<ReportStepDetail[]>({ url: `${reportUrl.CaseStepDetailStepUrl}/${reportId}/${stepId}` });
}

// 生成分享id
export function getShareInfo(data: GetShareId) {
  return MSR.post({ url: `${reportUrl.getShareIdUrl}`, data });
}
// 获取场景分享详情
export function getScenarioReportShareDetail(data: { shareId: string; reportId: string }) {
  return MSR.get<ReportStepDetail>({
    url: `${reportUrl.ScenarioReportDetailUrl}/${data.shareId}/${data.reportId}`,
  });
}
// 获取用例分享详情
export function getCaseReportShareDetail(data: { shareId: string; reportId: string }) {
  return MSR.get<ReportStepDetail>({
    url: `${reportUrl.CaseReportDetailUrl}/${data.shareId}/${data.reportId}`,
  });
}

// 报告分享(场景)
export function reportScenarioShare(shareId: string, stepId: string) {
  return MSR.get<ReportStepDetail[]>({ url: `${reportUrl.reportScenarioShareUrl}/${shareId}/${stepId}` });
}
// 报告分享(用例)
export function reportCaseShare(shareId: string, stepId: string) {
  return MSR.get<ReportStepDetail[]>({ url: `${reportUrl.reportCaseShareUrl}/${shareId}/${stepId}` });
}
// 获取分享报告id
export function getShareReportInfo(shareId: string) {
  return MSR.get<ReportDetail>({ url: `${reportUrl.getShareReportInfoUrl}/${shareId}` });
}

export default {};
