import { Language } from '@/components/pure/ms-code-editor/types';
import type { JsonSchema, JsonSchemaTableItem } from '@/components/pure/ms-json-schema/types';

import {
  type FullResponseAssertionType,
  RequestAssertionCondition,
  RequestAuthType,
  RequestBodyFormat,
  RequestConditionProcessor,
  RequestContentTypeEnum,
  RequestExtractEnvType,
  RequestExtractExpressionEnum,
  RequestExtractExpressionRuleType,
  RequestExtractResultMatchingRule,
  RequestExtractScope,
  RequestMethods,
  RequestParamsType,
  ResponseAssertionType,
  ResponseBodyAssertionDocumentType,
  ResponseBodyAssertionType,
  ResponseBodyDocumentAssertionType,
  ResponseBodyFormat,
  ResponseBodyXPathAssertionFormat,
  ScenarioExecuteStatus,
} from '@/enums/apiEnum';

// 获取插件表单选项参数
export interface GetPluginOptionsParams {
  orgId: string;
  pluginId: string;
  optionMethod: string;
  queryParam: Record<string, any>;
}
// 插件表单选项子项
export interface PluginOption {
  text: string;
  value: string;
}
// 协议列表子项
export interface ProtocolItem {
  protocol: string;
  polymorphicName: string;
  pluginId: string;
}
// 插件配置
export interface PluginConfig {
  id: string;
  name: string;
  options: Record<string, any>;
  script: Record<string, any>[];
  scriptType: string;
  apiDebugFields?: string[]; // 接口调试脚本内配置的全部字段集合
  apiDefinitionFields?: string[]; // 接口定义脚本内配置的全部字段集合
}
// 条件操作类型
export type ConditionType = RequestConditionProcessor;
// 断言-匹配条件规则
export type RequestAssertionConditionType = RequestAssertionCondition;
// 响应时间信息
export interface ResponseTiming {
  dnsLookupTime: number;
  tcpHandshakeTime: number;
  sslHandshakeTime: number;
  socketInitTime: number;
  latency: number;
  downloadTime: number;
  transferStartTime: number;
  responseTime: number;
}
// key-value参数信息
export interface KeyValueParam {
  id?: string; // id用于前端渲染，后台无此字段
  key: string;
  value: string;
  [key: string]: any; // 用于前端渲染时填充的自定义信息，后台无此字段
}
// 接口请求-带开启关闭的参数集合信息
export interface EnableKeyValueParam extends KeyValueParam {
  description: string;
  enable: boolean; // 参数是否启用
}
// 接口请求公共参数集合信息
export interface ExecuteRequestCommonParam extends EnableKeyValueParam {
  encode: boolean; // 是否编码
  maxLength?: number;
  minLength?: number;
  paramType: RequestParamsType; // 参数类型
  required: boolean;
  description: string;
  enable: boolean; // 参数是否启用
}
// 接口请求form-data、x-www-form-urlencoded参数集合信息
export type ExecuteRequestFormBodyFormValue = ExecuteRequestCommonParam & {
  files?: {
    fileId: string;
    fileName: string;
    local: boolean; // 是否是本地上传的文件
    fileAlias: string; // 文件别名
    delete: boolean; // 是否删除
    [key: string]: any; // 用于前端渲染时填充的自定义信息，后台无此字段
  }[];
  contentType?: RequestContentTypeEnum & string;
};
export interface ExecuteRequestFormBody {
  formValues: ExecuteRequestFormBodyFormValue[];
}
// 接口请求文件信息
export interface RequestFileInfo {
  fileId: string;
  fileName: string;
  fileAlias: string; // 文件别名
  local: boolean; // 是否是本地上传的文件
  delete?: boolean; // 关联文件是否被删除
  [key: string]: any; // 用于前端渲染时填充的自定义信息，后台无此字段
}
// 接口请求binary-body参数集合信息
export interface ExecuteBinaryBody {
  description: string;
  file?: RequestFileInfo;
  sendAsBody?: boolean; // 是否作为正文发送，只有 定义/mock 的响应体有此字段
}

