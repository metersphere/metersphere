import { filter, orderBy, sortBy } from 'lodash-es';

import { MsTableColumn, MsTableColumnData } from '@/components/pure/ms-table/type';

import { useAppStore } from '@/store';
import { PageSizeMap, SelectorColumnMap, TableOpenDetailMode } from '@/store/modules/ms-table/types';
import { isArraysEqualWithOrder } from '@/utils/equal';

import { SpecialColumnEnum } from '@/enums/tableEnum';

import localforage from 'localforage';

export default function useTableStore() {
  const state = reactive({
    baseSortIndex: 10,
    operationBaseIndex: 100,
  });

  const getSelectorColumnMap = async () => {
    try {
      const selectorColumnMap = await localforage.getItem<SelectorColumnMap>('selectorColumnMap');
      if (!selectorColumnMap) {
        return {};
      }
      return selectorColumnMap;
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
      return {};
    }
  };
  const getPageSizeMap = async () => {
    try {
      const pageSizeMap = await localforage.getItem<PageSizeMap>('pageSizeMap');
      if (!pageSizeMap) {
        return {};
      }
      return pageSizeMap;
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
      return {};
    }
  };

  const columnsTransform = (columns: MsTableColumn) => {
    columns.forEach((item, idx) => {
      if (item.sortIndex === undefined) {
        // 如果没有设置sortIndex，则默认按照顺序排序
        item.sortIndex = state.baseSortIndex + idx;
      }
      if (item.showDrag === undefined) {
        // 默认不可以拖拽
        item.showDrag = false;
      }
      if (item.showInTable === undefined) {
        // 默认在表格中展示
        item.showInTable = true;
      }
      if (item.dataIndex === SpecialColumnEnum.ID) {
        // dataIndex 为 id 的列默认不排序，且展示在列的最前面
        item.showDrag = false;
        item.sortIndex = 0;
      } else if (item.dataIndex === SpecialColumnEnum.NAME) {
        // dataIndex 为 name 的列默认不排序，且展示在列的第二位
        item.showDrag = false;
        item.sortIndex = 1;
      } else if (item.dataIndex === SpecialColumnEnum.OPERATION || item.dataIndex === SpecialColumnEnum.ACTION) {
        // dataIndex 为 operation 或 action  的列默认不排序，且展示在列的最后面
        item.showDrag = false;
        item.sortIndex = state.operationBaseIndex;
      }
    });
    return columns;
  };

  async function initColumn(tableKey: string, column: MsTableColumn, mode: TableOpenDetailMode) {
    try {
      const selectorColumnMap = await getSelectorColumnMap();
      if (!selectorColumnMap[tableKey]) {
        // 如果没有在indexDB里初始化
        column = columnsTransform(column);
        selectorColumnMap[tableKey] = { mode, column, columnBackup: JSON.parse(JSON.stringify(column)) };
        await localforage.setItem('selectorColumnMap', selectorColumnMap);
      } else {
        // 初始化过了，但是可能有新变动，如列的顺序，列的显示隐藏，列的拖拽
        column = columnsTransform(column);
        const { columnBackup: oldColumn } = selectorColumnMap[tableKey];
        const isEqual = isArraysEqualWithOrder<MsTableColumnData>(oldColumn, column);
        if (!isEqual) {
          // 如果不相等，说明有变动将新的column存入indexDB
          selectorColumnMap[tableKey] = { mode, column, columnBackup: JSON.parse(JSON.stringify(column)) };
          await localforage.setItem('selectorColumnMap', selectorColumnMap);
        }
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }
  async function setMode(key: string, mode: TableOpenDetailMode) {
    try {
      const selectorColumnMap = await getSelectorColumnMap();
      if (selectorColumnMap[key]) {
        const item = selectorColumnMap[key];
        if (item) {
          item.mode = mode;
        }
        await localforage.setItem('selectorColumnMap', selectorColumnMap);
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }
  async function setColumns(key: string, columns: MsTableColumn, mode: TableOpenDetailMode) {
    try {
      columns.forEach((item, idx) => {
        if (item.showDrag) {
          item.sortIndex = state.baseSortIndex + idx;
        }
      });
      const selectorColumnMap = await getSelectorColumnMap();
      if (!selectorColumnMap) {
        return;
      }
      selectorColumnMap[key] = {
        mode,
        column: JSON.parse(JSON.stringify(columns)),
        columnBackup: JSON.parse(JSON.stringify(columns)),
      };
      await localforage.setItem('selectorColumnMap', selectorColumnMap);
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('tableStore.setColumns', e);
    }
  }
  async function setPageSize(key: string, pageSize: number) {
    const pageSizeMap = await getPageSizeMap();
    pageSizeMap[key] = pageSize;
    await localforage.setItem('pageSizeMap', pageSizeMap);
  }

  async function getMode(key: string) {
    const selectorColumnMap = await getSelectorColumnMap();
    if (selectorColumnMap[key]) {
      return selectorColumnMap[key].mode;
    }
    return 'drawer';
  }
  async function getColumns(key: string) {
    const selectorColumnMap = await getSelectorColumnMap();
    if (selectorColumnMap[key]) {
      const tmpArr = selectorColumnMap[key].column;
      const { nonSortableColumns, couldSortableColumns } = tmpArr.reduce(
        (result: { nonSortableColumns: MsTableColumnData[]; couldSortableColumns: MsTableColumnData[] }, item) => {
          if (item.showDrag) {
            result.couldSortableColumns.push(item);
          } else {
            result.nonSortableColumns.push(item);
          }
          return result;
        },
        { nonSortableColumns: [], couldSortableColumns: [] }
      );
      return { nonSort: nonSortableColumns, couldSort: sortBy(couldSortableColumns, 'sortedIndex') };
    }
    return { nonSort: [], couldSort: [] };
  }
  async function getShowInTableColumns(key: string) {
    const selectorColumnMap = await getSelectorColumnMap();
    if (selectorColumnMap[key]) {
      const tmpArr: MsTableColumn = selectorColumnMap[key].column;
      return orderBy(
        filter(tmpArr, (i) => i.showInTable),
        ['sortIndex'],
        ['asc']
      ) as MsTableColumn;
    }
    return [];
  }
  async function getPageSize(key: string) {
    const pageSizeMap = await getPageSizeMap();
    if (pageSizeMap[key]) {
      return pageSizeMap[key];
    }
    return useAppStore().pageSize;
  }

  return {
    initColumn,
    setMode,
    setColumns,
    setPageSize,
    getMode,
    getColumns,
    getShowInTableColumns,
    getPageSize,
  };
}
