import MSR from '@/api/http/index';
import { GetApiTestList, GetApiTestListUrl } from '@/api/requrls/api-test';
import { QueryParams } from '@/components/ms-table/useTable';
import { ApiTestListI } from '@/models/api-test';

export function getTableList(params: QueryParams) {
  const { current, pageSize } = params;
  return MSR.post<ApiTestListI>({
    url: GetApiTestList,
    data: { current, pageSize, projectId: 'test-project-id' },
  });
}

export function getlist() {
  return MSR.get<ApiTestListI>({ url: GetApiTestListUrl });
}
