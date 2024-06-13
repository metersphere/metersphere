<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #num="{ record }">
      <MsButton type="text" @click="showReport(record)">{{ record.num }}</MsButton>
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
      <caseLevel :case-level="filterContent.value" />
    </template>
    <template #caseLevel="{ record }">
      <CaseLevel :case-level="record.caseLevel" />
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
      <ExecuteResult :execute-result="filterContent.key" />
    </template>
    <template #lastExecResult="{ record }">
      <ExecuteResult :execute-result="record.lastExecResult" />
      <!-- TOTO 暂时不上 -->
      <!-- <MsIcon
        v-show="record.lastExecResult !== LastExecuteResults.PENDING"
        type="icon-icon_take-action_outlined"
        class="ml-[8px] cursor-pointer text-[rgb(var(--primary-5))]"
        size="16"
        @click="showReport(record)"
      /> -->
    </template>
  </MsBaseTable>
  <CaseAndScenarioReportDrawer v-model:visible="reportVisible" :report-id="apiReportId" do-not-show-share />
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import CaseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';

  import { getApiPage, getShareApiPage } from '@/api/modules/test-plan/report';

  import { ApiOrScenarioCaseItem } from '@/models/testPlan/report';
  import { LastExecuteResults } from '@/enums/caseEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';
  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    reportId: string;
    shareId?: string;
  }>();

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
      dataIndex: 'priority',
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
      dataIndex: 'moduleName',
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
      dataIndex: 'executeUser',
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
  const reportApiList = () => {
    return !props.shareId ? getShareApiPage : getApiPage;
  };
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    reportApiList(),
    {
      scroll: { x: '100%' },
      columns,
      tableKey: TableKeyEnum.TEST_PLAN_REPORT_DETAIL_BUG,
      showSelectorAll: false,
    },
    (record) => {
      return {
        ...record,
        lastExecResult: record.executeResult ?? LastExecuteResults.PENDING,
      };
    }
  );

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

  const apiReportId = ref<string>('');

  function showReport(record: ApiOrScenarioCaseItem) {
    reportVisible.value = true;
    apiReportId.value = record.reportId;
  }
</script>
