import { type Languages } from '@/components/business/ms-common-script/utils';

export interface CommonScriptMenu {
  title: string;
  value: string;
  command?: string;
}

// 脚本列表
export interface CommonScriptItem {
  id: string;
  projectId: string;
  name: string;
  tags: string[];
  description: string;
  type: Languages; // 脚本语言类型
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
  type: Languages;
  status: string;
  tags: string[];
  description: string;
  params: string;
  script: string;
  result: string;
}

export interface ParamsRequestType {
  contentType: string;
  encode: boolean;
  id: string;
  max: number;
  min: number;
  mustContain: boolean; // 必填
  name: string;
  required: boolean;
  type: string;
  value: string;
}
