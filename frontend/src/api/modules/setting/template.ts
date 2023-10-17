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
  SetOrganizeTemplateUrl,
  UpdateFieldUrl,
  UpdateProjectTemplateUrl,
} from '@/api/requrls/setting/template';

import { CommonList, TableQueryParams } from '@/models/common';
import type { AddOrUpdateField, DefinedFieldItem, OrganizeTemplateItem } from '@/models/setting/template';

/** *
 * 模版
 */
// 获取模版列表(组织)
export function getOrganizeTemplateList(organizationId: string, scene: string) {
  return MSR.get<OrganizeTemplateItem[]>({ url: GetOrganizeTemplateUrl, params: `/${organizationId}/${scene}` });
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
