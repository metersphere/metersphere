<template>
  <div :class="`${props.enabledTestSet ? 'test-set-wrapper' : ''}`">
    <MsBaseTable ref="tableRef" v-bind="propsRes" :row-class="getRowClass" v-on="propsEvent">
      <template #num="{ record }">
        <MsButton :disabled="!props.isPreview" type="text" @click="toDetail(record)">{{ record.num }}</MsButton>
      </template>
      <template #caseLevel="{ record }">
        <CaseLevel :case-level="record.priority" />
      </template>
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
        <ExecuteResult :execute-result="filterContent.key" />
      </template>
      <template #lastExecResult="{ record }">
        <ExecuteResult :execute-result="record.executeResult" />
        <MsButton class="ml-[8px]" :disabled="!props.isPreview || !record.reportId" @click="openExecuteHistory(record)">
          {{ t('common.detail') }}
        </MsButton>
      </template>
      <template #expand-icon="{ record, expanded }">
        <div
          class="flex items-end gap-[2px] text-[var(--color-text-4)]"
          :class="[
            expanded ? '!text-[rgb(var(--primary-5))]' : '',
            record.testSetCount === 0 ? 'cursor-not-allowed' : '',
          ]"
        >
          <MsIcon type="icon-icon_split_turn-down_arrow" />
          <div v-if="record.testSetCount" class="break-keep">{{ record.testSetCount }}</div>
        </div>
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
  </div>
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
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';

  import { ApiOrScenarioCaseItem } from '@/models/testPlan/report';
  import type { ExecuteHistoryItem } from '@/models/testPlan/testPlan';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';
  import {
    detailTableExample,
    lastStaticColumns,
    testPlanNameColumn,
  } from '@/views/test-plan/report/detail/component/reportConfig';

  const tableStore = useTableStore();

  const { openNewPage } = useOpenNewPage();

  const props = defineProps<{
    enabledTestSet: boolean; // 开启测试集
    reportId: string;
    testSetId?: string; // 测试集id
    shareId?: string;
    isPreview?: boolean;
  }>();
  const { t } = useI18n();

  const innerKeyword = defineModel<string>('keyword', {
    required: true,
  });

  const sortableConfig = computed<TableSortable | undefined>(() => {
    return props.isPreview
      ? {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        }
      : undefined;
  });
  const isGroup = inject<Ref<boolean>>('isPlanGroup', ref(false));

  const staticColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      width: 150,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: cloneDeep(sortableConfig.value),
      width: 180,
      showInTable: true,
      columnSelectorDisabled: true,
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
      showInTable: true,
      showDrag: true,
      width: 150,
    },
  ];
  const caseLastStaticColumns: MsTableColumn = [
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      showInTable: true,
      showDrag: true,
      width: 120,
    },
    ...lastStaticColumns,
    {
      title: '',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 80,
    },
  ];

  const columns = computed(() => {
    if (isGroup.value) {
      return [...staticColumns, ...testPlanNameColumn, ...caseLastStaticColumns];
    }
    return [...staticColumns, ...caseLastStaticColumns];
  });

  const reportFeatureCaseList = () => {
    return !props.shareId ? getReportFeatureCaseList : getReportShareFeatureCaseList;
  };

  const tableKey = computed(() => {
    if (props.isPreview) {
      return isGroup.value
        ? TableKeyEnum.TEST_PLAN_REPORT_FUNCTIONAL_TABLE_GROUP
        : TableKeyEnum.TEST_PLAN_REPORT_FUNCTIONAL_TABLE;
    }
    return TableKeyEnum.TEST_PLAN_REPORT_FUNCTIONAL_TABLE_NOT_PREVIEW;
  });

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(reportFeatureCaseList(), {
    tableKey: tableKey.value,
    columns: columns.value,
    scroll: { x: '100%' },
    heightUsed: 236,
    showSetting: props.isPreview,
    isSimpleSetting: true,
    isHiddenSetting: props.enabledTestSet,
    paginationSize: 'mini',
    showSelectorAll: false,
  });

  async function setCurrentPageSize() {
    const pageSize = await tableStore.getPageSize(tableKey.value);
    if (propsRes.value.msPagination && propsRes.value.msPagination.pageSize) {
      propsRes.value.msPagination.pageSize = pageSize;
    }
  }

  const tableRef = ref();
  async function loadCaseList() {
    await setCurrentPageSize();
    setLoadListParams({
      reportId: props.reportId,
      keyword: innerKeyword.value,
      shareId: props.shareId ?? undefined,
      collectionId: props.testSetId,
    });
    loadList();
  }
  // 跳转用例详情
  function toDetail(record: ApiOrScenarioCaseItem) {
    openNewPage(CaseManagementRouteEnum.CASE_MANAGEMENT_CASE, {
      id: record.id,
      pId: record.projectId,
    });
  }

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

  const selectedId = ref<string>('');
  function openExecuteHistory(record: ApiOrScenarioCaseItem) {
    executeReportId.value = record.reportId;
    selectedId.value = record.reportId;
    showDetailVisible.value = true;
    getExecuteStep();
  }

  function getRowClass(record: ApiOrScenarioCaseItem) {
    return record.reportId === selectedId.value && props.isPreview ? 'selected-row-class' : '';
  }

  watch(
    () => props.isPreview,
    (val) => {
      if (!val) {
        propsRes.value.data = detailTableExample[ReportCardTypeEnum.FUNCTIONAL_DETAIL];
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.reportId,
    (val) => {
      if (val) {
        loadCaseList();
      }
    }
  );

  onMounted(() => {
    if (props.isPreview && props.reportId) {
      loadCaseList();
    }
  });

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
