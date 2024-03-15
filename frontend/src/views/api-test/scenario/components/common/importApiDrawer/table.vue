<template>
  <div class="min-w-[380px]">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="flex items-center">
        <a-tooltip :content="props.module.name">
          <div class="one-line-text max-w-[200px]">{{ props.module.name }}</div>
        </a-tooltip>
        <div>（{{ currentTable.propsRes.value.msPagination?.total }}）</div>
      </div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('apiScenario.quoteTableSearchTip')"
        allow-clear
        class="mr-[8px] w-[240px]"
        @search="() => loadPage()"
        @press-enter="() => loadPage()"
        @clear="() => loadPage()"
      />
    </div>
    <ms-base-table
      v-bind="currentTable.propsRes.value"
      no-disable
      filter-icon-align-left
      v-on="currentTable.propsEvent.value"
      @selected-change="handleTableSelect"
    >
      <template v-if="props.protocol === 'HTTP'" #methodFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="methodFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton type="text" class="arco-btn-text--secondary" @click="methodFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="methodFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="methodFilters" direction="vertical" size="small">
                  <a-checkbox v-for="key of RequestMethods" :key="key" :value="key">
                    <apiMethodName :method="key" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #statusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="statusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton type="text" class="arco-btn-text--secondary" @click="statusFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <a-checkbox v-for="val of Object.values(RequestDefinitionStatus)" :key="val" :value="val">
                    <apiStatus :status="val" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #num="{ record }">
        <MsButton type="text" @click="openApiDetail(record.id)">{{ record.num }}</MsButton>
      </template>
      <template #method="{ record }">
        <apiMethodName :method="record.method" tag-size="small" is-tag />
      </template>
      <template #status="{ record }">
        <apiStatus :status="record.status" size="small" />
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { RouteRecordName } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import { getCasePage, getDefinitionPage } from '@/api/modules/api-test/management';
  import { getScenarioPage } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    type: 'api' | 'case' | 'scenario';
    module: MsTreeNodeData;
    protocol: string;
    projectId: string | number;
    moduleIds: (string | number)[]; // 模块 id 以及它的子孙模块 id集合
  }>();
  const emit = defineEmits<{
    (e: 'select', ids: (string | number)[]): void;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const keyword = ref('');

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
      fixed: 'left',
      width: 100,
    },
    {
      title: 'apiTestManagement.apiName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
    },
    {
      title: 'apiTestManagement.apiType',
      dataIndex: 'method',
      slotName: 'method',
      titleSlotName: 'methodFilter',
      width: 140,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      width: 130,
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'apiTestManagement.version',
      dataIndex: 'versionName',
      width: 100,
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      width: 150,
    },
  ];
  const tableConfig = {
    columns,
    scroll: { x: 700 },
    selectable: true,
    showSelectorAll: false,
    heightUsed: 300,
  };
  // 接口定义表格
  const useApiTable = useTable(getDefinitionPage, tableConfig);
  // 接口用例表格
  const useCaseTable = useTable(getCasePage, tableConfig);
  // 接口场景表格
  const useScenarioTable = useTable(getScenarioPage, tableConfig);

  const methodFilterVisible = ref(false);
  const methodFilters = ref(Object.keys(RequestMethods));
  const statusFilterVisible = ref(false);
  const statusFilters = ref(Object.keys(RequestDefinitionStatus));
  const tableSelected = ref<(string | number)[]>([]);
  // 当前展示的表格数据类型
  const currentTable = computed(() => {
    switch (props.type) {
      case 'api':
        return useApiTable;
      case 'case':
        return useCaseTable;
      case 'scenario':
      default:
        return useScenarioTable;
    }
  });

  function loadPage(ids?: (string | number)[]) {
    nextTick(() => {
      // 等待currentTable计算完毕再调用对应的请求
      currentTable.value.setLoadListParams({
        keyword: keyword.value,
        projectId: props.projectId,
        moduleIds: ids || props.moduleIds,
        protocol: props.protocol,
        filter: {
          status:
            statusFilters.value.length === Object.keys(RequestDefinitionStatus).length
              ? undefined
              : statusFilters.value,
          method: methodFilters.value.length === Object.keys(RequestMethods).length ? undefined : methodFilters.value,
        },
      });
      currentTable.value.loadList();
    });
  }

  function handleFilterHidden(val: boolean) {
    if (!val) {
      loadPage();
    }
  }

  function resetTable() {
    currentTable.value.resetSelector();
    keyword.value = '';
    methodFilters.value = Object.keys(RequestMethods);
    statusFilters.value = Object.keys(RequestDefinitionStatus);
    loadPage();
  }

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
    emit('select', arr);
  }

  function openApiDetail(id: string | number) {
    let routeName: RouteRecordName;
    const query: Record<string, any> = {};
    switch (props.type) {
      case 'api':
        routeName = ApiTestRouteEnum.API_TEST_MANAGEMENT;
        query.dId = id;
        break;
      case 'case':
        routeName = ApiTestRouteEnum.API_TEST_MANAGEMENT;
        query.cId = id;
        break;
      case 'scenario':
      default:
        routeName = ApiTestRouteEnum.API_TEST_SCENARIO;
        break;
    }
    openNewPage(routeName, query);
  }

  defineExpose({
    resetTable,
    loadPage,
  });
</script>

<style lang="less" scoped></style>
