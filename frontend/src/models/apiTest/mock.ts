import type { MsFileItem } from '@/components/pure/ms-upload/types';

import type { RequestBodyFormat, RequestParamsType } from '@/enums/apiEnum';

import type { BatchApiParams } from '../common';
import type {
  ExecuteBinaryBody,
  ExecuteJsonBody,
  ExecuteValueBody,
  KeyValueParam,
  ResponseDefinitionBody,
} from './common';

// mock 信息-匹配项
export interface MatchRuleItem {
  id?: string; // 用于前端标识
  paramType: RequestParamsType; // 用于前端标识
  key: string;
  value: string;
  condition: string;
  description: string;
  files: ({
    fileId: string;
    fileName: string;
    local: boolean; // 是否是本地上传的文件
    fileAlias: string; // 文件别名
    delete: boolean; // 是否删除
    [key: string]: any; // 用于前端渲染时填充的自定义信息，后台无此字段
  } & MsFileItem)[];
}
// mock 信息-响应内容
export interface MockResponse {
  statusCode: number;
  headers: KeyValueParam[];
  useApiResponse: boolean;
  apiResponseId?: string; // useApiResponse 为 true 时必填
  body: ResponseDefinitionBody;
  delay: number;
}
// mock 信息-请求通用匹配规则
export interface MockMatchRuleCommon {
  matchRules: MatchRuleItem[];
  matchAll: boolean;
}
// mock 信息-请求体匹配规则
export interface MockBody {
  bodyType: RequestBodyFormat;
  formDataBody: MockMatchRuleCommon;
  wwwFormBody: MockMatchRuleCommon;
  jsonBody: ExecuteJsonBody;
  xmlBody: ExecuteValueBody;
  rawBody: ExecuteValueBody;
  binaryBody: ExecuteBinaryBody;
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
  apiDefinitionId: string | number;
  uploadFileIds: string[];
  linkFileIds: string[];
  // 前端扩展字段
  unSaved?: boolean;
  isNew: boolean;
}
// mock 信息-更新
export interface UpdateMockParams extends MockParams {
  id: string;
  deleteFileIds: string[];
  unLinkFileIds: string[];
}
// mock 信息-详情
export interface MockDetail extends MockParams {
  id: string;
}
// 批量编辑 mock
export interface BatchEditMockParams extends BatchApiParams {
  type: 'Status' | 'Tags'; // 编辑类型
  tags: string[]; // 标签
  append: boolean; // 是否追加
  enable: boolean; // 是否启用
  protocols: string[]; // 协议集合
  clear?: boolean; // 是否清空标签
}
