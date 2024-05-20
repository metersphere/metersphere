<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #caseLevel="{ record }">
      <CaseLevel :case-level="record.priority" />
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
      <ExecuteResult :execute-result="filterContent.key" />
    </template>
    <template #lastExecResult="{ record }">
      <ExecuteResult :execute-result="record.executeResult" />
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { onBeforeMount } from 'vue';
  import { useRoute } from 'vue-router';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';

  import { getReportFeatureCaseList } from '@/api/modules/test-plan/report';
  import { useTableStore } from '@/store';

  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';

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
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      width: 150,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      filterConfig: {
        valueKey: 'key',
        labelKey: 'statusText',
        options: Object.values(executionResultMap),
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      width: 150,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      ellipsis: true,
      showTooltip: true,
      width: 200,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      width: 100,
    },
    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUserName',
      showTooltip: true,
      width: 150,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getReportFeatureCaseList, {
    scroll: { x: '100%' },
    columns,
    tableKey: TableKeyEnum.TEST_PLAN_REPORT_DETAIL_FEATURE_CASE,
    heightUsed: 20,
    showSelectorAll: false,
  });

  async function loadCaseList() {
    setLoadListParams({ reportId: props.reportId, shareId: route.query.shareId as string | undefined });
    loadList();
  }

  onBeforeMount(() => {
    loadCaseList();
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_REPORT_DETAIL_FEATURE_CASE, columns, 'drawer');
</script>
