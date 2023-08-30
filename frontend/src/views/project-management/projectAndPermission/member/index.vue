<template>
  <div class="mb-4 grid grid-cols-4 gap-2">
    <div class="col-span-2"
      ><a-button class="mr-3" type="primary" @click="addMember">{{ t('project.member.addMember') }}</a-button></div
    >
    <div>
      <a-select v-model="searchParams.userId">
        <a-option v-for="item of userGroupListOptions" :key="item.value" :value="item.value">{{
          t(item.name)
        }}</a-option>
        <template #prefix
          ><span>{{ t('project.member.tableColumnUserGroup') }}</span></template
        >
      </a-select></div
    >
    <div>
      <a-input-search
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
      <a-tooltip :content="(record.userRoleIdNameMap||[]).map((e: any) => e.name).join(',')">
        <div v-if="!record.showUserSelect">
          <a-tag
            v-for="org of (record.userRoleIdNameMap || []).slice(0, 3)"
            :key="org"
            class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
            bordered
          >
            {{ org.name }}
          </a-tag>
          <a-tag
            v-if="record.userRoleIdNameMap.length > 3"
            class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
            bordered
          >
            +{{ record.userRoleIdNameMap.length - 3 }}
          </a-tag>
        </div>
        <a-select v-else v-model="record.selectUserList" multiple :max-tag-count="2">
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
      <MsRemoveButton
        position="br"
        :title="t('project.member.deleteMemberTip', { name: characterLimit(record.name) })"
        :sub-title-tip="t('project.member.subTitle')"
      />
    </template>
  </ms-base-table>
  <AddMemberModal v-model:visible="addMemberVisible" />
  <MSBatchModal
    ref="batchModalRef"
    v-model:visible="batchVisible"
    :table-selected="tableSelected"
    :action="batchAction"
    :tree-data="treeData"
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
  import { getMemberList } from '@/api/modules/setting/member';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { useTableStore, useUserStore } from '@/store';
  import useModal from '@/hooks/useModal';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import { characterLimit } from '@/utils';
  import AddMemberModal from './components/addMemberModal.vue';
  import MSBatchModal from '@/components/business/ms-batch-modal/index.vue';
  import type { TreeDataItem } from '@/models/projectManagement/member';

  const { t } = useI18n();
  const { openModal } = useModal();

  const tableStore = useTableStore();
  const userStore = useUserStore();
  const lastOrganizationId = userStore?.$state.lastOrganizationId;

  const userGroupListOptions = ref([
    {
      id: '',
      name: '全部',
      value: '',
    },
    {
      id: '1001',
      name: '用户组1',
      value: '1001',
    },
  ]);
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
      width: 80,
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
  };

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getMemberList, {
    tableKey: TableKeyEnum.PROJECT_MEMBER,
    selectable: true,
    showSetting: true,
    scroll: {
      x: 1000,
    },
  });

  const initData = async () => {
    setLoadListParams({ organizationId: lastOrganizationId });
    await loadList();
  };

  const searchParams = ref({
    userId: '',
  });

  const searchHandler = () => {};

  // 批量移除成员
  const batchRemoveMember = () => {
    openModal({
      type: 'error',
      title: t('project.member.batchRemoveTip', { number: tableSelected.value.length }),
      content: t('project.member.batchRemoveContent'),
      okText: t('project.member.deleteMemberConfirm'),
      cancelText: t('project.member.Cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {},
      hideCancel: false,
    });
  };

  const batchVisible = ref<boolean>(false);
  const selectData = ref<string[]>([]);
  const batchAction = ref('');
  const treeData = ref<TreeDataItem[]>([]);

  // 添加到用户组
  const addUserGroup = () => {};

  const handleTableBatch = (actionItem: any) => {
    if (actionItem.eventTag === 'batchActionRemove') {
      batchRemoveMember();
    }
    if (actionItem.eventTag === 'batchAddUserGroup') {
      batchVisible.value = true;
      addUserGroup();
    }
  };

  const userGroupOptions = ref([
    {
      id: '',
      name: '',
    },
  ]);

  const addMemberVisible = ref<boolean>(false);

  const addMember = () => {
    addMemberVisible.value = true;
  };

  onBeforeMount(() => {
    initData();
  });
</script>

<style scoped></style>
