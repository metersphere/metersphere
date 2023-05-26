export interface Pagination {
  current: number;
  pageSize: number;
  total: number;
  showPageSize: boolean;
}

export interface PaginationRes<T> {
  current: number;
  pageSize: number;
  total: number;
  list: T[];
}
