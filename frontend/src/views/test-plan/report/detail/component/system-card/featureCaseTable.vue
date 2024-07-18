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
      <MsButton class="ml-[8px]" :disabled="!props.isPreview || !record.reportId" @click="openExecuteHistory(record)">{{
        t('common.detail')
      }}</MsButton>
    </template>
  </MsBaseTable>
  <MsDrawer
    v-model:visible="showDetailVisible"
    :title="t('common.detail')"
    :width="1200"
    :footer="false"
    no-content-padding
    unmount-on-close
  >
    <div class="p-[16px]">
      <ExecutionHistory show-step-result :loading="executeLoading" :execute-list="executeList" />
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { TableSortable } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import ExecutionHistory from '@/views/test-plan/testPlan/detail/featureCase/detail/executionHistory/index.vue';

  import {
    getFunctionalExecuteStep,
    getReportFeatureCaseList,
    getReportShareFeatureCaseList,
  } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';

  import { FeatureCaseItem } from '@/models/testPlan/report';
  import type { ExecuteHistoryItem } from '@/models/testPlan/testPlan';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';
  import { detailTableExample } from '@/views/test-plan/report/detail/component/reportConfig';

  const props = defineProps<{
    reportId: string;
    shareId?: string;
    isPreview?: boolean;
    isGroup?: boolean;
  }>();
  const { t } = useI18n();

  const sortableConfig = computed<TableSortable | undefined>(() => {
    return props.isPreview
      ? {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        }
      : undefined;
  });

  const staticColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: cloneDeep(sortableConfig.value),
      fixed: 'left',
      width: 100,
      ellipsis: true,
      showTooltip: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: cloneDeep(sortableConfig.value),
      width: 180,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      filterConfig: {
        valueKey: 'key',
        labelKey: 'statusText',
        options: props.isPreview ? Object.values(executionResultMap) : [],
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      width: 150,
    },
  ];
  const lastStaticColumns: MsTableColumn = [
    {
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      ellipsis: true,
      showTooltip: true,
      width: 200,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      width: 120,
    },

    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUser',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      width: 100,
    },
  ];

  const testPlanNameColumns: MsTableColumn = [
    {
      title: 'report.plan.name',
      dataIndex: 'planName',
      showTooltip: true,
      width: 200,
    },
  ];

  const columns = computed(() => {
    if (props.isGroup) {
      return [...staticColumns, ...testPlanNameColumns, ...lastStaticColumns];
    }
    return [...staticColumns, ...lastStaticColumns];
  });

  const reportFeatureCaseList = () => {
    return !props.shareId ? getReportFeatureCaseList : getReportShareFeatureCaseList;
  };
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(reportFeatureCaseList(), {
    scroll: { x: '100%' },
    columns: columns.value,
    heightUsed: 20,
    showSelectorAll: false,
  });

  async function loadCaseList() {
    setLoadListParams({ reportId: props.reportId, shareId: props.shareId ?? undefined });
    loadList();
  }

  watch(
    [() => props.reportId, () => props.isPreview],
    () => {
      if (props.reportId && props.isPreview) {
        loadCaseList();
      } else {
        propsRes.value.data = detailTableExample[ReportCardTypeEnum.FUNCTIONAL_DETAIL];
      }
    },
    {
      immediate: true,
    }
  );

  const showDetailVisible = ref<boolean>(false);

  const executeReportId = ref<string>('');
  const executeList = ref<ExecuteHistoryItem[]>([]);
  const executeLoading = ref<boolean>(false);
  // 执行历史步骤
  async function getExecuteStep() {
    executeLoading.value = true;
    try {
      const res = await getFunctionalExecuteStep({
        reportId: executeReportId.value,
        shareId: props.shareId,
      });
      executeList.value = [res];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      executeLoading.value = false;
    }
  }

  function openExecuteHistory(record: FeatureCaseItem) {
    executeReportId.value = record.reportId;
    showDetailVisible.value = true;
    getExecuteStep();
  }
</script>
