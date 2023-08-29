<template>
  <a-button type="primary" @click="handleAddUser">{{ t('system.userGroup.quickAddUser') }}</a-button>
  <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
    <template #action="{ record }">
      <MsRemoveButton
        :title="t('system.userGroup.removeName', { name: record.name })"
        :sub-title-tip="t('system.userGroup.removeTip')"
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
  import { useAppStore } from '@/store';
  import useUserGroupStore from '@/store/modules/setting/organization/usergroup';
  import { watchEffect, ref, computed } from 'vue';
  import { postOrgUserByUserGroup, deleteOrgUserFromUserGroup } from '@/api/modules/setting/usergroup';
  import { UserTableItem } from '@/models/setting/usergroup';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import AddUserModal from './addUserModal.vue';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';

  const { t } = useI18n();
  const store = useUserGroupStore();
  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);
  const userVisible = ref(false);
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

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(postOrgUserByUserGroup, {
    columns: userGroupUsercolumns,
    scroll: { y: 'auto', x: '600px' },
    selectable: false,
    noDisable: false,
    size: 'default',
    showSetting: false,
  });

  const fetchData = async () => {
    setKeyword(props.keyword);
    await loadList();
  };
  const handleRemove = async (record: UserTableItem) => {
    try {
      await deleteOrgUserFromUserGroup({
        organizationId: currentOrgId.value,
        userRoleId: store.currentId,
        userIds: [record.id],
      });
      await fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };
  const handleAddUser = () => {
    userVisible.value = true;
  };
  const handleAddUserModalCancel = (shouldSearch: boolean) => {
    if (shouldSearch) {
      fetchData();
    }
    userVisible.value = false;
  };
  watchEffect(() => {
    if (store.currentId && currentOrgId.value) {
      setLoadListParams({ userRoleId: store.currentId, organizationId: currentOrgId.value });
      fetchData();
    }
  });
  defineExpose({
    fetchData,
  });
</script>
