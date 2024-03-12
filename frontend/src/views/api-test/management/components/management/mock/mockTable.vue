<template>
  <div :class="['p-[16px_22px]', props.class]">
    <div class="mb-[16px] flex items-center justify-end">
      <div class="flex items-center gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiTestManagement.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadMockList"
          @press-enter="loadMockList"
        />
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
      <template #action="{ record }">
        <a-switch
          v-model="record.enable"
          size="small"
          type="line"
          @change="(value) => changeDefault(value, record)"
        ></a-switch>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction :list="tableMoreActionList" @select="handleTableMoreActionSelect($event, record)" />
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import {
    deleteDefinitionMockMock,
    getDefinitionMockPage,
    updateMockStatusPage,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import { ApiDefinitionMockDetail } from '@/models/apiTest/management';
  import { OrdTemplateManagement } from '@/models/setting/template';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    class?: string;
    activeModule: string;
    offspringIds: string[];
    protocol: string; // 查看的协议类型
    readOnly?: boolean; // 是否是只读模式
  }>();
  const emit = defineEmits<{
    (e: 'init', params: any): void;
    (e: 'change'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const showSubdirectory = ref(false);
  const checkedEnv = ref('DEV');
  const keyword = ref('');

  let columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'id',
      slotName: 'id',
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

  function loadMockList() {
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      filter: {},
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

  watch(
    () => props.activeModule,
    () => {
      loadMockList();
    }
  );

  watch(
    () => props.protocol,
    () => {
      loadMockList();
    }
  );

  const changeDefault = async (value: any, record: OrdTemplateManagement) => {
    try {
      await updateMockStatusPage(record.id);
      Message.success(t('system.orgTemplate.setSuccessfully'));
      loadMockList();
    } catch (error) {
      console.log(error);
    }
  };

  onBeforeMount(() => {
    loadMockList();
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
  function deleteMock(record?: ApiDefinitionMockDetail, isBatch?: boolean, params?: BatchActionQueryParams) {
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
            // await batchDeleteMock({
            //   selectIds,
            //   selectAll: !!params?.selectAll,
            //   excludeIds: params?.excludeIds || [],
            //   condition: { keyword: keyword.value },
            //   projectId: appStore.currentProjectId,
            // });
          } else {
            await deleteDefinitionMockMock({
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

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleTableMoreActionSelect(item: ActionsItem, record: ApiDefinitionMockDetail) {
    switch (item.eventTag) {
      case 'delete':
        deleteMock(record);
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
        deleteMock(undefined, true, params);
        break;
      default:
        break;
    }
  }

  defineExpose({
    loadMockList,
  });

  if (!props.readOnly) {
    const tableStore = useTableStore();
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
