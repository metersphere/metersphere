export interface organizationItem {
  id?: string;
  num: number;
  name: string;
  description: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  updateUser: string;
  deleted: boolean;
  deleteUser: string;
  deleteTime: number;
  enable: boolean;
}
export type organizationList = organizationItem[];

export interface PluginForms {
  id: string;
  name: string;
}
export interface OrganizationsItem {
  id: string;
  num: number;
  name: string;
}
export type OrganizationsItemList = OrganizationsItem[];
// 插件管理列表项
export interface PluginItem {
  id?: string;
  name: string;
  pluginId: string;
  fileName: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  enable: boolean;
  global: boolean;
  xpack: boolean;
  description: string;
  scenario: string;
  organizations: OrganizationsItemList;
  pluginForms?: PluginForms[];
  expand?: any;
}
// 插件管理列表
export type PluginList = PluginItem[];
// 更新插件参数定义
export interface UpdatePluginModel {
  id?: string;
  name?: string;
  global?: boolean | string;
  description?: string;
  enable?: boolean;
  organizationIds?: Array<string | number>;
  [key: string]: any;
}

// 添加返回的接口
export interface AddReqData {
  id?: string;
  name: string;
  pluginId: string;
  fileName: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  enable: boolean;
  global: boolean;
  xpack: boolean;
  description: string;
  scenario: string;
}
export type Options = {
  title: string;
  visible: boolean;
  onClose?: () => void;
};
// 抽屉配置
export interface DrawerConfig {
  title: string;
  pluginId: string;
}
export interface PluginState {
  doNotShowAgain: boolean;
}
