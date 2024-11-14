<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="font-medium"> {{ props.label }} </div>
    <div v-if="props.isPreview" class="flex items-center">
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('report.detail.api.placeHolderName')"
        allow-clear
        class="mr-[8px] w-[240px]"
        @search="loadReportDetailList"
        @press-enter="loadReportDetailList"
        @clear="loadReportDetailList"
      />
      <a-radio-group
        v-if="props.isPreview"
        v-model:model-value="currentMode"
        type="button"
        size="medium"
        @change="handleModeChange"
      >
        <a-radio value="drawer">
          <div class="mode-button">
            <MsIcon :class="{ 'active-color': currentMode === 'drawer' }" type="icon-icon_drawer" />
            <span class="mode-button-title">{{ t('msTable.columnSetting.drawer') }}</span>
          </div>
        </a-radio>
        <a-radio value="new_window">
          <div class="mode-button">
            <MsIcon :class="{ 'active-color': currentMode === 'new_window' }" type="icon-icon_into-item_outlined" />
            <span class="mode-button-title">{{ t('msTable.columnSetting.newWindow') }}</span>
          </div>
        </a-radio>
      </a-radio-group>
    </div>
  </div>
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
    <template #passThreshold="{ record }">
      <div>
        {{ `${record.passThreshold || '0.00'}%` }}
      </div>
    </template>
    <template #passRate="{ record }">
      <div>
        {{ `${record.passRate || '0.00'}%` }}
      </div>
    </template>
    <template #[FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER]="{ filterContent }">
      <ExecutionStatus :status="filterContent.value" />
    </template>
    <template #operation="{ record }">
      <MsButton class="!mx-0" :disabled="record.deleted || !props.isPreview" @click="openReport(record)">{{
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
  import ReportDrawer from '@/views/test-plan/report/component/reportDrawer.vue';
  import ExecutionStatus from '@/views/test-plan/report/component/reportStatus.vue';

  import { getReportDetailPage, getReportDetailSharePage } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { PlanReportDetail } from '@/models/testPlan/testPlanReport';
  import { PlanReportStatus } from '@/enums/reportEnum';
  import { RouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { detailTableExample } from '@/views/test-plan/report/detail/component/reportConfig';

  const { openNewPage } = useOpenNewPage();

  const { t } = useI18n();

  const props = defineProps<{
    reportId: string;
    shareId?: string;
    isPreview?: boolean;
    label?: string;
  }>();

  const keyword = ref<string>('');

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
      dataIndex: 'testPlanName',
      showTooltip: true,
      width: 180,
    },
    {
      title: 'report.detail.testPlanGroup.result',
      dataIndex: 'resultStatus',
      slotName: 'resultStatus',
      filterConfig: {
        options: props.isPreview ? statusResultOptions.value : [],
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER,
      },
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

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    reportDetailList(),
    {
      scroll: { x: '100%' },
      columns,
      heightUsed: 20,
      showSelectorAll: false,
    },
    (item) => {
      return {
        ...item,
        caseTotal: item.caseTotal || 0,
      };
    }
  );

  function loadReportDetailList() {
    setLoadListParams({ reportId: props.reportId, shareId: props.shareId ?? undefined, keyword: keyword.value });
    loadList();
  }

  watch(
    () => props.isPreview,
    (val) => {
      if (!val) {
        propsRes.value.data = detailTableExample[ReportCardTypeEnum.SUB_PLAN_DETAIL];
      }
    },
    {
      immediate: true,
    }
  );

  onMounted(() => {
    if (props.reportId) {
      loadReportDetailList();
    }
  });

  watch(
    () => props.reportId,
    (val) => {
      if (val) {
        loadReportDetailList();
      }
    }
  );

  const reportVisible = ref(false);

  const independentReportId = ref<string>('');
  const currentMode = ref<string>('drawer');

  function openReport(record: PlanReportDetail) {
    independentReportId.value = record.id;
    if (currentMode.value === 'drawer') {
      reportVisible.value = true;
    } else {
      openNewPage(RouteEnum.TEST_PLAN_REPORT_DETAIL, {
        id: record.id,
      });
    }
  }

  function handleModeChange(value: string | number | boolean, ev: Event) {
    currentMode.value = value as string;
  }
</script>

<style scoped></style>
