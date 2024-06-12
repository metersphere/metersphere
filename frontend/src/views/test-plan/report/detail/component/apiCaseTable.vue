<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #num="{ record }">
      <MsButton type="text">{{ record.num }}</MsButton>
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
      <CaseLevel :case-level="filterContent.value" />
    </template>
    <template #caseLevel="{ record }">
      <CaseLevel :case-level="record.caseLevel" />
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
      <ExecuteResult :execute-result="filterContent.key" />
    </template>
    <template #lastExecResult="{ record }">
      <ExecuteResult :execute-result="record.lastExecResult" />
      <MsIcon
        v-show="record.lastExecResult !== LastExecuteResults.PENDING"
        type="icon-icon_take-action_outlined"
        class="ml-[8px] cursor-pointer text-[rgb(var(--primary-5))]"
        size="16"
        @click="showReport(record)"
      />
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { onBeforeMount } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getReportBugList, getReportShareBugList } from '@/api/modules/test-plan/report';
  import { getPlanDetailApiCaseList } from '@/api/modules/test-plan/testPlan';
  import { useTableStore } from '@/store';

  import type { PlanDetailApiScenarioItem } from '@/models/testPlan/testPlan';
  import { LastExecuteResults } from '@/enums/caseEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';
  import { executionResultMap, getCaseLevels } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    reportId: string;
    shareId?: string;
  }>();

  const tableStore = useTableStore();

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      sortIndex: 1,
      fixed: 'left',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'common.name',
      dataIndex: 'name',
      width: 150,
      showTooltip: true,
    },
    {
      title: 'report.detail.level',
      dataIndex: 'caseLevel',
      slotName: 'caseLevel',
      filterConfig: {
        options: casePriorityOptions,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'lastExecResult',
      slotName: 'lastExecResult',
      filterConfig: {
        valueKey: 'key',
        labelKey: 'statusText',
        options: Object.values(executionResultMap),
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleId',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },

    {
      title: 'case.tableColumnCreateUser',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 130,
      showDrag: true,
    },
    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUserName',
      showTooltip: true,
      width: 130,
      showDrag: true,
    },
    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUserName',
      showTooltip: true,
      width: 130,
      showDrag: true,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      slotName: 'bugCount',
      width: 100,
      showDrag: true,
    },
  ];
  const reportBugList = () => {
    return !props.shareId ? getPlanDetailApiCaseList : getReportShareBugList;
  };
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getPlanDetailApiCaseList, {
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

  // 显示执行报告
  const reportVisible = ref(false);

  const apiReportId = ref('');

  function showReport(record: PlanDetailApiScenarioItem) {
    reportVisible.value = true;
    apiReportId.value = record.lastExecReportId;
  }

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_REPORT_DETAIL_BUG, columns, 'drawer');
</script>
