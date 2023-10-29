import MSR from '@/api/http/index';
import {
  CreateFieldUrl,
  CreateOrganizeTemplateUrl,
  DeleteFieldDetailUrl,
  DeleteOrganizeTemplateUrl,
  EnableOrOffTemplateUrl,
  GetDefinedFieldListUrl,
  GetFieldDetailUrl,
  GetOrganizeTemplateDetailUrl,
  GetOrganizeTemplateUrl,
  GetProjectTemplateDetailUrl,
  isEnableTemplateUrl,
  SetOrganizeTemplateUrl,
  UpdateFieldUrl,
  UpdateOrganizeTemplateUrl,
  UpdateProjectTemplateUrl,
} from '@/api/requrls/setting/template';

import { CommonList, TableQueryParams } from '@/models/common';
import type {
  ActionTemplateManage,
  AddOrUpdateField,
  DefinedFieldItem,
  OrganizeTemplateItem,
} from '@/models/setting/template';

/** *
 * 模版
 */
// 获取模版列表(组织)
export function getOrganizeTemplateList(params: TableQueryParams) {
  return MSR.get({ url: `${GetOrganizeTemplateUrl}/${params.organizationId}/${params.scene}` });
}
// 获取模版详情
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
// 是否启用组织XX模板
export function isEnableTemplate(organizationId: string) {
  return MSR.get<Record<string, boolean>>({ url: `${isEnableTemplateUrl}/${organizationId}` });
}
// 删除模板
export function deleteOrdTemplate(id: string) {
  return MSR.get({ url: `${DeleteOrganizeTemplateUrl}/${id}` });
}

/** *
 * 自定义字段(组织)
 */
// 获取自定义字段列表
export function getFieldList(params: TableQueryParams) {
  return MSR.get({ url: `${GetDefinedFieldListUrl}${params.organizationId}/${params.scene}` });
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

export default {};
