<template>
  <MsBaseTable
    v-if="enabledTestSet"
    v-bind="propsRes"
    :expanded-keys="expandedKeys"
    :expandable="expandable"
    row-class="test-set-expand-tr"
    v-on="propsEvent"
    @expand="(record) => handleExpand(record.id as string)"
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
    <template #other>
      <span></span>
    </template>
    <template #empty>
      <span></span>
    </template>
    <template #outContent>
      <span></span>
    </template>
    <template #operation>
      <ColumnSelectorIcon
        :table-key="props.tableKey"
        :is-simple="true"
        :only-page-size="false"
        :show-pagination="true"
        @init-data="handleInitColumn"
        @page-size-change="pageSizeChange"
      />
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import ColumnSelectorIcon from '@/components/pure/ms-table/columnSelectorIcon.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ApiAndScenarioTable from '@/views/test-plan/report/detail/component/system-card/apiAndScenarioTable.vue';
  import FeatureCaseTable from '@/views/test-plan/report/detail/component/system-card/featureCaseTable.vue';

  import { getCollectApiPage, getCollectFunctionalPage, getCollectScenarioPage } from '@/api/modules/test-plan/report';
  import useTableStore from '@/hooks/useTableStore';

  import type { SelectedReportCardTypes } from '@/models/testPlan/testPlanReport';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { getApiDetailColumn, getFeatureColumns } from '@/views/test-plan/report/utils';

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
    (e: 'pageSizeChange', pageSize: number): void;
    (e: 'initColumn'): void;
  }>();

  const expandedKeys = defineModel<string[]>('expandedKeys', {
    required: true,
  });

  const isGroup = inject<Ref<boolean>>('isPlanGroup', ref(false));

  const expandable = reactive({
    title: '',
    width: 30,
    expandedRowRender: (record: Record<string, any>) => {
      if (record.count) {
        if (props.activeType === ReportCardTypeEnum.FUNCTIONAL_DETAIL) {
          return h(FeatureCaseTable, {
            keyword: props.keyword,
            reportId: props.reportId,
            shareId: props.shareId,
            isPreview: props.isPreview,
            isGroup: isGroup.value,
            enabledTestSet: props.enabledTestSet,
            testSetId: record.id,
          });
        }
        if (
          props.activeType === ReportCardTypeEnum.API_CASE_DETAIL ||
          props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL
        ) {
          return h(ApiAndScenarioTable, {
            activeType: props.activeType,
            keyword: props.keyword,
            reportId: props.reportId,
            shareId: props.shareId,
            isPreview: props.isPreview,
            isGroup: isGroup.value,
            enabledTestSet: props.enabledTestSet,
            testSetId: record.id,
          });
        }
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

  const columns = computed<MsTableColumn>(() => {
    if (isGroup.value) {
      return [
        {
          title: 'ms.case.associate.testSet',
          dataIndex: 'name',
          showInTable: true,
          showDrag: true,
          width: 200,
        },
        {
          title: 'report.plan.name',
          dataIndex: 'planName',
          showInTable: true,
          showDrag: true,
          width: 300,
        },
        {
          title: '',
          dataIndex: 'other',
          slotName: 'other',
          showInTable: true,
          showDrag: true,
          width: 300,
        },
        {
          title: '',
          dataIndex: 'other',
          slotName: 'other',
          showInTable: true,
          showDrag: true,
          width: 300,
        },
        {
          title: '',
          titleSlotName: 'operation',
          slotName: 'outContent',
          width: 30,
        },
      ];
    }
    return [
      {
        title: 'ms.case.associate.testSet',
        dataIndex: 'name',
        showInTable: true,
        showDrag: true,
        width: 200,
      },
      // 字段很少第一级别靠左展示，填充表头
      {
        title: '',
        dataIndex: 'other',
        slotName: 'other',
        showInTable: true,
        showDrag: true,
        width: 300,
      },
      {
        title: '',
        dataIndex: 'empty',
        slotName: 'empty',
        showInTable: true,
        showDrag: true,
        width: 300,
      },
      {
        title: '',
        dataIndex: 'empty',
        slotName: 'empty',
        showInTable: true,
        showDrag: true,
        width: 300,
      },
      {
        title: '',
        titleSlotName: 'operation',
        slotName: 'outContent',
        width: 30,
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
    isSimpleSetting: true,
    showSelectorAll: false,
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

  // 页码改变
  async function pageSizeChange(pageSize: number) {
    emit('pageSizeChange', pageSize);
  }
  // 列配置改变
  async function handleInitColumn() {
    emit('initColumn');
  }

  function getTestSetColumns(): MsTableColumn | undefined {
    const apiAndScenarioDetail = [ReportCardTypeEnum.API_CASE_DETAIL, ReportCardTypeEnum.SCENARIO_CASE_DETAIL];
    if (props.activeType === ReportCardTypeEnum.FUNCTIONAL_DETAIL) {
      return getFeatureColumns(isGroup.value, props.isPreview);
    }
    if (apiAndScenarioDetail.includes(props.activeType)) {
      return getApiDetailColumn(isGroup.value, props.isPreview);
    }
  }

  async function initSetColumnConfig() {
    const detailColumns = await getTestSetColumns();
    if (detailColumns && props.enabledTestSet) {
      await tableStore.initColumn(props.tableKey, detailColumns, 'drawer');
    }
  }

  initSetColumnConfig();

  defineExpose({
    loadCaseList,
  });
</script>

<style scoped lang="less">
  :deep(.arco-table-tr-expand .arco-table-td) {
    border-bottom: 1px solid var(--color-text-n8) !important;
    background: none;
  }
  :deep(.arco-table-tr-expand) {
    & .arco-table-td:last-child {
      border-bottom: 1px solid transparent !important;
      background: none;
    }
  }
  :deep(.arco-table-tr-expand) {
    background: var(--color-text-n9) !important;
  }
  :deep(.arco-table .arco-table-expand-btn:hover) {
    border-color: transparent;
  }
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
        padding: 8px !important;
      }
    }
  }
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 8px 16px !important;
  }
  :deep(.arco-table-cell-with-sorter) {
    margin: 8px 0 !important;
  }
  :deep(.arco-table .arco-table-tr-expand .arco-table-td .arco-table) {
    margin: 0 !important;
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
  :deep(.arco-table-tr-expand .arco-table-td .arco-table .arco-table-td) {
    border-color: var(--color-text-n8) !important;
  }
  :deep(.arco-table-tr.test-set-expand-tr) {
    &:hover {
      > .arco-table-td {
        background: white !important;
      }
    }
  }
</style>
