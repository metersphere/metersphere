<template>
  <MsCard simple>
    <MsAdvanceFilter :filter-config-list="filterConfigList" :row-count="filterRowCount">
      <template #left>
        <div></div>
      </template>
    </MsAdvanceFilter>
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
        </div>
      </template>
      <template #empty> </template>
    </MsBaseTable>
  </MsCard>
</template>

<script lang="ts" setup>
  import { Message } from '@arco-design/web-vue';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterType } from '@/components/pure/ms-advance-filter/type';
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

  const tableStore = useTableStore();
  const appStore = useAppStore();
  const projectId = computed(() => appStore.currentProjectId);
  const filterVisible = ref(false);
  const filterRowCount = ref(0);
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

  const handleNameChange = async (record: OrgProjectTableItem) => {
    try {
      await updateOrAddProjectUserGroup(record);
      Message.success(t('common.updateSuccess'));
      return true;
    } catch (error) {
      return false;
    }
  };

  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams, setProps } = useTable(
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

  watchEffect(() => {
    setProps({ heightUsed: heightUsed.value });
  });

  const fetchData = async (v = '') => {
    setKeyword(v);
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
