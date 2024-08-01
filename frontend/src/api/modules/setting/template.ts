import MSR from '@/api/http/index';
import {
  CreateFieldUrl,
  CreateOrganizeTemplateUrl,
  CreateProjectFieldUrl,
  CreateProjectTemplateUrl,
  DeleteFieldDetailUrl,
  DeleteOrganizeTemplateUrl,
  DeleteProjectFieldDetailUrl,
  DeleteProjectTemplateUrl,
  EnableOrOffTemplateUrl,
  GetDefinedFieldListUrl,
  GetDefinedProjectFieldListUrl,
  GetFieldDetailUrl,
  GetFieldProjectDetailUrl,
  getOrdTemplateStateUrl,
  GetOrganizeTemplateDetailUrl,
  GetOrganizeTemplateUrl,
  GetProjectTemplateDetailUrl,
  getProjectTemplateStateUrl,
  GetProjectTemplateUrl,
  OrdCreateFlowStatusUrl,
  OrdDeleteFlowStatusUrl,
  OrdSetStateUrl,
  OrdStateSortUrl,
  OrdUpdateFlowStatusUrl,
  OrdUpdateStateFlowUrl,
  OrdWorkFlowUrl,
  orgRichUploadImageUrl,
  ProjectCreateFlowStatusUrl,
  ProjectDeleteFlowStatusUrl,
  ProjectSetStateUrl,
  ProjectStateSortUrl,
  ProjectUpdateFlowStatusUrl,
  ProjectUpdateStateFlowUrl,
  ProjectWorkFlowUrl,
  proRichUploadImageUrl,
  SetProjectTemplateUrl,
  UpdateFieldUrl,
  UpdateOrganizeTemplateUrl,
  UpdateProjectFieldUrl,
  UpdateProjectTemplateUrl,
} from '@/api/requrls/setting/template';

import { TableQueryParams } from '@/models/common';
import type {
  ActionTemplateManage,
  AddOrUpdateField,
  OrdWorkStatus,
  SeneType,
  SetStateType,
  UpdateWorkFlowSetting,
  WorkFlowType,
} from '@/models/setting/template';

/** *
 * 模板(组织)
 */
// 获取模板列表(组织)
export function getOrganizeTemplateList(params: TableQueryParams) {
  return MSR.get({
    url: `${GetOrganizeTemplateUrl}/${params.organizationId}/${params.scene}`,
  });
}
// 获取模板详情(组织)
export function getOrganizeTemplateInfo(id: string) {
  return MSR.get({ url: `${GetOrganizeTemplateDetailUrl}/${id}` });
}
// 创建模板列表(组织)
export function createOrganizeTemplateInfo(data: ActionTemplateManage) {
  return MSR.post({ url: `${CreateOrganizeTemplateUrl}`, data });
}

// 编辑模板列表(组织)
export function updateOrganizeTemplateInfo(data: ActionTemplateManage) {
  return MSR.post({ url: `${UpdateOrganizeTemplateUrl}`, data });
}

// 获取模板列表的状态(组织)
export function getOrdTemplate(scopedId: string) {
  return MSR.get<Record<string, boolean>>({ url: `${getOrdTemplateStateUrl}/${scopedId}` });
}

// 获取模板列表的状态(项目)
export function getProTemplate(scopedId: string) {
  return MSR.get<Record<string, boolean>>({ url: `${getProjectTemplateStateUrl}/${scopedId}` });
}

// 删除模板(组织)
export function deleteOrdTemplate(id: string) {
  return MSR.get({ url: `${DeleteOrganizeTemplateUrl}/${id}` });
}
// 关闭组织模板||开启项目模板
export function enableOrOffTemplate(organizationId: string, scene: SeneType) {
  return MSR.get({ url: `${EnableOrOffTemplateUrl}/${organizationId}/${scene}` });
}

/** *
 * 自定义字段(组织)
 */
// 获取自定义字段列表(组织)
export function getFieldList(params: TableQueryParams) {
  return MSR.get({ url: `${GetDefinedFieldListUrl}/${params.scopedId}/${params.scene}` });
}

// 创建自定义字段(组织)
export function addOrUpdateOrdField(data: AddOrUpdateField) {
  if (data.id) {
    return MSR.post({ url: UpdateFieldUrl, data });
  }
  return MSR.post({ url: CreateFieldUrl, data });
}

// 删除自定义字段(组织)
export function deleteOrdField(id: string) {
  return MSR.get({ url: DeleteFieldDetailUrl, params: id });
}

// 获取自定义字段详情选项(组织)
export function getOrdFieldDetail(id: string) {
  return MSR.get({ url: GetFieldDetailUrl, params: id });
}

// 组织模板-工作流

