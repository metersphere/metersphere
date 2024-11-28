<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button
          v-permission.all="['SYSTEM_USER:READ+ADD']"
          class="mr-3"
          type="primary"
          @click="showUserModal('create')"
        >
          {{ t('system.user.createUser') }}
        </a-button>
        <a-button
          v-permission.all="['SYSTEM_USER:READ+INVITE']"
          class="mr-3"
          type="outline"
          @click="showEmailInviteModal"
        >
          {{ t('system.user.emailInvite') }}
        </a-button>
        <a-button v-permission.all="['SYSTEM_USER:READ+IMPORT']" class="mr-3" type="outline" @click="showImportModal">
          {{ t('system.user.importUser') }}
        </a-button>
      </div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('system.user.searchUser')"
        class="w-[230px]"
        allow-clear
        @search="searchUser"
        @press-enter="searchUser"
        @clear="searchUser"
      />
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
      @enable-change="enableChange"
    >
      <template #userGroup="{ record }">
        <MsTagGroup
          v-if="!record.selectUserGroupVisible"
          :tag-list="record.userRoleList"
          type="primary"
          theme="outline"
          allow-edit
          show-table
          @click="handleTagClick(record)"
        />
        <MsSelect
          v-else
          v-model:model-value="record.userRoleList"
          :placeholder="t('system.user.createUserUserGroupPlaceholder')"
          :options="userGroupOptions"
          :search-keys="['name']"
          :loading="record.selectUserGroupLoading"
          :disabled="record.selectUserGroupLoading"
          :fallback-option="(val) => ({
              label: (val as Record<string, any>).name,
              value: val,
            })"
          value-key="id"
          label-key="name"
          class="w-full max-w-[300px]"
          allow-clear
          :multiple="true"
          :at-least-one="true"
          :object-value="true"
          @popup-visible-change="(value) => handleUserGroupChange(value, record)"
        >
        </MsSelect>
      </template>
      <template #action="{ record }">
        <template v-if="!record.enable">
          <MsButton v-permission="['SYSTEM_USER:READ+DELETE']" @click="deleteUser(record)">
            {{ t('system.user.delete') }}
          </MsButton>
        </template>
        <template v-else>
          <MsButton v-permission="['SYSTEM_USER:READ+UPDATE']" @click="showUserModal('edit', record)">
            {{ t('system.user.editUser') }}
          </MsButton>
          <MsTableMoreAction
            v-permission="['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE']"
            :list="tableActions"
            @select="handleSelect($event, record)"
          />
        </template>
      </template>
    </ms-base-table>
  </MsCard>
  <a-modal
    v-model:visible="visible"
    :title="userFormMode === 'create' ? t('system.user.createUserModalTitle') : t('system.user.editUserModalTitle')"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :mask-closable="false"
    :closable="false"
  >
    <a-alert class="mb-[16px]">{{ t('system.user.createUserTip') }}</a-alert>
    <a-form
      v-if="visible"
      ref="userFormRef"
      class="rounded-[4px]"
      :model="userForm"
      layout="vertical"
      scroll-to-first-error
    >
      <MsBatchForm
        ref="batchFormRef"
        :models="batchFormModels"
        :form-mode="userFormMode"
        add-text="system.user.addUser"
        :default-vals="userForm.list"
        max-height="250px"
        @change="handleBatchFormChange"
      ></MsBatchForm>
      <a-form-item
        class="mb-0"
        field="userGroup"
        :label="t('system.user.createUserUserGroup')"
        :rules="[{ required: true, message: t('system.user.createUserUserGroupNotNull') }]"
        asterisk-position="end"
      >
        <MsSelect
          v-model:model-value="userForm.userGroup"
          :multiple="true"
          :placeholder="t('system.user.createUserUserGroupPlaceholder')"
          :options="userGroupOptions"
          :search-keys="['name']"
          :fallback-option="(val) => ({
              label: (val as Record<string, any>).name,
              value: val,
            })"
          class="w-full"
          :object-value="true"
          value-key="id"
          label-key="name"
          allow-clear
          :at-least-one="true"
          :should-calculate-max-tag="false"
        >
        </MsSelect>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="secondary" :disabled="loading" @click="handleBeforeClose">
        {{ t('system.user.editUserModalCancelCreate') }}
      </a-button>
      <a-button v-if="userFormMode === 'create'" type="secondary" :loading="loading" @click="saveAndContinue">
        {{ t('system.user.editUserModalSaveAndContinue') }}
      </a-button>
      <a-button type="primary" :loading="loading" @click="beforeCreateUser">
        {{ t(userFormMode === 'create' ? 'system.user.editUserModalCreateUser' : 'system.user.editUserModalEditUser') }}
      </a-button>
    </template>
  </a-modal>
  <a-modal
    v-model:visible="importVisible"
    :title="t('system.user.importModalTitle')"
    title-align="start"
    class="ms-modal-upload"
    @close="userImportFile = []"
  >
    <a-alert class="mb-[16px]">
      {{ t('system.user.importModalTip') }}
      <a-button type="text" size="small" @click="downLoadUserTemplate">
        {{ t('system.user.importDownload') }}
      </a-button>
    </a-alert>
    <MsUpload
      v-model:file-list="userImportFile"
      accept="excel"
      :show-file-list="false"
      :auto-upload="false"
      :disabled="importLoading"
      size-unit="MB"
    ></MsUpload>
    <template #footer>
      <a-button type="secondary" :disabled="importLoading" @click="cancelImport">
        {{ t('system.user.importModalCancel') }}
      </a-button>
      <a-button type="primary" :loading="importLoading" :disabled="userImportFile.length === 0" @click="importUser">
        {{ t('system.user.importModalConfirm') }}
      </a-button>
    </template>
  </a-modal>
  <a-modal v-model:visible="importResultVisible" title-align="start" class="ms-modal-upload" :closable="false">
    <template #title>
      {{ importResultTitle }}
    </template>
    <div v-if="importResult === 'success'" class="flex flex-col items-center justify-center">
      <icon-check-circle-fill class="text-[32px] text-[rgb(var(--success-6))]" />
      <div class="mb-[8px] mt-[16px] text-[16px] font-medium leading-[24px] text-[var(--color-text-1)]">
        {{ t('system.user.importSuccess') }}
      </div>
      <div class="sub-text">
        {{ t('system.user.importResultSuccessContent', { successNum: importSuccessCount }) }}
      </div>
    </div>
    <template v-else>
      <a-alert type="error">
        <template #icon>
          <icon-exclamation-circle-fill class="text-[rgb(var(--danger-6))]" />
        </template>
        <div class="flex items-center">
          {{ t('system.user.importResultContent', { successNum: importSuccessCount }) }}
          <div class="mx-[4px] text-[rgb(var(--danger-6))]">{{ importFailCount }}</div>
          {{ t('system.user.importResultContentEnd') }}
          <a-popover v-if="Object.keys(importErrorMessages).length > 0" content-class="w-[400px] p-0" position="bottom">
            <MsButton type="text">
              {{ t('system.user.importErrorDetail') }}
            </MsButton>
            <template #content>
              <div class="px-[16px] pt-[16px] text-[14px]">
                <div class="flex items-center font-medium">
                  <div class="text-[var(--color-text-1)]">{{ t('system.user.importErrorMessageTitle') }}</div>
                  <div class="ml-[4px] text-[var(--color-text-4)]">({{ importFailCount }})</div>
                </div>
                <div class="import-error-message-list mt-[8px]">
                  <div
                    v-for="key of Object.keys(importErrorMessages)"
                    :key="key"
                    class="mb-[16px] flex items-center text-[var(--color-text-2)]"
                  >
                    {{ t('system.user.num') }}
                    <div class="mx-[4px] font-medium">{{ key }}</div>
                    {{ t('system.user.line') }}：
                    {{ importErrorMessages[key] }}
                  </div>
                </div>
              </div>
              <div v-if="Object.keys(importErrorMessages).length > 8" class="import-error-message-footer">
                <MsButton type="text" @click="importErrorMessageDrawerVisible = true">
                  {{ t('system.user.seeMore') }}
                </MsButton>
              </div>
            </template>
          </a-popover>
        </div>
      </a-alert>
    </template>
    <template #footer>
      <a-button type="text" class="!text-[var(--color-text-1)]" @click="cancelImport">
        {{ t('system.user.importResultReturn') }}
      </a-button>
      <a-button type="text" @click="continueImport">
        {{ t('system.user.importResultContinue') }}
      </a-button>
    </template>
  </a-modal>
  <inviteModal v-model:visible="inviteVisible" :user-group-options="userGroupOptions"></inviteModal>
  <batchModal
    v-model:visible="showBatchModal"
    :table-selected="tableSelected"
    :action="batchAction"
    :batch-params="batchModalParams"
    :keyword="keyword"
    @finished="resetSelector"
  ></batchModal>
  <MsDrawer v-model:visible="importErrorMessageDrawerVisible" :width="680" :footer="false">
    <template #title>
      <div class="flex items-center font-medium">
        <div class="text-[var(--color-text-1)]">{{ t('system.user.importErrorMessageTitle') }}</div>
        <div class="ml-[4px] text-[var(--color-text-4)]">({{ importFailCount }})</div>
      </div>
    </template>
    <div class="import-error-message-list !max-h-full">
      <div
        v-for="key of Object.keys(importErrorMessages)"
        :key="key"
        class="mb-[16px] flex items-center text-[var(--color-text-2)]"
      >
        <div class="mr-[8px] h-[6px] w-[6px] rounded-full bg-[var(--color-text-input-border)]"></div>
        {{ t('system.user.num') }}
        <div class="mx-[4px] font-medium">{{ key }}</div>
        {{ t('system.user.line') }}：
        {{ importErrorMessages[key] }}
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  /**
   * @description 系统设置-用户
   */
  import { onBeforeMount, Ref, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import type { FormItemModel } from '@/components/business/ms-batch-form/types';
  import MsSelect from '@/components/business/ms-select';
  import inviteModal from '../components/inviteModal.vue';
  import batchModal from './components/batchModal.vue';

  import {
    batchCreateUser,
    deleteUserInfo,
    getSystemRoles,
    getUserList,
    importUserInfo,
    resetUserPassword,
    toggleUserStatus,
    updateUserInfo,
  } from '@/api/modules/setting/user';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useLocale from '@/locale/useLocale';
  import { useTableStore } from '@/store';
  import { characterLimit, formatPhoneNumber } from '@/utils';
  import { hasAllPermission, hasAnyPermission } from '@/utils/permission';
  import { validateEmail, validatePhone } from '@/utils/validate';

  import type { SimpleUserInfo, SystemRole, UserListItem } from '@/models/setting/user';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import type { FileItem, FormInstance, ValidatedError } from '@arco-design/web-vue';
  import type { FieldData } from '@arco-design/web-vue/es/form/interface';

  const { t } = useI18n();
  const { currentLocale } = useLocale();
  const route = useRoute();

  const hasOperationSysUserPermission = computed(() =>
    hasAnyPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE'])
  );

  const columns: MsTableColumn = [
    {
      title: 'system.user.userName',
      dataIndex: 'email',
      showTooltip: true,
      sortIndex: 0,
      columnSelectorDisabled: true,
    },
    {
      title: 'system.user.tableColumnName',
      dataIndex: 'name',
      showTooltip: true,
      sortIndex: 1,
      columnSelectorDisabled: true,
    },
    {
      title: 'system.user.tableColumnEmail',
      dataIndex: 'email',
      showTooltip: true,
      sortIndex: 2,
      columnSelectorDisabled: true,
    },
    {
      title: 'system.user.tableColumnPhone',
      dataIndex: 'phone',
      showDrag: true,
      showTooltip: true,
      width: 140,
    },
    {
      title: 'system.user.tableColumnOrg',
      dataIndex: 'organizationList',
      isTag: true,
      showDrag: true,
      tagPrimary: 'default',
      width: 300,
    },
    {
      title: 'system.user.tableColumnUserGroup',
      dataIndex: 'userRoleList',
      slotName: 'userGroup',
      isTag: true,
      showDrag: true,
      allowEditTag: true,
      width: 300,
    },
    {
      title: 'system.user.tableColumnStatus',
      slotName: 'enable',
      dataIndex: 'enable',
      showDrag: true,
      permission: ['SYSTEM_USER:READ+UPDATE'],
    },
    {
      title: hasOperationSysUserPermission.value ? 'system.user.tableColumnActions' : '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationSysUserPermission.value ? 110 : 50,
    },
  ];
  const tableStore = useTableStore();
  const { propsRes, propsEvent, loadList, setKeyword, resetSelector } = useTable(
    getUserList,
    {
      tableKey: TableKeyEnum.SYSTEM_USER,
      columns,
      selectable: !!hasAnyPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE']),
      showSetting: true,
      heightUsed: 238,
    },
    (record) => ({
      ...record,
      organizationList: record.organizationList.filter((e: any) => e),
      userRoleList: record.userRoleList.filter((e: any) => e),
      phone: formatPhoneNumber(record.phone || ''),
      selectUserGroupVisible: false,
      selectUserGroupLoading: false,
    })
  );

  const keyword = ref('');

  async function searchUser() {
    setKeyword(keyword.value);
    await loadList();
  }

  const tableSelected = ref<(string | number)[]>([]);

  const { openModal } = useModal();

  /**
   * 重置密码
   */
  function resetPassword(record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('system.user.resetPswTip', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('system.user.batchResetPswTip', { count: params?.currentSelectCount || tableSelected.value.length });
      selectIds = tableSelected.value as string[];
    }

    let content = t('system.user.resetPswContent');
    if (record && record.id === 'admin') {
      content = t('system.user.resetAdminPswContent');
    }
    openModal({
      type: 'warning',
      title,
      content,
      okText: t('system.user.resetPswConfirm'),
      cancelText: t('system.user.resetPswCancel'),
      onBeforeOk: async () => {
        try {
          await resetUserPassword({
            selectIds,
            selectAll: !!params?.selectAll,
            excludeIds: params?.excludeIds || [],
            condition: { keyword: keyword.value },
          });
          Message.success(t('system.user.resetPswSuccess'));
          resetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 禁用用户
   */
  function disabledUser(record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('system.user.disableUserTip', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('system.user.batchDisableUserTip', { count: params?.currentSelectCount || tableSelected.value.length });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'warning',
      title,
      content: t('system.user.disableUserContent'),
      okText: t('system.user.disableUserConfirm'),
      cancelText: t('system.user.disableUserCancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await toggleUserStatus({
            selectIds,
            selectAll: !!params?.selectAll,
            excludeIds: params?.excludeIds || [],
            condition: { keyword: keyword.value },
            enable: false,
          });
          Message.success(t('system.user.disableUserSuccess'));
          resetSelector();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 启用用户
   */
  function enableUser(record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('system.user.enableUserTip', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('system.user.batchEnableUserTip', { count: params?.currentSelectCount || tableSelected.value.length });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'info',
      title,
      content: t('system.user.enableUserContent'),
      okText: t('system.user.enableUserConfirm'),
      cancelText: t('system.user.enableUserCancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await toggleUserStatus({
            selectIds,
            selectAll: !!params?.selectAll,
            excludeIds: params?.excludeIds || [],
            condition: { keyword: keyword.value },
            enable: true,
          });
          Message.success(t('system.user.enableUserSuccess'));
          resetSelector();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function enableChange(record: UserListItem, newValue: string | number | boolean) {
    if (newValue) {
      enableUser(record);
    } else {
      disabledUser(record);
    }
  }

  /**
   * 删除用户
   */
  function deleteUser(record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('system.user.deleteUserTip', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('system.user.batchDeleteUserTip', { count: params?.currentSelectCount || tableSelected.value.length });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('system.user.deleteUserContent'),
      okText: t('system.user.deleteUserConfirm'),
      cancelText: t('system.user.deleteUserCancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteUserInfo({
            selectIds,
            selectAll: !!params?.selectAll,
            excludeIds: params?.excludeIds || [],
            condition: { keyword: keyword.value },
          });
          Message.success(t('system.user.deleteUserSuccess'));
          resetSelector();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.resetPassword',
      eventTag: 'resetPassword',
      permission: ['SYSTEM_USER:READ+UPDATE'],
    },
    {
      isDivider: true,
    },
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
      permission: ['SYSTEM_USER:READ+DELETE'],
    },
  ];

  const tableBatchActions = {
    baseAction: [
      {
        label: 'system.user.batchActionAddProject',
        eventTag: 'batchAddProject',
        permission: [
          'SYSTEM_USER:READ+UPDATE',
          'SYSTEM_USER_ROLE:READ',
          'SYSTEM_ORGANIZATION_PROJECT:READ',
          'SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER',
        ],
      },
      {
        label: 'system.user.batchActionAddUserGroup',
        eventTag: 'batchAddUserGroup',
        permission: ['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER_ROLE:READ'],
      },
      {
        label: 'system.user.batchActionAddOrganization',
        eventTag: 'batchAddOrganization',
        permission: [
          'SYSTEM_USER:READ+UPDATE',
          'SYSTEM_USER_ROLE:READ',
          'SYSTEM_ORGANIZATION_PROJECT:READ',
          'SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER',
        ],
      },
    ],
    moreAction: [
      {
        label: 'system.user.resetPassword',
        eventTag: 'resetPassword',
        permission: ['SYSTEM_USER:READ+UPDATE'],
      },
      {
        label: 'system.user.disable',
        eventTag: 'disabled',
        permission: ['SYSTEM_USER:READ+UPDATE'],
      },
      {
        label: 'system.user.enable',
        eventTag: 'enable',
        permission: ['SYSTEM_USER:READ+UPDATE'],
      },
      {
        isDivider: true,
      },
      {
        label: 'system.user.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['SYSTEM_USER:READ+DELETE'],
      },
    ],
  };

  const showBatchModal = ref(false);
  const batchAction = ref(''); // 表格选中批量操作动作
  const batchModalParams = ref();

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    switch (event.eventTag) {
      case 'batchAddProject':
      case 'batchAddUserGroup':
      case 'batchAddOrganization':
        batchAction.value = event.eventTag;
        batchModalParams.value = params;
        showBatchModal.value = true;
        break;
      case 'resetPassword':
        resetPassword(undefined, true, params);
        break;
      case 'disabled':
        disabledUser(undefined, true, params);
        break;
      case 'enable':
        enableUser(undefined, true, params);
        break;
      case 'delete':
        deleteUser(undefined, true, params);
        break;
      default:
        break;
    }
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleSelect(item: ActionsItem, record: UserListItem) {
    switch (item.eventTag) {
      case 'resetPassword':
        resetPassword(record);
        break;
      case 'delete':
        deleteUser(record);
        break;
      default:
        break;
    }
  }

  type UserModalMode = 'create' | 'edit';

  interface UserForm {
    list: SimpleUserInfo[];
    userGroup: Record<string, any>[];
  }

  const visible = ref(false);
  const loading = ref(false);
  const userFormMode = ref<UserModalMode>('create');
  const userFormRef = ref<FormInstance | null>(null);
  const defaultUserForm = {
    list: [
      {
        name: '',
        email: '',
        phone: '',
      },
    ],
    userGroup: [],
  };
  const userForm = ref<UserForm>(cloneDeep(defaultUserForm));
  const userGroupOptions = ref<SystemRole[]>([]);

  async function init() {
    try {
      userGroupOptions.value = await getSystemRoles();
      if (userGroupOptions.value.length) {
        userForm.value.userGroup = userGroupOptions.value.filter((e: SystemRole) => e.selected === true);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(async () => {
    setKeyword(keyword.value);
    if (route.query.id) {
      setKeyword(route.query.id as string);
    }
    init();
    loadList();
  });

  /**
   * 重置用户表单
   */
  function resetUserForm() {
    userForm.value.list = [];
    userFormRef.value?.resetFields();
    userForm.value.userGroup = userGroupOptions.value.filter((e: SystemRole) => e.selected === true);
  }

  /**
   * 显示用户表单弹窗
   * @param mode 模式，编辑或创建
   * @param record 编辑时传入的用户信息
   */
  function showUserModal(mode: UserModalMode, record?: UserListItem) {
    visible.value = true;
    userFormMode.value = mode;
    if (mode === 'edit' && record) {
      userForm.value.list = [
        {
          id: record.id,
          name: record.name,
          email: record.email,
          phone: record.phone ? record.phone.replace(/\s/g, '') : record.phone,
        },
      ];
      userForm.value.userGroup = record.userRoleList;
    }
  }

  /**
   * 校验用户姓名
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   */
  function checkUerName(value: string | undefined, callback: (error?: string) => void) {
    if (value === '' || value === undefined) {
      callback(t('system.user.createUserNameNotNull'));
    } else if (value.length > 255) {
      callback(t('system.user.createUserNameOverLength'));
    }
  }

  /**
   * 校验用户邮箱
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   * @param index 当前输入的表单项对应 list 的下标，用于校验重复输入的时候排除自身
   */
  function checkUerEmail(value: string | undefined, callback: (error?: string) => void) {
    if (value === '' || value === undefined) {
      callback(t('system.user.createUserEmailNotNull'));
    } else if (!validateEmail(value)) {
      callback(t('system.user.createUserEmailErr'));
    }
  }

  /**
   * 校验用户手机号
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   */
  function checkUerPhone(value: string | undefined, callback: (error?: string) => void) {
    if (value !== null && value !== '' && value !== undefined && !validatePhone(value)) {
      callback(t('system.user.createUserPhoneErr'));
    }
  }

  const batchFormRef = ref<InstanceType<typeof MsBatchForm>>();
  const batchFormModels: Ref<FormItemModel[]> = ref([
    {
      field: 'name',
      type: 'input',
      label: 'system.user.createUserName',
      rules: [{ required: true, message: t('system.user.createUserNameNotNull') }, { validator: checkUerName }],
      placeholder: 'system.user.createUserNamePlaceholder',
    },
    {
      field: 'email',
      type: 'input',
      label: 'system.user.createUserEmail',
      rules: [
        { required: true, message: t('system.user.createUserEmailNotNull') },
        { validator: checkUerEmail },
        { notRepeat: true, message: 'system.user.createUserEmailNoRepeat' },
      ],
      placeholder: 'system.user.createUserEmailPlaceholder',
    },
    {
      field: 'phone',
      type: 'input',
      label: 'system.user.createUserPhone',
      rules: [{ validator: checkUerPhone }],
      placeholder: 'system.user.createUserPhonePlaceholder',
    },
  ]);
  const isBatchFormChange = ref(false);

  function handleBatchFormChange() {
    isBatchFormChange.value = true;
  }

  /**
   * 取消创建，重置用户表单
   */
  function cancelCreate() {
    visible.value = false;
    resetUserForm();
  }

  /**
   * 更新用户
   */
  async function updateUser() {
    const activeUser = userForm.value.list[0];
    const params = {
      id: activeUser.id as string,
      name: activeUser.name,
      email: activeUser.email,
      phone: activeUser.phone,
      userRoleIdList: userForm.value.userGroup.map((e) => e.id),
    };
    await updateUserInfo(params);
    Message.success(t('system.user.updateUserSuccess'));
    visible.value = false;
    loadList();
  }

  function handleTagClick(record: UserListItem & Record<string, any>) {
    if (hasAllPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER_ROLE:READ'])) {
      record.selectUserGroupVisible = true;
    }
  }

  /**
   * 快捷修改用户组
   */
  async function handleUserGroupChange(val: boolean, record: UserListItem & Record<string, any>) {
    try {
      if (!val) {
        record.selectUserGroupLoading = true;
        const params = {
          id: record.id,
          name: record.name,
          email: record.email,
          phone: record.phone,
          userRoleIdList: record.userRoleList.map((e) => e.id),
        };
        await updateUserInfo(params);
        Message.success(t('system.user.updateUserSuccess'));
        record.selectUserGroupVisible = false;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      record.selectUserGroupLoading = false;
    }
  }

  /**
   * 创建用户
   * @param isContinue 是否继续创建
   */
  async function createUser(isContinue?: boolean) {
    const params = {
      userInfoList: userForm.value.list,
      userRoleIdList: userForm.value.userGroup.map((e) => e.id),
    };
    const res = await batchCreateUser(params);
    if (res.errorEmails !== null) {
      const errData: Record<string, FieldData> = {};
      Object.keys(res.errorEmails).forEach((key) => {
        const filedIndex = userForm.value.list.findIndex((e) => e.email === key);
        if (filedIndex > -1) {
          errData[`list[${filedIndex}].email`] = {
            status: 'error',
            message: t('system.user.createUserEmailExist'),
          };
        }
      });
      batchFormRef.value?.setFields(errData);
    } else {
      Message.success(t('system.user.addUserSuccess'));
      if (!isContinue) {
        cancelCreate();
      }
      loadList();
    }
  }

  /**
   * 触发创建用户表单校验
   * @param cb 校验通过后执行回调
   */
  function userFormValidate(cb: () => Promise<any>) {
    userFormRef.value?.validate((errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      batchFormRef.value?.formValidate(async (list: any) => {
        try {
          loading.value = true;
          userForm.value.list = [...list];
          await cb();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          loading.value = false;
        }
      });
    });
  }

  /**
   * 创建前的校验
   */
  function beforeCreateUser() {
    if (userFormMode.value === 'create') {
      userFormValidate(createUser);
    } else {
      userFormValidate(updateUser);
    }
  }

  /**
   * 保存并继续创建，重置用户表单
   */
  function saveAndContinue() {
    userFormValidate(async () => {
      await createUser(true);
      resetUserForm();
      batchFormRef.value?.resetForm();
    });
  }

  function handleBeforeClose() {
    if (isBatchFormChange.value) {
      openModal({
        type: 'warning',
        title: t('common.tip'),
        content: t('system.user.closeTip'),
        okText: t('common.close'),
        onBeforeOk: async () => {
          cancelCreate();
        },
        hideCancel: false,
      });
    } else {
      cancelCreate();
    }
  }

  const inviteVisible = ref(false);
  function showEmailInviteModal() {
    inviteVisible.value = true;
  }

  const importVisible = ref(false);
  const importLoading = ref(false);
  const userImportFile = ref<FileItem[]>([]);
  const importResultVisible = ref(false);
  const importSuccessCount = ref(0);
  const importFailCount = ref(0);
  const importResult = ref<'success' | 'allFail' | 'fail'>('allFail');
  const importResultTitle = ref(t('system.user.importSuccessTitle'));
  const importErrorMessages = ref<Record<string, any>>({});
  const importErrorMessageDrawerVisible = ref(false);

  function showImportModal() {
    importVisible.value = true;
    importFailCount.value = 0;
    importSuccessCount.value = 0;
    importResult.value = 'allFail';
  }

  function cancelImport() {
    importVisible.value = false;
    importResultVisible.value = false;
    userImportFile.value = [];
  }

  /**
   * 根据导入结果展示结果弹窗
   */
  function showImportResult() {
    importVisible.value = false;
    switch (importResult.value) {
      case 'success':
        importResultTitle.value = t('system.user.importSuccessTitle');
        loadList();
        break;
      case 'allFail':
        importResultTitle.value = t('system.user.importModalTitle');
        break;
      case 'fail':
        importResultTitle.value = t('system.user.importModalTitle');
        loadList();
        break;
      default:
        break;
    }
    importResultVisible.value = true;
  }

  function continueImport() {
    importResultVisible.value = false;
    userImportFile.value = [];
    importVisible.value = true;
  }

  async function importUser() {
    try {
      importLoading.value = true;
      const res = await importUserInfo({
        fileList: [userImportFile.value[0].file],
      });
      const { data } = res;
      const failCount = data.importCount - data.successCount;
      if (failCount === data.importCount) {
        importResult.value = 'allFail';
      } else if (failCount > 0) {
        importResult.value = 'fail';
      } else {
        importResult.value = 'success';
      }
      importSuccessCount.value = data.successCount;
      importFailCount.value = failCount;
      importErrorMessages.value = data.errorMessages;
      showImportResult();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      importLoading.value = false;
    }
  }

  function downLoadUserTemplate() {
    if (currentLocale.value === 'zh-CN') {
      window.open('/templates/user_import_cn.xlsx', '_blank');
    } else {
      window.open('/templates/user_import_en.xlsx', '_blank');
    }
  }
  await tableStore.initColumn(TableKeyEnum.SYSTEM_USER, columns, 'drawer');
</script>

<style lang="less" scoped>
  .disableTag {
    border-color: rgb(var(--primary-3));
    color: rgb(var(--primary-3));
  }
  .enableTag {
    border-color: rgb(var(--primary-5));
    color: rgb(var(--primary-5));
  }
  .import-error-message-list {
    .ms-scroll-bar();
    @apply overflow-y-auto;

    max-height: 250px;
  }
  .import-error-message-footer {
    @apply flex items-center justify-center;

    padding: 8px 0;
    box-shadow: 0 -1px 4px 0 rgb(31 35 41 / 10%);
  }
</style>
