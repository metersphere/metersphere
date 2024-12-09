<template>
  <div>
    <div class="flex items-center justify-between">
      <a-button v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']" type="outline" @click="handleAdd">
        {{ t('project.environmental.database.addDatabase') }}
      </a-button>
      <a-input-search
        v-model="keyword"
        class="w-[240px]"
        allow-clear
        :placeholder="t('project.menu.nameSearch')"
        @press-enter="fetchData"
        @search="fetchData"
        @clear="fetchData"
      ></a-input-search>
    </div>
    <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
      <template #driverId="{ record }">
        {{ getDriver(record.driverId) }}
      </template>
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap items-center">
          <MsButton class="!mr-0" :disabled="isDisabled" @click="handleCopy(record)">{{ t('common.copy') }}</MsButton>
          <a-divider class="h-[16px]" direction="vertical" />
          <MsButton class="!mr-0" :disabled="isDisabled" @click="handleEdit(record)">{{ t('common.edit') }}</MsButton>
          <a-divider class="h-[16px]" direction="vertical" />
          <MsTableMoreAction
            v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']"
            :list="moreActionList"
            trigger="click"
            @select="handleMoreActionSelect($event, record)"
          />
        </div>
      </template>
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex w-full items-center justify-center text-[var(--color-text-4)]">
          <span v-if="hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE'])">{{
            t('caseManagement.caseReview.tableNoData')
          }}</span>
          <span v-else>{{ t('caseManagement.featureCase.tableNoData') }}</span>
          <MsButton v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']" class="ml-[8px]" @click="handleAdd">
            {{ t('project.environmental.database.addDatabase') }}
          </MsButton>
        </div>
      </template>
    </MsBaseTable>
  </div>
  <AddDatabaseModal
    v-model:visible="addVisible"
    :current-id="currentId"
    :is-copy="isCopy"
    @close="addVisible = false"
  />
</template>

<script lang="ts" async setup>
  import { TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddDatabaseModal from './popUp/addDatabaseModal.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';
  import { hasAnyPermission } from '@/utils/permission';

  import { BugListItem } from '@/models/bug-management';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const store = useProjectEnvStore();

  const innerParam = computed(() => store.currentEnvDetailInfo.config.dataSources || []);

  const keyword = ref('');
  const tableStore = useTableStore();
  const addVisible = ref(false);
  const currentId = ref('');
  const columns: MsTableColumn = [
    {
      title: 'project.environmental.database.name',
      dataIndex: 'dataSource',
      showTooltip: true,
      showDrag: false,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'project.environmental.database.driver',
      dataIndex: 'driver',
      slotName: 'driver',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'URL',
      dataIndex: 'dbUrl',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'project.environmental.database.username',
      dataIndex: 'username',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
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
      width: 130,
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
    showSetting: true,
    showPagination: false,
    heightUsed: 590,
    showMode: false,
  });

  const moreActionList: ActionsItem[] = [
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
    },
  ];

  const isDisabled = computed(() => !hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE']));
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

  function getDriver(driverId: string) {
    return driverId === 'oracle.jdbc.OracleDriver&oracle.jdbc.OracleDriver'
      ? 'oracle.jdbc.OracleDriver'
      : 'com.mysql.cj.jdbc.Driver';
  }

  const isCopy = ref<boolean>(false);

  /**
   * 复制
   */
  const handleCopy = (record: any) => {
    isCopy.value = true;
    currentId.value = record.id;
    addVisible.value = true;
  };

  /**
   * 编辑
   */
  const handleEdit = (record: any) => {
    isCopy.value = false;
    currentId.value = record.id;
    addVisible.value = true;
  };

  /**
   * 添加
   */
  const handleAdd = () => {
    currentId.value = '';
    isCopy.value = false;
    addVisible.value = true;
  };

  /**
   * 查询
   */
  const fetchData = () => {
    if (keyword.value) {
      propsRes.value.data = innerParam.value.filter((item) => item.dataSource.includes(keyword.value));
    } else {
      propsRes.value.data = innerParam.value;
    }
  };

  watch(
    () => innerParam.value,
    (val) => {
      if (val) {
        propsRes.value.data = val;
      }
    },
    {
      deep: true,
      immediate: true,
    }
  );
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
