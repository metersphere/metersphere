export interface ModelConfigParam {
  key: string;
  baseUrl: string;
}

export interface ModelSourceParam {
  id: string;
  type: string; // 类型：私有/公有
  enable: boolean; // 是否可取消
  modelType: string; // 模型类型： LLM ...
  ownerType: string; // 拥有者类型： 企业/个人
  orgId: string;
  configDTO: ModelConfigParam;
}

export interface ModelSourceDTO {
  id: string;
  name: string;
  baseName: string;
  apiKey: string;
  apiUrl: string;
  type: string;
  provider: string;
  avatar: string;
  permissionType: string;
  status: boolean;
  owner: string;
  ownerType: string;
  orgId: string;
}
