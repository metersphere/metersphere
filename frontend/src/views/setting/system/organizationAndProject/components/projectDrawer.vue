<template>
  <ms-drawer
    :mask="false"
    :width="680"
    :visible="currentVisible"
    unmount-on-close
    class="ms-drawer-no-mask"
    :footer="false"
    :title="t('system.organization.projectName', { name: props.currentName })"
    @cancel="handleCancel"
  >
    <div>
      <div class="flex flex-row justify-end">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('system.project.searchPlaceholder')"
          class="w-[230px]"
          allow-clear
          @search="searchUser"
          @press-enter="searchUser"
          @clear="searchUser"
        ></a-input-search>
      </div>
      <ms-base-table v-bind="propsRes" v-on="propsEvent">
        <template #num="{ record }">
          <a-tooltip v-if="!record.enable" position="tl" :content="t('system.organization.projectIsDisabled')">
            <div>{{ record.num }}</div>
          </a-tooltip>
          <span v-else>{{ record.num }}</span>
        </template>
      </ms-base-table>
    </div>
  </ms-drawer>
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { postProjectTableByOrgId } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';

  export interface projectDrawerProps {
    visible: boolean;
    organizationId: string;
    currentName: string;
  }
  const { t } = useI18n();
  const props = defineProps<projectDrawerProps>();
  const emit = defineEmits<{
    (e: 'cancel', v: boolean): void;
  }>();

  const currentVisible = ref(props.visible);
  const keyword = ref('');

  const projectColumn: MsTableColumn = [
    {
      title: 'system.organization.ID',
      dataIndex: 'num',
      slotName: 'num',
    },
    {
      title: 'system.project.name',
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'system.organization.status',
      dataIndex: 'enable',
      disableTitle: 'common.end',
    },
    {
      title: 'system.organization.creator',
      dataIndex: 'createUser',
    },
    {
      title: 'system.organization.createTime',
      dataIndex: 'createTime',
      width: 180,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(postProjectTableByOrgId, {
    columns: projectColumn,
    scroll: { x: '100%' },
    heightUsed: 240,
    selectable: false,
    noDisable: false,
    pageSimple: true,
  });

  async function searchUser() {
    setKeyword(keyword.value);
    await loadList();
  }

  const handleCancel = () => {
    emit('cancel', false);
  };

  const fetchData = async () => {
    await loadList();
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
      if (visible) {
        fetchData();
      } else {
        setKeyword('');
      }
    }
  );
</script>
