<template>
  <a-button type="primary" @click="handleAddUser">{{ t('system.userGroup.quickAddUser') }}</a-button>
  <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
    <template #action="{ record }">
      <MsRemoveButton
        :title="t('system.userGroup.removeName', { name: record.name })"
        :sub-title-tip="t('system.userGroup.removeTip')"
        :loading="removeLoading"
        @ok="handleRemove(record)"
      />
    </template>
  </MsBaseTable>
  <AddUserModal :visible="userVisible" @cancel="handleAddUserModalCancel" />
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useUserGroupStore from '@/store/modules/setting/system/usergroup';
  import { watchEffect, ref } from 'vue';
  import { postUserByUserGroup, deleteUserFromUserGroup } from '@/api/modules/setting/usergroup';
  import { UserTableItem } from '@/models/setting/usergroup';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import AddUserModal from './addUserModal.vue';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';

  const { t } = useI18n();
  const store = useUserGroupStore();
  const userVisible = ref(false);
  const removeLoading = ref(false);
  const props = defineProps<{
    keyword: string;
  }>();

  const userGroupUsercolumns: MsTableColumn = [
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

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(postUserByUserGroup, {
    columns: userGroupUsercolumns,
    scroll: { x: '600px' },
    noDisable: true,
  });

  const fetchData = async () => {
    setKeyword(props.keyword);
    await loadList();
  };
  const handleRemove = async (record: UserTableItem) => {
    try {
      removeLoading.value = true;
      await deleteUserFromUserGroup(record.id);
      await fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      removeLoading.value = false;
    }
  };
  const handleAddUser = () => {
    userVisible.value = true;
  };
  const handleAddUserModalCancel = () => {
    fetchData();
    userVisible.value = false;
  };
  watchEffect(() => {
    if (store.currentId) {
      setLoadListParams({ roleId: store.currentId });
      fetchData();
    }
  });
  defineExpose({
    fetchData,
  });
</script>
