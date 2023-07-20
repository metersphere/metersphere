<template>
  <div class="h-[100vh] bg-white px-[20px] py-[16px] pb-0">
    <ms-base-table v-bind="propsRes" :action-config="actionConfig" v-on="propsEvent"> </ms-base-table>
  </div>
  <a-divider />
</template>

<script lang="ts" setup>
  import { onMounted } from 'vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { BatchActionConfig, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { getTableList } from '@/api/modules/api-test/index';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { useTableStore } from '@/store';

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

  const actionConfig: BatchActionConfig = {
    baseAction: [
      {
        label: 'msTable.batch.export',
        eventTag: 'batchExport',
        isDivider: false,
        danger: false,
      },
      {
        label: 'msTable.batch.edit',
        eventTag: 'batchEdit',
        isDivider: false,
        danger: false,
      },
      {
        label: 'msTable.batch.moveTo',
        eventTag: 'batchMoveTo',
        isDivider: false,
        danger: false,
      },
      {
        label: 'msTable.batch.copyTo',
        eventTag: 'batchCopyTo',
        isDivider: false,
        danger: false,
      },
    ],
    moreAction: [
      {
        label: 'msTable.batch.related',
        eventTag: 'batchRelated',
        isDivider: false,
        danger: false,
      },
      {
        label: 'msTable.batch.generateDep',
        eventTag: 'batchGenerate',
        isDivider: false,
        danger: false,
      },
      {
        label: 'msTable.batch.addPublic',
        eventTag: 'batchAddTo',
        isDivider: false,
        danger: false,
      },
      {
        isDivider: true,
      },
      {
        label: 'msTable.batch.delete',
        eventTag: 'batchDelete',
        isDivider: false,
        danger: true,
      },
    ],
  };

  const tableStore = useTableStore();

  tableStore.initColumn(TableKeyEnum.API_TEST, columns, 'drawer');

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
