import { FormItem } from '@/components/pure/ms-form-create/types';

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

// 上传文件
export type UploadFile = {
  fileList?: FormData[]; // 上传文件
  [key: string]: any; // 表单收集字段字符串
};

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
export type PluginItem = Partial<{
  id: string;
  name: string;
  pluginId: string;
  fileName: string; // 文件名称
  createTime: string;
  updateTime: string;
  createUser: string;
  enable: boolean;
  global: boolean;
  xpack: boolean; // 授权类型
  description: string;
  scenario: string; // 应用场景
  organizations: OrganizationsItemList; // 组织列表
  pluginForms: PluginForms[]; // 插件步骤
}>;
// 插件管理列表
export type PluginList = PluginItem[];
// 更新插件参数定义
export interface UpdatePluginModel {
  id?: string;
  name?: string;
  global?: boolean | string; // 是否选择全部组织
  description?: string;
  enable?: boolean;
  organizationIds?: string[]; // 指定组织
  [key: string]: any;
}

// 添加返回的接口
export interface AddReqData {
  id?: string;
  name: string;
  pluginId: string; // 插件id
  fileName: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  enable: boolean;
  global: boolean;
  xpack: boolean; // 授权类型
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
  pluginId: string; // 插件id
}
export interface DrawerReqParams {
  pluginId: string; // 插件id
  scriptId: string; // 脚本id
}
export interface PluginState {
  doNotShowAgain: boolean;
}

// 定义统一插件下拉选项请求参数
export interface OptionsParams {
  pluginId: string;
  organizationId: string;
  optionMethod: string;
  projectConfig: string;
}
