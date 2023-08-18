export type ServiceItem = Partial<{
  id?: string;
  pluginId: string; // 插件id
  title: string;
  description: string;
  enable: boolean;
  config: boolean;
  logo: string;
  organizationId: string; // 组织id
  configuration?: null; // 配置项
}>;

export type ServiceList = ServiceItem[];

// 创建和更新服务

export type AddOrUpdateServiceModel = Partial<{
  id?: string;
  pluginId: string;
  enable: boolean | undefined;
  organizationId: string;
  configuration?: any;
}>;

export interface SkipTitle {
  name: string;
  src: string;
  active: boolean; // 是否激活
}

export interface StepListType {
  id: string;
  icon: string;
  title: string;
  skipTitle: SkipTitle[];
  step: string;
  description: string;
}
