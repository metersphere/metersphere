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
  <AddUserModal :current-id="props.current.id" :visible="userVisible" @cancel="handleAddUserModalCancel" />
</template>

<script lang="ts" setup>
  import { computed, inject, ref, watchEffect } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import AddUserModal from './addUserModal.vue';

  import {
    deleteOrgUserFromUserGroup,
    deleteUserFromUserGroup,
    postOrgUserByUserGroup,
    postUserByUserGroup,
  } from '@/api/modules/setting/usergroup';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { CurrentUserGroupItem, UserTableItem } from '@/models/setting/usergroup';
  import { AuthScopeEnum } from '@/enums/commonEnum';

  const systemType = inject<AuthScopeEnum>('systemType');

  const { t } = useI18n();
  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);
  const userVisible = ref(false);
  const props = defineProps<{
    keyword: string;
    current: CurrentUserGroupItem;
  }>();

  const userGroupUsercolumns: MsTableColumn = [
    {
      title: 'system.userGroup.name',
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'system.userGroup.email',
      dataIndex: 'email',
      showTooltip: true,
    },
    {
      title: 'system.userGroup.phone',
      dataIndex: 'phone',
      showTooltip: true,
    },
    {
      title: 'system.userGroup.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 100,
    },
  ];

  const getRequestBySystemType = () => {
    if (systemType === AuthScopeEnum.SYSTEM) {
      return postUserByUserGroup;
    }

    return postOrgUserByUserGroup;
  };

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(getRequestBySystemType(), {
    columns: userGroupUsercolumns,
    scroll: { x: '100%' },
    selectable: false,
    noDisable: true,
    showSetting: false,
    heightUsed: 380,
  });

  const fetchData = async () => {
    setKeyword(props.keyword);
    await loadList();
  };
  const handleRemove = async (record: UserTableItem) => {
    try {
      if (systemType === AuthScopeEnum.SYSTEM) {
        await deleteUserFromUserGroup(record.id);
      } else if (systemType === AuthScopeEnum.ORGANIZATION) {
        await deleteOrgUserFromUserGroup({
          organizationId: currentOrgId.value,
          userRoleId: props.current.id,
          userIds: [record.id],
        });
      }
      // TODO 项目-用户组
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
    if (props.current.id && currentOrgId.value) {
      if (systemType === AuthScopeEnum.SYSTEM) {
        setLoadListParams({ roleId: props.current.id });
      } else if (systemType === AuthScopeEnum.ORGANIZATION) {
        setLoadListParams({ userRoleId: props.current.id, organizationId: currentOrgId.value });
      }
      // TODO 项目-用户组
      fetchData();
    }
  });
  defineExpose({
    fetchData,
  });
</script>
