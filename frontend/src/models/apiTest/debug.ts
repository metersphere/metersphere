import { Language } from '@/components/pure/ms-code-editor/types';

import {
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
  ResponseBodyXPathAssertionFormat,
} from '@/enums/apiEnum';

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
// 接口请求binary-body参数集合信息
export interface ExecuteBinaryBody {
  description: string;
  file?: {
    fileId: string;
    fileName: string;
    local: boolean; // 是否是本地上传的文件
    [key: string]: any; // 用于前端渲染时填充的自定义信息，后台无此字段
  };
}
// 接口请求json-body参数集合信息
export interface ExecuteJsonBody {
  enableJsonSchema?: boolean;
  enableTransition?: boolean;
  jsonSchema?: string;
  jsonValue: string;
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
  condition: RequestAssertionConditionType;
  expectedValue: string;
  expression: string;
  enable?: boolean;
}
// 断言-JSONPath断言子项
export type ResponseJSONPathAssertionItem = ResponseAssertionItem;
// 断言-正则断言子项
export type ResponseRegexAssertionItem = Pick<ResponseAssertionItem, 'expression'>;
// 断言-Xpath断言子项
export type ResponseXPathAssertionItem = Pick<ResponseAssertionItem, 'expression' | 'expectedValue'>;
// 脚本公共配置
export interface ScriptCommonConfig {
  enableCommonScript: boolean; // 是否启用公共脚本
  script: string; // 脚本内容
  scriptId: string; // 脚本id
  scriptName: string; // 脚本名称
  scriptLanguage: Language; // 脚本语言
  commonScriptInfo: {
    id: string; // 公共脚本id
    name: string; // 公共脚本名称
    script: string; // 公共脚本内容
    params: KeyValueParam[]; // 公共脚本参数
    scriptLanguage: Language; // 脚本语言
  }; // 公共脚本信息
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
// 执行请求-前后置条件处理器
export interface ExecuteConditionProcessorCommon {
  id: number; // 处理器ID，前端列表渲染需要，后台无此字段
  enable: boolean; // 是否启用
  name?: string; // 条件处理器名称
  processorType: RequestConditionProcessor;
}
// 执行请求-前后置条件-脚本处理器
export type ScriptProcessor = ScriptCommonConfig & ExecuteConditionProcessorCommon;
// 执行请求-前后置条件-SQL脚本处理器
export interface SQLProcessor extends ExecuteConditionProcessorCommon {
  description: string; // 描述
  dataSourceId: string; // 数据源ID
  environmentId: string; // 环境ID
  queryTimeout: number; // 超时时间
  resultVariable: string; // 按结果存储时的结果变量
  script: string; // 脚本内容
  variableNames: string; // 按列存储时的变量名集合,多个列可以使用逗号分隔
  variables: EnableKeyValueParam[]; // 变量列表
  extractParams: KeyValueParam[]; // 提取参数列表
}
// 执行请求-前后置条件-等待时间处理器
export interface TimeWaitingProcessor extends ExecuteConditionProcessorCommon {
  delay: number; // 等待时间 单位：毫秒
}
// 表达式类型
export type ExpressionType = RequestExtractExpressionEnum;
// 表达式配置
export interface ExpressionCommonConfig {
  enable: boolean; // 是否启用
  expression: string;
  extractType: ExpressionType; // 表达式类型
  variableName: string;
  variableType: RequestExtractEnvType;
  resultMatchingRule: RequestExtractResultMatchingRule; // 结果匹配规则
  resultMatchingRuleNum: number; // 匹配第几条结果
}
// 正则提取配置
export interface RegexExtract extends ExpressionCommonConfig {
  expressionMatchingRule: RequestExtractExpressionRuleType; // 正则表达式匹配规则
  extractScope: RequestExtractScope; // 正则提取范围
}
// JSONPath提取配置
export type JSONPathExtract = ExpressionCommonConfig;
// XPath提取配置
export interface XPathExtract extends ExpressionCommonConfig {
  responseFormat: ResponseBodyXPathAssertionFormat; // 响应格式
}
// 执行请求-前后置条件-参数提取处理器
export interface ExtractProcessor extends ExecuteConditionProcessorCommon {
  extractors: (RegexExtract | JSONPathExtract | XPathExtract)[];
}
// 执行请求-前后置条件配置
export type ExecuteConditionProcessor = Partial<
  ScriptProcessor & SQLProcessor & TimeWaitingProcessor & ExtractProcessor
> &
  ExecuteConditionProcessorCommon;
export interface ExecuteConditionConfig {
  enableGlobal?: boolean; // 是否启用全局前/后置 默认为 true
  processors: ExecuteConditionProcessor[];
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
  enableGlobal: boolean; // 是否启用全局断言
  assertions: ExecuteAssertionItem[];
}
// 执行请求-共用配置子项
export interface ExecuteCommonChild {
  polymorphicName: 'MsCommonElement'; // 协议多态名称，写死MsCommonElement
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
export interface ExecuteHTTPRequestFullParams {
  authConfig: ExecuteAuthConfig;
  body: ExecuteBody;
  headers: EnableKeyValueParam[];
  method: RequestMethods;
  otherConfig: ExecuteOtherConfig;
  path: string;
  query: ExecuteRequestCommonParam[];
  rest: ExecuteRequestCommonParam[];
  url: string;
  polymorphicName: string; // 协议多态名称
  children: ExecuteCommonChild[]; // 协议共有的子项配置
}
// 执行插件请求入参
export interface ExecutePluginRequestParams {
  [key: string]: any; // key-value形式的插件参数
  polymorphicName: string; // 协议多态名称
  children: ExecuteCommonChild[]; // 协议共有的子项配置
}
// 执行接口调试入参
export interface ExecuteRequestParams {
  id?: string;
  reportId: string;
  environmentId: string;
  uploadFileIds: string[];
  linkFileIds: string[];
  request: ExecuteHTTPRequestFullParams | ExecutePluginRequestParams;
  projectId: string;
}
// 保存接口调试入参
export interface SaveDebugParams {
  name: string;
  protocol: string;
  method: RequestMethods;
  path: string;
  projectId: string;
  moduleId: string;
  request: ExecuteHTTPRequestFullParams | ExecutePluginRequestParams;
  uploadFileIds: string[];
  linkFileIds: string[];
}
// 更新接口调试入参
export interface UpdateDebugParams extends Partial<SaveDebugParams> {
  id: string;
  deleteFileIds?: string[];
  unLinkFileIds?: string[];
}
// 更新模块入参
export interface UpdateDebugModule {
  id: string;
  name: string;
}
// 添加模块入参
export interface AddDebugModuleParams {
  projectId: string;
  name: string;
  parentId: string;
}
// 接口调试详情-请求参数
export interface DebugDetailRequest {
  stepId: string;
  resourceId: string;
  projectId: string;
  name: string;
  enable: boolean;
  children: string[];
  parent: string;
  polymorphicName: string;
}
// 接口调试详情
export interface DebugDetail {
  id: string;
  name: string;
  protocol: string;
  method: string;
  path: string;
  projectId: string;
  moduleId: string;
  createTime: number;
  createUser: string;
  updateTime: number;
  updateUser: string;
  pos: number;
  request: DebugDetailRequest & (ExecuteHTTPRequestFullParams | ExecutePluginRequestParams);
  response: string;
}
