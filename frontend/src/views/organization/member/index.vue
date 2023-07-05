<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button class="mr-3" type="primary" @click="AddMember">{{ t('organization.member.addMember') }}</a-button>
      </div>
      <a-input-search :placeholder="t('organization.member.searchMember')" class="w-[230px]"></a-input-search>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="tableBatchActions"
      @selected-change="handleTableSelect"
      v-on="propsEvent"
    >
      <template #project="{ record }">
        <a-tag v-for="pro of record.projectList.slice(0, 2)" :key="pro.id" class="mr-[4px] bg-transparent" bordered>
          {{ pro.name }}
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
        <MsButton @click="deleteMember(record)">{{ t('organization.member.remove') }}</MsButton>
      </template>
    </ms-base-table>
    <add-member-modal :visible="addMemberVisible" @cancel="addMemberVisible = false" />
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import addMemberModal from './components/addMemberModal.vue';
  import { getMemberList } from '@/api/modules/system/member';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';

  const columns: MsTableColumn = [
    {
      title: 'organization.member.tableColunmEmail',
      dataIndex: 'email',
      width: 200,
    },
    {
      title: 'organization.member.tableColunmName',
      dataIndex: 'name',
    },
    {
      title: 'organization.member.tableColunmPhone',
      dataIndex: 'phone',
    },
    {
      title: 'organization.member.tableColunmPro',
      slotName: 'project',
      dataIndex: 'projectList',
      width: 250,
    },
    {
      title: 'organization.member.tableColunmUsergroup',
      slotName: 'userRole',
      dataIndex: 'userRoleList',
      width: 250,
    },
    {
      title: 'organization.member.tableColunmStatus',
      slotName: 'enable',
      dataIndex: 'enable',
    },
    {
      title: 'organization.member.tableColunmActions',
      slotName: 'action',
      fixed: 'right',
      width: 80,
    },
  ];

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

  const { propsRes, propsEvent, loadList, setKeyword } = useTable(getMemberList, {
    columns,
    scroll: { y: 'auto', x: 1200 },
    selectable: true,
  });

  const keyword = ref('');

  const addMemberVisible = ref<boolean>(false);

  onMounted(async () => {
    setKeyword(keyword.value);
    await loadList();
  });

  const { t } = useI18n();

  function deleteMember(record: any) {
    console.log(record, 1);
  }
  const tableSelected = ref<(string | number)[]>([]);
  function handleTableSelect(selectArr: (string | number)[]) {
    tableSelected.value = selectArr;
  }
  function AddMember() {
    addMemberVisible.value = true;
  }
</script>

<style lang="less" scoped></style>
