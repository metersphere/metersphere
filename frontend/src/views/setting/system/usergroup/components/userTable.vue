<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #action="{ record }">
      <ms-button type="link" @click="handleRemove(record)">{{ t('system.userGroup.remove') }}</ms-button>
    </template>
  </MsBaseTable>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useUserGroupStore from '@/store/modules/setting/usergroup';
  import { watchEffect } from 'vue';
  import { postUserByUserGroup, deleteUserFromUserGroup } from '@/api/modules/setting/usergroup';
  import { UserTableItem } from '@/models/setting/usergroup';

  const { t } = useI18n();
  const store = useUserGroupStore();

  const columns: MsTableColumn = [
    {
      title: 'system.userGroup.name',
      dataIndex: 'name',
    },
    {
      title: 'system.userGroup.email',
      dataIndex: 'email',
    },
    {
      title: 'system.userGroup.phone',
      dataIndex: 'email',
    },
    {
      title: 'system.userGroup.operation',
      slotName: 'action',
      fixed: 'right',
      width: 200,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(postUserByUserGroup, {
    columns,
    scroll: { y: 750, x: 2000 },
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
@/models/setting/usergroup @/api/modules/setting/usergroup
