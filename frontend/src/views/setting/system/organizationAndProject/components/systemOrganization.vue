<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #name="{ record }">
      <span>{{ record.name }}</span>
      <a-tooltip background-color="#FFFFFF">
        <template #content>
          <span class="text-[var(--color-text-1)]">{{ t('system.organization.revokeDeleteToolTip') }}</span>
          <MsButton class="ml-[8px]" @click="handleRevokeDelete(record)">{{ t('common.revokeDelete') }}</MsButton>
        </template>
        <MsIcon v-if="record.deleted" type="icon-icon_alarm_clock" class="ml-[4px] text-[rgb(var(--danger-6))]" />
      </a-tooltip>
    </template>
    <template #creator="{ record }">
      <span>{{ record.createUser }}</span>
      <span v-if="record.orgCreateUserIsAdmin" class="ml-[8px] text-[var(--color-text-4)]">{{
        `(${t('common.admin')})`
      }}</span>
    </template>
    <template #memberCount="{ record }">
      <span class="primary-color" @click="showUserDrawer(record)">{{ record.memberCount }}</span>
    </template>
    <template #projectCount="{ record }">
      <span class="primary-color" @click="showProjectDrawer(record)">{{ record.projectCount }}</span>
    </template>
    <template #operation="{ record }">
      <template v-if="record.deleted">
        <MsButton @click="handleRevokeDelete(record)">{{ t('common.revokeDelete') }}</MsButton>
      </template>
      <template v-else-if="!record.enable">
        <MsButton @click="handleEnableOrDisableOrg(record)">{{ t('common.enable') }}</MsButton>
        <MsButton @click="handleDelete(record)">{{ t('common.delete') }}</MsButton>
      </template>
      <template v-else>
        <MsButton @click="showOrganizationModal(record)">{{ t('common.edit') }}</MsButton>
        <MsButton @click="showAddUserModal(record)">{{ t('system.organization.addMember') }}</MsButton>
        <MsButton @click="handleEnableOrDisableOrg(record, false)">{{ t('common.end') }}</MsButton>
        <MsTableMoreAction :list="tableActions" @select="handleMoreAction($event, record)"></MsTableMoreAction>
      </template>
    </template>
  </MsBaseTable>
  <AddOrganizationModal
    type="edit"
    :current-organization="currentUpdateOrganization"
    :visible="orgVisible"
    @cancel="handleAddOrgModalCancel"
  />
  <AddUserModal
    :organization-id="currentOrganizationId"
    :visible="userVisible"
    @cancel="handleAddUserModalCancel"
    @submit="fetchData"
  />
  <ProjectDrawer v-bind="currentProjectDrawer" @cancel="handleProjectDrawerCancel" />
  <UserDrawer v-bind="currentUserDrawer" @cancel="handleUserDrawerCancel" />
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { useTableStore } from '@/store';
  import { ref, reactive } from 'vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import {
    postOrgTable,
    deleteOrg,
    enableOrDisableOrg,
    revokeDeleteOrg,
  } from '@/api/modules/setting/organizationAndProject';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import AddOrganizationModal from './addOrganizationModal.vue';
  import ProjectDrawer from './projectDrawer.vue';
  import { Message, TableData } from '@arco-design/web-vue';
  import UserDrawer from './userDrawer.vue';
  import AddUserModal from './addUserModal.vue';
  import useModal from '@/hooks/useModal';
  import { CreateOrUpdateSystemOrgParams } from '@/models/setting/system/orgAndProject';

  export interface SystemOrganizationProps {
    keyword: string;
  }

  const props = defineProps<SystemOrganizationProps>();

  const { t } = useI18n();
  const tableStore = useTableStore();
  const userVisible = ref(false);
  const orgVisible = ref(false);
  const currentOrganizationId = ref('');
  const currentUpdateOrganization = ref<CreateOrUpdateSystemOrgParams>();
  const { openDeleteModal, openModal } = useModal();

  const organizationColumns: MsTableColumn = [
    {
      title: 'system.organization.ID',
      dataIndex: 'num',
      width: 100,
    },
    {
      title: 'system.organization.name',
      slotName: 'name',
      editable: true,
    },
    {
      title: 'system.organization.member',
      slotName: 'memberCount',
    },
    {
      title: 'system.organization.project',
      slotName: 'projectCount',
    },
    {
      title: 'system.organization.status',
      dataIndex: 'enable',
    },
    {
      title: 'system.organization.description',
      dataIndex: 'description',
    },
    {
      title: 'system.organization.creator',
      slotName: 'creator',
      dataIndex: 'createUser',
    },
    {
      title: 'system.organization.createTime',
      dataIndex: 'createTime',
      width: 230,
    },
    {
      title: 'system.organization.operation',
      slotName: 'operation',
      fixed: 'right',
      width: 208,
    },
  ];

  tableStore.initColumn(TableKeyEnum.SYSTEM_ORGANIZATION, organizationColumns, 'drawer');

  const { propsRes, propsEvent, loadList, setKeyword, setLoading } = useTable(postOrgTable, {
    tableKey: TableKeyEnum.SYSTEM_ORGANIZATION,
    scroll: { y: 'auto', x: '1300px' },
    selectable: false,
    noDisable: false,
    size: 'default',
    showSetting: true,
  });

  const fetchData = async () => {
    setKeyword(props.keyword);
    await loadList();
  };

  const currentProjectDrawer = reactive({
    visible: false,
    organizationId: '',
    currentName: '',
  });

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
          await deleteOrg(record.id);
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

  const handleEnableOrDisableOrg = async (record: any, isEnable = true) => {
    const title = isEnable ? t('system.organization.enableTitle') : t('system.organization.enableTitle');
    const content = isEnable ? t('system.organization.enableContent') : t('system.organization.endContent');
    const okText = isEnable ? t('common.confirmEnable') : t('common.confirmClose');
    openModal({
      type: 'warning',
      cancelText: t('common.cancel'),
      title,
      content,
      okText,
      onBeforeOk: async () => {
        try {
          await enableOrDisableOrg(record.id, isEnable);
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

  const showOrganizationModal = (record: any) => {
    currentOrganizationId.value = record.id;
    orgVisible.value = true;
    currentUpdateOrganization.value = {
      id: record.id,
      name: record.name,
      description: record.description,
      memberIds: record.orgAdmins.map((item: any) => item.id) || [],
    };
  };

  const showAddUserModal = (record: any) => {
    currentOrganizationId.value = record.id;
    userVisible.value = true;
  };

  const handleProjectDrawerCancel = () => {
    currentProjectDrawer.visible = false;
    fetchData();
  };

  const showProjectDrawer = (record: TableData) => {
    currentProjectDrawer.visible = true;
    currentProjectDrawer.organizationId = record.id;
    currentProjectDrawer.currentName = record.name;
  };

  const showUserDrawer = (record: TableData) => {
    currentUserDrawer.visible = true;
    currentUserDrawer.organizationId = record.id;
  };

  const handleUserDrawerCancel = () => {
    currentUserDrawer.visible = false;
    fetchData();
  };

  const handleAddUserModalCancel = () => {
    userVisible.value = false;
  };
  const handleAddOrgModalCancel = () => {
    orgVisible.value = false;
    fetchData();
  };

  const handleRevokeDelete = async (record: TableData) => {
    try {
      setLoading(true);
      await revokeDeleteOrg(record.id);
      Message.success(t('common.revokeDeleteSuccess'));
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      setLoading(false);
    }
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
@/api/modules/setting/organizationAndProject
