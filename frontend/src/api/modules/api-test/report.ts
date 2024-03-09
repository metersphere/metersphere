import MSR from '@/api/http';
import * as orgUrl from '@/api/requrls/setting/organizationAndProject';

import type { TableQueryParams } from '@/models/common';
// 报告列表
export function reportList(data: TableQueryParams) {
  return MSR.post({ url: orgUrl.postProjectTableUrl, data });
}
// 删除报告
export function reportDelete(id: string) {}

// 重命名
export function reportRename(id: string, reportName: string) {}

// 批量删除
export function reportBathDelete(ids: Array<string>) {}

export default {};
