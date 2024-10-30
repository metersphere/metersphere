import { ExecuteStatusEnum } from '@/enums/taskCenter';
// Node 类型资源信息
export interface NodesListItem {
  ip: string;
  port: string;
  concurrentNumber: number;
  singleTaskConcurrentNumber: number; // 单个任务最大并发数
}

// 应用组织id和name映射对象
export interface OrgIdNameMap {
  id: string;
  name: string;
}

// 资源池配置信息对象
export interface TestResourceDTO {
  nodesList: NodesListItem[]; // node资源
  ip: string; // k8s ip
  token: string; // k8s token
  namespace: string; // k8s 命名空间
  concurrentNumber: number; // k8s 最大并发数
  singleTaskConcurrentNumber: number; // 单个任务最大并发数
  podThreads: number; // k8s 单pod最大线程数
  jobDefinition: string; // k8s job自定义模板
  deployName: string; // k8s api测试部署名称
  uiGrid: string; // ui测试selenium-grid
  girdConcurrentNumber: number; // ui测试selenium-grid最大并发数
  orgIds: string[]; // 应用范围选择指定组织时的id集合
  orgIdNameMap: OrgIdNameMap[]; // 应用范围选择指定组织时的id和name映射
}

// 资源池信息对象
export interface ResourcePoolInfo {
  name: string; // 资源池名称
  description: string; // 资源池描述
  type: string; // 资源池类型
  enable: boolean; // 是否启用
  apiTest: boolean; // 是否支持api测试
  uiTest: boolean; // 是否支持ui测试
  serverUrl: string; // 资源池地址
  allOrg: boolean; // 是否应用范围选择全部组织
  testResourceDTO: TestResourceDTO; // 测试资源信息对象
}

// 资源池列表项对象
export interface ResourcePoolItem extends ResourcePoolInfo {
  id: string;
}

export type ResourcePoolDetail = Omit<ResourcePoolInfo, 'testResourceDTO'> & {
  id: string;
  deleted: boolean;
  testResourceReturnDTO: TestResourceDTO;
};

// 添加资源池参数对象
export type AddResourcePoolParams = Omit<ResourcePoolInfo, 'testResourceDTO'> & {
  testResourceDTO?: Partial<TestResourceDTO>;
};

// 更新资源池参数对象
export type UpdateResourcePoolParams = Omit<ResourcePoolInfo, 'testResourceDTO'> & {
  id: string;
  testResourceDTO?: Partial<TestResourceDTO>;
};
// 资源池容量列表项
export interface CapacityTaskItem {
  id: string;
  taskId: string;
  resourceId: string;
  resourceName: string;
  taskOrigin: string; // 任务来源（任务组下的任务id
  status: ExecuteStatusEnum; // 执行状态
  result: string; // 执行结果
  resourcePoolId: string;
  resourcePoolNode: string;
  resourceType: string; // 资源类型
  projectId: string;
  organizationId: string;
  threadId: string; // 线程id
  startTime: number;
  endTime: number;
  executor: string; // 执行人
  num: number;
  taskName: string;
  userName: string;
  resourcePoolName: string;
  triggerMode: string;
  lineNum: string;
}

export interface CapacityDetailType {
  concurrentNumber: number; // 最大并发数
  occupiedConcurrentNumber: number; // 剩余并发数
  memoryUsage: number; // 内存使用量
  cpuusage: number; // CPU占用量
}
