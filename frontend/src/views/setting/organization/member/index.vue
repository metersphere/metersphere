<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button
          v-permission="['ORGANIZATION_MEMBER:READ+ADD']"
          class="mr-3"
          type="primary"
          @click="addOrEditMember('add')"
          >{{ t('organization.member.addMember') }}</a-button
        >
      </div>
      <a-input-search
        v-model="keyword"
        :max-length="255"
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
      <template #projectIdNameMap="{ record }">
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
          class="w-[260px]"
          :popup-visible="record.showProjectSelect"
          @change="(value) => selectUserOrProject(value, record, 'project')"
          @popup-visible-change="visibleChange($event, record, 'project')"
        >
          <a-option v-for="item of projectOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
        </a-select>
        <span v-if="(record.projectIdNameMap || []).length === 0">-</span>
      </template>
      <template #userRoleIdNameMap="{ record }">
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
          class="w-[260px]"
          :popup-visible="record.showUserSelect"
          @change="(value) => selectUserOrProject(value, record, 'user')"
          @popup-visible-change="(value) => visibleChange(value, record, 'user')"
        >
          <a-option v-for="item of userGroupOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
        </a-select>
        <span v-if="(record.userRoleIdNameMap || []).length === 0">-</span>
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
        <MsButton v-permission="['ORGANIZATION_MEMBER:READ+UPDATE']" @click="addOrEditMember('edit', record)">{{
          t('organization.member.edit')
        }}</MsButton>
        <MsRemoveButton
          v-permission="['ORGANIZATION_MEMBER:READ+DELETE']"
          position="br"
          :title="t('organization.member.deleteMemberTip', { name: characterLimit(record.name) })"
          :sub-title-tip="t('organization.member.subTitle')"
          :loading="deleteLoading"
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
    :action="batchAction"
    :select-data="selectedData"
    @add-project="addProjectOrAddUserGroup"
    @add-user-group="addProjectOrAddUserGroup"
  />
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-成员
   */
  import { onBeforeMount, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { isEqual } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MSBatchModal from '@/components/business/ms-batch-modal/index.vue';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import AddMemberModal from './components/addMemberModal.vue';

  import {
    addOrUpdate,
    batchAddProject,
    batchAddUserGroup,
    deleteMemberReq,
    getGlobalUserGroup,
    getMemberList,
    getProjectList,
  } from '@/api/modules/setting/member';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit, formatPhoneNumber } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { AddOrUpdateMemberModel, BatchAddProjectModel, LinkList, MemberItem } from '@/models/setting/member';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const tableStore = useTableStore();
  const appStore = useAppStore();
  const { t } = useI18n();
  const lastOrganizationId = computed(() => appStore.currentOrgId);

  const hasOrdMemberOperationPermission = computed(() =>
    hasAnyPermission(['ORGANIZATION_MEMBER:READ+UPDATE', 'ORGANIZATION_MEMBER:READ+DELETE'])
  );
  const columns: MsTableColumn = [
    {
      title: 'organization.member.tableColunmEmail',
      dataIndex: 'email',
      width: 200,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
      sortIndex: 0,
      showDrag: false,
    },
    {
      title: 'organization.member.tableColunmName',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'organization.member.tableColunmPhone',
      dataIndex: 'phone',
      showInTable: true,
      width: 200,
      showTooltip: true,
      ellipsis: true,
      showDrag: true,
    },
    {
      title: 'organization.member.tableColunmPro',
      slotName: 'projectIdNameMap',
      dataIndex: 'projectIdNameMap',
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'organization.member.tableColunmUsergroup',
      slotName: 'userRoleIdNameMap',
      dataIndex: 'userRoleIdNameMap',
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'organization.member.tableColunmStatus',
      slotName: 'enable',
      dataIndex: 'enable',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: hasOrdMemberOperationPermission.value ? 'organization.member.tableColunmActions' : '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOrdMemberOperationPermission.value ? 140 : 50,
      showInTable: true,
      showDrag: false,
    },
  ];
  await tableStore.initColumn(TableKeyEnum.ORGANIZATION_MEMBER, columns, 'drawer');

  const tableBatchActions = {
    baseAction: [
      {
        label: 'organization.member.batchActionAddProject',
        eventTag: 'batchAddProject',
        permission: ['ORGANIZATION_MEMBER:READ+UPDATE'],
      },
      {
        label: 'organization.member.batchActionAddUserGroup',
        eventTag: 'batchAddUserGroup',
        permission: ['ORGANIZATION_MEMBER:READ+UPDATE'],
      },
    ],
  };
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getMemberList,
    {
      tableKey: TableKeyEnum.ORGANIZATION_MEMBER,
      scroll: { x: 1600 },
      selectable: hasAnyPermission(['ORGANIZATION_MEMBER:READ+ADD', 'ORGANIZATION_MEMBER:READ+UPDATE']),
      heightUsed: 288,
      showSetting: true,
      showJumpMethod: true,
      size: 'default',
    },
    (record) => {
      return {
        ...record,
        phone: formatPhoneNumber(record.phone || ''),
      };
    }
  );
  const keyword = ref('');
  const tableSelected = ref<(string | number)[]>([]);
  // 跨页多选
  const selectedData = ref<string[] | undefined>([]);

  const initData = async () => {
    setLoadListParams({ keyword: keyword.value, organizationId: lastOrganizationId.value });
    await loadList();
  };
  const searchHandler = () => {
    initData();
  };

  const addMemberVisible = ref<boolean>(false);
  const AddMemberRef = ref();

  const addOrEditMember = (type: string, record: AddOrUpdateMemberModel = {}) => {
    addMemberVisible.value = true;
    AddMemberRef.value.type = type;
    if (type === 'edit') {
      AddMemberRef.value.edit(record);
    }
  };

  const deleteLoading = ref<boolean>(false);
  const deleteMember = async (record: MemberItem) => {
    deleteLoading.value = true;
    try {
      if (lastOrganizationId.value) await deleteMemberReq(lastOrganizationId.value, record.id);
      Message.success(t('organization.member.deleteMemberSuccess'));
      initData();
      resetSelector();
    } catch (error) {
      console.log(error);
    } finally {
      deleteLoading.value = false;
    }
  };
  const handleTableSelect = (selectArr: (string | number)[]) => {
    tableSelected.value = selectArr;
  };

  const batchModalRef = ref();

  const getData = (callBack: any) => {
    batchModalRef.value.getTreeList(callBack, lastOrganizationId.value);
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
      organizationId: lastOrganizationId.value,
      memberIds: selectedData.value,
    };
    if (type === 'project') {
      params.projectIds = target;
    } else {
      params.userRoleIds = target;
    }
    if (currentType) await batchModalRef.value.batchRequestFun(currentType.request, params);
    initData();
    resetSelector();
  };

  // 批量操作
  const handleTableBatch = (event: BatchActionParams, params: BatchActionQueryParams) => {
    showBatchModal.value = true;
    selectedData.value = params.selectedIds;
    if (event.eventTag) batchAction.value = event.eventTag;
    if (event.eventTag === 'batchAddProject') getData(getProjectList);
    if (event.eventTag === 'batchAddUserGroup') getData(getGlobalUserGroup);
  };
  // 列表编辑更新用户组和项目
  const updateUserOrProject = async (record: MemberItem) => {
    try {
      const params = {
        organizationId: lastOrganizationId.value,
        projectIds: [...record.selectProjectList],
        userRoleIds: [...record.selectUserList],
        memberId: record.id,
      };
      await addOrUpdate(params, 'edit');
      Message.success(t('organization.member.batchUpdateSuccess'));
      initData();
      resetSelector();
    } catch (error) {
      console.log(error);
    } finally {
      record.showUserSelect = false;
      record.showProjectSelect = false;
    }
  };
  // 编辑模式和下拉选择切换
  const changeUserOrProject = (record: MemberItem, type: string) => {
    if (!hasAnyPermission(['ORGANIZATION_MEMBER:READ+UPDATE'])) {
      return;
    }
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
    if (!hasAnyPermission(['ORGANIZATION_MEMBER:READ+UPDATE'])) {
      return;
    }
    if (type === 'project') {
      record.selectProjectList = value;
    } else {
      record.selectUserList = value;
    }
  };
  // 面板切换的回调
  const visibleChange = (visible: boolean, record: MemberItem, type: string) => {
    const originMapIds =
      type === 'user'
        ? (record.userRoleIdNameMap || []).map((item: any) => item.id)
        : (record.projectIdNameMap || []).map((item: any) => item.id);
    if (!visible) {
      if (type === 'user' && record.selectUserList.length < 1) {
        Message.warning(t('organization.member.selectUserEmptyTip'));
        return;
      }
      const currentEditUser = type === 'user' ? record.selectUserList : record.selectProjectList;
      if (isEqual(originMapIds, currentEditUser)) {
        record.showProjectSelect = false;
        record.showUserSelect = false;
        return;
      }

      updateUserOrProject(record);
    }
  };

  const userGroupOptions = ref<LinkList>([]);
  const projectOptions = ref<LinkList>([]);
  const getLinkList = async () => {
    if (lastOrganizationId.value) {
      userGroupOptions.value = await getGlobalUserGroup(lastOrganizationId.value);
      if (hasAnyPermission(['ORGANIZATION_PROJECT:READ'])) {
        projectOptions.value = await getProjectList(lastOrganizationId.value);
      }
    }
  };

  onBeforeMount(() => {
    initData();
    getLinkList();
  });
</script>

<style lang="less" scoped></style>
