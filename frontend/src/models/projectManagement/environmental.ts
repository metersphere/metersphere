import { ExecuteConditionProcessor } from '../apiTest/common';

export interface EnvListItem {
  mock?: boolean;
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
  id: string;
  dataSource: string; // 数据源名称
  driverId: string; // 驱动id
  dbUrl: string; // 数据库连接url
  username: string; // 用户名
  password: string; // 密码
  poolMax?: number; // 最大连接数
  timeout?: number; // 超时时间
}

export interface EnvConfigItem {
  [key: string]: any;
}
export interface ProcessorConfig {
  apiProcessorConfig: {
    scenarioProcessorConfig: {
      processors: ExecuteConditionProcessor[];
    };
    requestProcessorConfig: {
      processors: [];
    };
  };
}
export interface AssertionConfig {
  assertions: EnvConfigItem[];
}

export interface CommonParams {
  requestTimeout: number;
  responseTimeout: number;
  [key: string]: any;
}
export interface EnvConfig {
  commonParams?: CommonParams;
  commonVariables: EnvConfigItem[];
  httpConfig: EnvConfigItem[];
  dataSources: DataSourceItem[];
  hostConfig: EnvConfigItem;
  // TODO  数据参数有问题
  preProcessorConfig: ProcessorConfig;
  postProcessorConfig: ProcessorConfig;
  assertionConfig: AssertionConfig;
  pluginConfigMap: EnvConfigItem;
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
  fields: string[];
}
export interface EnvPluginListItem {
  pluginId: string;
  script: EnvPluginScript;
}

export interface EnvGroupProjectItem {
  projectId: string;
  environmentId: string;
}

export interface GroupItem {
  id: string;
  name: string;
  description: string;
  projectId: string;
  envGroupProject: EnvGroupProjectItem[];
  environmentGroupInfo?: EnvGroupProjectItem[];
}

export interface DragParam {
  projectId: string;
  targetId: string;
  moveMode: string;
  moveId: string;
}

export interface HttpForm {
  id?: string;
  protocol: string;
  description?: string;
  hostname: string;
  type: string;
  headers: Record<string, any>[];
  // pathMatchRule: {
  path: string;
  condition: string;
  moduleId: string[];
  moduleMatchRule: {
    modules: {
      moduleId: string;
      containChildModule: boolean;
    }[];
  };
  url: string;
  pathMatchRule: {
    path: '';
    condition: '';
  };
}
// 环境列表项
export interface EnvironmentItem {
  id: string;
  name: string;
  projectId: string;
  createUser: string;
  updateUser: string;
  createTime: number;
  updateTime: number;
  mock: boolean;
  description: string;
  pos: number;
}
