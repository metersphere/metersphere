import MSR from '@/api/http/index';
import {
  GetEnvironmentUrl,
  GetEnvListUrl,
  GetPluginOptionsUrl,
  GetPluginScriptUrl,
  GetProtocolListUrl,
  ImportCurlUrl,
  LocalExecuteApiDebugUrl,
  StopExecuteUrl,
  StopLocalExecuteUrl,
} from '@/api/requrls/api-test/common';

import {
  type CurlParseResult,
  ExecuteRequestParams,
  GetPluginOptionsParams,
  PluginConfig,
  PluginOption,
  ProtocolItem,
} from '@/models/apiTest/common';
import { EnvConfig, EnvironmentItem } from '@/models/projectManagement/environmental';
import { ScenarioStepType } from '@/enums/apiEnum';

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

// 本地执行调试
export function localExecuteApiDebug(host: string, data: ExecuteRequestParams) {
  return MSR.post<ExecuteRequestParams>({ url: `${host}${LocalExecuteApiDebugUrl}`, data });
}

// 获取环境列表
export function getEnvList(projectId: string) {
  return MSR.get<EnvironmentItem[]>({ url: GetEnvListUrl, params: projectId });
}

// 获取环境详情
export function getEnvironment(envId: string) {
  return MSR.get<EnvConfig>({ url: GetEnvironmentUrl, params: envId });
}

// 停止本地执行
export function stopLocalExecute(host: string, id: string | number, type?: ScenarioStepType) {
  return MSR.post({
    url: type ? `${host}${StopLocalExecuteUrl}/${type}/${id}` : `${host}${StopLocalExecuteUrl}/${id}`,
  });
}

// 停止执行
export function stopExecute(id: string | number, type?: ScenarioStepType) {
  return MSR.get({ url: type ? `${StopExecuteUrl}/${type}/${id}` : `${StopExecuteUrl}/${id}` });
}

// 导入curl
export function importByCurl(curl: string) {
  return MSR.post<CurlParseResult>({ url: ImportCurlUrl, data: { curl } });
}
