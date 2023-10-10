// 核心的封装方法，详细参数看文档  https://arco.design/vue/component/table
// hook/table-props.ts

import { ref, watchEffect } from 'vue';
import dayjs from 'dayjs';
import { useAppStore, useTableStore } from '@/store';
import type { TableData } from '@arco-design/web-vue';
import type { TableQueryParams, CommonList } from '@/models/common';
import { SelectAllEnum } from '@/enums/tableEnum';
import type { MsTableProps, MsTableDataItem, MsTableColumn, MsTableErrorStatus, SetPaginationPrams } from './type';

export interface Pagination {
  current: number;
  pageSize: number;
  total: number;
}

const appStore = useAppStore();
const tableStore = useTableStore();
export default function useTableProps<T>(
  loadListFunc: (v: TableQueryParams) => Promise<CommonList<MsTableDataItem<T>>>,
  props?: Partial<MsTableProps<T>>,
  // 数据处理的回调函数
  dataTransform?: (item: T) => TableData & T,
  // 编辑操作的保存回调函数
  saveCallBack?: (item: T) => Promise<any>
) {
  const defaultProps: MsTableProps<T> = {
    tableKey: '', // 缓存pageSize 或 column 的 key
    bordered: true, // 是否显示边框
    showPagination: true, // 是否显示分页
    size: 'default', // 表格大小
    heightUsed: 294, // 表格所在的页面已经使用的高度
    scroll: { x: 1400, y: appStore.innerHeight - 294 }, // 表格滚动配置
    loading: false, // 加载效果
    data: [], // 表格数据
    /**
     * 表格列配置
     * 当showSetting为true时，此配置无效,通过TableStore.initColumn(tableKey: string, column: MsTableColumn)初始化。
     * 当showSetting为false时，此配置生效
     */
    columns: [] as MsTableColumn,
    rowKey: 'id', // 表格行的key
    /** 选择器相关 */
    rowSelection: null, // 禁用表格默认的选择器
    selectable: false, // 是否显示选择器
    selectorType: 'checkbox', // 选择器类型
    selectedKeys: new Set<string>(), // 选中的key
    excludeKeys: new Set<string>(), // 排除的key
    selectorStatus: SelectAllEnum.NONE, // 选择器状态
    /** end */
    enableDrag: false, // 是否可拖拽
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
    ...props,
  };

  // 属性组
  const propsRes = ref<MsTableProps<T>>(defaultProps);

  // 排序
  const sortItem = ref<object>({});

  // 筛选
  const filterItem = ref<object>({});

  // keyword
  const keyword = ref('');

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

  // 是否可拖拽
  if (propsRes.value.enableDrag) {
    propsRes.value.draggable = { type: 'handle' };
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
    const pageSize = tableStore.getPageSize(propsRes.value.tableKey);
    propsRes.value.msPagination.pageSize = pageSize;
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

  // 单独设置默认属性
  const setProps = (params: Partial<MsTableProps<T>>) => {
    const tmpProps = propsRes.value;
    Object.keys(params).forEach((key) => {
      tmpProps[key] = params[key];
    });
    propsRes.value = tmpProps;
  };

  // 设置请求参数，如果出了分页参数还有搜索参数，在模板页面调用此方法，可以加入参数
  const loadListParams = ref<object>({});
  const setLoadListParams = (params?: object) => {
    loadListParams.value = params || {};
  };

  const setKeyword = (v: string) => {
    keyword.value = v;
  };

  // 给表格设置选中项 - add rowKey to selectedKeys
  const setTableSelected = (key: string) => {
    const { selectedKeys } = propsRes.value;
    if (!selectedKeys.has(key)) {
      selectedKeys.add(key);
    }
    propsRes.value.selectedKeys = selectedKeys;
  };

  // 加载分页列表数据
  const loadList = async () => {
    if (propsRes.value.showPagination) {
      const { current, pageSize } = propsRes.value.msPagination as Pagination;
      const { rowKey, selectorStatus, excludeKeys } = propsRes.value;
      try {
        setLoading(true);
        const data = await loadListFunc({
          current,
          pageSize,
          sort: sortItem.value,
          filter: filterItem.value,
          keyword: keyword.value,
          ...loadListParams.value,
        });
        const tmpArr = data.list;
        (propsRes.value.data as MsTableDataItem<T>[]) = tmpArr.map((item) => {
          if (item.updateTime) {
            item.updateTime = dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss');
          }
          if (item.createTime) {
            item.createTime = dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss');
          }
          if (dataTransform) {
            item = dataTransform(item);
          }
          if (selectorStatus === SelectAllEnum.ALL) {
            if (!excludeKeys.has(item[rowKey])) {
              setTableSelected(item[rowKey]);
            }
          }
          return item;
        });
        if (data.total === 0) {
          setTableErrorStatus('empty');
        } else {
          setTableErrorStatus(false);
        }
        setPagination({ current: data.current, total: data.total });
        return data;
      } catch (err) {
        setTableErrorStatus('error');
      } finally {
        setLoading(false);
        // debug 模式下打印属性
        if (propsRes.value.debug && import.meta.env.DEV) {
          // eslint-disable-next-line no-console
          // console.log('Table propsRes: ', propsRes.value);
        }
      }
    } else {
      // 没分页的情况下，直接调用loadListFunc
      try {
        setLoading(true);
        const data = await loadListFunc({ keyword: keyword.value, ...loadListParams.value });
        if (data.total === 0) {
          setTableErrorStatus('empty');
          return;
        }
        setTableErrorStatus(false);
        const tmpArr = data.list;
        (propsRes.value.data as MsTableDataItem<T>[]) = tmpArr.map((item) => {
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
      } catch (err) {
        setTableErrorStatus('error');
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

  // 重置选择器
  const resetSelector = () => {
    propsRes.value.selectedKeys.clear();
    propsRes.value.excludeKeys.clear();
    propsRes.value.selectorStatus = SelectAllEnum.NONE;
  };

  // 获取当前表格的选中项数量
  const getSelectedCount = () => {
    const { selectorStatus, msPagination, excludeKeys, selectedKeys } = propsRes.value;
    if (msPagination) {
      if (selectorStatus === SelectAllEnum.ALL) {
        // 如果是全选状态，返回总数减去排除的数量
        return msPagination.total - excludeKeys.size;
      }
      // if (selectorStatus === SelectAllEnum.NONE) {
      // 如果是全不选状态，返回选中的数量
      return selectedKeys.size;
      // }
      // if (selectorStatus === SelectAllEnum.CURRENT) {
      //   // 如果是当前页状态，返回当前页减去排除的数量
      //   return msPagination.pageSize - excludeKeys.size;
      // }
    }
  };

  // 事件触发组
  const propsEvent = ref({
    // 排序触发
    sorterChange: (sortObj: { [key: string]: string }) => {
      sortItem.value = sortObj;
      loadList();
    },

    // 筛选触发
    filterChange: (dataIndex: string, filteredValues: string[]) => {
      filterItem.value = { [dataIndex]: filteredValues };
      loadList();
    },
    // 分页触发
    pageChange: async (current: number) => {
      setPagination({ current });
      await loadList();
    },
    // 修改每页显示条数
    pageSizeChange: (pageSize: number) => {
      if (propsRes.value.msPagination && typeof propsRes.value.msPagination === 'object') {
        propsRes.value.msPagination.pageSize = pageSize;
        if (propsRes.value.tableKey) {
          // 如果表格设置了tableKey，缓存分页大小
          tableStore.setPageSize(propsRes.value.tableKey, pageSize);
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
    rowNameChange: (record: T) => {
      if (saveCallBack) {
        saveCallBack(record);
      }
    },
    // 重置排序
    resetSort: () => {
      sortItem.value = {};
    },
    // 重置筛选
    clearSelector: () => {
      resetSelector();
    },

    // 表格SelectAll change
    selectAllChange: (v: SelectAllEnum) => {
      propsRes.value.selectorStatus = v;
      const { data, rowKey, selectedKeys } = propsRes.value;
      if (v === SelectAllEnum.NONE) {
        // 清空选中项
        resetSelector();
      } else {
        data.forEach((item) => {
          if (item[rowKey] && !selectedKeys.has(item[rowKey])) {
            selectedKeys.add(item[rowKey]);
          }
        });
        propsRes.value.selectedKeys = selectedKeys;
      }
    },

    // 表格行的选中/取消事件
    rowSelectChange: (key: string) => {
      const { selectedKeys, excludeKeys } = propsRes.value;
      if (selectedKeys.has(key)) {
        // 当前已选中，取消选中
        selectedKeys.delete(key);
        excludeKeys.add(key);
      } else {
        // 当前未选中，选中
        selectedKeys.add(key);
        if (excludeKeys.has(key)) {
          excludeKeys.delete(key);
        }
      }
      propsRes.value.selectedKeys = selectedKeys;
      propsRes.value.excludeKeys = excludeKeys;
    },
  });

  watchEffect(() => {
    // TODO 等UI出图，表格设置里加入分页配置等操作
    const { heightUsed } = propsRes.value;
    const currentY = appStore.innerHeight - (heightUsed || 294);
    propsRes.value.scroll = { ...propsRes.value.scroll, y: currentY };
  });

  return {
    propsRes,
    propsEvent,
    setProps,
    setLoading,
    loadList,
    setPagination,
    setLoadListParams,
    setKeyword,
    resetPagination,
    getSelectedCount,
    resetSelector,
  };
}
