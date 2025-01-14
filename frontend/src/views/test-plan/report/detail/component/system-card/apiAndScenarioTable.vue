<template>
  <div :class="`${props.enabledTestSet ? 'test-set-wrapper' : ''}`">
    <MsBaseTable
      v-bind="currentCaseTable.propsRes.value"
      :row-class="getRowClass"
      v-on="currentCaseTable.propsEvent.value"
    >
      <template #num="{ record }">
        <MsButton type="text" :disabled="!props.isPreview" @click="toDetail(record)">{{ record.num }}</MsButton>
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
        <ExecutionStatus
          :module-type="ReportEnum.API_REPORT"
          :status="record.executeResult"
          :class="[!record.executeResult ? '' : 'cursor-pointer']"
          @click="showReport(record)"
        />
      </template>
    </MsBaseTable>
    <CaseAndScenarioReportDrawer
      v-model:visible="reportVisible"
      :report-id="apiReportId"
      do-not-show-share
      :is-scenario="props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL"
      :report-detail="
        props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL ? reportScenarioDetail : reportCaseDetail
      "
      :get-report-step-detail="
        props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL ? reportStepDetail : reportCaseStepDetail
      "
    />
  </div>
</template>

<script setup lang="ts">
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
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';

  import { ApiOrScenarioCaseItem } from '@/models/testPlan/report';
  import type { PlanDetailApiCaseItem } from '@/models/testPlan/testPlan';
  import { ReportEnum } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { casePriorityOptions, lastReportStatusListOptions } from '@/views/api-test/components/config';
  import {
    detailTableExample,
    lastStaticColumns,
    testPlanNameColumn,
  } from '@/views/test-plan/report/detail/component/reportConfig';

  const tableStore = useTableStore();

  const { openNewPage } = useOpenNewPage();

  const props = defineProps<{
    reportId: string;
    enabledTestSet: boolean;
    testSetId?: string; // 测试集id
    shareId?: string;
    activeType: ReportCardTypeEnum;
    isPreview?: boolean;
  }>();

  const innerKeyword = defineModel<string>('keyword', {
    required: true,
  });
  const isGroup = inject<Ref<boolean>>('isPlanGroup', ref(false));

  const staticColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      width: 100,
      showInTable: true,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'common.name',
      dataIndex: 'name',
      width: 150,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'report.detail.level',
      dataIndex: 'priority',
      slotName: 'priority',
      filterConfig: {
        options: props.isPreview ? casePriorityOptions : [],
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      filterConfig: {
        options: props.isPreview ? lastReportStatusListOptions.value : [],
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS,
        emptyFilter: true,
      },
      width: 150,
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'report.requestTime',
      dataIndex: 'requestTime',
      sortable: props.isPreview
        ? {
            sortDirections: ['ascend', 'descend'],
            sorter: true,
          }
        : undefined,
      width: 100,
      showInTable: true,
      showDrag: true,
    },
  ];
  const apiLastStaticColumns: MsTableColumn = [
    ...lastStaticColumns,
    {
      title: '',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 30,
    },
  ];

  const columns = computed(() => {
    if (isGroup.value) {
      return [...staticColumns, ...testPlanNameColumn, ...apiLastStaticColumns];
    }
    return [...staticColumns, ...apiLastStaticColumns];
  });

  const keyMap: Record<string, any> = {
    GROUP: {
      API_CASE_DETAIL: TableKeyEnum.TEST_PLAN_REPORT_API_TABLE_GROUP,
      SCENARIO_CASE_DETAIL: TableKeyEnum.TEST_PLAN_REPORT_SCENARIO_TABLE_GROUP,
    },
    TEST_PLAN: {
      API_CASE_DETAIL: TableKeyEnum.TEST_PLAN_REPORT_API_TABLE,
      SCENARIO_CASE_DETAIL: TableKeyEnum.TEST_PLAN_REPORT_SCENARIO_TABLE,
    },
  };

  const tableKey = computed(() => {
    if (props.isPreview) {
      return isGroup.value ? keyMap.GROUP[props.activeType] : keyMap.TEST_PLAN[props.activeType];
    }
    return TableKeyEnum.TEST_PLAN_REPORT_API_TABLE_NOT_PREVIEW;
  });

  const useApiTable = useTable(getApiPage, {
    tableKey: tableKey.value,
    scroll: { x: '100%' },
    columns: columns.value,
    showSelectorAll: false,
    heightUsed: 236,
    showSetting: props.isPreview,
    isSimpleSetting: true,
    isHiddenSetting: props.enabledTestSet,
    paginationSize: 'mini',
  });

  const useScenarioTable = useTable(getScenarioPage, {
    tableKey: tableKey.value,
    scroll: { x: '100%' },
    columns: columns.value,
    showSelectorAll: false,
    showSetting: props.isPreview,
    heightUsed: 236,
    isSimpleSetting: true,
    isHiddenSetting: props.enabledTestSet,
    paginationSize: 'mini',
  });

  const currentCaseTable = computed(() => {
    return props.activeType === ReportCardTypeEnum.API_CASE_DETAIL ? useApiTable : useScenarioTable;
  });

  async function setCurrentPageSize() {
    const pageSize = await tableStore.getPageSize(tableKey.value);
    const { propsRes } = currentCaseTable.value;
    if (propsRes.value.msPagination && propsRes.value.msPagination.pageSize) {
      propsRes.value.msPagination.pageSize = pageSize;
    }
  }

  async function loadCaseList() {
    setCurrentPageSize();
    currentCaseTable.value.setLoadListParams({
      reportId: props.reportId,
      shareId: props.shareId ?? undefined,
      keyword: innerKeyword.value,
      collectionId: props.testSetId,
    });
    currentCaseTable.value.loadList();
  }

  // 显示执行报告
  const reportVisible = ref(false);

  const apiReportId = ref<string>('');
  const selectedReportId = ref<string>('');

  function showReport(record: ApiOrScenarioCaseItem) {
    if (!record.reportId) {
      return;
    }
    if (!record.executeResult || record.executeResult === 'STOPPED') return;
    reportVisible.value = true;
    apiReportId.value = record.reportId;
    selectedReportId.value = record.reportId;
  }

  function getRowClass(record: ApiOrScenarioCaseItem) {
    return record.reportId === selectedReportId.value && props.isPreview ? 'selected-row-class' : '';
  }

  // 去接口用例详情页面
  function toDetail(record: PlanDetailApiCaseItem) {
    if (props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL) {
      openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, {
        id: record.id,
        pId: record.projectId,
      });
    } else {
      openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
        cId: record.id,
        pId: record.projectId,
      });
    }
  }

  onMounted(() => {
    if (props.reportId) {
      currentCaseTable.value.resetPagination();
      loadCaseList();
    }
  });

  watch(
    () => props.reportId,
    (val) => {
      if (val) {
        currentCaseTable.value.resetPagination();
        loadCaseList();
      }
    }
  );

  watch(
    () => props.isPreview,
    (val) => {
      if (!val) {
        currentCaseTable.value.propsRes.value.data = detailTableExample[props.activeType];
      }
    },
    {
      immediate: true,
    }
  );

  async function initSetColumnConfig() {
    if (!props.enabledTestSet) {
      const tmpArr = await tableStore.getStoreColumns(tableKey.value);
      await tableStore.initColumn(
        tableKey.value,
        tmpArr?.length ? tmpArr.filter((item) => item.dataIndex !== 'collectionName') : columns.value,
        'drawer'
      );
    }
  }

  defineExpose({
    loadCaseList,
  });

  await initSetColumnConfig();
</script>

<style lang="less" scoped></style>
