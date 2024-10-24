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
  BASE_INFO = 'BASE_INFO',
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
  NONE = 'NONE',
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

// 接口场景状态
export enum ApiScenarioStatus {
  DEPRECATED = 'DEPRECATED',
  UNDERWAY = 'UNDERWAY',
  COMPLETED = 'COMPLETED',
}
// 接口导入支持格式
export enum RequestImportFormat {
  SWAGGER = 'Swagger3',
  MeterSphere = 'MeterSphere',
  Postman = 'Postman',
  Jmeter = 'Jmeter',
  Har = 'Har',
}

export enum RequestExportFormat {
  SWAGGER = 'Swagger',
  MeterSphere = 'MeterSphere',
}
// 接口导入方式
export enum RequestImportType {
  API = 'API',
  SCHEDULE = 'Schedule',
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
  BOOLEAN = 'boolean',
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
  SCRIPT = 'SCRIPT',
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
// 接口请求-前后置操作-处理器类型
export enum RequestConditionProcessor {
  SCRIPT = 'SCRIPT', // 脚本操作
  SQL = 'SQL', // SQL操作
  TIME_WAITING = 'TIME_WAITING', // 等待时间
  EXTRACT = 'EXTRACT', // 参数提取
  SCENARIO_SCRIPT = 'ENV_SCENARIO_SCRIPT', // 场景脚本
  REQUEST_SCRIPT = 'ENV_REQUEST_SCRIPT', // 请求脚本
}
// 接口请求-前后置操作-脚本处理器语言
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
  // GLOBAL = 'GLOBAL', // 全局参数，暂时不上
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
// 接口用例状态
export enum RequestCaseStatus {
  DEPRECATED = 'DEPRECATED',
  PROCESSING = 'PROCESSING',
  DONE = 'DONE',
}
// 创建接口场景组成部分
export enum ScenarioCreateComposition {
  STEP = 'STEP',
  PARAMS = 'PARAMS',
  PRE_POST = 'PRE_POST',
  ASSERTION = 'ASSERTION',
  SETTING = 'SETTING',
}
// 接口场景详情组成部分
export enum ScenarioDetailComposition {
  BASE_INFO = 'BASE_INFO',
  STEP = 'STEP',
  PARAMS = 'PARAMS',
  PRE_POST = 'PRE_POST',
  ASSERTION = 'ASSERTION',
  SETTING = 'SETTING',
  EXECUTE_HISTORY = 'EXECUTE_HISTORY',
  CHANGE_HISTORY = 'CHANGE_HISTORY',
  DEPENDENCY = 'DEPENDENCY',
  QUOTE = 'QUOTE',
}
// 场景执行状态
export enum ScenarioExecuteStatus {
  SUCCESS = 'SUCCESS',
  EXECUTING = 'EXECUTING',
  FAILED = 'FAILED',
  STOP = 'STOP',
  UN_EXECUTE = 'UN_EXECUTE',
  FAKE_ERROR = 'FAKE_ERROR',
}

// 场景步骤类型
export enum ScenarioStepType {
  API_CASE = 'API_CASE', // 接口用例
  LOOP_CONTROLLER = 'LOOP_CONTROLLER', // 循环控制器
  API = 'API', // 接口定义
  TEST_PLAN_API_CASE = 'TEST_PLAN_API_CASE', // 测试计划接口用例
  JMETER_COMPONENT = 'JMETER_COMPONENT', // Jmeter组件
  CUSTOM_REQUEST = 'CUSTOM_REQUEST', // 自定义请求
  API_SCENARIO = 'API_SCENARIO', // 场景
  IF_CONTROLLER = 'IF_CONTROLLER', // 条件控制器
  ONCE_ONLY_CONTROLLER = 'ONCE_ONLY_CONTROLLER', // 一次控制器
  CONSTANT_TIMER = 'CONSTANT_TIMER', // 等待控制器
  SCRIPT = 'SCRIPT', // 脚本
}
export enum ScenarioStepRefType {
  COPY = 'COPY', // 复制
  DIRECT = 'DIRECT', // 在场景中直接创建的步骤 例如 自定义请求，逻辑控制器
  PARTIAL_REF = 'PARTIAL_REF', // 部分引用
  REF = 'REF', // 完全引用
}
// 场景添加步骤操作类型
export enum ScenarioAddStepActionType {
  IMPORT_SYSTEM_API = 'IMPORT_SYSTEM_API',
  CUSTOM_API = 'CUSTOM_API',
  LOOP_CONTROL = 'LOOP_CONTROL',
  CONDITION_CONTROL = 'CONDITION_CONTROL',
  ONLY_ONCE_CONTROL = 'ONLY_ONCE_CONTROL',
  SCRIPT_OPERATION = 'SCRIPT_OPERATION',
  WAIT_TIME = 'WAIT_TIME',
}

// 接口场景-执行结果状态
export enum ExecuteStatusFilters {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  // RERUNNING = 'RERUNNING',
  ERROR = 'ERROR',
  SUCCESS = 'SUCCESS',
  FAKE_ERROR = 'FAKE_ERROR',
  STOPPED = 'STOPPED',
}

// 变更历史类型
export enum ChangeHistoryStatusFilters {
  ADD = 'ADD',
  UPDATE = 'UPDATE',
  IMPORT = 'IMPORT',
  DELETE = 'DELETE',
}
// 场景步骤-循环控制器类型
export enum ScenarioStepLoopTypeEnum {
  WHILE = 'WHILE',
  LOOP_COUNT = 'LOOP_COUNT',
  FOREACH = 'FOREACH',
}
// 场景步骤-循环控制器-while循环类型
export enum WhileConditionType {
  CONDITION = 'CONDITION',
  SCRIPT = 'SCRIPT',
}
// 场景步骤多态名
export enum ScenarioStepPolymorphicName {
  COMMON_SCRIPT = 'MsCommentScriptElement',
  IF_CONTROLLER = 'MsIfController',
  LOOP_CONTROLLER = 'MsLoopController',
  ONLY_ONCE = 'MsOnceOnlyController',
  TIME_CONTROLLER = 'MsConstantTimerController',
}
// 场景设置-失败执行策略
export enum ScenarioFailureStrategy {
  CONTINUE = 'CONTINUE',
  STOP = 'STOP',
}
// 接口响应-断言项类型
export enum FullResponseAssertionType {
  DOCUMENT = 'DOCUMENT',
  RESPONSE_CODE = 'RESPONSE_CODE',
  RESPONSE_HEADER = 'RESPONSE_HEADER',
  RESPONSE_TIME = 'RESPONSE_TIME',
  SCRIPT = 'SCRIPT',
  VARIABLE = 'VARIABLE',
  JSON_PATH = 'JSON_PATH',
  XPATH = 'XPATH',
  REGEX = 'REGEX',
}

export enum ReportExecStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  STOPPED = 'STOPPED',
  COMPLETED = 'COMPLETED',
}

export enum ProtocolKeyEnum {
  API_MODULE_TREE_PROTOCOL = 'API_MODULE_TREE_PROTOCOL',
  TEST_PLAN_API_CASE_PROTOCOL = 'TEST_PLAN_API_CASE_PROTOCOL',
  ASSOCIATE_CASE_PROTOCOL = 'ASSOCIATE_CASE_PROTOCOL',
  API_SCENARIO_IMPORT_PROTOCOL = 'API_SCENARIO_IMPORT_PROTOCOL',
  API_SCENARIO_CUSTOM_PROTOCOL = 'API_SCENARIO_CUSTOM_PROTOCOL',
  API_NEW_PROTOCOL = 'API_NEW_PROTOCOL',
  API_DEBUG_NEW_PROTOCOL = 'API_DEBUG_NEW_PROTOCOL',
  CASE_MANAGEMENT_ASSOCIATE_PROTOCOL = 'CASE_MANAGEMENT_ASSOCIATE_PROTOCOL', // 功能用例关联用例
}
