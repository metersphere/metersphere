<template>
  <MsDrawer
    :width="680"
    :visible="currentVisible"
    unmount-on-close
    :footer="false"
    :title="t('system.organization.addMember')"
    :mask="false"
    @cancel="handleCancel"
  >
    <div>
      <div class="flex flex-row justify-between">
        <a-button
          v-if="hasAnyPermission(['ORGANIZATION_PROJECT:READ+ADD+MEMBER'])"
          type="primary"
          @click="handleAddMember"
        >
          {{ t('system.organization.addMember') }}
        </a-button>
        <a-input-search
          v-model:model-value="keyword"
          allow-clear
          :placeholder="t('system.organization.searchUserPlaceholder')"
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
            v-permission="['ORGANIZATION_PROJECT:READ+DELETE_MEMBER']"
            :title="t('system.project.removeName', { name: record.name })"
            :sub-title-tip="t('system.project.removeTip')"
            @ok="handleRemove(record)"
          />
        </template>
      </ms-base-table>
    </div>
  </MsDrawer>
  <AddUserModal
    :project-id="props.projectId"
    :organization-id="props.organizationId"
    :visible="userVisible"
    @cancel="handleHideUserModal"
  />
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { Message, TableData } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import AddUserModal from './addUserModal.vue';

  import { deleteProjectMemberByOrg, postProjectMemberByProjectId } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';
  import { formatPhoneNumber } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

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

  const hasOperationPermission = computed(() => hasAnyPermission(['ORGANIZATION_PROJECT:READ+DELETE_MEMBER']));

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
    {
      title: hasOperationPermission.value ? 'system.organization.operation' : '',
      slotName: 'operation',
      width: hasOperationPermission.value ? 60 : 20,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(
    postProjectMemberByProjectId,
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
      if (props.projectId) {
        await deleteProjectMemberByOrg(props.projectId, record.id);
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
    () => props.projectId,
    () => {
      setLoadListParams({ projectId: props.projectId });
      fetchData();
    }
  );
  watch(
    () => props.visible,
    (visible) => {
      currentVisible.value = visible;
      if (visible) {
        fetchData();
      }
    }
  );
</script>

<style lang="less" scoped>
  :deep(.custom-height) {
    height: 100vh !important;
    border: 1px solid red;
  }
</style>
