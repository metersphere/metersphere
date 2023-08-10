<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #operation="{ record }">
      <template v-if="!record.enable">
        <MsButton @click="handleEnable(record)">{{ t('system.organization.enable') }}</MsButton>
        <MsButton @click="handleDelete(record)">{{ t('system.organization.delete') }}</MsButton>
      </template>
      <template v-else>
        <MsButton @click="showOrganizationModal(record)">{{ t('system.organization.edit') }}</MsButton>
        <MsButton @click="showAddUserModal(record)">{{ t('system.organization.addUser') }}</MsButton>
        <MsButton @click="handleEnd(record)">{{ t('system.organization.end') }}</MsButton>
        <MsTableMoreAction :list="tableActions" @select="handleMoreAction($event, record)"></MsTableMoreAction>
      </template>
    </template>
  </MsBaseTable>
  <AddOrganizationModal :visible="userVisible" @cancel="handleAddUserModalCancel" />
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { useTableStore } from '@/store';
  import { watchEffect, ref } from 'vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { postUserByUserGroup } from '@/api/modules/setting/usergroup';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import AddOrganizationModal from './addOrganizationModal.vue';

  const { t } = useI18n();
  const tableStore = useTableStore();
  const userVisible = ref(false);

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

  const projectColumn: MsTableColumn = [
    {
      title: 'system.organization.ID',
      dataIndex: 'id',
    },
    {
      title: 'system.organization.name',
      dataIndex: 'name',
    },
    {
      title: 'system.organization.member',
      dataIndex: 'name',
    },
    {
      title: 'system.organization.project',
      dataIndex: 'project',
    },
    {
      title: 'system.organization.enabled',
      dataIndex: 'enabled',
    },
    {
      title: 'system.organization.description',
      dataIndex: 'description',
    },
    {
      title: 'system.organization.createUser',
      dataIndex: 'createUser',
    },
    {
      title: 'system.organization.createTime',
      dataIndex: 'createTime',
    },
    {
      title: 'system.organization.operation',
      slotName: 'operation',
      fixed: 'right',
      width: 200,
    },
  ];

  tableStore.initColumn(TableKeyEnum.SYSTEM_PROJECT, projectColumn, 'drawer');

  const { propsRes, propsEvent, loadList } = useTable(postUserByUserGroup, {
    tableKey: TableKeyEnum.SYSTEM_PROJECT,
    scroll: { y: 'auto', x: '600px' },
    selectable: false,
    noDisable: false,
  });

  const fetchData = async () => {
    await loadList();
  };

  const handleAddUserModalCancel = () => {
    userVisible.value = false;
  };
  watchEffect(() => {
    fetchData();
  });
</script>
