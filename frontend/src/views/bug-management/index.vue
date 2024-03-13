<template>
  <MsCard simple>
    <MsAdvanceFilter
      v-model:keyword="keyword"
      :search-placeholder="t('caseManagement.featureCase.searchByIdAndName')"
      :filter-config-list="filterConfigList"
      :row-count="filterRowCount"
      @keyword-search="fetchData"
      @adv-search="handleAdvSearch"
      @refresh="handleAdvSearch"
    >
      <template #left>
        <div class="flex gap-[12px]">
          <a-button v-permission="['PROJECT_BUG:READ+ADD']" type="primary" @click="handleCreate"
            >{{ t('bugManagement.createBug') }}
          </a-button>
          <a-button v-permission="['PROJECT_BUG:READ+IMPORT']" :loading="!isComplete" type="outline" @click="handleSync"
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
      @sorter-change="saveSort"
    >
      <!-- ID -->
      <template #num="{ record, rowIndex }">
        <a-button type="text" class="px-0" @click="handleShowDetail(record.id, rowIndex)">{{ record.num }}</a-button>
      </template>
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap">
          <span v-permission="['PROJECT_BUG:READ+ADD']" class="flex flex-row">
            <MsButton class="!mr-0" @click="handleCopy(record)">{{ t('common.copy') }}</MsButton>
            <a-divider class="h-[16px]" direction="vertical" />
          </span>
          <span v-permission="['PROJECT_BUG:READ+UPDATE']" class="flex flex-row">
            <MsButton class="!mr-0" @click="handleEdit(record)">{{ t('common.edit') }}</MsButton>
            <a-divider class="h-[16px]" direction="vertical" />
          </span>
          <MsTableMoreAction
            v-permission="['PROJECT_BUG:READ+DELETE']"
            :list="moreActionList"
            trigger="click"
            @select="handleMoreActionSelect($event, record)"
          />
        </div>
      </template>

      <template #createUserFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="createUserFilterVisible"
          v-model:status-filters="createUserFilterValue"
          :title="(columnConfig.title as string)"
          :list="createUserFilterOptions"
          value-key="value"
          @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>

      <template #updateUserFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="updateUserFilterVisible"
          v-model:status-filters="updateUserFilterValue"
          :title="(columnConfig.title as string)"
          :list="updateUserFilterOptions"
          value-key="value"
          @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>

      <template #handleUserFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="handleUserFilterVisible"
          v-model:status-filters="handleUserFilterValue"
          :title="(columnConfig.title as string)"
          :list="handleUserFilterOptions"
          value-key="value"
          @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>

      <template #statusFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="statusFilterVisible"
          v-model:status-filters="statusFilterValue"
          :title="(columnConfig.title as string)"
          :list="statusFilterOptions"
          value-key="value"
          @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>

      <template #severityFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="severityFilterVisible"
          v-model:status-filters="severityFilterValue"
          :title="(columnConfig.title as string)"
          :list="severityFilterOptions"
          value-key="value"
          @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
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
    @ok="handleSyncModalOk"
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
    <div class="mb-[8px] mt-[16px]">{{ t('bugManagement.createTime') }}</div>
    <div class="flex flex-row gap-[8px]">
      <a-select v-model="syncObject.operator" class="w-[120px]">
        <a-option
          v-for="option in timeSelectOptions"
          :key="option.label"
          :label="t(option.label)"
          :value="option.value"
        />
      </a-select>
      <a-date-picker v-model="syncObject.time" value-format="timestamp" show-time class="w-[304px]" />
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
    @submit="fetchData"
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
    @submit="fetchData"
  />
</template>

