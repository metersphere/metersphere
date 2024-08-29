import { get } from '../plugins/request';


export const GetWeComInfoUrl = '/we_com/info'; // 获取企业微信登陆的配置信息
export const GetWeComCallbackUrl = '/sso/callback/we_com'; // 获取企业微信登陆的回调信息
export const GetPlatformParamUrl = '/platform/get/param';
export const GeDingInfoUrl = '/ding_talk/info'; // 获取企业微信登陆的配置信息
export const GetDingCallbackUrl = '/sso/callback/ding_talk'; // 获取企业微信登陆的回调信息
export const GeLarkInfoUrl = '/lark/info'; // 获取飞书登陆的配置信息
export const GetLarkCallbackUrl = '/sso/callback/lark'; // 获取飞书登陆的回调信息
export const GeLarkSuiteInfoUrl = '/lark_suite/info'; // 获取国际飞书登陆的配置信息
export const GetLarkSuiteCallbackUrl = '/sso/callback/lark_suite'; // 获取国际飞书登陆的回调信息


export function getPlatformParamUrl() {
  return get(GetPlatformParamUrl);
}

export function getWeComInfo() {
  return get(GetWeComInfoUrl);
}

export function getWeComCallback(code) {
  return get(GetWeComCallbackUrl, code);
}

export function getDingInfo() {
  return get(GeDingInfoUrl);
}

export function getDingCallback(code) {
  return get(GetDingCallbackUrl, code );
}

export function getLarkInfo() {
  return get(GeLarkInfoUrl);
}

export function getLarkCallback(code) {
  return get(GetLarkCallbackUrl, code);
}

export function getLarkSuiteInfo() {
  return get(GeLarkSuiteInfoUrl);
}

export function getLarkSuiteCallback(code) {
  return get(GetLarkSuiteCallbackUrl, code);
}
