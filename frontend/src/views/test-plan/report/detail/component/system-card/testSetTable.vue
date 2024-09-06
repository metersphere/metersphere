<template>
  <MsBaseTable
    v-if="enabledTestSet"
    v-bind="propsRes"
    :expanded-keys="expandedKeys"
    :expandable="expandable"
    v-on="propsEvent"
    @expand="(record) => handleExpand(record.id as string)"
  >
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
</template>

<script setup lang="ts">
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ApiAndScenarioTable from '@/views/test-plan/report/detail/component/system-card/apiAndScenarioTable.vue';
  import FeatureCaseTable from '@/views/test-plan/report/detail/component/system-card/featureCaseTable.vue';

  import { getReportFeatureCaseList, getReportShareFeatureCaseList } from '@/api/modules/test-plan/report';

  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { detailTableExample } from '@/views/test-plan/report/detail/component/reportConfig';

  const props = defineProps<{
    enabledTestSet: boolean;
    activeType: ReportCardTypeEnum; // 卡片类型
    keyword: string;
    reportId: string;
    shareId?: string;
    isPreview?: boolean;
    isGroup?: boolean;
  }>();

  const expandedKeys = ref<string[]>([]);

  const expandable = reactive({
    title: '',
    width: 30,
    expandedRowRender: (record: Record<string, any>) => {
      if (record.testSetCount) {
        if (props.activeType === ReportCardTypeEnum.FUNCTIONAL_DETAIL) {
          return h(FeatureCaseTable, {
            keyword: props.keyword,
            reportId: props.reportId,
            shareId: props.shareId,
            isPreview: props.isPreview,
            isGroup: props.isGroup,
            enabledTestSet: props.enabledTestSet,
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
            isGroup: props.isGroup,
            enabledTestSet: props.enabledTestSet,
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

  const testSetColumns: MsTableColumn = [
    {
      title: 'ms.case.associate.testSet',
      dataIndex: 'testSetName',
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
  ];

  const reportFeatureCaseList = () => {
    return !props.shareId ? getReportFeatureCaseList : getReportShareFeatureCaseList;
  };
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(reportFeatureCaseList(), {
    columns: testSetColumns,
    scroll: { x: '100%' },
    heightUsed: 320,
    showSelectorAll: false,
  });
  // TODO 待联调等接口
  async function loadCaseList() {
    if (props.enabledTestSet) {
      expandedKeys.value = [];
    }
    // setLoadListParams({ reportId: props.reportId, keyword: props.keyword, shareId: props.shareId ?? undefined });
    // loadList();
  }

  watch(
    [() => props.reportId, () => props.isPreview],
    () => {
      if (props.reportId && props.isPreview) {
        // TODO 待联调等接口
        // loadCaseList();
      } else {
        propsRes.value.data = detailTableExample[ReportCardTypeEnum.FUNCTIONAL_DETAIL];
      }
    },
    {
      immediate: true,
    }
  );

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
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 12px !important;
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
</style>
