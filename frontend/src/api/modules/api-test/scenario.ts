import type { CaseLevel } from '@/components/business/ms-case-associate/types';

import MSR from '@/api/http/index';
import {
  AddModuleUrl,
  AddScenarioUrl,
  BatchCopyScenarioUrl,
  BatchDeleteScenarioUrl,
  BatchEditScenarioUrl,
  BatchMoveScenarioUrl,
  BatchRecoverScenarioUrl,
  BatchRecycleScenarioUrl,
  BatchRunScenarioUrl,
  DebugScenarioUrl,
  DeleteModuleUrl,
  DeleteScenarioUrl,
  dragSortUrl,
  ExecuteHistoryUrl,
  ExecuteScenarioUrl,
  ExportScenarioUrl,
  FollowScenarioUrl,
  GetExportScenarioFileUrl,
  GetModuleCountUrl,
  GetModuleTreeUrl,
  GetScenarioBatchExportParamsUrl,
  GetScenarioStepUrl,
  GetScenarioUrl,
  GetStepProjectInfoUrl,
  GetSystemRequestUrl,
  GetTrashModuleCountUrl,
  GetTrashModuleTreeUrl,
  ImportScenarioUrl,
  MoveModuleUrl,
  RecoverScenarioUrl,
  RecycleScenarioUrl,
  ScenarioAssociateExportUrl,
  ScenarioBatchEditScheduleUrl,
  ScenarioBatchExportLogUrl,
  ScenarioCopyStepFilesUrl,
  ScenarioExportLogUrl,
  ScenarioHistoryUrl,
  ScenarioPageUrl,
  ScenarioScheduleConfigDeleteUrl,
  ScenarioScheduleConfigUrl,
  ScenarioStatisticsUrl,
  ScenarioStepTransferFileUrl,
  ScenarioTransferFileUrl,
  ScenarioTransferModuleOptionsUrl,
  ScenarioTrashPageUrl,
  ScenarioUploadTempFileUrl,
  StopExportScenarioUrl,
  UpdateModuleUrl,
  UpdateScenarioPriorityUrl,
  UpdateScenarioStatusUrl,
  UpdateScenarioUrl,
} from '@/api/requrls/api-test/scenario';

import { ExecuteConditionProcessor } from '@/models/apiTest/common';
import {
  ApiScenarioBatchDeleteParams,
  ApiScenarioBatchEditParams,
  ApiScenarioBatchOptionResult,
  ApiScenarioBatchRunParams,
  type ApiScenarioBatchScheduleConfig,
  ApiScenarioDebugRequest,
  ApiScenarioGetModuleParams,
  ApiScenarioModuleUpdateParams,
  ApiScenarioPageParams,
  ApiScenarioScheduleConfig,
  ApiScenarioTableItem,
  ApiScenarioUpdateDTO,
  ExecuteHistoryItem,
  ExecutePageParams,
  type ExportScenarioParams,
  type GetScenarioUnSaveStepParams,
  GetSystemRequestParams,
  type ImportScenarioParams,
  ImportSystemData,
  Scenario,
  ScenarioDetail,
  ScenarioHistoryItem,
  ScenarioHistoryPageParams,
  type ScenarioStatisticsItem,
  ScenarioStepResourceInfo,
} from '@/models/apiTest/scenario';
import {
  AddModuleParams,
  type BatchApiParams,
  CommonList,
  DragSortParams,
  ModuleTreeNode,
  MoveModules,
  TransferFileParams,
} from '@/models/common';
import { ApiScenarioStatus, ScenarioStepType } from '@/enums/apiEnum';

import type { RequestParam as CaseRequestParam } from '@/views/api-test/components/requestComposition/index.vue';
import type { RequestParam } from '@/views/api-test/scenario/components/common/customApiDrawer.vue';

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
  return MSR.post<CommonList<ApiScenarioTableItem>>({ url: ScenarioPageUrl, data });
}

// 获取回收站的接口场景列表
export function getTrashScenarioPage(data: ApiScenarioPageParams) {
  return MSR.post<CommonList<ApiScenarioTableItem>>({ url: ScenarioTrashPageUrl, data });
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
    condition: { keyword: string; filter: Record<string, any> };
    excludeIds: any[];
    selectIds: any[];
    projectId: string;
    targetModuleId: string;
  }
) {
  if (optionType === 'batchMove') {
    return MSR.post<ApiScenarioBatchOptionResult>({ url: BatchMoveScenarioUrl, data });
  }
  if (optionType === 'batchCopy') {
    return MSR.post<ApiScenarioBatchOptionResult>({ url: BatchCopyScenarioUrl, data });
  }
}

// 批量编辑场景
export function batchEditScenario(params: ApiScenarioBatchEditParams) {
  return MSR.post({ url: BatchEditScenarioUrl, params });
}

// 批量编辑场景
export function batchRunScenario(params: ApiScenarioBatchRunParams) {
  return MSR.post({ url: BatchRunScenarioUrl, params });
}

// 批量编辑场景
export function scenarioScheduleConfig(params: ApiScenarioScheduleConfig) {
  return MSR.post({ url: ScenarioScheduleConfigUrl, params });
}

