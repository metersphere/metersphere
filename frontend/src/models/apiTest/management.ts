import { ModuleTreeNode, TableQueryParams } from '../common';
import { ExecuteRequestParams, ResponseDefinition } from './common';

// 定义-自定义字段
export interface ApiDefinitionCustomField {
  apiId: string;
  fieldId: string;
  value: string;
}
// 创建定义参数
export interface ApiDefinitionCreateParams extends ExecuteRequestParams {
  tags: string[];
  response: ResponseDefinition;
  description: string;
  status: string;
  customFields: ApiDefinitionCustomField[];
  moduleId: string;
  versionId: string;
  [key: string]: any; // 其他前端定义的参数
}
// 更新定义参数
export interface ApiDefinitionUpdateParams extends ApiDefinitionCreateParams {
  id: string;
  deleteFileIds: string[];
  unLinkFileIds: string[];
}
// 定义-自定义字段详情
export interface ApiDefinitionCustomFieldDetail {
  id: string;
  name: string;
  scene: string;
  type: string;
  remark: string;
  internal: boolean;
  scopeType: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  refId: string;
  enableOptionKey: boolean;
  scopeId: string;
  value: string;
  apiId: string;
  fieldId: string;
}
// 定义详情
export interface ApiDefinitionDetail extends ApiDefinitionCreateParams {
  id: string;
  name: string;
  protocol: string;
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
  customFields: ApiDefinitionCustomFieldDetail[];
}
// 定义-更新模块参数
export interface ApiDefinitionUpdateModuleParams {
  id: string;
  name: string;
}
// 定义-获取模块树参数
export interface ApiDefinitionGetModuleParams {
  keyword: string;
  searchMode?: 'AND' | 'OR';
  filter?: Record<string, any>;
  combine?: Record<string, any>;
  moduleIds: string[];
  protocol: string;
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
// 定义-获取环境的模块树参数
export interface ApiDefinitionGetEnvModuleParams {
  projectId: string;
  selectedModules: SelectedModule[];
}
// 环境-模块树
export interface EnvModule {
  moduleTree: ModuleTreeNode[];
  selectedModules: SelectedModule[];
}
// 定义列表查询参数
export interface ApiDefinitionPageParams extends TableQueryParams {
  id: string;
  name: string;
  protocol: string;
  projectId: string;
  versionId: string;
  refId: string;
  moduleIds: string[];
  deleted: boolean;
}
