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
}
