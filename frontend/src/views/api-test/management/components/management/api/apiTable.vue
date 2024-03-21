<template>
  <div :class="['p-[0_16px_16px_16px]', props.class]">
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
      @drag-change="handleTableDragSort"
      @module-change="loadApiList"
    >
      <template v-if="props.protocol === 'HTTP'" #methodFilter="{ columnConfig }">
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
      <template #num="{ record }">
        <MsButton type="text" @click="openApiTab(record)">{{ record.num }}</MsButton>
      </template>
      <template #method="{ record }">
        <a-select
          v-if="props.protocol === 'HTTP' && hasAnyPermission(['PROJECT_API_DEFINITION:READ+UPDATE'])"
          v-model:model-value="record.method"
          class="param-input w-full"
          size="mini"
          @change="() => handleMethodChange(record)"
        >
          <template #label>
            <apiMethodName :method="record.method" tag-size="small" is-tag />
          </template>
          <a-option v-for="item of Object.values(RequestMethods)" :key="item" :value="item">
            <apiMethodName :method="item" tag-size="small" is-tag />
          </a-option>
        </a-select>
        <apiMethodName v-else :method="record.method" tag-size="small" is-tag />
      </template>
      <template #status="{ record }">
        <a-select
          v-if="hasAnyPermission(['PROJECT_API_DEFINITION:READ+UPDATE'])"
          v-model:model-value="record.status"
          class="param-input w-full"
          size="mini"
          @change="() => handleStatusChange(record)"
        >
          <template #label>
            <apiStatus :status="record.status" size="small" />
          </template>
          <a-option v-for="item of Object.values(RequestDefinitionStatus)" :key="item" :value="item">
            <apiStatus :status="item" size="small" />
          </a-option>
        </a-select>
        <apiStatus v-else :status="record.status" size="small" />
      </template>
      <template #action="{ record }">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION:READ+EXECUTE']"
          type="text"
          class="!mr-0"
          @click="executeDefinition(record)"
        >
          {{ t('apiTestManagement.execute') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_DEFINITION:READ+EXECUTE']" direction="vertical" :margin="8"></a-divider>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION:READ+ADD']"
          type="text"
          class="!mr-0"
          @click="copyDefinition(record)"
        >
          {{ t('common.copy') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_DEFINITION:READ+ADD']" direction="vertical" :margin="8"></a-divider>
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
        v-if="batchForm.attr === 'tags'"
        field="values"
        :label="t('apiTestManagement.batchUpdate')"
        :validate-trigger="['blur', 'input']"
        :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
        required
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
        <apiMethodSelect v-if="batchForm.attr === 'method'" v-model:model-value="batchForm.value" />
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
      :is-modal="true"
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
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import moduleTree from '@/views/api-test/management/components/moduleTree.vue';

  import {
    batchDeleteDefinition,
    batchMoveDefinition,
    batchUpdateDefinition,
    deleteDefinition,
    getDefinitionPage,
    sortDefinition,
    updateDefinition,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { ApiDefinitionDetail } from '@/models/apiTest/management';
  import { DragSortParams } from '@/models/common';
  import { RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    class?: string;
    activeModule: string;
    offspringIds: string[];
    protocol: string; // 查看的协议类型
    readOnly?: boolean; // 是否是只读模式
  }>();
  const emit = defineEmits<{
    (e: 'openApiTab', record: ApiDefinitionDetail, isExecute?: boolean): void;
    (e: 'openCopyApiTab', record: ApiDefinitionDetail): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const folderTreePathMap = inject('folderTreePathMap');
  const refreshModuleTree: (() => Promise<any>) | undefined = inject('refreshModuleTree');
  const keyword = ref('');

  const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'PROJECT_API_DEFINITION:READ+DELETE',
      'PROJECT_API_DEFINITION:READ+ADD',
      'PROJECT_API_DEFINITION:READ+EXECUTE',
    ])
  );
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
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      width: 130,
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      width: 150,
    },
    {
      title: 'apiTestManagement.version',
      dataIndex: 'versionName',
      width: 100,
    },
    {
      title: 'apiTestManagement.createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
    {
      title: 'apiTestManagement.updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 150 : 50,
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
      heightUsed: 308,
    },
    (item) => ({
      ...item,
      fullPath: folderTreePathMap?.[item.moduleId],
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );
  const batchActions = {
    baseAction: [
      // {
      //   label: 'common.export',
      //   eventTag: 'export',
      // },
      {
        label: 'common.edit',
        eventTag: 'edit',
        permission: ['PROJECT_API_DEFINITION:READ+UPDATE'],
      },
      {
        label: 'common.move',
        eventTag: 'move',
        permission: ['PROJECT_API_DEFINITION:READ+UPDATE'],
      },
    ],
    moreAction: [
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['PROJECT_API_DEFINITION:READ+DELETE'],
      },
    ],
  };
  const tableMoreActionList = [
    {
      eventTag: 'delete',
      label: t('common.delete'),
      danger: true,
      permission: ['PROJECT_API_DEFINITION:READ+DELETE'],
    },
  ];

  const methodFilterVisible = ref(false);
  const methodFilters = ref(Object.keys(RequestMethods));
  const statusFilterVisible = ref(false);
  const statusFilters = ref(Object.keys(RequestDefinitionStatus));

  const tableStore = useTableStore();
  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all') {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.API_TEST);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return moduleIds;
  }
  async function loadApiList() {
    const moduleIds = await getModuleIds();

    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds,
      protocol: props.protocol,
      filter: {
        status:
          statusFilters.value.length === Object.keys(RequestDefinitionStatus).length ? undefined : statusFilters.value,
        method: methodFilters.value.length === Object.keys(RequestMethods).length ? undefined : methodFilters.value,
      },
    };
    setLoadListParams(params);
    loadList();
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

  async function handleMethodChange(record: ApiDefinitionDetail) {
    try {
      await updateDefinition({
        id: record.id,
        method: record.method,
      });
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function handleStatusChange(record: ApiDefinitionDetail) {
    try {
      await updateDefinition({
        id: record.id,
        status: record.status,
      });
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
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
   * 删除接口
   */
  function deleteApi(record?: ApiDefinitionDetail, isBatch?: boolean, params?: BatchActionQueryParams) {
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
          if (isBatch) {
            await batchDeleteDefinition({
              selectIds,
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              condition: { keyword: keyword.value },
              projectId: appStore.currentProjectId,
              moduleIds: await getModuleIds(),
              deleteAll: true,
              protocol: props.protocol,
            });
          } else {
            await deleteDefinition(record?.id as string);
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadList();
          if (typeof refreshModuleTree === 'function') {
            refreshModuleTree();
          }
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
  function handleTableMoreActionSelect(item: ActionsItem, record: ApiDefinitionDetail) {
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
  const fullAttrs = [
    {
      name: 'apiTestManagement.apiStatus',
      value: 'status',
    },
    {
      name: 'apiTestManagement.apiType',
      value: 'method',
    },
    {
      name: 'common.tag',
      value: 'tags',
    },
  ];
  const attrOptions = computed(() => {
    if (props.protocol === 'HTTP') {
      return fullAttrs;
    }
    return fullAttrs.filter((e) => e.value !== 'method');
  });
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
          await batchUpdateDefinition({
            selectIds: batchParams.value?.selectedIds || [],
            selectAll: !!batchParams.value?.selectAll,
            excludeIds: batchParams.value?.excludeIds || [],
            condition: { keyword: keyword.value },
            projectId: appStore.currentProjectId,
            moduleIds: await getModuleIds(),
            protocol: props.protocol,
            type: batchForm.value.attr,
            [batchForm.value.attr]: batchForm.value.attr === 'tags' ? batchForm.value.values : batchForm.value.value,
          });
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
  const activeApi = ref<ApiDefinitionDetail | null>(null); // 当前查看的接口项
  const batchMoveApiLoading = ref(false); // 批量移动文件loading

  /**
   * 单个/批量移动接口
   */
  async function handleApiMove() {
    try {
      batchMoveApiLoading.value = true;
      await batchMoveDefinition({
        selectIds: isBatchMove.value ? batchParams.value?.selectedIds || [] : [activeApi.value?.id || ''],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: { keyword: keyword.value },
        projectId: appStore.currentProjectId,
        moduleIds: await getModuleIds(),
        moduleId: selectedModuleKeys.value[0],
        protocol: props.protocol,
      });
      Message.success(t('common.batchMoveSuccess'));
      if (isBatchMove.value) {
        tableSelected.value = [];
        isBatchMove.value = false;
      } else {
        activeApi.value = null;
      }
      loadList();
      resetSelector();
      if (typeof refreshModuleTree === 'function') {
        refreshModuleTree();
      }
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

  function openApiTab(record: ApiDefinitionDetail) {
    emit('openApiTab', record);
  }

  function copyDefinition(record: ApiDefinitionDetail) {
    emit('openCopyApiTab', record);
  }

  function executeDefinition(record: ApiDefinitionDetail) {
    emit('openApiTab', record, true);
  }

  // 拖拽排序
  async function handleTableDragSort(params: DragSortParams) {
    try {
      await sortDefinition({
        ...params,
      });
      Message.success(t('caseManagement.featureCase.sortSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  defineExpose({
    loadApiList,
  });

  if (!props.readOnly) {
    await tableStore.initColumn(TableKeyEnum.API_TEST, columns, 'drawer', true);
  } else {
    columns = columns.filter(
      (item) => !['version', 'createTime', 'updateTime', 'operation'].includes(item.dataIndex as string)
    );
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
