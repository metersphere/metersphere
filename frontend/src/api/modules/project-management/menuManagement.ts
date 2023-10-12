import MSR from '@/api/http/index';
import * as Url from '@/api/requrls/project-management/menuManagement';
import type {
  MenuTableListItem,
  MenuTableListParams,
  MenuTableConfigItem,
} from '@/models/projectManagement/menuManagement';
import { MenuEnum } from '@/enums/commonEnum';

import { TableQueryParams, CommonList } from '@/models/common';

export async function postTabletList(params: TableQueryParams): Promise<CommonList<MenuTableListItem>> {
  const list = await MSR.get<MenuTableListItem[]>({ url: `${Url.getMenuListUrl}${params.projectId}` });
  const result: CommonList<MenuTableListItem> = {
    total: list.length,
    list,
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
      suffix = 'bug';
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
  return MSR.post<MenuTableListItem>({ url: `${Url.updateConfigByMenuTypeUrl}${suffix}`, data });
}

export function getConfigByMenuItem(data: MenuTableListParams) {
  let suffix = '';
  switch (data.type) {
    case MenuEnum.workstation:
      suffix = 'workstation';
      break;
    case MenuEnum.testPlan:
      suffix = 'test-plan';
      break;
    case MenuEnum.bugManagement:
      suffix = 'bug';
      break;
    case MenuEnum.caseManagement:
      suffix = 'case';
      break;
    case MenuEnum.apiTest:
      suffix = 'api';
      break;
    case MenuEnum.uiTest:
      suffix = 'ui';
      break;
    default:
      suffix = 'performance-test';
      break;
  }
  return MSR.post<MenuTableConfigItem[]>({ url: `${Url.getConfigByMenuTypeUrl}${suffix}`, data });
}
