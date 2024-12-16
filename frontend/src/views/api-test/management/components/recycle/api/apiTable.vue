<template>
  <div :class="['p-[16px_22px]', props.class]">
    <div class="mb-[16px] flex items-center justify-end">
      <div class="flex items-center gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiTestManagement.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadApiList(false)"
          @press-enter="loadApiList(false)"
          @clear="loadApiList(false)"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="loadApiList(false)">
          <template #icon>
            <icon-refresh class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
      </div>
    </div>
    <ms-base-table
      ref="apiTableRef"
      v-bind="propsRes"
      :action-config="batchActions"
      :first-column-width="44"
      no-disable
      filter-icon-align-left
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
    >
      <template #[FilterSlotNameEnum.API_TEST_API_REQUEST_METHODS]="{ filterContent }">
        <apiMethodName :method="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_API_REQUEST_API_STATUS]="{ filterContent }">
        <apiStatus :status="filterContent.value" />
      </template>
      <template #deleteUserFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="deleteUserFilterVisible"
          v-model:status-filters="deleteUserFilters"
          :title="(columnConfig.title as string)"
          :list="memberOptions"
          @search="loadApiList"
        >
          <template #item="{ item }">
            {{ item.label }}
          </template>
        </TableFilter>
      </template>
      <template #deleteUserName="{ record }">
        <span type="text" class="px-0">{{ record.deleteUserName || '-' }}</span>
      </template>
      <template #protocol="{ record }">
        <apiMethodName :method="record.protocol" />
      </template>
      <template #method="{ record }">
        <apiMethodName :method="record.method" is-tag />
      </template>
      <template #status="{ record }">
        <apiStatus :status="record.status" />
      </template>
      <template #action="{ record }">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION:READ+DELETE']"
          type="text"
          class="!mr-0"
          @click="recover(record)"
        >
          {{ t('apiTestManagement.recycle.batchRecover') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_DEFINITION:READ+DELETE']" direction="vertical" :margin="8"></a-divider>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION:READ+DELETE']"
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
  import { FormInstance, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import {
    batchCleanOutDefinition,
    batchRecoverDefinition,
    deleteRecycleApiList,
    getDefinitionPage,
    recoverDefinition,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { characterLimit, operationWidth } from '@/utils';

  import { ProtocolItem } from '@/models/apiTest/common';
  import {
    ApiDefinitionDetail,
    ApiDefinitionGetModuleParams,
    BatchRecoverApiParams,
  } from '@/models/apiTest/management';
  import { RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const props = defineProps<{
    class?: string;
    activeModule: string;
    offspringIds: string[];
    selectedProtocols: string[];
    readOnly?: boolean; // 是否是只读模式
    memberOptions: { label: string; value: string }[];
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const folderTreePathMap = inject<MsTreeNodeData[]>('folderTreePathMap');
  const keyword = ref('');
  const refreshModuleTree: (() => Promise<any>) | undefined = inject('refreshModuleTree');
  const refreshModuleTreeCount: ((data: ApiDefinitionGetModuleParams) => Promise<any>) | undefined =
    inject('refreshModuleTreeCount');

  // TODO: 后期优化 放store里
  const protocolList = ref<ProtocolItem[]>([]);
  async function initProtocolList() {
    try {
      const res = await getProtocolList(appStore.currentOrgId);
      protocolList.value = res.map((e) => ({
        protocol: e.protocol,
        polymorphicName: e.polymorphicName,
        pluginId: e.pluginId,
      }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  onBeforeMount(() => {
    initProtocolList();
  });

  // TODO 后期提到apiTest公用里
  const requestMethodsOptions = computed(() => {
    const otherMethods = protocolList.value
      .filter((e) => e.protocol !== 'HTTP')
      .map((item) => {
        return {
          value: item.protocol,
          key: item.protocol,
        };
      });
    const httpMethods = Object.values(RequestMethods).map((e) => {
      return {
        value: e,
        key: e,
      };
    });
    return [...httpMethods, ...otherMethods];
  });
  const requestApiStatus = computed(() => {
    return Object.values(RequestDefinitionStatus).map((e) => {
      return {
        value: e,
        key: e,
      };
    });
  });
  let columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      columnSelectorDisabled: true,
      width: 100,
    },
    {
      title: 'apiTestManagement.apiName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
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
      title: 'apiTestManagement.apiType',
      dataIndex: 'method',
      slotName: 'method',
      titleSlotName: 'methodFilter',
      width: 140,
      showDrag: true,
      filterConfig: {
        options: requestMethodsOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_API_REQUEST_METHODS,
      },
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      width: 130,
      showDrag: true,
      filterConfig: {
        options: requestApiStatus.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_API_REQUEST_API_STATUS,
      },
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
      title: 'apiTestManagement.deleteTime',
      dataIndex: 'deleteTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.deleteUser',
      slotName: 'deleteUserName',
      titleSlotName: 'deleteUserFilter',
      showTooltip: true,
      dataIndex: 'deleteUser',
      width: 180,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: operationWidth(230, 150),
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getDefinitionPage,
    {
      columns: props.readOnly ? columns : [],
      scroll: { x: '100%' },
      tableKey: props.readOnly ? undefined : TableKeyEnum.API_TEST,
      showSetting: !props.readOnly,
      selectable: true,
      showSelectAll: !props.readOnly,
      draggable: props.readOnly ? undefined : { type: 'handle', width: 32 },
      heightUsed: 374,
      showSubdirectory: true,
    },
    (item) => ({
      ...item,
      fullPath: folderTreePathMap?.[item.moduleId],
      deleteTime: dayjs(item.deleteTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );
  const batchActions = {
    baseAction: [
      {
        label: 'apiTestManagement.recycle.batchRecover',
        eventTag: 'batchRecover',
        permission: ['PROJECT_API_DEFINITION:READ+DELETE'],
      },
      {
        label: 'apiTestManagement.recycle.batchCleanOut',
        eventTag: 'batchCleanOut',
        permission: ['PROJECT_API_DEFINITION:READ+DELETE'],
      },
    ],
  };

  const methodFilterVisible = ref(false);
  const methodFilters = ref<string[]>([]);
  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>([]);
  const deleteUserFilterVisible = ref(false);
  const deleteUserFilters = ref<string[]>([]);

  const tableQueryParams = ref<any>();
  const tableStore = useTableStore();
  async function getModuleIds() {
    let queryModuleIds: string[] = [];
    if (props.activeModule !== 'all') {
      queryModuleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.API_TEST);
      if (getAllChildren) {
        queryModuleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return queryModuleIds;
  }

  // hasRefreshTree:调用该方法前后有没有重新加载tree。 重新加载tree时会自动计算count。没有重新加载的话会手动处理
  async function loadApiList(hasRefreshTree?: boolean) {
    const queryModuleIds = await getModuleIds();
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds: props.activeModule === 'all' ? [] : queryModuleIds,
      deleted: true,
      protocols: props.selectedProtocols,
      filter: {
        status: statusFilters.value,
        method: methodFilters.value,
        deleteUser: deleteUserFilters.value,
      },
    };
    if (!hasRefreshTree && typeof refreshModuleTreeCount === 'function') {
      refreshModuleTreeCount({
        keyword: keyword.value,
        filter: {
          status: statusFilters.value,
          method: methodFilters.value,
          deleteUser: deleteUserFilters.value,
        },
        moduleIds: [],
        protocols: props.selectedProtocols,
        projectId: appStore.currentProjectId,
      });
    }

    setLoadListParams(params);
    loadList();
    tableQueryParams.value = {
      ...params,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    };
  }

  watch(
    () => props.activeModule,
    () => {
      resetSelector();
      loadApiList(false);
    }
  );

  // 第一次初始化会触发，所以不需要onBeforeMount再次调用
  watch(
    () => props.selectedProtocols,
    () => {
      resetSelector();
      loadApiList(true);
      if (typeof refreshModuleTree === 'function') {
        refreshModuleTree();
      }
    }
  );

  function handleFilterHidden(val: boolean) {
    if (!val) {
      loadApiList(false);
      methodFilterVisible.value = false;
      statusFilterVisible.value = false;
    }
  }

  function resetMethodFilter() {
    methodFilters.value = [];
    methodFilterVisible.value = false;
    loadApiList(false);
  }

  function resetStatusFilter() {
    statusFilters.value = [];
    statusFilterVisible.value = false;
    loadApiList(false);
  }

  const tableSelected = ref<(string | number)[]>([]);
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  const showBatchModal = ref(false);
  const batchFormRef = ref<FormInstance>();
  const batchForm = ref({
    attr: '',
    value: '',
    values: [],
  });

  function cancelBatch() {
    showBatchModal.value = false;
    batchFormRef.value?.resetFields();
    batchForm.value = {
      attr: '',
      value: '',
      values: [],
    };
  }

  // 批量操作参数
  async function getBatchParams(): Promise<BatchRecoverApiParams> {
    const queryModuleIds = await getModuleIds();
    return {
      excludeIds: batchParams.value.excludeIds,
      selectAll: batchParams.value.selectAll,
      selectIds: batchParams.value.selectedIds as string[],
      moduleIds: props.activeModule === 'all' ? [] : queryModuleIds,
      projectId: appStore.currentProjectId,
      protocols: props.selectedProtocols,
      condition: {
        keyword: keyword.value,
        filter: {
          status: statusFilters.value,
          method: methodFilters.value,
          deleteUser: deleteUserFilters.value,
        },
      },
    };
  }

  // 批量恢复
  async function batchRecover() {
    try {
      await batchRecoverDefinition(await getBatchParams());
      Message.success(t('apiTestManagement.recycle.recoveredSuccessfully'));
      resetSelector();
      loadApiList(true);
      if (typeof refreshModuleTree === 'function') {
        refreshModuleTree();
      }
    } catch (error) {
      console.log(error);
    }
  }

  // 批量彻底删除
  async function batchCleanOut() {
    const title = t('apiTestManagement.recycle.batchDeleteApiTip', {
      count: batchParams.value.currentSelectCount || tableSelected.value.length,
    });
    openModal({
      type: 'error',
      title,
      content: t('apiTestManagement.recycle.cleanOutDeleteOnRecycleTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await batchCleanOutDefinition(await getBatchParams());
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadApiList(true);
          if (typeof refreshModuleTree === 'function') {
            refreshModuleTree();
          }
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = { ...params };

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

  // 列表彻底删除
  async function cleanOut(record: ApiDefinitionDetail) {
    openModal({
      type: 'error',
      title: t('apiTestManagement.recycle.completedDeleteCaseTitle', { name: characterLimit(record.name) }),
      content: t('apiTestManagement.recycle.cleanOutDeleteOnRecycleTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await deleteRecycleApiList(record.id);
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadApiList(true);
          if (typeof refreshModuleTree === 'function') {
            refreshModuleTree();
          }
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 列表恢复
  async function recover(record: ApiDefinitionDetail) {
    try {
      await recoverDefinition({
        id: record.id,
        projectId: record.projectId,
      });
      Message.success(t('apiTestManagement.recycle.recoveredSuccessfully'));
      resetSelector();
      loadApiList(true);
      if (typeof refreshModuleTree === 'function') {
        refreshModuleTree();
      }
    } catch (error) {
      console.log(error);
    }
  }

  defineExpose({
    loadApiList,
  });

  function initFilterColumn() {
    columns = columns.map((item) => {
      if (item.dataIndex === 'method') {
        return {
          ...item,
          filterConfig: {
            ...item.filterConfig,
            options: requestMethodsOptions.value,
          },
        };
      }
      return item;
    });
  }

  await initFilterColumn();
  await tableStore.initColumn(TableKeyEnum.API_TEST, columns, 'drawer', true);
  const apiTableRef = ref();
  watch(
    () => requestMethodsOptions.value,
    () => {
      initFilterColumn();
      apiTableRef.value.initColumn(columns);
    }
  );
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
