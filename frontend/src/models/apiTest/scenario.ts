import type { CaseLevel } from '@/components/business/ms-case-associate/types';

import { ApiDefinitionCustomField, ApiRunModeRequest } from '@/models/apiTest/management';
import {
  ApiScenarioStatus,
  RequestAssertionCondition,
  RequestComposition,
  RequestDefinitionStatus,
  RequestMethods,
  ScenarioExecuteStatus,
  ScenarioFailureStrategy,
  ScenarioStepLoopTypeEnum,
  ScenarioStepPolymorphicName,
  ScenarioStepRefType,
  ScenarioStepType,
  WhileConditionType,
} from '@/enums/apiEnum';

import { BatchApiParams, TableQueryParams } from '../common';
import {
  ExecuteApiRequestFullParams,
  ExecuteAssertionItem,
  ExecuteConditionConfig,
  RequestResult,
  ResponseDefinition,
} from './common';

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
  id: string | number;
  name?: string;
  priority?: string;
  status?: ApiScenarioStatus;
  moduleId?: string | number;
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
export interface ApiScenarioTableItem {
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

// 场景批量编辑参数
export interface ApiScenarioBatchParams extends BatchApiParams {
  projectId?: string;
  moduleIds?: string[];
  apiScenarioId?: string;
  versionId?: string;
  refId?: string;
}

// 批量移动场景参数
export interface ApiScenarioBatchMoveParams extends ApiScenarioBatchParams {
  targetModuleId: string | number;
}

// 批量编辑场景参数
export interface ApiScenarioBatchEditParams extends ApiScenarioBatchParams {
  // 修改操作的类型
  type?: string;

