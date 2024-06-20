<template>
  <div class="p-[16px]">
    <div class="mb-4 flex items-center justify-between">
      <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
        <a-radio value="All">{{ t('report.all') }}</a-radio>
        <a-radio value="INDEPENDENT">{{ t('report.detail.testReport') }}</a-radio>
        <a-radio value="INTEGRATED">{{ t('report.detail.testPlanGroupReport') }}</a-radio>
      </a-radio-group>
      <div class="items-right flex gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('project.menu.nameSearch')"
          allow-clear
          class="mx-[8px] w-[240px]"
          @search="searchList"
          @press-enter="searchList"
          @clear="searchList"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="initData()">
          <template #icon>
            <icon-refresh class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
      </div>
    </div>
    <!-- 报告列表 -->
    <ms-base-table
      v-bind="propsRes"
      ref="tableRef"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
      @filter-change="filterChange"
    >
      <template #name="{ record }">
        <div
          type="text"
          class="one-line-text flex w-full text-[rgb(var(--primary-5))]"
          @click="showReportDetail(record.id, record.integrated)"
        >
          {{ characterLimit(record.name) }}
        </div>
      </template>
      <template #integrated="{ record }">
        <MsTag theme="light" :type="record.integrated ? 'primary' : undefined">
          {{
            record.integrated
              ? t('report.detail.testPlanGroupReport')
              : t('report.detail.testReport')
          }}
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
      <template #execStatus="{ record }">
        <ExecStatus :status="record.execStatus" />
      </template>
      <template #[FilterSlotNameEnum.TEST_PLAN_REPORT_EXEC_STATUS]="{ filterContent }">
        <ExecStatus :status="filterContent.value" />
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
        <MsButton
          v-permission="['PROJECT_TEST_PLAN_REPORT:READ+DELETE']"
          class="!mr-0"
          @click="handleDelete(record.id, record.name)"
        >
          {{ t('ms.comment.delete') }}
        </MsButton>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import ExecStatus from '@/views/test-plan/report/component/execStatus.vue';
  import ExecutionStatus from '@/views/test-plan/report/component/reportStatus.vue';

  import { reportBathDelete, reportDelete, reportList, reportRename } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useTableStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { ReportExecStatus } from '@/enums/apiEnum';
  import { PlanReportStatus, TriggerModeLabel } from '@/enums/reportEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const { openModal } = useModal();

  const appStore = useAppStore();
  const tableStore = useTableStore();
  const { t } = useI18n();
  const keyword = ref<string>('');
  const router = useRouter();
  const route = useRoute();

  type ReportShowType = 'All' | 'INDEPENDENT' | 'INTEGRATED';
  const showType = ref<ReportShowType>('All');

  const executeResultOptions = computed(() => {
    return Object.values(ReportExecStatus).map((e) => {
      return {
        value: e,
        key: e,
      };
    });
  });

  const statusResultOptions = computed(() => {
    return Object.keys(PlanReportStatus).map((key) => {
      return {
        value: key,
        label: PlanReportStatus[key].statusText,
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
      title: 'report.execStatus',
      dataIndex: 'execStatus',
      slotName: 'execStatus',
      filterConfig: {
        options: executeResultOptions.value,
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_REPORT_EXEC_STATUS,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
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
      width: 200,
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
  const { propsRes, propsEvent, loadList, setLoadListParams, setPagination, resetSelector, resetFilterParams } =
    useTable(
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
    });
    loadList();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.delete',
        eventTag: 'batchStop',
        permission: ['PROJECT_TEST_PLAN_REPORT:READ+DELETE'],
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
      },
      projectId: appStore.currentProjectId,
    };

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
  };

  function searchList() {
    resetSelector();
    initData();
  }

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

  onBeforeMount(() => {
    if (route.query.id) {
      showReportDetail(route.query.id as string, route.query.type === 'GROUP');
    }
    initData();
  });
</script>

<style lang="less" scoped>
  .ms-table--special-small();
</style>
