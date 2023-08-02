import MSR from '@/api/http/index';
import {
  TestEmailUrl,
  SaveBaseInfoUrl,
  SaveEmailInfoUrl,
  GetBaseInfoUrl,
  GetEmailInfoUrl,
  SavePageConfigUrl,
  GetPageConfigUrl,
} from '@/api/requrls/setting/config';

import type {
  SaveInfoParams,
  TestEmailParams,
  EmailConfig,
  BaseConfig,
  SavePageConfigParams,
  PageConfigReturns,
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