  // 修改标签相关
  append?: boolean;
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

// 批量编辑场景参数
export interface ApiScenarioBatchRunParams extends ApiScenarioBatchParams {
  // 运行模式配置
  runModeConfig?: ApiRunModeRequest;
}

// 批量删除场景参数
export interface ApiScenarioBatchDeleteParams extends ApiScenarioBatchParams {
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

export type CustomApiStep = ExecuteApiRequestFullParams & {
  protocol: string;
  activeTab: RequestComposition;
  useEnv: string;
};
// 场景步骤-循环控制器类型
export type ScenarioStepLoopType = ScenarioStepLoopTypeEnum;
// 场景步骤-步骤插入类型
export type CreateStepAction = 'inside' | 'before' | 'after';
export interface OtherConfig {
  enableGlobalCookie: boolean;
  enableCookieShare: boolean;
  stepWaitTime: number;
  enableStepWait: boolean;
  failureStrategy: ScenarioFailureStrategy;
}
export interface AssertionConfig {
  assertions: ExecuteAssertionItem[];
}
export interface CsvVariable {
  id: string;
  fileId: string;
  scenarioId: string;
  name: string;
  fileName: string;
  scope: string;
  enable: boolean;
  association: boolean;
  encoding: string;
  random: boolean;
  variableNames: string;
  ignoreFirstLine: boolean;
  delimiter: string;
  allowQuotedData: boolean;
  recycleOnEof: boolean;
  stopThreadOnEof: boolean;
}
export interface CommonVariable {
  id: string | number;
  key: string;
  paramType: string;
  value: string;
  enable: boolean;
  description: string;
  tags: string[];
}
export interface Variable {
  commonVariables: CommonVariable[];
  csvVariables: CsvVariable[];
}
export interface ScenarioConfig {
  variable: Variable;
  preProcessorConfig: ExecuteConditionConfig;
  postProcessorConfig: ExecuteConditionConfig;
  assertionConfig: AssertionConfig;
  otherConfig: OtherConfig;
}
export interface ForEachController {
  loopTime: number; // 循环间隔时间
  value: string; // 变量值
  variable: string; // 变量名
}
export interface CountController {
  loops: number; // 循环次数
}
export interface WhileScript {
  scriptValue: string; // 脚本值
}
export interface WhileVariable {
  condition: RequestAssertionCondition; // 条件操作符
  value: string; // 变量值
  variable: string; // 变量名
}
export interface WhileController {
  conditionType: WhileConditionType; // 条件类型
  timeout: number; // 超时时间
  msWhileScript: WhileScript; // 脚本
  msWhileVariable: WhileVariable; // 变量
}
export type ExtendedScenarioStepPolymorphicName = ScenarioStepPolymorphicName | string;
// 场景步骤详情公共部分
export interface StepDetailsCommon {
  id: string | number;
  copyFromStepId?: string; // 如果步骤是复制的，这个字段是复制的步骤id
  name: string;
  enable: boolean;
  polymorphicName: ExtendedScenarioStepPolymorphicName; // 多态名称，用于后台区分使用的是哪个组件
}
// 自定义请求
export interface CustomApiStepDetail extends StepDetailsCommon {
  customizeRequest: boolean; // 是否自定义请求
  customizeRequestEnvEnable: boolean; // 是否启用环境
}
// 条件控制器
export interface ConditionStepDetail extends StepDetailsCommon {
  value: string; // 变量值
  variable: string; // 变量名
  condition: RequestAssertionCondition; // 条件操作符
}
// 循环控制器
export interface LoopStepDetail extends StepDetailsCommon {
  loopType: ScenarioStepLoopType;
  forEachController: ForEachController;
  msCountController: CountController;
  whileController: WhileController;
}
export type ScenarioStepDetail = Partial<
  CustomApiStepDetail & ConditionStepDetail & LoopStepDetail & { protocol: string; method: RequestMethods }
>;
export interface ScenarioStepItem {
  id: string | number;
  sort: number;
  name: string;
  enable: boolean; // 是否启用
  copyFromStepId?: string; // 如果步骤是复制的，这个字段是复制的步骤id；如果复制的步骤也是复制的，并且没有加载过详情，则这个 id 是最原始的 被复制的步骤 id
  resourceId?: string; // 详情或者引用的类型才有
  resourceNum?: string; // 详情或者引用的类型才有
  stepType: ScenarioStepType;
  refType: ScenarioStepRefType;
  config: ScenarioStepDetail; // 存储步骤列表需要展示的信息
  csvFileIds?: string[];
  projectId?: string;
  versionId?: string;
  children?: ScenarioStepItem[];
  isNew: boolean; // 是否新建的步骤，引用复制类型以此区分调用步骤详情还是资源详情
  // 页面渲染以及交互需要字段
  checked?: boolean; // 是否选中
  expanded?: boolean; // 是否展开
  createActionsVisible?: boolean; // 是否展示创建步骤下拉
  responsePopoverVisible?: boolean; // 是否展示步骤响应 popover
  parent?: ScenarioStepItem; // 父级节点，第一层的父级节点为undefined
  resourceName?: string; // 引用复制接口、用例、场景时的源资源名称
  method?: RequestMethods;
  executeStatus?: ScenarioExecuteStatus;
  isExecuting?: boolean; // 是否正在执行
  reportId?: string | number; // 步骤单个调试时的报告id
}
// 场景
export interface Scenario {
  id?: string | number;
  num?: number;
  name: string;
  moduleId: string | number;
  priority: CaseLevel;
  status: ApiScenarioStatus;
  tags: string[];
  projectId: string;
  description: string;
  grouped?: boolean;
  environmentId?: string;
  scenarioConfig: ScenarioConfig;
  steps: ScenarioStepItem[];
  stepDetails: Record<string, ScenarioStepDetail>;
  follow?: boolean;
  uploadFileIds: string[];
  linkFileIds: string[];
  // 前端渲染字段
  label: string;
  closable: boolean;
  isNew: boolean;
  unSaved: boolean;
  executeLoading: boolean; // 执行loading
  executeTime?: string | number; // 执行时间
  executeSuccessCount: number; // 执行成功数量
  executeFailCount: number; // 执行失败数量
  reportId?: string | number; // 场景报告 id
  stepResponses: Record<string | number, RequestResult>; // 步骤响应集合，key 为步骤 id，value 为步骤响应内容
  isExecute?: boolean; // 是否从列表执行进去场景详情
  isDebug?: boolean; // 是否调试，区分执行场景和批量调试步骤
}
export interface ScenarioDetail extends Scenario {
  stepTotal: number;
  requestPassRate: string;
  lastReportStatus?: string;
  lastReportId?: string;
  deleted: boolean;
  versionId: string;
  refId: string;
  latest: boolean;
  modulePath: string;
  createUser: string;
  createTime: number;
  updateTime: number;
  updateUser: string;
}

export interface ApiScenarioDebugRequest {
  id: string | number; // 场景 id
  grouped: boolean;
  environmentId: string;
  scenarioConfig: ScenarioConfig;
  stepDetails: Record<string, ScenarioStepDetail>;
  reportId?: string | number;
  steps: ScenarioStepItem[];
  projectId: string;
  uploadFileIds: string[];
  linkFileIds: string[];
  frontendDebug?: boolean;
}
