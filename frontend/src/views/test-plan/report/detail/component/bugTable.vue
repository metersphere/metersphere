<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent"> </MsBaseTable>
</template>

<script setup lang="ts">
  import { onBeforeMount } from 'vue';
  import { useRoute } from 'vue-router';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getReportBugList } from '@/api/modules/test-plan/report';
  import { useTableStore } from '@/store';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    reportId: string;
  }>();

  const tableStore = useTableStore();
  const route = useRoute();

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      width: 100,
      ellipsis: true,
      showTooltip: true,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      width: 150,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'status',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUserName',
      showTooltip: true,
      width: 125,
    },
    {
      title: 'bugManagement.numberOfCase',
      dataIndex: 'relationCaseCount',
      width: 80,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getReportBugList, {
    scroll: { x: '100%' },
    columns,
    tableKey: TableKeyEnum.TEST_PLAN_REPORT_DETAIL_BUG,
    showSelectorAll: false,
  });

  async function loadCaseList() {
    setLoadListParams({ reportId: props.reportId, shareId: route.query.shareId as string | undefined });
    loadList();
  }

  onBeforeMount(() => {
    loadCaseList();
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_REPORT_DETAIL_BUG, columns, 'drawer');
</script>
