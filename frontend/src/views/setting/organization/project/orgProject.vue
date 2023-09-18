<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <a-button type="primary" @click="showAddProject">{{ t('system.organization.createProject') }}</a-button>
      <a-input-search
        v-model="keyword"
        :placeholder="t('system.user.searchUser')"
        class="w-[240px]"
        allow-clear
        @press-enter="fetchData"
        @search="fetchData"
      ></a-input-search>
    </div>
    <MsBaseTable v-bind="propsRes" v-on="propsEvent">
      <template #revokeDelete="{ record }">
        <a-tooltip background-color="#FFFFFF">
          <template #content>
            <span class="text-[var(--color-text-1)]">{{ t('system.project.revokeDeleteToolTip') }}</span>
            <MsButton class="ml-[8px]" @click="handleRevokeDelete(record)">{{ t('common.revokeDelete') }}</MsButton>
          </template>
          <MsIcon v-if="record.deleted" type="icon-icon_alarm_clock" class="ml-[4px] text-[rgb(var(--danger-6))]" />
        </a-tooltip>
      </template>
      <template #creator="{ record }">
        <MsUserAdminDiv :is-admin="record.orgCreateUserIsAdmin" :name="record.createUser" />
      </template>
      <template #memberCount="{ record }">
        <span class="cursor-pointer text-[rgb(var(--primary-5))]" @click="showUserDrawer(record)">{{
          record.memberCount
        }}</span>
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
      :visible="addProjectVisible"
      :current-project="currentUpdateProject"
      @cancel="handleAddProjectModalCancel"
    />
    <AddUserModal :project-id="currentProjectId" :visible="userVisible" @cancel="handleAddUserModalCancel" />
    <UserDrawer v-bind="currentUserDrawer" @request-fetch-data="fetchData" @cancel="handleUserDrawerCancel" />
  </MsCard>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { useTableStore, useAppStore } from '@/store';
  import { ref, reactive, onMounted, computed } from 'vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import {
    postProjectTableByOrg,
    deleteProjectByOrg,
    enableOrDisableProjectByOrg,
    revokeDeleteProjectByOrg,
    createOrUpdateProjectByOrg,
  } from '@/api/modules/setting/organizationAndProject';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { Message, TableData } from '@arco-design/web-vue';
  import UserDrawer from './components/userDrawer.vue';
  import AddUserModal from './components/addUserModal.vue';
  import useModal from '@/hooks/useModal';
  import { CreateOrUpdateSystemProjectParams, OrgProjectTableItem } from '@/models/setting/system/orgAndProject';
  import AddProjectModal from './components/addProjectModal.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import { UserItem } from '@/models/setting/log';
  import MsUserAdminDiv from '@/components/pure/ms-user-admin-div/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  const { t } = useI18n();
  const tableStore = useTableStore();
  const userVisible = ref(false);
  const addProjectVisible = ref(false);
  const currentProjectId = ref('');
  const currentUpdateProject = ref<CreateOrUpdateSystemProjectParams>();
  const { openDeleteModal, openModal } = useModal();
  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);

  const keyword = ref('');

  const organizationColumns: MsTableColumn = [
    {
      title: 'system.organization.ID',
      dataIndex: 'num',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'system.organization.name',
      revokeDeletedSlot: 'revokeDelete',
      editType: ColumnEditTypeEnum.INPUT,
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'system.organization.member',
      slotName: 'memberCount',
      showDrag: true,
      dataIndex: 'memberCount',
    },
    {
      title: 'system.organization.status',
      dataIndex: 'enable',
      disableTitle: 'common.end',
      showDrag: true,
    },
    {
      title: 'system.organization.description',
      dataIndex: 'description',
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'system.organization.subordinateOrg',
      dataIndex: 'organizationName',
      showDrag: true,
    },
    {
      title: 'system.organization.creator',
      slotName: 'creator',
      width: 180,
      showDrag: true,
      dataIndex: 'createUser',
    },
    {
      title: 'system.organization.createTime',
      dataIndex: 'createTime',
      width: 180,
      showDrag: true,
    },
    {
      title: 'system.organization.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 230,
    },
  ];

  const handleNameChange = async (record: OrgProjectTableItem) => {
    try {
      await createOrUpdateProjectByOrg(record);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      Message.error(t('common.updateFailed'));
    }
  };

  tableStore.initColumn(TableKeyEnum.ORGANIZATION_PROJECT, organizationColumns, 'drawer');

  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams } = useTable(
    postProjectTableByOrg,
    {
      tableKey: TableKeyEnum.ORGANIZATION_PROJECT,
      selectable: false,
      noDisable: false,
      size: 'default',
      showSetting: true,
    },
    undefined,
    (record) => handleNameChange(record)
  );

  const fetchData = async () => {
    setKeyword(keyword.value);
    await loadList();
  };

  const currentUserDrawer = reactive({
    visible: false,
    projectId: '',
  });

  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const showAddProject = () => {
    currentUpdateProject.value = undefined;
    addProjectVisible.value = true;
  };

  const handleDelete = (record: TableData) => {
    openDeleteModal({
      title: t('system.organization.deleteName', { name: record.name }),
      content: t('system.organization.deleteTip'),
      onBeforeOk: async () => {
        try {
          await deleteProjectByOrg(record.id);
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
          await enableOrDisableProjectByOrg(record.id, isEnable);
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
    currentUpdateProject.value = {
      id,
      name,
      description,
      enable,
      userIds: adminList.map((item: UserItem) => item.id),
      organizationId,
      moduleIds,
    };
    addProjectVisible.value = true;
  };

  const showAddUserModal = (record: any) => {
    currentProjectId.value = record.id;
    userVisible.value = true;
  };

  const showUserDrawer = (record: TableData) => {
    currentUserDrawer.visible = true;
    currentUserDrawer.projectId = record.id;
  };

  const handleUserDrawerCancel = () => {
    currentUserDrawer.visible = false;
  };

  const handleAddUserModalCancel = (shouldSearch: boolean) => {
    userVisible.value = false;
    if (shouldSearch) {
      fetchData();
    }
  };
  const handleAddProjectModalCancel = (shouldSearch: boolean) => {
    addProjectVisible.value = false;
    if (shouldSearch) {
      fetchData();
    }
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
          await revokeDeleteProjectByOrg(record.id);
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

  onMounted(() => {
    setLoadListParams({ organizationId: currentOrgId.value });
    fetchData();
  });
</script>
