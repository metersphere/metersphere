export interface ServiceItem {
  id?: string;
  pluginId: string; // 插件id
  title: string;
  description: string;
  enable: boolean;
  config: boolean;
  logo: string;
  organizationId: string; // 组织id
  configuration?: null; // 配置项
}

export type ServiceList = ServiceItem[];

// 创建和更新服务

export interface AddOrUpdateServiceModel {
  id?: string;
  pluginId: string;
  enable: boolean;
  organizationId: string;
  configuration: any;
}
