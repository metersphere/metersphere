<template>
  <div class="p-[16px]">
    <MsAdvanceFilter
      ref="msAdvanceFilterRef"
      v-model:keyword="keyword"
      :view-type="ViewTypeEnum.TEST_PLAN_REPORT"
      :filter-config-list="filterConfigList"
      :search-placeholder="t('project.menu.nameSearch')"
      @keyword-search="searchList()"
      @adv-search="handleAdvSearch"
      @refresh="initData()"
    >
      <template #left>
        <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
          <a-radio value="All">{{ t('report.all') }}</a-radio>
          <a-radio value="INDEPENDENT">{{ t('report.detail.testReport') }}</a-radio>
          <a-radio value="INTEGRATED">{{ t('report.detail.testPlanGroupReport') }}</a-radio>
        </a-radio-group>
      </template>
    </MsAdvanceFilter>
    <!-- 报告列表 -->
    <ms-base-table
      v-bind="propsRes"
      ref="tableRef"
      class="mt-4"
      :action-config="tableBatchActions"
      :not-show-table-filter="isAdvancedSearchMode"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
      @filter-change="filterChange"
    >
      <template #name="{ record }">
        <div class="one-line-text text-[rgb(var(--primary-5))]" @click="showReportDetail(record.id, record.integrated)">
          {{ record.name }}
        </div>
      </template>
      <template #integrated="{ record }">
        <MsTag theme="light" :type="record.integrated ? 'primary' : undefined">
          {{ record.integrated ? t('report.detail.testPlanGroupReport') : t('report.detail.testReport') }}
        </MsTag>
      </template>

      <!-- 通过率 -->
      <template #passRateColumn>
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t('report.passRate') }}
          <a-tooltip :content="t('report.passRateTip')" position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
      </template>
      <template #passRate="{ record }">
        <div class="text-[var(--color-text-1)]">
          {{ `${record.passRate || '0.00'}%` }}
        </div>
      </template>
      <!-- 执行状态筛选 -->
      <template #resultStatus="{ record }">
        <ExecutionStatus v-if="record.resultStatus !== '-'" :status="record.resultStatus" />
      </template>
      <template #[FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER]="{ filterContent }">
        <ExecutionStatus :status="filterContent.value" />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode as keyof typeof TriggerModeLabel]) }}</span>
      </template>
      <template #operationTime="{ record }">
        <span>{{ dayjs(record.operationTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #operation="{ record }">
        <MsButton v-permission="['PROJECT_TEST_PLAN_REPORT:READ+DELETE']" @click="handleDelete(record.id, record.name)">
          {{ t('ms.comment.delete') }}
        </MsButton>
        <MsButton
          v-permission="['PROJECT_TEST_PLAN_REPORT:READ+EXPORT']"
          class="!mr-0"
          @click="() => exportPdf(record, record.integrated)"
        >
          {{ t('common.export') }}
        </MsButton>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsAdvanceFilter from '@/components/pure/ms-advance-filter/index.vue';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  // import ExecStatus from '@/views/test-plan/report/component/execStatus.vue';
  import ExecutionStatus from '@/views/test-plan/report/component/reportStatus.vue';

  import { reportBathDelete, reportDelete, reportList, reportRename } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useTableStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  // import { ReportExecStatus } from '@/enums/apiEnum';
  import { PlanReportStatus, TriggerModeLabel } from '@/enums/reportEnum';
  import { FullPageEnum, TestPlanRouteEnum } from '@/enums/routeEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const { openModal } = useModal();

  const appStore = useAppStore();
  const tableStore = useTableStore();
  const cacheStore = useCacheStore();

  const { t } = useI18n();
  const keyword = ref<string>('');
  const router = useRouter();
  const route = useRoute();
  const { openNewPage, openNewPageWithParams } = useOpenNewPage();

  type ReportShowType = 'All' | 'INDEPENDENT' | 'INTEGRATED';
  const localSHowTypeKey = 'testPlanReportShowType';
  const showType = ref<ReportShowType>((localStorage.getItem(localSHowTypeKey) as ReportShowType) || 'All');

  // const executeResultOptions = computed(() => {
  //   return Object.values(ReportExecStatus).map((e) => {
  //     return {
  //       value: e,
  //       key: e,
  //     };
  //   });
  // });

  const statusResultOptions = computed(() => {
    return Object.keys(PlanReportStatus).map((key) => {
      return {
        value: key,
        label: t(PlanReportStatus[key].label),
      };
    });
  });

  const triggerModeOptions = computed(() => {
    return Object.keys(TriggerModeLabel).map((key) => {
      return {
        value: key,
        label: t(TriggerModeLabel[key as keyof typeof TriggerModeLabel]),
      };
    });
  });

  const integratedFilters = computed(() => {
    if (showType.value === 'All') {
      return undefined;
    }
    if (showType.value === 'INTEGRATED') {
      return [true];
    }
    return [false];
  });

  const columns: MsTableColumn = [
    {
      title: 'report.name',
      dataIndex: 'name',
      slotName: 'name',
      width: 200,
      showInTable: true,
      showTooltip: true,
      editType: hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: false,
      columnSelectorDisabled: true,
    },
    {
      title: 'report.type',
      slotName: 'integrated',
      dataIndex: 'integrated',
      width: 150,
      showDrag: true,
    },
    {
      title: 'report.plan.name',
      slotName: 'planName',
      dataIndex: 'planName',
      width: 200,
      showInTable: true,
      showTooltip: true,
      showDrag: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'report.result',
      dataIndex: 'resultStatus',
      slotName: 'resultStatus',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: statusResultOptions.value,
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER,
      },
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'report.passRate',
      slotName: 'passRate',
      titleSlotName: 'passRateColumn',
      showDrag: true,
      width: 200,
    },
    {
      title: 'report.trigger.mode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      showInTable: true,
      width: 150,
      showDrag: true,
      filterConfig: {
        options: triggerModeOptions.value,
      },
    },
    {
      title: 'report.operator',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 300,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'report.operating',
      dataIndex: 'createTime',
      slotName: 'createTime',
      width: 180,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      title: hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+DELETE']) ? 'common.operation' : '',
      width: hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+DELETE']) ? 130 : 50,
    },
  ];

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_REPORT_TABLE, columns, 'drawer');

  const rename = async (record: any) => {
    try {
      await reportRename(record.id, record.name);
      Message.success(t('common.updateSuccess'));
      return true;
    } catch (error) {
      return false;
    }
  };
  const {
    propsRes,
    propsEvent,
    viewId,
    advanceFilter,
    setAdvanceFilter,
    loadList,
    setLoadListParams,
    setPagination,
    resetSelector,
    resetFilterParams,
  } = useTable(
    reportList,
    {
      tableKey: TableKeyEnum.TEST_PLAN_REPORT_TABLE,
      scroll: {
        x: '100%',
      },
      showSetting: true,
      selectable: hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+DELETE']),
      heightUsed: 242,
      paginationSize: 'mini',
      showSelectorAll: true,
    },
    (item) => ({
      ...item,
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
    }),
    rename
  );

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);

  function initData(dataIndex?: string, value?: string[] | (string | number | boolean)[] | undefined) {
    const filterParams = {
      ...propsRes.value.filter,
    };
    if (dataIndex && value) {
      filterParams[dataIndex] = value;
    }
    setLoadListParams({
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      filter: { ...filterParams, integrated: integratedFilters.value },
      viewId: viewId.value,
      combineSearch: advanceFilter,
    });
    loadList();
  }

  function searchList() {
    resetSelector();
    initData();
  }

  const filterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'report.name',
      dataIndex: 'name',
      type: FilterType.INPUT,
    },
    {
      title: 'report.plan.name',
      dataIndex: 'testPlanName',
      type: FilterType.INPUT,
    },
    {
      title: 'report.result',
      dataIndex: 'resultStatus',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: statusResultOptions.value,
      },
    },
    {
      title: 'report.passRate',
      dataIndex: 'passRate',
      type: FilterType.NUMBER,
      numberProps: {
        min: 0,
        suffix: '%',
      },
    },
    {
      title: 'report.trigger.mode',
      dataIndex: 'triggerMode',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: triggerModeOptions.value,
      },
    },
    {
      title: 'common.creator',
      dataIndex: 'createUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
  ]);
  // 高级检索
  const handleAdvSearch = async (filter: FilterResult, id: string) => {
    keyword.value = '';
    setAdvanceFilter(filter, id);
    searchList(); // 基础筛选都清空
  };

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.delete',
        eventTag: 'batchStop',
        permission: ['PROJECT_TEST_PLAN_REPORT:READ+DELETE'],
      },
      {
        label: 'common.export',
        eventTag: 'batchExport',
        permission: ['PROJECT_TEST_PLAN_REPORT:READ+EXPORT'],
      },
    ],
  };

  const batchParams = ref<BatchApiParams>({
    selectIds: [],
    selectAll: false,
    excludeIds: [] as string[],
    condition: {},
  });

  // 批量删除
  const handleTableBatch = async (event: BatchActionParams, params: BatchActionQueryParams) => {
    batchParams.value = {
      ...params,
      selectIds: params?.selectedIds || [],
      condition: {
        filter: { ...propsRes.value.filter, integrated: integratedFilters.value },
        keyword: keyword.value,
        viewId: viewId.value,
        combineSearch: advanceFilter,
      },
      projectId: appStore.currentProjectId,
    };

    if (event.eventTag === 'batchExport') {
      openNewPageWithParams(
        FullPageEnum.FULL_PAGE_TEST_PLAN_EXPORT_PDF,
        {
          type: showType.value,
        },
        batchParams.value
      );
    } else if (event.eventTag === 'batchStop') {
      openModal({
        type: 'error',
        title: t('report.delete.tip', {
          count: params?.currentSelectCount || params?.selectedIds?.length,
        }),
        content: '',
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            await reportBathDelete(batchParams.value);
            Message.success(t('apiTestDebug.deleteSuccess'));
            resetSelector();
            initData();
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
    }
  };

  const handleDelete = async (id: string, currentName: string) => {
    openModal({
      type: 'error',
      title: t('apiTestManagement.deleteApiTipTitle', { name: characterLimit(currentName) }),
      content: '',
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await reportDelete(id);
          Message.success(t('apiTestDebug.deleteSuccess'));
          initData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  function changeShowType(val: string | number | boolean) {
    showType.value = val as ReportShowType;
    localStorage.setItem(localSHowTypeKey, val as string);
    resetFilterParams();
    // 重置分页
    setPagination({
      current: 1,
    });
    searchList();
  }

  function filterChange(dataIndex: string, value: string[] | (string | number | boolean)[] | undefined) {
    initData(dataIndex, value);
  }

  /**
   * 报告详情 showReportDetail
   */
  function showReportDetail(id: string, type: boolean) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_REPORT_DETAIL,
      query: {
        id,
        type: type ? 'GROUP' : 'TEST_PLAN',
      },
    });
  }

  function exportPdf(record: any, type: boolean) {
    openNewPage(FullPageEnum.FULL_PAGE_TEST_PLAN_EXPORT_PDF, {
      id: record.id,
      type: type ? 'GROUP' : 'TEST_PLAN',
    });
  }

  const isActivated = computed(() => cacheStore.cacheViews.includes(TestPlanRouteEnum.TEST_PLAN_REPORT));

  onBeforeMount(() => {
    if (route.query.id) {
      showReportDetail(route.query.id as string, route.query.type === 'GROUP');
    }
    initData();
  });

  onActivated(() => {
    if (isActivated.value) {
      if (route.query.id) {
        showReportDetail(route.query.id as string, route.query.type === 'GROUP');
      }
      initData();
    }
  });
</script>

<style lang="less" scoped>
  .ms-table--special-small();
</style>
