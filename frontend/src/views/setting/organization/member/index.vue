<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button class="mr-3" type="primary" @click="addOrEditMember('add')">{{
          t('organization.member.addMember')
        }}</a-button>
      </div>
      <a-input-search
        v-model="keyword"
        :max-length="250"
        allow-clear
        :placeholder="t('organization.member.searchMember')"
        class="w-[230px]"
        @search="searchHandler"
        @press-enter="searchHandler"
      ></a-input-search>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="tableBatchActions"
      @selected-change="handleTableSelect"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #project="{ record }">
        <MsTagGroup
          v-if="!record.showProjectSelect"
          :tag-list="record.projectIdNameMap || []"
          theme="outline"
          @click="changeUserOrProject(record, 'project')"
        >
        </MsTagGroup>
        <a-select
          v-else
          v-model="record.selectProjectList"
          multiple
          :max-tag-count="2"
          size="small"
          @change="(value) => selectUserOrProject(value, record, 'project')"
          @popup-visible-change="visibleChange($event, record, 'project')"
        >
          <a-option v-for="item of projectOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
        </a-select>
      </template>
      <template #userRole="{ record }">
        <MsTagGroup
          v-if="!record.showUserSelect"
          :tag-list="record.userRoleIdNameMap || []"
          type="primary"
          theme="outline"
          @click="changeUserOrProject(record, 'user')"
        >
        </MsTagGroup>
        <a-select
          v-else
          v-model="record.selectUserList"
          multiple
          :max-tag-count="2"
          @change="(value) => selectUserOrProject(value, record, 'user')"
          @popup-visible-change="(value) => visibleChange(value, record, 'user')"
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
        <MsButton @click="addOrEditMember('edit', record)">{{ t('organization.member.edit') }}</MsButton>
        <MsRemoveButton
          position="br"
          :title="t('organization.member.deleteMemberTip', { name: characterLimit(record.name) })"
          :sub-title-tip="t('organization.member.subTitle')"
          @ok="deleteMember(record)"
        />
      </template>
    </ms-base-table>
  </MsCard>
  <AddMemberModal
    ref="AddMemberRef"
    v-model:visible="addMemberVisible"
    :project-list="projectOptions"
    :user-group-options="userGroupOptions"
    @success="initData()"
  />
  <MSBatchModal
    ref="batchModalRef"
    v-model:visible="showBatchModal"
    :table-selected="tableSelected"
    :action="batchAction"
    :select-data="selectData"
    @add-project="addProjectOrAddUserGroup"
    @add-user-group="addProjectOrAddUserGroup"
  />
</template>

