<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent"> </MsBaseTable>
</template>

<script lang="ts" setup>
  import { getTableList } from '@/api/modules/api-test/index';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import { onMounted } from 'vue';

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
  const { propsRes, propsEvent, loadList } = useTable(getTableList, {
    columns,
    scroll: { y: 750, x: 2000 },
    selectable: true,
  });
  const fetchData = async () => {
    await loadList();
  };
  onMounted(() => {
    fetchData();
  });
</script>
