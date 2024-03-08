import {
  EnableKeyValueParam,
  ExecuteRequestCommonParam,
  ExecuteRequestFormBodyFormValue,
  KeyValueParam,
  ResponseDefinition,
} from '@/models/apiTest/common';
import { RequestContentTypeEnum, RequestParamsType, ResponseBodyFormat, ResponseComposition } from '@/enums/apiEnum';

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
    },
  },
};

// 默认提取参数的 key-value 表格行的值
export const defaultKeyValueParamItem: KeyValueParam = {
  key: '',
  value: '',
};

// 请求的响应 response 的响应状态码集合
export const statusCodes = [200, 201, 202, 203, 204, 205, 400, 401, 402, 403, 404, 405, 500, 501, 502, 503, 504, 505];
