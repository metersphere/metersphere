import MSR from '@/api/http/index';
import * as Url from '@/api/requrls/project-management/menuManagement';
import type { MenuTableListItem, MenuTableListParams } from '@/models/projectManagement/menuManagement';
import { MenuEnum } from '@/enums/commonEnum';

import { TableQueryParams, CommonList } from '@/models/common';

const tableList: MenuTableListItem[] = [
  'workstation',
  'loadTest',
  'testPlan',
  'bugManagement',
  'caseManagement',
  'apiTest',
  'uiTest',
].map((item, index) => {
  return { module: item, moduleEnable: index % 2 === 0 };
});

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export function postTabletList(data: TableQueryParams) {
  // return MSR.post<CommonList<MenuTableListItem>>({ url: Url.getMenuListUrl, data });
  const result: CommonList<MenuTableListItem> = {
    list: tableList,
    total: tableList.length,
    pageSize: 10,
    current: 1,
  };
  return Promise.resolve(result);
}

export function postUpdateMenu(data: MenuTableListParams) {
  let suffix = '';
  switch (data.type) {
    case MenuEnum.workstation:
      suffix = 'workstation';
      break;
    case MenuEnum.testPlan:
      suffix = 'test-plan';
      break;
    case MenuEnum.bugManagement:
      suffix = 'issue';
      break;
    case MenuEnum.caseManagement:
      suffix = 'case';
      break;
    case MenuEnum.apiTest:
      suffix = 'api';
      break;
    case MenuEnum.uiTest:
      suffix = 'uiTest';
      break;
    default:
      suffix = 'performance-test';
      break;
  }
  return MSR.post<string>({ url: `${Url.updateConfigByMenuTypeUrl}${suffix}`, data });
}

export default {};
