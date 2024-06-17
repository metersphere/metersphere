import { cloneDeep } from 'lodash-es';

import { EQUAL } from '@/components/pure/ms-advance-filter';

import { useI18n } from '@/hooks/useI18n';

import {
  EnableKeyValueParam,
  ExecuteBody,
  ExecuteRequestCommonParam,
  ExecuteRequestFormBodyFormValue,
  KeyValueParam,
  RequestTaskResult,
  ResponseAssertionItem,
  ResponseDefinition,
} from '@/models/apiTest/common';
import type { MockParams } from '@/models/apiTest/mock';
import {
  FullResponseAssertionType,
  RequestAssertionCondition,
  RequestBodyFormat,
  RequestCaseStatus,
  RequestContentTypeEnum,
  RequestExtractEnvType,
  RequestExtractExpressionEnum,
  RequestExtractExpressionRuleType,
  RequestExtractResultMatchingRule,
  RequestExtractScope,
  RequestParamsType,
  ResponseBodyFormat,
  ResponseBodyXPathAssertionFormat,
  ResponseComposition,
} from '@/enums/apiEnum';
import { ReportStatus } from '@/enums/reportEnum';

import type { ExpressionConfig } from './fastExtraction/moreSetting.vue';

const { t } = useI18n();

// 请求 body 参数表格默认行的值
export const defaultBodyParamsItem: ExecuteRequestFormBodyFormValue = {
  key: '',
  value: '',
  paramType: RequestParamsType.STRING,
  description: '',
  required: false,
  maxLength: undefined,
  minLength: undefined,
  encode: false,
  enable: true,
  contentType: RequestContentTypeEnum.TEXT,
  files: [],
};

// 请求 header 参数表格默认行的值
export const defaultHeaderParamsItem: EnableKeyValueParam = {
  key: '',
  value: '',
  description: '',
  enable: true,
};

// 请求 query、rest 参数表格默认行的值
export const defaultRequestParamsItem: ExecuteRequestCommonParam = {
  key: '',
  value: '',
  paramType: RequestParamsType.STRING,
  description: '',
  required: false,
  maxLength: undefined,
  minLength: undefined,
  encode: false,
  enable: true,
};

// 请求的响应 response 默认的响应信息项
export const defaultResponseItem: ResponseDefinition = {
  id: new Date().getTime(),
  name: 'apiTestManagement.response',
  label: 'apiTestManagement.response',
  closable: false,
  statusCode: 200,
  defaultFlag: true,
  showPopConfirm: false,
  showRenamePopConfirm: false,
  responseActiveTab: ResponseComposition.BODY,
  headers: [],
  body: {
    bodyType: ResponseBodyFormat.JSON,
    jsonBody: {
      jsonValue: '',
      enableJsonSchema: false,
      enableTransition: false,
    },
    xmlBody: {
      value: '',
    },
    rawBody: {
      value: '',
    },
    binaryBody: {
      description: '',
      file: undefined,
      sendAsBody: false,
    },
  },
};

// 请求的默认 body 参数
export const defaultBodyParams: ExecuteBody = {
  bodyType: RequestBodyFormat.NONE,
  formDataBody: {
    formValues: [],
  },
  wwwFormBody: {
    formValues: [],
  },
  jsonBody: {
    jsonValue: '',
  },
  xmlBody: { value: '' },
  rawBody: { value: '' },
  binaryBody: {
    description: '',
    file: undefined,
    sendAsBody: false,
  },
};

// 默认的响应内容结构
export const defaultResponse: RequestTaskResult = {
  requestResults: [
    {
      body: '',
      headers: '',
      method: '',
      url: '',
      responseResult: {
        body: '',
        contentType: '',
        headers: '',
        dnsLookupTime: 0,
        downloadTime: 0,
        latency: 0,
        responseCode: 0,
        responseTime: 0,
        responseSize: 0,
        socketInitTime: 0,
        tcpHandshakeTime: 0,
        transferStartTime: 0,
        sslHandshakeTime: 0,
        vars: '',
        extractResults: [],
        assertions: [],
      },
    },
  ],
  console: '',
};

// 默认提取参数的 key-value 表格行的值
export const defaultKeyValueParamItem: KeyValueParam = {
  key: '',
  value: '',
};

// 请求的响应 response 的响应状态码集合
export const statusCodes = [200, 201, 202, 203, 204, 205, 400, 401, 402, 403, 404, 405, 500, 501, 502, 503, 504, 505];

