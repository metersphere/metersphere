import {
  RequestCaseStatus,
  RequestDefinitionStatus,
  RequestImportFormat,
  RequestImportType,
  RequestMethods,
} from '@/enums/apiEnum';

import { BatchApiParams, ModuleTreeNode, TableQueryParams } from '../common';
import { ExecuteRequestParams, ResponseDefinition } from './common';
import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

// 定义-自定义字段
export interface ApiDefinitionCustomField {
  apiId: string;
  fieldId: string;
  value: string;
}

// 创建定义参数
export interface ApiDefinitionCreateParams extends ExecuteRequestParams {
  tags: string[];
  response: ResponseDefinition[];
  description: string;
  status: RequestDefinitionStatus;
  customFields: ApiDefinitionCustomField[];
  moduleId: string;
  versionId: string;
  [key: string]: any; // 其他前端定义的参数
}

// 更新定义参数
export interface ApiDefinitionUpdateParams extends Partial<ApiDefinitionCreateParams> {
  id: string;
  deleteFileIds?: string[];
  unLinkFileIds?: string[];
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
  method: RequestMethods | string;
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
  protocols: string[];
  projectId: string;
  versionId?: string;
  refId?: string;
  shareId?: string;
  orgId?: string; // 组织id
}

// 环境-选中的模块
export interface SelectedModule {
  // 选中的模块
  moduleId: string;
  containChildModule: boolean; // 是否包含新增子模块
  disabled?: boolean;
}

// 定义-获取环境的模块树参数
export interface ApiDefinitionGetEnvModuleParams {
  projectId: string;
  selectedModules?: SelectedModule[];
}

// 环境-模块树
export interface EnvModule {
  moduleTree: ModuleTreeNode[];
  selectedModules: SelectedModule[];
}

// 环境列表
export interface Environment {
  id: string;
  name: string;
  projectId: string;
}

// 定义列表查询参数
export interface ApiDefinitionPageParams extends TableQueryParams {
  id: string;
  name: string;
  protocols: string[];
  projectId: string;
  versionId: string;
  refId: string;
  moduleIds: string[];
  deleted: boolean;
}

// mock列表请求参数
export interface ApiDefinitionMockPageParams extends TableQueryParams {
  id: string;
  name: string;
  projectId: string;
  enable: boolean;
  apiDefinitionId: string;
}

export interface ApiDefinitionMockDetail {
  id: string;
  name: string;
  tags: string[];
  enable: boolean;
  expectNum: string;
  projectId: string;
  apiDefinitionId: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  matching: string;
  response: string;
  createUserName: string;
  apiNum: string;
  apiName: string;
  apiPath: string;
  apiMethod: string;
}