// 获取组织下边模板工作流
export function getWorkFlowList(scopedId: string, scene: SeneType) {
  return MSR.get<WorkFlowType[]>({ url: `${OrdWorkFlowUrl}/${scopedId}/${scene}` });
}
// 创建工作流状态
export function createWorkFlowStatus(data: OrdWorkStatus) {
  return MSR.post<WorkFlowType[]>({ url: OrdCreateFlowStatusUrl, data });
}
// 更新工作流状态
export function updateWorkFlowStatus(data: OrdWorkStatus) {
  return MSR.post<WorkFlowType[]>({ url: OrdUpdateFlowStatusUrl, data });
}

// 删除工作流状态
export function deleteOrdWorkState(id: string) {
  return MSR.get({ url: OrdDeleteFlowStatusUrl, params: id });
}
// 设置工作流状态初始态 || 结束态
export function setOrdWorkState(data: SetStateType) {
  return MSR.post({ url: OrdSetStateUrl, data });
}
// 设置工作流状态排序
export function setOrdWorkStateSort(scopedId: string, scene: SeneType, data: string[]) {
  return MSR.post({ url: `${OrdStateSortUrl}/${scopedId}/${scene}`, data });
}
// 更新工作流流转状态
export function updateOrdWorkStateFlow(data: UpdateWorkFlowSetting) {
  return MSR.post({ url: `${OrdUpdateStateFlowUrl}`, data });
}

/** *
 * 自定义字段(项目)
 */
// 获取自定义字段列表(组织)
export function getProjectFieldList(params: TableQueryParams) {
  return MSR.get({ url: `${GetDefinedProjectFieldListUrl}/${params.scopedId}/${params.scene}` });
}

// 创建自定义字段(组织)
export function addOrUpdateProjectField(data: AddOrUpdateField) {
  if (data.id) {
    return MSR.post({ url: UpdateProjectFieldUrl, data });
  }
  return MSR.post({ url: CreateProjectFieldUrl, data });
}

// 删除自定义字段(组织)
export function deleteProjectField(id: string) {
  return MSR.get({ url: DeleteProjectFieldDetailUrl, params: id });
}

// 获取自定义字段详情选项(组织)
export function getProjectFieldDetail(id: string) {
  return MSR.get({ url: GetFieldProjectDetailUrl, params: id });
}

// 富文本编辑器上传图片文件
export function editorUploadFile(data: { fileList: File[] }, mode: 'organization' | 'project') {
  const url = mode === 'organization' ? orgRichUploadImageUrl : proRichUploadImageUrl;
  return MSR.uploadFile({ url }, { fileList: data.fileList }, '', false);
}

/** *
 * 模板(项目)
 */
// 获取模板列表(项目)
export function getProjectTemplateList(params: TableQueryParams) {
  return MSR.get({
    url: `${GetProjectTemplateUrl}/${params.projectId}/${params.scene}`,
  });
}
// 获取模板详情(项目)
export function getProjectTemplateInfo(id: string) {
  return MSR.get({ url: `${GetProjectTemplateDetailUrl}/${id}` });
}
// 创建模板列表(项目)
export function createProjectTemplateInfo(data: ActionTemplateManage) {
  return MSR.post({ url: `${CreateProjectTemplateUrl}`, data });
}

// 编辑模板列表(项目)
export function updateProjectTemplateInfo(data: ActionTemplateManage) {
  return MSR.post({ url: `${UpdateProjectTemplateUrl}`, data });
}

// 删除模板(项目)
export function deleteProjectTemplate(id: string) {
  return MSR.get({ url: `${DeleteProjectTemplateUrl}/${id}` });
}

// 设置默认项目模板
export function setDefaultTemplate(projectId: string, id: string) {
  return MSR.get({ url: `${SetProjectTemplateUrl}/${projectId}/${id}` });
}

// 项目模板-工作流

// 获取项目模板工作流
export function getProjectWorkFlowList(scopedId: string, scene: SeneType) {
  return MSR.get<WorkFlowType[]>({ url: `${ProjectWorkFlowUrl}/${scopedId}/${scene}` });
}
// 创建工作流状态
export function createProjectWorkFlowStatus(data: OrdWorkStatus) {
  return MSR.post<WorkFlowType[]>({ url: ProjectCreateFlowStatusUrl, data });
}
// 更新工作流状态
export function updateProjectWorkFlowStatus(data: OrdWorkStatus) {
  return MSR.post<WorkFlowType[]>({ url: ProjectUpdateFlowStatusUrl, data });
}

// 删除工作流状态
export function deleteProjectWorkState(id: string) {
  return MSR.get({ url: ProjectDeleteFlowStatusUrl, params: id });
}
// 设置工作流状态初始态 || 结束态
export function setProjectWorkState(data: SetStateType) {
  return MSR.post({ url: ProjectSetStateUrl, data });
}
// 设置工作流状态排序
export function setProjectWorkStateSort(scopedId: string, scene: SeneType, data: string[]) {
  return MSR.post({ url: `${ProjectStateSortUrl}/${scopedId}/${scene}`, data });
}
// 更新工作流流转状态
export function updateProjectWorkStateFlow(data: UpdateWorkFlowSetting) {
  return MSR.post({ url: `${ProjectUpdateStateFlowUrl}`, data });
}