<script setup lang="ts">
  import { onBeforeMount, ref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { Message } from '@arco-design/web-vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import AddMemberModal from './components/addMemberModal.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import {
    getMemberList,
    deleteMemberReq,
    addOrUpdate,
    batchAddProject,
    batchAddUserGroup,
    getProjectList,
    getGlobalUserGroup,
  } from '@/api/modules/setting/member';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import MSBatchModal from '@/components/business/ms-batch-modal/index.vue';
  import { useTableStore, useUserStore } from '@/store';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import type { MemberItem, AddorUpdateMemberModel, LinkList, BatchAddProjectModel } from '@/models/setting/member';
  import { characterLimit } from '@/utils';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';

  const tableStore = useTableStore();
  const { t } = useI18n();
  const userStore = useUserStore();
  const lastOrganizationId = userStore.$state?.lastOrganizationId;

  const columns: MsTableColumn = [
    {
      title: 'organization.member.tableColunmEmail',
      dataIndex: 'email',
      width: 200,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: 'organization.member.tableColunmName',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: 'organization.member.tableColunmPhone',
      dataIndex: 'phone',
      showInTable: true,
      width: 200,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: 'organization.member.tableColunmPro',
      slotName: 'project',
      dataIndex: 'projectIdNameMap',
      showInTable: true,
      isTag: true,
    },
    {
      title: 'organization.member.tableColunmUsergroup',
      slotName: 'userRole',
      dataIndex: 'userRoleIdNameMap',
      showInTable: true,
      isTag: true,
    },
    {
      title: 'organization.member.tableColunmStatus',
      slotName: 'enable',
      dataIndex: 'enable',
      showInTable: true,
      width: 200,
    },
    {
      title: 'organization.member.tableColunmActions',
      slotName: 'action',
      fixed: 'right',
      width: 140,
      showInTable: true,
    },
  ];
  tableStore.initColumn(TableKeyEnum.ORGANNATIONMEMBER, columns, 'drawer');

  const tableBatchActions = {
    baseAction: [
      {
        label: 'organization.member.batchActionAddProject',
        eventTag: 'batchAddProject',
      },
      {
        label: 'organization.member.batchActionAddUserGroup',
        eventTag: 'batchAddUserGroup',
      },
    ],
  };
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getMemberList, {
    tableKey: TableKeyEnum.ORGANNATIONMEMBER,
    scroll: { x: 2000 },
    selectable: true,
    showSetting: true,
    size: 'default',
  });
  const keyword = ref('');
  const tableSelected = ref<(string | number)[]>([]);
  const selectData = ref<string[]>([]);

  const initData = async () => {
    setLoadListParams({ keyword: keyword.value, organizationId: lastOrganizationId });
    await loadList();
  };
  const searchHandler = () => {
    initData();
  };

  const addMemberVisible = ref<boolean>(false);
  const AddMemberRef = ref();

  const addOrEditMember = (type: string, record: AddorUpdateMemberModel = {}) => {
    addMemberVisible.value = true;
    AddMemberRef.value.type = type;
    if (type === 'edit') {
      AddMemberRef.value.edit(record);
    }
  };
  const deleteMember = async (record: MemberItem) => {
    try {
      if (lastOrganizationId) await deleteMemberReq(lastOrganizationId, record.id);
      Message.success(t('organization.member.deleteMemberSuccess'));
      initData();
    } catch (error) {
      console.log(error);
    }
  };
  const handleTableSelect = (selectArr: (string | number)[]) => {
    tableSelected.value = selectArr;
  };

  const batchModalRef = ref();

  const getData = (callBack: any) => {
    batchModalRef.value.getTreeList(callBack, lastOrganizationId);
  };

  const showBatchModal = ref(false);

  const batchList = [
    {
      type: 'project',
      request: batchAddProject,
    },
    {
      type: 'userGroup',
      request: batchAddUserGroup,
    },
  ];

  const batchAction = ref('');

  // 添加到项目和用户组
  const addProjectOrAddUserGroup = async (target: string[], type: string) => {
    const currentType = batchList.find((item) => item.type === type);
    const params: BatchAddProjectModel = {
      organizationId: lastOrganizationId,
      memberIds: tableSelected.value,
    };
    if (type === 'project') {
      params.projectIds = target;
    } else {
      params.userRoleIds = target;
    }
    if (currentType) await batchModalRef.value.batchRequestFun(currentType.request, params);
    loadList();
  };
  // 批量操作
  const handleTableBatch = (actionItem: any) => {
    showBatchModal.value = true;
    batchAction.value = actionItem.eventTag;
    if (actionItem.eventTag === 'batchAddProject') getData(getProjectList);
    if (actionItem.eventTag === 'batchAddUserGroup') getData(getGlobalUserGroup);
  };
  // 列表编辑更新用户组和项目
  const updateUserOrProject = async (record: MemberItem) => {
    try {
      const params = {
        organizationId: lastOrganizationId,
        projectIds: [...record.selectProjectList],
        userRoleIds: [...record.selectUserList],
        memberId: record.id,
      };
      await addOrUpdate(params, 'edit');
      Message.success(t('organization.member.batchUpdateSuccess'));
      initData();
    } catch (error) {
      console.log(error);
    } finally {
      record.showUserSelect = false;
      record.showProjectSelect = false;
    }
  };
  // 编辑模式和下拉选择切换
  const changeUserOrProject = (record: MemberItem, type: string) => {
    if (!record.enable) {
      return;
    }
    if (type === 'project') {
      record.showProjectSelect = true;
      record.showUserSelect = false;
    } else {
      record.showUserSelect = true;
      record.showProjectSelect = false;
    }
    record.selectProjectList = (record.projectIdNameMap || []).map((item) => item.id);
    record.selectUserList = (record.userRoleIdNameMap || []).map((item) => item.id);
  };
  // 用户和项目选择改变的回调
  const selectUserOrProject = (value: any, record: MemberItem, type: string) => {
    if (type === 'project') {
      record.selectProjectList = value;
    } else {
      record.selectUserList = value;
    }
  };
  // 面板切换的回调
  const visibleChange = (visible: boolean, record: MemberItem, type: string) => {
    if (!visible) {
      if (type === 'user' && record.selectUserList.length < 1) {
        Message.warning(t('organization.member.selectUserEmptyTip'));
        return;
      }
      updateUserOrProject(record);
    }
  };

  const userGroupOptions = ref<LinkList>([]);
  const projectOptions = ref<LinkList>([]);
  const getLinkList = async () => {
    if (lastOrganizationId) {
      userGroupOptions.value = await getGlobalUserGroup(lastOrganizationId);
      projectOptions.value = await getProjectList(lastOrganizationId);
    }
  };
  onBeforeMount(() => {
    initData();
    getLinkList();
  });
</script>

<style lang="less" scoped></style>
