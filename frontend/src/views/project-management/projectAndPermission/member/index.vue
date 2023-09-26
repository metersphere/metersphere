<template>
  <div class="mb-4 grid grid-cols-4 gap-2">
    <div class="col-span-2"
      ><a-button class="mr-3" type="primary" @click="addMember">{{ t('project.member.addMember') }}</a-button></div
    >
    <div>
      <a-select v-model="roleIds" @change="changeSelect">
        <a-option v-for="item of userGroupAll" :key="item.id" :value="item.id">{{ t(item.name) }}</a-option>
        <template #prefix
          ><span>{{ t('project.member.tableColumnUserGroup') }}</span></template
        >
      </a-select></div
    >
    <div>
      <a-input-search
        v-model="searchParams.keyword"
        :max-length="250"
        :placeholder="t('project.member.searchMember')"
        allow-clear
        @search="searchHandler"
        @press-enter="searchHandler"
      ></a-input-search
    ></div>
  </div>
  <ms-base-table
    v-bind="propsRes"
    :action-config="tableBatchActions"
    @selected-change="handleTableSelect"
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
    <template #userRole="{ record }">
      <MsTagGroup
        v-if="!record.showUserSelect"
        :tag-list="record.userRoles || []"
        type="primary"
        theme="outline"
        @click="changeUser(record)"
      />
      <a-select
        v-else
        v-model="record.selectUserList"
        multiple
        class="w-[260px]"
        :max-tag-count="2"
        @popup-visible-change="(value) => userGroupChange(value, record)"
      >
        <a-option v-for="item of userGroupOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
      </a-select>
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
    <template #action="{ record }">
      <MsRemoveButton
        position="br"
        :title="t('project.member.deleteMemberTip', { name: characterLimit(record.name) })"
        :sub-title-tip="t('project.member.subTitle')"
        :loading="deleteLoading"
        @ok="removeMember(record)"
      />
    </template>
  </ms-base-table>
  <AddMemberModal
    ref="projectMemberRef"
    v-model:visible="addMemberVisible"
    :user-group-options="userGroupOptions"
    @success="loadList()"
  />
  <MSBatchModal
    ref="batchModalRef"
    v-model:visible="batchVisible"
    :action="batchAction"
    :select-data="selectData"
    @add-user-group="addUserGroup"
  />
</template>

