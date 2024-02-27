import { key } from 'localforage';

export interface EnvListItem {
  name: string;
  id: string;
}

export interface EnvGroupProjectListItem {
  name: string;
  env: string;
  host: string;
  description: string;
}
export interface EnvGroupListItem {
  name: string;
  id: string;
  projectList: EnvGroupProjectListItem[];
}
export interface DataSourceItem {
  id?: string;
  name: string;
  driverId?: string;
  dbUrl: string;
  username: string;
  password?: string;
  poolMax?: number;
  timeout?: number;
  enable?: boolean;
}

export interface EnvConfigItem {
  [key: string]: any;
}
export interface EnvConfig {
  commonParams?: EnvConfigItem;
  commmonVariables?: EnvConfigItem[];
  httpConfig?: EnvConfigItem[];
  dataSource?: DataSourceItem[];
  hostConfig?: EnvConfigItem[];
  authConfig?: EnvConfigItem;
  preScript?: EnvConfigItem[];
  postScript?: EnvConfigItem[];
  assertions?: EnvConfigItem[];
}
export interface EnvDetailItem {
  id?: string;
  projectId: string;
  name: string;
  config: EnvConfig;
  mock?: boolean;
  description?: string;
}
export interface GlobalParamsItem {
  headers: EnvConfigItem[];
  commonVariables: EnvConfigItem[];
}
export interface GlobalParams {
  id?: string;
  projectId: string;
  globalParams: GlobalParamsItem;
}

export interface ContentTabItem {
  value: string;
  label: string;
  canHide: boolean;
  isShow: boolean;
}

export interface ContentTabsMap {
  tabList: ContentTabItem[];
  backupTabList: ContentTabItem[];
}

export interface ProjectOptionItem {
  id: string;
  name: string;
}

export interface EnvPluginScript {
  id: string;
  name: string;
  options: any;
  script: any[];
  scriptType: string;
  tabName: string;
}
export interface EnvPluginListItem {
  pluginId: string;
  script: EnvPluginScript;
}
