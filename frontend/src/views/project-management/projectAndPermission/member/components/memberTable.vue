<template>
  <div>
    <div class="mb-[16px] grid grid-cols-4 gap-2">
      <div class="col-span-2">
        <a-button v-permission="['PROJECT_USER:READ+ADD']" class="mr-3" type="primary" @click="addMember">
          {{ t('project.member.addMember') }}
        </a-button>
        <a-button v-permission="['PROJECT_USER:READ+INVITE']" type="outline" class="mr-3" @click="inviteVisible = true">
          {{ t('system.user.emailInvite') }}
        </a-button>
      </div>
      <div>
        <a-select v-model="roleIds" @change="changeSelect">
          <a-option v-for="item of userGroupAll" :key="item.id" :value="item.id">{{ t(item.name) }}</a-option>
          <template #prefix>
            <span>{{ t('project.member.tableColumnUserGroup') }}</span>
          </template>
        </a-select>
      </div>
      <div>
        <a-input-search
          v-model="keyword"
          :max-length="255"
          :placeholder="t('project.member.searchMember')"
          allow-clear
          @search="searchHandler"
          @press-enter="searchHandler"
          @clear="searchHandler"
        >
        </a-input-search>
      </div>
    </div>
    <MsBaseTable
      v-bind="propsRes"
      :action-config="tableBatchActions"
      :selectable="hasAnyPermission(['PROJECT_USER:READ+UPDATE', 'PROJECT_USER:READ+DELETE'])"
      @selected-change="handleTableSelect"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #userRoles="{ record }">
        <MsTagGroup
          v-if="!record.showUserSelect"
          :tag-list="record.userRoles || []"
          type="primary"
          theme="outline"
          allow-edit
          show-table
          @click="changeUser(record)"
        />
        <MsSelect
          v-else
          v-model:model-value="record.selectUserList"
          v-model:loading="dialogLoading"
          :max-tag-count="1"
          class="w-full"
          :options="userGroupOptions"
          :search-keys="['name']"
          value-key="id"
          label-key="name"
          allow-search
          :multiple="true"
          :placeholder="t('common.pleaseSelect')"
          :at-least-one="true"
          :fallback-option="
              (val:any) => ({
                label: userGroupOptions.find((e) => e.id === val)?.name || (val as string),
                value: val,
              })
            "
          @popup-visible-change="(value:boolean) => userGroupChange(value as boolean, record)"
        />
      </template>
      <template #enable="{ record }">
        <div v-if="record.enable" class="flex items-center">
          <icon-check-circle-fill class="mr-[2px] text-[rgb(var(--success-6))]" />
          {{ t('organization.member.statusEnable') }}
        </div>
        <div v-else class="flex items-center text-[var(--color-text-4)]">
          <MsIcon type="icon-icon_disable" class="mr-[2px]" />
          {{ t('organization.member.statusDisable') }}
        </div>
      </template>
      <template #operation="{ record }">
        <MsRemoveButton
          v-permission="['PROJECT_USER:READ+DELETE']"
          position="br"
          :title="t('project.member.deleteMemberTip', { name: characterLimit(record.name) })"
          :sub-title-tip="t('project.member.subTitle')"
          :loading="deleteLoading"
          @ok="removeMember(record)"
        />
      </template>
    </MsBaseTable>
  </div>
  <AddMemberModal
    ref="projectMemberRef"
    v-model:visible="addMemberVisible"
    :user-group-options="userGroupOptions"
    @success="initData()"
  />
  <MsBatchModal
    ref="batchModalRef"
    v-model:visible="batchVisible"
    :action="batchAction"
    :current-select-count="batchParams.currentSelectCount"
    @add-user-group="addUserGroup"
  />
  <inviteModal v-model:visible="inviteVisible" :user-group-options="userGroupOptions" range="project"></inviteModal>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import { isEqual } from 'lodash-es';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsBatchModal from '@/components/business/ms-batch-modal/index.vue';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import MsSelect from '@/components/business/ms-select';
  import AddMemberModal from './addMemberModal.vue';
  import inviteModal from '@/views/setting/system/components/inviteModal.vue';

  import {
    addOrUpdateProjectMember,
    addProjectUserGroup,
    batchRemoveMember,
    getProjectMemberList,
    getProjectUserGroup,
    removeProjectMember,
  } from '@/api/modules/project-management/projectMember';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit, formatPhoneNumber } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { TableQueryParams } from '@/models/common';
  import {
    ActionProjectMember,
    ProjectMemberItem,
    ProjectUserOption,
  } from '@/models/projectManagement/projectAndPermission';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();
  const tableStore = useTableStore();

  const lastProjectId = computed(() => appStore.currentProjectId);
  const hasOperationPermission = computed(() => hasAnyPermission(['PROJECT_USER:READ+DELETE']));
  const columns: MsTableColumn = [
    {
      title: 'system.user.userName',
      dataIndex: 'email',
      showTooltip: true,
      sortIndex: 0,
      showDrag: false,
    },
    {
      title: 'project.member.tableColumnName',
      slotName: 'name',
      dataIndex: 'name',
      showTooltip: true,
      showDrag: false,
    },
    {
      title: 'project.member.tableColumnEmail',
      slotName: 'email',
      dataIndex: 'email',
      showTooltip: true,
      showDrag: false,
    },
    {
      title: 'project.member.tableColumnPhone',
      slotName: 'phone',
      dataIndex: 'phone',
      showDrag: true,
      width: 150,
    },
    {
      title: 'project.member.tableColumnUserGroup',
      slotName: 'userRoles',
      dataIndex: 'userRoles',
      showDrag: true,
      isTag: true,
      allowEditTag: true,
      width: 300,
    },
    {
      title: 'project.member.tableColumnStatus',
      slotName: 'enable',
      dataIndex: 'enable',
      showDrag: true,
      width: 150,
    },
    {
      title: hasOperationPermission.value ? 'project.member.tableColumnActions' : '',
      slotName: 'operation',
      fixed: 'right',
      dataIndex: 'operation',
      width: hasOperationPermission.value ? 120 : 50,
      showDrag: false,
    },
  ];

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.member.batchActionAddUserGroup',
        eventTag: 'batchAddUserGroup',
        permission: ['PROJECT_USER:READ+UPDATE'],
      },
      {
        label: 'project.member.batchActionRemove',
        eventTag: 'batchActionRemove',
        permission: ['PROJECT_USER:READ+DELETE'],
      },
    ],
  };

  const tableSelected = ref<(string | number)[]>([]);

  const handleTableSelect = (selectArr: (string | number)[]) => {
    tableSelected.value = selectArr;
  };

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getProjectMemberList,
    {
      tableKey: TableKeyEnum.PROJECT_MEMBER,
      selectable: hasAnyPermission(['ORGANIZATION_MEMBER:READ+UPDATE']),
      showSetting: true,
      heightUsed: 288,
      scroll: {
        x: 1200,
      },
    },
    (record) => {
      return {
        ...record,
        phone: formatPhoneNumber(record.phone || ''),
      };
    }
  );

  const userGroupAll = ref<ProjectUserOption[]>([]);
  const userGroupOptions = ref<ProjectUserOption[]>([]);

  const initOptions = async () => {
    userGroupOptions.value = await getProjectUserGroup(appStore.currentProjectId);
    userGroupAll.value = [
      {
        id: '',
        name: '全部',
      },
      ...userGroupOptions.value,
    ];
  };

  const roleIds = ref<string>('');
  const keyword = ref<string>('');

  const initData = async () => {
    await nextTick();
    setLoadListParams({
      filter: {
        roleIds: roleIds.value ? [roleIds.value] : [],
      },
      projectId: lastProjectId.value,
      keyword: keyword.value,
    });
    await loadList();
  };

  const searchHandler = () => {
    initData();
  };

  const changeSelect = () => {
    initData();
  };

  // 跨页选择项
  // const selectData = ref<string[] | undefined>([]);
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  // 批量移除项目成员
  const batchRemoveHandler = () => {
    const { selectedIds, excludeIds, selectAll } = batchParams.value;
    openModal({
      type: 'error',
      title: t('project.member.batchRemoveTip', { number: batchParams.value.currentSelectCount }),
      content: t('project.member.batchRemoveContent'),
      okText: t('project.member.deleteMemberConfirm'),
      cancelText: t('project.member.Cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const params: TableQueryParams = {
            selectAll: !!selectAll,
            excludeIds: excludeIds || [],
            selectIds: selectedIds || [],
            projectId: lastProjectId.value,
            keyword: keyword.value,
            condition: {
              keyword: keyword.value,
              filter: {
                ...propsRes.value.filter,
                roleIds: roleIds.value ? [roleIds.value] : [],
              },
              combine: batchParams.value.condition,
            },
          };
          await batchRemoveMember(params);
          Message.success(t('project.member.deleteMemberSuccess'));
          loadList();
          resetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  // 移除项目成员
  const deleteLoading = ref<boolean>(false);

  const removeMember = async (record: ProjectMemberItem) => {
    deleteLoading.value = true;
    try {
      if (lastProjectId.value && record.id) {
        await removeProjectMember(lastProjectId.value, record.id);
        Message.success(t('project.member.deleteMemberSuccess'));
        initData();
        resetSelector();
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      deleteLoading.value = false;
    }
  };

  const batchVisible = ref<boolean>(false);
  const batchAction = ref('');
  const batchModalRef = ref();
  // 添加到用户组
  const addUserGroup = async (target: string[]) => {
    try {
      const { selectedIds, excludeIds, selectAll } = batchParams.value;
      const params = {
        projectId: lastProjectId.value,
        userIds: batchParams.value.selectedIds || [],
        selectAll: !!selectAll,
        excludeIds: excludeIds || [],
        selectIds: selectedIds || [],
        roleIds: target,
        condition: {
          keyword: keyword.value,
          filter: {
            ...propsRes.value.filter,
            roleIds: roleIds.value ? [roleIds.value] : [],
          },
          combine: batchParams.value.condition,
        },
      };
      await batchModalRef.value.batchRequestFun(addProjectUserGroup, params);
      resetSelector();
      initData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 表格批量处理
  const handleTableBatch = (event: BatchActionParams, params: BatchActionQueryParams) => {
    batchParams.value = params;
    if (event.eventTag === 'batchActionRemove') {
      batchRemoveHandler();
    }
    if (event.eventTag === 'batchAddUserGroup') {
      batchVisible.value = true;
      batchAction.value = event.eventTag;
      batchModalRef.value.getTreeList(getProjectUserGroup, lastProjectId.value);
    }
  };

  // 添加项目成员
  const addMemberVisible = ref<boolean>(false);
  const projectMemberRef = ref();

  const addMember = () => {
    addMemberVisible.value = true;
    projectMemberRef.value.initProjectMemberOptions();
  };
  const dialogLoading = ref(false);

  // 编辑项目成员
  const editProjectMember = async (record: ProjectMemberItem) => {
    try {
      dialogLoading.value = true;
      const params: ActionProjectMember = {
        projectId: lastProjectId.value,
        userId: record.id,
        roleIds: record.selectUserList,
      };
      await addOrUpdateProjectMember(params);
      Message.success(t('project.member.batchUpdateSuccess'));
      record.showUserSelect = false;
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      dialogLoading.value = false;
    }
  };

  // 项目用户组改变回调
  const changeUser = (record: ProjectMemberItem) => {
    if (!hasAnyPermission(['PROJECT_USER:READ+UPDATE'])) {
      return;
    }
    if (record.enable) {
      record.showUserSelect = true;
      record.selectUserList = (record.userRoles || []).map((item) => item.id);
    }
  };

  // 面板改变的回调
  const userGroupChange = (visible: boolean, record: ProjectMemberItem) => {
    if (visible) {
      return;
    }
    if ((record.selectUserList || []).length < 1) {
      Message.warning(t('project.member.selectUserEmptyTip'));
      return;
    }
    const userGroupIds = (record.userRoles || []).map((item: any) => item.id);
    if (isEqual(userGroupIds, record.selectUserList)) {
      record.showUserSelect = false;
      return;
    }
    editProjectMember(record);
  };

  const inviteVisible = ref(false);

  onBeforeMount(async () => {
    await initOptions();
    initData();
  });

  await tableStore.initColumn(TableKeyEnum.PROJECT_MEMBER, columns, 'drawer');
</script>

<style lang="less" scoped></style>
