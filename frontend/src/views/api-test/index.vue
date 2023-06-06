<template>
  <div class="h-[100vh] bg-white px-[20px] py-[16px] pb-0">
    <div class="mb-10">表格</div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #createTime="{ record }">
        {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
    </ms-base-table>
  </div>
  <a-divider />
</template>

<script lang="ts" setup>
  import { onMounted } from 'vue';
  import MsBaseTable from '@/components/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/ms-table/type';
  import useTable from '@/components/ms-table/useTable';
  import { getTableList } from '@/api/modules/api-test/index';
  import dayjs from 'dayjs';

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
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
      slotName: 'updataTime',
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

  const { propsRes, propsEvent, loadList } = useTable(getTableList, { columns, scroll: { y: 750, x: 2000 } });

  const fetchData = async () => {
    await loadList();
  };

  onMounted(() => {
    fetchData();
  });
</script>
