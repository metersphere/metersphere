import MSR from '@/api/http/index';
import {
  GetDingTalkInfoUrl,
  GetPlatformInfoUrl,
  GetWeComInfoUrl,
  PostDingTalkEnableUrl,
  PostDingTalkSaveUrl,
  PostValidateDingTalkUrl,
  PostValidateWeComUrl,
  PostWeComEnableUrl,
  PostWeComSaveUrl,
} from '@/api/requrls/setting/qrCode';

import { DingTalkInfo, EnableEditorRequest, PlatformSourceList, WeComInfo } from '@/models/setting/qrCode';

// 获取企业微信配置
export function getWeComInfo() {
  return MSR.get<WeComInfo>({ url: GetWeComInfoUrl });
}

// 获取钉钉配置
export function getDingInfo() {
  return MSR.get<DingTalkInfo>({ url: GetDingTalkInfoUrl });
}

// 保存企业微信登陆配置
export function saveWeComConfig(data: WeComInfo) {
  return MSR.post({ url: PostWeComSaveUrl, data });
}

// 保存钉钉登陆配置
export function saveDingTalkConfig(data: DingTalkInfo) {
  return MSR.post({ url: PostDingTalkSaveUrl, data });
}

// 校验企业微信外链接
export function validateWeComConfig(data: WeComInfo) {
  return MSR.post({ url: PostValidateWeComUrl, data });
}

// 校验钉钉外链接
export function validateDingTalkConfig(data: DingTalkInfo) {
  return MSR.post({ url: PostValidateDingTalkUrl, data });
}

// 开启企业微信登陆
export function enableWeCom(data: EnableEditorRequest) {
  return MSR.post({ url: PostWeComEnableUrl, data });
}

// 开启钉钉登陆
export function enableDingTalk(data: EnableEditorRequest) {
  return MSR.post({ url: PostDingTalkEnableUrl, data });
}

// 获取所有平台配置基础信息
export function getPlatformSourceList() {
  return MSR.get<PlatformSourceList>({ url: GetPlatformInfoUrl });
}
