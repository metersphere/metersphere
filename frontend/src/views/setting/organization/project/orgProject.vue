<template>
  <MsCard simple>
    <div
      class="mb-[16px] flex items-center"
      :class="{ 'justify-between': hasAddPermission, 'justify-end': !hasAddPermission }"
    >
      <a-button v-permission="['ORGANIZATION_PROJECT:READ+ADD']" type="primary" @click="showAddProject">
        {{ t('system.organization.createProject') }}
      </a-button>
      <a-input-search
        v-model="keyword"
        :placeholder="t('system.organization.searchIndexPlaceholder')"
        class="w-[240px]"
        allow-clear
        @press-enter="fetchData"
        @search="fetchData"
        @clear="fetchData"
      ></a-input-search>
    </div>
    <MsBaseTable v-bind="propsRes" :row-class="getRowClass" v-on="propsEvent" @enable-change="enableChange">
      <template #revokeDelete="{ record }">
        <a-tooltip class="ms-tooltip-white">
          <template #content>
            <div class="flex flex-row">
              <span class="text-[var(--color-text-1)]">
                {{ t('system.project.revokeDeleteToolTip', { count: record.remainDayCount }) }}
              </span>
              <MsButton
                v-if="hasAnyPermission(['ORGANIZATION_PROJECT:READ+RECOVER'])"
                class="ml-[8px]"
                @click="handleRevokeDelete(record)"
              >
                {{ t('common.revokeDelete') }}
              </MsButton>
            </div>
          </template>
          <MsIcon
            v-if="record.deleted"
            type="icon-icon_delete_countdown"
            class="ml-[4px] text-[rgb(var(--danger-6))]"
          />
        </a-tooltip>
      </template>
      <template #creator="{ record }">
        <MsUserAdminDiv :is-admin="record.orgCreateUserIsAdmin" :name="record.createUser" />
      </template>
      <template #memberCount="{ record }">
        <span
          v-if="hasAnyPermission(['ORGANIZATION_PROJECT:READ+ADD_MEMBER', 'ORGANIZATION_PROJECT:READ'])"
          class="cursor-pointer text-[rgb(var(--primary-5))]"
          @click="showUserDrawer(record)"
        >
          {{ record.memberCount }}
        </span>
        <span v-else>{{ record.memberCount }}</span>
      </template>
      <template #operation="{ record }">
        <template v-if="record.deleted">
          <MsButton v-permission="['ORGANIZATION_PROJECT:READ+RECOVER']" @click="handleRevokeDelete(record)">
            {{ t('common.revokeDelete') }}
          </MsButton>
        </template>
        <template v-else-if="!record.enable">
          <MsButton v-permission="['ORGANIZATION_PROJECT:READ+DELETE']" @click="handleDelete(record)">
            {{ t('common.delete') }}
          </MsButton>
        </template>
        <template v-else>
          <MsButton v-permission="['ORGANIZATION_PROJECT:READ+UPDATE']" @click="showAddProjectModal(record)">
            {{ t('common.edit') }}
          </MsButton>
          <MsButton v-permission="['ORGANIZATION_PROJECT:READ+ADD_MEMBER']" @click="showAddUserModal(record)">
            {{ t('system.organization.addMember') }}
          </MsButton>
          <MsButton v-permission="['PROJECT_BASE_INFO:READ']" @click="enterProject(record.id)">
            {{ t('system.project.enterProject') }}
          </MsButton>
          <MsTableMoreAction
            v-permission="['ORGANIZATION_PROJECT:READ+DELETE']"
            :list="tableActions"
            @select="handleMoreAction($event, record)"
          ></MsTableMoreAction>
        </template>
      </template>
    </MsBaseTable>
    <AddProjectModal
      v-if="addProjectVisible"
      :visible="addProjectVisible"
      :current-project="currentUpdateProject"
      @cancel="handleAddProjectModalCancel"
    />
    <AddUserModal
      v-model:visible="userVisible"
      is-organization
      :project-id="currentProjectId"
      :organization-id="currentOrgId"
      @submit="fetchData"
    />
    <UserDrawer
      v-bind="currentUserDrawer"
      :organization-id="currentOrgId"
      @request-fetch-data="fetchData"
      @cancel="handleUserDrawerCancel"
    />
  </MsCard>
</template>

