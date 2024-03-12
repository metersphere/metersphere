import MSR from '@/api/http/index';
import {
  AddModuleUrl,
  DeleteModuleUrl,
  GetModuleCountUrl,
  GetModuleTreeUrl,
  MoveModuleUrl,
  UpdateModuleUrl,
} from '@/api/requrls/api-test/scenario';

import { ApiScenarioGetModuleParams, ApiScenarioModuleUpdateParams } from '@/models/apiTest/scenario';
import {
  AddModuleParams,
  ModuleTreeNode,
  MoveModules,
} from '@/models/common';

// 更新模块
export function updateModule(data: ApiScenarioModuleUpdateParams) {
  return MSR.post({ url: UpdateModuleUrl, data });
}

// 获取模块树
export function getModuleTree(data: ApiScenarioGetModuleParams) {
  return MSR.post<ModuleTreeNode[]>({ url: GetModuleTreeUrl, data });
}

// 移动模块
export function moveModule(data: MoveModules) {
  return MSR.post({ url: MoveModuleUrl, data });
}


// 获取模块统计数量
export function getModuleCount(data: ApiScenarioGetModuleParams) {
  return MSR.post({ url: GetModuleCountUrl, data });
}

// 添加模块
export function addModule(data: AddModuleParams) {
  return MSR.post({ url: AddModuleUrl, data });
}

// 删除模块
export function deleteModule(id: string) {
  return MSR.get({ url: DeleteModuleUrl, params: id });
}
