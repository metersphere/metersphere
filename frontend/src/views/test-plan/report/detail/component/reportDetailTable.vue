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
    <template #operation="{ record }">
      <MsButton class="!mx-0" @click="openReport(record)">{{ t('report.detail.testPlanGroup.viewReport') }}</MsButton>
    </template>
  </MsBaseTable>
  <ReportDrawer v-model:visible="reportVisible" :report-id="reportId" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ReportDrawer from '@/views/test-plan/testPlan/detail/reportDrawer.vue';

  import { getReportBugList, getReportShareBugList } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { RouteEnum } from '@/enums/routeEnum';

  const { openNewPage } = useOpenNewPage();

  const { t } = useI18n();

  const props = defineProps<{
    reportId: string;
    shareId?: string;
    currentMode: string;
  }>();
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
      dataIndex: 'result',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'report.detail.threshold',
      dataIndex: 'threshold',
      slotName: 'threshold',
      width: 150,
    },
    {
      title: 'report.passRate',
      dataIndex: 'executeUser',
      titleSlotName: 'passRateTitle',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'report.detail.testPlanGroup.useCasesCount',
      dataIndex: 'bugCount',
      width: 100,
    },
  ];

  const reportBugList = () => {
    return !props.shareId ? getReportBugList : getReportShareBugList;
  };

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(reportBugList(), {
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
  function openReport(record: any) {
    if (props.currentMode === 'drawer') {
      reportVisible.value = true;
    } else {
      openNewPage(RouteEnum.TEST_PLAN_REPORT_DETAIL, {
        reportId: record.id,
      });
    }
  }
</script>

<style scoped></style>
