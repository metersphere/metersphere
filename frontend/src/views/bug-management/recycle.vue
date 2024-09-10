<template>
  <MsCard simple no-content-padding>
    <div class="h-full p-[16px]">
      <MsAdvanceFilter
        v-model:keyword="keyword"
        :search-placeholder="t('bugManagement.recycle.searchPlaceholder')"
        :filter-config-list="filterConfigList"
        @keyword-search="fetchData"
        @refresh="searchData"
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
        <template #deleteUserName="{ record }">
          <a-tooltip :content="`${record.deleteUserName}`" position="tl">
            <div class="one-line-text">{{ characterLimit(record.deleteUserName) }}</div>
          </a-tooltip>
        </template>
        <template #statusName="{ record }">
          <a-tooltip :content="`${record.statusName}`" position="tl">
            <div class="one-line-text">{{ characterLimit(record.statusName) }}</div>
          </a-tooltip>
        </template>
        <template #operation="{ record }">
          <div class="flex flex-row flex-nowrap">
            <MsButton v-permission="['PROJECT_BUG:READ+DELETE']" class="!mr-0" @click="handleRecover(record)">
              {{ t('bugManagement.recycle.recover') }}
            </MsButton>
            <a-divider direction="vertical" />
            <MsButton v-permission="['PROJECT_BUG:READ+DELETE']" class="!mr-0" @click="handleDelete(record)">
              {{ t('bugManagement.recycle.permanentlyDelete') }}
            </MsButton>
          </div>
        </template>

        <template #deleteTimeColumn="{ record }">
          {{ dayjs(record.deleteTime).format('YYYY-MM-DD HH:mm:ss') || '-' }}
        </template>
        <template #empty> </template>
      </MsBaseTable>
    </div>
  </MsCard>
</template>