// 接口请求json-body参数集合信息
export interface ExecuteJsonBody {
  enableJsonSchema?: boolean;
  enableTransition?: boolean;
  jsonSchema?: JsonSchema;
  jsonValue: string;
  // 前端渲染字段
  jsonSchemaTableData?: JsonSchemaTableItem[];
  jsonSchemaTableSelectedRowKeys?: string[];
}
// 执行请求配置
export interface ExecuteOtherConfig {
  autoRedirects: boolean; // 是否自动重定向 默认 false
  certificateAlias: string; // 证书别名
  connectTimeout: number; // 连接超时时间
  followRedirects: boolean; // 是否跟随重定向 默认 false
  responseTimeout: number; // 响应超时时间
}
// 断言-断言公共信息
export interface ResponseAssertionCommon {
  name: string; // 断言名称
  enable: boolean; // 是否启用断言
  assertionType: ResponseAssertionType; // 断言类型
}
// 断言-断言列表泛型
export interface ResponseAssertionGenerics<T> {
  assertions: T[];
  responseFormat?: ResponseBodyXPathAssertionFormat;
}
// 断言-响应头断言子项
export interface ResponseHeaderAssertionItem {
  condition: RequestAssertionConditionType;
  enable: boolean;
  expectedValue: string;
  header: string; // 响应头
}
// 断言-状态码断言
export type ResponseCodeAssertion = Pick<ResponseHeaderAssertionItem, 'condition' | 'expectedValue'>;
// 断言-文档断言-JSON断言\XML断言
export interface ResponseDocumentAssertionElement {
  id?: string;
  arrayVerification: boolean; // 是否组内验证
  children: ResponseDocumentAssertionElement[];
  condition: RequestAssertionConditionType;
  expectedResult: Record<string, any>; // 匹配值 即预期结果
  include: boolean; // 是否必含
  paramName: string; // 参数名
  type: ResponseBodyDocumentAssertionType; // 断言类型
  typeVerification: boolean; // 是否类型验证
}
// 断言-文档断言
export interface ResponseDocumentAssertion {
  enable: boolean; // 是否启用
  documentType: ResponseBodyAssertionDocumentType; // 文档类型
  followApiId: string; // 跟随定义的apiId 传空为不跟随接口定义
  jsonAssertion: ResponseDocumentAssertionElement;
  xmlAssertion: ResponseDocumentAssertionElement;
}
// 断言-断言列表的断言子项
export interface ResponseAssertionItem {
  condition?: RequestAssertionConditionType;
  expectedValue?: string;
  expression: string;
  enable?: boolean;
  extractType?: RequestExtractExpressionEnum;
  valid?: boolean;
}
// 断言-JSONPath断言子项
export type ResponseJSONPathAssertionItem = ResponseAssertionItem;
// 断言-正则断言子项
export type ResponseRegexAssertionItem = Pick<ResponseAssertionItem, 'expression'>;
// 断言-Xpath断言子项
export type ResponseXPathAssertionItem = Pick<ResponseAssertionItem, 'expression' | 'expectedValue'>;
// 脚本公共配置
export interface CommonScriptInfo {
  id: string; // 公共脚本id
  name: string; // 公共脚本名称
  script: string; // 公共脚本内容
  params: KeyValueParam[]; // 公共脚本参数
  scriptLanguage: Language; // 脚本语言
}
export interface ScriptCommonConfig {
  enableCommonScript: boolean; // 是否启用公共脚本
  script: string; // 脚本内容
  scriptId: string; // 脚本id
  scriptName: string; // 脚本名称
  scriptLanguage: Language; // 脚本语言
  commonScriptInfo: CommonScriptInfo; // 公共脚本信息
}
// 断言-响应体断言
export interface ResponseBodyAssertion {
  assertionBodyType: ResponseBodyAssertionType; // 断言类型
  documentAssertion: ResponseDocumentAssertion; // 文档断言
  jsonPathAssertion: ResponseAssertionGenerics<ResponseJSONPathAssertionItem>; // JSONPath断言
  regexAssertion: ResponseAssertionGenerics<ResponseRegexAssertionItem>; // 正则断言
  xpathAssertion: ResponseAssertionGenerics<ResponseXPathAssertionItem>; // XPath断言
}
// 断言-响应时间断言
export type ResponseTimeAssertion = Pick<ResponseAssertionItem, 'expectedValue'>;
// 断言-脚本断言
export type ResponseScriptAssertion = ScriptCommonConfig;
// 断言-变量断言
export interface ResponseVariableAssertion {
  variableAssertionItems: ResponseAssertionItem[];
}
// 执行请求-前后置操作处理器
export interface ExecuteConditionProcessorCommon {
  id: string | number; // 处理器ID，前端列表渲染需要，后台无此字段
  enable: boolean; // 是否启用
  name?: string; // 条件处理器名称
  processorType: RequestConditionProcessor;
  associateScenarioResult?: boolean; // 是否关联场景结果
  ignoreProtocols: string[]; // 忽略协议
  beforeStepScript: boolean; // 是否是步骤内前置脚本前
  assertionType?: RequestConditionProcessor;
}
// 执行请求-前后置操作-SQL脚本处理器
export interface SQLProcessor extends ExecuteConditionProcessorCommon {
  name: string; // 描述
  dataSourceId: string; // 数据源ID
  dataSourceName: string; // 数据源名称
  queryTimeout: number; // 超时时间
  resultVariable: string; // 按结果存储时的结果变量
  script: string; // 脚本内容
  variableNames: string; // 按列存储时的变量名集合,多个列可以使用逗号分隔
  extractParams: KeyValueParam[]; // 提取参数列表
}
// 执行请求-前后置操作-等待时间处理器
export interface TimeWaitingProcessor extends ExecuteConditionProcessorCommon {
  delay: number; // 等待时间 单位：毫秒
}
// 表达式类型
export type ExpressionType = RequestExtractExpressionEnum;
// 表达式配置
export interface ExpressionCommonConfig {
  id?: number | string; // 前端渲染使用字段
  enable: boolean; // 是否启用
  expression: string;
  extractType: ExpressionType; // 表达式类型
  variableName?: string;
  variableType?: RequestExtractEnvType;
  resultMatchingRule?: RequestExtractResultMatchingRule; // 结果匹配规则
  resultMatchingRuleNum?: number; // 匹配第几条结果
}
// 正则提取配置
export interface RegexExtract extends ExpressionCommonConfig {
  expressionMatchingRule?: RequestExtractExpressionRuleType; // 正则表达式匹配规则
  extractScope?: RequestExtractScope; // 正则提取范围
}
// JSONPath提取配置
export type JSONPathExtract = ExpressionCommonConfig;
// XPath提取配置
export interface XPathExtract extends ExpressionCommonConfig {
  responseFormat?: ResponseBodyXPathAssertionFormat; // 响应格式
}
// 执行请求-前后置操作-参数提取处理器
export interface ExtractProcessor extends ExecuteConditionProcessorCommon {
  extractors: (RegexExtract | JSONPathExtract | XPathExtract)[];
}
// 执行请求-断言配置子项
export type ExecuteAssertionItem = ResponseAssertionCommon &
  ResponseCodeAssertion &
  ResponseAssertionGenerics<ResponseHeaderAssertionItem> &
  ResponseBodyAssertion &
  ResponseTimeAssertion &
  ResponseScriptAssertion &
  ResponseVariableAssertion;
