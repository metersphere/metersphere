<template>
  <div class="flex items-center justify-between">
    <a-button type="outline" @click="handleAdd">{{ t('project.environmental.database.addDatabase') }}</a-button>
    <a-input-search
      v-model="keyword"
      class="w-[240px]"
      allow-clear
      @press-enter="fetchData"
      @search="fetchData"
    ></a-input-search>
  </div>
  <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
    <template #operation="{ record }">
      <div class="flex flex-row flex-nowrap">
        <MsButton class="!mr-0" @click="handleCopy(record)">{{ t('common.copy') }}</MsButton>
        <a-divider direction="vertical" />
        <MsButton class="!mr-0" @click="handleEdit(record)">{{ t('common.edit') }}</MsButton>
        <a-divider direction="vertical" />
        <MsTableMoreAction :list="moreActionList" trigger="click" @select="handleMoreActionSelect($event, record)" />
      </div>
    </template>
  </MsBaseTable>
  <AddDatabaseModal
    v-model:visible="addVisible"
    :current-database="currentDatabase"
    :current-id="currentId"
    @close="addVisible = false"
    @add-or-update="handleAddOrUpdate"
  />
</template>

<script lang="ts" async setup>
  import { TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn, MsTableDataItem } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddDatabaseModal from './popUp/addDatabaseModal.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { BugListItem } from '@/models/bug-management';
  import { CommonList } from '@/models/common';
  import { DataSourceItem } from '@/models/projectManagement/environmental';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const store = useProjectEnvStore();

  const innerParam = computed(() => store.currentEnvDetailInfo.config.dataSources || []);

  const keyword = ref('');
  const tableStore = useTableStore();
  const addVisible = ref(false);
  const currentId = ref('');

  const currentDatabase = ref<DataSourceItem>({
    id: '',
    name: '',
    driverId: '',
    dbUrl: '',
    username: '',
    password: '',
    poolMax: 1,
    timeout: 1000,
    enable: true,
  });

  const columns: MsTableColumn = [
    {
      title: 'project.environmental.database.name',
      dataIndex: 'name',
      showTooltip: true,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.database.driver',
      dataIndex: 'desc',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'URL',
      dataIndex: 'url',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.database.username',
      dataIndex: 'username',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.database.poolMax',
      dataIndex: 'poolMax',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.database.timeout',
      dataIndex: 'timeout',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 170,
    },
  ];
  await tableStore.initColumn(TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_HTTP, columns);
  const { propsRes, propsEvent } = useTable(
    () =>
      Promise.resolve([]) as unknown as Promise<
        MsTableDataItem<DataSourceItem> | CommonList<MsTableDataItem<DataSourceItem>>
      >,
    {
      tableKey: TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_HTTP,
      scroll: { x: '100%' },
      selectable: false,
      showSetting: true,
      showPagination: false,
      showMode: false,
      isSimpleSetting: true,
    }
  );

  const moreActionList: ActionsItem[] = [
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
    },
  ];

  const handleSingleDelete = (record?: TableData) => {
    if (record) {
      const index = innerParam.value.findIndex((item) => item.id === record.id);
      innerParam.value.splice(index, 1);
    }
  };

  function handleMoreActionSelect(item: ActionsItem, record: BugListItem) {
    if (item.eventTag === 'delete') {
      handleSingleDelete(record);
    }
  }

  const handleCopy = (record: any) => {
    addVisible.value = true;
    currentId.value = '';
    currentDatabase.value = { ...record, id: '' };
  };
  const handleEdit = (record: any) => {
    addVisible.value = true;
    currentId.value = record.id;
  };
  const handleAdd = () => {
    currentDatabase.value = { name: '', dbUrl: '', username: '' };
    addVisible.value = true;
  };
  const fetchData = () => {
    if (keyword.value) {
      propsRes.value.data = innerParam.value.filter((item) => item.name.includes(keyword.value));
    } else {
      propsRes.value.data = innerParam.value;
    }
  };
  const handleAddOrUpdate = (data: DataSourceItem, cb: (v: boolean) => void) => {
    if (data.id) {
      const index = innerParam.value.findIndex((item) => item.id === data.id);
      store.currentEnvDetailInfo.config.dataSources[index] = data;
    } else {
      data.id = new Date().getTime().toString();
      store.currentEnvDetailInfo.config.dataSources.push(data);
    }
    cb(true);
  };
  watch(innerParam.value, () => {
    fetchData();
  });
</script>

<style lang="less" scoped>
  .title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 0 16px;
    height: 38px;
    border: 1px solid rgb(var(--primary-5));
    border-radius: 4px;
    background-color: rgb(var(--primary-1));
  }
</style>
