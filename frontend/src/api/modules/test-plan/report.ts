import MSR from '@/api/http';
import * as reportUrl from '@/api/requrls/test-plan/report';

import type { TableQueryParams } from '@/models/common';

// 报告列表
export function reportList(data: TableQueryParams) {
  return MSR.post({ url: reportUrl.PlanReportListUrl, data });
}

// 删除报告
export function reportDelete(moduleType: string, id: string) {
  return MSR.get({ url: `${reportUrl.PlanDeleteUrl}/${id}` });
}

// 重命名
export function reportRename(moduleType: string, id: string, data: string) {
  return MSR.post({ url: `${reportUrl.PlanReportRenameUrl}/${id}`, data });
}

// 批量删除
export function reportBathDelete(moduleType: string, data: TableQueryParams) {
  return MSR.post({ url: reportUrl.PlanBatchDeleteUrl, data });
}

export default {};
