<template>
  <MsBaseTable v-bind="propsRes" no-disable filter-icon-align-left v-on="propsEvent">
    <template #passRateTitle="{ columnConfig }">
      <div class="flex items-center text-[var(--color-text-3)]">
        {{ t(columnConfig.title as string) }}
        <a-tooltip position="right" :content="t('testPlan.testPlanIndex.passRateTitleTip')">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </a-tooltip>
      </div>
    </template>
    <template #resultStatus="{ record }">
      <ExecutionStatus v-if="record.resultStatus !== '-'" :status="record.resultStatus" />
    </template>
    <template #[FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER]="{ filterContent }">
      <ExecutionStatus :status="filterContent.value" />
    </template>
    <template #operation="{ record }">
      <MsButton class="!mx-0" :disabled="record.deleted" @click="openReport(record)">{{
        t('report.detail.testPlanGroup.viewReport')
      }}</MsButton>
    </template>
  </MsBaseTable>
  <ReportDrawer v-model:visible="reportVisible" :report-id="independentReportId" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ExecutionStatus from '@/views/test-plan/report/component/reportStatus.vue';
  import ReportDrawer from '@/views/test-plan/testPlan/detail/reportDrawer.vue';

  import { getReportDetailPage, getReportDetailSharePage } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { PlanReportDetail } from '@/models/testPlan/testPlanReport';
  import { PlanReportStatus } from '@/enums/reportEnum';
  import { RouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const { openNewPage } = useOpenNewPage();

  const { t } = useI18n();

  const props = defineProps<{
    reportId: string;
    shareId?: string;
    currentMode: string;
  }>();

  const statusResultOptions = computed(() => {
    return Object.keys(PlanReportStatus).map((key) => {
      return {
        value: key,
        label: PlanReportStatus[key].statusText,
      };
    });
  });

  const columns: MsTableColumn = [
    {
      title: 'testPlan.testPlanIndex.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'left',
      showInTable: true,
      showDrag: false,
      width: 80,
    },
    {
      title: 'report.plan.name',
      dataIndex: 'name',
      showTooltip: true,
      width: 180,
    },
    {
      title: 'report.detail.testPlanGroup.result',
      dataIndex: 'resultStatus',
      slotName: 'resultStatus',
      filterConfig: {
        options: statusResultOptions.value,
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER,
      },
      showTooltip: true,
      width: 200,
    },
    {
      title: 'report.detail.threshold',
      dataIndex: 'passThreshold',
      slotName: 'passThreshold',
      width: 150,
    },
    {
      title: 'report.passRate',
      dataIndex: 'passRate',
      slotName: 'passRate',
      titleSlotName: 'passRateTitle',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'report.detail.testPlanGroup.useCasesCount',
      dataIndex: 'caseTotal',
      width: 100,
    },
  ];

  const reportDetailList = () => {
    return !props.shareId ? getReportDetailPage : getReportDetailSharePage;
  };

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(reportDetailList(), {
    columns,
    heightUsed: 20,
    showSelectorAll: false,
  });

  function loadReportDetailList() {
    setLoadListParams({ reportId: props.reportId, shareId: props.shareId ?? undefined });
    loadList();
  }

  watchEffect(() => {
    if (props.reportId) {
      loadReportDetailList();
    }
  });

  const reportVisible = ref(false);

  const independentReportId = ref<string>('');

  function openReport(record: PlanReportDetail) {
    independentReportId.value = record.id;
    if (props.currentMode === 'drawer') {
      reportVisible.value = true;
    } else {
      openNewPage(RouteEnum.TEST_PLAN_REPORT_DETAIL, {
        id: record.id,
      });
    }
  }
</script>

<style scoped></style>
