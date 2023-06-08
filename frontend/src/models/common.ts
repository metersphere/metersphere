// 请求返回结构
export default interface CommonReponse<T> {
  code: number;
  message: string;
  messageDetail: string;
  data: T;
}

// 表格查询
export interface QueryParams {
  // 当前页
  current: number;
  // 每页条数
  pageSize: number;
  // 排序仅针对单个字段
  sort?: object;
  // 表头筛选
  filter?: object;
  // 查询条件
  keyword?: string;
  [key: string]: any;
}
