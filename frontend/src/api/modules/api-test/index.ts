import MSR from '@/api/http/index';
import { GetApiTestList, GetApiTestListUrl } from '@/api/requrls/api-test';
import { TableQueryParams, CommonList } from '@/models/common';
import { APIListItemI } from '@/models/api-test';

export function getTableList(params: TableQueryParams) {
  const { current, pageSize, sort, filter, keyword } = params;
  return MSR.post<CommonList<APIListItemI>>({
    url: GetApiTestList,
    data: { current, pageSize, sort, filter, keyword, projectId: 'test-project-id' },
  });
}

export function getlist() {
  return MSR.get<CommonList<APIListItemI>>({ url: GetApiTestListUrl });
}
