import { ConditionType } from '@/models/apiTest/common';
import { RequestBodyFormat, RequestConditionProcessor } from '@/enums/apiEnum';

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
  { label: 'Accept', value: 'Accept' },
  { label: 'Accept-Encoding', value: 'Accept-Encoding' },
  { label: 'Accept-Language', value: 'Accept-Language' },
  { label: 'Cache-Control', value: 'Cache-Control' },
  { label: 'Content-Type', value: 'Content-Type' },
  { label: 'Content-Length', value: 'Content-Length' },
  { label: 'User-Agent', value: 'User-Agent' },
  { label: 'Referer', value: 'Referer' },
  { label: 'Cookie', value: 'Cookie' },
  { label: 'Authorization', value: 'Authorization' },
  { label: 'If-None-Match', value: 'If-None-Match' },
  { label: 'If-Modified-Since', value: 'If-Modified-Since' },
];
