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
  children?: PluginItem[];
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
