<template>
  <MsBaseTable
    v-if="enabledTestSet"
    ref="tableRef"
    v-bind="propsRes"
    :expanded-keys="expandedKeys"
    :expandable="expandable"
    row-class="test-set-expand-tr"
    v-on="propsEvent"
    @sorter-change="handleInitColumn"
    @expand="(record) => handleExpand(record.id as string)"
    @column-resize="getActualColumnWidth"
    @init-end="getActualColumnWidth"
  >
    <template #expand-icon="{ record, expanded }">
      <div
        class="flex items-end gap-[2px] text-[var(--color-text-4)]"
        :class="[expanded ? '!text-[rgb(var(--primary-5))]' : '', record.count === 0 ? 'cursor-not-allowed' : '']"
      >
        <MsIcon type="icon-icon_split_turn-down_arrow" />
        <div v-if="record.count" class="break-keep">{{ record.count }}</div>
      </div>
    </template>
    <template #name>
      <span></span>
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
      <ExecuteResult :execute-result="filterContent.key" />
    </template>
    <template #[FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS]="{ filterContent }">
      <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
      <caseLevel :case-level="filterContent.value" />
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { useResizeObserver } from '@vueuse/core';
  import { debounce } from 'lodash-es';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import TestSetTableChildrenList from '@/views/test-plan/report/detail/component/system-card/testSetTableChildrenList.vue';

  import { getCollectApiPage, getCollectFunctionalPage, getCollectScenarioPage } from '@/api/modules/test-plan/report';
  import useTableStore from '@/hooks/useTableStore';

  import type { SelectedReportCardTypes } from '@/models/testPlan/testPlanReport';
  import { ReportEnum } from '@/enums/reportEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { casePriorityOptions, lastReportStatusListOptions } from '@/views/api-test/components/config';
  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';
  import {
    collectionNameColumn,
    lastStaticColumns,
    testPlanNameColumn,
  } from '@/views/test-plan/report/detail/component/reportConfig';

  const tableStore = useTableStore();

  const props = defineProps<{
    enabledTestSet: boolean;
    activeType: ReportCardTypeEnum; // 卡片类型
    keyword: string;
    reportId: string;
    shareId?: string;
    isPreview?: boolean;
    tableKey: TableKeyEnum;
  }>();

  const emit = defineEmits<{
    (e: 'initColumn'): void;
  }>();

  const expandedKeys = defineModel<string[]>('expandedKeys', {
    required: true,
  });

  const isGroup = inject<Ref<boolean>>('isPlanGroup', ref(false));

  const tableRef = ref();

  const actualColumnWidth = ref<number[]>([]);
  async function getActualColumnWidth() {
    await nextTick();
    const thElements = tableRef.value?.$el
      .querySelector('.arco-table-tr')
      ?.querySelectorAll('.arco-table-th') as HTMLElement[];
    actualColumnWidth.value = Array.from(thElements).map((th) => th.clientWidth);
  }

  useResizeObserver(
    tableRef,
    debounce(() => {
      getActualColumnWidth();
    }, 300)
  );

  const expandable = reactive({
    title: '',
    width: 30,
    expandedRowRender: (record: Record<string, any>) => {
      if (record.count) {
        return h(TestSetTableChildrenList, {
          list: record.reportDetailCaseList,
          activeType: props.activeType,
          tableKey: props.tableKey,
          reportId: props.reportId,
          shareId: props.shareId,
          actualColumnWidth: actualColumnWidth.value,
        });
      }
      return undefined;
    },
  });

  const handleExpand = (rowKey: string) => {
    if (expandedKeys.value.includes(rowKey)) {
      expandedKeys.value = expandedKeys.value.filter((key) => key !== rowKey);
    } else {
      expandedKeys.value = [...expandedKeys.value, rowKey];
    }
  };

  const featureStaticColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      width: 150,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      slotName: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
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
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      showInTable: true,
      showDrag: true,
      width: 120,
    },
  ];
  const apiStaticColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
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
      showDrag: true,
      sortable: props.isPreview
        ? {
            sortDirections: ['ascend', 'descend'],
            sorter: true,
          }
        : undefined,
      width: 100,
      showInTable: true,
    },
  ];
  const columns = computed<MsTableColumn>(() => {
    return [
      ...collectionNameColumn,
      ...(isGroup.value ? testPlanNameColumn : []),
      ...(props.activeType === ReportCardTypeEnum.FUNCTIONAL_DETAIL ? featureStaticColumns : apiStaticColumns),
      ...lastStaticColumns,
      {
        title: '',
        dataIndex: 'operation',
        slotName: 'operation',
        width: 80,
      },
    ];
  });

  const apiMap: Record<SelectedReportCardTypes, any> = {
    [ReportCardTypeEnum.FUNCTIONAL_DETAIL]: getCollectFunctionalPage,
    [ReportCardTypeEnum.API_CASE_DETAIL]: getCollectApiPage,
    [ReportCardTypeEnum.SCENARIO_CASE_DETAIL]: getCollectScenarioPage,
  };

  const getReportTestSetList = () => {
    return apiMap[props.activeType as SelectedReportCardTypes];
  };

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getReportTestSetList(), {
    columns: columns.value,
    scroll: { x: '100%' },
    heightUsed: 320,
    tableKey: props.tableKey,
    showSetting: true,
    isSimpleSetting: true,
    showSelectorAll: false,
    emptyDataShowLine: false,
  });

  function loadCaseList() {
    if (props.enabledTestSet) {
      expandedKeys.value = [];
    }
    setLoadListParams({ reportId: props.reportId, keyword: props.keyword, shareId: props.shareId });
    loadList();
  }

  onMounted(() => {
    if (props.reportId) {
      loadCaseList();
    }
  });

  watch(
    () => props.reportId,
    (val) => {
      if (val) {
        loadCaseList();
      }
    }
  );

  // 列配置改变
  async function handleInitColumn() {
    emit('initColumn');
  }

  async function initSetColumnConfig() {
    if (props.enabledTestSet) {
      const tmpArr = await tableStore.getStoreColumns(props.tableKey);
      const columnsConfig = tmpArr?.length
        ? [...collectionNameColumn, ...tmpArr.filter((item) => item.dataIndex !== 'collectionName')]
        : columns.value;
      await tableStore.initColumn(props.tableKey, columnsConfig, 'drawer');
    }
  }

  defineExpose({
    loadCaseList,
  });

  await initSetColumnConfig();
