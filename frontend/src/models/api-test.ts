export interface ListItemI {
  id: number;
  type: string;
  receiver: string;
  title: string;
  status: string;
  createTime: number | string;
  operator: string;
  operation: string;
  resourceId: string;
  resourceType: string;
  resourceName: string;
  content: string;
}

export interface SortItem {
  [key: string]: string;
}
export interface FilterItem {
  [key: string]: any;
}
export interface ApiTestListI {
  [x: string]: any;
  pageSize: number;
  total: number;
  current: number;
  list: ListItemI;
}
