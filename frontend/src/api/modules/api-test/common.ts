import MSR from '@/api/http/index';
import { GetPluginOptionsUrl, GetPluginScriptUrl, GetProtocolListUrl } from '@/api/requrls/api-test/common';

import { GetPluginOptionsParams, PluginConfig, PluginOption, ProtocolItem } from '@/models/apiTest/common';

// 获取协议列表
export function getProtocolList(organizationId: string) {
  return MSR.get<ProtocolItem[]>({ url: GetProtocolListUrl, params: organizationId });
}

// 获取插件表单选项
export function getPluginOptions(data: GetPluginOptionsParams) {
  return MSR.get<PluginOption[]>({ url: GetPluginOptionsUrl, data });
}

// 获取插件配置
export function getPluginScript(pluginId: string) {
  return MSR.get<PluginConfig>({ url: GetPluginScriptUrl, params: pluginId });
}
