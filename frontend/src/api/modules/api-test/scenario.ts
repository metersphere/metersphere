import MSR from '@/api/http/index';
import {
  AddModuleUrl,
  BatchCopyScenarioUrl,
  BatchDeleteScenarioUrl,
  BatchEditScenarioUrl,
  BatchMoveScenarioUrl,
  BatchRecoverScenarioUrl,
  BatchRecycleScenarioUrl,
  DeleteModuleUrl,
  DeleteScenarioUrl,
  ExecuteHistoryUrl,
  GetModuleCountUrl,
  GetModuleTreeUrl,
  GetTrashModuleCountUrl,
  GetTrashModuleTreeUrl,
  MoveModuleUrl,
  RecoverScenarioUrl,
  RecycleScenarioUrl,
  ScenarioHistoryUrl,
  ScenarioPageUrl,
  ScenarioTrashPageUrl,
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
  ExecuteHistoryItem,
  ExecutePageParams,
  ScenarioHistoryItem,
  ScenarioHistoryPageParams,
} from '@/models/apiTest/scenario';
import { AddModuleParams, BatchApiParams, CommonList, ModuleTreeNode, MoveModules } from '@/models/common';

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

// 获取回收站模块统计数量
export function getTrashModuleCount(data: ApiScenarioGetModuleParams) {
  return MSR.post({ url: GetTrashModuleCountUrl, data });
}

// 获取回收站模块树
export function getTrashModuleTree(data: ApiScenarioGetModuleParams) {
  return MSR.post<ModuleTreeNode[]>({ url: GetTrashModuleTreeUrl, data });
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

// 获取回收站的接口场景列表
export function getTrashScenarioPage(data: ApiScenarioPageParams) {
  return MSR.post<CommonList<ApiScenarioDetail>>({ url: ScenarioTrashPageUrl, data });
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

// 场景执行历史接口
export function getExecuteHistory(data: ExecutePageParams) {
  return MSR.post<CommonList<ExecuteHistoryItem>>({ url: ExecuteHistoryUrl, data });
}

// 场景变更历史接口
export function getScenarioHistory(data: ScenarioHistoryPageParams) {
  return MSR.post<CommonList<ScenarioHistoryItem>>({ url: ScenarioHistoryUrl, data });
}

// 恢复场景
export function recoverScenario(id: string) {
  return MSR.get({ url: RecoverScenarioUrl, params: id });
}

// 批量恢复场景
export function batchRecoverScenario(data: {
  moduleIds: string[];
  selectAll: boolean;
  condition: { keyword: string };
  excludeIds: any[];
  selectIds: any[];
  projectId: string;
}) {
  return MSR.post({ url: BatchRecoverScenarioUrl, data });
}

// 恢复场景
export function deleteScenario(id: string) {
  return MSR.get({ url: DeleteScenarioUrl, params: id });
}

// 批量恢复场景
export function batchDeleteScenario(data: {
  moduleIds: string[];
  selectAll: boolean;
  condition: { keyword: string };
  excludeIds: any[];
  selectIds: any[];
  projectId: string;
}) {
  return MSR.post({ url: BatchDeleteScenarioUrl, data });
}
