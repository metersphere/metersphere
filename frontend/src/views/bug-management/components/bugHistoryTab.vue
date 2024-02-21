<template>
  <ms-base-table class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
    <template #changeNumber="{ record }">
      <span>{{ record.id }}</span>
      <!-- TODO: 先不上 -->
      <!-- <a-tag size="small" class="ml-[4px]">{{ t('bugManagement.history.current') }}</a-tag> -->
    </template>
  </ms-base-table>
</template>

<script lang="ts" setup>
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

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
    heightUsed: 240,
    columns,
    scroll: { x: '100%' },
    selectable: false,
    noDisable: false,
    pageSimple: true,
    debug: true,
  });

  const fetchData = async (id: string) => {
    setLoadListParams({ sourceId: id, projectId: appStore.currentProjectId });
    await loadList();
  };

  watchEffect(() => {
    fetchData(props.bugId);
  });
</script>