<script lang="ts" async setup>
  import { ref } from 'vue';
  import { Message, TableData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import {
    deleteBatchByRecycle,
    deleteSingleByRecycle,
    getCustomFieldHeader,
    getCustomOptionHeader,
    getRecycleList,
    recoverBatchByRecycle,
    recoverSingleByRecycle,
  } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit, customFieldDataToTableData, customFieldToColumns } from '@/utils';

  import { BugEditCustomField, BugListItem, BugOptionItem } from '@/models/bug-management';
  import { FilterType } from '@/enums/advancedFilterEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { makeColumns } from '@/views/case-management/caseManagementFeature/components/utils';

  const { t } = useI18n();
  const { openDeleteModal } = useModal();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const projectId = computed(() => appStore.currentProjectId);
  const keyword = ref('');
  // 自定义字段
  const customFields = ref<BugEditCustomField[]>([]);
  const filterConfigList = reactive<FilterFormItem[]>([
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      type: FilterType.INPUT,
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
  ]);

  const severityFilterOptions = ref<BugOptionItem[]>([]);
  const currentSelectParams = ref<BatchActionQueryParams>({ selectAll: false, currentSelectCount: 0 });

  let columns: MsTableColumn = [
    {
      title: 'bugManagement.recycle.deleteTime',
      dataIndex: 'deleteTime',
      showDrag: true,
      width: 199,
      slotName: 'deleteTimeColumn',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.recycle.deleteMan',
      dataIndex: 'deleteUser',
      width: 125,
      showDrag: true,
      slotName: 'deleteUserName',
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      width: 100,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      width: 250,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUser',
      slotName: 'handleUser',
      showTooltip: true,
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      width: 125,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'status',
      width: 100,
      slotName: 'statusName',
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'bugManagement.tag',
      showDrag: true,
      width: 456,
      isStringTag: true,
      dataIndex: 'tags',
    },
    {
      title: 'bugManagement.creator',
      dataIndex: 'createUser',
      slotName: 'createUser',
      width: 125,
      showTooltip: true,
      showDrag: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      showDrag: true,
      width: 199,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.updateUser',
      dataIndex: 'updateUser',
      width: 125,
      showTooltip: true,
      showDrag: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.updateTime',
      dataIndex: 'updateTime',
      showDrag: true,
      width: 199,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 150,
    },
  ];

  // 获取自定义字段
  const getCustomFieldColumns = async () => {
    const res = await getCustomFieldHeader(projectId.value);
    customFields.value = res;

    //  实例化自定义字段的filters
    customFields.value.forEach((item) => {
      if ((item.fieldName === '严重程度' || item.fieldName === 'Bug Degree') && item.options) {
        item.options.forEach((option) => {
          severityFilterOptions.value.push({
            value: option.value,
            text: option.text,
          });
        });
      }
    });

    return customFieldToColumns(res);
  };

  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams, resetSelector, resetFilterParams } = useTable(
    getRecycleList,
    {
      tableKey: TableKeyEnum.BUG_MANAGEMENT_RECYCLE,
      selectable: true,
      noDisable: true,
      showSetting: true,
      scroll: { x: '1900px' },
      heightUsed: 256,
    },
    (record: TableData) => ({
      ...record,
      handleUser: record.handleUserName,
      createUser: record.createUserName,
      updateUser: record.updateUserName,
      ...customFieldDataToTableData(record.customFields, customFields.value),
    })
  );

  const tableAction = {
    baseAction: [
      {
        eventTag: 'recover',
        label: t('bugManagement.recycle.recover'),
        permission: ['PROJECT_BUG:READ+DELETE'],
      },
      {
        eventTag: 'delete',
        label: t('bugManagement.recycle.permanentlyDelete'),
        permission: ['PROJECT_BUG:READ+DELETE'],
      },
    ],
  };

  function initTableParams() {
    return {
      keyword: keyword.value,
      projectId: projectId.value,
      condition: {
        keyword: keyword.value,
        filter: propsRes.value.filter,
      },
    };
  }

  function searchData() {
    setLoadListParams(initTableParams());
    loadList();
  }

  const fetchData = async (v = '') => {
    setKeyword(v);
    searchData();
  };

  // 单个恢复
  const handleRecover = async (record: BugListItem) => {
    try {
      await recoverSingleByRecycle(record.id);
      Message.success(t('bugManagement.recycle.recoverSuccess'));
      resetSelector();
      resetFilterParams();
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 批量恢复
  const handleBatchRecover = async () => {
    try {
      appStore.showLoading(t('bugManagement.recycle.recovering'));
      const tmpObj = { ...currentSelectParams.value, projectId: projectId.value };
      await recoverBatchByRecycle(tmpObj);
      appStore.hideLoading();
      Message.success(t('bugManagement.recycle.recoverSuccess'));
      keyword.value = '';
      resetSelector();
      resetFilterParams();
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
          resetSelector();
          resetFilterParams();
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };
  // 批量删除
  const handleBatchDelete = () => {
    openDeleteModal({
      title: t('bugManagement.recycle.batchDelete', { count: currentSelectParams.value.currentSelectCount }),
      content: t('bugManagement.recycle.deleteContent'),
      onBeforeOk: async () => {
        try {
          const tmpObj = { ...currentSelectParams.value, projectId: projectId.value };
          await deleteBatchByRecycle(tmpObj);
          Message.success(t('common.deleteSuccess'));
          keyword.value = '';
          resetSelector();
          resetFilterParams();
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
    if (params.condition) {
      params.condition.filter = propsRes.value.filter;
    } else {
      params.condition = { filter: { ...propsRes.value.filter } };
    }
    const condition = {
      keyword: keyword.value,
      filter: params.condition.filter,
    };
    currentSelectParams.value = {
      excludeIds: params.excludeIds,
      selectAll: params.selectAll,
      selectIds: params.selectedIds,
      currentSelectCount: params.currentSelectCount,
      condition,
    };

    switch (event.eventTag) {
      case 'recover':
        handleBatchRecover();
        break;
      case 'delete':
        handleBatchDelete();
        break;
      default:
        break;
    }
  }

  async function initFilterOptions() {
    const res = await getCustomOptionHeader(appStore.currentProjectId);
    const filterOptionsMaps: Record<string, any> = {
      status: res.statusOption,
      handleUser: res.handleUserOption,
      createUser: res.userOption,
      updateUser: res.userOption,
      deleteUser: res.userOption,
    };
    columns = makeColumns(filterOptionsMaps, columns);
  }

  let customColumns: MsTableColumn = [];

  async function getColumnHeaders() {
    try {
      customColumns = await getCustomFieldColumns();
      customColumns.forEach((item) => {
        if (item.title === '严重程度' || item.title === 'Bug Degree') {
          item.showInTable = true;
          item.slotName = 'severity';
          item.dataIndex = `custom_single_${item.dataIndex}`;
          item.filterConfig = {
            options: cloneDeep(unref(severityFilterOptions.value)) || [],
            labelKey: 'text',
          };
        } else {
          item.showInTable = false;
        }
      });
    } catch (error) {
      console.log(error);
    }
  }

  await getColumnHeaders();
  await initFilterOptions();

  await tableStore.initColumn(TableKeyEnum.BUG_MANAGEMENT_RECYCLE, columns.concat(customColumns), 'drawer');

  onMounted(() => {
    setLoadListParams({ projectId: projectId.value });
    fetchData();
  });
</script>

<style lang="less" scoped>
  .ms-table--special-small();
</style>