// 用例等级选项
export const casePriorityOptions = [
  { label: 'P0', value: 'P0' },
  { label: 'P1', value: 'P1' },
  { label: 'P2', value: 'P2' },
  { label: 'P3', value: 'P3' },
];

// 用例状态选项
export const caseStatusOptions = [
  { label: 'apiTestManagement.processing', value: RequestCaseStatus.PROCESSING },
  { label: 'apiTestManagement.deprecate', value: RequestCaseStatus.DEPRECATED },
  { label: 'apiTestManagement.done', value: RequestCaseStatus.DONE },
];

// 断言 参数表格默认行的值
export const defaultAssertParamsItem: ResponseAssertionItem = {
  expression: '',
  condition: RequestAssertionCondition.EQUALS,
  expectedValue: '',
  enable: true,
};

// 断言xpath & reg
export const defaultAssertXpathParamsItem: ResponseAssertionItem = {
  expression: '',
  enable: true,
};
// 断言 xpath
export const defaultExtractParamItem: ExpressionConfig = {
  enable: true,
  variableName: '',
  variableType: RequestExtractEnvType.TEMPORARY,
  extractScope: RequestExtractScope.BODY,
  expression: '',
  extractType: RequestExtractExpressionEnum.JSON_PATH,
  expressionMatchingRule: RequestExtractExpressionRuleType.EXPRESSION,
  resultMatchingRule: RequestExtractResultMatchingRule.RANDOM,
  resultMatchingRuleNum: 1,
  responseFormat: ResponseBodyXPathAssertionFormat.XML,
  moreSettingPopoverVisible: false,
};
// @desc 断言的字段xpath和上边的defaultExtractParamItem不匹配所以添加此类型为了保存参数过滤正确
export const assertDefaultParamsItem: ResponseAssertionItem = {
  expression: '',
  condition: RequestAssertionCondition.EQUALS,
  expectedValue: '',
  enable: true,
};

