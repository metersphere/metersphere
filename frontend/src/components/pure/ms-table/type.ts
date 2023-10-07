import { ColumnEditTypeEnum, SelectAllEnum } from '@/enums/tableEnum';
import { TableQueryParams } from '@/models/common';
import { TableColumnData, TableData, TableDraggable, TableChangeExtra, TableExpandable } from '@arco-design/web-vue';

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
  tableKey: string; // 表格key, 用于存储表格列配置,pageSize等
  rowKey: string; // 表格行的key rowId
  // 表格列 - 详见 TableColumn  https://arco.design/web-vue/components/table-column;
  columns: MsTableColumnData[];
  showPagination?: boolean; // 是否显示分页
  size?: 'mini' | 'small' | 'default' | 'large'; // 表格尺寸
  scroll?: {
    x?: number | string;
    y?: number | string;
    maxHeight?: number | string;
    minWidth?: number | string;
  }; // 表格是否可滚动
  heightUsed?: number; // 已经使用的高度
  enableDrag?: boolean; // 表格是否可拖拽
  draggable?: TableDraggable;
  /** 选择器相关 */
  selectable?: boolean; // 是否显示选择器
  selectorType: 'none' | 'checkbox' | 'radio'; // 选择器类型
  selectedKeys: Set<string>; // 选中的key
  excludeKeys: Set<string>; // 排除的key
  selectorStatus: SelectAllEnum; // 选择器状态
  /** end */
  loading?: boolean; // 加载效果
  bordered?: boolean; // 是否显示边框
  msPagination?: MsPaginationI;
  showSetting?: boolean; // 展示列表选择按钮
  pageSimple?: boolean; // 分页是否是简单模式
  noDisable?: boolean; // 是否展示禁用的行
  tableErrorStatus?: MsTableErrorStatus; // 表格的错误状态，默认为false
  debug?: boolean; // debug模式，开启后会打印表格所有state
  showFirstOperation?: boolean; // 是否展示第一行的操作
  /** 展开行相关 */
  showExpand?: boolean; // 是否显示展开行
  expandedKeys?: string[]; // 显示的展开行、子树（受控模式）

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

export interface SetPaginationPrams {
  current: number;
  total?: number;
}

export interface BatchActionQueryParams {
  excludeIds?: string[]; // 排除的id
  selectedIds?: string[]; // 选中的id
  selectAll: boolean; // 是否跨页全选
  params?: TableQueryParams; // 查询参数
  currentSelectCount: number; // 当前选中的数量
}