// 执行请求-断言配置
export interface ExecuteAssertionConfig {
  enableGlobal?: boolean; // 是否启用全局断言，部分地方没有
  assertions: ExecuteAssertionItem[];
}
// 执行请求-前后置操作-脚本处理器
export interface ScriptProcessorChild {
  polymorphicName: string; // 协议多态名称，写死MsCommonElement
  assertionConfig: ExecuteAssertionConfig;
}
export interface ScriptProcessor extends ScriptCommonConfig, ExecuteConditionProcessorCommon {
  children: ScriptProcessorChild[]; // 协议共有的子项配置
}
// 执行请求-前后置操作配置
export type ExecuteConditionProcessor = Partial<
  ScriptProcessor & SQLProcessor & TimeWaitingProcessor & ExtractProcessor
> &
  ExecuteConditionProcessorCommon;
export interface ExecuteConditionConfig {
  enableGlobal?: boolean; // 是否启用全局前/后置 默认为 true
  processors: ExecuteConditionProcessor[];
  activeItemId?: number | string;
}
// 执行请求-共用配置子项
export interface ExecuteCommonChild {
  polymorphicName: string; // 协议多态名称，写死MsCommonElement
  assertionConfig: ExecuteAssertionConfig;
  postProcessorConfig: ExecuteConditionConfig; // 后置处理器配置
  preProcessorConfig: ExecuteConditionConfig; // 前置处理器配置
}
// 执行请求-认证配置
export interface ExecuteAuthConfig {
  authType: RequestAuthType;
  basicAuth: {
    password: string;
    userName: string;
  };
  digestAuth: {
    password: string;
    userName: string;
  };
}
// 执行请求- body 配置-文本格式的 body
export interface ExecuteValueBody {
  value: string;
}
// 执行请求- body 配置
export interface ExecuteBody {
  bodyType: RequestBodyFormat;
  binaryBody: ExecuteBinaryBody;
  formDataBody: ExecuteRequestFormBody;
  jsonBody: ExecuteJsonBody;
  rawBody: ExecuteValueBody;
  wwwFormBody: ExecuteRequestFormBody;
  xmlBody: ExecuteValueBody;
}
// 执行HTTP请求入参
export interface ExecuteApiRequestFullParams {
  authConfig: ExecuteAuthConfig;
  body: ExecuteBody;
  headers: EnableKeyValueParam[];
  method: RequestMethods | string;
  otherConfig: ExecuteOtherConfig;
  path: string;
  query: ExecuteRequestCommonParam[];
  rest: ExecuteRequestCommonParam[];
  url: string;
  polymorphicName: string; // 协议多态名称
  children: ExecuteCommonChild[]; // 协议共有的子项配置
  aiCreate?: boolean; // 是否AI创建
}
// 执行插件请求入参
export interface ExecutePluginRequestParams {
  polymorphicName: string; // 协议多态名称
  children: ExecuteCommonChild[]; // 协议共有的子项配置
  [key: string]: any; // key-value形式的插件参数
}
// 执行接口调试入参
export interface ExecuteRequestParams {
  id?: string;
  reportId?: string;
  environmentId: string;
  uploadFileIds: string[];
  linkFileIds: string[];
  request: ExecuteApiRequestFullParams | ExecutePluginRequestParams;
  projectId: string;
  frontendDebug?: boolean; // 是否本地调试，该模式下接口会返回执行参数，用来调用本地执行服务
  apiDefinitionId?: string | number; // 接口用例执行和调试时需要传
}
// 断言-断言列表表格子项
export interface ResponseAssertionTableItem {
  actualValue: string;
  assertionType: FullResponseAssertionType;
  condition: RequestAssertionConditionType;
  content: string;
  expectedValue: string;
  message: string;
  name: string;
  pass: boolean;
}
// 请求提取结果项
export interface ResponseExtractItem {
  name: string;
  value: string;
  type: RequestExtractEnvType;
  expression: string;
}
// 响应结果
export interface ResponseResult {
  body: string;
  contentType: string;
  filePath?: string;
  headers: string;
  dnsLookupTime: number;
  downloadTime: number;
  latency: number;
  responseCode: number;
  responseTime: number;
  responseSize: number;
  socketInitTime: number;
  sslHandshakeTime: number;
  tcpHandshakeTime: number;
  transferStartTime: number;
  vars: string;
  extractResults: ResponseExtractItem[];
  assertions: ResponseAssertionTableItem[];
  imageUrl?: string; // 返回为图片时的图片地址
}

