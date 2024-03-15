import { ApiDefinitionCustomField } from '@/models/apiTest/management';
import { ApiScenarioStatus, RequestDefinitionStatus, RequestImportFormat, RequestImportType } from '@/enums/apiEnum';

import { BatchApiParams, TableQueryParams } from '../common';
import { ResponseDefinition } from './common';

// 场景-更新模块参数
export interface ApiScenarioModuleUpdateParams {
  id: string;
  name: string;
}

// 场景-获取模块树参数
export interface ApiScenarioGetModuleParams {
  keyword?: string;
  searchMode?: 'AND' | 'OR';
  filter?: Record<string, any>;
  combine?: Record<string, any>;
  moduleIds?: string[];
  projectId: string;
  versionId?: string;
  refId?: string;
}

// 场景修改参数
export interface ApiScenarioUpdateDTO {
  id: string;
  name?: string;
  priority?: string;
  status?: ApiScenarioStatus;
  moduleId?: string;
  description?: string;
  tags?: string[];
  grouped?: boolean;
  environmentId?: string;
  uploadFileIds?: string[];
  linkFileIds?: string[];
  deleteFileIds?: string[];
  unLinkFileIds?: string[];
}

// 场景详情
export interface ApiScenarioDetail {
  id: string;
  name: string;
  method: string;
  path: string;
  num: number;
  pos: number;
  projectId: string;
  moduleId: string;
  latest: boolean;
  versionId: string;
  refId: string;
  createTime: number;
  createUser: string;
  updateTime: number;
  updateUser: string;
  deleteUser: string;
  deleteTime: number;
  deleted: boolean;
  createUserName: string;
  updateUserName: string;
  deleteUserName: string;
  versionName: string;
  caseTotal: number;
  casePassRate: string;
  caseStatus: string;
  follow: boolean;
  tags: string[];
  response: ResponseDefinition;
  description: string;
  status: RequestDefinitionStatus;
  customFields: ApiDefinitionCustomField[];
}

// 场景列表查询参数
export interface ApiScenarioPageParams extends TableQueryParams {
  id: string;
  name: string;
  projectId: string;
  versionId: string;
  refId: string;
  moduleIds: string[];
  deleted: boolean;
}

// 批量更新场景参数
export interface ApiScenarioBatchUpdateParams extends BatchApiParams {
  type?: string;
  append?: boolean;
  method?: string;
  versionId?: string;
  tags?: string[];
  customField?: Record<string, any>;
}

export interface BatchOptionParams extends BatchApiParams {
  apiScenarioId?: string;
  versionId?: string;
  refId?: string;
}

// 批量移动场景参数
export interface ApiScenarioBatchMoveParams extends BatchOptionParams {
  targetModuleId: string | number;
}

// 批量编辑场景参数
export interface ApiScenarioBatchEditParams extends BatchOptionParams {
  // 修改操作的类型
  type?: string;

  // 修改标签相关
  appendTag?: boolean;
  tags?: string[];

  // 修改环境相关
  grouped?: boolean;
  envId?: string;
  groupId?: string;

  // 修改状态
  status?: string;

  // 修改优先级
  priority?: string;
}

// 批量删除场景参数
export interface ApiScenarioBatchDeleteParams extends BatchApiParams {
  deleteAll: boolean;
}

// 场景-执行历史-请求参数
export interface ExecutePageParams extends TableQueryParams {
  id: string;
}

// 场景-执行历史-请求参数
export interface ExecuteHistoryItem {
  id: string;
  num: string;
  name: string;
  operationUser: string;
  createUser: string;
  startTime: number;
  status: string;
  triggerMode: string;
}

// 场景-变更历史列表查询参数
export interface ScenarioHistoryPageParams extends TableQueryParams {
  projectId: string;
  sourceId: string;
  createUser: string;
  types: string[];
  modules: string[];
}

// 场景-变更历史列表项
export interface ScenarioHistoryItem {
  id: number;
  projectId: string;
  createTime: number;
  createUser: string;
  sourceId: string;
  type: string;
  module: string;
  refId: number;
  createUserName: string;
  versionName: string;
}
