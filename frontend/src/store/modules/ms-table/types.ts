import { MsTableColumn } from '@/components/pure/ms-table/type';

export type TableOpenDetailMode = 'drawer' | 'new_window';

export interface MsTableSelectorItem {
  // 详情打开模式
  mode: TableOpenDetailMode;
  // 列配置
  column: MsTableColumn;
  // 列配置的备份，用于比较当前定义的列配置是否和备份的列配置相同
  columnBackup: MsTableColumn;
}

export interface SelectorColumnMap {
  [key: string]: MsTableSelectorItem;
}
export interface PageSizeMap {
  [key: string]: number;
}
export interface MsTableState {
  // 列配置， 持久化
  selectorColumnMap: SelectorColumnMap;
  // 列排序基数
  baseSortIndex: number;
  // 操作列基数
  operationBaseIndex: number;
  // 分页大小
  pageSizeMap: PageSizeMap;
}