</script>

<style scoped lang="less">
  .ms-table-expand :deep(.arco-scrollbar-container + .arco-scrollbar-track-direction-vertical) {
    left: 0 !important;
  }
  :deep(.arco-table-content + .arco-scrollbar-track-direction-vertical .arco-scrollbar-thumb-direction-vertical) {
    height: 0 !important;
  }
  :deep(.arco-table-tr) {
    .arco-table-th {
      .arco-table-cell {
        padding: 8px 16px !important;
      }
    }
  }
  :deep(.arco-table-tr-expand) {
    > .arco-table-td {
      > .arco-table-cell {
        padding: 0 !important;
      }
    }
  }
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 8px 16px !important;
  }
  :deep(.arco-table-cell-with-sorter) {
    margin: 8px 0 !important;
  }
  :deep(.arco-table-tr-expand):hover {
    .arco-table-tr {
      .arco-table-td {
        background: none !important;
      }
      .arco-table-col-fixed-left.arco-table-col-fixed-left-last {
        background: none !important;
      }
      .arco-table-td.arco-table-col-fixed-left::before {
        background: none !important;
      }
    }
  }
  :deep(.arco-table-tr.test-set-expand-tr) {
    &:hover {
      > .arco-table-td {
        background: var(--color-text-fff) !important;
      }
    }
  }
</style>
