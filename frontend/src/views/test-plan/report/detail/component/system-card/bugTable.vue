<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #num="{ record }">
      <MsButton :disabled="!props.isPreview" type="text" @click="toDetail(record)">{{ record.num }}</MsButton>
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { TableSortable } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getReportBugList, getReportShareBugList } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { ReportBugItem } from '@/models/testPlan/report';
  import { BugManagementRouteEnum } from '@/enums/routeEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { detailTableExample } from '@/views/test-plan/report/detail/component/reportConfig';

  const { openNewPage } = useOpenNewPage();

  const { t } = useI18n();

  const props = defineProps<{
    reportId: string;
    shareId?: string;
    isPreview?: boolean;
  }>();

  const sortableConfig = computed<TableSortable | undefined>(() => {
    return props.isPreview
      ? {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        }
      : undefined;
  });

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: cloneDeep(sortableConfig.value),
      fixed: 'left',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      width: 150,
      showTooltip: true,
      sortable: cloneDeep(sortableConfig.value),
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'status',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUserName',
      showTooltip: true,
      width: 125,
    },
    {
      title: 'bugManagement.numberOfCase',
      dataIndex: 'relationCaseCount',
      width: 80,
    },
  ];
  const reportBugList = () => {
    return !props.shareId ? getReportBugList : getReportShareBugList;
  };
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(reportBugList(), {
    scroll: { x: '100%' },
    columns,
    showSelectorAll: false,
  });

  async function loadCaseList() {
    setLoadListParams({ reportId: props.reportId, shareId: props.shareId ?? undefined });
    loadList();
  }

  // 跳转缺陷详情
  function toDetail(record: ReportBugItem) {
    openNewPage(BugManagementRouteEnum.BUG_MANAGEMENT_INDEX, {
      id: record.id,
    });
  }

  watch(
    [() => props.reportId, () => props.isPreview],
    () => {
      if (props.reportId && props.isPreview) {
        loadCaseList();
      } else {
        propsRes.value.data = detailTableExample[ReportCardTypeEnum.BUG_DETAIL];
      }
    },
    {
      immediate: true,
    }
  );
</script>
