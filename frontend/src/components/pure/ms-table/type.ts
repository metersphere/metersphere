import { ColumnEditTypeEnum } from '@/enums/tableEnum';
import { TableColumnData, TableData, TableDraggable, TableChangeExtra } from '@arco-design/web-vue';

export interface MsPaginationI {
  current: number;
  pageSize: number;
  total: number;
  showPageSize?: boolean;
  showTotal?: boolean;
  showJumper?: boolean;
  hideOnSinglePage?: boolean;
  simple?: boolean;
}

export interface MsTableColumnData extends TableColumnData {
  // 是否可排序
  showDrag?: boolean;
  // 是否展示在表格上
  showInTable?: boolean;
  // 是否可编辑
  editable?: boolean;
  // 是否展示tooltip
  showTooltip?: boolean;
  // 启用Title 默认为启用
  enableTitle?: string;
  // 禁用Title 默认为禁用
  disableTitle?: string;
  // 当展示tooltip时，是否是Tag
  isTag?: boolean;
  // editType
  editType?: ColumnEditTypeEnum;
  // 撤销删除的组件
  revokeDeletedSlot?: string;
  // 表格列排序字段
  sortIndex?: number;
}

export type MsTableErrorStatus = boolean | 'error' | 'empty';
export type MsTableDataItem<T> = T & {
  updateTime?: string | number | null;
  createTime?: string | number | null;
} & TableData;
// 表格属性
export interface MsTableProps<T> {
  // 表格数据 - 详见 TableData  https://arco.design/web-vue/components/table-data;
  data: MsTableDataItem<T>[];
  // 表格key, 用于存储表格列配置
  tableKey: string;
  // 表格列 - 详见 TableColumn  https://arco.design/web-vue/components/table-column;
  columns: MsTableColumnData[];
  // 表格尺寸
  size?: 'mini' | 'small' | 'default' | 'large';
  // 表格是否可滚动
  scroll?: {
    x?: number | string;
    y?: number | string;
    maxHeight?: number | string;
    minWidth?: number | string;
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
  // 表格是否可固定表头
  fixedHeader?: boolean;
  // 表格是否可固定列
  fixedColumns?: boolean;
  // rowKey
  rowKey?: string;
  // loading
  loading?: boolean;
  bordered?: boolean;
  msPagination?: MsPaginationI;
  // 展示列表选择按钮
  showSetting?: boolean;
  // 分页是否是简单模式
  pageSimple?: boolean;
  // 是否展示禁用的行
  noDisable?: boolean;
  // 表格的错误状态，默认为false
  tableErrorStatus?: MsTableErrorStatus;
  // debug模式，开启后会打印表格所有state
  debug?: boolean;
  // 是否展示第一行的操作
  showFirstOperation?: boolean;
  [key: string]: any;
}

export interface MsTableSelectAll {
  value: boolean;
  total: number;
  current: number;
  type: 'all' | 'page';
}

// export type MsTableData = TableData[];
export type MsTableColumn = MsTableColumnData[];
export type MSTableChangeExtra = TableChangeExtra;

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

export interface renamePopconfirmVisibleType {
  [key: string]: boolean;
}
