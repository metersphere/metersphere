import { ResponseDefinition } from './common';
import { ExecuteRequestParams } from './debug';

export interface ApiDefinitionCustomField {
  apiId: string;
  fieldId: string;
  value: string;
}

export interface ApiDefinitionCreateParams extends ExecuteRequestParams {
  response: ResponseDefinition;
  customFields: ApiDefinitionCustomField[];
}

export interface ApiDefinitionCustomFieldDetail {
  id: string;
  name: string;
  scene: string;
  type: string;
  remark: string;
  internal: boolean;
  scopeType: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  refId: string;
  enableOptionKey: boolean;
  scopeId: string;
  value: string;
  apiId: string;
  fieldId: string;
}

export interface ApiDefinitionDetail extends ApiDefinitionCreateParams {
  id: string;
  name: string;
  protocol: string;
  method: string;
  path: string;
  status: string;
  num: number;
  tags: string[];
  pos: number;
  projectId: string;
  moduleId: string;
  latest: boolean;
  versionId: string;
  refId: string;
  description: string;
  createTime: number;
  createUser: string;
  updateTime: number;
  updateUser: string;
  deleteUser: string;
  deleteTime: number;
  deleted: boolean;
  createUserName: string;
  updateUserName: string;
  deleteUserName: string;
  versionName: string;
  caseTotal: number;
  casePassRate: string;
  caseStatus: string;
  follow: boolean;
  customFields: ApiDefinitionCustomFieldDetail[];
}
