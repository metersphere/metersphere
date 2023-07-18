<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #action="{ record }">
      <ms-button @click="handleRemove(record)">{{ t('system.userGroup.remove') }}</ms-button>
    </template>
  </MsBaseTable>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useUserGroupStore from '@/store/modules/system/usergroup';
  import { watchEffect } from 'vue';
  import { postUserByUserGroup, deleteUserFromUserGroup } from '@/api/modules/system/usergroup';
  import { UserTableItem } from '@/models/system/usergroup';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import MsButton from '@/components/pure/ms-button/index.vue';

  const { t } = useI18n();
  const store = useUserGroupStore();

  const columns: MsTableColumn = [
    {
      title: 'system.userGroup.name',
      dataIndex: 'name',
      showDrag: false,
      priority: 1,
      showInTable: true,
    },
    {
      title: 'system.userGroup.email',
      dataIndex: 'email',
      showDrag: false,
      priority: 1,
      showInTable: true,
    },
    {
      title: 'system.userGroup.phone',
      dataIndex: 'email',
      showDrag: true,
      priority: 1,
      showInTable: true,
    },
    {
      title: 'system.userGroup.operation',
      slotName: 'action',
      fixed: 'right',
      width: 200,
      showDrag: true,
      priority: 1,
      showInTable: true,
      showSetting: true,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(postUserByUserGroup, {
    tableKey: TableKeyEnum.USERGROUPUSER,
    columns,
    scroll: { y: 750, x: '600px' },
    selectable: true,
  });
  const fetchData = async () => {
    await loadList();
  };
  const handleRemove = async (record: UserTableItem) => {
    await deleteUserFromUserGroup(record.id);
    await fetchData();
  };
  watchEffect(() => {
    if (store.currentId) {
      setLoadListParams({ roleId: store.currentId });
      fetchData();
    }
  });
</script>