<script setup lang="ts">
  import { ref, onBeforeMount } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import {
    getProjectMemberList,
    getProjectUserGroup,
    addOrUpdateProjectMember,
    batchRemoveMember,
    removeProjectMember,
    addProjectUserGroup,
  } from '@/api/modules/project-management/projectMember';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { useTableStore, useUserStore } from '@/store';
  import useModal from '@/hooks/useModal';
  import type { MsTableColumn, BatchActionParams, BatchActionQueryParams } from '@/components/pure/ms-table/type';
  import { characterLimit } from '@/utils';
  import AddMemberModal from './components/addMemberModal.vue';
  import MSBatchModal from '@/components/business/ms-batch-modal/index.vue';
  import { Message } from '@arco-design/web-vue';
  import type {
    ProjectUserOption,
    ActionProjectMember,
    ProjectMemberItem,
    SearchParams,
  } from '@/models/projectManagement/projectAndPermission';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';

  const { t } = useI18n();
  const { openModal } = useModal();

  const tableStore = useTableStore();
  const userStore = useUserStore();
  const lastProjectId = userStore?.$state?.lastProjectId;

  const columns: MsTableColumn = [
    {
      title: 'project.member.tableColumnEmail',
      dataIndex: 'email',
      showInTable: true,
      width: 200,
      showTooltip: true,
    },
    {
      title: 'project.member.tableColumnName',
      dataIndex: 'name',
      showInTable: true,
      width: 200,
      showTooltip: true,
    },
    {
      title: 'project.member.tableColumnPhone',
      dataIndex: 'phone',
      showInTable: true,
      width: 150,
    },
    {
      title: 'project.member.tableColumnUserGroup',
      slotName: 'userRole',
      dataIndex: 'userRoleIdNameMap',
      showInTable: true,
      width: 300,
    },
    {
      title: 'project.member.tableColumnStatus',
      slotName: 'enable',
      dataIndex: 'enable',
      showInTable: true,
    },
    {
      title: 'project.member.tableColumnActions',
      slotName: 'action',
      fixed: 'right',
      width: 100,
      showInTable: true,
    },
  ];
  tableStore.initColumn(TableKeyEnum.PROJECT_MEMBER, columns, 'drawer');

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.member.batchActionAddUserGroup',
        eventTag: 'batchAddUserGroup',
      },
      {
        label: 'project.member.batchActionRemove',
        eventTag: 'batchActionRemove',
      },
    ],
  };

  const tableSelected = ref<(string | number)[]>([]);

  const handleTableSelect = (selectArr: (string | number)[]) => {
    tableSelected.value = selectArr;
    console.log(selectArr, 'selectArrselectArrselectArr');
  };

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getProjectMemberList, {
    tableKey: TableKeyEnum.PROJECT_MEMBER,
    selectable: true,
    showSetting: true,
    size: 'default',
    scroll: {
      x: 1200,
    },
  });

  const searchParams = ref<SearchParams>({
    filter: {
      roleIds: [],
    },
    projectId: lastProjectId as string,
    keyword: '',
  });

  const roleIds = ref<string>('');
  const initData = async () => {
    setLoadListParams({ ...searchParams.value });
    await loadList();
  };

  const searchHandler = () => {
    initData();
  };

  const changeSelect = () => {
    searchParams.value.filter.roleIds = roleIds.value ? [roleIds.value] : [];
    initData();
  };

  // 跨页选择项
  const selectData = ref<string[] | undefined>([]);

  // 批量移除项目成员
  const batchRemoveHandler = () => {
    openModal({
      type: 'error',
      title: t('project.member.batchRemoveTip', { number: (selectData.value || []).length }),
      content: t('project.member.batchRemoveContent'),
      okText: t('project.member.deleteMemberConfirm'),
      cancelText: t('project.member.Cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const params: ActionProjectMember = {
            projectId: lastProjectId,
            userIds: selectData.value,
          };
          await batchRemoveMember(params);
          Message.success(t('project.member.deleteMemberSuccess'));
          loadList();
          resetSelector();
        } catch (error) {
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
      if (lastProjectId && record.id) {
        await removeProjectMember(lastProjectId, record.id);
        Message.success(t('project.member.deleteMemberSuccess'));
        loadList();
        resetSelector();
      }
    } catch (error) {
      console.log(error);
    } finally {
      deleteLoading.value = false;
    }
  };

  const batchVisible = ref<boolean>(false);
  const batchAction = ref('');
  const userGroupOptions = ref<ProjectUserOption[]>([]);
  const batchModalRef = ref();

  // 添加到用户组
  const addUserGroup = async (target: string[]) => {
    const params = {
      projectId: lastProjectId,
      userIds: selectData.value,
      roleIds: target,
    };
    try {
      await batchModalRef.value.batchRequestFun(addProjectUserGroup, params);
      loadList();
      resetSelector();
    } catch (error) {
      console.log(error);
    }
  };

  // 表格批量处理
  const handleTableBatch = (event: BatchActionParams, params: BatchActionQueryParams) => {
    selectData.value = params.selectedIds;
    if (event.eventTag === 'batchActionRemove') {
      batchRemoveHandler();
    }
    if (event.eventTag === 'batchAddUserGroup') {
      batchVisible.value = true;
      batchAction.value = event.eventTag;
      batchModalRef.value.getTreeList(getProjectUserGroup, lastProjectId);
    }
  };

  // 添加项目成员
  const addMemberVisible = ref<boolean>(false);
  const projectMemberRef = ref();

  const addMember = () => {
    addMemberVisible.value = true;
    projectMemberRef.value.initProjectMemberOptions();
  };

  // 编辑项目成员
  const editProjectMember = async (record: ProjectMemberItem) => {
    const params: ActionProjectMember = {
      projectId: lastProjectId,
      userId: record.id,
      roleIds: record.selectUserList,
    };
    try {
      await addOrUpdateProjectMember(params);
      Message.success(t('project.member.batchUpdateSuccess'));
      record.showUserSelect = false;
      loadList();
    } catch (error) {
      console.log(error);
    }
  };

  // 项目用户组改变回调
  const changeUser = (record: ProjectMemberItem) => {
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
    editProjectMember(record);
  };

  const userGroupAll = ref<ProjectUserOption[]>([]);

  onBeforeMount(async () => {
    initData();
    userGroupOptions.value = await getProjectUserGroup(lastProjectId as string);
    userGroupAll.value = [
      {
        id: '',
        name: '全部',
      },
      ...userGroupOptions.value,
    ];
  });
</script>

<style scoped></style>
