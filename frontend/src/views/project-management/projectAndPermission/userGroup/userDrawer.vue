<template>
  <MsDrawer
    :width="680"
    :visible="currentVisible"
    unmount-on-close
    :footer="false"
    :title="t('system.organization.addMember')"
    :mask="false"
    class="ms-drawer-no-mask"
    @cancel="handleCancel"
  >
    <div>
      <div class="flex flex-row justify-between">
        <a-button type="primary" @click="handleAddMember">
          {{ t('system.organization.addMember') }}
        </a-button>
        <a-input-search
          v-model:model-value="keyword"
          allow-clear
          :placeholder="t('system.user.searchUser')"
          class="w-[230px]"
          @search="searchUser"
          @press-enter="searchUser"
          @clear="searchUser"
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
            :title="t('system.organization.removeName', { name: characterLimit(record.name) })"
            :sub-title-tip="t('project.userGroup.removeTip')"
            @ok="handleRemove(record)"
          />
        </template>
      </ms-base-table>
    </div>
  </MsDrawer>
  <AddUserModal
    :project-id="props.projectId"
    :user-role-id="props.userRoleId"
    :visible="userVisible"
    @cancel="handleHideUserModal"
  />
</template>

<script lang="ts" setup>
  import { ref, watchEffect } from 'vue';
  import { Message, TableData } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import AddUserModal from './addUserModal.vue';

  import { deleteUserFromUserGroup, postUserByUserGroup } from '@/api/modules/project-management/usergroup';
  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit, formatPhoneNumber } from '@/utils';

  export interface projectDrawerProps {
    visible: boolean;
    userRoleId: string;
    projectId: string;
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
      dataIndex: 'name',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.organization.email',
      dataIndex: 'email',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.organization.phone',
      dataIndex: 'phone',
    },
    { title: 'system.organization.operation', slotName: 'operation', width: 60 },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(
    postUserByUserGroup,
    {
      heightUsed: 240,
      columns: projectColumn,
      scroll: { x: '100%' },
      selectable: false,
      noDisable: false,
      pageSimple: true,
    },
    (record) => {
      return {
        ...record,
        phone: formatPhoneNumber(record.phone || ''),
      };
    }
  );

  async function searchUser() {
    setKeyword(keyword.value);
    await loadList();
  }

  const handleCancel = () => {
    keyword.value = '';
    emit('cancel');
  };

  const fetchData = async () => {
    await loadList();
  };

  const handleAddMember = () => {
    userVisible.value = true;
  };

  const handleHideUserModal = (shouldSearch: boolean) => {
    userVisible.value = false;
    if (shouldSearch) {
      fetchData();
      emit('requestFetchData');
    }
  };

  const handleRemove = async (record: TableData) => {
    try {
      const { projectId, userRoleId } = props;
      if (projectId && userRoleId) {
        await deleteUserFromUserGroup({ projectId, userRoleId, userIds: [record.id] });
      }
      Message.success(t('common.removeSuccess'));
      fetchData();
      emit('requestFetchData');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };
  watch(
    () => props.visible,
    (visible) => {
      currentVisible.value = visible;
      if (visible) {
        setKeyword(keyword.value);
        fetchData();
      }
    }
  );
  watchEffect(() => {
    const { projectId, userRoleId } = props;
    if (projectId && userRoleId) {
      setLoadListParams({ projectId, userRoleId });
      fetchData();
    }
  });
</script>

<style lang="less" scoped>
  .arco-table {
    div,
    span {
      color: var(--color-text-1);
    }
  }
</style>
