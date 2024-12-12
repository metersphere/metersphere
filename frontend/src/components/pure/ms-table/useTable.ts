// 核心的封装方法，详细参数看文档  https://arco.design/vue/component/table
// hook/table-props.ts

import { ref, watchEffect } from 'vue';
import { cloneDeep } from 'lodash-es';
import dayjs from 'dayjs';

import { useAppStore, useTableStore } from '@/store';

import type { CommonList, TableQueryParams } from '@/models/common';
import { SelectAllEnum } from '@/enums/tableEnum';

import { FilterResult } from '../ms-advance-filter/type';
import type { MsTableColumn, MsTableDataItem, MsTableErrorStatus, MsTableProps, SetPaginationPrams } from './type';
import type { TableData } from '@arco-design/web-vue';

export interface Pagination {
  current: number;
  pageSize: number;
  total: number;
}

const appStore = useAppStore();
const tableStore = useTableStore();
export default function useTableProps<T>(
  loadListFunc?: (v?: TableQueryParams | any) => Promise<CommonList<MsTableDataItem<T>> | MsTableDataItem<T>>,
  props?: Partial<MsTableProps<T>>,
  // 数据处理的回调函数
  dataTransform?: (item: TableData) => (TableData & T) | any,
  // 编辑操作的保存回调函数
  saveCallBack?: (item: T) => Promise<any>
) {
  const defaultHeightUsed = 286;
  // 底部操作栏的height和marginTop
  const defaultProps: MsTableProps<T> = {
    bordered: true, // 是否显示边框
    showPagination: true, // 是否显示分页
    showSubdirectory: false, // 是否显示子目录开关
    size: 'default', // 表格大小
    heightUsed: defaultHeightUsed, // 表格所在的页面已经使用的高度
    scroll: { x: 1400, y: appStore.innerHeight - defaultHeightUsed }, // 表格滚动配置
    loading: false, // 加载效果
    data: [], // 表格数据
    /**
     * 表格列配置
     * 当showSetting为true时，此配置无效,通过 await tableStore.initColumn(tableKey: string, column: MsTableColumn)初始化。
     * 当showSetting为false时，此配置生效
     */
    columns: [] as MsTableColumn,
    rowKey: 'id', // 表格行的key
    /** 选择器相关 */
    rowSelection: undefined, // 禁用表格默认的选择器
    selectable: false, // 是否显示选择器
    selectorType: 'checkbox', // 选择器类型
    selectedKeys: new Set<string>(), // 选中的key, 多选
    selectedKey: '', // 选中的key，单选
    excludeKeys: new Set<string>(), // 排除的key
    selectorStatus: SelectAllEnum.NONE, // 选择器状态
    showSelectorAll: true, // 是否显示全选
    /** end */
    showSetting: false, // 是否展示列选择器
    columnResizable: true,
    pagination: false, // 禁用 arco-table 的分页
    pageSimple: false, // 简易分页模式
    tableErrorStatus: false, // 表格的错误状态
    debug: false, // debug 模式
    showFirstOperation: false, // 展示第一行的操作
    /** 展开行相关 */
    showExpand: false, // 是否显示展开行
    emptyDataShowLine: true, // 空数据是否显示 "-"
    /** Column Selector */
    showJumpMethod: false, // 是否显示跳转方法
    isSimpleSetting: false, // 是否是简易column设置
    filterIconAlignLeft: true, // 筛选图标是否靠左
    filter: {}, // 筛选条件
    ...props,
  };

  // 属性组
  const propsRes = ref<MsTableProps<T>>(cloneDeep(defaultProps));

  // 排序
  const sortItem = ref<object>({});

  // 筛选
  const filterItem = ref<Record<string, any>>({});

  // keyword
  const keyword = ref('');
  // 高级筛选
  const advanceFilter = reactive<FilterResult>({ searchMode: 'AND', conditions: [] });
  // 视图Id
  const viewId = ref('');
  // 表格请求参数集合
  const tableQueryParams = ref<TableQueryParams>({});

  // 是否分页
  if (propsRes.value.showPagination) {
    propsRes.value.msPagination = {
      current: 1,
      pageSize: appStore.pageSize,
      total: 0,
      showPageSize: appStore.showPageSize,
      showTotal: appStore.showTotal,
      showJumper: appStore.showJumper,
      hideOnSinglePage: appStore.hideOnSinglePage,
      simple: defaultProps.pageSimple,
    };
  }

  // 加载效果
  const setLoading = (status: boolean) => {
    propsRes.value.loading = status;
  };

  // 设置表格错误状态
  const setTableErrorStatus = (status: MsTableErrorStatus) => {
    propsRes.value.tableErrorStatus = status;
  };

  // 如果表格设置了tableKey，设置缓存的分页大小
  if (propsRes.value.msPagination && typeof propsRes.value.msPagination === 'object' && propsRes.value.tableKey) {
    tableStore.getPageSize(propsRes.value.tableKey).then((res) => {
      if (propsRes.value.msPagination && res) {
        propsRes.value.msPagination.pageSize = res;
      }
    });
  }

  /**
   * 分页设置
   * @param current //当前页数
   * @param total //总页数默认是0条，可选
   */
  const setPagination = ({ current, total }: SetPaginationPrams) => {
    if (propsRes.value.msPagination && typeof propsRes.value.msPagination === 'object') {
      propsRes.value.msPagination.current = current;
      if (total !== undefined) {
        propsRes.value.msPagination.total = total;
      }
    }
  };

  // 重置表头筛选
  const resetFilterParams = () => {
    filterItem.value = {};
    propsRes.value.filter = cloneDeep(filterItem.value);
  };

  // 单独设置默认属性
  const setProps = (params: Partial<MsTableProps<T>>) => {
    const tmpProps = propsRes.value;
    Object.keys(params).forEach((key) => {
      tmpProps[key] = params[key];
    });
    propsRes.value = tmpProps;
  };

  // 设置请求参数，如果出了分页参数还有搜索参数，在模板页面调用此方法，可以加入参数
  const loadListParams = ref<TableQueryParams>({});
  const setLoadListParams = (params?: TableQueryParams) => {
    loadListParams.value = params || {};
  };
  // 设置keyword
  const setKeyword = (v: string) => {
    keyword.value = v;
  };

  // 设置 advanceFilter
  const setAdvanceFilter = (v: FilterResult, id: string) => {
    advanceFilter.searchMode = v.searchMode;
    advanceFilter.conditions = v.conditions;
    viewId.value = id;
    // 基础筛选都清空
    loadListParams.value.filter = {};
    keyword.value = '';
    resetFilterParams();
  };
  // 给表格设置选中项 - add rowKey to selectedKeys
  const setTableSelected = (key: string) => {
    const { selectedKeys } = propsRes.value;
    if (!selectedKeys.has(key)) {
      selectedKeys.add(key);
    }
    propsRes.value.selectedKeys = selectedKeys;
  };
  // 处理全选状态下切换分页也要处理下边的子节点
  const processRecordItem = (item: MsTableDataItem<T>): MsTableDataItem<T> => {
    const { rowKey, selectorStatus, excludeKeys } = propsRes.value;

    if (item.updateTime) {
      item.updateTime = dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss');
    }
    if (item.createTime) {
      item.createTime = dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss');
    }
    if (dataTransform) {
      item = dataTransform(item);
    }

    if (selectorStatus === SelectAllEnum.ALL && !excludeKeys.has(item[rowKey])) {
      setTableSelected(item[rowKey]);

      const selectChildren = (children: MsTableDataItem<T>[]) => {
        children.forEach((child) => {
          if (!excludeKeys.has(child[rowKey])) {
            setTableSelected(child[rowKey]);
          }
          if (child.children && child.children.length) {
            selectChildren(child.children);
          }
        });
      };

      if (item.children && item.children.length) {
        selectChildren(item.children);
      }
    }

    return item;
  };

  // 加载分页列表数据
  const loadList = async () => {
    if (propsRes.value.showPagination) {
      const { current, pageSize } = propsRes.value.msPagination as Pagination;
      let currentPageSize = pageSize;
      if (propsRes.value.tableKey) {
        // 如果表格设置了tableKey，缓存分页大小
        currentPageSize = await tableStore.getPageSize(propsRes.value.tableKey);
      }
      try {
        if (loadListFunc) {
          setLoading(true);
          tableQueryParams.value = {
            current,
            pageSize: currentPageSize,
            sort: sortItem.value,
            keyword: keyword.value,
            viewId: viewId.value,
            combineSearch: advanceFilter,
            ...loadListParams.value,
            filter: {
              ...filterItem.value,
              ...loadListParams.value.filter,
            },
          };
          const data = await loadListFunc(tableQueryParams.value);
          const tmpArr = data.list || data.data.list;
          propsRes.value.data = tmpArr.map((item: MsTableDataItem<T>) => {
            return processRecordItem(item);
          });
          if (data.total === 0) {
            setTableErrorStatus('empty');
          } else {
            setTableErrorStatus(false);
          }
          setPagination({ current: data.current, total: data.total });
        }
      } catch (err) {
        setTableErrorStatus('error');
        propsRes.value.data = [];
        // eslint-disable-next-line no-console
        console.log(err);

        throw err; // 将错误抛出
      } finally {
        setLoading(false);
        // debug 模式下打印属性
        if (propsRes.value.debug && import.meta.env.DEV) {
          // eslint-disable-next-line no-console
          console.log('Table propsRes: ', propsRes.value);
        }
      }
    } else {
      // 没分页的情况下，直接调用loadListFunc
      try {
        if (loadListFunc) {
          setLoading(true);
          const data = await loadListFunc({ keyword: keyword.value, ...loadListParams.value });
          if (data.length === 0) {
            setTableErrorStatus('empty');
            propsRes.value.data = [];
            return data;
          }
          setTableErrorStatus(false);
          propsRes.value.data = data.map((item: MsTableDataItem<T>) => {
            if (item.updateTime) {
              item.updateTime = dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss');
            }
            if (item.createTime) {
              item.createTime = dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss');
            }
            if (dataTransform) {
              item = dataTransform(item);
            }
            return item;
          });
          return data;
        }
      } catch (err) {
        setTableErrorStatus('error');
        propsRes.value.data = [];
        // eslint-disable-next-line no-console
        console.log(err);
      } finally {
        setLoading(false);
      }
    }
  };

  // 重置页码和条数
  const resetPagination = () => {
    if (propsRes.value.msPagination) {
      propsRes.value.msPagination.current = 1;
      propsRes.value.msPagination.pageSize = appStore.pageSize;
    }
  };
  // 非全选级取消包含或者不包含子级别
  const processChildren = (data: MsTableDataItem<T>[], rowKey: string) => {
    data.forEach((item: MsTableDataItem<T>) => {
      propsRes.value.selectedKeys.delete(item[rowKey]);

      if (propsRes.value.selectorStatus === SelectAllEnum.ALL) {
        propsRes.value.excludeKeys.add(item[rowKey]);
      } else {
        propsRes.value.excludeKeys.delete(item[rowKey]);
      }

      if (item.children && item.children.length > 0 && !props?.rowSelectionDisabledConfig?.disabledChildren) {
        processChildren(item.children as MsTableDataItem<T>[], rowKey);
      }
    });
  };

  // 重置选择器
  const resetSelector = (isNone = true) => {
    const { rowKey } = propsRes.value;
    if (isNone) {
      propsRes.value.selectorStatus = SelectAllEnum.NONE;
      // 清空选中项
      propsRes.value.selectedKeys.clear();
      propsRes.value.excludeKeys.clear();
    } else {
      // 取消当前页的选中项
      processChildren(propsRes.value.data as MsTableDataItem<T>[], rowKey);
    }
  };

  // 重置筛选
  const clearSelector = () => {
    propsRes.value.selectorStatus = SelectAllEnum.NONE; // 重置选择器状态
    resetSelector(true);
  };

  // 获取当前表格的选中项数量
  const getSelectedCount = () => {
    const { selectorStatus, msPagination, excludeKeys, selectedKeys } = propsRes.value;
    if (msPagination) {
      if (selectorStatus === SelectAllEnum.ALL) {
        // 如果是全选状态，返回总数减去排除的数量
        return msPagination.total - excludeKeys.size;
      }
      return selectedKeys.size;
    }
  };
  const collectIds = (data: MsTableDataItem<T>[], rowKey: string) => {
    data.forEach((item: MsTableDataItem<T>) => {
      // 有数据没有勾选上，且该数据没有被禁用
      if (
        item[rowKey] &&
        !propsRes.value.selectedKeys.has(item[rowKey]) &&
        !(props?.rowSelectionDisabledConfig?.disabledKey && item[props?.rowSelectionDisabledConfig?.disabledKey])
      ) {
        propsRes.value.selectedKeys.add(item[rowKey]);
        propsRes.value.excludeKeys.delete(item[rowKey]);
      }
      if (item.children && !props?.rowSelectionDisabledConfig?.disabledChildren) {
        collectIds(item.children, rowKey);
      }
    });
  };

  // 处理父节点半选
  const handleParentSelection = (record: MsTableDataItem<T>) => {
    const { rowKey, selectedKeys, excludeKeys, rowSelectionDisabledConfig } = propsRes.value;
    const key = record[rowKey || 'id'];
    const parentKey = record[rowSelectionDisabledConfig?.parentKey || 'parent'];
    if (!record[rowSelectionDisabledConfig?.parentKey || 'parent']) return;

    const allChildrenSelected =
      record.children && record.children.length
        ? record.children.every((child: any) => selectedKeys.has(child[key]))
        : false;

    const someChildrenSelected =
      record.children && record.children.length
        ? record.children.some((child: any) => selectedKeys.has(child[key]))
        : false;

    if (allChildrenSelected) {
      selectedKeys.add(parentKey);
      excludeKeys.delete(parentKey);
    } else if (someChildrenSelected) {
      selectedKeys.add(parentKey);
      excludeKeys.delete(parentKey);
    } else {
      selectedKeys.delete(parentKey);
      excludeKeys.add(parentKey);
    }

    handleParentSelection(record[rowSelectionDisabledConfig?.parentKey || 'parent']);
  };

  // 选择/取消当前节点下边的子节点
  const handleSelectChildren = (record: MsTableDataItem<T>, select: boolean) => {
    const { rowKey, selectedKeys, excludeKeys, rowSelectionDisabledConfig } = propsRes.value;
    const key = record[rowKey || 'id'];
    const parentKey = record[rowSelectionDisabledConfig?.parentKey || 'parent'];
    if (select) {
      selectedKeys.add(key);
      excludeKeys.delete(key);
    } else {
      selectedKeys.delete(key);
      excludeKeys.add(key);
    }

    if (record.children && record.children.length) {
      record.children.forEach((childRecord: any) => handleSelectChildren(childRecord, select));
    }
    // 处理父节点的选中状态
    if (!select && parentKey && rowSelectionDisabledConfig?.checkStrictly) {
      handleParentSelection(record);
    }
  };

  // 获取表格请求参数
  const getTableQueryParams = () => {
    return tableQueryParams.value;
  };

  /**
   * 设置表格是否可拖拽
   * @param otherCondition 其他条件，如果为false，则不设置
   */
  const setTableDraggable = (otherCondition?: boolean) => {
    if (otherCondition === false || props?.draggableCondition === false) {
      propsRes.value.draggable = undefined;
    } else {
      propsRes.value.draggable = props?.draggable;
    }
  };

  watch(
    () => props?.draggableCondition,
    () => {
      setTableDraggable(Object.keys(sortItem.value).length === 0 && Object.keys(filterItem.value).length === 0);
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props?.showSelectorAll,
    (val) => {
      propsRes.value.showSelectorAll = val;
    }
  );

  // 事件触发组
  const propsEvent = ref({
    // 排序触发
    sorterChange: (sortObj: { [key: string]: string }) => {
      sortItem.value = sortObj;
      setTableDraggable(Object.keys(sortItem.value).length === 0);
      loadList();
      propsRes.value.sorter = sortObj;
    },

    // 筛选触发
    filterChange: (
      dataIndex: string,
      filteredValues: (string | number)[],
      multiple: boolean,
      isCustomParma: boolean
    ) => {
      if (isCustomParma) {
        filterItem.value = { [`custom_${multiple ? 'multiple' : 'single'}_${dataIndex}`]: filteredValues };
      } else {
        filterItem.value = { ...getTableQueryParams().filter, [dataIndex]: filteredValues };
        loadListParams.value.filter = {
          ...loadListParams.value.filter,
          [dataIndex]: filteredValues,
        };
      }
      propsRes.value.filter = cloneDeep(filterItem.value);
      setTableDraggable((filterItem.value[dataIndex] || []).length === 0);
      loadList();
    },
    // 分页触发
    pageChange: async (current: number) => {
      setPagination({ current });
      await loadList();
    },
    // 修改每页显示条数
    pageSizeChange: async (pageSize: number) => {
      if (propsRes.value.msPagination && typeof propsRes.value.msPagination === 'object') {
        propsRes.value.msPagination.pageSize = pageSize;
        if (propsRes.value.tableKey) {
          // 如果表格设置了tableKey，缓存分页大小
          await tableStore.setPageSize(propsRes.value.tableKey, pageSize);
        }
      }
      loadList();
    },
    change: (_data: MsTableDataItem<T>[]) => {
      if (propsRes.value.draggable && _data instanceof Array) {
        (propsRes.value.data as MsTableDataItem<T>[]) = _data;
      }
    },
    // 编辑触发
    rowNameChange: async (record: T, cb: (v: boolean) => void) => {
      if (saveCallBack) {
        const res = await saveCallBack(record);
        cb(res);
      }
    },
    // 重置排序
    resetSort: () => {
      sortItem.value = {};
      propsRes.value.sorter = {};
    },
    // 重置筛选
    clearSelector: () => {
      propsRes.value.selectorStatus = SelectAllEnum.NONE; // 重置选择器状态
      resetSelector(true);
    },

    // 表格SelectAll change
    selectAllChange: (v: SelectAllEnum, onlyCurrent: boolean) => {
      const { data, rowKey, selectorStatus } = propsRes.value;
      if (v === SelectAllEnum.CANCEL_ALL) {
        // 清空全选
        resetSelector(true);
      } else if (v === SelectAllEnum.NONE) {
        // 清空选中项
        resetSelector(false);
      } else if (v === SelectAllEnum.CURRENT) {
        // 如果是全选先清空选中项选和排除项，再选中当前页面所有数据,
        if (selectorStatus === SelectAllEnum.ALL) {
          propsRes.value.selectedKeys.clear();
          propsRes.value.excludeKeys.clear();
        }

        collectIds(data as MsTableDataItem<T>[], rowKey);
      } else if (v === SelectAllEnum.ALL) {
        // 全选所有页的时候先清空排除项，再选中所有数据
        propsRes.value.excludeKeys.clear();
        collectIds(data as MsTableDataItem<T>[], rowKey);
      }
      if (
        (propsRes.value.selectorStatus === SelectAllEnum.ALL &&
          v === SelectAllEnum.NONE &&
          propsRes.value.msPagination &&
          propsRes.value.excludeKeys.size < propsRes.value.msPagination.total) ||
        (propsRes.value.selectorStatus === SelectAllEnum.ALL && v === SelectAllEnum.CURRENT && !onlyCurrent)
      ) {
        // 如果当前是全选所有页状态，且是取消选中当前页操作，且排除项小于总数，则保持跨页全选状态
        // 如果当前是全选所有页状态，且是选中当前页操作(是点击全选的多选框，非下拉菜单全选当前页)，则保持跨页全选状态
        propsRes.value.selectorStatus = SelectAllEnum.ALL;
      } else {
        propsRes.value.selectorStatus = v;
      }
    },
    // 表格行的选中/取消事件
    rowSelectChange: (record: MsTableDataItem<T>) => {
      const { rowKey, rowSelectionDisabledConfig } = propsRes.value;
      const key = record[rowKey || 'id'];
      const { selectedKeys, excludeKeys } = propsRes.value;
      if (selectedKeys.has(key)) {
        // 当前已选中，取消选中
        selectedKeys.delete(key);
        excludeKeys.add(key);
        if (rowSelectionDisabledConfig?.checkStrictly) {
          // 取消当前节点的所有子节点
          handleSelectChildren(record, false);
        }
      } else {
        // 当前未选中，选中
        selectedKeys.add(key);
        if (excludeKeys.has(key)) {
          excludeKeys.delete(key);
        }
        if (rowSelectionDisabledConfig?.checkStrictly) {
          // 选择当前节点的所有子节点
          handleSelectChildren(record, true);
        }
      }

      if (selectedKeys.size === 0 && propsRes.value.selectorStatus === SelectAllEnum.CURRENT) {
        propsRes.value.selectorStatus = SelectAllEnum.NONE;
      } else if (selectedKeys.size > 0 && propsRes.value.selectorStatus === SelectAllEnum.NONE) {
        propsRes.value.selectorStatus = SelectAllEnum.CURRENT;
      }
      propsRes.value.selectedKeys = selectedKeys;
      propsRes.value.excludeKeys = excludeKeys;
    },
  });

  watchEffect(() => {
    const { heightUsed } = propsRes.value;
    if (props?.heightUsed) {
      const currentY = appStore.innerHeight - (heightUsed || defaultHeightUsed);
      propsRes.value.scroll = { ...propsRes.value.scroll, y: currentY };
    }
  });

  return {
    propsRes,
    propsEvent,
    advanceFilter,
    viewId,
    setProps,
    setLoading,
    loadList,
    setPagination,
    setLoadListParams,
    setKeyword,
    setAdvanceFilter,
    resetPagination,
    getSelectedCount,
    clearSelector,
    resetSelector,
    getTableQueryParams,
    setTableSelected,
    resetFilterParams,
  };
}
