import { LocationQueryValue } from 'vue-router';

import type { FormItem, FormItemType, FormRuleItem } from '@/components/pure/ms-form-create/types';

import { FormRule } from '@form-create/arco-design';

// 模板管理（组织）
export interface OrganizeTemplateItem {
  id: string;
  name: string;
  remark: string; // 备注
  internal: true; // 是否是内置模板
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
  value: any;
  text: string;
  internal?: boolean; // 是否是内置模板
  pos?: number; // 排序字段
}

// 自定义字段
export interface DefinedFieldItem {
  id: string;
  name: string;
  scene: SeneType; // 使用场景
  type: FormItemType; // 表单类型
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
  formRules?: FormRuleItem[] | FormItem[] | FormRule[]; // 表单列表
  [key: string]: any;
}

// 创建自定义字段

export interface FieldOption {
  value: string;
  text: string;
}

// 新增 || 编辑参数
export interface AddOrUpdateField {
  id?: string;
  name: string;
  used: boolean;
  scene: SeneType; // 使用场景
  type: FormItemType;
  remark: string; // 备注
  scopeId: string; // 组织或项目ID
  options?: FieldOptions[];
  enableOptionKey: boolean;
  [key: string]: any;
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
  required?: boolean; // 是否必填
  apiFieldId?: string; // api字段名
  defaultValue: string | (string | number)[] | number; // 默认值
  [key: string]: any;
}

export interface ActionTemplateManage {
  id?: string;
  name: string;
  remark: string;
  scopeId: string;
  enableThirdPart?: boolean; // 是否开启api字段名配置
  scene?: SeneType;
  customFields?: CustomField[];
  fieldType?: string;
  systemFields?: Record<string, any>[];
  enablePlatformDefault?: boolean;
  internal?: boolean; // 是否为系统模板
  platForm?: string;
  uploadImgFileIds: string[]; // 模板附件
  [key: string]: any;
}

// 工作流列表字段
export interface WorkFlowType {
  id: string;
  name: string;
  scene: string;
  remark: string;
  internal: true; // 是否是内置字段
  scopeType: string; // 组织或项目级别字段（PROJECT, ORGANIZATION)
  refId: string; // 项目状态所关联的组织状态ID
  scopeId: string;
  pos: number; // 排序字段
  statusDefinitions: string[]; // 设置初始状态
  statusFlowTargets: string[]; // 可流转状态
  [key: string]: any;
}

// 创建组织工作流状态
export interface OrdWorkStatus {
  scopeId?: string;
  id?: string;
  name: string;
  scene?: SeneType;
  remark: string;
  allTransferTo?: boolean; // 是否允许所有状态流转到该状态
}

// 状态列表
export type StateList = {
  statusId: string;
  definitionId: string;
};
// 设置工作流状态初始或结束
export interface SetStateType {
  statusId: string;
  definitionId: string;
  enable: boolean;
}

// 更新流转状态
export interface UpdateWorkFlowSetting {
  fromId: string; // 开始id
  toId: string; // 结束id
  enable: boolean;
}

// 默认缺陷模板系统自定义字段
export interface defaultBugField {
  title: string;
  description: string;
  descriptionFileIds: string[];
  [key: string]: any;
}

// 用例模模板默认自定义系统字段
export interface defaultCaseField {
  name: string;
  prerequisite: string; // 前置条件
  caseEditType: string; // 编辑模式：步骤模式/文本模式
  steps: string;
  textDescription: string;
  expectedResult: string; // 预期结果
  description: string;
  [key: string]: any;
}

export interface DetailCustomField extends CustomField {
  fieldId: string;
  fieldName: string;
  fieldKey: string;
  required: boolean;
  type: string;
  internal: boolean;
  internalFieldKey: string; // 系统字段标识 例如用例等级
  options: FieldOptions[];
  supportSearch?: boolean;
  optionMethod?: string;
  platformOptionJson?: string; // 三方平台下拉选项
  platformPlaceHolder?: string;
  platformSystemField?: any; // 三方平台字段
  apiFieldId?: string; // 三方api
}
