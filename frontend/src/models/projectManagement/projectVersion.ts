// 项目版本公共信息
export interface ProjectCommon {
  name: string;
  description?: string;
  status?: boolean;
  publishTime?: number;
  latest?: boolean;
  projectId: string;
}

// 项目版本项
export interface ProjectItem extends ProjectCommon {
  createTime: number;
  createUser: string;
  id: string;
}
// 项目版本选项列表
export interface ProjectVersionOption {
  name: string;
  id: string;
  latest: boolean;
  enable: boolean;
}
