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
        <a-tooltip :content="(record.projectIdNameMap||[]).map((e: any) => e.name).join(',')">
          <div v-if="!record.showProjectSelect">
            <a-tag
              v-for="pro of (record.projectIdNameMap || []).slice(0, 3)"
              :key="pro.id"
              class="mr-[4px] bg-transparent"
              bordered
              @click="changeUserOrProject(record, 'project')"
            >
              {{ pro.name }}
            </a-tag>
            <a-tag
              v-if="(record.projectIdNameMap || []).length > 3"
              class="mr-[4px] bg-transparent"
              bordered
              @click="changeUserOrProject(record, 'project')"
            >
              +{{ (record.projectIdNameMap || []).length - 3 }}
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
            <a-option v-for="item of projectOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </a-tooltip>
      </template>
      <template #userRole="{ record }">
        <a-tooltip :content="(record.userRoleIdNameMap||[]).map((e: any) => e.name).join(',')">
          <div v-if="!record.showUserSelect">
            <a-tag
              v-for="org of (record.userRoleIdNameMap || []).slice(0, 3)"
              :key="org"
              class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
              bordered
              @click="changeUserOrProject(record, 'user')"
            >
              {{ org.name }}
            </a-tag>
            <a-tag
              v-if="record.userRoleIdNameMap.length > 3"
              class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
              bordered
              @click="changeUserOrProject(record, 'user')"
            >
              +{{ record.userRoleIdNameMap.length - 3 }}
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
        </a-tooltip>
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
    v-if="treeData.length > 0"
    ref="batchModalRef"
    v-model:visible="showBatchModal"
    :table-selected="tableSelected"
    :action="batchAction"
    :tree-data="treeData"
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
  import type {
    MemberItem,
    AddorUpdateMemberModel,
    LinkList,
    LinkItem,
    BatchAddProjectModel,
  } from '@/models/setting/member';
  import { characterLimit } from '@/utils';

  const tableStore = useTableStore();
  const { t } = useI18n();
  const userStore = useUserStore();
  const lastOrganizationId = userStore.$state?.lastOrganizationId as string;

  const columns: MsTableColumn = [
    {
      title: 'organization.member.tableColunmEmail',
      dataIndex: 'email',
      width: 200,
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmName',
      dataIndex: 'name',
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmPhone',
      dataIndex: 'phone',
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmPro',
      slotName: 'project',
      dataIndex: 'projectIdNameMap',
      width: 300,
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmUsergroup',
      slotName: 'userRole',
      dataIndex: 'userRoleIdNameMap',
      width: 300,
      showInTable: true,
    },
    {
      title: 'organization.member.tableColunmStatus',
      slotName: 'enable',
      dataIndex: 'enable',
      showInTable: true,
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
    scroll: { x: 1600 },
    selectable: true,
    showSetting: true,
  });
  const keyword = ref('');
  const tableSelected = ref<(string | number)[]>([]);
  const selectData = ref<string[]>([]);

  interface TreeDataItem {
    key: string;
    title: string;
    children?: TreeDataItem[];
  }
  const batchAction = ref('');

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
      await deleteMemberReq(lastOrganizationId, record.id);
      Message.success(t('organization.member.deleteMemberSuccess'));
      initData();
    } catch (error) {
      console.log(error);
    }
  };
  const handleTableSelect = (selectArr: (string | number)[]) => {
    tableSelected.value = selectArr;
  };

  const treeData = ref<TreeDataItem[]>([]);
  const getData = async (callBack: any) => {
    try {
      const links = await callBack(lastOrganizationId);
      treeData.value = links.map((item: LinkItem) => {
        return {
          title: item.name,
          key: item.id,
          id: item.id,
        };
      });
    } catch (error) {
      console.log(error);
    }
  };

  const batchModalRef = ref();
  const showBatchModal = ref(false);

  const batchList = [
    {
      type: 'project',
      request: batchAddProject,
    },
    {
      type: 'usergroup',
      request: batchAddUserGroup,
    },
  ];
  // 添加到项目和用户组
  const addProjectOrAddUserGroup = (target: string[], type: string) => {
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
    if (currentType) batchModalRef.value.batchRequestFun(currentType.request, params);
  };
  // 批量操作
  const handleTableBatch = (actionItem: any) => {
    showBatchModal.value = true;
    treeData.value = [];
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
    userGroupOptions.value = await getGlobalUserGroup(lastOrganizationId);
    projectOptions.value = await getProjectList(lastOrganizationId);
  };
  onBeforeMount(() => {
    initData();
    getLinkList();
  });
</script>

<style lang="less" scoped></style>
