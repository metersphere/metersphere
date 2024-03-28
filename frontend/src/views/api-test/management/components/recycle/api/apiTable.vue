<template>
  <div :class="['p-[16px_22px]', props.class]">
    <div class="mb-[16px] flex items-center justify-end">
      <div class="flex items-center gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiTestManagement.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadApiList"
          @press-enter="loadApiList"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="loadApiList">
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
      <template #methodFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="methodFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton type="text" class="arco-btn-text--secondary ml-[10px]" @click="methodFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="methodFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="methodFilters" direction="vertical" size="small">
                  <a-checkbox v-for="key of RequestMethods" :key="key" :value="key">
                    <apiMethodName :method="key" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
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
      <template #deleteUserName="{ record }">
        <span type="text" class="px-0">{{ record.updateUserName || '-' }}</span>
      </template>
      <template #method="{ record }">
        <apiMethodName :method="record.method" is-tag />
      </template>
      <template #status="{ record }">
        <apiStatus :status="record.status" />
      </template>
      <template #action="{ record }">
        <MsButton type="text" class="!mr-0" @click="recover(record)">
          {{ t('apiTestManagement.recycle.batchRecover') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsButton type="text" class="!mr-0" @click="cleanOut(record)">
          {{ t('apiTestManagement.recycle.batchCleanOut') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
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
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

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
  import { characterLimit } from '@/utils';

  import { ApiDefinitionDetail, BatchRecoverApiParams } from '@/models/apiTest/management';
  import { RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    class?: string;
    activeModule: string;
    offspringIds: string[];
    protocol: string; // 查看的协议类型
    readOnly?: boolean; // 是否是只读模式
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const folderTreePathMap = inject('folderTreePathMap');
  const keyword = ref('');

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
      title: 'apiTestManagement.apiName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
    },
    {
      title: 'apiTestManagement.apiType',
      dataIndex: 'method',
      slotName: 'method',
      titleSlotName: 'methodFilter',
      width: 140,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      width: 130,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      showTooltip: true,
      width: 200,
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
      dataIndex: 'deleteUser',
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
        // permission: ['FUNCTIONAL_CASE:READ+DELETE'],
      },
      {
        label: 'apiTestManagement.recycle.batchCleanOut',
        eventTag: 'batchCleanOut',
      },
    ],
  };

  const methodFilterVisible = ref(false);
  const methodFilters = ref(Object.keys(RequestMethods));
  const statusFilterVisible = ref(false);
  const statusFilters = ref(Object.keys(RequestDefinitionStatus));
  const moduleIds = computed(() => {
    if (props.activeModule === 'all') {
      return [];
    }
    return [props.activeModule];
  });
  const tableQueryParams = ref<any>();

  function loadApiList() {
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds: moduleIds.value,
      deleted: true,
      protocol: props.protocol,
      filter: { status: statusFilters.value, method: methodFilters.value },
    };
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
      loadApiList();
    }
  );

  watch(
    () => props.protocol,
    () => {
      resetSelector();
      loadApiList();
    }
  );

  function handleFilterHidden(val: boolean) {
    if (!val) {
      loadApiList();
    }
  }

  onBeforeMount(() => {
    loadApiList();
  });

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
  function getBatchParams(): BatchRecoverApiParams {
    return {
      excludeIds: batchParams.value.excludeIds,
      selectAll: batchParams.value.selectAll,
      selectIds: batchParams.value.selectedIds as string[],
      moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
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
    try {
      await batchRecoverDefinition(getBatchParams());
      Message.success(t('apiTestManagement.recycle.recoveredSuccessfully'));
      resetSelector();
      loadApiList();
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
          await batchCleanOutDefinition(getBatchParams());
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadApiList();
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
          loadApiList();
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
        protocol: props.protocol,
      });
      Message.success(t('apiTestManagement.recycle.recoveredSuccessfully'));
      resetSelector();
      loadApiList();
    } catch (error) {
      console.log(error);
    }
  }

  defineExpose({
    loadApiList,
  });

  const tableStore = useTableStore();
  await tableStore.initColumn(TableKeyEnum.API_TEST, columns, 'drawer', true);
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
