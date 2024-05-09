<template>
  <div :class="['p-[8px_22px]', props.class]">
    <div :class="['mb-[8px]', 'flex', 'items-center', props.isApi ? 'justify-between' : 'justify-end']">
      <a-button
        v-show="props.isApi"
        v-permission="['PROJECT_API_DEFINITION_MOCK:READ+ADD']"
        type="primary"
        @click="createMock"
      >
        {{ t('mockManagement.createMock') }}
      </a-button>
      <div class="flex gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiTestManagement.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadMockList"
          @press-enter="loadMockList"
          @clear="loadMockList"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="loadMockList">
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
      <template #expectNum="{ record }">
        <MsButton type="text" @click="handleOpenDetail(record)">
          {{ record.expectNum }}
        </MsButton>
      </template>
      <template #enable="{ record }">
        <a-switch
          v-model="record.enable"
          size="small"
          type="line"
          @change="(value) => changeDefault(value, record)"
        ></a-switch>
      </template>
      <template #action="{ record }">
        <MsButton type="text" class="!mr-0" @click="debugMock(record)">
          {{ t('apiTestManagement.debug') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsButton type="text" class="!mr-0" @click="handleCopyMock(record)">
          {{ t('common.copy') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction :list="tableMoreActionList" @select="handleTableMoreActionSelect($event, record)" />
      </template>
      <template v-if="hasAnyPermission(['PROJECT_API_DEFINITION_MOCK:READ+ADD']) && props.isApi" #empty>
        <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{ t('apiTestManagement.tableNoDataAndPlease') }}
          <MsButton class="ml-[8px]" @click="createMock">
            {{ t('mockManagement.createMock') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
  </div>
  <mockDetailDrawer
    v-if="mockDetailDrawerVisible"
    v-model:visible="mockDetailDrawerVisible"
    :definition-detail="mockBelongDefinitionDetail"
    :detail-id="activeMockRecord?.id"
    :is-copy="isCopy"
    @add-done="loadMockList"
    @delete="() => removeMock(activeMockRecord)"
  />
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import {
    batchDeleteMock,
    deleteMock,
    getDefinitionDetail,
    getDefinitionMockPage,
    getMockUrl,
    updateMockStatusPage,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { ApiDefinitionMockDetail } from '@/models/apiTest/management';
  import { OrdTemplateManagement } from '@/models/setting/template';
  import { RequestComposition } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const mockDetailDrawer = defineAsyncComponent(() => import('./mockDetailDrawer.vue'));

  const props = defineProps<{
    isApi?: boolean; // 接口定义详情的case tab下
    class?: string;
    activeModule: string;
    offspringIds: string[];
    definitionDetail: RequestParam;
    readOnly?: boolean; // 是否是只读模式
    protocol: string; // 查看的协议类型
  }>();
  const emit = defineEmits<{
    (e: 'init', params: any): void;
    (e: 'change'): void;
  }>();

  const appStore = useAppStore();
  const tableStore = useTableStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const keyword = ref('');

  let columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'expectNum',
      slotName: 'expectNum',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      width: 100,
    },
    {
      title: 'mockManagement.name',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
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
      title: 'mockManagement.apiPath',
      dataIndex: 'apiPath',
      slotName: 'apiPath',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'common.status',
      dataIndex: 'enable',
      slotName: 'enable',
      width: 100,
    },
    {
      title: 'mockManagement.operationUser',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      width: 200,
      showDrag: true,
    },
    {
      title: 'mockManagement.updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
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
    getDefinitionMockPage,
    {
      columns: props.readOnly ? columns : [],
      scroll: { x: '100%' },
      tableKey: props.readOnly ? undefined : TableKeyEnum.API_TEST,
      showSetting: !props.readOnly,
      selectable: true,
      showSelectAll: !props.readOnly,
      draggable: props.readOnly ? undefined : { type: 'handle', width: 32 },
      showSubdirectory: true,
    },
    (item) => ({
      ...item,
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );
  const batchActions = {
    baseAction: [
      {
        label: 'mockManagement.batchEnable',
        eventTag: 'batchEnable',
      },
      {
        label: 'mockManagement.batchDisEnable',
        eventTag: 'batchDisEnable',
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
      eventTag: 'copyMock',
      label: t('mockManagement.copyMock'),
      danger: false,
    },
    {
      eventTag: 'delete',
      label: t('common.delete'),
      danger: true,
    },
  ];

  const tableQueryParams = ref<any>();

  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all') {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.API_TEST_MANAGEMENT_CASE);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return moduleIds;
  }

  async function loadMockList() {
    const selectModules = await getModuleIds();
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      protocol: props.protocol,
      apiDefinitionId: props.definitionDetail.id !== 'all' ? props.definitionDetail.id : undefined,
      filter: {},
      moduleIds: selectModules,
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

  watchEffect(() => {
    if (props.activeModule || props.protocol) {
      loadMockList();
    }
  });

  const changeDefault = async (value: any, record: OrdTemplateManagement) => {
    try {
      await updateMockStatusPage(record.id);
      Message.success(t('system.orgTemplate.setSuccessfully'));
      loadMockList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

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
  function removeMock(record?: ApiDefinitionMockDetail, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('apiTestManagement.deleteApiTipTitle', { name: record?.name });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('apiTestManagement.batchDeleteMockTip', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('apiTestManagement.deleteMockTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            const selectModules = await getModuleIds();
            await batchDeleteMock({
              selectIds,
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              condition: { keyword: keyword.value },
              projectId: appStore.currentProjectId,
              moduleIds: selectModules,
            });
          } else {
            await deleteMock({
              id: record?.id as string,
              projectId: appStore.currentProjectId,
            });
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadMockList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const { copy, isSupported } = useClipboard({ legacy: true });

  async function copyMockUrl(record: ApiDefinitionMockDetail) {
    try {
      appStore.showLoading();
      const url = await getMockUrl(record.id);
      if (isSupported) {
        copy(url);
        Message.success(t('common.copySuccess'));
      } else {
        Message.warning(t('common.copyNotSupport'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleTableMoreActionSelect(item: ActionsItem, record: ApiDefinitionMockDetail) {
    switch (item.eventTag) {
      case 'delete':
        deleteMock(record);
        break;
      case 'copyMock':
        copyMockUrl(record);
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

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'delete':
        removeMock(undefined, true, params);
        break;
      default:
        break;
    }
  }

  const mockDetailDrawerVisible = ref(false);
  const activeMockRecord = ref<ApiDefinitionMockDetail>();

  function createMock() {
    activeMockRecord.value = undefined;
    mockDetailDrawerVisible.value = true;
  }

  const mockBelongDefinitionDetail = ref<RequestParam>(props.definitionDetail);
  async function openMockDetailDrawer(record: ApiDefinitionMockDetail) {
    try {
      activeMockRecord.value = record;
      if (props.definitionDetail.id === 'all') {
        // 从全部 mock 列表页查看 mock 详情，需要先加载其接口定义详情
        appStore.showLoading();
        const res = await getDefinitionDetail(record.apiDefinitionId);
        mockBelongDefinitionDetail.value = {
          ...(res.request as RequestParam),
          id: res.id,
          type: 'mock',
          isNew: false,
          protocol: res.protocol,
          activeTab: RequestComposition.BODY,
          executeLoading: false,
          responseDefinition: res.response,
        };
      } else {
        mockBelongDefinitionDetail.value = props.definitionDetail;
      }
      mockDetailDrawerVisible.value = true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  const isCopy = ref(false);

  function handleOpenDetail(record: ApiDefinitionMockDetail) {
    isCopy.value = false;
    openMockDetailDrawer(record);
  }

  function handleCopyMock(record: ApiDefinitionMockDetail) {
    isCopy.value = true;
    openMockDetailDrawer(record);
  }

  function debugMock(record: ApiDefinitionMockDetail) {
    activeMockRecord.value = record;
  }

  defineExpose({
    loadMockList,
  });

  if (!props.readOnly) {
    await tableStore.initColumn(TableKeyEnum.API_TEST, columns, 'drawer');
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