export interface mockParams {
  id: string;
  projectId: string;
}
// 批量操作参数
export interface ApiDefinitionBatchParams extends BatchApiParams {
  protocols: string[];
}
// 批量导出定义参数
export interface ApiDefinitionBatchExportParams extends ApiDefinitionBatchParams {
  exportApiCase: boolean;
  exportApiMock: boolean;
  fileId: string;
  sort: Record<string, any>;
}
// 批量更新定义参数
export interface ApiDefinitionBatchUpdateParams extends ApiDefinitionBatchParams {
  type?: string;
  append?: boolean;
  method?: RequestMethods;
  status?: RequestDefinitionStatus;
  versionId?: string;
  tags?: string[];
  customField?: Record<string, any>;
  clear?: boolean; // 是否清空标签
}
// 批量移动定义参数
export interface ApiDefinitionBatchMoveParams extends ApiDefinitionBatchParams {
  moduleId: string | number;
}
// 批量删除定义参数
export interface ApiDefinitionBatchDeleteParams extends ApiDefinitionBatchParams {
  deleteAll: boolean;
}
// 定义-定时同步-更新参数
export interface UpdateScheduleParams {
  id: string;
  taskId: string;
}
// 定义-定时同步-检查 url 是否存在参数
export interface CheckScheduleParams {
  projectId: string;
  swaggerUrl: string;
}
// 导入定义-request参数
export interface ImportApiDefinitionRequest {
  userId: string;
  versionId?: string;
  updateVersionId?: string;
  defaultVersion?: boolean;
  platform: RequestImportFormat;
  type: RequestImportType;
  coverModule: boolean; // 是否覆盖子目录
  coverData: boolean; // 是否覆盖数据
  syncCase: boolean; // 是否同步导入用例
  syncMock: boolean; // 是否同步导入mock
  protocol: string;
  authSwitch?: boolean;
  authUsername?: string;
  authPassword?: string;
  uniquelyIdentifies?: string;
  resourceId?: string;
  swaggerUrl?: string;
  swaggerToken?: string;
  moduleId: string;
  projectId: string;
  name?: string;
}
// 导入定义参数
export interface ImportApiDefinitionParams {
  file: File | null;
  request: ImportApiDefinitionRequest;
}
// 导入定义-创建定时同步参数
export interface CreateImportApiDefinitionScheduleParams extends ImportApiDefinitionRequest {
  value: string; // cron 表达式
  config?: string;
}
// 定义-变更历史列表项
export interface DefinitionHistoryItem {
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
// 变更历史列表查询参数
export interface DefinitionHistoryPageParams extends TableQueryParams {
  projectId: string;
  sourceId: string;
  createUser: string;
  types: string[];
  modules: string[];
}
// 定义-恢复历史版本参数
export interface RecoverDefinitionParams {
  id: string | number;
  sourceId: string | number;
  versionId?: string;
}

// 引用关系列表查询参数
export interface DefinitionReferencePageParams extends TableQueryParams {
  resourceId: string;
}

// 定义-复制文件参数
export interface DefinitionFileCopyParams {
  resourceId: string;
  fileIds: string[];
}

// 回收站-恢复接口定义参数
export interface ApiDefinitionDeleteParams {
  id: string;
  projectId: string;
  deleteAll?: boolean;
}

// 回收站-批量恢复接口定义参数
export interface BatchRecoverApiParams extends ApiDefinitionBatchParams {
  projectId: string;
  moduleIds?: string[];
}

// --------------------用例
// 用例列表查询参数
export interface ApiCasePageParams extends TableQueryParams {
  protocols: string[];
  projectId: string;
  versionId?: string;
  refId?: string;
  moduleIds?: string[];
  apiDefinitionId?: string;
}
// 用例列表和用例详情
export interface ApiCaseDetail extends ExecuteRequestParams {
  id: string;
  name: string;
  priority: string;
  num: number;
  status: RequestCaseStatus;
  protocol: string;
  lastReportStatus: string;
  lastReportId: string;
  projectId: string;
  apiDefinitionId: string;
  environmentId: string;
  environmentName: string;
  follow: boolean;
  method: RequestMethods | string;
  path: string;
  tags: string[];
  passRate: string;
  modulePath: string;
  moduleId: string;
  createTime: number;
  createUser: string;
  createName: string;
  updateTime: number;
  updateUser: string;
  updateName: string;
  deleteTime: number;
  deleteUser: string;
  deleteName: string;
  apiChange: boolean; // 接口定义参数变更标识
  inconsistentWithApi: boolean; // 与接口定义不一致
  aiCreate: boolean; // 是否AI创建
}
// 批量操作参数
export interface ApiCaseBatchParams extends BatchApiParams {
  protocols: string[];
  apiDefinitionId?: string;
  versionId?: string;
}
// 用例批量编辑参数
export interface ApiCaseBatchEditParams extends ApiCaseBatchParams {
  priority?: string;
  tags?: string[];
  status?: RequestCaseStatus;
  environmentId?: string;
  type: string;
  append?: boolean;
  clear?: boolean;
}
// 添加用例参数
export interface AddApiCaseParams extends ExecuteRequestParams {
  name: string;
  priority: string;
  status: RequestCaseStatus;
  tags: string[];
  deleteFileIds?: string[];
  unLinkFileIds?: string[];
}

export interface ApiRunModeRequest {
  runMode: string;
  integratedReport: boolean;
  integratedReportName: string;
  stopOnFailure: boolean;
  poolId: string;
  grouped: boolean;
  environmentId: string;
}

// 接口用例批量执行参数
export interface ApiCaseBatchExecuteParams extends BatchApiParams {
  apiDefinitionId?: string | number;
  protocols: string[];
  versionId?: string;
  refId?: string;
  runModeConfig: ApiRunModeRequest;
}

export interface ApiCaseExecuteHistoryParams extends TableQueryParams {
  id: string;
}

export interface ApiCaseChangeHistoryParams extends TableQueryParams {
  resourceId: string;
  projectId: string;
  createUser?: string;
  types: string[];
  modules: string[];
}

export interface ApiCaseDependencyParams extends TableQueryParams {
  resourceId: string;
}
export interface syncItem {
  header: boolean;
  body: boolean;
  query: boolean;
  rest: boolean;
}
// 批量同步
export interface batchSyncForm {
  notificationConfig: {
    apiCaseCreator: boolean;
    scenarioCreator: boolean;
  };
  // 同步项目
  syncItems: syncItem;
  deleteRedundantParam: boolean;
}
// 对比同步参数
export interface diffSyncParams {
  syncItems: syncItem; // 同步项
  id: string;
  deleteRedundantParam: boolean; // 是否删除多余参数
  apiCaseRequest: RequestParam; // 用例详情请求request
}

export type ApiRangeType = 'ALL' | 'MODULE' | 'PATH' | 'TAG';

// 接口定义-接口文档-分享
export interface ShareDetail {
  id?: string;
  name: string;
  apiRange: ApiRangeType; // 接口范围;全部接口(ALL)、模块(MODULE)、路径(PATH)、标签(TAG)
  rangeMatchSymbol: string; // 范围匹配符
  rangeMatchVal: string; // 范围匹配值;eg: 选中路径范围时, 该值作为路径匹配
  isPrivate: boolean; // 是否公开
  password: string; // 访问密码
  allowExport: boolean; // 允许导出
  projectId: string;
  invalidTime?: number; // 失效时间值
}
// 分享列表
export interface shareItem extends ShareDetail {
  createTime: number;
  createUser: string;
  invalid: boolean;
  apiShareNum: number;
  deadline: number;
}
export interface CheckSharePsdType {
  docShareId: string;
  password: string;
}
// 分享详情
export interface ShareDetailType {
  invalid: boolean;
  allowExport: boolean;
  isPrivate: boolean;
  projectName?: string;
}
// 接口用例统计项
export interface ApiCaseStatisticsItem {
  id: string;
  passRate: string;
}
