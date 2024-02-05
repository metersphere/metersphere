<template>
  <MsCard simple>
    <MsAdvanceFilter
      v-model:keyword="keyword"
      :filter-config-list="filterConfigList"
      :row-count="filterRowCount"
      @keyword-search="fetchData"
      @adv-search="handleAdvSearch"
    >
      <template #left>
        <div class="flex gap-[12px]">
          <a-button v-permission="['PROJECT_BUG:READ+ADD']" type="primary" @click="handleCreate"
            >{{ t('bugManagement.createBug') }}
          </a-button>
          <a-button
            v-permission="['PROJECT_BUG:READ+IMPORT']"
            :disabled="syncBugLoading"
            type="outline"
            @click="handleSync"
            >{{ t('bugManagement.syncBug') }}
          </a-button>
        </div>
      </template>
    </MsAdvanceFilter>
    <MsBaseTable
      class="mt-[16px]"
      v-bind="propsRes"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <!-- ID -->
      <template #num="{ record, rowIndex }">
        <a-button type="text" class="px-0" @click="handleShowDetail(record.id, rowIndex)">{{ record.num }}</a-button>
      </template>
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap">
          <span v-permission="['PROJECT_BUG:READ+ADD']" class="flex flex-row">
            <MsButton class="!mr-0" @click="handleCopy(record)">{{ t('common.copy') }}</MsButton>
            <a-divider direction="vertical" />
          </span>
          <span v-permission="['PROJECT_BUG:READ+UPDATE']" class="flex flex-row">
            <MsButton class="!mr-0" @click="handleEdit(record)">{{ t('common.edit') }}</MsButton>
            <a-divider direction="vertical" />
          </span>
          <MsTableMoreAction
            v-permission="['PROJECT_BUG:READ+DELETE']"
            :list="moreActionList"
            trigger="click"
            @select="handleMoreActionSelect($event, record)"
          />
        </div>
      </template>
      <template #empty> </template>
    </MsBaseTable>
  </MsCard>
  <a-modal
    v-model:visible="syncVisible"
    title-align="start"
    class="ms-modal-form ms-modal-small"
    :ok-text="t('bugManagement.sync')"
    unmount-on-close
    @cancel="handleSyncCancel()"
  >
    <template #title>
      <div class="flex flex-row items-center gap-[4px]">
        <div class="medium text-[var(--color-text-1)]">{{ t('bugManagement.syncBug') }} </div>
        <a-tooltip position="top">
          <template #content>
            <div>{{ t('bugManagement.syncBugTipRowOne') }}</div>
            <div>{{ t('bugManagement.syncBugTipRowTwo') }}</div>
          </template>
          <MsIcon class="text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
        </a-tooltip>
      </div>
    </template>
    <div
      class="flex flex-row items-center gap-[8px] rounded-[4px] border-[1px] border-[rgb(var(--primary-5))] bg-[rgb(var(--primary-1))] px-[16px] py-[12px]"
    >
      <icon-exclamation-circle-fill class="text-[rgb(var(--primary-5))]" />
      <div>{{ t('bugManagement.bugAutoSync', { name: '每天00:00:00' }) }}</div>
    </div>
    <div class="mb-[8px] mt-[16px]">{{ t('bugManagement.syncTime') }}</div>
    <div class="flex flex-row gap-[8px]">
      <a-select v-model="syncObject.operator" class="w-[120px]">
        <a-option
          v-for="option in timeSelectOptions"
          :key="option.label"
          :label="t(option.label)"
          :value="option.value"
        />
      </a-select>
      <a-date-picker v-model="syncObject.time" show-time class="w-[304px]" />
    </div>
  </a-modal>
  <MsExportDrawer v-model:visible="exportVisible" :all-data="exportOptionData" @confirm="exportConfirm">
    <template #title>
      <span class="text-[var(--color-text-1)]">{{ t('bugManagement.exportBug') }}</span>
      <span v-if="currentSelectParams.currentSelectCount" class="text-[var(--color-text-4)]"
        >({{ t('bugManagement.exportBugCount', { count: currentSelectParams.currentSelectCount }) }})</span
      >
    </template>
  </MsExportDrawer>
  <BugDetailDrawer
    v-model:visible="detailVisible"
    :detail-id="activeDetailId"
    :detail-index="activeCaseIndex"
    :table-data="propsRes.data"
    :page-change="propsEvent.pageChange"
    :pagination="propsRes.msPagination!"
  />
  <DeleteModal
    :id="currentDeleteObj.id"
    v-model:visible="deleteVisible"
    :name="currentDeleteObj.title"
    :remote-func="deleteSingleBug"
    @submit="handleSingleDelete"
  />
  <BatchEditModal
    v-model:visible="batchEditVisible"
    :select-param="currentSelectParams"
    :custom-fields="customFields"
  />