<script lang="ts" setup>
  /**
   * @description 系统设置-组织-项目
   */
  import { computed, onMounted, reactive, ref } from 'vue';
  import { Message, TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsUserAdminDiv from '@/components/pure/ms-user-admin-div/index.vue';
  import AddProjectModal from './components/addProjectModal.vue';
  import UserDrawer from './components/userDrawer.vue';
  import AddUserModal from '@/views/setting/system/organizationAndProject/components/addUserModal.vue';

  import {
    deleteProjectByOrg,
    enableOrDisableProjectByOrg,
    postProjectTableByOrg,
    renameProjectByOrg,
    revokeDeleteProjectByOrg,
  } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { UserItem } from '@/models/setting/log';
  import { CreateOrUpdateSystemProjectParams, OrgProjectTableItem } from '@/models/setting/system/orgAndProject';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { enterProject } from '@/views/setting/utils';

  const { t } = useI18n();
  const tableStore = useTableStore();
  const userVisible = ref(false);
  const addProjectVisible = ref(false);
  const currentProjectId = ref('');
  const currentUpdateProject = ref<CreateOrUpdateSystemProjectParams>();
  const { openDeleteModal, openModal } = useModal();
  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);
  const hasAddPermission = computed(() => hasAnyPermission(['ORGANIZATION_PROJECT:READ+ADD']));
  const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'ORGANIZATION_PROJECT:READ+RECOVER',
      'ORGANIZATION_PROJECT:READ+UPDATE',
      'ORGANIZATION_PROJECT:READ+DELETE',
    ])
  );

  const keyword = ref('');
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
      width: 100,
      showTooltip: true,
    },
    {
      title: 'system.organization.name',
      revokeDeletedSlot: 'revokeDelete',
      editType: hasAnyPermission(['ORGANIZATION_PROJECT:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
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
      permission: ['ORGANIZATION_PROJECT:READ+UPDATE'],
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'system.organization.subordinateOrg',
      dataIndex: 'organizationName',
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'system.organization.creator',
      slotName: 'creator',
      width: 200,
      showDrag: true,
      dataIndex: 'createUser',
      showTooltip: false,
    },
    {
      title: 'system.organization.createTime',
      dataIndex: 'createTime',
      width: 180,
      showDrag: true,
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
      await renameProjectByOrg({ id: record.id, name: record.name, organizationId: record.organizationId });
      Message.success(t('common.updateSuccess'));
      return true;
    } catch (error) {
      return false;
    }
  };

  await tableStore.initColumn(TableKeyEnum.ORGANIZATION_PROJECT, organizationColumns, 'drawer');

  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams } = useTable(
    postProjectTableByOrg,
    {
      tableKey: TableKeyEnum.ORGANIZATION_PROJECT,
      selectable: false,
      noDisable: false,
      showSetting: true,
      heightUsed: 300,
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
    currentName: '',
  });

  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const showAddProject = () => {
    addProjectVisible.value = true;
    currentUpdateProject.value = undefined;
  };

  const handleEnableOrDisableProject = async (record: any, isEnable = true) => {
    const title = isEnable ? t('system.project.enableTitle') : t('system.project.endTitle');
    const content = isEnable ? t('system.project.enableContent') : t('system.project.endContent');
    const okText = isEnable ? t('common.confirmStart') : t('common.confirmClose');
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

  function enableChange(record: OrgProjectTableItem, newValue: string | number | boolean) {
    handleEnableOrDisableProject(record, newValue as boolean);
  }

  const showAddProjectModal = (record: OrgProjectTableItem) => {
    const { id, name, description, enable, adminList, organizationId, moduleIds, resourcePoolList, allResourcePool } =
      record;
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
    addProjectVisible.value = true;
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
  };

  const handleAddProjectModalCancel = (shouldSearch: boolean) => {
    addProjectVisible.value = false;
    currentUpdateProject.value = undefined;
    if (shouldSearch) {
      fetchData();
    }
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
  const handleDelete = (record: TableData) => {
    openDeleteModal({
      title: t('system.project.deleteName', { name: characterLimit(record.name) }),
      content: t('system.project.deleteTip'),
      onBeforeOk: async () => {
        try {
          await deleteProjectByOrg(record.id);
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
                        value: ['ORG_PROJECT:READ+RECOVER'],
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

  onMounted(() => {
    setLoadListParams({ organizationId: currentOrgId.value });
    fetchData();
  });
</script>
