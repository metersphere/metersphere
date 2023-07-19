<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #action="{ record }">
      <ms-button @click="handleRemove(record)">{{ t('system.userGroup.remove') }}</ms-button>
    </template>
  </MsBaseTable>
</template>

<script lang="ts" setup>
  import { watchEffect } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useUserGroupStore from '@/store/modules/setting/usergroup';
  import { postUserByUserGroup, deleteUserFromUserGroup } from '@/api/modules/setting/usergroup';
  import { UserTableItem } from '@/models/setting/usergroup';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import MsButton from '@/components/pure/ms-button/index.vue';

  const { t } = useI18n();
  const store = useUserGroupStore();

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(postUserByUserGroup, {
    tableKey: TableKeyEnum.USERGROUPUSER,
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
