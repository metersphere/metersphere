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
  AddAuthUrl,
} from '@/api/requrls/setting/config';
import { TableQueryParams } from '@/models/common';

import type {
  SaveInfoParams,
  TestEmailParams,
  EmailConfig,
  BaseConfig,
  SavePageConfigParams,
  PageConfigReturns,
  AuthItem,
  AuthParams,
} from '@/models/setting/config';

// 测试邮箱连接
export function testEmail(data: TestEmailParams) {
  return MSR.post({ url: TestEmailUrl, data });
}

// 保存基础信息
export function saveBaseInfo(data: SaveInfoParams) {
  return MSR.post({ url: SaveBaseInfoUrl, data });
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
  return MSR.get<PageConfigReturns>({ url: GetPageConfigUrl });
}

// 获取认证源列表
export function getAuthList(data: TableQueryParams) {
  return MSR.post<AuthItem[]>({ url: GetAuthListUrl, data });
}

// 获取认证源详情
export function getAuthDetail(id: string) {
  return MSR.get<AuthItem>({ url: GetAuthDetailUrl, params: { id } });
}

// 添加认证源
export function addAuth(data: AuthParams) {
  return MSR.post({ url: AddAuthUrl, data });
}

// 更新认证源
export function updateAuth(data: AuthParams) {
  return MSR.post({ url: UpdateAuthUrl, data });
}
