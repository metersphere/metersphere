import MSR from '@/api/http/index';
import { GetApiTestList, GetApiTestListUrl } from '@/api/requrls/api-test';
import { TableQueryParams } from '@/models/common';
import { CommonList } from '@/models/api-test';

export function getTableList(params: TableQueryParams) {
  const { current, pageSize, sort, filter, keyword } = params;
  return MSR.post<CommonList>({
    url: GetApiTestList,
    data: { current, pageSize, sort, filter, keyword, projectId: 'test-project-id' },
  });
}

export function getlist() {
  return MSR.get<CommonList>({ url: GetApiTestListUrl });
}
