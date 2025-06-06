import type { ViewDetail, ViewList, ViewParams } from '@/components/pure/ms-advance-filter/type';

import MSR from '@/api/http/index';
import {
  AddAPIKEYUrl,
  AddLocalConfigUrl,
  DeleteAPIKEYUrl,
  DisableAPIKEYUrl,
  DisableLocalConfigUrl,
  EnableAPIKEYUrl,
  EnableLocalConfigUrl,
  GeDingInfoUrl,
  GeLarkInfoUrl,
  GeLarkSuiteInfoUrl,
  GetAPIKEYListUrl,
  getAuthenticationUrl,
  GetDefaultLocaleUrl,
  GetDingCallbackUrl,
  GetInfoUrl,
  GetLarkCallbackUrl,
  GetLarkSuiteCallbackUrl,
  GetLocalConfigUrl,
  GetMenuListUrl,
  GetPlatformAccountUrl,
  GetPlatformOrgOptionUrl,
  GetPlatformParamUrl,
  GetPlatformUrl,
  GetPublicKeyUrl,
  GetWeComCallbackUrl,
  GetWeComInfoUrl,
  isLoginUrl,
  ldapLoginUrl,
  LoginUrl,
  LogoutUrl,
  PersonalDeleteModelConfigUrl,
  PersonalEditModelConfigUrl,
  PersonalGetModelConfigDetailUrl,
  PersonalModelConfigListUrl,
  SavePlatformUrl,
  UpdateAPIKEYUrl,
  UpdateInfoUrl,
  UpdateLanguageUrl,
  UpdateLocalConfigUrl,
  UpdatePswUrl,
  ValidAPIKEYUrl,
  ValidatePlatformUrl,
  ValidLocalConfigUrl,
} from '@/api/requrls/user';

import type { CommonList } from '@/models/common';
import type {
  GetModelConfigListQueryParams,
  ModelConfigItem,
  ModelFormConfigParams,
} from '@/models/setting/modelConfig';
import type {
  AddLocalConfigParams,
  APIKEY,
  LocalConfig,
  LoginData,
  LoginRes,
  OrgOptionItem,
  PersonalInfo,
  UpdateAPIKEYParams,
  UpdateBaseInfo,
  UpdateLanguage,
  UpdateLocalConfigParams,
  UpdatePswParams,
} from '@/models/user';
import { DingInfo, LarkInfo, WecomInfo } from '@/models/user';

import type { LocaleType } from '#/global';
import type { RouteRecordNormalized } from 'vue-router';

export function login(data: LoginData) {
  let url = '';
  if (data.authenticate === 'LOCAL') {
    url = LoginUrl;
  } else if (data.authenticate === 'LDAP') {
    url = ldapLoginUrl;
  }
  return MSR.post<LoginRes>({ url, data });
}

export function isLogin() {
  return MSR.get<LoginRes>({ url: isLoginUrl }, { ignoreCancelToken: true, errorMessageMode: 'none' });
}
// 获取登录认证方式
export function getAuthenticationList() {
  return MSR.get<string[]>({ url: getAuthenticationUrl }, { ignoreCancelToken: true, errorMessageMode: 'none' });
}

export function getPlatformParamUrl() {
  return MSR.get<OrgOptionItem[]>({ url: GetPlatformParamUrl }, { ignoreCancelToken: true, errorMessageMode: 'none' });
}

export function getWeComInfo() {
  return MSR.get<WecomInfo>({ url: GetWeComInfoUrl }, { ignoreCancelToken: true, errorMessageMode: 'none' });
}

export function getWeComCallback(code: string) {
  return MSR.get<LoginRes>({ url: GetWeComCallbackUrl, params: { code } });
}

export function getDingInfo() {
  return MSR.get<DingInfo>({ url: GeDingInfoUrl }, { ignoreCancelToken: true, errorMessageMode: 'none' });
}

export function getDingCallback(code: string) {
  return MSR.get<LoginRes>({ url: GetDingCallbackUrl, params: { code } });
}

export function getLarkInfo() {
  return MSR.get<LarkInfo>({ url: GeLarkInfoUrl }, { ignoreCancelToken: true, errorMessageMode: 'none' });
}

export function getLarkCallback(code: string) {
  return MSR.get<LoginRes>({ url: GetLarkCallbackUrl, params: { code } });
}

export function getLarkSuiteInfo() {
  return MSR.get<LarkInfo>({ url: GeLarkSuiteInfoUrl }, { ignoreCancelToken: true, errorMessageMode: 'none' });
}

export function getLarkSuiteCallback(code: string) {
  return MSR.get<LoginRes>({ url: GetLarkSuiteCallbackUrl, params: { code } });
}

export function logout() {
  return MSR.get<LoginRes>({ url: LogoutUrl });
}

export function getMenuList() {
  return MSR.post<RouteRecordNormalized[]>({ url: GetMenuListUrl });
}

export function getPublicKeyRequest() {
  return MSR.get<string>({ url: GetPublicKeyUrl }, { ignoreCancelToken: true });
}

// 个人设置-更新本地执行
export function updateLocalConfig(data: UpdateLocalConfigParams) {
  return MSR.post({ url: UpdateLocalConfigUrl, data });
}

