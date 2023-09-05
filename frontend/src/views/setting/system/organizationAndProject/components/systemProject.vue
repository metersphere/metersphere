<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #name="{ record }">
      <span class="one-line-text">{{ record.name }}</span>
      <a-tooltip background-color="#FFFFFF">
        <template #content>
          <span class="text-[var(--color-text-1)]">{{ t('system.project.revokeDeleteToolTip') }}</span>
          <MsButton class="ml-[8px]" @click="handleRevokeDelete(record)">{{ t('common.revokeDelete') }}</MsButton>
        </template>
        <MsIcon v-if="record.deleted" type="icon-icon_alarm_clock" class="ml-[4px] text-[rgb(var(--danger-6))]" />
      </a-tooltip>
    </template>
    <template #creator="{ record }">
      <MsUserAdminDiv :is-admin="record.projectCreateUserIsAdmin" :name="record.createUser" />
    </template>
    <template #memberCount="{ record }">
      <span class="primary-color" @click="showUserDrawer(record)">{{ record.memberCount }}</span>
    </template>
    <template #operation="{ record }">
      <template v-if="record.deleted">
        <MsButton @click="handleRevokeDelete(record)">{{ t('common.revokeDelete') }}</MsButton>
      </template>
      <template v-else-if="!record.enable">
        <MsButton @click="handleEnableOrDisableProject(record)">{{ t('common.enable') }}</MsButton>
        <MsButton @click="handleDelete(record)">{{ t('common.delete') }}</MsButton>
      </template>
      <template v-else>
        <MsButton @click="showAddProjectModal(record)">{{ t('common.edit') }}</MsButton>
        <MsButton @click="showAddUserModal(record)">{{ t('system.organization.addMember') }}</MsButton>
        <MsButton @click="handleEnableOrDisableProject(record, false)">{{ t('common.end') }}</MsButton>
        <MsTableMoreAction :list="tableActions" @select="handleMoreAction($event, record)"></MsTableMoreAction>
      </template>
    </template>
  </MsBaseTable>
  <AddProjectModal
    :current-project="currentUpdateProject"
    :visible="addProjectVisible"
    @cancel="handleAddProjectModalCancel"
  />
  <AddUserModal
    :project-id="currentProjectId"
    :visible="userVisible"
    @submit="fetchData"
    @cancel="handleAddUserModalCancel"
  />
  <UserDrawer :project-id="currentProjectId" v-bind="currentUserDrawer" @cancel="handleUserDrawerCancel" />
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { useTableStore } from '@/store';
  import { ref, reactive } from 'vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import {
    postProjectTable,
    deleteProject,
    enableOrDisableProject,
    revokeDeleteProject,
    createOrUpdateProject,
  } from '@/api/modules/setting/organizationAndProject';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { Message, TableData } from '@arco-design/web-vue';
  import UserDrawer from './userDrawer.vue';
  import AddUserModal from './addUserModal.vue';
  import useModal from '@/hooks/useModal';
  import { CreateOrUpdateSystemProjectParams, OrgProjectTableItem } from '@/models/setting/system/orgAndProject';
  import AddProjectModal from './addProjectModal.vue';
  import { UserItem } from '@/models/setting/log';
  import MsUserAdminDiv from '@/components/pure/ms-user-admin-div/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  export interface SystemOrganizationProps {
    keyword: string;
  }

  const props = defineProps<SystemOrganizationProps>();

  const { t } = useI18n();
  const tableStore = useTableStore();
  const userVisible = ref(false);
  const addProjectVisible = ref(false);
  const currentProjectId = ref('');
  const currentUpdateProject = ref<CreateOrUpdateSystemProjectParams>();
  const { openDeleteModal, openModal } = useModal();

  const organizationColumns: MsTableColumn = [
    {
      title: 'system.organization.ID',
      dataIndex: 'num',
      showTooltip: true,
    },
    {
      title: 'system.organization.name',
      slotName: 'name',
      dataIndex: 'name',
      editable: true,
      showTooltip: true,
      width: 300,
    },
    {
      title: 'system.organization.member',
      slotName: 'memberCount',
    },
    {
      title: 'system.organization.status',
      dataIndex: 'enable',
      disableTitle: 'common.end',
    },
    {
      title: 'system.organization.description',
      dataIndex: 'description',
      ellipsis: true,
      tooltip: true,
    },
    {
      title: 'system.organization.subordinateOrg',
      dataIndex: 'organizationName',
      sortable: {
        sorter: true,
        sortDirections: ['ascend', 'descend'],
      },
    },
    {
      title: 'system.organization.creator',
      slotName: 'creator',
      dataIndex: 'createUser',
      width: 200,
    },
    {
      title: 'system.organization.createTime',
      dataIndex: 'createTime',
      width: 180,
    },
    {
      title: 'system.organization.operation',
      slotName: 'operation',
      fixed: 'right',
      width: 230,
    },
  ];

  const handleNameChange = async (record: OrgProjectTableItem) => {
    try {
      await createOrUpdateProject(record);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      Message.error(t('common.updateFailed'));
    }
  };

  tableStore.initColumn(TableKeyEnum.SYSTEM_PROJECT, organizationColumns, 'drawer');

  const { propsRes, propsEvent, loadList, setKeyword } = useTable(
    postProjectTable,
    {
      tableKey: TableKeyEnum.SYSTEM_PROJECT,
      selectable: false,
      noDisable: false,
      size: 'default',
      showSetting: true,
      editKey: 'name',
    },
    undefined,
    handleNameChange
  );

  const fetchData = async () => {
    setKeyword(props.keyword);
    await loadList();
  };

  const currentUserDrawer = reactive({
    visible: false,
    organizationId: '',
  });

  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const handleDelete = (record: TableData) => {
    openDeleteModal({
      title: t('system.organization.deleteName', { name: record.name }),
      content: t('system.organization.deleteTip'),
      onBeforeOk: async () => {
        try {
          await deleteProject(record.id);
          Message.success(t('common.deleteSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };

  const handleMoreAction = (tag: ActionsItem, record: TableData) => {
    if (tag.eventTag === 'delete') {
      handleDelete(record);
    }
  };

  const handleEnableOrDisableProject = async (record: any, isEnable = true) => {
    const title = isEnable ? t('system.project.enableTitle') : t('system.project.endTitle');
    const content = isEnable ? t('system.project.enableContent') : t('system.project.endContent');
    const okText = isEnable ? t('common.confirmEnable') : t('common.confirmClose');
    openModal({
      type: 'error',
      cancelText: t('common.cancel'),
      title,
      content,
      okText,
      onBeforeOk: async () => {
        try {
          await enableOrDisableProject(record.id, isEnable);
          Message.success(isEnable ? t('common.enableSuccess') : t('common.closeSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  const showAddProjectModal = (record: any) => {
    const { id, name, description, enable, adminList, organizationId, moduleIds } = record;
    addProjectVisible.value = true;
    currentUpdateProject.value = {
      id,
      name,
      description,
      enable,
      userIds: adminList.map((item: UserItem) => item.id),
      organizationId,
      moduleIds,
    };
  };

  const showAddUserModal = (record: any) => {
    currentProjectId.value = record.id;
    userVisible.value = true;
  };

  const showUserDrawer = (record: TableData) => {
    currentUserDrawer.visible = true;
    currentUserDrawer.organizationId = record.id;
  };

  const handleUserDrawerCancel = () => {
    currentUserDrawer.visible = false;
  };

  const handleAddUserModalCancel = () => {
    userVisible.value = false;
  };
  const handleAddProjectModalCancel = (shouldSearch: boolean) => {
    if (shouldSearch) {
      fetchData();
    }
    addProjectVisible.value = false;
  };

  const handleRevokeDelete = async (record: TableData) => {
    openModal({
      type: 'error',
      cancelText: t('common.cancel'),
      title: t('system.project.revokeDeleteTitle', { name: record.name }),
      content: t('system.project.enableContent'),
      okText: t('common.revokeDelete'),
      onBeforeOk: async () => {
        try {
          await revokeDeleteProject(record.id);
          Message.success(t('common.revokeDeleteSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.error(error);
        }
      },
      hideCancel: false,
    });
  };
  defineExpose({
    fetchData,
  });
</script>

<style lang="scss" scoped>
  .primary-color {
    color: rgb(var(--primary-5));
    cursor: pointer;
  }
</style>
