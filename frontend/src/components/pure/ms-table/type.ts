import { TableColumnData, TableData, TableDraggable, TableChangeExtra } from '@arco-design/web-vue';

export interface MsPaginationI {
  current: number;
  pageSize: number;
  total: number;
  showPageSize: boolean;
}

// 表格属性
export interface MsTableProps {
  // 表格列 - 详见 TableColumn  https://arco.design/web-vue/components/table-column;
  columns: TableColumnData[];
  // 表格数据 - 详见 TableData  https://arco.design/web-vue/components/table-data;
  data: TableData[];
  // 表格尺寸
  size?: 'mini' | 'small' | 'default' | 'large';
  // 表格是否可滚动
  scroll?: {
    x?: number | string;
    y?: number | string;
  };
  // 表格是否可拖拽
  enableDrag?: boolean;
  draggable?: TableDraggable;
  // 表格是否可编辑
  editable?: boolean;
  // 表格是否可筛选
  filterable?: boolean;
  // 表格是否可排序
  sortable?: boolean;
  // 表格是否可选中
  selectable?: boolean;
  // 展示自定义全选
  showSelectAll?: boolean;
  // 表格是否可展开
  expandable?: boolean;
  // 表格是否可固定表头
  fixedHeader?: boolean;
  // 表格是否可固定列
  fixedColumns?: boolean;
  // rowKey
  rowKey?: string;
  // loading
  loading?: boolean;
  bordered?: boolean;
  // pagination
  pagination: MsPaginationI | boolean;
  [key: string]: any;
}

export interface MsTableSelectAll {
  value: boolean;
  total: number;
  current: number;
  type: 'all' | 'page';
}

export type MsTableData = TableData[];
export type MsTableColumn = TableColumnData[];
export type MSTableChangeExtra = TableChangeExtra;

// eslint-disable-next-line no-shadow
export enum SelectAllEnum {
  ALL = 'all',
  CURRENT = 'current',
  NONE = 'none',
}

export interface SortItem {
  [key: string]: string;
}

export interface BatchActionParams {
  label?: string;
  eventTag?: string;
  isDivider?: boolean;
  danger?: boolean;
}
export interface BatchActionConfig {
  baseAction: BatchActionParams[];
  moreAction?: BatchActionParams[];
}
