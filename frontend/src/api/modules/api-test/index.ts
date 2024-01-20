import MSR from '@/api/http/index';
import { GetApiTestList, GetApiTestListUrl } from '@/api/requrls/api-test';

import { CommonList, TableQueryParams } from '@/models/common';

export function getTableList(params: TableQueryParams) {
  const { current, pageSize, sort, filter, keyword } = params;
  return MSR.post<CommonList<any>>({
    url: GetApiTestList,
    data: { current, pageSize, sort, filter, keyword, projectId: 'test-project-id' },
  });
}

export function getlist() {
  return MSR.get<CommonList<any>>({ url: GetApiTestListUrl });
}
