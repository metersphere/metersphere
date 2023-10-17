import { LocationQueryValue } from 'vue-router';

// 模版管理
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
  refId: string; // 项目字段所关联的组织字段ID
  enableOptionKey: boolean; // 是否需要手动输入选项key
  scopeId: string; // 组织或项目ID
}

// 创建自定义字段

export interface FieldOption {
  value: string;
  text: string;
}

export interface AddOrUpdateField {
  id?: string;
  name: string;
  scene: SeneType; // 使用场景
  type: string;
  remark: string; // 备注
  scopeId: string; // 组织或项目ID
  options?: FieldOption[];
}
