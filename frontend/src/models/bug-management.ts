import { FormItemType } from '@/components/pure/ms-form-create/types';

import { BatchApiParams } from './common';

export interface BugListItem {
  id: string; // 缺陷id
  num: string; // 缺陷编号
  title: string; // 缺陷名称
  severity: string; // 缺陷严重程度
  status: string; // 缺陷状态
  handleUser: string; // 缺陷处理人
  relationCaseCount: number; // 关联用例数
  platform: string; // 所属平台
  tag: string[]; // 缺陷标签
  createUser: string; // 创建人
  updateUser: string; // 更新人
  createTime: string; // 创建时间
  updateTime: string; // 更新时间
  deleted: boolean; // 删除标志
  testPlanId: string; // 测试计划ID
}

export interface BugOptionItem {
  value: string; // 缺陷id
  text: string; // 缺陷编号
}

export interface BugOptionListItem {
  userOption: BugOptionItem[]; // 处理人下拉选项
  handleUserOption: BugOptionItem[]; // 处理人下拉选项
  statusOption: BugOptionItem[]; // 缺陷状态下拉选项
}

export interface BugExportColumn {
  key: string; // 字段key
  text?: string; // 字段名称
  columnType?: string; // 字段类型
}
export interface BugExportParams extends BatchApiParams {
  bugExportColumns: BugExportColumn[]; // 导出字段
}
export interface BugEditCustomField {
  type: FormItemType; // 表单项类型
  fieldId: string;
  fieldName: string;
  value: string;
  platformOptionJson?: string; // 选项的 Json
  required: boolean;
  isMutiple?: boolean;
  options?: any[];
  defaultValue: string;
  platformSystemField: boolean;
  sourceType?: string;
  field?: string;
}
export interface BugEditFormObject {
  [key: string]: any;
}
export interface BugEditCustomFieldItem {
  id: string;
  name: string;
  type: string | undefined;
  value: string;
  text?: string | null; // 消息通知文本提示
}
export type BugBatchUpdateFiledType = 'single_select' | 'multiple_select' | 'tags' | 'input' | 'user_selector' | 'date';
export interface BugBatchUpdateFiledForm {
  attribute: string;
  value: string[];
  append: boolean;
  inputValue: string;
}

export interface CreateOrUpdateComment {
  id?: string;
  bugId: string;
  notifier: string;
  replyUser: string;
  parentId: string;
  content: string;
  event: string; // 任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)
}
export interface CustomFieldItemOptionItem {
  fieldId: string;
  value: string;
  text: string;
}
export interface CustomFieldItem {
  fieldId: string;
  fieldName: string;
  fieldKey: string;
  required: boolean;
  apiFieldId: string;
  defaultValue: string;
  type: string;
  options: CustomFieldItemOptionItem[];
  platformOptionJson: string;
  supportSearch: boolean;
  optionMethod: string;
  internal: boolean;
}

export interface OperationFile {
  id?: string;
  projectId: string;
  bugId: string;
  fileId?: string; // 文件id
  associated: boolean; // 是否是本地 还是关联
  moduleId?: string; // 文件转存模块id
}

export interface BugTemplateRequest {
  fromStatusId: string; // 缺陷当前状态
  platformBugKey: string; // 缺陷第三方平台Key
}
