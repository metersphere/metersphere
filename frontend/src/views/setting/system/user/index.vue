<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button class="mr-3" type="primary" @click="showUserModal('create')">{{
          t('system.user.createUser')
        }}</a-button>
        <a-button class="mr-3" type="outline" @click="showEmailInviteModal">{{
          t('system.user.emailInvite')
        }}</a-button>
        <a-button class="mr-3" type="outline" @click="showImportModal">{{ t('system.user.importUser') }}</a-button>
      </div>
      <a-input-search
        :placeholder="t('system.user.searchUser')"
        class="w-[230px]"
        @search="searchUser"
      ></a-input-search>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handelTableBatch"
    >
      <template #organization="{ record }">
        <a-tag
          v-for="org of record.organizationList.slice(0, 2)"
          :key="org.id"
          class="mr-[4px] bg-transparent"
          bordered
        >
          {{ org.name }}
        </a-tag>
        <a-tag v-show="record.organizationList.length > 2" class="mr-[4px] bg-transparent" bordered>
          +{{ record.organizationList.length - 2 }}
        </a-tag>
      </template>
      <template #userRole="{ record }">
        <a-tag
          v-for="org of record.userRoleList.slice(0, 2)"
          :key="org.id"
          class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
          bordered
        >
          {{ org.name }}
        </a-tag>
        <a-tag
          v-show="record.organizationList.length > 2"
          class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
          bordered
        >
          +{{ record.organizationList.length - 2 }}
        </a-tag>
      </template>
      <template #enable="{ record }">
        <div v-if="record.enable" class="flex items-center">
          <icon-check-circle-fill class="mr-[2px] text-[rgb(var(--success-6))]" />
          {{ t('system.user.tableEnable') }}
        </div>
        <div v-else class="flex items-center text-[var(--color-text-4)]">
          <icon-stop class="mr-[2px]" />
          {{ t('system.user.tableDisable') }}
        </div>
      </template>
      <template #action="{ record }">
        <template v-if="!record.enable">
          <MsButton @click="enableUser(record)">{{ t('system.user.enable') }}</MsButton>
          <MsButton @click="deleteUser(record)">{{ t('system.user.delete') }}</MsButton>
        </template>
        <template v-else>
          <MsButton @click="showUserModal('edit', record)">{{ t('system.user.editUser') }}</MsButton>
          <MsTableMoreAction :list="tableActions" @select="handleSelect($event, record)"></MsTableMoreAction>
        </template>
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
          <div v-if="userFormMode === 'create'" class="w-full">
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
        <a-button v-if="userFormMode === 'create'" type="secondary" @click="saveAndContinue">{{
          t('system.user.editUserModalSaveAndContinue')
        }}</a-button>
        <a-button type="primary" @click="beforeCreateUser">{{
          t(userFormMode === 'create' ? 'system.user.editUserModalCreateUser' : 'system.user.editUserModalEditUser')
        }}</a-button>
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
      <MsUpload
        action="/"
        accept="excel"
        main-text="system.user.importModalDragtext"
        sub-text="system.user.importModalFileTip"
        :show-file-list="false"
      ></MsUpload>
      <template #footer>
        <a-button type="secondary" @click="cancelImport">{{ t('system.user.importModalCancel') }}</a-button>
        <a-button type="primary" @click="importUser">
          {{ t('system.user.importModalConfirm') }}
        </a-button>
      </template>
    </a-modal>
    <a-modal v-model:visible="importResultVisible" title-align="start" class="ms-modal-upload">
      <template #title>
        <icon-exclamation-circle-fill
          v-if="importResult === 'fail'"
          class="mr-[8px] text-[20px] text-[rgb(var(--warning-6))]"
        />
        <icon-close-circle-fill
          v-if="importResult === 'allFail'"
          class="mr-[8px] text-[20px] text-[rgb(var(--danger-6))]"
        />
        {{ importResultTitle }}
      </template>
      <div v-if="importResult === 'success'" class="flex flex-col items-center justify-center">
        <icon-check-circle-fill class="text-[32px] text-[rgb(var(--success-6))]" />
        <div class="mb-[8px] mt-[16px] text-[16px] font-medium text-[var(--color-text-000)]">{{
          t('system.user.importSuccess')
        }}</div>
        <div class="sub-text">{{
          t('system.user.importResultSuccessContent', { successNum: importSuccessCount })
        }}</div>
      </div>
      <template v-else>
        <div>{{
          t('system.user.importResultContent', { successNum: importSuccessCount, failNum: importFailCount })
        }}</div>
        <div
          >{{ t('system.user.importResultContentSubStart')
          }}<a-link
            class="text-[rgb(var(--primary-5))]"
            :href="importErrorFileUrl"
            :download="`${t('system.user.importErrorFile')}.pdf`"
            >{{ t('system.user.importResultContentDownload') }}</a-link
          >{{ t('system.user.importResultContentSubEnd') }}</div
        >
      </template>
      <template #footer>
        <a-button type="text" class="!text-[var(--color-text-1)]" @click="cancelImport">{{
          t('system.user.importResultReturn')
        }}</a-button>
        <a-button type="text" @click="continueImport">
          {{ t('system.user.importResultContinue') }}
        </a-button>
      </template>
    </a-modal>
    <inviteModal v-model:visible="inviteVisible"></inviteModal>
    <batchModal
      v-model:visible="showBatchModal"
      :table-selected="tableSelected"
      :action="batchAction"
      :tree-data="treeData"
    ></batchModal>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import useModal from '@/hooks/useModal';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { getUserList, batchCreateUser } from '@/api/modules/setting/user';
  import { validateEmail, validatePhone } from '@/utils/validate';
  import batchModal from './components/batchModal.vue';
  import inviteModal from './components/inviteModal.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import type { UserListItem } from '@/models/setting/user';

  const { t } = useI18n();

  const columns: MsTableColumn = [
    {
      title: 'system.user.tableColunmEmail',
      dataIndex: 'email',
      width: 200,
    },
    {
      title: 'system.user.tableColunmName',
      dataIndex: 'name',
    },
    {
      title: 'system.user.tableColunmPhone',
      dataIndex: 'phone',
    },
    {
      title: 'system.user.tableColunmOrg',
      slotName: 'organization',
      dataIndex: 'organizationList',
    },
    {
      title: 'system.user.tableColunmUsergroup',
      slotName: 'userRole',
      dataIndex: 'userRoleList',
    },
    {
      title: 'system.user.tableColunmStatus',
      slotName: 'enable',
      dataIndex: 'enable',
    },
    {
      title: 'system.user.tableColunmActions',
      slotName: 'action',
      fixed: 'right',
      width: 90,
    },
  ];

  const { propsRes, propsEvent, loadList, setKeyword } = useTable(getUserList, {
    columns,
    scroll: { y: 'auto' },
    selectable: true,
  });

  const keyword = ref('');

  onMounted(async () => {
    setKeyword(keyword.value);
    await loadList();
  });

  async function searchUser() {
    setKeyword(keyword.value);
    await loadList();
  }

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

  const tableBatchActions = {
    baseAction: [
      {
        label: 'system.user.batchActionAddProject',
        eventTag: 'batchAddProject',
      },
      {
        label: 'system.user.batchActionAddUsergroup',
        eventTag: 'batchAddUsergroup',
      },
      {
        label: 'system.user.batchActionAddOrgnization',
        eventTag: 'batchAddOrgnization',
      },
    ],
    moreAction: [
      {
        label: 'system.user.disable',
        eventTag: 'disabled',
      },
      {
        label: 'system.user.enable',
        eventTag: 'enable',
      },
      {
        isDivider: true,
      },
      {
        label: 'system.user.delete',
        eventTag: 'delete',
        danger: true,
      },
    ],
  };

  const showBatchModal = ref(false);
  const batchAction = ref('');
  const treeData = ref([
    {
      title: 'Trunk 0-3',
      key: '0-3',
    },
    {
      title: 'Trunk 0-0',
      key: '0-0',
      children: [
        {
          title: 'Leaf 0-0-1',
          key: '0-0-1',
        },
        {
          title: 'Branch 0-0-2',
          key: '0-0-2',
        },
      ],
    },
    {
      title: 'Trunk 0-1',
      key: '0-1',
      children: [
        {
          title: 'Branch 0-1-1',
          key: '0-1-1',
        },
        {
          title: 'Leaf 0-1-2',
          key: '0-1-2',
        },
      ],
    },
  ]);

  function handelTableBatch() {
    batchAction.value = 'batchAddProject';
    showBatchModal.value = true;
  }

  const tableSelected = ref<(string | number)[]>([]);

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }
  const { openModal } = useModal();

  /**
   * 重置密码
   */
  function resetPassword(record: any) {
    openModal({
      type: 'warning',
      title: t('system.user.resetPswTip', { name: record.name }),
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

  /**
   * 禁用用户
   */
  function disabledUser(record: any) {
    openModal({
      type: 'warning',
      title: t('system.user.disableUserTip', { name: record.name }),
      content: t('system.user.disableUserContent'),
      okText: t('system.user.disableUserConfirm'),
      cancelText: t('system.user.disableUserCancel'),
      okButtonProps: {
        status: 'danger',
      },
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

  /**
   * 启用用户
   */
  function enableUser(record: any) {
    openModal({
      type: 'info',
      title: t('system.user.enableUserTip', { name: record.name }),
      content: t('system.user.enableUserContent'),
      okText: t('system.user.enableUserConfirm'),
      cancelText: t('system.user.enableUserCancel'),
      onBeforeOk: async () => {
        try {
          Message.success(t('system.user.enableUserSuccess'));
          return true;
        } catch (error) {
          console.log(error);
          return false;
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 删除用户
   */
  function deleteUser(record: any) {
    openModal({
      type: 'warning',
      title: t('system.user.deleteUserTip', { name: record.name }),
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

  /**
   * 处理表格更多按钮事件
   * @param item
   */
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

  type UserModalMode = 'create' | 'edit';
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

  interface UserForm {
    list: number[];
    userGroup: string[];
    [key: string]: any;
  }

  const userForm = ref<UserForm>(cloneDeep(defaulUserForm));

  /**
   * 触发创建用户表单校验
   * @param cb 校验通过后执行回调
   */
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

  /**
   * 添加用户表单项
   */
  function addUserField() {
    userFormValidate(async () => {
      const lastIndex = userForm.value.list.length - 1;
      const lastOrder = userForm.value.list[lastIndex] + 1;
      userForm.value.list.push(lastOrder); // 序号自增，不会因为删除而重复
      userForm.value[`username${lastOrder}`] = '';
      userForm.value[`email${lastOrder}`] = '';
      userForm.value[`phone${lastOrder}`] = '';
    });
  }

  /**
   * 移除用户表单项
   * @param index 表单项的序号
   * @param i 表单项对应 list 的下标
   */
  function removeUserField(index: number, i: number) {
    delete userForm.value[`username${index}`];
    delete userForm.value[`email${index}`];
    delete userForm.value[`phone${index}`];
    userForm.value.list.splice(i, 1);
  }

  /**
   * 校验用户姓名
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   */
  function checkUerName(value: string | undefined, callback: (error?: string) => void) {
    if (value === '' || value === undefined) {
      callback(t('system.user.createUserNameNotNull'));
    } else if (value.length > 50) {
      callback(t('system.user.createUserNameOverLength'));
    }
  }

  /**
   * 校验用户邮箱
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   * @param index 当前输入的表单项对应 list 的下标，用于校验重复输入的时候排除自身
   */
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

  /**
   * 校验用户手机号
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   */
  function checkUerPhone(value: string | undefined, callback: (error?: string) => void) {
    if (value !== '' && value !== undefined && !validatePhone(value)) {
      callback(t('system.user.createUserPhoneErr'));
    }
  }

  /**
   * 取消创建，重置用户表单
   */
  function cancelCreate() {
    visible.value = false;
    userFormRef.value?.resetFields();
    userForm.value = cloneDeep(defaulUserForm);
  }

  async function updateUser() {
    // const params = {
    //   userInfoList: [
    //     {
    //       name: userForm.value.username0,
    //       email: userForm.value.email0,
    //       phone: userForm.value.phone0,
    //     },
    //   ],
    //   userRoleIdList: userForm.value.userGroup,
    // };
    // await updateUserInfo(params);
  }

  async function createUser() {
    const params = {
      userInfoList: userForm.value.list.map((item: number) => {
        return {
          name: userForm.value[`username${item}`],
          email: userForm.value[`email${item}`],
          phone: userForm.value[`phone${item}`],
        };
      }),
      userRoleIdList: [],
    };
    await batchCreateUser(params);
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
      await createUser();
      userFormRef.value?.resetFields();
      userForm.value = cloneDeep(defaulUserForm);
    });
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
      userForm.value.username0 = record.name;
      userForm.value.email0 = record.email;
      userForm.value.phone0 = record.phone;
      userForm.value.userGroup = record.userRoleList.map((e) => e.name);
    }
  }

  const inviteVisible = ref(false);
  function showEmailInviteModal() {
    inviteVisible.value = true;
  }

  const importVisible = ref(false);
  const importLoading = ref(false);
  const importResultVisible = ref(false);
  const importSuccessCount = ref(0);
  const importFailCount = ref(0);
  const importErrorFileUrl = ref('');
  const importResult = ref<'success' | 'allFail' | 'fail'>('success');
  const importResultTitle = ref(t('system.user.importSuccessTitle'));

  function showImportModal() {
    importVisible.value = true;
  }

  function cancelImport() {
    importVisible.value = false;
    importResultVisible.value = false;
  }

  /**
   * 根据导入结果展示结果弹窗
   */
  function showImportResult() {
    importLoading.value = false;
    importVisible.value = false;
    switch (importResult.value) {
      case 'success':
        importResultTitle.value = t('system.user.importSuccessTitle');
        break;
      case 'allFail':
        importResultTitle.value = t('system.user.importAllfailTitle');
        break;
      case 'fail':
        importResultTitle.value = t('system.user.importFailTitle');
        break;
      default:
        break;
    }
    importResultVisible.value = true;
  }

  function continueImport() {
    importResultVisible.value = false;
    importVisible.value = true;
  }

  function importUser() {
    importResult.value = 'fail';
    importSuccessCount.value = 100;
    importFailCount.value = 20;
    showImportResult();
  }
</script>

<style lang="less" scoped></style>
@/models/setting/user @/api/modules/setting/user
