<template>
  <MsBaseTable v-bind="currentCaseTable.propsRes.value" v-on="currentCaseTable.propsEvent.value">
    <template #num="{ record }">
      <MsButton type="text" @click="showReport(record)">{{ record.num }}</MsButton>
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
      <caseLevel :case-level="filterContent.value" />
    </template>
    <template #priority="{ record }">
      <caseLevel :case-level="record.priority" />
    </template>
    <template #[FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS]="{ filterContent }">
      <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
    </template>

    <template #lastExecResult="{ record }">
      <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="record.executeResult" />
    </template>
  </MsBaseTable>
  <CaseAndScenarioReportDrawer
    v-model:visible="reportVisible"
    :report-id="apiReportId"
    do-not-show-share
    :is-scenario="props.activeTab === 'scenarioCase'"
    :report-detail="props.activeTab === 'scenarioCase' ? reportScenarioDetail : reportCaseDetail"
    :get-report-step-detail="props.activeTab === 'scenarioCase' ? reportStepDetail : reportCaseStepDetail"
  />
</template>

<script setup lang="ts" async>
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import CaseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import {
    getApiPage,
    getScenarioPage,
    reportCaseDetail,
    reportCaseStepDetail,
    reportScenarioDetail,
    reportStepDetail,
  } from '@/api/modules/test-plan/report';

  import { ApiOrScenarioCaseItem } from '@/models/testPlan/report';
  import { ReportEnum } from '@/enums/reportEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions, lastReportStatusListOptions } from '@/views/api-test/components/config';

  const props = defineProps<{
    reportId: string;
    shareId?: string;
    activeTab: string;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
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
      slotName: 'priority',
      filterConfig: {
        options: casePriorityOptions,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      filterConfig: {
        options: lastReportStatusListOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS,
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

  const getReportApiList = () => {
    if (props.activeTab === 'apiCase') {
      return getApiPage;
    }
    return getScenarioPage;
  };

  const useApiTable = useTable(getApiPage, {
    scroll: { x: '100%' },
    columns,
    showSelectorAll: false,
    showSetting: false,
  });
  const useScenarioTable = useTable(getScenarioPage, {
    scroll: { x: '100%' },
    columns,
    showSelectorAll: false,
    showSetting: false,
  });

  const currentCaseTable = computed(() => {
    return props.activeTab === 'apiCase' ? useApiTable : useScenarioTable;
  });

  async function loadCaseList() {
    currentCaseTable.value.setLoadListParams({ reportId: props.reportId, shareId: props.shareId ?? undefined });
    currentCaseTable.value.loadList();
  }

  // 显示执行报告
  const reportVisible = ref(false);

  const apiReportId = ref<string>('');

  function showReport(record: ApiOrScenarioCaseItem) {
    reportVisible.value = true;
    apiReportId.value = record.reportId;
  }

  watch(
    () => props.activeTab,
    () => {
      currentCaseTable.value.resetFilterParams();
      currentCaseTable.value.resetPagination();
      loadCaseList();
    }
  );

  onMounted(() => {
    loadCaseList();
  });
</script>
