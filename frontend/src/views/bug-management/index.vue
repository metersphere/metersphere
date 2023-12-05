<template>
  <MsCard simple>
    <MsAdvanceFilter :filter-config-list="filterConfigList" :row-count="filterRowCount">
      <template #left>
        <div class="flex gap-[12px]">
          <a-button type="primary" @click="handleCreate">{{ t('bugManagement.createBug') }} </a-button>
          <a-button type="outline" @click="handleSync">{{ t('bugManagement.syncBug') }} </a-button>
          <a-button type="outline" @click="handleExport">{{ t('common.export') }} </a-button>
        </div>
      </template>
    </MsAdvanceFilter>
    <MsBaseTable v-bind="propsRes" v-on="propsEvent">
      <template #name="{ record, rowIndex }">
        <a-button type="text" class="px-0" @click="handleShowDetail(record.id, rowIndex)">{{ record.name }}</a-button>
      </template>
      <template #numberOfCase="{ record }">
        <span class="cursor-pointer text-[rgb(var(--primary-5))]" @click="jumpToTestPlan(record)">{{
          record.memberCount
        }}</span>
      </template>
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap">
          <MsButton class="!mr-0" @click="handleCopy(record)">{{ t('common.copy') }}</MsButton>
          <a-divider direction="vertical" />
          <MsButton class="!mr-0" @click="handleEdit(record)">{{ t('common.edit') }}</MsButton>
          <a-divider direction="vertical" />
          <MsButton class="!mr-0" status="danger" @click="handleDelete(record)">{{ t('common.delete') }}</MsButton>
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
  <MsExportDrawer v-model:visible="exportVisible" :all-data="exportOptionData" />
  <BugDetailDrawer
    v-model:visible="detailVisible"
    :detail-id="activeDetailId"
    :detail-index="activeCaseIndex"
    :table-data="propsRes.data"
    :page-change="propsEvent.pageChange"
    :pagination="propsRes.msPagination!"
  />
</template>

<script lang="ts" setup>
  import { Message } from '@arco-design/web-vue';

  import { MsAdvanceFilter, timeSelectOptions } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsExportDrawer from '@/components/pure/ms-export-drawer/index.vue';
  import { MsExportDrawerMap } from '@/components/pure/ms-export-drawer/types';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import BugDetailDrawer from './components/bug-detail-drawer.vue';

  import { getBugList, getExportConfig } from '@/api/modules/bug-management';
  import { updateOrAddProjectUserGroup } from '@/api/modules/project-management/usergroup';
  import { useI18n } from '@/hooks/useI18n';
  import router from '@/router';
  import { useAppStore, useTableStore } from '@/store';

  import { BugListItem } from '@/models/bug-management';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

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
      title: 'bugManagement.severity',
      dataIndex: 'severity',
      type: FilterType.SELECT,
      selectProps: {
        mode: 'static',
        multiple: true,
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
      title: 'bugManagement.ID',
      dataIndex: 'num',
      showTooltip: true,
    },
    {
      title: 'bugManagement.bugName',
      editType: ColumnEditTypeEnum.INPUT,
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'bugManagement.severity',
      slotName: 'memberCount',
      showDrag: true,
      dataIndex: 'severity',
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'status',
      showDrag: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUser',
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'bugManagement.numberOfCase',
      dataIndex: 'relationCaseCount',
      slotName: 'numberOfCase',
      showDrag: true,
    },
    {
      title: 'bugManagement.belongPlatform',
      width: 180,
      showDrag: true,
      dataIndex: 'platform',
    },
    {
      title: 'bugManagement.tag',
      showDrag: true,
      isStringTag: true,
      dataIndex: 'tag',
    },
    {
      title: 'bugManagement.creator',
      dataIndex: 'createUser',
      showDrag: true,
    },
    {
      title: 'bugManagement.updateUser',
      dataIndex: 'updateUser',
      showDrag: true,
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      showDrag: true,
    },
    {
      title: 'bugManagement.updateTime',
      dataIndex: 'updateTime',
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 230,
    },
  ];
  await tableStore.initColumn(TableKeyEnum.BUG_MANAGEMENT, columns, 'drawer');

  const handleNameChange = async (record: BugListItem) => {
    try {
      await updateOrAddProjectUserGroup(record);
      Message.success(t('common.updateSuccess'));
      return true;
    } catch (error) {
      return false;
    }
  };

  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams, setProps } = useTable(
    getBugList,
    {
      tableKey: TableKeyEnum.BUG_MANAGEMENT,
      selectable: false,
      noDisable: false,
      showJumpMethod: true,
      showSetting: true,
      scroll: { x: '1769px' },
    },
    undefined,
    (record) => handleNameChange(record)
  );

  watchEffect(() => {
    setProps({ heightUsed: heightUsed.value });
  });

  const fetchData = async (v = '') => {
    setKeyword(v);
    await loadList();
  };

  const handleCreate = () => {
    router.push({
      name: 'bugManagementBugEdit',
    });
  };
  const handleSync = () => {
    syncVisible.value = true;
  };

  const handleShowDetail = (id: string, rowIndex: number) => {
    detailVisible.value = true;
    activeDetailId.value = id;
    activeCaseIndex.value = rowIndex;
  };

  const handleCopy = (record: BugListItem) => {
    // eslint-disable-next-line no-console
    console.log('create', record);
  };

  const handleEdit = (record: BugListItem) => {
    // eslint-disable-next-line no-console
    console.log('create', record);
  };

  const handleDelete = (record: BugListItem) => {
    // eslint-disable-next-line no-console
    console.log('create', record);
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

  onMounted(() => {
    setLoadListParams({ projectId: projectId.value });
    fetchData();
    setExportOptionData();
  });
</script>
