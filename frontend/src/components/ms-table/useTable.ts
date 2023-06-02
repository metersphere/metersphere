// 核心的封装方法，详细参数看文档  https://arco.design/vue/component/table
// hook/table-props.ts

import { ref } from 'vue';
import { MsTabelProps, MsTableData, MsTableColumn } from './type';
import { ApiTestListI } from '@/models/api-test';

export interface Pagination {
  current: number;
  pageSize: number;
  total: number;
  showPageSize: boolean;
}

export interface QueryParams {
  current: number;
  pageSize: number;
  [key: string]: any;
}

type GetListFunc = (v: QueryParams) => Promise<ApiTestListI>;
export default function useTbleProps(loadListFunc: GetListFunc, props?: Partial<MsTabelProps>) {
  const defaultProps: MsTabelProps = {
    'bordered': true,
    'size': 'mini',
    'scroll': { y: 550, x: '1400px' },
    'expandable': false,
    'loading': true,
    'data': [] as MsTableData,
    'columns': [] as MsTableColumn,
    'pagination': {
      current: 1,
      pageSize: 20,
      total: 0,
      showPageSize: true,
    } as Pagination,
    'draggable': { type: 'handle' },
    'row-key': 'id',
    ...props,
  };

  // 属性组
  const propsRes = ref(defaultProps);

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
    if (propsRes.value.pagination) {
      propsRes.value.pagination.current = current;
      if (total) propsRes.value.pagination.total = total;
    }
  };

  // 单独设置默认属性
  const setProps = (params: Partial<MsTabelProps>) => {
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

  // 加载分页列表数据
  const loadList = async () => {
    const { current, pageSize } = propsRes.value.pagination as Pagination;
    setLoading(true);
    const data = await loadListFunc({
      current,
      pageSize,
      ...loadListParams.value,
    });
    propsRes.value.data = data.list as unknown as MsTableData;
    setPagination({ current: data.current, total: data.total });
    setLoading(false);
    return data;
  };

  // 事件触发组
  const propsEvent = ref({
    // 排序触发
    sorterChange: (dataIndex: string, direction: string) => {
      // eslint-disable-next-line no-console
      console.log(dataIndex, direction);
    },
    // 分页触发
    pageChange: (current: number) => {
      setPagination({ current });
      loadList();
    },
    // 修改每页显示条数
    pageSizeChange: (pageSize: number) => {
      if (propsRes.value.pagination) {
        propsRes.value.pagination.pageSize = pageSize;
      }
      loadList();
    },
    change: (_data: MsTableData) => {
      if (propsRes.value.draggable) {
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
    setLoadListParams,
  };
}
