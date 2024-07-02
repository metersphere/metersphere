import MSR from '@/api/http/index';
import {
  GetDingTalkInfoUrl,
  GetLarkInfoUrl,
  GetLarkSuiteInfoUrl,
  GetPlatformInfoUrl,
  GetWeComInfoUrl,
  PostDingTalkEnableUrl,
  PostDingTalkSaveUrl,
  PostDingTalkValidateFalseUrl,
  PostLarkEnableUrl,
  PostLarkSaveUrl,
  PostLarkSuiteEnableUrl,
  PostLarkSuiteSaveUrl,
  PostLarkSuiteValidateFalseUrl,
  PostLarkValidateFalseUrl,
  PostValidateDingTalkUrl,
  PostValidateLarkSuiteUrl,
  PostValidateLarkUrl,
  PostValidateWeComUrl,
  PostWeComEnableUrl,
  PostWeComSaveUrl,
  PostWeComValidateFalseUrl,
} from '@/api/requrls/setting/qrCode';

import { DingTalkInfo, EnableEditorRequest, LarkInfo, PlatformSourceList, WeComInfo } from '@/models/setting/qrCode';

// 获取所有平台配置基础信息
export function getPlatformSourceList() {
  return MSR.get<PlatformSourceList>({ url: GetPlatformInfoUrl });
}

// 获取企业微信配置
export function getWeComInfo() {
  return MSR.get<WeComInfo>({ url: GetWeComInfoUrl });
}

// 获取钉钉配置
export function getDingInfo() {
  return MSR.get<DingTalkInfo>({ url: GetDingTalkInfoUrl });
}

// 获取飞书配置
export function getLarkInfo() {
  return MSR.get<LarkInfo>({ url: GetLarkInfoUrl });
}

// 获取国际飞书配置
export function getLarkSuiteInfo() {
  return MSR.get<LarkInfo>({ url: GetLarkSuiteInfoUrl });
}

// 保存企业微信登陆配置
export function saveWeComConfig(data: WeComInfo) {
  return MSR.post({ url: PostWeComSaveUrl, data });
}

// 保存钉钉登陆配置
export function saveDingTalkConfig(data: DingTalkInfo) {
  return MSR.post({ url: PostDingTalkSaveUrl, data });
}

// 保存飞书登陆配置
export function saveLarkConfig(data: LarkInfo) {
  return MSR.post({ url: PostLarkSaveUrl, data });
}

// 保存国际飞书登陆配置
export function saveLarkSuiteConfig(data: LarkInfo) {
  return MSR.post({ url: PostLarkSuiteSaveUrl, data });
}

// 校验企业微信外链接
export function validateWeComConfig(data: WeComInfo) {
  return MSR.post({ url: PostValidateWeComUrl, data });
}

// 校验钉钉外链接
export function validateDingTalkConfig(data: DingTalkInfo) {
  return MSR.post({ url: PostValidateDingTalkUrl, data });
}

// 校验飞书外链接
export function validateLarkConfig(data: LarkInfo) {
  return MSR.post({ url: PostValidateLarkUrl, data });
}

// 校验国际飞书外链接
export function validateLarkSuiteConfig(data: LarkInfo) {
  return MSR.post({ url: PostValidateLarkSuiteUrl, data });
}

// 开启企业微信登陆
export function enableWeCom(data: EnableEditorRequest) {
  return MSR.post({ url: PostWeComEnableUrl, data });
}

// 开启钉钉登陆
export function enableDingTalk(data: EnableEditorRequest) {
  return MSR.post({ url: PostDingTalkEnableUrl, data });
}

// 开启飞书登陆
export function enableLark(data: EnableEditorRequest) {
  return MSR.post({ url: PostLarkEnableUrl, data });
}

// 开启国际飞书登陆
export function enableLarkSuite(data: EnableEditorRequest) {
  return MSR.post({ url: PostLarkSuiteEnableUrl, data });
}

// 开启企业微信登陆
export function closeValidateWeCom() {
  return MSR.post({ url: PostWeComValidateFalseUrl });
}

// 开启钉钉登陆
export function closeValidateDingTalk() {
  return MSR.post({ url: PostDingTalkValidateFalseUrl });
}

// 开启飞书登陆
export function closeValidateLark() {
  return MSR.post({ url: PostLarkValidateFalseUrl });
}

// 开启国际飞书登陆
export function closeValidateLarkSuite() {
  return MSR.post({ url: PostLarkSuiteValidateFalseUrl });
}
