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

export interface PluginItem {
  id: string;
  name: string;
  describe: string;
  enable: boolean;
  createTime: number;
  updateTime: number;
  jarPackage: string;
  version: string;
  applicationScene: string;
  createUser: string;
  updateUser: string;
  organizationList: organizationList;
  steps?: string[];
}
export type PluginList = PluginItem[];

export interface StepItem {
  name: string;
  title: string;
  status: boolean;
}
export type StepList = StepItem[];

export interface SceneItem {
  name: string;
  description: string;
  isSelected: boolean;
  svg: string;
}
export type SceneList = SceneItem[];

export type PluginForm = {
  pluginName: string;
  organize: string | number;
  describe: string;
  organizeGroup: Array<string | number>;
};
