import { defineStore } from 'pinia';
import { MsTableSelectorItem, MsTableState, TableOpenDetailMode } from './types';
import { MsTableColumn } from '@/components/pure/ms-table/type';
import { parse, stringify } from '@/utils/serializeMap';

const msTableStore = defineStore('msTable', {
  // 开启数据持久化
  persist: {
    serializer: {
      deserialize: parse,
      serialize: stringify,
    },
  },
  state: (): MsTableState => ({
    selectorColumnMap: new Map<string, MsTableSelectorItem>(),
  }),
  actions: {
    initColumn(tableKey: string, column: MsTableColumn, mode: TableOpenDetailMode) {
      if (!this.selectorColumnMap.has(tableKey)) {
        const tmpMap = this.selectorColumnMap;
        column.forEach((item) => {
          if (item.showDrag === undefined) {
            item.showDrag = false;
          }
        });
        tmpMap.set(tableKey, { mode, column });
        this.selectorColumnMap = tmpMap;
      }
    },
    getMode(key: string): string {
      if (this.selectorColumnMap.has(key)) {
        return this.selectorColumnMap.get(key)?.mode || '';
      }
      return '';
    },
    setMode(key: string, mode: TableOpenDetailMode) {
      if (this.selectorColumnMap.has(key)) {
        const item = this.selectorColumnMap.get(key);
        if (item) {
          item.mode = mode;
        }
      }
    },
    getColumns(key: string): { nonSort: MsTableColumn; couldSort: MsTableColumn } {
      if (this.selectorColumnMap.has(key)) {
        const tmpArr = this.selectorColumnMap.get(key)?.column || [];
        const nonSortableColumns = tmpArr.filter((item) => !item.showDrag);
        const couldSortableColumns = tmpArr.filter((item) => !!item.showDrag);
        return { nonSort: nonSortableColumns, couldSort: couldSortableColumns };
      }
      return { nonSort: [], couldSort: [] };
    },
    setColumns(key: string, columns: MsTableColumn, mode: TableOpenDetailMode) {
      this.selectorColumnMap.set(key, { mode, column: columns });
    },
    getShowInTableColumns(key: string): MsTableColumn {
      if (this.selectorColumnMap.has(key)) {
        const tmpArr = this.selectorColumnMap.get(key)?.column;
        return tmpArr?.filter((item) => !!item.showInTable) || [];
      }
      return [];
    },
  },
});

export default msTableStore;
