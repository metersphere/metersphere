<template>
  <ms-drawer
    :mask="false"
    :width="680"
    :visible="currentVisible"
    unmount-on-close
    :footer="false"
    class="ms-drawer-no-mask"
    :title="t('system.organization.addMember')"
    @cancel="handleCancel"
  >
    <div>
      <div class="flex flex-row justify-between">
        <a-button type="primary" @click="handleAddMember">
          {{ t('system.organization.addMember') }}
        </a-button>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('system.user.searchUser')"
          class="w-[230px]"
          allow-clear
          @search="searchUser"
          @press-enter="searchUser"
        ></a-input-search>
      </div>
      <ms-base-table class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
        <template #name="{ record }">
          <span>{{ record.name }}</span>
          <span v-if="record.adminFlag" class="ml-[4px] text-[var(--color-text-4)]">{{
            `(${t('common.admin')})`
          }}</span>
        </template>
        <template #operation="{ record }">
          <MsRemoveButton
            :title="t('system.organization.removeName', { name: record.name })"
            :sub-title-tip="t('system.organization.removeTip')"
            @ok="handleRemove(record)"
          />
        </template>
      </ms-base-table>
    </div>
  </ms-drawer>
  <AddUserModal
    :project-id="props.projectId"
    :organization-id="props.organizationId"
    :visible="userVisible"
    @cancel="handleHideUserModal"
    @submit="handleAddMembeSubmit"
  />
</template>

<script lang="ts" setup>
  import {
    postUserTableByOrgIdOrProjectId,
    deleteUserFromOrgOrProject,
  } from '@/api/modules/setting/organizationAndProject';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { useI18n } from '@/hooks/useI18n';
  import { watch, ref } from 'vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import AddUserModal from './addUserModal.vue';
  import { TableData, Message } from '@arco-design/web-vue';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  export interface projectDrawerProps {
    visible: boolean;
    organizationId?: string;
    projectId?: string;
  }
  const { t } = useI18n();
  const props = defineProps<projectDrawerProps>();
  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'requestFetchData'): void;
  }>();

  const currentVisible = ref(props.visible);

  const userVisible = ref(false);

  const keyword = ref('');

  const projectColumn: MsTableColumn = [
    {
      title: 'system.organization.userName',
      slotName: 'name',
      dataIndex: 'nameTooltip',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.organization.email',
      dataIndex: 'email',
      width: 200,
      showTooltip: true,
    },
    {
      title: 'system.organization.phone',
      dataIndex: 'phone',
    },
    { title: 'system.organization.operation', slotName: 'operation' },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(
    postUserTableByOrgIdOrProjectId,
    {
      columns: projectColumn,
      scroll: { x: '100%' },
      heightUsed: 240,
      selectable: false,
      noDisable: false,
      pageSimple: true,
    },
    (record: any) => ({
      ...record,
      nameTooltip: record.name + (record.adminFlag ? `(${t('common.admin')})` : ''),
    })
  );

  async function searchUser() {
    setKeyword(keyword.value);
    await loadList();
  }

  const handleCancel = () => {
    emit('cancel');
  };

  const fetchData = async () => {
    if (props.organizationId) {
      setLoadListParams({ organizationId: props.organizationId });
    }
    if (props.projectId) {
      setLoadListParams({ projectId: props.projectId });
    }
    await loadList();
  };

  const handleAddMember = () => {
    userVisible.value = true;
  };
  const handleAddMembeSubmit = () => {
    fetchData();
    emit('requestFetchData');
  };

  const handleHideUserModal = () => {
    userVisible.value = false;
  };

  const handleRemove = async (record: TableData) => {
    try {
      if (props.organizationId) {
        await deleteUserFromOrgOrProject(props.organizationId, record.id);
      }
      if (props.projectId) {
        await deleteUserFromOrgOrProject(props.projectId, record.id, true);
      }
      Message.success(t('common.removeSuccess'));
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  watch(
    () => props.organizationId,
    () => {
      fetchData();
    }
  );
  watch(
    () => props.projectId,
    () => {
      fetchData();
    }
  );
  watch(
    () => props.visible,
    (visible) => {
      currentVisible.value = visible;
    }
  );
</script>
