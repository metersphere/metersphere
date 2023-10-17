import MSR from '@/api/http/index';
import * as Url from '@/api/requrls/project-management/menuManagement';

import { TableQueryParams } from '@/models/common';
import type {
  MenuTableConfigItem,
  MenuTableListItem,
  MenuTableListParams,
  PoolOption,
} from '@/models/projectManagement/menuManagement';
import { MenuEnum } from '@/enums/commonEnum';

export async function postTabletList(params: TableQueryParams) {
  return MSR.get<MenuTableListItem[]>({ url: `${Url.getMenuListUrl}${params.projectId}` });
}

// 获取资源池
export async function getPoolOptions(projectId: string, type: MenuEnum) {
  let suffix = '';
  if (type === MenuEnum.apiTest) {
    suffix = 'api';
  }
  if (type === MenuEnum.uiTest) {
    suffix = 'ui';
  }
  return MSR.get<PoolOption[]>({ url: `/project/application/${suffix}/resource/pool/${projectId}` });
}

// 获取审核人
export async function getAuditorOptions(projectId: string, type: MenuEnum) {
  let suffix = '';
  if (type === MenuEnum.loadTest) {
    suffix = 'performance-test';
  }
  if (type === MenuEnum.apiTest) {
    suffix = 'api';
  }
  return MSR.get<PoolOption[]>({ url: `/project/application/${suffix}/user/${projectId}` });
}

export function postUpdateMenu(data: MenuTableListParams, suffix: string) {
  let suffixUrl = '';
  switch (suffix) {
    case MenuEnum.workstation:
      suffixUrl = 'workstation';
      break;
    case MenuEnum.testPlan:
      suffixUrl = 'test-plan';
      break;
    case MenuEnum.bugManagement:
      suffixUrl = 'bug';
      break;
    case MenuEnum.caseManagement:
      suffixUrl = 'case';
      break;
    case MenuEnum.apiTest:
      suffixUrl = 'api';
      break;
    case MenuEnum.uiTest:
      suffixUrl = 'ui';
      break;
    default:
      suffixUrl = 'performance-test';
      break;
  }
  return MSR.post<MenuTableListItem>({ url: `${Url.updateConfigByMenuTypeUrl}${suffixUrl}`, data });
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
  return MSR.post<MenuTableConfigItem>({ url: `${Url.getConfigByMenuTypeUrl}${suffix}`, data });
}

/**
 * 获取平台的下拉选项
 * @param organizationId 组织id
 * @returns
 */
export function getPlatformOptions(organizationId: string, type: MenuEnum) {
  if (type === MenuEnum.bugManagement) {
    return MSR.get<PoolOption[]>({ url: `${Url.getPlatformOptionUrlByBug}${organizationId}` });
  }
  return MSR.get<PoolOption[]>({ url: `${Url.getPlatformOptionUrlByCase}${organizationId}` });
}

export function postSaveDefectSync(data: MenuTableConfigItem, projectId: string) {
  return MSR.post<MenuTableListItem>({ url: `${Url.postSyncBugConfigUrl}${projectId}`, data });
}
