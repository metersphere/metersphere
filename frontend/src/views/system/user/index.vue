<template>
  <div class="p-6">
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button class="mr-3" type="primary" @click="showUserModal('batch')">{{
          t('system.user.createUser')
        }}</a-button>
        <a-button class="mr-3" type="outline" @click="showEmailInviteModal">{{
          t('system.user.emailInvite')
        }}</a-button>
        <a-button class="mr-3" type="outline" @click="showImportModal">{{ t('system.user.importUser') }}</a-button>
      </div>
      <div>
        <a-input-search
          :placeholder="t('system.user.searchUser')"
          class="w-[230px]"
          @search="searchUser"
        ></a-input-search>
      </div>
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #action="{ record }">
        <MsButton @click="showUserModal('edit')">{{ t('system.user.editUser') }}</MsButton>
        <MsTableMoreAction :list="tableActions" @select="handleSelect($event, record)"></MsTableMoreAction>
      </template>
    </ms-base-table>
    <a-modal
      v-model:visible="visible"
      :title="userFormMode === 'create' ? t('system.user.createUserModalTitle') : t('system.user.editUserModalTitle')"
      title-align="start"
      class="ms-modal-form ms-modal-medium"
      :loading="loading"
    >
      <a-form ref="userFormRef" class="rounded-[4px]" :model="userForm" layout="vertical">
        <div class="mb-[16px] overflow-y-auto rounded-[4px] bg-[var(--color-fill-1)] p-[12px]">
          <a-scrollbar class="max-h-[30vh] overflow-y-auto">
            <div class="flex flex-wrap items-start justify-between gap-[8px]">
              <template v-for="(item, i) of userForm.list" :key="`user-item-${item}`">
                <div class="flex w-full items-start justify-between gap-[8px]">
                  <a-form-item
                    :field="`username${item}`"
                    :class="i > 0 ? 'hidden-item' : 'mb-0 flex-1'"
                    :rules="[
                      { required: true, message: t('system.user.createUserNameNotNull') },
                      { validator: checkUerName },
                    ]"
                    :label="i === 0 ? t('system.user.createUserName') : ''"
                  >
                    <a-input
                      v-model="userForm[`username${item}`]"
                      class="mb-[4px] flex-1"
                      :placeholder="t('system.user.createUserNamePlaceholder')"
                      allow-clear
                    />
                  </a-form-item>
                  <a-form-item
                    :field="`email${item}`"
                    :class="i > 0 ? 'hidden-item' : 'mb-0 flex-1'"
                    :rules="[
                      { required: true, message: t('system.user.createUserEmailNotNull') },
                      { validator: (value, callback) => checkUerEmail(value, callback, i) },
                    ]"
                    :label="i === 0 ? t('system.user.createUserEmail') : ''"
                  >
                    <a-input
                      v-model="userForm[`email${item}`]"
                      class="mb-[4px] flex-1"
                      :placeholder="t('system.user.createUserEmailPlaceholder')"
                      allow-clear
                    />
                  </a-form-item>
                  <a-form-item
                    :field="`phone${item}`"
                    :class="i > 0 ? 'hidden-item' : 'mb-0 flex-1'"
                    :rules="[{ validator: checkUerPhone }]"
                    :label="i === 0 ? t('system.user.createUserPhone') : ''"
                  >
                    <a-input
                      v-model="userForm[`phone${item}`]"
                      class="mb-[4px] flex-1"
                      :placeholder="t('system.user.createUserPhonePlaceholder')"
                      :max-length="11"
                      allow-clear
                    />
                  </a-form-item>
                  <div
                    v-show="userForm.list.length > 1"
                    :class="[
                      'flex',
                      'h-full',
                      'w-[32px]',
                      'cursor-pointer',
                      'items-center',
                      'justify-center',
                      i === 0 ? 'mt-[6%]' : 'mt-[5px]',
                    ]"
                    @click="removeUserField(item, i)"
                  >
                    <icon-minus-circle />
                  </div>
                </div>
              </template>
            </div>
          </a-scrollbar>
          <div class="w-full">
            <a-button class="px-0" type="text" @click="addUserField">
              <template #icon>
                <icon-plus class="text-[14px]" />
              </template>
              {{ t('system.user.addUser') }}
            </a-button>
          </div>
        </div>
        <a-form-item class="mb-0" field="userGroup" :label="t('system.user.createUserUserGroup')">
          <a-select
            v-model="userForm.userGroup"
            multiple
            :placeholder="t('system.user.createUserUserGroupPlaceholder')"
            allow-clear
          >
            <a-option v-for="item of userGroupOptions" :key="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button type="secondary" @click="cancelCreate">{{ t('system.user.editUserModalCancelCreate') }}</a-button>
        <a-button v-if="userFormMode === 'batch'" type="secondary" @click="saveAndContinue">{{
          t('system.user.editUserModalSaveAndContinue')
        }}</a-button>
        <a-button type="primary" @click="beforeCreateUser">{{
          t(userFormMode === 'create' ? 'system.user.editUserModalCreateUser' : 'system.user.editUserModalEditUser')
        }}</a-button>
      </template>
    </a-modal>
    <a-modal
      v-model:visible="inviteVisible"
      :title="t('system.user.invite')"
      title-align="start"
      class="ms-modal-form ms-modal-medium"
      :loading="inviteLoading"
    >
      <a-form ref="inviteFormRef" class="rounded-[4px]" :model="emailForm" layout="vertical">
        <a-form-item
          field="emails"
          :label="t('system.user.inviteEmail')"
          :rules="[{ required: true, message: t('system.user.createUserEmailNotNull') }]"
        >
          <a-input-tag v-model="emailForm.emails" :placeholder="t('system.user.inviteEmailPlaceholder')" allow-clear />
        </a-form-item>
        <a-form-item class="mb-0" field="userGroup" :label="t('system.user.createUserUserGroup')">
          <a-select
            v-model="emailForm.userGroup"
            multiple
            :placeholder="t('system.user.createUserUserGroupPlaceholder')"
            allow-clear
          >
            <a-option v-for="item of userGroupOptions" :key="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button type="secondary" @click="cancelInvite">{{ t('system.user.inviteCancel') }}</a-button>
        <a-button type="primary" @click="emailInvite">
          {{ t('system.user.inviteSendEmail') }}
        </a-button>
      </template>
    </a-modal>
    <a-modal
      v-model:visible="importVisible"
      :title="t('system.user.importModalTitle')"
      title-align="start"
      class="ms-modal-upload"
      :loading="importLoading"
    >
      <a-alert class="mb-[16px]" closable>
        {{ t('system.user.importModalTip') }}
        <a-button type="text" size="small">{{ t('system.user.importDownload') }} </a-button>
      </a-alert>
      <a-upload action="/" accept=".xls,.xlsx" :show-file-list="false">
        <template #upload-button>
          <div class="ms-upload-area">
            <div class="ms-upload-icon-box">
              <div class="ms-upload-icon ms-upload-icon--excel"></div>
            </div>
            <div class="ms-upload-main-text">{{ t('system.user.importModalDragtext') }}</div>
            <div class="ms-upload-sub-text">{{ t('system.user.importModalFileTip') }}</div>
          </div>
        </template>
      </a-upload>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import useModal from '@/hooks/useModal';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { getTableList } from '@/api/modules/api-test/index';
  import { validateEmail, validatePhone } from '@/utils/validate';
  import { cloneDeep } from 'lodash-es';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  const columns: MsTableColumn = [
    {
      title: '邮箱',
      dataIndex: 'email',
      width: 200,
    },
    {
      title: '姓名',
      dataIndex: 'name',
    },
    {
      title: '手机',
      dataIndex: 'phone',
    },
    {
      title: '组织',
      dataIndex: 'organization',
    },
    {
      title: '用户组',
      dataIndex: 'userGroup',
    },
    {
      title: '状态',
      dataIndex: 'status',
    },
    {
      title: '操作',
      slotName: 'action',
      fixed: 'right',
      width: 80,
    },
  ];

  const { t } = useI18n();
  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.resetPassword',
      eventTag: 'resetPassword',
    },
    {
      label: 'system.user.disable',
      eventTag: 'disabled',
    },
    {
      isDivider: true,
    },
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const { openModal } = useModal();

  function resetPassword(record: any) {
    openModal({
      type: 'warning',
      title: `${t('system.user.resetPswStart')} '${record.name}' ${t('system.user.resetPswEnd')}`,
      content: t('system.user.resetPswContent'),
      okText: t('system.user.resetPswConfirm'),
      cancelText: t('system.user.resetPswCancel'),
      onBeforeOk: async () => {
        try {
          Message.success(t('system.user.resetPswSuccess'));
          return true;
        } catch (error) {
          console.log(error);
          return false;
        }
      },
      hideCancel: false,
    });
  }

  function disabledUser(record: any) {
    openModal({
      type: 'warning',
      title: `${t('system.user.disableUserStart')} '${record.name}' ${t('system.user.disableUserEnd')}`,
      content: t('system.user.disableUserContent'),
      okText: t('system.user.disableUserConfirm'),
      cancelText: t('system.user.disableUserCancel'),
      onBeforeOk: async () => {
        try {
          Message.success(t('system.user.disableUserSuccess'));
          return true;
        } catch (error) {
          console.log(error);
          return false;
        }
      },
      hideCancel: false,
    });
  }

  function deleteUser(record: any) {
    openModal({
      type: 'warning',
      title: `${t('system.user.deleteUserStart')} '${record.name}' ${t('system.user.deleteUserEnd')}`,
      content: t('system.user.deleteUserContent'),
      okText: t('system.user.deleteUserConfirm'),
      cancelText: t('system.user.deleteUserCancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          Message.success(t('system.user.deleteUserSuccess'));
          return true;
        } catch (error) {
          console.log(error);
          return false;
        }
      },
      hideCancel: false,
    });
  }

  function handleSelect(item: ActionsItem, record: any) {
    switch (item.eventTag) {
      case 'resetPassword':
        resetPassword(record);
        break;
      case 'disabled':
        disabledUser(record);
        break;
      case 'delete':
        deleteUser(record);
        break;
      default:
        break;
    }
  }

  const { propsRes, propsEvent, loadList } = useTable(getTableList, {
    columns,
    selectable: true,
  });

  onMounted(async () => {
    await loadList();
  });

  type UserModalMode = 'create' | 'edit' | 'batch';
  const visible = ref(false);
  const loading = ref(false);
  const userFormMode = ref<UserModalMode>('create');
  const userFormRef = ref<FormInstance | null>(null);
  const defaulUserForm = {
    email0: '',
    phone0: '',
    username0: '',
    list: [0],
    userGroup: [],
  };

  interface UserForm {
    list: number[];
    userGroup: string[];
    [key: string]: any;
  }

  const userForm = ref<UserForm>(cloneDeep(defaulUserForm));
  const userGroupOptions = ref([
    {
      label: 'Beijing',
      value: 'Beijing',
    },
    {
      label: 'Shanghai',
      value: 'Shanghai',
    },
    {
      label: 'Guangzhou',
      value: 'Guangzhou',
    },
  ]);

  function userFormValidate(cb: () => Promise<any>) {
    userFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        loading.value = true;
        await cb();
      } catch (error) {
        console.log(error);
      } finally {
        loading.value = false;
      }
    });
  }

  function addUserField() {
    userFormValidate(async () => {
      const lastIndex = userForm.value.list.length - 1;
      const lastOrder = userForm.value.list[lastIndex] + 1;
      userForm.value.list.push(lastOrder); // 序号自增
      userForm.value[`username${lastOrder}`] = '';
      userForm.value[`email${lastOrder}`] = '';
      userForm.value[`phone${lastOrder}`] = '';
    });
  }

  function removeUserField(index: number, i: number) {
    delete userForm.value[`username${index}`];
    delete userForm.value[`email${index}`];
    delete userForm.value[`phone${index}`];
    userForm.value.list.splice(i, 1);
  }

  function checkUerName(value: string | undefined, callback: (error?: string) => void) {
    if (value === '' || value === undefined) {
      callback(t('system.user.createUserNameNotNull'));
    } else if (value.length > 50) {
      callback(t('system.user.createUserNameOverLength'));
    }
  }

  function checkUerEmail(value: string | undefined, callback: (error?: string) => void, index: number) {
    if (value === '' || value === undefined) {
      callback(t('system.user.createUserEmailNotNull'));
    } else if (!validateEmail(value)) {
      callback(t('system.user.createUserEmailErr'));
    } else {
      const hasEmails: string[] = userForm.value.list.map((item: any, i: number) => {
        if (i !== index) {
          return userForm.value[`email${item}`];
        }
        return null;
      });
      if (hasEmails.includes(value)) {
        callback(t('createUserEmailNoRepeat'));
      }
    }
  }

  function checkUerPhone(value: string | undefined, callback: (error?: string) => void) {
    if (value !== '' && value !== undefined && !validatePhone(value)) {
      callback(t('system.user.createUserPhoneErr'));
    }
  }

  function cancelCreate() {
    visible.value = false;
    userFormRef.value?.resetFields();
    userForm.value = cloneDeep(defaulUserForm);
  }

  async function updateUser() {
    console.log('updateUser');
  }

  async function createUser() {
    console.log('createUser');
  }

  function beforeCreateUser() {
    if (userFormMode.value === 'create') {
      userFormValidate(createUser);
    } else {
      userFormValidate(updateUser);
    }
  }

  function saveAndContinue() {
    userFormValidate(async () => {
      await createUser();
      userFormRef.value?.resetFields();
      userForm.value = cloneDeep(defaulUserForm);
    });
  }

  function showUserModal(mode: UserModalMode) {
    visible.value = true;
    userFormMode.value = mode;
  }

  const inviteVisible = ref(false);
  const inviteLoading = ref(false);
  const inviteFormRef = ref<FormInstance | null>(null);
  const defaulInviteForm = {
    emails: [],
    userGroup: [],
  };
  const emailForm = ref(cloneDeep(defaulInviteForm));

  function showEmailInviteModal() {
    inviteVisible.value = true;
  }

  function emailInvite() {
    inviteFormRef.value?.validate();
  }

  function cancelInvite() {
    inviteVisible.value = false;
    inviteFormRef.value?.resetFields();
    emailForm.value = cloneDeep(defaulInviteForm);
  }

  const importVisible = ref(false);
  const importLoading = ref(false);

  function showImportModal() {
    importVisible.value = true;
  }

  function importUser() {}

  async function searchUser() {}
</script>

<style lang="less" scoped>
  .ms-upload-area {
    @apply flex w-full flex-col items-center justify-center;

    height: 154px;
    border: 1px dashed var(--color-text-input-border);
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);
    .ms-upload-icon-box {
      @apply rounded-full bg-white;

      margin-bottom: 16px;
      padding: 8px;
      width: 48px;
      height: 48px;
      .ms-upload-icon {
        @apply h-full w-full bg-cover bg-no-repeat;

        background-image: url('@/assets/svg/icons/uploadfile.svg');
        &--excel {
          background-image: url('@/assets/svg/icons/excel.svg');
        }
      }
    }
    .ms-upload-main-text {
      color: var(--color-text-1);
    }
    .ms-upload-sub-text {
      margin-bottom: 6px;
      font-size: 12px;
      color: var(--color-text-4);
    }
  }
</style>
