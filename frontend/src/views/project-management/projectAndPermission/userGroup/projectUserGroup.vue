<template>
  <div class="flex flex-row items-center justify-between">
    <a-button type="primary" @click="addUserGroup">{{ t('project.userGroup.add') }}</a-button>
    <a-input-search
      v-model="keyword"
      :placeholder="t('project.userGroup.searchUser')"
      class="w-[240px]"
      allow-clear
      @press-enter="fetchData"
      @search="fetchData"
    ></a-input-search>
  </div>
  <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
    <template #name="{ record }">
      <div class="flex flex-row items-center gap-[4px]">
        <div class="one-text-line">{{ record.name }}</div>
        <div class="ml-1 text-[var(--color-text-4)]">{{
          `(${record.internal ? t('common.internal') : t('common.custom')})`
        }}</div>
      </div>
    </template>
    <template #memberCount="{ record }">
      <span class="cursor-pointer text-[rgb(var(--primary-5))]" @click="showUserDrawer(record)">{{
        record.memberCount
      }}</span>
    </template>
    <template #operation="{ record }">
      <MsButton @click="showAuthDrawer(record)">{{ t('project.userGroup.viewAuth') }}</MsButton>
      <a-divider />
      <MsButton @click="handleDelete(record)">{{ t('common.delete') }}</MsButton>
    </template>
  </MsBaseTable>
  <a-drawer
    :width="680"
    :visible="authVisible"
    unmount-on-close
    :footer="false"
    :title="t('system.organization.addMember')"
    :mask="false"
    class="ms-drawer-no-mask"
    @cancel="authVisible = false"
  >
    <AuthTable :current="currentItem" />
  </a-drawer>
  <UserDrawer
    :visible="userVisible"
    :rganization-id="appStore.currentOrgId"
    :project-id="appStore.currentProjectId"
    :user-group-id="currentItem.id"
    @cancel="userVisible = false"
  />
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import AuthTable from '@/components/business/ms-user-group-comp/authTable.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import { useTableStore, useAppStore } from '@/store';
  import { UserGroupItem, CurrentUserGroupItem } from '@/models/setting/usergroup';
  import { AuthScopeEnum } from '@/enums/commonEnum';
  import { postUserGroupList, deleteUserGroup } from '@/api/modules/project-management/usergroup';
  import UserDrawer from './userDrawer.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import useModal from '@/hooks/useModal';
  import { Message } from '@arco-design/web-vue';

  const { openDeleteModal } = useModal();
  const { t } = useI18n();
  const appStore = useAppStore();
  const tableStore = useTableStore();

  const keyword = ref('');
  const currentItem = ref<CurrentUserGroupItem>({
    id: '',
    name: '',
    type: AuthScopeEnum.PROJECT,
    internal: true,
  });
  const authVisible = ref(false);
  const userVisible = ref(false);
  const addUserGroupVisible = ref(false);

  const userGroupUsercolumns: MsTableColumn = [
    {
      title: 'project.userGroup.name',
      dataIndex: 'name',
      slotName: 'name',
      showTooltip: true,
      editType: ColumnEditTypeEnum.INPUT,
    },
    {
      title: 'project.userGroup.memberCount',
      slotName: 'memberCount',
      showDrag: true,
      dataIndex: 'memberCount',
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 100,
    },
  ];
  tableStore.initColumn(TableKeyEnum.PROJECT_USER_GROUP, userGroupUsercolumns, 'drawer');
  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(postUserGroupList, {
    tableKey: TableKeyEnum.PROJECT_USER_GROUP,
    selectable: false,
    noDisable: false,
  });

  const fetchData = async () => {
    setKeyword(keyword.value);
    await loadList();
  };

  const addUserGroup = () => {
    addUserGroupVisible.value = true;
  };

  const showAuthDrawer = (record: UserGroupItem) => {
    currentItem.value = record;
    authVisible.value = true;
  };

  const showUserDrawer = (record: UserGroupItem) => {
    currentItem.value = record;
    userVisible.value = true;
  };

  const handleDelete = (record: UserGroupItem) => {
    openDeleteModal({
      title: t('system.organization.deleteName', { name: record.name }),
      content: t('system.organization.deleteTip'),
      onBeforeOk: async () => {
        try {
          await deleteUserGroup(record.id);
          Message.success(t('common.deleteSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };
  onMounted(() => {
    setLoadListParams({ projectId: appStore.currentProjectId });
    fetchData();
  });
</script>

<style scoped></style>
