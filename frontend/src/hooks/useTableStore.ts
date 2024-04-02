import { filter, orderBy, sortBy } from 'lodash-es';
import localforage from 'localforage';

import { MsTableColumn, MsTableColumnData } from '@/components/pure/ms-table/type';

import { useAppStore } from '@/store';
import { MsTableSelectorItem, PageSizeMap, TableOpenDetailMode } from '@/store/modules/components/ms-table/types';
import { isArraysEqualWithOrder } from '@/utils/equal';

import { SpecialColumnEnum } from '@/enums/tableEnum';

export default function useTableStore() {
  const state = reactive({
    baseSortIndex: 10,
    operationBaseIndex: 100,
  });

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
        item.sortIndex = 0;
      } else if (item.dataIndex === SpecialColumnEnum.NAME) {
        // dataIndex 为 name 的列默认不排序，且展示在列的第二位
        item.sortIndex = 1;
      } else if (item.dataIndex === SpecialColumnEnum.OPERATION || item.dataIndex === SpecialColumnEnum.ACTION) {
        // dataIndex 为 operation 或 action  的列默认不排序，且展示在列的最后面
        item.sortIndex = state.operationBaseIndex;
      }
    });
    return columns;
  };

  async function initColumn(
    tableKey: string,
    column: MsTableColumn,
    mode?: TableOpenDetailMode,
    showSubdirectory?: boolean
  ) {
    try {
      const tableColumnsMap = await localforage.getItem<MsTableSelectorItem>(tableKey);
      if (!tableColumnsMap) {
        // 如果没有在indexDB里初始化
        column = columnsTransform(column);
        localforage.setItem(tableKey, {
          mode,
          showSubdirectory,
          column,
          columnBackup: JSON.parse(JSON.stringify(column)),
        });
      } else {
        // 初始化过了，但是可能有新变动，如列的顺序，列的显示隐藏，列的拖拽
        column = columnsTransform(column);
        const { columnBackup: oldColumn } = tableColumnsMap;
        // 比较页面上定义的 column 和 浏览器备份的column 是否相同
        const isEqual = isArraysEqualWithOrder(oldColumn, column);
        if (!isEqual) {
          // 如果不相等，说明有变动将新的column存入indexDB
          localforage.setItem(tableKey, {
            mode,
            showSubdirectory,
            column,
            columnBackup: JSON.parse(JSON.stringify(column)),
          });
        }
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }
  async function setMode(key: string, mode: TableOpenDetailMode) {
    try {
      const tableColumnsMap = await localforage.getItem<MsTableSelectorItem>(key);
      if (tableColumnsMap) {
        tableColumnsMap.mode = mode;
        await localforage.setItem(key, tableColumnsMap);
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }

  async function setSubdirectory(key: string, val: boolean) {
    try {
      const tableColumnsMap = await localforage.getItem<MsTableSelectorItem>(key);
      if (tableColumnsMap) {
        tableColumnsMap.showSubdirectory = val;
        await localforage.setItem(key, tableColumnsMap);
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }

  async function setColumns(
    key: string,
    columns: MsTableColumn,
    mode?: TableOpenDetailMode,
    showSubdirectory?: boolean,
    isSimple?: boolean
  ) {
    try {
      columns.forEach((item, idx) => {
        if (item.showDrag) {
          item.sortIndex = state.baseSortIndex + idx;
        }
      });
      const tableColumnsMap = await localforage.getItem<MsTableSelectorItem>(key);
      if (!tableColumnsMap) {
        return;
      }
      if (isSimple) {
        const oldColumns = tableColumnsMap.column;
        const operationColumn = oldColumns.find((i) => i.dataIndex === SpecialColumnEnum.OPERATION);
        if (operationColumn) columns.push(operationColumn);
      }

      await localforage.setItem(key, {
        mode,
        showSubdirectory,
        column: JSON.parse(JSON.stringify(columns)),
        columnBackup: tableColumnsMap.columnBackup,
      });
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
    const tableColumnsMap = await localforage.getItem<MsTableSelectorItem>(key);
    if (tableColumnsMap) {
      return tableColumnsMap.mode;
    }
    return 'drawer';
  }

  async function getSubShow(key: string) {
    const tableColumnsMap = await localforage.getItem<MsTableSelectorItem>(key);
    if (tableColumnsMap) {
      return tableColumnsMap.showSubdirectory;
    }
    return true as boolean;
  }

  async function getColumns(key: string, isSimple?: boolean) {
    const tableColumnsMap = await localforage.getItem<MsTableSelectorItem>(key);
    if (tableColumnsMap) {
      const tmpArr = tableColumnsMap.column;
      const { nonSortableColumns, couldSortableColumns } = tmpArr.reduce(
        (result: { nonSortableColumns: MsTableColumnData[]; couldSortableColumns: MsTableColumnData[] }, item) => {
          if (isSimple && item.dataIndex === SpecialColumnEnum.OPERATION) {
            return result;
          }
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
    const tableColumnsMap = await localforage.getItem<MsTableSelectorItem>(key);
    if (tableColumnsMap) {
      const tmpArr: MsTableColumn = tableColumnsMap.column;
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
    setSubdirectory,
    setColumns,
    setPageSize,
    getMode,
    getSubShow,
    getColumns,
    getShowInTableColumns,
    getPageSize,
  };
}
