// 接口请求方法
export enum RequestMethods {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
  PATCH = 'PATCH',
  OPTIONS = 'OPTIONS',
  HEAD = 'HEAD',
  CONNECT = 'CONNECT',
}
// 接口组成部分
export enum RequestComposition {
  PLUGIN = 'PLUGIN',
  HEADER = 'HEADER',
  BODY = 'BODY',
  QUERY = 'QUERY',
  REST = 'REST',
  PRECONDITION = 'PRECONDITION',
  POST_CONDITION = 'POST_CONDITION',
  ASSERTION = 'ASSERTION',
  AUTH = 'AUTH',
  SETTING = 'SETTING',
}
// 接口请求体格式
export enum RequestBodyFormat {
  NONE = 'NONE',
  FORM_DATA = 'FORM_DATA',
  WWW_FORM = 'WWW_FORM',
  JSON = 'JSON',
  XML = 'XML',
  RAW = 'RAW',
  BINARY = 'BINARY',
}
// 接口响应头格式
export enum RequestContentTypeEnum {
  JSON = 'application/json',
  TEXT = 'application/text',
  OGG = 'application/ogg',
  PDF = 'application/pdf',
  JAVASCRIPT = 'application/javascript',
  OCTET_STREAM = 'application/octet-stream',
  VND_API_JSON = 'application/vnd.api+json',
  ATOM_XML = 'application/atom+xml',
  ECMASCRIPT = 'application/ecmascript',
}
// 接口响应组成部分
export enum ResponseComposition {
  BODY = 'BODY',
  HEADER = 'HEADER',
  REAL_REQUEST = 'REAL_REQUEST', // 实际请求
  CONSOLE = 'CONSOLE',
  EXTRACT = 'EXTRACT',
  ASSERTION = 'ASSERTION',
  CODE = 'CODE',
}
// 接口响应体格式
export enum ResponseBodyFormat {
  JSON = 'JSON',
  XML = 'XML',
  RAW = 'RAW',
  BINARY = 'BINARY',
}
// 接口定义状态
export enum RequestDefinitionStatus {
  DEPRECATED = 'DEPRECATED',
  PROCESSING = 'PROCESSING',
  DEBUGGING = 'DEBUGGING',
  DONE = 'DONE',
}
// 接口导入支持格式
export enum RequestImportFormat {
  SWAGGER = 'SWAGGER',
}
// 接口认证设置类型
export enum RequestAuthType {
  BASIC = 'BASIC',
  DIGEST = 'DIGEST',
  NONE = 'NONE',
}
// 接口参数表格的参数类型
export enum RequestParamsType {
  STRING = 'string',
  INTEGER = 'integer',
  NUMBER = 'number',
  ARRAY = 'array',
  JSON = 'json',
  FILE = 'file',
}
// 接口断言类型
export enum ResponseAssertionType {
  RESPONSE_CODE = 'RESPONSE_CODE',
  RESPONSE_HEADER = 'RESPONSE_HEADER',
  RESPONSE_BODY = 'RESPONSE_BODY',
  RESPONSE_TIME = 'RESPONSE_TIME',
  SCRIPT = 'SCRIPT',
  VARIABLE = 'VARIABLE',
}
// 接口断言-响应体断言类型
export enum ResponseBodyAssertionType {
  DOCUMENT = 'DOCUMENT',
  JSON_PATH = 'JSON_PATH',
  REGEX = 'REGEX', // 正则表达式
  XPATH = 'XPATH',
}
// 接口断言-响应体断言-文档类型
export enum ResponseBodyAssertionDocumentType {
  JSON = 'JSON',
  XML = 'XML',
}
// 接口断言-响应体断言-文档断言类型
export enum ResponseBodyDocumentAssertionType {
  ARRAY = 'array',
  BOOLEAN = 'boolean',
  INTEGER = 'integer',
  NUMBER = 'number',
  STRING = 'string',
}
// 接口断言-响应体断言-Xpath断言类型
export enum ResponseBodyXPathAssertionFormat {
  HTML = 'HTML',
  XML = 'XML',
}
// 接口断言-断言匹配条件
export enum RequestAssertionCondition {
  CONTAINS = 'CONTAINS', // 包含
  EMPTY = 'EMPTY', // 为空
  END_WITH = 'END_WITH', // 以 xx 结尾
  EQUALS = 'EQUALS', // 等于
  GT = 'GT', // 大于
  GT_OR_EQUALS = 'GT_OR_EQUALS', // 大于等于
  LENGTH_EQUALS = 'LENGTH_EQUALS', // 长度等于
  LENGTH_GT = 'LENGTH_GT', // 长度大于
  LENGTH_GT_OR_EQUALS = 'LENGTH_GT_OR_EQUALS', // 长度大于等于
  LENGTH_LT = 'LENGTH_LT', // 长度小于
  LENGTH_LT_OR_EQUALS = 'LENGTH_LT_OR_EQUALS', // 长度小于等于
  LENGTH_NOT_EQUALS = 'LENGTH_NOT_EQUALS', // 长度不等于
  LT = 'LT', // 小于
  LT_OR_EQUALS = 'LT_OR_EQUALS', // 小于等于
  NOT_CONTAINS = 'NOT_CONTAINS', // 不包含
  NOT_EMPTY = 'NOT_EMPTY', // 不为空
  NOT_EQUALS = 'NOT_EQUALS', // 不等于
  REGEX = 'REGEX', // 正则表达式
  START_WITH = 'START_WITH', // 以 xx 开头
  UNCHECKED = 'UNCHECKED', // 不校验
}
// 接口请求-前后置条件-处理器类型
export enum RequestConditionProcessor {
  SCRIPT = 'SCRIPT', // 脚本操作
  SQL = 'SQL', // SQL操作
  TIME_WAITING = 'TIME_WAITING', // 等待时间
  EXTRACT = 'EXTRACT', // 参数提取
}
// 接口请求-前后置条件-脚本处理器语言
export enum RequestConditionScriptLanguage {
  BEANSHELL = 'BEANSHELL', // Beanshell
  BEANSHELL_JSR233 = 'BEANSHELL_JSR233', // Beanshell JSR233
  GROOVY = 'GROOVY', // Groovy
  JAVASCRIPT = 'JAVASCRIPT', // JavaScript
  PYTHON = 'PYTHON', // Python
}
// 接口请求-参数提取-环境类型
export enum RequestExtractEnvType {
  ENVIRONMENT = 'ENVIRONMENT', // 环境参数
  GLOBAL = 'GLOBAL', // 全局参数
  TEMPORARY = 'TEMPORARY', // 临时参数
}
// 接口请求-参数提取-表达式类型
export enum RequestExtractExpressionEnum {
  REGEX = 'REGEX', // 正则表达式
  JSON_PATH = 'JSON_PATH', // JSONPath
  X_PATH = 'X_PATH', // Xpath
}
// 接口请求-参数提取-表达式匹配规则类型
export enum RequestExtractExpressionRuleType {
  EXPRESSION = 'EXPRESSION', // 匹配表达式
  GROUP = 'GROUP', // 匹配组
}
// 接口请求-参数提取-提取范围
export enum RequestExtractScope {
  BODY = 'BODY',
  BODY_AS_DOCUMENT = 'BODY_AS_DOCUMENT',
  UNESCAPED_BODY = 'UNESCAPED_BODY',
  REQUEST_HEADERS = 'REQUEST_HEADERS',
  RESPONSE_CODE = 'RESPONSE_CODE',
  RESPONSE_HEADERS = 'RESPONSE_HEADERS',
  RESPONSE_MESSAGE = 'RESPONSE_MESSAGE',
  URL = 'URL',
}
// 接口请求-参数提取-表达式匹配结果规则类型
export enum RequestExtractResultMatchingRule {
  ALL = 'ALL', // 全部匹配
  RANDOM = 'RANDOM', // 随机匹配
  SPECIFIC = 'SPECIFIC', // 指定匹配
}
