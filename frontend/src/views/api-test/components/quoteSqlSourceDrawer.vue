<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :width="960"
    :title="t('apiTestDebug.quoteSource')"
    :ok-disabled="selectedKey === ''"
    @confirm="handleConfirm"
  >
    <div class="mb-[16px] flex items-center justify-between">
      <div class="text-[var(--color-text-1)]">{{ t('apiTestDebug.sourceList') }}</div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('project.projectVersion.searchPlaceholder')"
        class="w-[230px]"
        allow-clear
        @search="searchSource"
        @press-enter="searchSource"
      />
    </div>
    <MsBaseTable v-bind="propsRes" v-model:selected-key="selectedKey" v-on="propsEvent">
      <template #timeout="{ record }">
        <a-tooltip :content="record.timeout?.toLocaleString()">
          <div class="one-line-text">{{ record.timeout?.toLocaleString() }}</div>
        </a-tooltip>
      </template>
    </MsBaseTable>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    visible: boolean;
    selectedKey?: string;
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'apply', value: any): void;
  }>();

  const { t } = useI18n();

  const innerVisible = useVModel(props, 'visible', emit);
  const keyword = ref('');
  const selectedKey = ref(props.selectedKey || '');

  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.sqlSourceName',
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.driver',
      dataIndex: 'driver',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.username',
      dataIndex: 'username',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'apiTestDebug.maxConnection',
      dataIndex: 'maxConnection',
      width: 110,
    },
    {
      title: 'apiTestDebug.timeout',
      dataIndex: 'timeout',
      slotName: 'timeout',
      align: 'right',
      width: 120,
    },
  ];
  async function loadSource() {
    return Promise.resolve({
      list: [
        {
          id: '1',
          name: 'test',
          driver: 'com.mysql.cj.jdbc.Driver',
          username: 'root',
          maxConnection: 10,
          timeout: 1000,
          storageType: 'column',
          params: [],
          script: 'select * from test1',
        },
        {
          id: '2',
          name: 'test2',
          driver: 'com.mysql.cj.jdbc.Driver',
          username: 'root',
          maxConnection: 10,
          timeout: 1000,
          storageType: 'column',
          params: [],
          script: 'select * from test2',
        },
        {
          id: '3',
          name: 'test3',
          driver: 'com.mysql.cj.jdbc.Driver',
          username: 'root',
          maxConnection: 10,
          timeout: 10000000000,
          storageType: 'result',
          params: [],
          script: 'select * from test3',
        },
      ],
      total: 99,
    });
  }
  const { propsRes, propsEvent, setLoadListParams, loadList } = useTable(loadSource, {
    columns,
    scroll: { x: '100%' },
    heightUsed: 300,
    selectable: true,
    showSelectorAll: false,
    selectorType: 'radio',
    firstColumnWidth: 44,
  });
  function searchSource() {
    setLoadListParams({
      keyword: keyword.value,
    });
    loadList();
  }
  searchSource();

  function handleConfirm() {
    innerVisible.value = false;
    emit(
      'apply',
      propsRes.value.data.find((item) => item.id === selectedKey.value)
    );
  }
</script>

<style lang="less" scoped>
  :deep(.arco-table-cell-align-left) {
    padding-left: 9px;
  }
</style>
