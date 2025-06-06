import MSR from '@/api/http/index';
import {
  AddAuthUrl,
  DeleteAuthUrl,
  DeleteModelConfigUrl,
  EditModelConfigUrl,
  EditUploadConfigUrl,
  GetAuthDetailByTypeUrl,
  GetAuthDetailUrl,
  GetAuthListUrl,
  GetBaseInfoUrl,
  GetCleanConfigUrl,
  GetEmailInfoUrl,
  GetModelConfigDetailUrl,
  GetModelConfigNameListUrl,
  GetPageConfigUrl,
  ModelConfigListUrl,
  SaveBaseInfoUrl,
  SaveBaseUrlUrl,
  SaveCleanConfigUrl,
  SaveEmailInfoUrl,
  SavePageConfigUrl,
  TestEmailUrl,
  TestLdapConnectUrl,
  TestLdapLoginUrl,
  UpdateAuthStatusUrl,
  UpdateAuthUrl,
} from '@/api/requrls/setting/config';

import type { CommonList, TableQueryParams } from '@/models/common';
import type {
  AuthItem,
  AuthParams,
  BaseConfig,
  CleanupConfig,
  EmailConfig,
  LDAPConfig,
  LDAPConnectConfig,
  PageConfigReturns,
  SaveInfoParams,
  SavePageConfigParams,
  TestEmailParams,
  UpdateAuthStatusParams,
} from '@/models/setting/config';
import type {
  GetModelConfigListQueryParams,
  ModelConfigItem,
  ModelConfigNameItem,
  ModelFormConfigParams,
} from '@/models/setting/modelConfig';

// 测试邮箱连接
export function testEmail(data: TestEmailParams) {
  return MSR.post({ url: TestEmailUrl, data });
}

// 保存基础信息
export function saveBaseInfo(data: SaveInfoParams) {
  return MSR.post({ url: SaveBaseInfoUrl, data }, { ignoreCancelToken: true });
}

// 保存基础信息
export function saveBaseUrl(baseUrl: string) {
  return MSR.get({ url: SaveBaseUrlUrl, params: { baseUrl } }, { ignoreCancelToken: true });
}

// 获取基础信息
export function getBaseInfo() {
  return MSR.get<BaseConfig>({ url: GetBaseInfoUrl }, { ignoreCancelToken: true });
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

// 获取认证源详情
export function getAuthDetailByType(type: string) {
  return MSR.get<AuthItem>({ url: GetAuthDetailByTypeUrl, params: type });
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

// 保存内存清理配置
export function saveCleanupConfig(data: SaveInfoParams) {
  return MSR.post({ url: SaveCleanConfigUrl, data });
}

// 保存内存清理配置
export function getCleanupConfig() {
  return MSR.get<CleanupConfig>({ url: GetCleanConfigUrl });
}

// 保存内存清理配置
export function saveUploadConfig(data: SaveInfoParams) {
  return MSR.post({ url: EditUploadConfigUrl, data });
}

// 系统设置-模型配置
// 查看模型集合
export function getModelConfigList(data: GetModelConfigListQueryParams) {
  return MSR.post<CommonList<ModelConfigItem>>({ url: ModelConfigListUrl, data });
}

// 编辑模型
export function editModelConfig(data: ModelFormConfigParams) {
  return MSR.post({ url: EditModelConfigUrl, data });
}

// 模型详情
export function getModelConfigDetail(id: string) {
  return MSR.get<ModelConfigItem>({ url: `${GetModelConfigDetailUrl}/${id}` });
}

// 模型名称下拉集合
export function getModelConfigNameList() {
  return MSR.get<ModelConfigNameItem[]>({ url: GetModelConfigNameListUrl }, { ignoreCancelToken: true });
}

// 删除模型
export function deleteModelConfig(id: string) {
  return MSR.get({ url: `${DeleteModelConfigUrl}/${id}` });
}
