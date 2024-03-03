import { ResponseBodyFormat } from '@/enums/apiEnum';

import { EnableKeyValueParam, ExecuteBinaryBody, ExecuteJsonBody, ExecuteValueBody } from './debug';

// 获取插件表单选项参数
export interface GetPluginOptionsParams {
  orgId: string;
  pluginId: string;
  optionMethod: string;
  queryParam: Record<string, any>;
}
// 插件表单选项子项
export interface PluginOption {
  text: string;
  value: string;
}
// 协议列表子项
export interface ProtocolItem {
  protocol: string;
  polymorphicName: string;
  pluginId: string;
}
// 插件配置
export interface PluginConfig {
  id: string;
  name: string;
  options: Record<string, any>;
  script: Record<string, any>[];
  scriptType: string;
  apiDebugFields?: string[]; // 接口调试脚本内配置的全部字段集合
  apiDefinitionFields?: string[]; // 接口定义脚本内配置的全部字段集合
}
// 响应结果
export interface ResponseResult {
  requestResults: {
    body: string;
    headers: string;
    responseResult: {
      body: string;
      contentType: string;
      headers: string;
      dnsLookupTime: number;
      downloadTime: number;
      latency: number;
      responseCode: number;
      responseTime: number;
      responseSize: number;
      socketInitTime: number;
      sslHandshakeTime: number;
      tcpHandshakeTime: number;
      transferStartTime: number;
    };
  }[]; // 请求结果
  console: string;
}
// 响应定义-body
export interface ResponseDefinitionBody {
  bodyType: ResponseBodyFormat;
  jsonBody: ExecuteJsonBody;
  xmlBody: ExecuteValueBody;
  rawBody: ExecuteValueBody;
  binaryBody: ExecuteBinaryBody;
}
export interface ResponseDefinitionHeader extends EnableKeyValueParam {
  notBlankValue: boolean;
  valid: boolean;
}

// 响应定义
export interface ResponseDefinition {
  id: string | number;
  statusCode: string | number;
  defaultFlag: boolean; // 默认响应标志
  name: string; // 响应名称
  headers: ResponseDefinitionHeader[];
  body: ResponseDefinitionBody;
}
// 接口定义-JsonSchema
export interface JsonSchema {
  example: Record<string, any>;
  id: string;
  title: string;
  type: string;
  description: string;
  items: string;
  mock: Record<string, any>;
  properties: Record<string, any>;
  additionalProperties: string;
  required: string[];
  pattern: string;
  maxLength: number;
  minLength: number;
  minimum: number;
  maximum: number;
  schema: string;
  format: string;
  enumString: string[];
  enumInteger: number[];
  enumNumber: number[];
  extensions: Record<string, any>;
}
