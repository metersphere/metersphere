// 请求返回结构
export default interface CommonReponse<T> {
  code: number;
  message: string;
  messageDetail: string;
  data: T;
}

// 表格查询
export interface TableQueryParams {
  // 当前页
  current: number;
  // 每页条数
  pageSize: number;
  // 排序仅针对单个字段
  sort?: object;
  // 排序仅针对单个字段
  sortString?: string;
  // 表头筛选
  filter?: object;
  // 查询条件
  keyword?: string;
  [key: string]: any;
}

export interface TableResult<T> {
  [x: string]: any;
  pageSize: number;
  total: number;
  current: number;
  list: T[];
}
