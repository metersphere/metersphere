import MSR from '@/api/http/index';
import { GetApiTestList, GetApiTestListUrl } from '@/api/requrls/api-test';
import { QueryParams } from '@/models/common';
import { ApiTestListI } from '@/models/api-test';

export function getTableList(params: QueryParams) {
  const { current, pageSize, sort, filter, keyword } = params;
  return MSR.post<ApiTestListI>({
    url: GetApiTestList,
    data: { current, pageSize, sort, filter, keyword, projectId: 'test-project-id' },
  });
}

export function getlist() {
  return MSR.get<ApiTestListI>({ url: GetApiTestListUrl });
}
