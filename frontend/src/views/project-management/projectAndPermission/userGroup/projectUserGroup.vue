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
        <div class="one-line-text">{{ record.name }}</div>
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
      <div class="flex flex-row flex-nowrap">
        <MsButton class="!mr-0" @click="showAuthDrawer(record)">{{ t('project.userGroup.viewAuth') }}</MsButton>
        <a-divider v-if="!record.internal" direction="vertical" />
        <MsButton v-if="!record.internal" class="!mr-0" status="danger" @click="handleDelete(record)">{{
          t('common.delete')
        }}</MsButton>
      </div>
    </template>
  </MsBaseTable>
  <MsDrawer
    :width="928"
    :visible="authVisible"
    unmount-on-close
    :footer="!currentItem.internal"
    :title="currentItem.name"
    :mask="false"
    @cancel="authVisible = false"
  >
    <AuthTable ref="authRef" :current="currentItem" />
    <template #footer>
      <div class="flex items-center justify-between">
        <ms-button class="btn" :disabled="!canSave" @click="handleAuthReset">{{
          t('system.userGroup.reset')
        }}</ms-button>
        <a-button class="btn" :disabled="!canSave" type="primary" @click="handleAuthSave">{{
          t('common.save')
        }}</a-button>
      </div>
    </template>
  </MsDrawer>
  <UserDrawer
    :visible="userVisible"
    :project-id="currentProjectId"
    :user-role-id="currentItem.id"
    @request-fetch-data="fetchData"
    @cancel="userVisible = false"
  />
  <a-modal
    v-model:visible="addUserGroupVisible"
    :ok-text="t('common.create')"
    title-align="start"
    class="ms-modal-form ms-modal-small"
  >
    <template #title> {{ t('project.userGroup.addUserGroup') }} </template>
    <div class="form">
      <a-form ref="addUserGroupFormRef" :model="form" layout="vertical">
        <a-form-item
          field="name"
          :label="t('project.userGroup.name')"
          :rules="[{ required: true, message: t('project.userGroup.addRequired') }]"
          asterisk-position="end"
        >
          <a-input v-model="form.name" />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :loading="addUserGroupLoading" @click="handleAddUGCancel(false)">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="addUserGroupLoading" @click="handleCreateUserGroup">
        {{ t('common.add') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限-用户组
   */
  import { ref, onMounted, computed, provide } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import AuthTable from '@/components/business/ms-user-group-comp/authTable.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import { useAppStore } from '@/store';
  import { UserGroupItem, CurrentUserGroupItem } from '@/models/setting/usergroup';
  import { AuthScopeEnum } from '@/enums/commonEnum';
  import {
    postUserGroupList,
    deleteUserGroup,
    updateOrAddProjectUserGroup,
  } from '@/api/modules/project-management/usergroup';
  import UserDrawer from './userDrawer.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import useModal from '@/hooks/useModal';
  import { Message, ValidatedError } from '@arco-design/web-vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  provide('systemType', AuthScopeEnum.PROJECT);

  const { openDeleteModal } = useModal();
  const { t } = useI18n();
  const appStore = useAppStore();
  const keyword = ref('');
  const currentProjectId = computed(() => appStore.currentProjectId);
  const form = ref({
    name: '',
  });
  const addUserGroupFormRef = ref();
  const addUserGroupLoading = ref(false);
  const currentItem = ref<CurrentUserGroupItem>({
    id: '',
    name: '',
    type: AuthScopeEnum.PROJECT,
    internal: true,
    scopeId: '',
  });
  const authVisible = ref(false);
  const userVisible = ref(false);
  const addUserGroupVisible = ref(false);
  const authRef = ref();

  const canSave = computed(() => {
    return authRef.value?.canSave;
  });

  const userGroupcolumns: MsTableColumn = [
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
      width: 150,
    },
  ];

  const handleNameChange = async (record: UserGroupItem) => {
    try {
      await updateOrAddProjectUserGroup(record);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(
    postUserGroupList,
    {
      tableKey: TableKeyEnum.PROJECT_USER_GROUP,
      columns: userGroupcolumns,
      selectable: false,
      scroll: { x: '100%' },
      noDisable: true,
    },
    undefined,
    (record) => handleNameChange(record)
  );

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
    const { id, internal, name, scopeId, type } = record;
    currentItem.value = { id, internal, name, scopeId, type };
    userVisible.value = true;
  };

  const handleAuthReset = () => {
    authRef.value?.handleReset();
  };

  const handleAuthSave = () => {
    authRef.value?.handleSave();
  };

  const handleDelete = (record: UserGroupItem) => {
    openDeleteModal({
      title: t('project.userGroup.deleteName', { name: record.name }),
      content: t('project.userGroup.deleteTip'),
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
  const handleAddUGCancel = (shouldSearch: boolean) => {
    addUserGroupFormRef.value?.resetFields();
    addUserGroupVisible.value = false;
    if (shouldSearch) {
      fetchData();
    }
  };

  const handleCreateUserGroup = () => {
    addUserGroupFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        addUserGroupLoading.value = true;
        await updateOrAddProjectUserGroup({
          name: form.value.name,
          scopeId: appStore.currentProjectId,
        });
        Message.success(t('common.createSuccess'));
        handleAddUGCancel(true);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        addUserGroupLoading.value = false;
      }
    });
  };

  onMounted(() => {
    setLoadListParams({ projectId: currentProjectId.value });
    fetchData();
  });
</script>

<style scoped></style>