<script lang="ts" async setup>
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { useIntervalFn } from '@vueuse/core';
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
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';

  import {
    deleteBatchBug,
    deleteSingleBug,
    exportBug,
    getBugList,
    getCustomFieldHeader,
    getCustomOptionHeader,
    getExportConfig,
    getSyncStatus,
    syncBugEnterprise,
    syncBugOpenSource,
  } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import router from '@/router';
  import { useAppStore, useTableStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';
  import {
    customFieldDataToTableData,
    customFieldToColumns,
    downloadByteFile,
    tableParamsToRequestParams,
  } from '@/utils';

  import { BugEditCustomField, BugListItem, BugOptionItem } from '@/models/bug-management';
  import { RouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

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
  const route = useRoute();
  const createUserFilterOptions = ref<BugOptionItem[]>([]);
  const createUserFilterVisible = ref(false);
  const createUserFilterValue = ref<string[]>([]);
  const updateUserFilterOptions = ref<BugOptionItem[]>([]);
  const updateUserFilterVisible = ref(false);
  const updateUserFilterValue = ref<string[]>([]);
  const handleUserFilterOptions = ref<BugOptionItem[]>([]);
  const handleUserFilterVisible = ref(false);
  const handleUserFilterValue = ref<string[]>([]);
  const statusFilterOptions = ref<BugOptionItem[]>([]);
  const statusFilterVisible = ref(false);
  const statusFilterValue = ref<string[]>([]);
  const severityFilterOptions = ref<BugOptionItem[]>([]);
  const severityFilterVisible = ref(false);
  const severityFilterValue = ref<string[]>([]);
  const severityColumnId = ref('');

  // 是否同步完成
  const isComplete = ref(false);
  // 自定义字段
  const customFields = ref<BugEditCustomField[]>([]);
  // 当前选择的条数
  const currentSelectParams = ref<BatchActionQueryParams>({ selectAll: false, currentSelectCount: 0 });
  // 排序
  const sort = ref<{ [key: string]: string }>({});

  const syncObject = reactive({
    time: 0,
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

    //  实例化自定义字段的filters
    customFields.value.forEach((item) => {
      if ((item.fieldName === '严重程度' || item.fieldName === 'Bug Degree') && item.options) {
        severityFilterOptions.value = [];
        severityColumnId.value = `custom_single_${item.fieldId}`;
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

  const columns: MsTableColumn = [
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      slotName: 'num',
      width: 80,
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
      width: 200,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'statusName',
      width: 84,
      slotName: 'status',
      titleSlotName: 'statusFilter',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUser',
      slotName: 'handleUser',
      showTooltip: true,
      titleSlotName: 'handleUserFilter',
      width: 75,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'bugManagement.numberOfCase',
      dataIndex: 'relationCaseCount',
      slotName: 'numberOfCase',
      width: 80,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'bugManagement.belongPlatform',
      width: 135,
      showDrag: true,
      dataIndex: 'platform',
      showInTable: true,
    },
    {
      title: 'bugManagement.tag',
      showDrag: true,
      isStringTag: true,
      width: 456,
      dataIndex: 'tags',
      showInTable: true,
    },
    {
      title: 'bugManagement.creator',
      dataIndex: 'createUser',
      slotName: 'createUser',
      width: 112,
      showTooltip: true,
      showDrag: true,
      titleSlotName: 'createUserFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
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
      width: 112,
      showTooltip: true,
      showDrag: true,
      titleSlotName: 'updateUserFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
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
      width: 158,
    },
  ];

  const { propsRes, propsEvent, setKeyword, setAdvanceFilter, setLoadListParams, setProps, resetSelector, loadList } =
    useTable(
      getBugList,
      {
        tableKey: TableKeyEnum.BUG_MANAGEMENT,
        selectable: true,
        showJumpMethod: true,
        noDisable: false,
        showSetting: true,
      },
      (record: TableData) => ({
        ...record,
        handleUser: record.handleUserName,
        createUser: record.createUserName,
        updateUser: record.updateUserName,
        ...customFieldDataToTableData(record.customFields, customFields.value),
      })
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
      const params = tableParamsToRequestParams(currentSelectParams.value);
      const blob = await exportBug({
        ...params,
        exportColumns: option.map((item) => item),
        projectId: appStore.currentProjectId,
        exportSort: sort.value,
      });
      downloadByteFile(blob, `${t('bugManagement.exportBug')}.zip`);
      exportVisible.value = false;
      resetSelector();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
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
      params: {
        mode: 'add',
      },
    });
  };

  const checkSyncStatus = async () => {
    const { complete } = await getSyncStatus(appStore.currentProjectId);
    isComplete.value = complete;
  };
  /** 同步缺陷 */
  const { pause, resume } = useIntervalFn(() => {
    checkSyncStatus();
  }, 1000);
  // 初始化组件时关闭
  pause();
  watchEffect(() => {
    if (isComplete.value) {
      // 同步完成时关闭轮询
      pause();
    }
  });
  // 同步缺陷按钮触发
  const handleSync = async () => {
    if (isXpack.value) {
      // 企业版
      syncVisible.value = true;
    } else {
      try {
        // 开源版
        await syncBugOpenSource(appStore.currentProjectId);
        isComplete.value = false;
        // 开始轮询
        resume();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  };
  const handleSyncModalOk = async () => {
    try {
      await syncBugEnterprise({
        projectId: appStore.currentProjectId,
        pre: syncObject.operator === 'lt',
        createTime: syncObject.time,
      });
      isComplete.value = false;
      // 开始轮询
      resume();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
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
    let dataCount = params.selectedIds?.length;
    if (params.selectAll) {
      dataCount = params.currentSelectCount;
    }
    openDeleteModal({
      title: t('bugManagement.deleteCount', { count: dataCount }),
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

  async function initFilterOptions() {
    const res = await getCustomOptionHeader(appStore.currentProjectId);
    createUserFilterOptions.value = res.userOption;
    updateUserFilterOptions.value = res.userOption;
    handleUserFilterOptions.value = res.handleUserOption;
    statusFilterOptions.value = res.statusOption;
  }

  function saveSort(sortObj: { [key: string]: string }) {
    sort.value = sortObj;
  }

  function initTableParams() {
    const filterParams = {
      status: statusFilterValue.value,
      handleUser: handleUserFilterValue.value,
      updateUser: updateUserFilterValue.value,
      createUser: createUserFilterValue.value,
    };
    filterParams[severityColumnId.value] = severityFilterValue.value;
    return {
      keyword: keyword.value,
      projectId: projectId.value,
      filter: { ...filterParams },
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

  watchEffect(() => {
    setProps({ heightUsed: heightUsed.value });
  });

  onBeforeMount(() => {
    // 进入页面时检查当前项目轮训状态
    checkSyncStatus();
  });

  onMounted(() => {
    setLoadListParams({ projectId: projectId.value });
    setExportOptionData();
    initFilterOptions();
    fetchData();
    if (route.query.id) {
      // 分享或成功进来的页面
      handleShowDetail(route.query.id as string, 0);
    }
  });

  let customColumns: MsTableColumn = [];
  async function getColumnHeaders() {
    try {
      customColumns = await getCustomFieldColumns();
      customColumns.forEach((item) => {
        if (item.title === '严重程度' || item.title === 'Bug Degree') {
          item.showInTable = true;
          item.titleSlotName = 'severityFilter';
          item.slotName = 'severity';
        } else {
          item.showInTable = false;
        }
      });
    } catch (error) {
      console.log(error);
    }
  }
  await getColumnHeaders();

  await tableStore.initColumn(TableKeyEnum.BUG_MANAGEMENT, columns.concat(customColumns), 'drawer');
  onUnmounted(() => {
    // 组件销毁时关闭轮询
    pause();
  });
</script>

<style lang="less" scoped>
  :deep(.arco-divider-vertical) {
    margin: 0 8px;
  }
</style>