// 个人设置-添加本地执行
export function addLocalConfig(data: AddLocalConfigParams) {
  return MSR.post({ url: AddLocalConfigUrl, data });
}

// 个人设置-验证本地执行配置
export function validLocalConfig(host: string) {
  return MSR.get({ url: `${host}${ValidLocalConfigUrl}` });
}

// 个人设置-获取本地执行配置
export function getLocalConfig() {
  return MSR.get<LocalConfig[]>({ url: GetLocalConfigUrl }, { ignoreCancelToken: true });
}

// 个人设置-启用本地执行配置
export function enableLocalConfig(id: string) {
  return MSR.get({ url: EnableLocalConfigUrl, params: id });
}

// 个人设置-禁用本地执行配置
export function disableLocalConfig(id: string) {
  return MSR.get({ url: DisableLocalConfigUrl, params: id });
}

// 个人设置-修改 APIKEY
export function updateAPIKEY(data: UpdateAPIKEYParams) {
  return MSR.post({ url: UpdateAPIKEYUrl, data });
}

// 个人设置-验证 APIKEY
export function validAPIKEY() {
  return MSR.get({ url: ValidAPIKEYUrl });
}

// 个人设置-获取 APIKEY 列表
export function getAPIKEYList() {
  return MSR.get<APIKEY[]>({ url: GetAPIKEYListUrl });
}

// 个人设置-开启 APIKEY
export function enableAPIKEY(id: string) {
  return MSR.get({ url: EnableAPIKEYUrl, params: id });
}

// 个人设置-关闭 APIKEY
export function disableAPIKEY(id: string) {
  return MSR.get({ url: DisableAPIKEYUrl, params: id });
}

// 个人设置-删除 APIKEY
export function deleteAPIKEY(id: string) {
  return MSR.get({ url: DeleteAPIKEYUrl, params: id });
}

// 个人设置-生成 APIKEY
export function addAPIKEY() {
  return MSR.get({ url: AddAPIKEYUrl });
}

// 个人信息-获取基本信息
export function getBaseInfo(id: string) {
  return MSR.get<PersonalInfo>({ url: GetInfoUrl, params: id });
}

// 个人信息-修改基本信息
export function updateBaseInfo(data: UpdateBaseInfo) {
  return MSR.post({ url: UpdateInfoUrl, data });
}

// 个人信息-修改基本信息
export function updateLanguage(data: UpdateLanguage) {
  return MSR.post({ url: UpdateLanguageUrl, data });
}

// 个人信息-修改密码
export function updatePsw(data: UpdatePswParams) {
  return MSR.post({ url: UpdatePswUrl, data });
}

// 个人信息-校验第三方平台账号信息
export function validatePlatform(id: string, orgId: string, data: Record<string, any>) {
  return MSR.post({ url: `${ValidatePlatformUrl}/${id}/${orgId}`, data });
}

// 个人信息-保存第三方平台账号信息
export function savePlatform(data: Record<string, any>) {
  return MSR.post({ url: SavePlatformUrl, data });
}

// 个人信息-获取第三方平台账号信息
export function getPlatform(orgId: string) {
  return MSR.get({ url: GetPlatformUrl, params: orgId });
}

// 个人信息-获取第三方平台账号信息-插件信息
export function getPlatformAccount() {
  return MSR.get({ url: GetPlatformAccountUrl });
}

// 个人信息-获取第三方平台-组织下拉选项
export function getPlatformOrgOption() {
  return MSR.get<OrgOptionItem[]>({ url: GetPlatformOrgOptionUrl });
}

// 获取默认语言配置
export function getDefaultLocale() {
  return MSR.get<LocaleType>({ url: GetDefaultLocaleUrl });
}

// 视图列表
export function getViewList(viewType: string, scopeId: string) {
  return MSR.get<ViewList>({ url: `/user-view/${viewType}/grouped/list`, params: { scopeId } });
}
// 视图详情
export function getViewDetail(viewType: string, id: string) {
  return MSR.get<ViewDetail>({ url: `/user-view/${viewType}/get/${id}` });
}
// 编辑视图
export function updateView(viewType: string, data: ViewParams) {
  return MSR.post({ url: `/user-view/${viewType}/update`, data });
}
// 新增视图
export function addView(viewType: string, data: ViewParams) {
  return MSR.post({ url: `/user-view/${viewType}/add`, data });
}
// 删除视图
export function deleteView(viewType: string, id: string) {
  return MSR.get({ url: `/user-view/${viewType}/delete/${id}` });
}

// 个人中心-模型配置
// 查看模型集合
export function getPersonalModelConfigList(data: GetModelConfigListQueryParams) {
  return MSR.post<CommonList<ModelConfigItem>>({ url: PersonalModelConfigListUrl, data });
}
// 编辑模型
export function editPersonalModelConfig(data: ModelFormConfigParams) {
  return MSR.post({ url: PersonalEditModelConfigUrl, data });
}
// 模型详情
export function getPersonalModelConfigDetail(id: string) {
  return MSR.get<ModelConfigItem>({ url: `${PersonalGetModelConfigDetailUrl}/${id}` });
}
// 删除模型
export function deletePersonalModelConfig(id: string) {
  return MSR.get({ url: `${PersonalDeleteModelConfigUrl}/${id}` });
}
