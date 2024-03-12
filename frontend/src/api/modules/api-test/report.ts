import MSR from '@/api/http';
import * as reportUrl from '@/api/requrls/api-test/report';

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
export function reportDetail(reportId: string) {
  return MSR.get<Record<string, any>>({ url: `${reportUrl.ScenarioReportDetailUrl}/${reportId}` });
}

export default {};