// 删除定时任务配置
export function deleteScheduleConfig(id: string) {
  return MSR.get({ url: ScenarioScheduleConfigDeleteUrl, params: id });
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

// 添加场景
export function addScenario(params: Scenario) {
  return MSR.post({ url: AddScenarioUrl, params });
}

// 获取场景详情
export function getScenarioDetail(id: string | number) {
  return MSR.get<ScenarioDetail>({ url: GetScenarioUrl, params: id });
}

// 获取场景步骤详情
export function getScenarioStep(stepId: string | number) {
  return MSR.get<Partial<RequestParam & CaseRequestParam & ExecuteConditionProcessor>>({
    url: GetScenarioStepUrl,
    params: stepId,
  });
}

// 文件转存
export function transferFile(data: TransferFileParams) {
  return MSR.post({ url: ScenarioTransferFileUrl, data });
}

export function stepTransferFile(data: TransferFileParams) {
  return MSR.post({ url: ScenarioStepTransferFileUrl, data });
}

// 文件转存目录
export function getTransferOptions(projectId: string) {
  return MSR.get<ModuleTreeNode[]>({ url: ScenarioTransferModuleOptionsUrl, params: projectId });
}

// 上传文件
export function uploadTempFile(file: File) {
  return MSR.uploadFile({ url: ScenarioUploadTempFileUrl }, { fileList: [file] }, 'file');
}

// 场景调试
export function debugScenario(data: ApiScenarioDebugRequest) {
  return MSR.post({ url: DebugScenarioUrl, data });
}

// 场景执行
export function executeScenario(data: ApiScenarioDebugRequest) {
  return MSR.post({ url: ExecuteScenarioUrl, data });
}

// 获取导入的系统请求数据
export function getSystemRequest(data: GetSystemRequestParams) {
  return MSR.post<ApiScenarioTableItem[]>({ url: GetSystemRequestUrl, data });
}

// 关注/取消关注接口场景
export function followScenario(id: string | number) {
  return MSR.get({ url: FollowScenarioUrl, params: id });
}

// 更新场景状态
export function updateScenarioStatus(id: string | number, status: ApiScenarioStatus | undefined) {
  return MSR.get({ url: `${UpdateScenarioStatusUrl}/${id}/${status}` });
}

// 拖拽排序
export function dragSort(data: DragSortParams) {
  return MSR.post({ url: dragSortUrl, data });
}

export function updateScenarioPro(id: string | number, priority: CaseLevel | undefined) {
  return MSR.get({ url: `${UpdateScenarioPriorityUrl}/${id}/${priority}` });
}

// 获取跨项目信息
export function getStepProjectInfo(id: string, type: ScenarioStepType) {
  return MSR.get<ScenarioStepResourceInfo>({ url: `${GetStepProjectInfoUrl}/${id}`, params: { resourceType: type } });
}

// 场景导出报告日志记录
export function logScenarioReportExport(reportId: string) {
  return MSR.post({ url: `${ScenarioExportLogUrl}/${reportId}` });
}

// 场景导出报告日志记录
export function logScenarioReportBatchExport(data: BatchApiParams) {
  return MSR.post({ url: `${ScenarioBatchExportLogUrl}`, data });
}

// 场景导出报告id集合
export function getScenarioBatchExportParams(data: BatchApiParams) {
  return MSR.post({ url: `${GetScenarioBatchExportParamsUrl}`, data });
}

// 场景导出报告id集合
export function scenarioAssociateExport(data: ImportSystemData) {
  return MSR.post({ url: `${ScenarioAssociateExportUrl}`, data });
}

// 导入场景
export function importScenario(params: ImportScenarioParams) {
  return MSR.uploadFile({ url: ImportScenarioUrl }, { fileList: [params.file], request: params.request }, 'file');
}

// 导出场景
export function exportScenario(data: ExportScenarioParams, type: string) {
  return MSR.post({ url: `${ExportScenarioUrl}/${type}`, data });
}

// 停止导出场景
export function stopScenarioExport(taskId: string) {
  return MSR.get({ url: `${StopExportScenarioUrl}/${taskId}` });
}

// 获取导出的文件
export function getScenarioDownloadFile(projectId: string, fileId: string) {
  return MSR.get(
    {
      url: `${GetExportScenarioFileUrl}/${projectId}/${fileId}`,
      responseType: 'blob',
    },
    { isTransformResponse: false }
  );
}

// 批量编辑场景定时任务
export function scenarioBatchEditSchedule(data: ApiScenarioBatchScheduleConfig) {
  return MSR.post({ url: ScenarioBatchEditScheduleUrl, data });
}

// 场景执行率统计
export function getScenarioStatistics(data: string[]) {
  return MSR.post<ScenarioStatisticsItem[]>({ url: ScenarioStatisticsUrl, data });
}

// 复制步骤时复制文件
export function scenarioCopyStepFiles(data: GetScenarioUnSaveStepParams) {
  return MSR.post<Record<string, any>>({
    url: ScenarioCopyStepFilesUrl,
    data,
  });
}
