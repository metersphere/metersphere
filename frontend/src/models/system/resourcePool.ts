import type { OrganizationListItem } from './user';

export interface ResourcePoolItem {
  id: string;
  name: string;
  type: string;
  description: string;
  enable: boolean;
  createTime: number;
  updateTime: number;
  createUser: string;
  apiTest: boolean;
  loadTest: boolean;
  uiTest: boolean;
  serverUrl: string;
  deleted: boolean;
  configuration: string;
  organizationList: OrganizationListItem[];
  resources: string[];
}
