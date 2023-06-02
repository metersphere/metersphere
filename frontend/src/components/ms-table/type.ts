import { TableColumnData, TableData, TableDraggable } from '@arco-design/web-vue';

export interface MsPaginationI {
  pageSize?: number;
  total?: number;
  current?: number;
}

// 表格属性
export interface MsTabelProps extends MsPaginationI {
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
  draggable?: TableDraggable;
  // 表格是否可编辑
  editable?: boolean;
  // 表格是否可筛选
  filterable?: boolean;
  // 表格是否可排序
  sortable?: boolean;
  // 表格是否可选中
  selectable?: boolean;
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
  pagination?: MsPaginationI;
  [key: string]: any;
}

export type MsTableData = TableData[];
export type MsTableColumn = TableColumnData[];
