import type { RequestBodyFormat } from '@/enums/apiEnum';

import type { ExecuteBinaryBody, KeyValueParam, ResponseDefinitionBody } from './common';

// mock 信息-匹配项
export interface MatchRuleItem {
  key: string;
  value: string;
  condition: string;
  description: string;
}
// mock 信息-响应内容
export interface MockResponse {
  statusCode: number;
  headers: KeyValueParam[];
  useApiResponse: boolean;
  apiResponseId?: string; // useApiResponse 为 true 时必填
  body: ResponseDefinitionBody;
}
// mock 信息-请求通用匹配规则
export interface MockMatchRuleCommon {
  matchRules: MatchRuleItem[];
  matchAll: boolean;
}
// mock 信息-请求体匹配规则
export interface MockBody {
  paramType: RequestBodyFormat;
  formDataMatch: MockMatchRuleCommon;
  binaryBody: ExecuteBinaryBody;
  raw: string;
}
// mock 信息-匹配规则集合
export interface MockMatchRule {
  header: MockMatchRuleCommon;
  query: MockMatchRuleCommon;
  rest: MockMatchRuleCommon;
  body: MockBody;
}
// mock 信息
export interface MockParams {
  id?: string;
  projectId: string;
  name: string;
  statusCode: number;
  tags: string[];
  mockMatchRule: MockMatchRule;
  response: MockResponse;
  apiDefinitionId: string;
  uploadFileIds: string[];
  linkFileIds: string[];
  // 前端扩展字段
  unSaved?: boolean;
}
