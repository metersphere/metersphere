import { RequestMethods } from '@/enums/apiEnum';

import { ExecuteApiRequestFullParams, ExecutePluginRequestParams } from './common';

// 保存接口调试入参
export interface SaveDebugParams {
  name: string;
  protocol: string;
  method: RequestMethods | string;
  path: string;
  projectId: string;
  moduleId: string;
  request: ExecuteApiRequestFullParams | ExecutePluginRequestParams;
  uploadFileIds: string[];
  linkFileIds: string[];
}
// 更新接口调试入参
export interface UpdateDebugParams extends Partial<SaveDebugParams> {
  id: string;
  deleteFileIds?: string[];
  unLinkFileIds?: string[];
}
// 更新模块入参
export interface UpdateDebugModule {
  id: string;
  name: string;
}
// 添加模块入参
export interface AddDebugModuleParams {
  projectId: string;
  name: string;
  parentId: string;
}
// 接口调试详情-请求参数
export interface DebugDetailRequest {
  stepId: string;
  resourceId: string;
  projectId: string;
  name: string;
  enable: boolean;
  children: string[];
  parent: string;
  polymorphicName: string;
}
// 接口调试详情
export interface DebugDetail {
  id: string;
  name: string;
  protocol: string;
  method: RequestMethods | string;
  path: string;
  projectId: string;
  moduleId: string;
  createTime: number;
  createUser: string;
  updateTime: number;
  updateUser: string;
  pos: number;
  request: DebugDetailRequest & (ExecuteApiRequestFullParams | ExecutePluginRequestParams);
  response: string;
}
