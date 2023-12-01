import MSR from '@/api/http/index';
import {
  AddDemandUrl,
  BatchAssociationDemandUrl,
  BatchCopyCaseUrl,
  BatchDeleteCaseUrl,
  BatchDeleteRecycleCaseListUrl,
  BatchEditCaseUrl,
  BatchMoveCaseUrl,
  CancelAssociationDemandUrl,
  CreateCaseModuleTreeUrl,
  CreateCaseUrl,
  DeleteCaseModuleTreeUrl,
  DeleteCaseUrl,
  DeleteRecycleCaseListUrl,
  DetailCaseUrl,
  FollowerCaseUrl,
  GetAssociatedFilePageUrl,
  GetCaseListUrl,
  GetCaseModulesCountUrl,
  GetCaseModuleTreeUrl,
  GetDefaultTemplateFieldsUrl,
  GetDemandListUrl,
  GetRecycleCaseListUrl,
  GetRecycleCaseModulesCountUrl,
  GetTrashCaseModuleTreeUrl,
  MoveCaseModuleTreeUrl,
  RecoverRecycleCaseListUrl,
  RestoreCaseListUrl,
  UpdateCaseModuleTreeUrl,
  UpdateCaseUrl,
  UpdateDemandUrl,
} from '@/api/requrls/case-management/featureCase';

import type {
  AssociatedList,
  BatchDeleteType,
  BatchEditCaseType,
  BatchMoveOrCopyType,
  CaseManagementTable,
  CaseModuleQueryParams,
  CreateOrUpdateDemand,
  CreateOrUpdateModule,
  DeleteCaseType,
  DemandItem,
  ModulesTreeType,
  MoveModules,
  UpdateModule,
} from '@/models/caseManagement/featureCase';
import type { CommonList, TableQueryParams } from '@/models/common';
// 获取模块树
export function getCaseModuleTree(projectId: string) {
  return MSR.get<ModulesTreeType[]>({ url: `${GetCaseModuleTreeUrl}/${projectId}` });
}

// 创建模块树
export function createCaseModuleTree(data: CreateOrUpdateModule) {
  return MSR.post({ url: CreateCaseModuleTreeUrl, data });
}

// 更新模块树
export function updateCaseModuleTree(data: UpdateModule) {
  return MSR.post({ url: UpdateCaseModuleTreeUrl, data });
}

// 移动模块树
export function moveCaseModuleTree(data: MoveModules) {
  return MSR.post({ url: MoveCaseModuleTreeUrl, data });
}

// 回收站-模块-获取模块树
export function getTrashCaseModuleTree(projectId: string) {
  return MSR.get<ModulesTreeType[]>({ url: `${GetTrashCaseModuleTreeUrl}/${projectId}` });
}

// 删除模块
export function deleteCaseModuleTree(id: string) {
  return MSR.get({ url: `${DeleteCaseModuleTreeUrl}/${id}` });
}

// 用例分页表
export function getCaseList(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetCaseListUrl, data });
}
// 删除用例
export function deleteCaseRequest(data: DeleteCaseType) {
  return MSR.post({ url: `${DeleteCaseUrl}`, data });
}
// 获取默认模版自定义字段
export function getCaseDefaultFields(projectId: string) {
  return MSR.get({ url: `${GetDefaultTemplateFieldsUrl}/${projectId}` });
}
// 获取关联文件列表
export function getAssociatedFileListUrl(data: TableQueryParams) {
  return MSR.post<CommonList<AssociatedList>>({ url: GetAssociatedFilePageUrl, data });
}
// 关注用例
export function followerCaseRequest(data: { userId: string; functionalCaseId: string }) {
  return MSR.post({ url: FollowerCaseUrl, data });
}
// 创建用例
export function createCaseRequest(data: Record<string, any>) {
  return MSR.uploadFile({ url: CreateCaseUrl }, { request: data.request, fileList: data.fileList }, '', true);
}
// 编辑用例
export function updateCaseRequest(data: Record<string, any>) {
  return MSR.uploadFile({ url: UpdateCaseUrl }, { request: data.request, fileList: data.fileList }, '', true);
}
// 用例详情
export function getCaseDetail(id: string) {
  return MSR.get({ url: `${DetailCaseUrl}/${id}` });
}
// 批量删除用例
export function batchDeleteCase(data: BatchDeleteType) {
  return MSR.post({ url: `${BatchDeleteCaseUrl}`, data });
}
// 批量编辑属性
export function batchEditAttrs(data: BatchEditCaseType) {
  return MSR.post({ url: `${BatchEditCaseUrl}`, data });
}
// 批量移动到模块
export function batchMoveToModules(data: BatchMoveOrCopyType) {
  return MSR.post({ url: `${BatchMoveCaseUrl}`, data });
}

// 批量复制到模块
export function batchCopyToModules(data: BatchMoveOrCopyType) {
  return MSR.post({ url: `${BatchCopyCaseUrl}`, data });
}

// 回收站

// 回收站用例分页表
export function getRecycleListRequest(data: TableQueryParams) {
  return MSR.post<CommonList<CaseManagementTable>>({ url: GetRecycleCaseListUrl, data });
}
// 获取回收站模块数量
export function getRecycleModulesCounts(data: CaseModuleQueryParams) {
  return MSR.post({ url: GetRecycleCaseModulesCountUrl, data });
}
// 获取全部用例模块数量
export function getCaseModulesCounts(data: CaseModuleQueryParams) {
  return MSR.post({ url: GetCaseModulesCountUrl, data });
}
// 批量恢复回收站用例表
export function restoreCaseList(data: BatchMoveOrCopyType) {
  return MSR.post({ url: RestoreCaseListUrl, data });
}
// 批量彻底删除回收站用例表
export function batchDeleteRecycleCase(data: BatchMoveOrCopyType) {
  return MSR.post({ url: BatchDeleteRecycleCaseListUrl, data });
}
// 恢复回收站单个用例
export function recoverRecycleCase(id: string) {
  return MSR.get({ url: `${RecoverRecycleCaseListUrl}/${id}` });
}
// 删除回收站单个用例
export function deleteRecycleCaseList(id: string) {
  return MSR.get({ url: `${DeleteRecycleCaseListUrl}/${id}` });
}

// 关联需求

// 已关联需求列表
export function getDemandList(data: TableQueryParams) {
  return MSR.post<CommonList<DemandItem[]>>({ url: GetDemandListUrl, data });
}

// 添加需求
export function addDemandRequest(data: CreateOrUpdateDemand) {
  return MSR.post({ url: AddDemandUrl, data });
}

// 更新需求
export function updateDemand(data: CreateOrUpdateDemand) {
  return MSR.post({ url: UpdateDemandUrl, data });
}
// 批量关联需求
export function batchAssociationDemand(data: CreateOrUpdateDemand) {
  return MSR.post({ url: BatchAssociationDemandUrl, data });
}

// 取消关联
export function cancelAssociationDemand(id: string) {
  return MSR.get({ url: `${CancelAssociationDemandUrl}/${id}` });
}

export default {};
