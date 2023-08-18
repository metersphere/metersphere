export interface CreateOrUpdateSystemOrgParams {
  id?: string;
  name: string;
  description: string;
  memberIds: string[];
}
export interface CreateOrUpdateSystemProjectParams {
  id?: string;
  name: string;
  description: string;
  enable: boolean;
  userIds: string[];
  // 模块配置 后端需要的字段 JSON string
  moduleSetting?: string;
  // 前端展示的模块配置 string[]
  module?: string[];
  organizationId?: string;
}
