<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #memberCount="{ record }">
      <span class="primary-color" @click="showUserDrawer(record)">{{ record.memberCount }}</span>
    </template>
    <template #projectCount="{ record }">
      <span class="primary-color" @click="showProjectDrawer(record)">{{ record.projectCount }}</span>
    </template>
    <template #operation="{ record }">
      <template v-if="!record.enable">
        <MsButton @click="handleEnable(record)">{{ t('system.organization.enable') }}</MsButton>
        <MsButton @click="handleDelete(record)">{{ t('system.organization.delete') }}</MsButton>
      </template>
      <template v-else>
        <MsButton @click="showOrganizationModal(record)">{{ t('system.organization.edit') }}</MsButton>
        <MsButton @click="showAddUserModal(record)">{{ t('system.organization.addMember') }}</MsButton>
        <MsButton @click="handleEnd(record)">{{ t('system.organization.end') }}</MsButton>
        <MsTableMoreAction :list="tableActions" @select="handleMoreAction($event, record)"></MsTableMoreAction>
      </template>
    </template>
  </MsBaseTable>
  <AddOrganizationModal :visible="userVisible" @cancel="handleAddUserModalCancel" />
  <ProjectDrawer v-bind="currentProjectDrawer" @cancel="handleProjectDrawerCancel" />
  <UserDrawer v-bind="currentUserDrawer" @cancel="handleUserDrawerCancel" />
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { useTableStore } from '@/store';
  import { ref, reactive, watchEffect } from 'vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { postOrgTable } from '@/api/modules/setting/system/organizationAndProject';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import AddOrganizationModal from './addOrganizationModal.vue';
  import ProjectDrawer from './projectDrawer.vue';
  import { TableData } from '@arco-design/web-vue';
  import UserDrawer from './userDrawer.vue';

  export interface SystemOrganizationProps {
    keyword: string;
  }

  const props = defineProps<SystemOrganizationProps>();

  const { t } = useI18n();
  const tableStore = useTableStore();
  const userVisible = ref(false);

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

  const handleDelete = (record: any) => {
    // eslint-disable-next-line no-console
    console.log(record);
  };

  const handleMoreAction = (tag: string, record: any) => {
    if (tag === 'delete') {
      handleDelete(record);
    }
  };

  const handleEnable = (record: any) => {
    // eslint-disable-next-line no-console
    console.log(record);
  };

  const handleEnd = (record: any) => {
    // eslint-disable-next-line no-console
    console.log(record);
  };

  const showOrganizationModal = (record: any) => {
    // eslint-disable-next-line no-console
    console.log(record);
  };

  const showAddUserModal = (record: any) => {
    // eslint-disable-next-line no-console
    console.log(record);
    userVisible.value = true;
  };

  const handleProjectDrawerCancel = () => {
    currentProjectDrawer.visible = false;
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
  };

  const organizationColumns: MsTableColumn = [
    {
      title: 'system.organization.ID',
      dataIndex: 'num',
      width: 100,
      ellipsis: true,
    },
    {
      title: 'system.organization.name',
      dataIndex: 'name',
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

  const { propsRes, propsEvent, loadList, setKeyword } = useTable(postOrgTable, {
    tableKey: TableKeyEnum.SYSTEM_ORGANIZATION,
    scroll: { y: 'auto', x: '1300px' },
    selectable: false,
    noDisable: false,
    size: 'default',
  });

  const fetchData = async () => {
    await loadList();
  };

  const handleAddUserModalCancel = () => {
    userVisible.value = false;
  };
  watchEffect(() => {
    setKeyword(props.keyword);
    fetchData();
  });
</script>

<style lang="scss" scoped>
  .primary-color {
    color: rgb(var(--primary-5));
    cursor: pointer;
  }
</style>
