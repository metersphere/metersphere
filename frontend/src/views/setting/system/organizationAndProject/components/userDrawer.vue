<template>
  <MsDrawer
    :width="680"
    :visible="props.visible"
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
      <ms-base-table class="mt-[16px]" v-bind="propsRes" v-on="propsEvent" />
    </div>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { postUserTableByOrgId } from '@/api/modules/setting/system/organizationAndProject';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { useI18n } from '@/hooks/useI18n';
  import { watch, ref } from 'vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';

  export interface projectDrawerProps {
    visible: boolean;
    organizationId: string;
  }
  const { t } = useI18n();
  const props = defineProps<projectDrawerProps>();
  const emit = defineEmits<{
    (e: 'update:visible', v: boolean): void;
  }>();

  const keyword = ref('');

  const projectColumn: MsTableColumn = [
    {
      title: 'system.organization.ID',
      dataIndex: 'num',
    },
    {
      title: 'system.project.name',
      dataIndex: 'name',
    },
    {
      title: 'system.organization.status',
      dataIndex: 'enable',
    },
    {
      title: 'system.organization.creator',
      dataIndex: 'createUser',
    },
    {
      title: 'system.organization.createTime',
      dataIndex: 'createTime',
    },
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
    emit('update:visible', false);
  };

  const fetchData = async () => {
    await loadList();
  };

  const handleAddMember = () => {
    // TODO add member
    emit('update:visible', false);
  };

  watch(
    () => props.organizationId,
    (organizationId) => {
      setLoadListParams({ organizationId });
      fetchData();
    }
  );
</script>
