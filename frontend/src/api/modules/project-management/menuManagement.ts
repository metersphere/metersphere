import { FormItem } from '@/components/pure/ms-form-create/types';

import MSR from '@/api/http/index';
import * as Url from '@/api/requrls/project-management/menuManagement';

import { TableQueryParams } from '@/models/common';
import type {
  FakeTableListItem,
  FakeTableOperationParams,
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
    case MenuEnum.taskCenter:
      suffixUrl = 'task';
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
    case MenuEnum.taskCenter:
      suffix = 'task';
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

/**
 * 获取平台的下拉选项
 * @param organizationId 组织id
 * @returns
 */
export function getPlatformInfo(pluginId: string, type: MenuEnum) {
  if (type === MenuEnum.bugManagement) {
    return MSR.get<{ formItems: FormItem[] }>({ url: `${Url.getPluginInfoByBug}${pluginId}` });
  }
  return MSR.get<{ formItems: FormItem[] }>({ url: `${Url.getPluginInfoByCase}${pluginId}` });
}

/**
 * 缺陷同步保存
 * @param data 缺陷同步配置项
 * @param projectId 项目ID
 * @returns
 */
export function postSaveDefectSync(data: MenuTableConfigItem, projectId: string) {
  return MSR.post<MenuTableListItem>({ url: `${Url.postSyncBugConfigUrl}${projectId}`, data });
}

/**
 * 关联用例保存
 * @param data 缺陷同步配置项
 * @param projectId 项目ID
 * @returns
 */
export function postSaveRelatedCase(data: MenuTableConfigItem, projectId: string) {
  return MSR.post<MenuTableListItem>({ url: `${Url.postRelatedCaseConfigUrl}${projectId}`, data });
}

// 误报规则列表查询
export function postFakeTableList(data: TableQueryParams) {
  return MSR.post<FakeTableListItem[]>({ url: `${Url.postFakeTableUrl}`, data });
}
// 误报规则新增
export function postAddFake(data: FakeTableListItem[]) {
  return MSR.post<FakeTableListItem[]>({ url: Url.postFakeTableAddUrl, data });
}
// 误报规则更新
export function postUpdateFake(data: FakeTableListItem[]) {
  return MSR.post<FakeTableListItem[]>({ url: Url.postFakeTableUpdateUrl, data });
}

// 误报规则启用禁用
export function postUpdateEnableFake(data: FakeTableOperationParams) {
  return MSR.post<FakeTableListItem[]>({ url: Url.postFakeTableEnableUrl, data });
}

// 误报规则删除
export function getDeleteFake(data: FakeTableOperationParams) {
  return MSR.post<FakeTableListItem[]>({ url: Url.getFakeTableDeleteUrl, data });
}

// JIRA插件key校验
export function validateJIRAKey(data: object, pluginId: string) {
  return MSR.post<MenuTableConfigItem>({ url: `${Url.postValidateJiraKeyUrl}${pluginId}`, data });
}
// 缺陷管理-获取同步信息
export function getBugSyncInfo(projectId: string) {
  return MSR.get<MenuTableConfigItem>({ url: `${Url.getBugSyncInfoUrl}${projectId}` });
}

// 用例管理-获取关联需求信息
export function getCaseRelatedInfo(projectId: string) {
  return MSR.get<{
    demand_platform_config: string;
    platform_key: string;
    case_enable: string;
    sync_enable: string;
    cron_expression: string;
  }>({
    url: `${Url.getCaseRelatedInfoUrl}${projectId}`,
  });
}
