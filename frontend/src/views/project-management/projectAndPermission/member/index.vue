<template>
  <div class="mb-4 grid grid-cols-4 gap-2">
    <div class="col-span-2"
      ><a-button v-permission="['PROJECT_USER:READ+ADD']" class="mr-3" type="primary" @click="addMember">{{
        t('project.member.addMember')
      }}</a-button></div
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
  <MsBaseTable
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
  <AddMemberModal
    ref="projectMemberRef"
    v-model:visible="addMemberVisible"
    :user-group-options="userGroupOptions"
    @success="loadList()"
  />
  <MsBatchModal
    ref="batchModalRef"
    v-model:visible="batchVisible"
    :action="batchAction"
    :select-data="selectData"
    @add-user-group="addUserGroup"
  />
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限-成员
   */
  import { onMounted, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsBatchModal from '@/components/business/ms-batch-modal/index.vue';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import AddMemberModal from './components/addMemberModal.vue';

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
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type {
    ActionProjectMember,
    ProjectMemberItem,
    ProjectUserOption,
    SearchParams,
  } from '@/models/projectManagement/projectAndPermission';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const { openModal } = useModal();
  const appStore = useAppStore();
  const tableStore = useTableStore();
  const lastProjectId = computed(() => appStore.currentProjectId);

  const columns: MsTableColumn = [
    {
      title: 'project.member.tableColumnName',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      sortIndex: 0,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'project.member.tableColumnEmail',
      dataIndex: 'email',
      showInTable: true,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'project.member.tableColumnPhone',
      dataIndex: 'phone',
      showInTable: true,
      showDrag: true,
      width: 150,
    },
    {
      title: 'project.member.tableColumnUserGroup',
      slotName: 'userRole',
      dataIndex: 'userRoleIdNameMap',
      showInTable: true,
      showDrag: true,
      width: 300,
    },
    {
      title: 'project.member.tableColumnStatus',
      slotName: 'enable',
      dataIndex: 'enable',
      showInTable: true,
      width: 150,
    },
    {
      title: 'project.member.tableColumnActions',
      slotName: 'operation',
      fixed: 'right',
      dataIndex: 'operation',
      width: 100,
      showInTable: true,
      showDrag: false,
    },
  ];

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.member.batchActionAddUserGroup',
        eventTag: 'batchAddUserGroup',
        permission: ['PROJECT_USER:READ+ADD'],
      },
      {
        label: 'project.member.batchActionRemove',
        eventTag: 'batchActionRemove',
        permission: ['PROJECT_USER:READ+ADD'],
      },
    ],
  };

  const tableSelected = ref<(string | number)[]>([]);

  const handleTableSelect = (selectArr: (string | number)[]) => {
    tableSelected.value = selectArr;
  };

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getProjectMemberList, {
    tableKey: TableKeyEnum.PROJECT_MEMBER,
    selectable: true,
    showSetting: true,
    heightUsed: 288,
    columns,
    scroll: {
      x: 1200,
    },
  });

  const searchParams = ref<SearchParams>({
    filter: {
      roleIds: [],
    },
    projectId: lastProjectId.value,
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
            projectId: lastProjectId.value,
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
      if (lastProjectId.value && record.id) {
        await removeProjectMember(lastProjectId.value, record.id);
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
      projectId: lastProjectId.value,
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

  // 编辑项目成员
  const editProjectMember = async (record: ProjectMemberItem) => {
    const params: ActionProjectMember = {
      projectId: lastProjectId.value,
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
    editProjectMember(record);
  };

  const userGroupAll = ref<ProjectUserOption[]>([]);

  const initOptions = async () => {
    userGroupOptions.value = await getProjectUserGroup(lastProjectId.value);
    userGroupAll.value = [
      {
        id: '',
        name: '全部',
      },
      ...userGroupOptions.value,
    ];
  };

  onMounted(() => {
    initData();
    initOptions();
  });
  tableStore.initColumn(TableKeyEnum.PROJECT_MEMBER, columns, 'drawer');
</script>

<style scoped></style>
