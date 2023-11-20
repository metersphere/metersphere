<template>
  <MsCard simple>
    <div class="flex flex-row justify-between">
      <div class="flex gap-[12px]">
        <a-button type="primary" @click="handleCreate">
          {{ t('bugManagement.createBug') }}
        </a-button>
        <a-button type="outline" @click="handleSync">
          {{ t('bugManagement.syncBug') }}
        </a-button>
      </div>
      <div class="flex flex-row gap-[8px]">
        <a-input-search
          v-model="keyword"
          :placeholder="t('system.user.searchUser')"
          class="w-[240px]"
          allow-clear
          @press-enter="fetchData"
          @search="fetchData"
        ></a-input-search>
        <FilterIcon v-model:visible="filterVisible" :count="filterCount" />
      </div>
    </div>
    <FilterForm
      v-show="filterVisible"
      v-model:count="filterCount"
      :visible="filterVisible"
      :config-list="filterConfigList"
      class="mt-[8px]"
      @on-search="handleFilter"
      @data-index-change="dataIndexChange"
    />
    <MsBaseTable v-bind="propsRes" v-on="propsEvent">
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
</template>

<script lang="ts" setup>
  import { Message } from '@arco-design/web-vue';

  import { FilterForm, FilterIcon } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { updateOrAddProjectUserGroup } from '@/api/modules/project-management/usergroup';
  import { postProjectTableByOrg } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';
  import router from '@/router';
  import { useAppStore, useTableStore } from '@/store';

  import { BugListItem } from '@/models/bug-management';
  import { OrgProjectTableItem } from '@/models/setting/system/orgAndProject';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const keyword = ref('');
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const projectId = computed(() => appStore.currentProjectId);
  const filterVisible = ref(false);
  const filterCount = ref(0);
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
    },
    {
      title: 'bugManagement.severity',
      dataIndex: 'severity',
      type: FilterType.MUTIPLE_SELECT,
    },
  ]);

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

  const handleNameChange = async (record: OrgProjectTableItem) => {
    try {
      await updateOrAddProjectUserGroup(record);
      Message.success(t('common.updateSuccess'));
      return true;
    } catch (error) {
      return false;
    }
  };

  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams } = useTable(
    postProjectTableByOrg,
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

  const fetchData = async () => {
    setKeyword(keyword.value);
    await loadList();
  };

  const handleCreate = () => {
    // eslint-disable-next-line no-console
    console.log('create');
  };
  const handleSync = () => {
    // eslint-disable-next-line no-console
    console.log('sync');
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

  const handleFilter = (filter: FilterResult) => {
    // eslint-disable-next-line no-console
    console.log('filter', filter);
  };

  const dataIndexChange = (dataIndex: string) => {
    // eslint-disable-next-line no-console
    console.log('dataIndexChange', dataIndex);
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

  onMounted(() => {
    setLoadListParams({ projectId: projectId.value });
    fetchData();
  });
</script>
