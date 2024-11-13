// 项目列表项
export interface ProjectListItem {
  id: string;
  num: number;
  organizationId: string;
  name: string;
  description: string;
  createTime: number;
  updateTime: number;
  updateUser: string;
  createUser: string;
  deleteTime: number;
  deleted: boolean;
  deleteUser: string;
  enable: boolean;
  moduleIds: string[];
  moduleSetting: string;
}
