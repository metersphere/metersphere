<template>
  <a-button type="primary" @click="handleAddUser">{{ t('system.userGroup.quickAddUser') }}</a-button>
  <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
    <template #action="{ record }">
      <ms-button @click="handleRemove(record)">{{ t('system.userGroup.remove') }}</ms-button>
    </template>
  </MsBaseTable>
  <AddUserModal :visible="userVisible" @cancel="handleAddUserModalCancel" />
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { useUserGroupStore, useTableStore } from '@/store';
  import { watchEffect, ref } from 'vue';
  import { postUserByUserGroup, deleteUserFromUserGroup } from '@/api/modules/setting/usergroup';
  import { UserTableItem } from '@/models/setting/usergroup';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import AddUserModal from './addUserModal.vue';

  const { t } = useI18n();
  const store = useUserGroupStore();
  const tableStore = useTableStore();
  const userVisible = ref(false);

  const userGroupUsercolumns: MsTableColumn = [
    {
      title: 'system.userGroup.name',
      dataIndex: 'name',
      showDrag: false,
      showInTable: true,
      editable: true,
    },
    {
      title: 'system.userGroup.email',
      dataIndex: 'email',
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'system.userGroup.phone',
      dataIndex: 'email',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.userGroup.operation',
      slotName: 'action',
      fixed: 'right',
      width: 200,
      showDrag: true,
      showInTable: true,
    },
  ];

  tableStore.initColumn(TableKeyEnum.USERGROUPUSER, userGroupUsercolumns, 'drawer');

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(postUserByUserGroup, {
    tableKey: TableKeyEnum.USERGROUPUSER,
    scroll: { y: 750, x: '600px' },
    selectable: true,
  });

  const fetchData = async () => {
    await loadList();
  };
  const handleRemove = async (record: UserTableItem) => {
    try {
      await deleteUserFromUserGroup(record.id);
      await fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };
  const handleAddUser = () => {
    userVisible.value = true;
  };
  const handleAddUserModalCancel = () => {
    userVisible.value = false;
  };
  watchEffect(() => {
    if (store.currentId) {
      setLoadListParams({ roleId: store.currentId });
      fetchData();
    }
  });
</script>
