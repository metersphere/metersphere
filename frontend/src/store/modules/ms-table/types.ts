import { MsTableColumn } from '@/components/pure/ms-table/type';

export type TableOpenDetailMode = 'drawer' | 'new_window';

export interface MsTableSelectorItem {
  // 详情打开模式
  mode: TableOpenDetailMode;
  // 列配置
  column: MsTableColumn;
}
export interface MsTableState {
  selectorColumnMap?: Map<string, MsTableSelectorItem>;
}
