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
  fields?: string[]; // 插件脚本内配置的全部字段集合
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
