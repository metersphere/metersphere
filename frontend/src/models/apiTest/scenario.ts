import { RequestDefinitionStatus, RequestImportFormat, RequestImportType } from '@/enums/apiEnum';

import { BatchApiParams, ModuleTreeNode, TableQueryParams } from '../common';
import { ExecuteRequestParams, ResponseDefinition } from './common';


// 场景-更新模块参数
export interface ApiScenarioModuleUpdateParams {
  id: string;
  name: string;
}

// 场景-获取模块树参数
export interface ApiScenarioGetModuleParams {
  keyword: string;
  searchMode?: 'AND' | 'OR';
  filter?: Record<string, any>;
  combine?: Record<string, any>;
  moduleIds: string[];
  projectId: string;
  versionId?: string;
  refId?: string;
}

// 环境-选中的模块
export interface SelectedModule {
  // 选中的模块
  moduleId: string;
  containChildModule: boolean; // 是否包含新增子模块
  disabled: boolean;
}

// 环境-模块树
export interface EnvModule {
  moduleTree: ModuleTreeNode[];
  selectedModules: SelectedModule[];
}

// 定义列表查询参数
export interface ApiScenarioPageParams extends TableQueryParams {
  id: string;
  name: string;
  protocol: string;
  projectId: string;
  versionId: string;
  refId: string;
  moduleIds: string[];
  deleted: boolean;
}


export interface mockParams {
  id: string;
  projectId: string;
}

// 批量更新定义参数
export interface ApiScenarioBatchUpdateParams extends BatchApiParams {
  type?: string;
  append?: boolean;
  method?: string;
  versionId?: string;
  tags?: string[];
  customField?: Record<string, any>;
}

// 批量移动定义参数
export interface ApiScenarioBatchMoveParams extends BatchApiParams {
  moduleId: string | number;
}

// 批量删除定义参数
export interface ApiScenarioBatchDeleteParams extends BatchApiParams {
  deleteAll: boolean;
}

// 场景-定时同步-更新参数
export interface UpdateScheduleParams {
  id: string;
  taskId: string;
}
