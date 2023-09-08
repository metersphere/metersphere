import MSR from '@/api/http/index';
import { FileDetailUrl, FileListUrl } from '@/api/requrls/project-management/fileManagement';

import type { TableQueryParams, CommonList } from '@/models/common';

// 获取文件列表
export function getFileList(data: TableQueryParams) {
  return MSR.post<CommonList<any>>({ url: FileListUrl, data });
}

// 获取文件详情
export function getFileDetail(data: TableQueryParams) {
  return MSR.post<CommonList<any>>({ url: FileDetailUrl, data });
}
