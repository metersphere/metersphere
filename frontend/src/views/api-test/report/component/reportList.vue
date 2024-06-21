<template>
  <div class="px-[16px]">
    <div class="flex items-center justify-between">
      <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
        <a-radio value="All">{{ t('report.all') }}</a-radio>
        <a-radio value="INDEPENDENT">{{ t('report.independent') }}</a-radio>
        <a-radio value="INTEGRATED">{{ t('report.collection') }}</a-radio>
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
      <template #name="{ record, rowIndex }">
        <div class="one-line-text text-[rgb(var(--primary-5))]" @click="showReportDetail(record.id, rowIndex)">{{
          record.name
        }}</div>
      </template>
      <!-- 报告类型 -->
      <template #integrated="{ record }">
        <MsTag theme="light" :type="record.integrated ? 'primary' : undefined">
          {{ record.integrated ? t('report.collection') : t('report.independent') }}
        </MsTag>
      </template>
      <template #status="{ record }">
        <ExecutionStatus
          :module-type="props.moduleType"
          :status="record.status"
          :script-identifier="props.moduleType === ReportEnum.API_SCENARIO_REPORT ? record.scriptIdentifier : null"
        />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #execStatus="{ record }">
        <ExecStatus :status="record.execStatus" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT]="{ filterContent }">
        <ExecStatus :status="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_REPORT_TYPE]="{ filterContent }">
        <MsTag theme="light" :type="filterContent.value ? 'primary' : undefined">
          {{ filterContent.value ? t('report.collection') : t('report.independent') }}
        </MsTag>
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode as keyof typeof TriggerModeLabel]) }}</span>
      </template>
      <template #operationTime="{ record }">
        <span>{{ dayjs(record.operationTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #operation="{ record }">
        <MsButton
          v-permission="['PROJECT_API_REPORT:READ+DELETE']"
          class="!mr-0"
          @click="handleDelete(record.id, record.name)"
          >{{ t('ms.comment.delete') }}</MsButton
        >
      </template>
    </ms-base-table>
    <ReportDetailDrawer
      v-model:visible="showDetailDrawer"
      :report-id="activeDetailId"
      :active-report-index="activeReportIndex"
      :table-data="propsRes.data"
      :page-change="propsEvent.pageChange"
      :pagination="propsRes.msPagination!"
      :show-type="showType"
      :share-time="shareTime"
    />
    <CaseReportDrawer
      v-model:visible="showCaseDetailDrawer"
      :report-id="activeDetailId"
      :active-report-index="activeReportIndex"
      :table-data="propsRes.data"
      :page-change="propsEvent.pageChange"
      :pagination="propsRes.msPagination!"
      :share-time="shareTime"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import CaseReportDrawer from './caseReportDrawer.vue';
  import ReportDetailDrawer from './reportDetailDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import ExecStatus from '@/views/test-plan/report/component/execStatus.vue';

  import {
    getShareTime,
    reportBathDelete,
    reportDelete,
    reportList,
    reportRename,
  } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useTableStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { ReportExecStatus } from '@/enums/apiEnum';
  import { ReportEnum, ReportStatus, TriggerModeLabel } from '@/enums/reportEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { triggerModeOptions } from '@/views/api-test/report/utils';

  const { openModal } = useModal();

  const appStore = useAppStore();
  const tableStore = useTableStore();
  const route = useRoute();

  const { t } = useI18n();
  const props = defineProps<{
    moduleType: keyof typeof ReportEnum;
    name: string;
  }>();
  const keyword = ref<string>('');

  type ReportShowType = 'All' | 'INDEPENDENT' | 'INTEGRATED';
  const showType = ref<ReportShowType>('All');

  const statusList = computed(() => {
    return Object.keys(ReportStatus).map((key) => {
      return {
        value: key,
        label: t(ReportStatus[key].label),
      };
    });
  });

  const ExecStatusList = computed(() => {
    return Object.values(ReportExecStatus).map((e) => {
      return {
        value: e,
        key: e,
      };
    });
  });
  const columns: MsTableColumn = [
    {
      title: 'report.name',
      dataIndex: 'name',
      slotName: 'name',
      width: 300,
      showInTable: true,
      showTooltip: true,
      editType: hasAnyPermission(['PROJECT_API_REPORT:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      ellipsis: true,
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
      title: 'report.result',
      dataIndex: 'status',
      slotName: 'status',
      filterConfig: {
        options: statusList.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS,
      },
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'report.status',
      dataIndex: 'execStatus',
      slotName: 'execStatus',
      filterConfig: {
        options: ExecStatusList.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT,
      },
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'report.trigger.mode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: triggerModeOptions,
      },
      width: 150,
      showDrag: true,
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
      dataIndex: 'startTime',
      slotName: 'startTime',
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
      title: hasAnyPermission(['PROJECT_API_REPORT:READ+DELETE']) ? 'common.operation' : '',
      width: hasAnyPermission(['PROJECT_API_REPORT:READ+DELETE']) ? 130 : 50,
    },
  ];

  await tableStore.initColumn(TableKeyEnum.API_TEST_REPORT, columns, 'drawer');

  const rename = async (record: any) => {
    try {
      await reportRename(props.moduleType, record.id, record.name);
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
        tableKey: TableKeyEnum.API_TEST_REPORT,
        scroll: {
          x: '100%',
        },
        showSetting: true,
        selectable: hasAnyPermission(['PROJECT_API_REPORT:READ+DELETE']),
        heightUsed: 256,
        paginationSize: 'mini',
        showSelectorAll: true,
      },
      (item) => ({
        ...item,
        startTime: dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss'),
      }),
      rename
    );

  const typeFilter = computed(() => {
    if (showType.value === 'All') {
      return [];
    }
    return showType.value === 'INDEPENDENT' ? [false] : [true];
  });

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
      moduleType: props.moduleType,
      filter: {
        integrated: typeFilter.value,
        ...filterParams,
      },
    });
    loadList();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.delete',
        eventTag: 'batchStop',
        permission: ['PROJECT_API_REPORT:READ+DELETE'],
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
        filter: {
          ...propsRes.value.filter,
          integrated: typeFilter.value,
        },
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
          await reportBathDelete(props.moduleType, batchParams.value);
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
          await reportDelete(props.moduleType, id);
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

  onBeforeMount(() => {
    initData();
  });

  function changeShowType(val: string | number | boolean) {
    showType.value = val as ReportShowType;
    resetFilterParams();
    resetSelector();
    // 重置分页
    setPagination({
      current: 1,
    });
    initData();
  }

  function filterChange(dataIndex: string, value: string[] | (string | number | boolean)[] | undefined) {
    initData(dataIndex, value);
  }

  /**
   * 报告详情 showReportDetail
   */
  const activeDetailId = ref<string>('');
  const activeReportIndex = ref<number>(0);
  const showDetailDrawer = ref<boolean>(false);
  const showCaseDetailDrawer = ref<boolean>(false);

  function showReportDetail(id: string, rowIndex: number) {
    activeDetailId.value = id;
    activeReportIndex.value = rowIndex;
    if (props.moduleType === ReportEnum.API_SCENARIO_REPORT) {
      showDetailDrawer.value = true;
    } else {
      showCaseDetailDrawer.value = true;
    }
  }

  const shareTime = ref<string>('');
  async function getTime() {
    try {
      const res = await getShareTime(appStore.currentProjectId);
      const match = res.match(/^(\d+)([MYHD])$/);
      if (match) {
        const value = parseInt(match[1], 10);
        const type = match[2];
        const translations: Record<string, string> = {
          M: t('msTimeSelector.month'),
          Y: t('msTimeSelector.year'),
          H: t('msTimeSelector.hour'),
          D: t('msTimeSelector.day'),
        };
        shareTime.value = value + (translations[type] || translations.D);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function showDetail() {
    if ((route.query.reportId || route.query.id) && route.query.type) {
      activeDetailId.value = (route.query.reportId as string) || (route.query.id as string);
      activeReportIndex.value = 0;
      if (route.query.type === 'API_SCENARIO') {
        showDetailDrawer.value = true;
      } else {
        showCaseDetailDrawer.value = true;
      }
    }
  }

  onMounted(() => {
    showDetail();
    getTime();
  });

  onBeforeUnmount(() => {
    if (route.query.type === 'API_SCENARIO') {
      showDetailDrawer.value = false;
    } else {
      showCaseDetailDrawer.value = false;
    }
  });

  watch(
    () => props.moduleType,
    (val) => {
      if (val) {
        resetSelector();
        resetFilterParams();
        initData();
      }
    }
  );
</script>

<style lang="less" scoped>
  .ms-table--special-small();
</style>
