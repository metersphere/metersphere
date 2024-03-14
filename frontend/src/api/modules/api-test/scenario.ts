import MSR from '@/api/http/index';
import {
  AddModuleUrl,
  BatchCopyScenarioUrl,
  BatchEditScenarioUrl,
  BatchMoveScenarioUrl,
  BatchRecycleScenarioUrl,
  DeleteModuleUrl,
  GetModuleCountUrl,
  GetModuleTreeUrl,
  MoveModuleUrl,
  RecycleScenarioUrl,
  ScenarioPageUrl,
  UpdateModuleUrl,
  UpdateScenarioUrl,
} from '@/api/requrls/api-test/scenario';

import {
  ApiScenarioBatchDeleteParams,
  ApiScenarioBatchEditParams,
  ApiScenarioDetail,
  ApiScenarioGetModuleParams,
  ApiScenarioModuleUpdateParams,
  ApiScenarioPageParams,
  ApiScenarioUpdateDTO,
} from '@/models/apiTest/scenario';
import { AddModuleParams, CommonList, ModuleTreeNode, MoveModules } from '@/models/common';

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

// 获取接口场景列表
export function getScenarioPage(data: ApiScenarioPageParams) {
  return MSR.post<CommonList<ApiScenarioDetail>>({ url: ScenarioPageUrl, data });
}

// 更新接口场景
export function updateScenario(data: ApiScenarioUpdateDTO) {
  return MSR.post({ url: UpdateScenarioUrl, data });
}

// 删除场景
export function recycleScenario(id: string) {
  return MSR.get({ url: RecycleScenarioUrl, params: id });
}

// 批量删除场景
export function batchRecycleScenario(data: ApiScenarioBatchDeleteParams) {
  return MSR.post({ url: BatchRecycleScenarioUrl, data });
}

// 批量操作场景
export function batchOptionScenario(
  optionType: string,
  data: {
    moduleIds: string[];
    selectAll: boolean;
    condition: { keyword: string };
    excludeIds: any[];
    selectIds: any[];
    projectId: string;
    targetModuleId: string;
  }
) {
  if (optionType === 'batchMove') {
    return MSR.post({ url: BatchMoveScenarioUrl, data });
  }
  if (optionType === 'batchCopy') {
    return MSR.post({ url: BatchCopyScenarioUrl, data });
  }
}

// 批量编辑场景
export function batchEditScenario(params: ApiScenarioBatchEditParams) {
  return MSR.post({ url: BatchEditScenarioUrl, params });
}
