// 计划分页
export interface TestPlanItem {
  id?: string;
  projectId: string;
  num: number;
  name: string;
  status: string;
  type: string;
  tags: string[];
  schedule: string; // 是否定时
  createUser: string;
  createTime: string;
  moduleName: string;
  moduleId: string;
  children: TestPlanItem[];
  childrenCount: number;
  groupId: string;
}

export interface ResourcesItem {
  id: string;
  name: string;
  cpuRate: string;
  status: boolean;
}

export default {};
