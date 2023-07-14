import { MsTableColumn } from '@/components/pure/ms-table/type';

export type TableOpenDetailMode = 'drawer' | 'new_window';
export interface ModeItem {
  [key: string]: TableOpenDetailMode;
}
export interface MsTableSelectorColumn {
  [key: string]: MsTableColumn;
}
export interface MsTableState {
  modeMap?: ModeItem;
  selectorColumnMap?: MsTableSelectorColumn;
}
