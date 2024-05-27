<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent"> </MsBaseTable>
</template>

<script setup lang="ts">
  import { onBeforeMount } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getReportBugList, getReportShareBugList } from '@/api/modules/test-plan/report';
  import { useTableStore } from '@/store';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    reportId: string;
    shareId?: string;
  }>();

  const tableStore = useTableStore();

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
  const reportBugList = () => {
    return !props.shareId ? getReportBugList : getReportShareBugList;
  };
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(reportBugList(), {
    scroll: { x: '100%' },
    columns,
    tableKey: TableKeyEnum.TEST_PLAN_REPORT_DETAIL_BUG,
    showSelectorAll: false,
  });

  async function loadCaseList() {
    setLoadListParams({ reportId: props.reportId, shareId: props.shareId ?? undefined });
    loadList();
  }

  watchEffect(() => {
    if (props.reportId) {
      loadCaseList();
    }
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_REPORT_DETAIL_BUG, columns, 'drawer');
</script>
