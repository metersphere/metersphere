import {
  EnableKeyValueParam,
  ExecuteRequestCommonParam,
  ExecuteRequestFormBodyFormValue,
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
export const defaultResponseItem = {
  id: new Date().getTime(),
  label: 'apiTestManagement.response',
  name: 'apiTestManagement.response',
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
