<template>
  <div v-if="showTitle" class="title">
    <span class="text-[var(--color-text-1)]">{{ t('project.environmental.httpTitle') }}</span>
    <span class="cursor-pointer text-[var(--color-text-2)]" @click="handleNoWarning">{{
      t('project.environmental.httpNoWarning')
    }}</span>
  </div>
  <div class="flex items-center justify-between">
    <a-button type="outline" @click="handleAddHttp">{{ t('project.environmental.addHttp') }}</a-button>
    <div class="flex flex-row gap-[8px]">
      <a-input-number v-model:model-value="form.linkOutTime" class="w-[180px]">
        <template #prefix>
          <span class="text-[var(--color-text-3)]">{{ t('project.environmental.http.linkTimeOut') }}</span>
        </template>
      </a-input-number>
      <a-input-number v-model:model-value="form.timeOutTime" class="w-[180px]">
        <template #prefix>
          <span class="text-[var(--color-text-3)]">{{ t('project.environmental.http.linkTimeOut') }}</span>
        </template>
      </a-input-number>
      <a-select v-model:model-value="form.authType" class="w-[200px]">
        <template #prefix>
          <span class="text-[var(--color-text-3)]">{{ t('project.environmental.http.authType') }}</span>
        </template>
        <a-option>Basic Auth</a-option>
        <a-option>Basic Auth2</a-option>
        <a-option>Basic Auth3</a-option>
      </a-select>
    </div>
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
  <AddHttpDrawer v-model:visible="addVisible" :current-obj="currentObj" @close="addVisible = false" />
</template>

<script lang="ts" async setup>
  import { TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddHttpDrawer from './popUp/AddHttpDrawer.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { BugListItem } from '@/models/bug-management';
  import { HttpForm } from '@/models/projectManagement/environmental';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const store = useProjectEnvStore();

  const showTitle = computed(() => store.httpNoWarning);
  const tableStore = useTableStore();
  const addVisible = ref(false);
  const currentObj = ref<HttpForm>({
    id: '',
    hostname: '',
    enableCondition: 'none',
    path: '',
    operator: '',
    headerParams: [],
  });
  const columns: MsTableColumn = [
    {
      title: 'project.environmental.http.host',
      dataIndex: 'host',
      showTooltip: true,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.http.desc',
      dataIndex: 'desc',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.http.applyScope',
      dataIndex: 'applyScope',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.http.enableScope',
      dataIndex: 'enableScope',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.http.value',
      dataIndex: 'value',
      showTooltip: true,
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
  const { propsRes, propsEvent } = useTable(undefined, {
    tableKey: TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_HTTP,
    scroll: { x: '100%' },
    selectable: false,
    noDisable: true,
    showSetting: true,
    showPagination: false,
    showMode: false,
    debug: true,
  });

  const form = reactive({
    linkOutTime: undefined,
    timeOutTime: undefined,
    authType: 'Basic Auth',
  });

  const moreActionList: ActionsItem[] = [
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
    },
  ];

  const handleSingleDelete = (record?: TableData) => {
    if (record) {
      const index = store.currentEnvDetailInfo.config.httpConfig.findIndex((item) => item.id === record.id);
      if (index > -1) {
        store.currentEnvDetailInfo.config.httpConfig.splice(index, 1);
      }
    }
  };

  function handleMoreActionSelect(item: ActionsItem, record: BugListItem) {
    if (item.eventTag === 'delete') {
      handleSingleDelete(record);
    }
  }
  const handleCopy = (record: any) => {
    currentObj.value = record;
    currentObj.value.id = '';
    addVisible.value = true;
  };
  const handleEdit = (record: any) => {
    currentObj.value = record;
    addVisible.value = true;
  };

  const handleAddHttp = () => {
    currentObj.value = {
      id: '',
      hostname: '',
      enableCondition: 'none',
      path: '',
      operator: '',
      headerParams: [],
    };
    addVisible.value = true;
  };
  const handleNoWarning = () => {
    store.setHttpNoWarning(false);
  };
  watch(store.currentEnvDetailInfo.config.httpConfig, () => {
    propsRes.value.data = store.currentEnvDetailInfo.config.httpConfig;
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
