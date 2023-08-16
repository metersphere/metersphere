export interface OptionsItem {
  id: string;
  name: string;
}

export interface LogOptions {
  organizationList: OptionsItem[];
  projectList: OptionsItem[];
}

export interface LogItem {
  id: string;
  createUser: string;
  userName: string;
  projectId: string;
  projectName: string;
  organizationId: string;
  organizationName: string;
  module: string; // 操作对象
  type: string; // 操作类型
  content: string; // 操作名称
  createTime: number;
  sourceId: string;
}