export interface RequestResult {
  body: string;
  headers: string;
  url: string;
  method: RequestMethods | string;
  responseResult: ResponseResult;
  isSuccessful?: boolean;
  console?: string;
  status?: ScenarioExecuteStatus;
  fakeErrorCode?: string;
  [key: string]: any;
}
export interface RequestTaskResult {
  requestResults: RequestResult[]; // 请求结果
  console: string;
}
// 响应定义-body
export interface ResponseDefinitionBody {
  bodyType: ResponseBodyFormat;
  jsonBody: ExecuteJsonBody;
  xmlBody: ExecuteValueBody;
  rawBody: ExecuteValueBody;
  binaryBody: ExecuteBinaryBody;
}
// 响应定义
export interface ResponseDefinition {
  id: string | number;
  statusCode: string | number;
  defaultFlag: boolean; // 默认响应标志
  name: string; // 响应名称
  headers: KeyValueParam[];
  body: ResponseDefinitionBody;
  [key: string]: any; // 用于前端渲染时填充的自定义信息，后台无此字段
}

// 接口用例执行历史报告对象
export type ApiCaseReportDetail = {
  id: string | number;
  reportId: string | number;
  stepId: string | number;
  status: string;
  fakeCode: string;
  requestName: string;
  requestTime: number;
  code: string;
  responseSize: number;
  scriptIdentifier: string;
  content: RequestResult;
};
// curl解析结果
export interface CurlParseResult {
  method: RequestMethods | string;
  url: string;
  headers: Record<string, any>;
  body: Record<string, any> | string;
  bodyType: RequestBodyFormat;
  queryParams: Record<string, any>;
}
