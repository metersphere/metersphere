<template>
  <div class="p-[16px_22px]">
    <div class="mb-[16px] flex items-center justify-end">
      <div class="flex items-center gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiTestManagement.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadCaseList"
          @press-enter="loadCaseList"
          @clear="loadCaseList"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="loadCaseList">
          <template #icon>
            <icon-refresh class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
      </div>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="batchActions"
      :first-column-width="44"
      no-disable
      filter-icon-align-left
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
    >
      <template #caseLevel="{ record }">
        <span class="text-[var(--color-text-2)]"> <caseLevel :case-level="record.priority" /></span>
      </template>
      <!-- 用例等级 -->
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
        <caseLevel :case-level="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_STATUS]="{ filterContent }">
        <apiStatus :status="filterContent.value" />
      </template>
      <template #protocol="{ record }">
        <apiMethodName :method="record.protocol" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #deleteTime="{ record }">
        {{ dayjs(record.deleteTime).format('YYYY-MM-DD HH:mm:ss') || '-' }}
      </template>
      <template #status="{ record }">
        <apiStatus :status="record.status" />
      </template>

      <template #lastReportStatus="{ record }">
        <ExecutionStatus
          :module-type="ReportEnum.API_REPORT"
          :status="record.lastReportStatus"
          :class="[!record.lastReportId ? '' : 'cursor-pointer']"
        />
      </template>
      <template #passRateColumn>
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t('case.passRate') }}
          <a-tooltip :content="t('case.passRateTip')" position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
      </template>
      <template #action="{ record }">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']"
          type="text"
          class="!mr-0"
          @click="recover(record)"
        >
          {{ t('apiTestManagement.recycle.batchRecover') }}
        </MsButton>
        <a-divider
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']"
          direction="vertical"
          :margin="8"
        ></a-divider>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']"
          type="text"
          class="!mr-0"
          @click="cleanOut(record)"
        >
          {{ t('apiTestManagement.recycle.batchCleanOut') }}
        </MsButton>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import {
    batchDeleteRecycleCase,
    batchRecoverCase,
    deleteRecycleCase,
    getRecycleCasePage,
    recoverCase,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import { ApiCaseBatchParams, ApiCaseDetail } from '@/models/apiTest/management';
  import { RequestCaseStatus } from '@/enums/apiEnum';
  import { ReportEnum } from '@/enums/reportEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions, lastReportStatusListOptions } from '@/views/api-test/components/config';

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    selectedProtocols: string[];
    memberOptions: { label: string; value: string }[];
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const tableStore = useTableStore();
  const { openModal } = useModal();

  const keyword = ref('');

  const requestCaseStatusOptions = computed(() => {
    return Object.values(RequestCaseStatus).map((key) => {
      return {
        value: key,
        label: key,
      };
    });
  });

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 110,
      columnSelectorDisabled: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.protocol',
      dataIndex: 'protocol',
      slotName: 'protocol',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      titleSlotName: 'caseLevelFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: casePriorityOptions,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 150,
      showDrag: true,
      filterConfig: {
        options: requestCaseStatusOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_STATUS,
      },
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'case.lastReportStatus',
      dataIndex: 'lastReportStatus',
      slotName: 'lastReportStatus',
      titleSlotName: 'lastReportStatusFilter',
      showInTable: false,
      width: 150,
      showDrag: true,
      filterConfig: {
        options: lastReportStatusListOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS,
      },
    },
    {
      title: 'case.passRate',
      dataIndex: 'passRate',
      titleSlotName: 'passRateColumn',
      showInTable: false,
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'case.caseEnvironment',
      dataIndex: 'environmentName',
      showTooltip: true,
      showInTable: false,
      width: 150,
      showDrag: true,
    },
    {
      title: 'case.tableColumnUpdateUser',
      dataIndex: 'updateName',
      titleSlotName: 'updateUserFilter',
      showInTable: true,
      showTooltip: true,
      width: 180,
      showDrag: true,
    },
    {
      title: 'case.tableColumnUpdateTime',
      dataIndex: 'updateTime',
      showInTable: true,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      showDrag: true,
    },
    {
      title: 'case.tableColumnCreateUser',
      dataIndex: 'createName',
      titleSlotName: 'createUserFilter',
      showTooltip: true,
      width: 180,
      showDrag: true,
    },
    {
      title: 'case.tableColumnCreateTime',
      dataIndex: 'createTime',
      showTooltip: true,
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.deleteUser',
      dataIndex: 'deleteName',
      titleSlotName: 'deleteUserFilter',
      showInTable: true,
      showTooltip: true,
      width: 180,
      showDrag: true,
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
      },
    },
    {
      title: 'apiTestManagement.deleteTime',
      slotName: 'deleteTime',
      dataIndex: 'deleteTime',
      showInTable: true,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 150,
    },
  ];
  await tableStore.initColumn(TableKeyEnum.API_TEST_MANAGEMENT_CASE, columns, 'drawer');
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getRecycleCasePage, {
    columns,
    scroll: { x: '100%' },
    tableKey: TableKeyEnum.API_TEST_MANAGEMENT_CASE,
    showSetting: true,
    selectable: true,
    showSelectAll: true,
    draggable: { type: 'handle', width: 32 },
    showSubdirectory: true,
  });
  const batchActions = {
    baseAction: [
      {
        label: 'apiTestManagement.recycle.batchRecover',
        eventTag: 'batchRecover',
        permission: ['PROJECT_API_DEFINITION_CASE:READ+DELETE'],
      },
      {
        label: 'apiTestManagement.recycle.batchCleanOut',
        eventTag: 'batchCleanOut',
        danger: true,
        permission: ['PROJECT_API_DEFINITION_CASE:READ+DELETE'],
      },
    ],
  };

  const moduleIds = computed(() => {
    return props.activeModule === 'all' ? [] : [props.activeModule];
  });

  async function loadCaseList() {
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds: moduleIds.value,
      protocols: props.selectedProtocols,
    };
    setLoadListParams(params);
    loadList();
  }
  function loadCaseListAndResetSelector() {
    resetSelector();
    loadCaseList();
  }

  watch([() => props.activeModule, () => props.selectedProtocols], () => {
    loadCaseListAndResetSelector();
  });

  const tableSelected = ref<(string | number)[]>([]); // 表格选中的
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  // 列表彻底删除
  async function cleanOut(record: ApiCaseDetail) {
    openModal({
      type: 'error',
      title: t('case.batchDeleteCaseTipTitle', { name: record?.name }),
      content: t('case.recycle.cleanOutDeleteOnRecycleTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await deleteRecycleCase(record.id);
          Message.success(t('common.deleteSuccess'));
          loadCaseListAndResetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  async function recover(record: ApiCaseDetail) {
    openModal({
      type: 'info',
      title: t('case.batchRecoverCaseTipTitle', { name: record?.name }),
      content: t('case.recycle.recoverCaseTip'),
      okText: t('case.recycle.confirmRecovery'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {
        try {
          await recoverCase(record.id);
          Message.success(t('apiTestManagement.recycle.recoveredSuccessfully'));
          loadCaseListAndResetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 批量操作参数
  function getBatchParams(): ApiCaseBatchParams {
    return {
      excludeIds: batchParams.value.excludeIds,
      selectAll: batchParams.value.selectAll,
      selectIds: batchParams.value.selectedIds as string[],
      moduleIds: moduleIds.value,
      projectId: appStore.currentProjectId,
      protocols: props.selectedProtocols,
      condition: {
        keyword: keyword.value,
        filter: propsRes.value.filter,
        combine: batchParams.value.condition,
      },
    };
  }

  // 批量恢复
  async function batchRecover() {
    openModal({
      type: 'info',
      title: t('case.batchRecoverCaseTip', {
        count: batchParams.value.currentSelectCount || tableSelected.value.length,
      }),
      content: t('case.recycle.recoverCaseTip'),
      okText: t('case.recycle.confirmRecovery'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {
        try {
          await batchRecoverCase(getBatchParams());
          Message.success(t('apiTestManagement.recycle.recoveredSuccessfully'));
          loadCaseListAndResetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 批量彻底删除
  async function batchCleanOut() {
    const title = t('case.batchDeleteCaseTip', {
      count: batchParams.value.currentSelectCount || tableSelected.value.length,
    });
    openModal({
      type: 'error',
      title,
      content: t('case.recycle.cleanOutDeleteOnRecycleTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await batchDeleteRecycleCase(getBatchParams());
          Message.success(t('common.deleteSuccess'));
          loadCaseListAndResetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  // 处理表格选中后批量操作
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'batchRecover':
        batchRecover();
        break;
      case 'batchCleanOut':
        batchCleanOut();
        break;
      default:
        break;
    }
  }
</script>

<style lang="less" scoped>
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-brand);
      }
    }
  }
</style>
