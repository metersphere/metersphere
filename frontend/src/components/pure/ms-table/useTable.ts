// 核心的封装方法，详细参数看文档  https://arco.design/vue/component/table
// hook/table-props.ts

import { ref } from 'vue';
import { MsTableProps, MsTableData, MsTableColumn } from './type';
import { CommonList } from '@/models/api-test';
import { TableData } from '@arco-design/web-vue';
import dayjs from 'dayjs';
import { QueryParams } from '@/models/common';
import { useAppStore } from '@/store';

export interface Pagination {
  current: number;
  pageSize: number;
  total: number;
  showPageSize: boolean;
}

type GetListFunc = (v: QueryParams) => Promise<CommonList>;
const appStore = useAppStore();
export default function useTableProps(loadListFunc: GetListFunc, props?: Partial<MsTableProps>) {
  // 行选择
  const rowSelection = {
    type: 'checkbox',
    showCheckedAll: false,
  };

  const defaultProps: MsTableProps = {
    bordered: true,
    showPagination: true,
    size: 'small',
    scroll: { y: '550px', x: '1400px' },
    checkable: true,
    loading: true,
    data: [] as MsTableData,
    columns: [] as MsTableColumn,
    pagination: {
      current: 1,
      pageSize: appStore.pageSize,
      total: 0,
      showPageSize: appStore.showPageSize,
      showTotal: appStore.showTotal,
      showJumper: appStore.showJumper,
    } as Pagination,
    rowKey: 'id',
    selectedKeys: [],
    selectedAll: false,
    enableDrag: false,
    showSelectAll: true,
    ...props,
  };

  // 属性组
  const propsRes = ref(defaultProps);
  const oldPagination = ref<Pagination>({
    current: 1,
    pageSize: 20,
    total: 0,
    showPageSize: true,
  });

  // 排序
  const sortItem = ref<object>({});

  // 筛选
  const filterItem = ref<object>({});

  // keyword
  const keyword = ref('');

  // 是否分页
  if (!propsRes.value.showPagination) {
    propsRes.value.pagination = false;
  }

  // 是否可选中
  if (propsRes.value.selectable) {
    propsRes.value.rowSelection = rowSelection;
  }

  // 是否可拖拽
  if (propsRes.value.enableDrag) {
    propsRes.value.draggable = { type: 'handle' };
  }

  // 加载效果
  const setLoading = (status: boolean) => {
    propsRes.value.loading = status;
  };

  /**
   * 分页设置
   * @param current //当前页数
   * @param total //总页数默认是0条，可选
   * @param fetchData 获取列表数据,可选
   */
  interface SetPaginationPrams {
    current: number;
    total?: number;
  }

  const setPagination = ({ current, total }: SetPaginationPrams) => {
    if (propsRes.value.pagination && typeof propsRes.value.pagination === 'object') {
      propsRes.value.pagination.current = current;
      if (total) {
        propsRes.value.pagination.total = total;
      }
    }
  };

  // 单独设置默认属性
  const setProps = (params: Partial<MsTableProps>) => {
    const tmpProps = propsRes.value;
    Object.keys(params).forEach((key) => {
      tmpProps[key] = params[key];
    });
    propsRes.value = tmpProps;
  };

  // 设置请求参数，如果出了分页参数还有搜索参数，在模板页面调用此方法，可以加入参数
  const loadListParams = ref<object>({});
  const setLoadPaListrams = (params?: object) => {
    loadListParams.value = params || {};
  };

  const setKeyword = (v: string) => {
    keyword.value = v;
  };

  // 加载分页列表数据
  const loadList = async () => {
    const { current, pageSize } = propsRes.value.pagination as Pagination;
    setLoading(true);
    try {
      const data = await loadListFunc({
        current,
        pageSize,
        sort: sortItem.value,
        filter: filterItem.value,
        keyword: keyword.value,
      });
      const tmpArr = data.list as unknown as MsTableData;
      propsRes.value.data = tmpArr.map((item: TableData) => {
        if (item.updateTime) {
          item.updateTime = dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss');
        }
        if (item.createTime) {
          item.createTime = dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss');
        }
        return item;
      });
      setPagination({ current: data.current, total: data.total });
      return data;
    } catch (err) {
      // TODO 表格异常放到solt的empty
    } finally {
      setLoading(false);
    }
  };

  // 事件触发组
  const propsEvent = ref({
    // 排序触发
    sorterChange: (dataIndex: string, direction: string) => {
      // eslint-disable-next-line no-console
      sortItem.value = { [dataIndex]: direction };
      loadList();
    },

    // 筛选触发
    filterChange: (dataIndex: string, filteredValues: string[]) => {
      filterItem.value = { [dataIndex]: filteredValues };
      loadList();
    },
    // 分页触发
    pageChange: (current: number) => {
      setPagination({ current });
      loadList();
    },
    // 修改每页显示条数
    pageSizeChange: (pageSize: number) => {
      if (propsRes.value.pagination && typeof propsRes.value.pagination === 'object') {
        propsRes.value.pagination.pageSize = pageSize;
      }
      loadList();
    },
    // 选择触发
    selectedChange: (arr: (string | number)[]) => {
      if (arr.length === 0) {
        propsRes.value.pagination = oldPagination.value;
      } else {
        oldPagination.value = propsRes.value.pagination as Pagination;
        propsRes.value.pagination = false;
      }
      propsRes.value.selectedKeys = arr;
    },
    change: (_data: MsTableData) => {
      if (propsRes.value.draggable && _data instanceof Array) {
        // eslint-disable-next-line vue/require-explicit-emits
        propsRes.value.data = _data;
      }
    },
  });

  return {
    propsRes,
    propsEvent,
    setProps,
    setLoading,
    loadList,
    setPagination,
    setLoadPaListrams,
    setKeyword,
  };
}
