<template>
  <MsDrawer
    :width="680"
    :visible="currentVisible"
    unmount-on-close
    :footer="false"
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
          @search="searchUser"
          @press-enter="searchUser"
        ></a-input-search>
      </div>
      <ms-base-table class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
        <template #operation="{ record }">
          <ms-button @click="handleRemove(record)">{{ t('system.organization.remove') }}</ms-button>
        </template>
      </ms-base-table>
    </div>
  </MsDrawer>
  <AddUserModal :organization-id="props.organizationId" :visible="userVisible" @cancel="handleHideUserModal" />
</template>

<script lang="ts" setup>
  import { postUserTableByOrgId } from '@/api/modules/setting/system/organizationAndProject';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { useI18n } from '@/hooks/useI18n';
  import { watch, ref } from 'vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import AddUserModal from './addUserModal.vue';
  import { TableData } from '@arco-design/web-vue';
  import MsButton from '@/components/pure/ms-button/index.vue';

  export interface projectDrawerProps {
    visible: boolean;
    organizationId: string;
  }
  const { t } = useI18n();
  const props = defineProps<projectDrawerProps>();
  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();

  const currentVisible = ref(props.visible);

  const userVisible = ref(false);

  const keyword = ref('');

  const projectColumn: MsTableColumn = [
    {
      title: 'system.organization.userName',
      dataIndex: 'name',
    },
    {
      title: 'system.organization.email',
      dataIndex: 'email',
    },
    {
      title: 'system.organization.phone',
      dataIndex: 'phone',
    },
    { title: 'system.organization.operation', dataIndex: 'operation' },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(postUserTableByOrgId, {
    columns: projectColumn,
    showSetting: false,
    scroll: { y: 'auto', x: '600px' },
    selectable: false,
    size: 'small',
    noDisable: false,
  });

  async function searchUser() {
    setKeyword(keyword.value);
    await loadList();
  }

  const handleCancel = () => {
    emit('cancel');
  };

  const fetchData = async () => {
    await loadList();
  };

  const handleAddMember = () => {
    userVisible.value = true;
  };

  const handleHideUserModal = () => {
    userVisible.value = false;
  };

  const handleRemove = (record: TableData) => {
    // TODO: remove user
    // eslint-disable-next-line no-console
    console.log(record);
  };

  watch(
    () => props.organizationId,
    (organizationId) => {
      setLoadListParams({ organizationId });
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
