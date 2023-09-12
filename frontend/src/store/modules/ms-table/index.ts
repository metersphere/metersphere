import { defineStore } from 'pinia';
import { MsTableState, TableOpenDetailMode } from './types';
import { MsTableColumn, MsTableColumnData } from '@/components/pure/ms-table/type';
import { SpecialColumnEnum } from '@/enums/tableEnum';
import { orderBy, filter, cloneDeep } from 'lodash';

const msTableStore = defineStore('msTable', {
  // 开启数据持久化
  persist: {
    paths: ['selectorColumnMap'],
  },
  state: (): MsTableState => ({
    selectorColumnMap: {},
    baseSortIndex: 10,
    operationBaseIndex: 100,
  }),
  actions: {
    initColumn(tableKey: string, column: MsTableColumn, mode: TableOpenDetailMode) {
      if (!this.selectorColumnMap[tableKey]) {
        column.forEach((item, idx) => {
          if (item.sortIndex === undefined) {
            item.sortIndex = this.baseSortIndex + idx;
          }
          if (item.showDrag === undefined) {
            item.showDrag = false;
          }
          if (item.showInTable === undefined) {
            item.showInTable = true;
          }
          if (item.dataIndex === SpecialColumnEnum.ID) {
            item.showDrag = false;
            item.sortIndex = 0;
          }
          if (item.dataIndex === SpecialColumnEnum.NAME) {
            item.showDrag = false;
            item.sortIndex = 1;
          }
          if (item.dataIndex === SpecialColumnEnum.OPERATION || item.dataIndex === SpecialColumnEnum.ACTION) {
            item.showDrag = false;
            item.sortIndex = this.operationBaseIndex;
          }
        });
        this.selectorColumnMap[tableKey] = { mode, column };
      }
    },

    setMode(key: string, mode: TableOpenDetailMode) {
      if (this.selectorColumnMap[key]) {
        const item = this.selectorColumnMap[key];
        if (item) {
          item.mode = mode;
        }
      }
    },
    setColumns(key: string, columns: MsTableColumn, mode: TableOpenDetailMode) {
      columns.forEach((item, idx) => {
        if (item.showDrag) {
          item.sortIndex = this.baseSortIndex + idx;
        }
      });
      this.selectorColumnMap[key] = { mode, column: columns };
    },
  },
  getters: {
    getMode: (state) => {
      return (key: string) => {
        if (state.selectorColumnMap[key]) {
          return state.selectorColumnMap[key].mode;
        }
        return 'drawer';
      };
    },
    getColumns: (state) => {
      return (key: string) => {
        if (state.selectorColumnMap[key]) {
          const tmpArr = cloneDeep(state.selectorColumnMap[key].column);
          const nonSortableColumns = tmpArr.filter((item: MsTableColumnData) => !item.showDrag);
          const couldSortableColumns = tmpArr.filter((item: MsTableColumnData) => !!item.showDrag);
          return { nonSort: nonSortableColumns, couldSort: couldSortableColumns };
        }
        return { nonSort: [], couldSort: [] };
      };
    },
    getShowInTableColumns: (state) => {
      return (key: string) => {
        if (state.selectorColumnMap[key]) {
          const tmpArr: MsTableColumn = cloneDeep(state.selectorColumnMap[key].column);
          return orderBy(
            filter(tmpArr, (i) => i.showInTable),
            ['sortIndex'],
            ['asc']
          ) as MsTableColumn;
        }
        return [];
      };
    },
  },
});

export default msTableStore;
