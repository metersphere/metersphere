import { TagType } from '@/components/pure/ms-tag/ms-tag.vue';

import type { TableQueryParams } from '@/models/common';
import { ColumnEditTypeEnum, SelectAllEnum, TableKeyEnum } from '@/enums/tableEnum';
import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

import type {
  TableChangeExtra,
  TableColumnData,
  TableData,
  TableDraggable,
  TableRowSelection,
} from '@arco-design/web-vue';

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

export interface MsTableColumnFilterConfig {
  filterCheckedList?: string[]; // 筛选选中的值
  filterSlotName?: FilterSlotNameEnum; // 筛选组件的slotName @desc 定义枚举是为了table组件内的插槽的filterSlotName 可以精确的让外部组件使用的时候可以拿到插槽作用域的值
  options?: Record<string, any>[]; // 筛选数据
  valueKey?: string;
  labelKey?: string;
  mode?: 'static' | 'remote';
  emptyFilter?: boolean; // 是否空选项查询（包含未执行和排队中无状态）
  remoteMethod?: FilterRemoteMethodsEnum; // 加载选项的类型
  loadOptionParams?: Record<string, any>; // 请求下拉的参数
  firstLabelKey?: string;
  secondLabelKey?: string;
  disabledTooltip?: boolean;
}

export interface MsTableRowSelectionDisabledConfig {
  disabledChildren?: boolean; // 是否禁用子节点选择
  parentKey?: string; // 父节点Key
  checkStrictly?: boolean; // 父子节点选择是否关联，关联存在半选状态，不关联不存在，选择父即父，选择子即子
  disabledKey?: string; // 是否禁用选择
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
  // 为字符串Tag
  isStringTag?: boolean;
  // editType
  editType?: ColumnEditTypeEnum;
  // 撤销删除的组件
  revokeDeletedSlot?: string;
  // 表格列排序字段
  sortIndex?: number;
  // 筛选配置
  filterConfig?: MsTableColumnFilterConfig;
  // 列选择的title
  columnTitle?: string;
  // 是否是自定义字段
  isCustomParam?: boolean;
  // 插槽表格过滤筛选数据item
  filterItem?: any;
  // 是否标签编辑列
  allowEditTag?: boolean;
  tagPrimary?: TagType;
  // 自定义属性
  [key: string]: any;
}

export type MsTableErrorStatus = boolean | 'error' | 'empty';
export type MsTableDataItem<T> = T & {
  updateTime?: string | number | null;
  createTime?: string | number | null;
  children?: MsTableDataItem<T>[];
} & TableData;
// 表格属性
export interface MsTableProps<T> {
  // 表格数据 - 详见 TableData  https://arco.design/web-vue/components/table-data;
  data: MsTableDataItem<T>[];
  tableKey?: TableKeyEnum; // 表格key, 用于存储表格列配置,pageSize等
  rowKey: string; // 表格行的key rowId
  // 表格列 - 详见 TableColumn  https://arco.design/web-vue/components/table-column;
  columns: MsTableColumnData[];
  showPagination?: boolean; // 是否显示分页
  showSubdirectory?: boolean; // 是否显示子目录开关
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
  draggableCondition?: boolean; // 允许拖拽的条件
  /** 选择器相关 */
  selectable?: boolean; // 是否显示选择器
  selectorType: 'none' | 'checkbox' | 'radio'; // 选择器类型
  selectedKeys: Set<string>; // 选中的key，多选
  selectedKey: string; // 选中的key，单选
  excludeKeys: Set<string>; // 排除的key
  selectorStatus: SelectAllEnum; // 选择器状态
  showSelectorAll?: boolean; // 是否显示跨页全选选择器
  rowSelection?: TableRowSelection; // 行选择器
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
  emptyDataShowLine?: boolean; // 空数据是否显示 "-"
  showJumpMethod?: boolean; // 是否展示跳转方法
  isSimpleSetting?: boolean; // 是否是简单的设置
  onlyPageSize?: boolean; // 简单设置气泡下，是否只展示页码调整
  filterIconAlignLeft?: boolean; // 筛选图标是否靠左
  paginationSize?: 'small' | 'mini' | 'medium' | 'large';
  // 行选择器禁用配置
  rowSelectionDisabledConfig?: MsTableRowSelectionDisabledConfig;
  sorter?: Record<string, any>; // 排序
  hoverable?: boolean; // 是否展示hover效果
  isHiddenSetting?: boolean; // 是否隐藏设置
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
  children?: BatchActionParams[];
  permission?: string[];
  anyPermission?: string[];
}
export interface BatchActionConfig {
  baseAction: BatchActionParams[];
  moreAction?: BatchActionParams[];
}

export interface renamePopConfirmVisibleType {
  [key: string]: boolean;
}

export interface SetPaginationPrams {
  current: number;
  total?: number;
}

export interface BatchActionQueryParams {
  excludeIds?: string[]; // 排除的id
  selectedIds?: string[];
  selectAll: boolean; // 是否跨页全选
  params?: TableQueryParams; // 查询参数
  currentSelectCount?: number; // 当前选中的数量
  condition?: any; // 查询条件
  [key: string]: any;
}
