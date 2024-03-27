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
      <template #caseLevelFilter="{ columnConfig }">
        <a-trigger v-model:popup-visible="caseFilterVisible" trigger="click" @popup-visible-change="handleFilterHidden">
          <MsButton type="text" class="arco-btn-text--secondary ml-[10px]" @click="caseFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="caseFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="caseFilters" direction="vertical" size="small">
                  <a-checkbox v-for="item of caseLevelList" :key="item.text" :value="item.text">
                    <caseLevel :case-level="item.text" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #deleteTime="{ record }">
        {{ dayjs(record.deleteTime).format('YYYY-MM-DD HH:mm:ss') || '-' }}
      </template>
      <template #status="{ record }">
        <apiStatus :status="record.status" />
      </template>
      <template #statusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="statusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton type="text" class="arco-btn-text--secondary ml-[10px]" @click="statusFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <a-checkbox v-for="val of Object.values(RequestDefinitionStatus)" :key="val" :value="val">
                    <apiStatus :status="val" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #lastReportStatusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="lastReportStatusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton
            type="text"
            class="arco-btn-text--secondary ml-[10px]"
            @click="lastReportStatusFilterVisible = true"
          >
            {{ t(columnConfig.title as string) }}
            <icon-down :class="lastReportStatusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="lastReportStatusFilters" direction="vertical" size="small">
                  <a-checkbox v-for="val of lastReportStatusList" :key="val" :value="val">
                    <span>{{ val }}</span>
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
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
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import {
    batchDeleteRecycleCase,
    batchRecoverCase,
    deleteRecycleCase,
    getRecycleCasePage,
    recoverCase,
  } from '@/api/modules/api-test/management';
  import { getCaseDefaultFields } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import { ApiCaseBatchParams, ApiCaseDetail } from '@/models/apiTest/management';
  import { RequestDefinitionStatus } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    protocol: string; // 查看的协议类型
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const tableStore = useTableStore();
  const { openModal } = useModal();

  const keyword = ref('');
  const refreshModuleTree: (() => Promise<any>) | undefined = inject('refreshModuleTree');

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
      fixed: 'left',
      width: 100,
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
      titleSlotName: 'lastReportStatusFilter',
      showInTable: false,
      showTooltip: true,
      width: 150,
      showDrag: true,
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
      dataIndex: 'updateUser',
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
      showInTable: true,
      showTooltip: true,
      width: 180,
      showDrag: true,
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

  const statusFilterVisible = ref(false);
  const statusFilters = ref(Object.keys(RequestDefinitionStatus));
  const caseLevelFields = ref<Record<string, any>>({});
  const caseFilterVisible = ref(false);
  const caseFilters = ref<string[]>([]);
  const caseLevelList = computed(() => {
    return caseLevelFields.value?.options || [];
  });
  const lastReportStatusFilterVisible = ref(false);
  const lastReportStatusList = ['error', 'FakeError', 'success'];
  const lastReportStatusFilters = ref<string[]>([...lastReportStatusList]);

  const moduleIds = computed(() => {
    return props.activeModule === 'all' ? [] : [props.activeModule];
  });
  function loadCaseList() {
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds: moduleIds.value,
      protocol: props.protocol,
      filter: {
        status: statusFilters.value,
        priority: caseFilters.value,
        lastReportStatus: lastReportStatusFilters.value,
      },
    };
    setLoadListParams(params);
    loadList();
  }
  function loadCaseListAndResetSelector() {
    resetSelector();
    loadCaseList();
  }

  // 获取用例等级数据
  async function getCaseLevelFields() {
    const result = await getCaseDefaultFields(appStore.currentProjectId);
    caseLevelFields.value = result.customFields.find((item: any) => item.internal && item.fieldName === '用例等级');
    caseFilters.value = caseLevelFields.value?.options.map((item: any) => item.text);
  }

  onBeforeMount(() => {
    getCaseLevelFields();
    loadCaseList();
  });

  function handleFilterHidden(val: boolean) {
    if (!val) {
      loadCaseList();
    }
  }

  watch(
    () => props.activeModule,
    () => {
      loadCaseListAndResetSelector();
    }
  );

  watch(
    () => props.protocol,
    () => {
      loadCaseListAndResetSelector();
    }
  );

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
      protocol: props.protocol,
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
