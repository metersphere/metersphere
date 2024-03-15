import { ConditionType } from '@/models/apiTest/common';
import { RequestBodyFormat, RequestConditionProcessor, ScenarioStepType } from '@/enums/apiEnum';

// 条件操作类型
export type ConditionTypeNameMap = Record<ConditionType, string>;
export const conditionTypeNameMap = {
  [RequestConditionProcessor.SCRIPT]: 'apiTestDebug.script',
  [RequestConditionProcessor.SQL]: 'apiTestDebug.sql',
  [RequestConditionProcessor.TIME_WAITING]: 'apiTestDebug.waitTime',
  [RequestConditionProcessor.EXTRACT]: 'apiTestDebug.extractParameter',
  [RequestConditionProcessor.SCENARIO_SCRIPT]: 'apiTestDebug.script',
  [RequestConditionProcessor.REQUEST_SCRIPT]: 'apiTestDebug.script',
};
// 代码字符集
export const codeCharset = ['UTF-8', 'UTF-16', 'GBK', 'GB2312', 'ISO-8859-1', 'Shift_JIS', 'ASCII', 'BIG5', 'KOI8-R'];
// 请求体类型显示映射
export const requestBodyTypeMap = {
  [RequestBodyFormat.FORM_DATA]: 'form-data',
  [RequestBodyFormat.WWW_FORM]: 'x-www-form-urlencoded',
  [RequestBodyFormat.RAW]: 'raw',
  [RequestBodyFormat.BINARY]: 'binary',
  [RequestBodyFormat.JSON]: 'json',
  [RequestBodyFormat.XML]: 'xml',
  [RequestBodyFormat.NONE]: 'none',
};
// 请求/响应头选项
export const responseHeaderOption = [
  { label: 'Accept', value: 'accept' },
  { label: 'Accept-Encoding', value: 'acceptEncoding' },
  { label: 'Accept-Language', value: 'acceptLanguage' },
  { label: 'Cache-Control', value: 'cacheControl' },
  { label: 'Content-Type', value: 'contentType' },
  { label: 'Content-Length', value: 'contentLength' },
  { label: 'User-Agent', value: 'userAgent' },
  { label: 'Referer', value: 'referer' },
  { label: 'Cookie', value: 'cookie' },
  { label: 'Authorization', value: 'authorization' },
  { label: 'If-None-Match', value: 'ifNoneMatch' },
  { label: 'If-Modified-Since', value: 'ifModifiedSince' },
];
