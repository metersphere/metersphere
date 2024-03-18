<template>
  <div class="history-container">
    <div class="history-table-before">
      <span class="text-[var(--color-text-1)]">{{ t('case.detail.dependency.list') }}</span>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('apiTestManagement.quoteSearchPlaceholder')"
        allow-clear
        class="mr-[8px] w-[240px]"
        @search="loadQuoteList"
        @press-enter="loadQuoteList"
      />
    </div>

    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #id="{ record }">
        <MsButton type="text">{{ record.id }}</MsButton>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getApiCaseDependency } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    sourceId: string | number;
  }>();

  const { t } = useI18n();

  const keyword = ref('');
  const quoteLocaleMap = {
    COPY: 'common.copy',
    REF: 'apiTestManagement.quote',
  };
  const resourceLocaleMap = {
    API: 'case.detail.resource.api',
  };

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 150,
    },
    {
      title: 'apiTestManagement.resourceName',
      dataIndex: 'resourceName',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 150,
    },
    {
      title: 'apiTestManagement.resourceType',
      dataIndex: 'resourceType',
      width: 100,
    },
    {
      title: 'apiTestManagement.quoteType',
      dataIndex: 'refType',
      width: 100,
    },
    {
      title: 'apiTestManagement.belongOrg',
      dataIndex: 'organizationName',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'apiTestManagement.belongProject',
      dataIndex: 'projectName',
      showTooltip: true,
      width: 150,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getApiCaseDependency,
    {
      columns,
      scroll: { x: '100%' },
      selectable: false,
      heightUsed: 374,
    },
    (item) => ({
      ...item,
      refType: t(quoteLocaleMap[item.refType] || ''),
      resourceType: t(resourceLocaleMap[item.resourceType] || ''),
    })
  );

  function loadQuoteList() {
    setLoadListParams({
      keyword: keyword.value,
      resourceId: props.sourceId,
    });
    loadList();
  }

  onBeforeMount(() => {
    loadQuoteList();
  });
</script>

<style lang="less" scoped>
  .history-container {
    @apply h-full overflow-y-auto;

    .ms-scroll-bar();
  }
  .history-table-before {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
</style>
