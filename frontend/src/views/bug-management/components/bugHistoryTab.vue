<template>
  <ms-base-table v-bind="propsRes" v-on="propsEvent">
    <template #changeNumber="{ record }">
      <span>{{ record.id }}</span>
      <MsTag v-if="record.latest" size="small" class="ml-2">{{ t('bugManagement.history.current') }}</MsTag>
    </template>
  </ms-base-table>
</template>

<script lang="ts" setup>
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { getChangeHistoryList } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  const { t } = useI18n();
  const appStore = useAppStore();
  const props = defineProps<{
    bugId: string;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'bugManagement.history.changeNumber',
      slotName: 'changeNumber',
      dataIndex: 'id',
      width: 200,
    },
    {
      title: 'bugManagement.history.operation',
      dataIndex: 'type',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'bugManagement.history.operationMan',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'bugManagement.history.updateTime',
      dataIndex: 'createTime',
      width: 200,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getChangeHistoryList, {
    heightUsed: 380,
    columns,
    scroll: { x: '100%' },
    selectable: false,
    noDisable: false,
  });

  const fetchData = async (id: string) => {
    setLoadListParams({ sourceId: id, projectId: appStore.currentProjectId });
    await loadList();
  };

  watchEffect(() => {
    fetchData(props.bugId);
  });
</script>
