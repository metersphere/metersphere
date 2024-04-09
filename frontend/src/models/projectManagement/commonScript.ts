import { Language } from '@/components/pure/ms-code-editor/types';

export interface CommonScriptMenu {
  title: string;
  value: string;
  command?: string;
}

export interface CustomFuncColumnOptionItem {
  userOption: {
    value: string; // 缺陷id
    text: string; // 缺陷编号
  }[]; // 用户相关下拉选项
}
// 脚本列表
export interface CommonScriptItem {
  id: string;
  projectId: string;
  name: string;
  tags: string[];
  description: string;
  type: Language; // 脚本语言类型
  status: string; // 脚本状态（进行中/已完成)
  createTime: number;
  updateTime: number;
  createUser: string;
  updateUser: string;
  createUserName: string;
  params: string; // 参数列表
  script: string; // 函数体
  result: string; // 执行结果
}
// 新增或编辑
export interface AddOrUpdateCommonScript {
  id?: string;
  projectId: string;
  name: string;
  type: Language;
  status: string;
  tags: string[];
  description: string;
  params: string;
  script: string;
  result: string;
}

// 新增或编辑
export interface updateCommonScriptStatus {
  id?: string;
  status: string;
  projectId: string;
}

export interface ParamsRequestType {
  contentType: string;
  encode: boolean;
  id: string;
  max: number;
  min: number;
  mustContain: boolean; // 必填
  key: string;
  required: boolean;
  type: string;
  value: string;
}
export interface TestScriptType {
  type: string;
  params: {
    key: string;
    value: string;
    valid: boolean;
  }[];
  script: string;
  projectId: string;
}
// 变更历史详情
export interface changeHistory {
  id: string;
  projectId: string;
  createTime: string;
  createUser: string;
  sourceId: string;
  type: string;
  module: string;
  refId: string;
  createUserName: string;
  versionName: string;
}
