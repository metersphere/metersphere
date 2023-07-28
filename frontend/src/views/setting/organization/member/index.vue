<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button class="mr-3" type="primary" @click="addMember">{{ t('organization.member.addMember') }}</a-button>
      </div>
      <a-input-search
        v-model="keyword"
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
      @batch-action="handelTableBatch"
    >
      <template #project="{ record }">
        <div v-if="!record.showProjectSelect">
          <!-- <a-tag
            v-for="pro of record.projectIdNameMap"
            :key="pro.id"
            class="mr-[4px] bg-transparent"
            bordered
            @click="changeUserOrProject(record, 'project')"
          > -->
          <a-tag
            v-for="pro of 3"
            :key="pro"
            class="mr-[4px] bg-transparent"
            bordered
            @click="changeUserOrProject(record, 'project')"
          >
            {{ pro }}
          </a-tag>
        </div>
        <a-select
          v-else
          v-model="record.selectProjectList"
          multiple
          :max-tag-count="2"
          size="small"
          @change="(value) => selectUserOrProject(value, record, 'project')"
          @popup-visible-change="visibleChange($event, record, 'project')"
        >
          <a-option v-for="item of projectList" :key="item.id" :value="item.id">{{ item.name }}</a-option>
        </a-select>
      </template>
      <template #userRole="{ record }">
        <div v-if="!record.showUserSelect">
          <a-tag
            v-for="org of Object.keys(record.userRoleIdNameMap).slice(0, 3)"
            :key="org"
            class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
            bordered
            @click="changeUserOrProject(record, 'user')"
          >
            {{ record.userRoleIdNameMap[org] }}
          </a-tag>
          <a-tag
            v-if="Object.keys(record.userRoleIdNameMap).length > 3"
            class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
            bordered
            @click="changeUserOrProject(record, 'user')"
          >
            +{{ Object.keys(record.userRoleIdNameMap).length - 3 }}
          </a-tag>
        </div>

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
          {{ t('organization.member.tableEnable') }}
        </div>
        <div v-else class="flex items-center text-[var(--color-text-4)]">
          <icon-stop class="mr-[2px]" />
          {{ t('organization.member.tableDisable') }}
        </div>
      </template>
      <template #action="{ record }">
        <MsButton @click="editMember(record)">{{ t('organization.member.edit') }}</MsButton>
        <MsButton @click="deleteMember(record)">{{ t('organization.member.remove') }}</MsButton>
      </template>
    </ms-base-table>
  </MsCard>
  <AddMemberModal
    ref="AddMemberRef"
    v-model:visible="addMemberVisible"
    :member-list="memberList"
    :project-list="projectList"
    :user-group-options="userGroupOptions"
    @success="handlerOk"
  />
  <MSBatchModal
    v-if="treeData.length > 0"
    v-model:visible="showBatchModal"
    :table-selected="tableSelected"
    :action="batchAction"
    :tree-data="treeData"
    @add-project="addProject"
    @add-user-group="addUserGroup"
  />
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { Message } from '@arco-design/web-vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import AddMemberModal from './components/addMemberModal.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import {
    getMemberList,
    deleteMemberReq,
    addOrUpdate,
    batchAddProject,
    getProjectList,
    getGlobalUserGroup,
    getUser,
  } from '@/api/modules/setting/member';
  import useModal from '@/hooks/useModal';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import MSBatchModal from '@/components/bussiness/ms-batch-modal/index.vue';
  import { useTableStore, useUserStore } from '@/store';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import type { MemberItem, AddorUpdateMemberModel, LinkList, BatchAddProjectModel } from '@/models/setting/member';

  const tableStore = useTableStore();
  const { t } = useI18n();
  const userStore = useUserStore();

  const columns: MsTableColumn = [
    {
      title: 'organization.member.tableColunmEmail',
      dataIndex: 'email',
      width: 200,
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmName',
      dataIndex: 'name',
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmPhone',
      dataIndex: 'phone',
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmPro',
      slotName: 'project',
      dataIndex: 'projectIdNameMap',
      width: 280,
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmUsergroup',
      slotName: 'userRole',
      dataIndex: 'userRoleIdNameMap',
      width: 250,
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmStatus',
      slotName: 'enable',
      dataIndex: 'enable',
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmActions',
      slotName: 'action',
      fixed: 'right',
      width: 150,
      showDrag: false,
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
        label: 'organization.member.batchActionAddUsergroup',
        eventTag: 'batchAddUsergroup',
      },
    ],
  };
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getMemberList, {
    tableKey: TableKeyEnum.ORGANNATIONMEMBER,
    scroll: { y: 'auto', x: 1400 },
    selectable: true,
  });
  const keyword = ref('');
  const initData = async () => {
    setLoadListParams({ keyword: keyword.value, organizationId: userStore.$state?.lastOrganizationId });
    await loadList();
  };
  const searchHandler = () => {
    initData();
  };
  const addMemberVisible = ref<boolean>(false);
  const AddMemberRef = ref();
  const tableSelected = ref<(string | number)[]>([]);
  const { openModal } = useModal();
  function deleteMember(record: MemberItem) {
    openModal({
      type: 'warning',
      title: t('organization.member.deleteMemberTip', { name: record.name }),
      content: '',
      okText: t('organization.member.deleteMemberConfirm'),
      cancelText: t('organization.member.deleteMemberCancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await deleteMemberReq(record.lastOrganizationId, record.id);
          Message.success(t('organization.member.deleteMemberSuccess'));
          initData();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }
  function editMember(record: AddorUpdateMemberModel) {
    addMemberVisible.value = true;
    AddMemberRef.value.type = 'edit';
    AddMemberRef.value.edit(record);
  }
  function handleTableSelect(selectArr: (string | number)[]) {
    tableSelected.value = selectArr;
  }
  function addMember() {
    addMemberVisible.value = true;
    AddMemberRef.value.type = 'add';
  }
  function handlerOk() {
    initData();
  }
  const showBatchModal = ref(false);
  const batchAction = ref('');
  const treeData = ref([]);
  const getProjectTreeData = async () => {
    treeData.value = [];
    try {
      const result = await getProjectList({
        current: 1,
        pageSize: 10,
        organizationId: userStore.$state?.lastOrganizationId,
      });
      treeData.value = result.list.map((item: any) => {
        return {
          title: item.name,
          key: item.id,
        };
      });
    } catch (error) {
      console.log(error);
    }
  };
  const getUserTreeData = async () => {
    treeData.value = [];
  };
  // 添加到项目
  const addProject = async (target: string[]) => {
    try {
      const params: BatchAddProjectModel = {
        organizationId: userStore.$state?.lastOrganizationId,
        memberIds: tableSelected.value,
        projectIds: target,
      };
      await batchAddProject(params);
      Message.success(t('organization.member.batchModalSuccess'));
      initData();
    } catch (error) {
      console.log(error);
    }
  };
  // 添加到用户组
  function addUserGroup() {}
  function handelTableBatch(actionItem: any) {
    showBatchModal.value = true;
    batchAction.value = actionItem.eventTag;
    if (actionItem.eventTag === 'batchAddProject') getProjectTreeData();
    if (actionItem.eventTag === 'batchAddUsergroup') getUserTreeData();
  }
  // 列表编辑更新用户组和项目
  const updateUserOrProject = async (record: MemberItem) => {
    try {
      const params = {
        organizationId: record.lastOrganizationId,
        projectIds: [...record.selectProjectList],
        userRoleIds: [...record.selectUserList],
      };
      await addOrUpdate(params, 'edit');
      Message.success(t('organization.member.batchModalSuccess'));
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
    if (type === 'project') {
      record.showProjectSelect = true;
      record.showUserSelect = false;
    } else {
      record.showUserSelect = true;
      record.showProjectSelect = false;
    }
    record.selectProjectList = Object.keys(record.projectIdNameMap || {});
    record.selectUserList = Object.keys(record.userRoleIdNameMap || {});
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
      if (type === 'project' && record.selectProjectList.length < 1) {
        Message.warning(t('organization.member.selectProjectScope'));
        return;
      }
      updateUserOrProject(record);
    }
  };
  const memberList = ref<LinkList>([]);
  const userGroupOptions = ref<LinkList>([]);
  const projectList = ref<LinkList>([
    {
      id: '1',
      name: '项目一',
    },
    {
      id: '2',
      name: '项目二',
    },
  ]);
  const getUserAndGroupList = async () => {
    try {
      const userGroupData = await getGlobalUserGroup();
      const userData = await getUser();
      userGroupOptions.value = userGroupData.filter((item: any) => item.type === 'ORGANIZATION');
      memberList.value = userData;
    } catch (error) {
      console.log(error);
    }
  };
  onMounted(() => {
    getUserAndGroupList();
    initData();
  });
</script>

<style lang="less" scoped></style>
