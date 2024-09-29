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
        @clear="loadQuoteList"
      />
    </div>

    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #num="{ record }">
        <MsButton type="text" @click="toScenario(record.id)">{{ record.num }}</MsButton>
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
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { RouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    sourceId: string | number;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const keyword = ref('');
  const quoteLocaleMap: Record<string, any> = {
    COPY: 'common.copy',
    REF: 'apiTestManagement.quote',
  };
  const resourceLocaleMap: Record<string, any> = {
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

  function toScenario(id: string) {
    openNewPage(RouteEnum.API_TEST_SCENARIO, {
      id,
    });
  }

  watch(
    () => props.sourceId,
    () => {
      loadQuoteList();
    }
  );

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
    justify-content: space-between;
    align-items: center;
    padding-bottom: 16px;
    flex-direction: row;
  }
</style>
