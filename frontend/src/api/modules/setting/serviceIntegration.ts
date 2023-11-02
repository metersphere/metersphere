import MSR from '@/api/http/index';
import {
  AddServiceUrl,
  ConfigServiceScriptUrl,
  getLogoUrl,
  GetServiceListUrl,
  GetValidateServiceUrl,
  PostValidateServiceUrl,
  ResetServiceUrl,
  UpdateServiceUrl,
} from '@/api/requrls/setting/serviceIntegration';

import type { AddOrUpdateServiceModel, ServiceList } from '@/models/setting/serviceIntegration';
// 获取集成列表
export function getServiceList(organizationId: string) {
  return MSR.get<ServiceList>({ url: GetServiceListUrl, params: organizationId });
}
// 创建或更新服务集成
export function addOrUpdate(data: AddOrUpdateServiceModel, type: string) {
  if (type === 'create') {
    return MSR.post({ url: AddServiceUrl, data });
  }
  return MSR.post({ url: UpdateServiceUrl, data });
}
// 重置到未配置
export function resetService(id: string) {
  return MSR.get({ url: ResetServiceUrl, params: id });
}
// 外部校验测试连接
export function getValidate(id: string) {
  return MSR.get({ url: GetValidateServiceUrl, params: id });
}
// 内部校验测试连接 注:不同的平台对应的不同的字段
export function postValidate(data: any, pluginId: string) {
  return MSR.post({ url: PostValidateServiceUrl, data, params: pluginId });
}
// 前端配置脚本
export function configScript(pluginId: string) {
  return MSR.get({ url: ConfigServiceScriptUrl, params: pluginId });
}
export function getLogo(pluginId: string, params: { imagePath: string }) {
  return MSR.get({ url: `${getLogoUrl}${pluginId}`, params, responseType: 'blob' }, { isReturnNativeResponse: true });
}