// 断言 json默认值
export const jsonPathDefaultParamItem = {
  enable: true,
  variableName: '',
  variableType: RequestExtractEnvType.TEMPORARY,
  extractScope: RequestExtractScope.BODY,
  expression: '',
  condition: EQUAL.value,
  extractType: RequestExtractExpressionEnum.JSON_PATH,
  expressionMatchingRule: RequestExtractExpressionRuleType.EXPRESSION,
  resultMatchingRule: RequestExtractResultMatchingRule.RANDOM,
  resultMatchingRuleNum: 1,
  responseFormat: ResponseBodyXPathAssertionFormat.XML,
  moreSettingPopoverVisible: false,
};
// 断言 正则默认值
export const regexDefaultParamItem = {
  expression: '',
  enable: true,
  valid: true,
  variableType: RequestExtractEnvType.TEMPORARY,
  extractScope: RequestExtractScope.BODY,
  extractType: RequestExtractExpressionEnum.REGEX,
  expressionMatchingRule: RequestExtractExpressionRuleType.EXPRESSION,
  resultMatchingRule: RequestExtractResultMatchingRule.RANDOM,
  resultMatchingRuleNum: 1,
  responseFormat: ResponseBodyXPathAssertionFormat.XML,
  moreSettingPopoverVisible: false,
};
// 响应断言类型映射
export const responseAssertionTypeMap: Record<string, string> = {
  [FullResponseAssertionType.DOCUMENT]: 'apiTestManagement.document',
  [FullResponseAssertionType.RESPONSE_CODE]: 'apiTestManagement.responseCode',
  [FullResponseAssertionType.RESPONSE_HEADER]: 'apiTestManagement.responseHeader',
  [FullResponseAssertionType.RESPONSE_TIME]: 'apiTestManagement.responseTime',
  [FullResponseAssertionType.SCRIPT]: 'apiTestManagement.script',
  [FullResponseAssertionType.VARIABLE]: 'apiTestManagement.variable',
};
// 提取类型选项
export const extractTypeOptions = [
  // 全局参数，暂时不上
  // {
  //   label: t('apiTestDebug.globalParameter'),
  //   value: RequestExtractEnvType.GLOBAL,
  // },
  {
    label: 'apiTestDebug.envParameter',
    value: RequestExtractEnvType.ENVIRONMENT,
  },
  {
    label: 'apiTestDebug.tempParameter',
    value: RequestExtractEnvType.TEMPORARY,
  },
];
// mock 匹配规则默认项
export const defaultMatchRuleItem = {
  key: '',
  value: '',
  condition: 'EQUALS',
  description: '',
  paramType: RequestParamsType.STRING,
  files: [],
};
// mock 默认参数
export const mockDefaultParams: MockParams = {
  isNew: true,
  projectId: '',
  name: '',
  statusCode: 200,
  tags: [],
  mockMatchRule: {
    header: {
      matchRules: [],
      matchAll: true,
    },
    query: {
      matchRules: [],
      matchAll: true,
    },
    rest: {
      matchRules: [],
      matchAll: true,
    },
    body: {
      bodyType: RequestBodyFormat.NONE,
      formDataBody: {
        matchRules: [],
        matchAll: true,
      },
      wwwFormBody: {
        matchRules: [],
        matchAll: true,
      },
      jsonBody: {
        jsonValue: '',
      },
      xmlBody: { value: '' },
      rawBody: { value: '' },
      binaryBody: {
        description: '',
        file: undefined,
        sendAsBody: false,
      },
    },
  },
  response: {
    statusCode: 200,
    headers: [],
    useApiResponse: false,
    apiResponseId: '',
    body: {
      bodyType: ResponseBodyFormat.JSON,
      jsonBody: {
        jsonValue: '',
        enableJsonSchema: false,
        enableTransition: false,
      },
      xmlBody: {
        value: '',
      },
      rawBody: {
        value: '',
      },
      binaryBody: {
        description: '',
        file: undefined,
        sendAsBody: false,
      },
    },
    delay: 0,
  },
  apiDefinitionId: '',
  uploadFileIds: [],
  linkFileIds: [],
};
export const makeMockDefaultParams = () => {
  const defaultParams = cloneDeep(mockDefaultParams);
  defaultParams.id = Date.now().toString();
  defaultParams.mockMatchRule.body.formDataBody.matchRules.push({
    ...defaultMatchRuleItem,
    id: Date.now().toString(),
  });
  defaultParams.mockMatchRule.body.wwwFormBody.matchRules.push({
    ...defaultMatchRuleItem,
    id: Date.now().toString(),
  });
  defaultParams.mockMatchRule.header.matchRules.push({ ...defaultMatchRuleItem, id: Date.now().toString() });
  defaultParams.mockMatchRule.query.matchRules.push({ ...defaultMatchRuleItem, id: Date.now().toString() });
  defaultParams.mockMatchRule.rest.matchRules.push({ ...defaultMatchRuleItem, id: Date.now().toString() });
  defaultParams.response.headers.push({ ...defaultMatchRuleItem, id: Date.now().toString() });
  return cloneDeep(defaultParams);
};
// mock 匹配规则选项
export const matchRuleOptions = [
  {
    label: 'mockManagement.equals',
    value: 'EQUALS',
  },
  {
    label: 'mockManagement.notEquals',
    value: 'NOT_EQUALS',
  },
  {
    label: 'mockManagement.lengthEquals',
    value: 'LENGTH_EQUALS',
  },
  {
    label: 'mockManagement.lengthNotEquals',
    value: 'LENGTH_NOT_EQUALS',
  },
  {
    label: 'mockManagement.lengthLarge',
    value: 'LENGTH_LARGE',
  },
  {
    label: 'mockManagement.lengthLess',
    value: 'LENGTH_SHOT',
  },
  {
    label: 'mockManagement.contain',
    value: 'CONTAINS',
  },
  {
    label: 'mockManagement.notContain',
    value: 'NOT_CONTAINS',
  },
  {
    label: 'mockManagement.empty',
    value: 'IS_EMPTY',
  },
  {
    label: 'mockManagement.notEmpty',
    value: 'IS_NOT_EMPTY',
  },
  {
    label: 'mockManagement.regular',
    value: 'REGULAR_MATCH',
  },
];
// mock 参数为文件类型的匹配规则选项
export const mockFileMatchRules = ['EQUALS', 'NOT_EQUALS', 'IS_EMPTY', 'IS_NOT_EMPTY'];

// 执行结果筛选下拉
export const lastReportStatusListOptions = computed(() => {
  return Object.keys(ReportStatus).map((key) => {
    return {
      value: key,
      label: t(ReportStatus[key].label),
    };
  });
});
