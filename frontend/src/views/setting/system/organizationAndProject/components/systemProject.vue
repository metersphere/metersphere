<template>
  <MsBaseTable v-bind="propsRes" :row-class="getRowClass" v-on="propsEvent" @enable-change="enableChange">
    <template #revokeDelete="{ record }">
      <a-tooltip class="ms-tooltip-white">
        <template #content>
          <div class="flex flex-row">
            <span class="text-[var(--color-text-1)]">{{
              t('system.project.revokeDeleteToolTip', { count: record.remainDayCount })
            }}</span>
            <MsButton
              v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER']"
              class="ml-[8px]"
              @click="handleRevokeDelete(record)"
              >{{ t('common.revokeDelete') }}</MsButton
            >
          </div>
        </template>
        <MsIcon v-if="record.deleted" type="icon-icon_delete_countdown" class="ml-[4px] text-[rgb(var(--danger-6))]" />
      </a-tooltip>
    </template>
    <template #creator="{ record }">
      <MsUserAdminDiv :is-admin="record.projectCreateUserIsAdmin" :name="record.createUser" />
    </template>
    <template #memberCount="{ record }">
      <span
        v-if="hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER', 'SYSTEM_ORGANIZATION_PROJECT:READ'])"
        class="primary-color"
        @click="showUserDrawer(record)"
        >{{ record.memberCount }}</span
      >
      <span v-else>{{ record.memberCount }}</span>
    </template>
    <template #operation="{ record }">
      <template v-if="record.deleted">
        <MsButton v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER']" @click="handleRevokeDelete(record)">{{
          t('common.revokeDelete')
        }}</MsButton>
      </template>
      <template v-else-if="!record.enable">
        <MsButton v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE']" @click="handleDelete(record)">{{
          t('common.delete')
        }}</MsButton>
      </template>
      <template v-else>
        <MsButton v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE']" @click="showAddProjectModal(record)">{{
          t('common.edit')
        }}</MsButton>
        <MsButton v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER']" @click="showAddUserModal(record)">{{
          t('system.organization.addMember')
        }}</MsButton>
        <MsButton v-permission="['PROJECT_BASE_INFO:READ']" @click="enterProject(record.id, record.organizationId)">{{
          t('system.project.enterProject')
        }}</MsButton>
        <MsTableMoreAction
          v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE']"
          :list="tableActions"
          @select="handleMoreAction($event, record)"
        ></MsTableMoreAction>
      </template>
    </template>
  </MsBaseTable>
  <AddProjectModal
    :current-project="currentUpdateProject"
    :visible="addProjectVisible"
    @cancel="handleAddProjectModalCancel"
  />
  <AddUserModal v-model:visible="userVisible" :project-id="currentProjectId" @submit="fetchData" />
  <UserDrawer v-bind="currentUserDrawer" @request-fetch-data="fetchData" @cancel="handleUserDrawerCancel" />
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  import { Message, TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsUserAdminDiv from '@/components/pure/ms-user-admin-div/index.vue';
  import AddProjectModal from './addProjectModal.vue';
  import AddUserModal from './addUserModal.vue';
  import UserDrawer from './userDrawer.vue';

  import {
    deleteProject,
    enableOrDisableProject,
    postProjectTable,
    renameProject,
    revokeDeleteProject,
  } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { UserItem } from '@/models/setting/log';
  import { CreateOrUpdateSystemProjectParams, OrgProjectTableItem } from '@/models/setting/system/orgAndProject';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { enterProject } from '@/views/setting/utils';

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
  const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER',
      'SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE',
      'SYSTEM_ORGANIZATION_PROJECT:READ+DELETE',
    ])
  );
  const operationWidth = computed(() => {
    if (hasOperationPermission.value) {
      return 250;
    }
    if (hasAnyPermission(['PROJECT_BASE_INFO:READ'])) {
      return 100;
    }
    return 50;
  });
  const organizationColumns: MsTableColumn = [
    {
      title: 'system.organization.ID',
      dataIndex: 'num',
      showTooltip: true,
    },
    {
      title: 'system.organization.name',
      dataIndex: 'name',
      revokeDeletedSlot: 'revokeDelete',
      editType: hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
      showTooltip: true,
    },
    {
      title: 'system.organization.member',
      dataIndex: 'memberCount',
      slotName: 'memberCount',
    },
    {
      title: 'system.organization.status',
      dataIndex: 'enable',
      disableTitle: 'common.end',
      permission: ['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE'],
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      showTooltip: true,
    },
    {
      title: 'system.organization.subordinateOrg',
      dataIndex: 'organizationName',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.organization.creator',
      slotName: 'creator',
      dataIndex: 'createUser',
      width: 200,
      showTooltip: false,
    },
    {
      title: 'system.organization.createTime',
      dataIndex: 'createTime',
      width: 180,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: hasOperationPermission.value ? 'system.organization.operation' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: operationWidth.value,
    },
  ];

  const handleNameChange = async (record: OrgProjectTableItem) => {
    try {
      await renameProject({ id: record.id, name: record.name, organizationId: record.organizationId });
      Message.success(t('common.updateSuccess'));
      return true;
    } catch (error) {
      return false;
    }
  };

  const { propsRes, propsEvent, loadList, setKeyword } = useTable(
    postProjectTable,
    {
      tableKey: TableKeyEnum.SYSTEM_PROJECT,
      scroll: { x: '1600px' },
      heightUsed: 286,
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
    await loadList();
  };

  const currentUserDrawer = reactive({
    visible: false,
    projectId: '',
    currentName: '',
  });

  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const handleEnableOrDisableProject = async (record: OrgProjectTableItem, isEnable = true) => {
    const title = isEnable ? t('system.project.enableTitle') : t('system.project.endTitle');
    const content = isEnable ? t('system.project.enableContent') : t('system.project.endContent');
    const okText = isEnable ? t('common.confirmStart') : t('common.confirmClose');
    openModal({
      type: 'info',
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

  function enableChange(record: OrgProjectTableItem, newValue: string | number | boolean) {
    handleEnableOrDisableProject(record, newValue as boolean);
  }

  const showAddProjectModal = (record: OrgProjectTableItem) => {
    const { id, name, description, enable, adminList, organizationId, moduleIds, resourcePoolList, allResourcePool } =
      record;
    addProjectVisible.value = true;
    currentUpdateProject.value = {
      id,
      name,
      description,
      enable,
      userIds: adminList.map((item: UserItem) => item.id),
      organizationId,
      moduleIds,
      resourcePoolIds: resourcePoolList.map((item: { id: string }) => item.id),
      allResourcePool,
    };
  };

  const showAddUserModal = (record: OrgProjectTableItem) => {
    currentProjectId.value = record.id;
    userVisible.value = true;
  };

  const showUserDrawer = (record: TableData) => {
    currentUserDrawer.visible = true;
    currentUserDrawer.projectId = record.id;
    currentUserDrawer.currentName = record.name;
  };

  function getRowClass(record: TableData) {
    return record.id === currentUserDrawer.projectId ? 'selected-row-class' : '';
  }

  const handleUserDrawerCancel = () => {
    currentUserDrawer.visible = false;
    currentUserDrawer.projectId = '';
    currentUserDrawer.currentName = '';
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
      title: t('system.project.revokeDeleteTitle', { name: characterLimit(record.name) }),
      content: '',
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
  const handleDelete = (record: TableData) => {
    openDeleteModal({
      title: t('system.project.deleteName', { name: characterLimit(record.name) }),
      content: t('system.project.deleteTip'),
      onBeforeOk: async () => {
        try {
          await deleteProject(record.id);
          Message.success({
            content: () =>
              h('span', [
                h('span', t('common.deleteSuccess')),
                h(
                  'span',
                  {
                    directives: [
                      {
                        name: 'permission',
                        value: ['SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER'],
                      },
                    ],
                    class: 'ml-[8px] cursor-pointer text-[rgb(var(--primary-5))]',
                    onClick() {
                      handleRevokeDelete(record);
                    },
                  },
                  t('common.revoke')
                ),
              ]),
          });
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };
  const handleMoreAction = (tag: ActionsItem, record: TableData) => {
    const { eventTag } = tag;
    switch (eventTag) {
      case 'delete':
        handleDelete(record);
        break;
      default:
        break;
    }
  };

  defineExpose({
    fetchData,
  });

  watchEffect(() => {
    setKeyword(props.keyword);
  });

  onMounted(() => {
    fetchData();
  });

  await tableStore.initColumn(TableKeyEnum.SYSTEM_PROJECT, organizationColumns, 'drawer');
</script>

<style lang="less" scoped>
  .primary-color {
    color: rgb(var(--primary-5));
    cursor: pointer;
  }
</style>
