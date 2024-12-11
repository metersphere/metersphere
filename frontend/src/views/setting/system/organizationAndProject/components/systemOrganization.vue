<template>
  <MsBaseTable v-bind="propsRes" :row-class="getRowClass" v-on="propsEvent" @enable-change="enableChange">
    <template #revokeDelete="{ record }">
      <a-tooltip class="ms-tooltip-white">
        <template #content>
          <div class="flex flex-row">
            <span class="text-[var(--color-text-1)]">{{
              t('system.organization.revokeDeleteToolTip', { count: record.remainDayCount })
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
    <template #projectCount="{ record }">
      <span
        v-if="hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE'])"
        class="primary-color"
        @click="showProjectDrawer(record)"
      >
        {{ record.projectCount }}
      </span>
      <span v-else>{{ record.projectCount }}</span>
    </template>
    <template #operation="{ record }">
      <template v-if="record.deleted">
        <MsButton v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER']" @click="handleRevokeDelete(record)">
          {{ t('common.revokeDelete') }}
        </MsButton>
      </template>
      <template v-else-if="!record.enable">
        <MsButton v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE']" @click="handleDelete(record)">
          {{ t('common.delete') }}
        </MsButton>
      </template>
      <template v-else>
        <MsButton v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE']" @click="showOrganizationModal(record)">
          {{ t('common.edit') }}
        </MsButton>
        <MsButton v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER']" @click="showAddUserModal(record)">
          {{ t('system.organization.addMember') }}
        </MsButton>
        <MsButton
          v-if="record.switchAndEnter && licenseStore.hasLicense()"
          :disabled="appStore.currentOrgId === record.id"
          @click="enterOrganization(record.id)"
        >
          {{ t('system.project.enterOrganization') }}
        </MsButton>
        <MsTableMoreAction
          v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE']"
          :list="tableActions"
          @select="handleMoreAction($event, record)"
        />
      </template>
    </template>
  </MsBaseTable>
  <AddOrganizationModal
    type="edit"
    :current-organization="currentUpdateOrganization"
    :visible="orgVisible"
    @cancel="handleAddOrgModalCancel"
  />
  <AddUserModal v-model:visible="userVisible" :organization-id="currentOrganizationId" @submit="fetchData" />
  <ProjectDrawer v-bind="currentProjectDrawer" @cancel="handleProjectDrawerCancel" />
  <UserDrawer v-bind="currentUserDrawer" @request-fetch-data="fetchData" @cancel="handleUserDrawerCancel" />
</template>

<script lang="ts" setup async>
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
  import AddOrganizationModal from './addOrganizationModal.vue';
  import AddUserModal from './addUserModal.vue';
  import ProjectDrawer from './projectDrawer.vue';
  import UserDrawer from './userDrawer.vue';

  import {
    deleteOrg,
    enableOrDisableOrg,
    modifyOrgName,
    postOrgTable,
    revokeDeleteOrg,
  } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useTableStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import useLicenseStore from '@/store/modules/setting/license';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { CreateOrUpdateSystemOrgParams, OrgProjectTableItem } from '@/models/setting/system/orgAndProject';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { enterOrganization } from '@/views/setting/utils';

  const licenseStore = useLicenseStore();

  const appStore = useAppStore();

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
    if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ'])) {
      return 100;
    }
  });

  const organizationColumns: MsTableColumn = [
    {
      title: 'system.organization.ID',
      dataIndex: 'num',
      width: 100,
    },
    {
      title: 'system.organization.name',
      dataIndex: 'name',
      width: 300,
      revokeDeletedSlot: 'revokeDelete',
      editType: hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
      showTooltip: true,
    },
    {
      title: 'system.organization.member',
      slotName: 'memberCount',
      dataIndex: 'memberCount',
    },
    {
      title: 'system.organization.project',
      slotName: 'projectCount',
      dataIndex: 'projectCount',
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
      await modifyOrgName({ id: record.id, name: record.name });
      Message.success(t('common.updateSuccess'));
      return true;
    } catch (error) {
      return false;
    }
  };

  const { propsRes, propsEvent, loadList, setKeyword, setLoading } = useTable(
    postOrgTable,
    {
      tableKey: TableKeyEnum.SYSTEM_ORGANIZATION,
      selectable: false,
      noDisable: false,
      size: 'default',
      showSetting: true,
      heightUsed: 286,
    },
    undefined,
    handleNameChange
  );

  const fetchData = async () => {
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
    currentName: '',
  });

  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const handleEnableOrDisableOrg = async (record: OrgProjectTableItem, isEnable = true) => {
    const title = isEnable ? t('system.organization.enableTitle') : t('system.organization.endTitle');
    const content = isEnable ? t('system.organization.enableContent') : t('system.organization.endContent');
    const okText = isEnable ? t('common.confirmStart') : t('common.confirmClose');
    openModal({
      type: 'info',
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

  function enableChange(record: OrgProjectTableItem, newValue: string | number | boolean) {
    handleEnableOrDisableOrg(record, newValue as boolean);
  }

  const showOrganizationModal = (record: OrgProjectTableItem) => {
    currentOrganizationId.value = record.id;
    orgVisible.value = true;
    currentUpdateOrganization.value = {
      id: record.id,
      name: record.name,
      description: record.description,
      userIds: record.orgAdmins.map((item: any) => item.id) || [],
    };
  };

  const showAddUserModal = (record: OrgProjectTableItem) => {
    currentOrganizationId.value = record.id;
    userVisible.value = true;
  };

  const handleProjectDrawerCancel = () => {
    currentProjectDrawer.visible = false;
  };

  const showProjectDrawer = (record: TableData) => {
    currentUserDrawer.visible = false;
    currentProjectDrawer.visible = true;
    currentProjectDrawer.organizationId = record.id;
    currentProjectDrawer.currentName = record.name;
  };

  const showUserDrawer = (record: TableData) => {
    currentProjectDrawer.visible = false;
    currentUserDrawer.visible = true;
    currentUserDrawer.organizationId = record.id;
    currentUserDrawer.currentName = record.name;
  };

  function getRowClass(record: TableData) {
    return record.id === currentUserDrawer.organizationId ? 'selected-row-class' : '';
  }

  const handleUserDrawerCancel = () => {
    currentUserDrawer.visible = false;
    currentUserDrawer.organizationId = '';
  };

  const handleAddOrgModalCancel = (shouldSearch: boolean) => {
    orgVisible.value = false;
    if (shouldSearch) {
      fetchData();
    }
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

  const handleDelete = (record: TableData) => {
    openDeleteModal({
      title: t('system.organization.deleteName', { name: characterLimit(record.name) }),
      content: t('system.organization.deleteTip'),
      onBeforeOk: async () => {
        try {
          await deleteOrg(record.id);
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
    if (tag.eventTag === 'delete') {
      handleDelete(record);
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
  await tableStore.initColumn(TableKeyEnum.SYSTEM_ORGANIZATION, organizationColumns, 'drawer');
</script>

<style lang="less" scoped>
  .primary-color {
    color: rgb(var(--primary-5));
    cursor: pointer;
  }
</style>
