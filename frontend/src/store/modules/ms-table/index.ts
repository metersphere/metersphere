import { defineStore } from 'pinia';
import { MsTableSelectorItem, MsTableState, TableOpenDetailMode } from './types';
import userGroupUsercolumns from './module/system/usergroup';
import { TableKeyEnum } from '@/enums/tableEnum';
import { MsTableColumn } from '@/components/pure/ms-table/type';

const msTableStore = defineStore('msTable', {
  // 开启数据持久化
  persist: true,
  state: (): MsTableState => ({
    selectorColumnMap: new Map<string, MsTableSelectorItem>(),
  }),
  actions: {
    initColumn() {
      const tmpMap = new Map<string, MsTableSelectorItem>();
      tmpMap.set(TableKeyEnum.USERGROUPUSER, {
        mode: 'drawer',
        column: userGroupUsercolumns,
      });
      this.selectorColumnMap = tmpMap;
    },
    getMode(key: string): string {
      if (this.selectorColumnMap?.has(key)) {
        return this.selectorColumnMap.get(key)?.mode || '';
      }
      return '';
    },
    setMode(key: string, mode: TableOpenDetailMode) {
      if (this.selectorColumnMap?.has(key)) {
        const item = this.selectorColumnMap.get(key);
        if (item) {
          item.mode = mode;
        }
      }
    },
    getColumns(key: string): { nonSortableColumns: MsTableColumn; couldSortableColumns: MsTableColumn } {
      if (this.selectorColumnMap?.has(key)) {
        const tmpArr = this.selectorColumnMap.get(key)?.column || [];
        const sortArr = tmpArr.sort((a, b) => {
          if (a.priority && b.priority) {
            return a.priority - b.priority || 0;
          }
          return 0;
        });
        const nonSortableColumns = sortArr.filter((item) => !item.showDrag);
        const couldSortableColumns = sortArr.filter((item) => item.showDrag);
        return { nonSortableColumns, couldSortableColumns };
      }
      return { nonSortableColumns: [], couldSortableColumns: [] };
    },
  },
});

export default msTableStore;