</template>

<script lang="ts" async setup>
  import { Message, TableData } from '@arco-design/web-vue';

  import { MsAdvanceFilter, timeSelectOptions } from '@/components/pure/ms-advance-filter';
  import { BackEndEnum, FilterFormItem, FilterResult, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsExportDrawer from '@/components/pure/ms-export-drawer/index.vue';
  import { MsExportDrawerMap, MsExportDrawerOption } from '@/components/pure/ms-export-drawer/types';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import BatchEditModal from './components/batchEditModal.vue';
  import BugDetailDrawer from './components/bug-detail-drawer.vue';
  import DeleteModal from './components/deleteModal.vue';

  import {
    deleteBatchBug,
    deleteSingleBug,
    exportBug,
    getBugList,
    getCustomFieldHeader,
    getExportConfig,
    syncBugOpenSource,
  } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import router from '@/router';
  import { useAppStore, useTableStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';
  import { customFieldToColumns, tableParamsToRequestParams } from '@/utils';

  import { BugEditCustomField, BugListItem } from '@/models/bug-management';
  import { RouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { useRequest } from 'ahooks-vue';

  const { t } = useI18n();

  const tableStore = useTableStore();
  const appStore = useAppStore();
  const projectId = computed(() => appStore.currentProjectId);
  const filterVisible = ref(false);
  const filterRowCount = ref(0);
  const syncVisible = ref(false);
  const exportVisible = ref(false);
  const exportOptionData = ref<MsExportDrawerMap>({});
  const detailVisible = ref(false);
  const activeDetailId = ref<string>('');
  const activeCaseIndex = ref<number>(0);
  const currentDeleteObj = reactive<{ id: string; title: string }>({ id: '', title: '' });
  const deleteVisible = ref(false);
  const batchEditVisible = ref(false);
  const keyword = ref('');
  const filterResult = ref<FilterResult>({ accordBelow: 'AND', combine: {} });
  const licenseStore = useLicenseStore();
  const isXpack = computed(() => licenseStore.hasLicense());
  const { openDeleteModal } = useModal();
  // 自定义字段
  const customFields = ref<BugEditCustomField[]>([]);
  // 当前选择的条数
  const currentSelectParams = ref<BatchActionQueryParams>({ selectAll: false, currentSelectCount: 0 });

  const { loading: syncBugLoading, run: syncBugRun } = useRequest(
    () => syncBugOpenSource({ projectId: projectId.value }),
    {
      pollingInterval: 1 * 1000,
      pollingWhenHidden: true,
      manual: true,
    }
  );

  const syncObject = reactive({
    time: '',
    operator: '',
  });
  const handleSyncCancel = () => {
    syncVisible.value = false;
  };
  const filterConfigList = reactive<FilterFormItem[]>([
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      type: FilterType.INPUT,
      backendType: BackEndEnum.NUMBER,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      type: FilterType.INPUT,
      backendType: BackEndEnum.STRING,
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
  ]);

  const heightUsed = computed(() => 286 + (filterVisible.value ? 160 + (filterRowCount.value - 1) * 60 : 0));

  // 获取自定义字段
  const getCustomFieldColumns = async () => {
    const res = await getCustomFieldHeader(projectId.value);
    customFields.value = res;
    return customFieldToColumns(res);
  };

  const columns: MsTableColumn = [
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      slotName: 'num',
      width: 80,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      width: 200,
      showTooltip: true,
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'statusName',
      width: 84,
      slotName: 'status',
      showDrag: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUser',
      showTooltip: true,
      width: 75,
      showDrag: true,
    },
    {
      title: 'bugManagement.numberOfCase',
      dataIndex: 'relationCaseCount',
      slotName: 'numberOfCase',
      width: 80,
      showDrag: true,
    },
    {
      title: 'bugManagement.belongPlatform',
      width: 135,
      showDrag: true,
      dataIndex: 'platform',
    },
    {
      title: 'bugManagement.tag',
      showDrag: true,
      isStringTag: true,
      width: 200,
      dataIndex: 'tag',
    },
    {
      title: 'bugManagement.creator',
      dataIndex: 'createUser',
      width: 112,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'bugManagement.updateUser',
      dataIndex: 'updateUser',
      width: 112,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      showDrag: true,
      width: 199,
    },
    {
      title: 'bugManagement.updateTime',
      dataIndex: 'updateTime',
      showDrag: true,
      width: 199,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 158,
    },
  ];
  const customColumns = await getCustomFieldColumns();
  await tableStore.initColumn(TableKeyEnum.BUG_MANAGEMENT, columns.concat(customColumns), 'drawer');

  const { propsRes, propsEvent, setKeyword, setAdvanceFilter, setLoadListParams, setProps, resetSelector, loadList } =
    useTable(
      getBugList,
      {
        tableKey: TableKeyEnum.BUG_MANAGEMENT,
        selectable: true,
        noDisable: false,
        showJumpMethod: true,
        showSetting: true,
        scroll: { x: '1800px' },
      },
      (record: TableData) => ({ ...record, handleUser: record.handleUserName })
    );

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.export',
        eventTag: 'export',
        permission: ['PROJECT_BUG:READ+EXPORT'],
      },
      {
        label: 'common.edit',
        eventTag: 'edit',
        permission: ['PROJECT_BUG:READ+UPDATE'],
      },
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['PROJECT_BUG:READ+DELETE'],
      },
    ],
  };

  const fetchData = async (v = '') => {
    setKeyword(v);
    await loadList();
  };

  const handleAdvSearch = (filter: FilterResult) => {
    filterResult.value = filter;
    const { accordBelow, combine } = filter;
    setAdvanceFilter(filter);
    currentSelectParams.value = {
      ...currentSelectParams.value,
      condition: {
        keyword: keyword.value,
        searchMode: accordBelow,
        filter: propsRes.value.filter,
        combine,
      },
    };
    fetchData();
  };

  const exportConfirm = async (option: MsExportDrawerOption[]) => {
    try {
      const { selectedIds, selectAll, excludeIds } = currentSelectParams.value;
      await exportBug({
        selectIds: selectedIds || [],
        selectAll,
        excludeIds,
        condition: { keyword: keyword.value },
        bugExportColumns: option.map((item) => item),
      });
      Message.success(t('common.exportSuccess'));
      exportVisible.value = false;
      resetSelector();
    } catch (error) {
      Message.error(t('common.exportFail'));
    }
  };

  const handleSingleDelete = (record?: TableData) => {
    if (record) {
      currentDeleteObj.id = record.id;
      currentDeleteObj.title = record.title;
      deleteVisible.value = true;
    } else {
      fetchData();
      currentDeleteObj.id = '';
      currentDeleteObj.title = '';
    }
  };

  const handleCreate = () => {
    router.push({
      name: RouteEnum.BUG_MANAGEMENT_DETAIL,
    });
  };
  const handleSync = () => {
    if (isXpack.value) {
      syncVisible.value = true;
    } else {
      // 直接开始轮询
      syncBugRun();
    }
  };

  const handleShowDetail = (id: string, rowIndex: number) => {
    detailVisible.value = true;
    activeDetailId.value = id;
    activeCaseIndex.value = rowIndex;
  };

  const handleCopy = (record: BugListItem) => {
    router.push({
      name: RouteEnum.BUG_MANAGEMENT_DETAIL,
      query: {
        id: record.id,
      },
      params: {
        mode: 'copy',
      },
    });
  };

  const handleEdit = (record: BugListItem) => {
    router.push({
      name: RouteEnum.BUG_MANAGEMENT_DETAIL,
      query: {
        id: record.id,
      },
      params: {
        mode: 'edit',
      },
    });
  };

  const handleBatchDelete = (params: BatchActionQueryParams) => {
    openDeleteModal({
      title: t('bugManagement.deleteCount', { count: params.selectedIds?.length }),
      content: t('bugManagement.deleteTip'),
      onBeforeOk: async () => {
        try {
          const tmpObj = {
            ...tableParamsToRequestParams(params),
            projectId: projectId.value,
          };
          await deleteBatchBug(tmpObj);
          Message.success(t('common.deleteSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };

  const handleBatchEdit = (params: BatchActionQueryParams) => {
    batchEditVisible.value = true;
    const condition = {
      keyword: keyword.value,
      searchMode: filterResult.value.accordBelow,
      filter: propsRes.value.filter,
      combine: filterResult.value.combine,
    };
    currentSelectParams.value = {
      ...params,
      condition,
    };
  };

  const handleExport = () => {
    exportVisible.value = true;
  };

  const jumpToTestPlan = (record: BugListItem) => {
    router.push({
      name: 'testPlan',
      query: {
        bugId: record.id,
        projectId: projectId.value,
      },
    });
  };

  const setExportOptionData = async () => {
    const res = await getExportConfig(projectId.value);
    exportOptionData.value = res;
  };

  const moreActionList: ActionsItem[] = [
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
    },
  ];

  function handleMoreActionSelect(item: ActionsItem, record: BugListItem) {
    if (item.eventTag === 'delete') {
      handleSingleDelete(record);
    }
  }

  const severityOption = [
    {
      label: t('bugManagement.severityO.fatal'),
      value: 'High',
    },
    {
      label: t('bugManagement.severityO.serious'),
      value: 'Medium',
    },
    {
      label: t('bugManagement.severityO.general'),
      value: 'Low',
    },
    {
      label: t('bugManagement.severityO.reminder'),
      value: 'Info',
    },
  ];

  const statusOption = [
    {
      label: t('bugManagement.statusO.create'),
      value: 'Create',
    },
    {
      label: t('bugManagement.statusO.processing'),
      value: 'Processing',
    },
    {
      label: t('bugManagement.statusO.resolved'),
      value: 'Resolved',
    },
    {
      label: t('bugManagement.statusO.closed'),
      value: 'Closed',
    },
    { label: t('bugManagement.statusO.refused'), value: 'Refused' },
  ];

  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    currentSelectParams.value = params;
    switch (event.eventTag) {
      case 'export':
        handleExport();
        break;
      case 'delete':
        handleBatchDelete(params);
        break;
      case 'edit':
        handleBatchEdit(params);
        break;
      default:
        break;
    }
  }

  watchEffect(() => {
    setProps({ heightUsed: heightUsed.value });
  });

  onMounted(() => {
    setLoadListParams({ projectId: projectId.value });
    fetchData();
    setExportOptionData();
  });
</script>

<style lang="less" scoped>
  :deep(.arco-divider-vertical) {
    margin: 0 8px;
  }
</style>
