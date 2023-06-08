<template>
  <div class="h-[100vh] bg-white px-[20px] py-[16px] pb-0">
    <div class="mb-10">表格</div>
    <a-input v-model="queryParmas.name"></a-input>
    <a-button @click="handleClick">搜索</a-button>
    <ms-base-table v-bind="propsRes" v-on="propsEvent"> </ms-base-table>
  </div>
  <a-divider />
</template>

<script lang="ts" setup>
  import { onMounted, watchEffect, ref } from 'vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { getTableList } from '@/api/modules/api-test/index';

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      filterable: {
        filters: [
          {
            text: '> 20000',
            value: '20000',
          },
          {
            text: '> 30000',
            value: '30000',
          },
        ],
        filter: (value, record) => record.salary > value,
        multiple: true,
      },
    },
    {
      title: '接口名称',
      dataIndex: 'name',
      width: 200,
    },
    {
      title: '请求类型',
      dataIndex: 'method',
    },
    {
      title: '责任人',
      dataIndex: 'username',
    },
    {
      title: '路径',
      dataIndex: 'path',
    },
    {
      title: '标签',
      dataIndex: 'tags',
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
    },
    {
      title: '用例数',
      dataIndex: 'caseTotal',
    },
    {
      title: '用例状态',
      dataIndex: 'caseStatus',
    },
    {
      title: '用例通过率',
      dataIndex: 'casePassingRate',
    },
    {
      title: '接口状态',
      dataIndex: 'status',
    },
    {
      title: '创建时间',
      slotName: 'createTime',
      width: 200,
    },
    {
      title: '描述',
      dataIndex: 'description',
    },
    {
      title: '操作',
      slotName: 'action',
      fixed: 'right',
      width: 200,
    },
  ];

  const queryParmas = ref({
    name: '',
  });

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getTableList, {
    columns,
    scroll: { y: 750, x: 2000 },
    selectable: true,
  });

  const fetchData = async () => {
    await loadList();
  };

  const handleClick = async () => {
    setLoadListParams(queryParmas.value);
    await fetchData();
  };

  onMounted(() => {
    fetchData();
  });
  watchEffect(() => {
    setLoadListParams(queryParmas.value);
  });
</script>
