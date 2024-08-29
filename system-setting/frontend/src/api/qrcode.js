import {get, post} from 'metersphere-frontend/src/plugins/request';

const GetPlatformInfoUrl = '/platform/get/info';
const PostValidateWeComUrl = '/we_com/validate';
const GetWeComInfoUrl = '/we_com/info/with_detail';
const PostWeComSaveUrl = '/we_com/save';
const PostWeComEnableUrl = '/we_com/enable';
const PostWeComValidateFalseUrl = '/we_com/change/validate';

const PostValidateDingTalkUrl = '/ding_talk/validate';
const GetDingTalkInfoUrl = '/ding_talk/info/with_detail';
const PostDingTalkSaveUrl = '/ding_talk/save';
const PostDingTalkEnableUrl = '/ding_talk/enable';
const PostDingTalkValidateFalseUrl = '/ding_talk/change/validate';

const PostValidateLarkUrl = '/lark/validate';
const GetLarkInfoUrl = '/lark/info/with_detail';
const PostLarkSaveUrl = '/lark/save';
const PostLarkEnableUrl = '/lark/enable';
const PostLarkValidateFalseUrl = '/lark/change/validate';

const PostValidateLarkSuiteUrl = '/lark_suite/validate';
const GetLarkSuiteInfoUrl = '/lark_suite/info/with_detail';
const PostLarkSuiteSaveUrl = '/lark_suite/save';
const PostLarkSuiteEnableUrl = '/lark_suite/enable';
const PostLarkSuiteValidateFalseUrl = '/lark_suite/change/validate';

// 获取所有平台配置基础信息
export function getPlatformSourceList() {
  return get(GetPlatformInfoUrl);
}

// 获取企业微信配置
export function getWeComInfo() {
  return get(GetWeComInfoUrl);
}

// 获取钉钉配置
export function getDingInfo() {
  return get(GetDingTalkInfoUrl);
}

// 获取飞书配置
export function getLarkInfo() {
  return get(GetLarkInfoUrl);
}

// 获取国际飞书配置
export function getLarkSuiteInfo() {
  return get(GetLarkSuiteInfoUrl);
}

// 保存企业微信登陆配置
export function saveWeComConfig(data) {
  return post(PostWeComSaveUrl, data);
}

// 保存钉钉登陆配置
export function saveDingTalkConfig(data) {
  return post(PostDingTalkSaveUrl, data);
}

// 保存飞书登陆配置
export function saveLarkConfig(data) {
  return post(PostLarkSaveUrl, data);
}

// 保存国际飞书登陆配置
export function saveLarkSuiteConfig(data) {
  return post(PostLarkSuiteSaveUrl, data);
}

// 校验企业微信外链接
export function validateWeComConfig(data) {
  return post(PostValidateWeComUrl, data);
}

// 校验钉钉外链接
export function validateDingTalkConfig(data) {
  return post(PostValidateDingTalkUrl, data);
}

// 校验飞书外链接
export function validateLarkConfig(data) {
  return post(PostValidateLarkUrl, data);
}

// 校验国际飞书外链接
export function validateLarkSuiteConfig(data) {
  return post(PostValidateLarkSuiteUrl, data);
}

// 开启企业微信登陆
export function enableWeCom(data) {
  return post(PostWeComEnableUrl, data);
}

// 开启钉钉登陆
export function enableDingTalk(data) {
  return post(PostDingTalkEnableUrl, data);
}

// 开启飞书登陆
export function enableLark(data) {
  return post(PostLarkEnableUrl, data);
}

// 开启国际飞书登陆
export function enableLarkSuite(data) {
  return post(PostLarkSuiteEnableUrl, data);
}

// 开启企业微信登陆
export function closeValidateWeCom() {
  return post(PostWeComValidateFalseUrl);
}

// 开启钉钉登陆
export function closeValidateDingTalk() {
  return post(PostDingTalkValidateFalseUrl);
}

// 开启飞书登陆
export function closeValidateLark() {
  return post(PostLarkValidateFalseUrl);
}

// 开启国际飞书登陆
export function closeValidateLarkSuite() {
  return post(PostLarkSuiteValidateFalseUrl);
}
