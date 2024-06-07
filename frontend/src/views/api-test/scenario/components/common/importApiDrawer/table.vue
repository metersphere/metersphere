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
        :placeholder="props.searchPlaceholder"
        allow-clear
        class="mr-[8px] w-[240px]"
        @search="() => loadPage()"
        @press-enter="() => loadPage()"
        @clear="() => loadPage()"
      />
    </div>
    <ms-base-table
      v-bind="currentTable.propsRes.value"
      v-model:selected-key="selectedKey"
      no-disable
      filter-icon-align-left
      v-on="currentTable.propsEvent.value"
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
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetMethodFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
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
              <div class="ml-[6px] flex items-center justify-start px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <div v-if="type === 'scenario'">
                    <a-checkbox v-for="val of Object.values(ApiScenarioStatus)" :key="val" :value="val">
                      <apiStatus :status="val" />
                    </a-checkbox>
                  </div>
                  <div v-else>
                    <a-checkbox v-for="val of Object.values(RequestDefinitionStatus)" :key="val" :value="val">
                      <apiStatus :status="val" />
                    </a-checkbox>
                  </div>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetStatusFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
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
      <template #priority="{ record }">
        <caseLevel :case-level="record.priority" />
      </template>
      <template #priorityFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="priorityFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton type="text" class="arco-btn-text--secondary ml-[10px]" @click="priorityFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="priorityFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="ml-[6px] flex items-center justify-start px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="priorityFilters" direction="vertical" size="small">
                  <a-checkbox v-for="item of casePriorityOptions" :key="item.value" :value="item.value">
                    <caseLevel :case-level="item.label as CaseLevel" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetPriorityFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #stepTotal="{ record }">
        {{ record.stepTotal }}
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { RouteRecordName } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableDataItem } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import { getCasePage, getDefinitionPage } from '@/api/modules/api-test/management';
  import { getScenarioPage } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { ApiCaseDetail, ApiDefinitionDetail } from '@/models/apiTest/management';
  import { ApiScenarioTableItem } from '@/models/apiTest/scenario';
  import { ApiScenarioStatus, RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { SelectAllEnum } from '@/enums/tableEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';

  const props = defineProps<{
    type: 'api' | 'case' | 'scenario';
    module: MsTreeNodeData;
    protocol: string;
    projectId: string | number;
    moduleIds: (string | number)[]; // 模块 id 以及它的子孙模块 id集合
    selectedApis: MsTableDataItem<ApiDefinitionDetail>[]; // 已选中的接口
    selectedCases: MsTableDataItem<ApiCaseDetail>[]; // 已选中的用例
    selectedScenarios: MsTableDataItem<ApiScenarioTableItem>[]; // 已选中的场景
    scenarioId?: string | number;
    caseId?: string | number;
    apiId?: string | number;
    singleSelect?: boolean; // 是否单选
    searchPlaceholder: string; // 搜索框提示词。场景和接口、用例不一样，需要区分。
  }>();
  const emit = defineEmits<{
    (e: 'select', data: MsTableDataItem<ApiCaseDetail | ApiDefinitionDetail | ApiScenarioTableItem>[]): void;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();
  const priorityFilterVisible = ref(false);
  const priorityFilters = ref<string[]>([]);
  const keyword = ref('');

  const tableConfig = {
    scroll: { x: 700 },
    selectable: true,
    showSelectorAll: false,
    heightUsed: 300,
    selectorType: props.singleSelect ? 'radio' : ('checkbox' as 'checkbox' | 'radio' | 'none' | undefined),
  };
  // 接口定义表格
  const useApiTable = useTable(getDefinitionPage, {
    ...tableConfig,
    columns: [
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
        width: 120,
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
        title: 'apiTestManagement.path',
        dataIndex: 'path',
        showTooltip: true,
        width: 200,
      },
      {
        title: 'apiTestManagement.apiStatus',
        dataIndex: 'status',
        slotName: 'status',
        titleSlotName: 'statusFilter',
        width: 130,
      },
      {
        title: 'common.tag',
        dataIndex: 'tags',
        isTag: true,
        isStringTag: true,
        width: 150,
      },
    ],
  });
  // 接口用例表格
  const useCaseTable = useTable(getCasePage, {
    ...tableConfig,
    columns: [
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
        width: 130,
        ellipsis: true,
        showTooltip: true,
      },
      {
        title: 'case.caseName',
        dataIndex: 'name',
        showTooltip: true,
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
        width: 180,
      },
      {
        title: 'case.caseLevel',
        dataIndex: 'priority',
        slotName: 'priority',
        titleSlotName: 'priorityFilter',
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
        width: 150,
      },
      {
        title: 'apiTestManagement.apiStatus',
        dataIndex: 'status',
        slotName: 'status',
        titleSlotName: 'statusFilter',
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
        width: 150,
      },
      {
        title: 'apiTestManagement.path',
        dataIndex: 'path',
        showTooltip: true,
        width: 150,
      },
      {
        title: 'common.tag',
        dataIndex: 'tags',
        isTag: true,
        isStringTag: true,
      },
    ],
  });
  // 接口场景表格
  const useScenarioTable = useTable(getScenarioPage, {
    ...tableConfig,
    columns: [
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
        showTooltip: true,
        columnSelectorDisabled: true,
      },
      {
        title: 'apiScenario.table.columns.name',
        dataIndex: 'name',
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
        width: 134,
        showTooltip: true,
        columnSelectorDisabled: true,
      },
      {
        title: 'apiScenario.table.columns.level',
        dataIndex: 'priority',
        slotName: 'priority',
        titleSlotName: 'priorityFilter',
        width: 140,
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
      },
      {
        title: 'apiScenario.table.columns.status',
        dataIndex: 'status',
        slotName: 'status',
        titleSlotName: 'statusFilter',
        width: 140,
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
      },
      {
        title: 'apiScenario.table.columns.tags',
        dataIndex: 'tags',
        isTag: true,
        isStringTag: true,
        width: 240,
      },
      {
        title: 'apiScenario.table.columns.scenarioEnv',
        dataIndex: 'environmentName',
        width: 159,
        showTooltip: true,
      },
      {
        title: 'apiScenario.table.columns.steps',
        dataIndex: 'stepTotal',
        slotName: 'stepTotal',
        width: 100,
      },
      {
        title: 'apiScenario.table.columns.module',
        dataIndex: 'modulePath',
        width: 120,
        showTooltip: true,
      },
    ],
  });

  const methodFilterVisible = ref(false);
  const methodFilters = ref<string[]>([]);
  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>([]);
  const tableSelectedData = ref<MsTableDataItem<ApiCaseDetail | ApiDefinitionDetail | ApiScenarioTableItem>[]>([]);
  const tableSelectedKeys = computed(() => {
    return tableSelectedData.value.map((e) => e.id);
  });
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

  /**
   * 表格单行选中事件处理
   */
  function handleRowSelectChange(record: ApiCaseDetail | ApiDefinitionDetail | ApiScenarioTableItem) {
    const selectedData = currentTable.value.propsRes.value.data.find((e: any) => e.id === record.id);
    if (tableSelectedKeys.value.includes(record.id)) {
      // 取消选中
      tableSelectedData.value = tableSelectedData.value.filter((e) => e.id !== record.id);
    } else if (selectedData) {
      tableSelectedData.value.push(selectedData);
    }
    emit('select', tableSelectedData.value);
  }

  const selectedKey = ref('');

  watch(
    () => selectedKey.value,
    (val) => {
      const selectedData = currentTable.value.propsRes.value.data.find((e: any) => e.id === val);
      tableSelectedData.value = selectedData ? [selectedData] : [];
      emit('select', tableSelectedData.value);
    }
  );

  function clearSelector() {
    tableSelectedData.value = [];
    currentTable.value.clearSelector();
    emit('select', []);
  }

  /**
   * 表格全选事件处理
   */
  function handleSelectAllChange(v: SelectAllEnum) {
    if (v === SelectAllEnum.CURRENT) {
      tableSelectedData.value.push(...currentTable.value.propsRes.value.data);
    } else {
      const dataSet = new Set(currentTable.value.propsRes.value.data.map((e) => e.id));
      tableSelectedData.value = tableSelectedData.value.filter((e) => !dataSet.has(e.id));
    }
    emit('select', tableSelectedData.value);
  }

  // 绑定表格事件
  useApiTable.propsEvent.value.rowSelectChange = handleRowSelectChange;
  useApiTable.propsEvent.value.selectAllChange = handleSelectAllChange;
  useApiTable.propsEvent.value.clearSelector = clearSelector;
  useCaseTable.propsEvent.value.rowSelectChange = handleRowSelectChange;
  useCaseTable.propsEvent.value.selectAllChange = handleSelectAllChange;
  useCaseTable.propsEvent.value.clearSelector = clearSelector;
  useScenarioTable.propsEvent.value.rowSelectChange = handleRowSelectChange;
  useScenarioTable.propsEvent.value.selectAllChange = handleSelectAllChange;
  useScenarioTable.propsEvent.value.clearSelector = clearSelector;

  function loadPage(ids?: (string | number)[]) {
    nextTick(() => {
      // 等待currentTable计算完毕再调用对应的请求
      currentTable.value.setLoadListParams({
        keyword: keyword.value,
        projectId: props.projectId,
        moduleIds: ids || props.moduleIds,
        protocols: [props.protocol],
        filter: {
          status: statusFilters.value,
          method: methodFilters.value,
          priority: priorityFilters.value,
        },
        excludeIds: [props.scenarioId || '', props.caseId || '', props.apiId || ''],
      });
      currentTable.value.loadList();
    });
  }

  watch(
    () => props.type,
    (val) => {
      switch (val) {
        case 'api':
          tableSelectedData.value = props.selectedApis;
          break;
        case 'case':
          tableSelectedData.value = props.selectedCases;
          break;
        case 'scenario':
        default:
          tableSelectedData.value = props.selectedScenarios;
          break;
      }
      keyword.value = '';
    }
  );

  watch(
    () => tableSelectedKeys.value,
    (arr) => {
      currentTable.value.propsRes.value.selectedKeys = new Set(arr);
    }
  );
  function resetPriorityFilter() {
    priorityFilterVisible.value = false;
    priorityFilters.value = [];
    loadPage();
  }
  function resetStatusFilter() {
    statusFilterVisible.value = false;
    statusFilters.value = [];
    loadPage();
  }
  function handleFilterHidden(val: boolean) {
    if (!val) {
      statusFilterVisible.value = false;
      priorityFilterVisible.value = false;
      methodFilterVisible.value = false;
      statusFilterVisible.value = false;
      loadPage();
    }
  }

  function resetMethodFilter() {
    methodFilters.value = [];
    methodFilterVisible.value = false;
    loadPage();
  }

  function resetTable() {
    currentTable.value.resetSelector();
    keyword.value = '';
    methodFilters.value = [];
    statusFilters.value = [];
    priorityFilters.value = [];
    loadPage();
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
        query.id = id;
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
