<template>
  <div class="history-container">
    <a-input-search
      v-model:model-value="keyword"
      :placeholder="t('apiTestManagement.quoteSearchPlaceholder')"
      allow-clear
      class="mr-[8px] w-[240px]"
      @search="loadQuoteList"
      @press-enter="loadQuoteList"
    />
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #id="{ record }">
        <MsButton type="text" @click="gotoResource(record)">{{ record.id }}</MsButton>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  // import { useRouter } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const props = defineProps<{
    sourceId: string | number;
  }>();

  // const router = useRouter();
  const appStore = useAppStore();
  const { t } = useI18n();

  const keyword = ref('');
  const quoteLocaleMap = {
    COPY: 'common.copy',
    QUOTE: 'apiTestManagement.quote',
  };

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'id',
      slotName: 'id',
      width: 150,
    },
    {
      title: 'apiTestManagement.resourceName',
      dataIndex: 'resourceName',
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
      dataIndex: 'quoteType',
      width: 100,
    },
    {
      title: 'apiTestManagement.belongOrg',
      dataIndex: 'belongOrg',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'apiTestManagement.belongProject',
      dataIndex: 'belongProject',
      showTooltip: true,
      width: 150,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    () =>
      Promise.resolve({
        list: [
          {
            id: '1',
            resourceName: '资源名称',
            resourceType: '资源类型',
            quoteType: '引用类型',
            belongOrg: '所属组织',
            belongProject: '所属项目',
          },
        ],
        total: 1,
      }),
    {
      columns,
      scroll: { x: '100%' },
      selectable: false,
      heightUsed: 374,
    },
    (item) => ({
      ...item,
      type: t(quoteLocaleMap[item.type] || ''),
    })
  );

  function loadQuoteList() {
    setLoadListParams({
      projectId: appStore.currentProjectId,
      sourceId: props.sourceId,
      keyword: keyword.value,
    });
    loadList();
  }

  function gotoResource(record: any) {
    // router.push({
    //   name: 'apiTestManagementApiPreview',
    //   query: {
    //     id: record.id,
    //   },
    // });
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
</style>
