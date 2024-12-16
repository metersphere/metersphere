import { filter, orderBy, sortBy } from 'lodash-es';

import { MsTableColumn, MsTableColumnData } from '@/components/pure/ms-table/type';

import useAppStore from '@/store/modules/app';
import { MsTableSelectorItem, TableOpenDetailMode } from '@/store/modules/components/ms-table/types';
import { isArraysEqualWithOrder } from '@/utils/equal';

import { SpecialColumnEnum, TableKeyEnum } from '@/enums/tableEnum';

import useLocalForage from './useLocalForage';

export default function useTableStore() {
  const state = reactive({
    baseSortIndex: 10,
    operationBaseIndex: 100,
  });
  const { getItem, setItem } = useLocalForage();

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
    tableKey: TableKeyEnum,
    column: MsTableColumn,
    mode?: TableOpenDetailMode,
    showSubdirectory?: boolean
  ) {
    try {
      const tableColumnsMap = await getItem<MsTableSelectorItem>(
        tableKey,
        tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
      );
      if (!tableColumnsMap) {
        // 如果没有在indexDB里初始化
        column = columnsTransform(column);
        setItem(
          tableKey,
          {
            mode,
            showSubdirectory,
            column,
            columnBackup: JSON.parse(JSON.stringify(column)),
            pageSize: useAppStore().pageSize,
          },
          tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
        );
      } else {
        // 初始化过了，但是可能有新变动，如列的顺序，列的显示隐藏，列的拖拽
        column = columnsTransform(column);
        const { columnBackup: oldColumn, pageSize } = tableColumnsMap;
        // 比较页面上定义的 column 和 浏览器备份的column 是否相同
        const isEqual = isArraysEqualWithOrder(oldColumn, column);

        if (!isEqual) {
          column.forEach((col) => {
            const storedCol = tableColumnsMap.column.find((sc) => sc.dataIndex === col.dataIndex);
            if (storedCol) {
              col.width = storedCol.width; // 使用上一次拖拽存储的宽度，避免组件里边使用时候初始化到最初的列宽
            }
          });
          // 如果不相等，说明有变动将新的column存入indexDB
          setItem(
            tableKey,
            {
              mode,
              showSubdirectory,
              column,
              columnBackup: JSON.parse(JSON.stringify(column)),
              pageSize: pageSize || useAppStore().pageSize,
            },
            tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
          );
        }
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }

  async function setSubdirectory(tableKey: string, val: boolean) {
    try {
      const tableColumnsMap = await getItem<MsTableSelectorItem>(
        tableKey,
        tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
      );
      if (tableColumnsMap) {
        tableColumnsMap.showSubdirectory = val;
        await setItem(tableKey, tableColumnsMap, tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION'));
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }

  async function getSubShow(tableKey: TableKeyEnum) {
    const tableColumnsMap = await getItem<MsTableSelectorItem>(
      tableKey,
      tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
    );
    if (tableColumnsMap) {
      return tableColumnsMap.showSubdirectory;
    }
    return true as boolean;
  }

  async function setColumns(
    tableKey: TableKeyEnum,
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
      const tableColumnsMap = await getItem<MsTableSelectorItem>(
        tableKey,
        tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
      );
      if (!tableColumnsMap) {
        return;
      }
      if (isSimple) {
        const oldColumns = tableColumnsMap.column;
        const operationColumn = oldColumns.find((i) => i.dataIndex === SpecialColumnEnum.OPERATION);
        if (operationColumn) columns.push(operationColumn);
      }

      await setItem(
        tableKey,
        {
          mode,
          showSubdirectory,
          column: JSON.parse(JSON.stringify(columns)),
          columnBackup: tableColumnsMap.columnBackup,
          pageSize: tableColumnsMap.pageSize || useAppStore().pageSize,
        },
        tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
      );
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('tableStore.setColumns', e);
    }
  }

  // 获取存储的列
  async function getStoreColumns(tableKey: TableKeyEnum) {
    const tableColumnsMap = await getItem<MsTableSelectorItem>(
      tableKey,
      tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
    );
    if (tableColumnsMap) {
      return tableColumnsMap.column;
    }
    return [];
  }

  async function getColumns(tableKey: TableKeyEnum, isSimple?: boolean) {
    const tableColumnsMap = await getItem<MsTableSelectorItem>(
      tableKey,
      tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
    );
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

  async function getMode(tableKey: TableKeyEnum) {
    const tableColumnsMap = await getItem<MsTableSelectorItem>(
      tableKey,
      tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
    );
    if (tableColumnsMap) {
      return tableColumnsMap.mode;
    }
    return 'drawer';
  }

  async function setMode(tableKey: string, mode: TableOpenDetailMode) {
    try {
      const tableColumnsMap = await getItem<MsTableSelectorItem>(
        tableKey,
        tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
      );
      if (tableColumnsMap) {
        tableColumnsMap.mode = mode;
        await setItem(tableKey, tableColumnsMap, tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION'));
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }

  async function getShowInTableColumns(tableKey: TableKeyEnum) {
    const tableColumnsMap = await getItem<MsTableSelectorItem>(
      tableKey,
      tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
    );
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

  async function getPageSize(tableKey: TableKeyEnum) {
    const tableColumnsMap = await getItem<MsTableSelectorItem>(
      tableKey,
      tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
    );
    if (tableColumnsMap) {
      return tableColumnsMap.pageSize;
    }
    return useAppStore().pageSize;
  }
  async function setPageSize(tableKey: TableKeyEnum, pageSize: number) {
    try {
      const tableColumnsMap = await getItem<MsTableSelectorItem>(
        tableKey,
        tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
      );
      if (tableColumnsMap) {
        tableColumnsMap.pageSize = pageSize;
        await setItem(tableKey, tableColumnsMap, tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION'));
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }

  async function updateColumnWidth(tableKey: TableKeyEnum, column: MsTableColumn) {
    try {
      // 从 IndexedDB 获取当前存储的列配置
      const tableColumnsMap = await getItem<MsTableSelectorItem>(
        tableKey,
        tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
      );

      if (tableColumnsMap) {
        // 存储更新后的列配置到 IndexedDB
        await setItem(
          tableKey,
          {
            ...tableColumnsMap,
            column,
          },
          tableKey.startsWith('SYSTEM') || tableKey.startsWith('ORGANIZATION')
        );
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error('Error updating column width:', error);
    }
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
    getStoreColumns,
    updateColumnWidth,
  };
}
