import { LocationQueryValue } from 'vue-router';

import { FormRule } from '@form-create/arco-design';

// 模版管理（组织）
export interface OrganizeTemplateItem {
  id: string;
  name: string;
  remark: string; // 备注
  internal: true; // 是否是内置模版
  updateTime: number;
  createTime: number;
  createUser: string;
  scopeType: string; // 组织或项目级别字段
  scopeId: string; // 组织或项目ID
  enableThirdPart: boolean; // 是否开启api字段名配置
  enableDefault: boolean; // 是否是默认模板
  refId: string; // 项目模板所关联的组织模板ID
  scene: string; // 使用场景
}

export type SeneType = 'FUNCTIONAL' | 'BUG' | 'API' | 'UI' | 'TEST_PLAN' | LocationQueryValue[] | LocationQueryValue;

export interface FieldOptions {
  fieldId?: string;
  value: string;
  text: string;
  internal?: boolean;
}
// 自定义字段
export interface DefinedFieldItem {
  id: string;
  name: string;
  scene: SeneType; // 使用场景
  type: string;
  remark: string;
  internal: boolean; // 是否是内置字段
  scopeType: string; // 组织或项目级别字段（PROJECT, ORGANIZATION）
  createTime: number;
  updateTime: number;
  createUser: string;
  refId: string | null; // 项目字段所关联的组织字段ID
  enableOptionKey: boolean | null; // 是否需要手动输入选项key
  scopeId: string; // 组织或项目ID
  options: FieldOptions[] | null;
  required?: boolean | undefined;
  fApi?: any; // 表单值
  formRules?: FormRule;
}

// 创建自定义字段

export interface FieldOption {
  value: string;
  text: string;
}
// 字段类型约束
export type fieldTypes =
  | 'INPUT'
  | 'TEXTAREA'
  | 'SELECT'
  | 'MULTIPLE_SELECT'
  | 'RADIO'
  | 'CHECKBOX'
  | 'MEMBER'
  | 'MULTIPLE_MEMBER'
  | 'DATE'
  | 'DATETIME'
  | 'INT'
  | 'FLOAT'
  | 'MULTIPLE_INPUT'
  | 'INT'
  | 'FLOAT'
  | 'NUMBER'
  | undefined;

// 新增 || 编辑参数
export interface AddOrUpdateField {
  id?: string;
  name: string;
  scene: SeneType; // 使用场景
  type: fieldTypes;
  remark: string; // 备注
  scopeId: string; // 组织或项目ID
  options?: FieldOption[];
}

export interface fieldIconAndNameModal {
  key: string;
  iconName: string; // 图标名称
  label: string; // 对应标签
}

// 模板管理列表(组织)
export interface OrdTemplateManagement {
  id: string;
  name: string;
  remark: string; // 描述
  internal: boolean; // 是否是系统模板
  updateTime: number;
  createTime: number;
  createUser: string; // 创建人
  scopeType: string; // 组织或项目级别字段
  scopeId: string; // 组织或项目ID
  enableThirdPart: boolean; // 是否开启api字段名配置
  enableDefault: boolean; // 是否是默认模板
  refId: string; // 项目模板所关联的组织模板ID
  scene: string; // 使用场景
}

// 创建模板& 更新模板管理项

export interface CustomField {
  fieldId: string;
  required: boolean; // 是否必填
  apiFieldId: string; // api字段名
  defaultValue: string; // 默认值
}

export interface ActionTemplateManage {
  id?: string;
  name: string;
  remark: string;
  scopeId: string;
  enableThirdPart?: boolean; // 是否开启api字段名配置
  scene: SeneType;
  customFields: CustomField[];
}
