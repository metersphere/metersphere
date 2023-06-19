export interface UserGroupListItem {
  name: string;
  id: number;
  title?: string;
  authScope: string;
}
export interface UserOption {
  id: number;
  name: string;
  email: string;
}
export interface CustomMoreActionItem {
  eventKey: string;
  name: string;
}
export interface PopVisibleItem {
  [key: string]: boolean;
}

export type RenameType = 'rename' | 'auth';

export interface UserGroupItem {
  // 组ID
  id: string;
  // 组名称
  name: string;
  // 组描述
  description: string;
  // 是否是内置用户组
  internal: true;
  // 所属类型
  type: string;
  createTime: number;
  updateTime: number;
  // 创建人
  createUser: string;
  // 应用范围
  scopeId: string;
  // 自定义排序
  pos: number;
}
