import MSR from '@/api/http/index';
import {
  TestEmailUrl,
  SaveBaseInfoUrl,
  SaveEmailInfoUrl,
  GetBaseInfoUrl,
  GetEmailInfoUrl,
  SavePageConfigUrl,
  GetPageConfigUrl,
  GetAuthListUrl,
  GetAuthDetailUrl,
  UpdateAuthUrl,
  UpdateAuthStatusUrl,
  AddAuthUrl,
  DeleteAuthUrl,
  TestLdapConnectUrl,
  TestLdapLoginUrl,
} from '@/api/requrls/setting/config';

import type { CommonList, TableQueryParams } from '@/models/common';
import type {
  SaveInfoParams,
  TestEmailParams,
  EmailConfig,
  BaseConfig,
  SavePageConfigParams,
  PageConfigReturns,
  AuthItem,
  AuthParams,
  UpdateAuthStatusParams,
  LDAPConfig,
  LDAPConnectConfig,
} from '@/models/setting/config';

// 测试邮箱连接
export function testEmail(data: TestEmailParams) {
  return MSR.post({ url: TestEmailUrl, data });
}

// 保存基础信息
export function saveBaseInfo(data: SaveInfoParams) {
  return MSR.post({ url: SaveBaseInfoUrl, data }, { ignoreCancelToken: true });
}

// 获取基础信息
export function getBaseInfo() {
  return MSR.get<BaseConfig>({ url: GetBaseInfoUrl });
}

// 保存邮箱信息
export function saveEmailInfo(data: SaveInfoParams) {
  return MSR.post({ url: SaveEmailInfoUrl, data });
}

// 获取邮箱信息
export function getEmailInfo() {
  return MSR.get<EmailConfig>({ url: GetEmailInfoUrl });
}

// 保存界面配置
export function savePageConfig(data: SavePageConfigParams) {
  return MSR.uploadFile({ url: SavePageConfigUrl }, data, 'files');
}

// 获取界面配置
export function getPageConfig() {
  return MSR.get<PageConfigReturns>({ url: GetPageConfigUrl }, { ignoreCancelToken: true });
}

// 获取认证源列表
export function getAuthList(data: TableQueryParams) {
  return MSR.post<CommonList<AuthItem>>({ url: GetAuthListUrl, data });
}

// 获取认证源详情
export function getAuthDetail(id: string) {
  return MSR.get<AuthItem>({ url: GetAuthDetailUrl, params: id });
}

// 添加认证源
export function addAuth(data: AuthParams) {
  return MSR.post({ url: AddAuthUrl, data });
}

// 更新认证源
export function updateAuth(data: AuthParams) {
  return MSR.post({ url: UpdateAuthUrl, data });
}

// 更新认证源状态
export function updateAuthStatus(data: UpdateAuthStatusParams) {
  return MSR.post({ url: UpdateAuthStatusUrl, data });
}

// 删除认证源
export function deleteAuth(id: string) {
  return MSR.get({ url: DeleteAuthUrl, params: id });
}

// 测试ldap连接
export function testLdapConnect(data: LDAPConnectConfig) {
  return MSR.post({ url: TestLdapConnectUrl, data });
}

// 测试ldap登录
export function testLdapLogin(data: LDAPConfig) {
  return MSR.post({ url: TestLdapLoginUrl, data });
}
