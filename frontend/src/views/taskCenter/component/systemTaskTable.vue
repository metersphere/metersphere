<template>
  <div class="my-[16px] flex items-center justify-end">
    <a-input-search
      v-model:model-value="keyword"
      :placeholder="t('ms.taskCenter.search')"
      class="mr-[12px] w-[240px]"
      allow-clear
      @search="searchTask"
      @press-enter="searchTask"
      @clear="searchTask"
    />
    <MsTag no-margin size="large" :tooltip-disabled="true" class="cursor-pointer" theme="outline" @click="searchTask">
      <MsIcon class="text-[16px] text-[var(color-text-4)]" :size="32" type="icon-icon_reset_outlined" />
    </MsTag>
  </div>
  <ms-base-table
    v-bind="propsRes"
    :action-config="tableBatchActions"
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
    <template #status="{ record }">
      <a-switch v-model:model-value="record.enable" size="small"></a-switch>
    </template>
    <template #action="{ record }">
      <MsButton v-permission="['SYSTEM_USER:READ+DELETE']" @click="deleteTask(record)">
        {{ t('common.delete') }}
      </MsButton>
    </template>
  </ms-base-table>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const { openModal } = useModal();
  const tableStore = useTableStore();

  const keyword = ref('');
  const tableSelected = ref<string[]>([]);
  const batchModalParams = ref();
  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      width: 100,
    },
    {
      title: 'ms.taskCenter.taskName',
      dataIndex: 'name',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'common.status',
      dataIndex: 'status',
      slotName: 'status',
      width: 50,
    },
    {
      title: 'ms.taskCenter.type',
      dataIndex: 'type',
      slotName: 'type',
      width: 120,
    },
    {
      title: 'ms.taskCenter.runRule',
      dataIndex: 'rule',
      width: 100,
    },
    {
      title: 'ms.taskCenter.operationUser',
      dataIndex: 'operationUser',
      width: 100,
    },
    {
      title: 'ms.taskCenter.operationTime',
      dataIndex: 'operationTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'ms.taskCenter.lastFinishTime',
      dataIndex: 'lastFinishTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'ms.taskCenter.nextExecuteTime',
      dataIndex: 'nextExecuteTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 60,
    },
  ];
  await tableStore.initColumn(TableKeyEnum.TASK_CENTER_SYSTEM_TASK, columns, 'drawer');

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.open',
        eventTag: 'open',
      },
      {
        label: 'common.close',
        eventTag: 'close',
      },
    ],
  };
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    () =>
      Promise.resolve({
        list: [
          {
            id: '1',
            num: 10086,
            name: '测试任务',
            status: 'success',
            type: 'API 导入',
            rule: '每 1 小时',
            enable: true,
            operationUser: 'admin',
            operationTime: '2021-09-01 12:00:00',
            lastFinishTime: '2021-09-01 12:00:00',
            nextExecuteTime: '2021-09-01 12:00:00',
          },
        ],
        total: 1,
      }),
    {
      tableKey: TableKeyEnum.TASK_CENTER_SYSTEM_TASK,
      scroll: { x: '100%' },
      selectable: true,
      heightUsed: 288,
      showSetting: true,
      size: 'default',
    },
    (item) => {
      return {
        ...item,
        operationTime: dayjs(item.operationTime).format('YYYY-MM-DD HH:mm:ss'),
        lastFinishTime: dayjs(item.lastFinishTime).format('YYYY-MM-DD HH:mm:ss'),
        nextExecuteTime: dayjs(item.nextExecuteTime).format('YYYY-MM-DD HH:mm:ss'),
      };
    }
  );

  function searchTask() {
    setLoadListParams({ keyword: keyword.value });
    loadList();
  }

  /**
   * 删除任务
   */
  function deleteTask(record?: any, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('ms.taskCenter.deleteTaskTitle', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('ms.taskCenter.deleteTimeTaskTitle', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('ms.taskCenter.deleteTimeTaskTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          // await deleteUserInfo({
          //   selectIds,
          //   selectAll: !!params?.selectAll,
          //   excludeIds: params?.excludeIds || [],
          //   condition: { keyword: keyword.value },
          // });
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

  function openTask(record?: any, isBatch?: boolean, params?: BatchActionQueryParams) {
    console.log(record);
  }

  function closeTask(record?: any, isBatch?: boolean, params?: BatchActionQueryParams) {
    console.log(record);
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchModalParams.value = params;
    switch (event.eventTag) {
      case 'open':
        openTask(undefined, true, params);
        break;
      case 'close':
        closeTask(undefined, true, params);
        break;
      default:
        break;
    }
  }

  onMounted(() => {
    loadList();
  });
</script>

<style lang="less" scoped></style>
