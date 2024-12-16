<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="font-medium"> {{ props.label }} </div>
    <div v-if="props.isPreview" class="flex items-center">
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('common.searchByIdName')"
        allow-clear
        class="w-[240px]"
        @search="loadCaseList"
        @press-enter="loadCaseList"
        @clear="loadCaseList"
      />
    </div>
  </div>
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
  import useTableStore from '@/hooks/useTableStore';

  import { ReportBugItem } from '@/models/testPlan/report';
  import { BugManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { detailTableExample } from '@/views/test-plan/report/detail/component/reportConfig';

  const { t } = useI18n();

  const { openNewPage } = useOpenNewPage();
  const tableStore = useTableStore();

  const props = defineProps<{
    reportId: string;
    shareId?: string;
    isPreview?: boolean;
    label: string;
  }>();

  const keyword = ref<string>('');

  const sortableConfig = computed<TableSortable | undefined>(() => {
    return props.isPreview
      ? {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        }
      : undefined;
  });
  const isGroup = inject<Ref<boolean>>('isPlanGroup', ref(false));

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: cloneDeep(sortableConfig.value),
      width: 100,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      width: 150,
      showTooltip: true,
      sortable: cloneDeep(sortableConfig.value),
      columnSelectorDisabled: true,
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'statusName',
      width: 100,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUserName',
      showTooltip: true,
      showDrag: true,
      width: 125,
    },
    {
      title: 'bugManagement.numberOfCase',
      dataIndex: 'relationCaseCount',
      showDrag: true,
      width: 80,
    },
    {
      title: '',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 30,
    },
  ];
  const reportBugList = () => {
    return !props.shareId ? getReportBugList : getReportShareBugList;
  };

  const tableKey = computed(() => {
    if (props.isPreview) {
      return isGroup.value
        ? TableKeyEnum.TEST_PLAN_REPORT_BUG_TABLE_DETAIL_GROUP
        : TableKeyEnum.TEST_PLAN_REPORT_BUG_TABLE_DETAIL;
    }
    return TableKeyEnum.TEST_PLAN_REPORT_BUG_TABLE_DETAIL_NOT_PREVIEW;
  });
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(reportBugList(), {
    columns,
    tableKey: tableKey.value,
    scroll: { x: '100%' },
    showSetting: props.isPreview,
    isSimpleSetting: true,
    showSelectorAll: false,
  });

  function loadCaseList() {
    setLoadListParams({ reportId: props.reportId, shareId: props.shareId ?? undefined, keyword: keyword.value });
    loadList();
  }

  // 跳转缺陷详情
  function toDetail(record: ReportBugItem) {
    openNewPage(BugManagementRouteEnum.BUG_MANAGEMENT_INDEX, {
      id: record.id,
    });
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

  watch(
    () => props.isPreview,
    (val) => {
      if (!val) {
        propsRes.value.data = detailTableExample[ReportCardTypeEnum.BUG_DETAIL];
      }
    },
    {
      immediate: true,
    }
  );
  await tableStore.initColumn(tableKey.value, columns, 'drawer');
</script>
