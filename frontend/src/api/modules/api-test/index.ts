import MSR from '@/api/http/index';
import { GetApiTestList, GetApiTestListUrl } from '@/api/requrls/api-test';
import { QueryParams } from '@/components/ms-table/useTable';
import { ApiTestListI } from '@/models/api-test';
import CommonReponse from '@/models/common';

export function getTableList(params: QueryParams) {
  const { current, pageSize } = params;
  return MSR.get<CommonReponse<ApiTestListI>>({ url: `${GetApiTestList}/${current}/${pageSize}` });
}

export function getlist() {
  return MSR.get<CommonReponse<ApiTestListI>>({ url: GetApiTestListUrl });
}
