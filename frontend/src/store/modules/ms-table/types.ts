import { MsTableColumn } from '@/components/pure/ms-table/type';

export type TableOpenDetailMode = 'drawer' | 'new_window';

export interface MsTableSelectorItem {
  // 详情打开模式
  mode: TableOpenDetailMode;
  // 列配置
  column: MsTableColumn;
}

export interface SelectorColumnMap {
  [key: string]: MsTableSelectorItem;
}
export interface MsTableState {
  // 列配置， 持久化
  selectorColumnMap: SelectorColumnMap;
  // 列排序基数
  baseSortIndex: number;
  // 操作列基数
  operationBaseIndex: number;
}
