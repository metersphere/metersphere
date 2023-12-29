import { FormItemType } from '@/components/pure/ms-form-create/types';

import { BatchApiParams } from './common';

export interface BugListItem {
  id: string; // 缺陷id
  num: string; // 缺陷编号
  name: string; // 缺陷名称
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
}
export interface BugEditFormObject {
  [key: string]: any;
}
export interface BugEditCustomFieldItem {
  id: string;
  name: string;
  type: string;
  value: string;
}
export type BugBatchUpdateFiledType = 'single_select' | 'multiple_select' | 'tag' | 'input' | 'user_selector' | 'date';
export interface BugBatchUpdateFiledForm {
  attribute: string;
  value: string[];
  append: boolean;
  inputValue: string;
}
export default {};
