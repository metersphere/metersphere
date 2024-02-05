<template>
  <MsCard simple>
    <MsAdvanceFilter
      v-model:keyword="keyword"
      :search-placeholder="t('bugManagement.recycle.searchPlaceholder')"
      :filter-config-list="filterConfigList"
      :row-count="filterRowCount"
      @keyword-search="fetchData"
    >
      <template #left>
        <div></div>
      </template>
    </MsAdvanceFilter>
    <MsBaseTable
      class="mt-[16px]"
      v-bind="propsRes"
      :action-config="tableAction"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap">
          <MsButton class="!mr-0" @click="handleRecover(record)">{{ t('bugManagement.recycle.recover') }}</MsButton>
          <a-divider direction="vertical" />
          <MsButton class="!mr-0" @click="handleDelete(record)">{{
            t('bugManagement.recycle.permanentlyDelete')
          }}</MsButton>
        </div>
      </template>
      <template #empty> </template>
    </MsBaseTable>
  </MsCard>
</template>

<script lang="ts" async setup>
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import {
    deleteBatchByRecycle,
    deleteSingleByRecycle,
    getRecycleList,
    recoverBatchByRecycle,
    recoverSingleByRecycle,
  } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit, tableParamsToRequestParams } from '@/utils';

  import { BugListItem } from '@/models/bug-management';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const { openDeleteModal } = useModal();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const projectId = computed(() => appStore.currentProjectId);
  const filterVisible = ref(false);
  const filterRowCount = ref(0);
  const keyword = ref('');
  const filterConfigList = reactive<FilterFormItem[]>([
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'name',
      type: FilterType.SELECT,
      selectProps: {
        mode: 'static',
      },
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
  ]);

  const heightUsed = computed(() => 286 + (filterVisible.value ? 160 + (filterRowCount.value - 1) * 60 : 0));

  const columns: MsTableColumn = [
    {
      title: 'bugManagement.recycle.deleteTime',
      dataIndex: 'deleteTime',
      showDrag: true,
      width: 180,
    },
    {
      title: 'bugManagement.recycle.deleteMan',
      dataIndex: 'deleteUserName',
      showDrag: true,
    },
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      showDrag: true,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      showTooltip: true,
    },
    {
      title: 'bugManagement.creator',
      dataIndex: 'createUser',
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      showDrag: true,
      width: 180,
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'statusName',
      showDrag: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUserName',
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'bugManagement.tag',
      showDrag: true,
      isStringTag: true,
      dataIndex: 'tags',
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 150,
    },
  ];
  await tableStore.initColumn(TableKeyEnum.BUG_MANAGEMENT_RECYCLE, columns, 'drawer');

  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams, setProps } = useTable(
    getRecycleList,
    {
      tableKey: TableKeyEnum.BUG_MANAGEMENT_RECYCLE,
      selectable: true,
      noDisable: true,
      showSetting: true,
      scroll: { x: '1900px' },
    },
    (record) => {
      record.deleteTime = dayjs(record.deleteTime).format('YYYY-MM-DD HH:mm:ss');
      return record;
    }
  );

  const tableAction = {
    baseAction: [
      {
        eventTag: 'recover',
        label: t('bugManagement.recycle.recover'),
        permission: ['PROJECT_BUG:READ+UPDATE'],
      },
      {
        eventTag: 'delete',
        label: t('bugManagement.recycle.permanentlyDelete'),
        permission: ['PROJECT_BUG:READ+DELETE'],
      },
    ],
  };

  watchEffect(() => {
    setProps({ heightUsed: heightUsed.value });
  });

  const fetchData = async (v = '') => {
    setKeyword(v);
    await loadList();
  };

  // 单个恢复
  const handleRecover = async (record: BugListItem) => {
    try {
      await recoverSingleByRecycle(record.id);
      Message.success(t('bugManagement.recycle.recoverSuccess'));
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 批量恢复
  const handleBatchRecover = async (params: BatchActionQueryParams) => {
    try {
      const tmpObj = { ...tableParamsToRequestParams(params), projectId: projectId.value };
      await recoverBatchByRecycle(tmpObj);
      Message.success(t('bugManagement.recycle.recoverSuccess'));
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 单个删除
  const handleDelete = (record: BugListItem) => {
    openDeleteModal({
      title: t('bugManagement.recycle.permanentlyDeleteTip', { name: characterLimit(record.title) }),
      content: t('bugManagement.recycle.deleteContent'),
      onBeforeOk: async () => {
        try {
          await deleteSingleByRecycle(record.id);
          Message.success(t('common.deleteSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };
  // 批量删除
  const handleBatchDelete = (params: BatchActionQueryParams) => {
    openDeleteModal({
      title: t('bugManagement.recycle.batchDelete', { count: params.currentSelectCount }),
      content: t('bugManagement.recycle.deleteContent'),
      onBeforeOk: async () => {
        try {
          const tmpObj = { ...tableParamsToRequestParams(params), projectId: projectId.value };
          await deleteBatchByRecycle(tmpObj);
          Message.success(t('common.deleteSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };

  // 处理表格选中后批量操作
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    switch (event.eventTag) {
      case 'recover':
        handleBatchRecover(params);
        break;
      case 'delete':
        handleBatchDelete(params);
        break;
      default:
        break;
    }
  }

  onMounted(() => {
    setLoadListParams({ projectId: projectId.value });
    fetchData();
  });
</script>
