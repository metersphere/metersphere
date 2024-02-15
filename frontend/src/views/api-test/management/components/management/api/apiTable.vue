<template>
  <div :class="['p-[16px_22px]', props.class]">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="flex items-center gap-[8px]">
        <a-switch v-model:model-value="showSubdirectory" size="small" type="line"></a-switch>
        {{ t('apiTestManagement.showSubdirectory') }}
      </div>
      <div class="flex items-center gap-[8px]">
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]">
          <template #icon>
            <icon-location class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
        <MsSelect
          v-model:model-value="checkedEnv"
          mode="static"
          :options="envOptions"
          class="!w-[150px]"
          :search-keys="['label']"
          allow-search
        />
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiTestManagement.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadApiList"
          @press-enter="loadApiList"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]">
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
      <template #typeFilter="{ columnConfig }">
        <a-trigger v-model:popup-visible="typeFilterVisible" trigger="click" @popup-visible-change="handleFilterHidden">
          <a-button type="text" class="arco-btn-text--secondary" @click="typeFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="typeFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="typeFilters" direction="vertical" size="small">
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
          <a-button type="text" class="arco-btn-text--secondary" @click="statusFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <a-checkbox v-for="key of RequestDefinitionStatus" :key="key" :value="key">
                    <apiStatus :status="key" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #type="{ record }">
        <apiMethodName :method="record.type" is-tag />
      </template>
      <template #status="{ record }">
        <a-select
          v-model:model-value="record.status"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full"
          @change="() => handleStatusChange(record)"
        >
          <template #label>
            <apiStatus :status="record.status" />
          </template>
          <a-option v-for="item of RequestDefinitionStatus" :key="item" :value="item">
            <apiStatus :status="item" />
          </a-option>
        </a-select>
      </template>
      <template #action="{ record }">
        <MsButton type="text" class="!mr-0">
          {{ t('apiTestManagement.execute') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsButton type="text" class="!mr-0">
          {{ t('common.copy') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction :list="tableMoreActionList" @select="handleTableMoreActionSelect($event, record)" />
      </template>
    </ms-base-table>
  </div>
  <a-modal v-model:visible="showBatchModal" title-align="start" class="ms-modal-upload ms-modal-medium" :width="480">
    <template #title>
      {{ t('common.edit') }}
      <div class="text-[var(--color-text-4)]">
        {{
          t('apiTestManagement.batchModalSubTitle', {
            count: batchParams.currentSelectCount || tableSelected.length,
          })
        }}
      </div>
    </template>
    <a-form ref="batchFormRef" class="rounded-[4px]" :model="batchForm" layout="vertical">
      <a-form-item
        field="attr"
        :label="t('apiTestManagement.chooseAttr')"
        :rules="[{ required: true, message: t('apiTestManagement.attrRequired') }]"
        asterisk-position="end"
      >
        <a-select v-model="batchForm.attr" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of attrOptions" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr === 'tag'"
        field="values"
        :label="t('apiTestManagement.batchUpdate')"
        :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <MsTagsInput
          v-model:model-value="batchForm.values"
          placeholder="common.tagsInputPlaceholder"
          allow-clear
          unique-value
          retain-input-value
        />
      </a-form-item>
      <a-form-item
        v-else
        field="value"
        :label="t('apiTestManagement.batchUpdate')"
        :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <apiMethodSelect
          v-if="batchForm.attr === 'type'"
          v-model:model-value="batchForm.value"
          @change="handleActiveDebugChange"
        />
        <a-select
          v-else
          v-model="batchForm.value"
          :placeholder="t('common.pleaseSelect')"
          :disabled="batchForm.attr === ''"
        >
          <a-option v-for="item of valueOptions" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="secondary" :disabled="batchUpdateLoading" @click="cancelBatch">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="batchUpdateLoading" @click="batchUpdate">
        {{ t('common.update') }}
      </a-button>
    </template>
  </a-modal>
  <a-modal
    v-model:visible="moveModalVisible"
    title-align="start"
    class="ms-modal-no-padding ms-modal-small"
    :mask-closable="false"
    :ok-text="t('apiTestManagement.batchMoveConfirm')"
    :ok-button-props="{ disabled: selectedModuleKeys.length === 0 }"
    :cancel-button-props="{ disabled: batchMoveApiLoading }"
    :on-before-ok="handleApiMove"
    @close="handleMoveApiModalCancel"
  >
    <template #title>
      <div class="flex items-center">
        {{ isBatchMove ? t('common.batchMove') : t('common.move') }}
        <div
          class="one-line-text ml-[4px] max-w-[70%] text-[var(--color-text-4)]"
          :title="
            isBatchMove
              ? t('apiTestManagement.batchModalSubTitle', { count: tableSelected.length })
              : `(${activeApi?.name})`
          "
        >
          {{
            isBatchMove
              ? t('apiTestManagement.batchModalSubTitle', { count: tableSelected.length })
              : `(${activeApi?.name})`
          }}
        </div>
      </div>
    </template>
    <moduleTree
      v-if="moveModalVisible"
      :is-expand-all="true"
      is-modal
      :active-module="props.activeModule"
      @folder-node-select="folderNodeSelect"
    />
  </a-modal>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import moduleTree from '@/views/api-test/management/components/moduleTree.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import { RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    class?: string;
    activeModule: string;
    offspringIds: string[];
    readOnly?: boolean; // 是否是只读模式
  }>();
  const emit = defineEmits<{
    (e: 'init', params: any): void;
    (e: 'change'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  function handleActiveDebugChange() {
    emit('change');
  }

  const showSubdirectory = ref(false);
  const checkedEnv = ref('DEV');
  const envOptions = ref([
    {
      label: 'DEV',
      value: 'DEV',
    },
    {
      label: 'TEST',
      value: 'TEST',
    },
    {
      label: 'PRE',
      value: 'PRE',
    },
    {
      label: 'PROD',
      value: 'PROD',
    },
  ]);
  const keyword = ref('');

  let columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
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
      dataIndex: 'type',
      slotName: 'type',
      titleSlotName: 'typeFilter',
      width: 120,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      width: 130,
    },
    {
      title: 'apiTestManagement.responsiblePerson',
      titleSlotName: 'responsiblePersonFilter',
      showTooltip: true,
      width: 100,
    },
    {
      title: 'apiTestManagement.path',
      slotName: 'path',
      dataIndex: 'path',
      width: 100,
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      isTag: true,
      width: 150,
    },
    {
      title: 'apiTestManagement.version',
      dataIndex: 'version',
      width: 100,
    },
    {
      title: 'apiTestManagement.createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
    },
    {
      title: 'apiTestManagement.updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 150,
    },
  ];
  if (!props.readOnly) {
    const tableStore = useTableStore();
    await tableStore.initColumn(TableKeyEnum.API_TEST, columns, 'drawer');
  } else {
    columns = columns.filter(
      (item) => !['version', 'createTime', 'updateTime', 'operation'].includes(item.dataIndex as string)
    );
  }
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    () =>
      Promise.resolve({
        list: [
          {
            num: 1001,
            name: 'asdasdasd',
            type: RequestMethods.CONNECT,
            status: RequestDefinitionStatus.DEBUGGING,
          },
          {
            num: 10011,
            name: '1123',
            type: RequestMethods.OPTIONS,
            status: RequestDefinitionStatus.DEPRECATED,
          },
          {
            num: 10012,
            name: 'vfd',
            type: RequestMethods.POST,
            status: RequestDefinitionStatus.DONE,
          },
          {
            num: 10013,
            name: 'ccf',
            type: RequestMethods.DELETE,
            status: RequestDefinitionStatus.PROCESSING,
          },
        ],
        total: 0,
      }),
    {
      columns: props.readOnly ? columns : [],
      scroll: { x: '100%' },
      tableKey: props.readOnly ? undefined : TableKeyEnum.API_TEST,
      showSetting: !props.readOnly,
      selectable: true,
      showSelectAll: !props.readOnly,
      draggable: props.readOnly ? undefined : { type: 'handle', width: 32 },
    },
    (item) => ({
      ...item,
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );
  const batchActions = {
    baseAction: [
      {
        label: 'common.export',
        eventTag: 'export',
      },
      {
        label: 'common.edit',
        eventTag: 'edit',
      },
      {
        label: 'common.move',
        eventTag: 'move',
      },
    ],
    moreAction: [
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
      },
    ],
  };
  const tableMoreActionList = [
    {
      eventTag: 'delete',
      label: t('common.delete'),
      danger: true,
    },
  ];

  const typeFilterVisible = ref(false);
  const typeFilters = ref(Object.keys(RequestMethods));
  const statusFilterVisible = ref(false);
  const statusFilters = ref(Object.keys(RequestDefinitionStatus));
  const tableQueryParams = ref<any>();
  function loadApiList() {
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds: props.activeModule === 'all' ? [] : [props.activeModule, ...props.offspringIds],
      env: checkedEnv.value,
      showSubdirectory: showSubdirectory.value,
      filter: { status: statusFilters.value, type: typeFilters.value },
    };
    setLoadListParams(params);
    loadList();
    tableQueryParams.value = {
      ...params,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    };
    emit('init', {
      ...tableQueryParams.value,
    });
  }

  function handleFilterHidden(val: boolean) {
    if (!val) {
      loadApiList();
    }
  }

  async function handleStatusChange(record: any) {
    try {
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    loadApiList();
  });

  function emitTableParams() {
    emit('init', {
      keyword: keyword.value,
      moduleIds: [],
      projectId: appStore.currentProjectId,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }

  const tableSelected = ref<(string | number)[]>([]);
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  /**
   * 删除接口
   */
  function deleteApi(record?: any, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('apiTestManagement.deleteApiTipTitle', { name: record?.name });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('apiTestManagement.batchDeleteApiTip', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('apiTestManagement.deleteApiTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleTableMoreActionSelect(item: ActionsItem, record: any) {
    switch (item.eventTag) {
      case 'delete':
        deleteApi(record);
        break;
      default:
        break;
    }
  }

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  const showBatchModal = ref(false);
  const batchUpdateLoading = ref(false);
  const batchFormRef = ref<FormInstance>();
  const batchForm = ref({
    attr: '',
    value: '',
    values: [],
  });
  const attrOptions = [
    {
      name: 'apiTestManagement.apiStatus',
      value: 'status',
    },
    {
      name: 'apiTestManagement.apiType',
      value: 'type',
    },
    {
      name: 'common.tag',
      value: 'tag',
    },
  ];
  const valueOptions = computed(() => {
    switch (batchForm.value.attr) {
      case 'status':
        return [
          {
            name: 'apiTestManagement.processing',
            value: RequestDefinitionStatus.PROCESSING,
          },
          {
            name: 'apiTestManagement.done',
            value: RequestDefinitionStatus.DONE,
          },
          {
            name: 'apiTestManagement.deprecate',
            value: RequestDefinitionStatus.DEPRECATED,
          },
          {
            name: 'apiTestManagement.debugging',
            value: RequestDefinitionStatus.DEBUGGING,
          },
        ];
      default:
        return [];
    }
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

  function batchUpdate() {
    batchFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          batchUpdateLoading.value = true;
          Message.success(t('common.updateSuccess'));
          cancelBatch();
          resetSelector();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          batchUpdateLoading.value = false;
        }
      }
    });
  }

  const moveModalVisible = ref(false);
  const selectedModuleKeys = ref<(string | number)[]>([]); // 移动文件选中节点
  const isBatchMove = ref(false); // 是否批量移动文件
  const activeApi = ref<any | null>(null); // 当前查看的接口项
  const batchMoveApiLoading = ref(false); // 批量移动文件loading

  /**
   * 单个/批量移动接口
   */
  async function handleApiMove() {
    try {
      batchMoveApiLoading.value = true;
      // await batchMoveFile({
      //   selectIds: isBatchMove.value ? batchParams.value?.selectedIds || [] : [activeApi.value?.id || ''],
      //   selectAll: !!batchParams.value?.selectAll,
      //   excludeIds: batchParams.value?.excludeIds || [],
      //   condition: { keyword: keyword.value },
      //   projectId: appStore.currentProjectId,
      //   moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
      //   moveModuleId: selectedModuleKeys.value[0],
      // });
      Message.success(t('apiTestManagement.batchMoveSuccess'));
      if (isBatchMove.value) {
        tableSelected.value = [];
        isBatchMove.value = false;
      } else {
        activeApi.value = null;
      }
      loadList();
      resetSelector();
      emitTableParams();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchMoveApiLoading.value = false;
    }
  }

  function handleMoveApiModalCancel() {
    moveModalVisible.value = false;
    selectedModuleKeys.value = [];
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(keys: (string | number)[]) {
    selectedModuleKeys.value = keys;
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'delete':
        deleteApi(undefined, true, params);
        break;
      case 'edit':
        showBatchModal.value = true;
        break;
      case 'move':
        isBatchMove.value = true;
        moveModalVisible.value = true;
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
