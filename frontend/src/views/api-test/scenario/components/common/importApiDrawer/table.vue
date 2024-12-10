<template>
  <div class="min-w-[380px]">
    <MsAdvanceFilter
      ref="msAdvanceFilterRef"
      v-model:keyword="keyword"
      :view-type="viewType"
      :count="currentTable.propsRes.value.msPagination?.total || 0"
      :name="props.module.name"
      :filter-config-list="filterConfigList"
      :search-placeholder="props.searchPlaceholder"
      @keyword-search="loadPage()"
      @adv-search="handleAdvSearch"
      @refresh="loadPage()"
    />
    <ms-base-table
      v-bind="currentTable.propsRes.value"
      v-model:selected-key="selectedKey"
      class="mt-[16px]"
      :not-show-table-filter="isAdvancedSearchMode"
      no-disable
      filter-icon-align-left
      v-on="currentTable.propsEvent.value"
      @row-select-change="currentUseModuleSelection.rowSelectChange"
      @select-all-change="currentUseModuleSelection.selectAllChange"
      @clear-selector="currentUseModuleSelection.clearSelector"
    >
      <template v-if="props.protocol === 'HTTP' && !isAdvancedSearchMode" #methodFilter="{ columnConfig }">
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
      <template v-if="!isAdvancedSearchMode" #statusFilter="{ columnConfig }">
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
      <template v-if="!isAdvancedSearchMode" #priorityFilter="{ columnConfig }">
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
  import { SelectOptionData } from '@arco-design/web-vue';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import type { moduleKeysType } from '@/components/business/ms-associate-case/types';
  import useModuleSelection from '@/components/business/ms-associate-case/useModuleSelection';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import { getCasePage, getDefinitionPage } from '@/api/modules/api-test/management';
  import { getScenarioPage } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';

  import { ModuleTreeNode } from '@/models/common';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { ApiScenarioStatus, RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  import {
    apiStatusOptions,
    casePriorityOptions,
    caseStatusOptions,
    lastReportStatusListOptions,
  } from '@/views/api-test/components/config';
  import { scenarioStatusOptions } from '@/views/api-test/scenario/components/config';

  const props = defineProps<{
    type: 'api' | 'case' | 'scenario';
    module: MsTreeNodeData;
    protocol: string;
    protocolsParam: string[];
    protocolOptions: SelectOptionData[];
    projectId: string | number;
    moduleIds: (string | number)[]; // 模块 id 以及它的子孙模块 id集合
    scenarioId?: string | number;
    caseId?: string | number;
    apiId?: string | number;
    singleSelect?: boolean; // 是否单选
    searchPlaceholder: string; // 搜索框提示词。场景和接口、用例不一样，需要区分。
    moduleTree: ModuleTreeNode[];
    modulesCount: Record<string, any>;
  }>();

  const emit = defineEmits<{
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();
  const appStore = useAppStore();
  const priorityFilterVisible = ref(false);
  const priorityFilters = ref<string[]>([]);
  const keyword = ref('');

  const innerApiSelectedModulesMaps = defineModel<Record<string, moduleKeysType>>('apiSelectedModulesMaps', {
    required: true,
  });
  const innerCaseSelectedModulesMaps = defineModel<Record<string, moduleKeysType>>('caseSelectedModulesMaps', {
    required: true,
  });
  const innerScenarioSelectedModulesMaps = defineModel<Record<string, moduleKeysType>>('scenarioSelectedModulesMaps', {
    required: true,
  });

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

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);

  function loadPage(ids?: (string | number)[]) {
    nextTick(() => {
      // 等待currentTable计算完毕再调用对应的请求
      currentTable.value.setLoadListParams({
        keyword: keyword.value,
        projectId: props.projectId,
        moduleIds: isAdvancedSearchMode.value ? [] : ids || props.moduleIds,
        protocols: props.protocolsParam,
        filter: {
          status: statusFilters.value,
          method: methodFilters.value,
          priority: priorityFilters.value,
        },
        viewId: currentTable.value.viewId.value,
        combineSearch: currentTable.value.advanceFilter,
        excludeIds: [props.scenarioId || '', props.caseId || '', props.apiId || ''],
      });
      currentTable.value.loadList();
    });
  }

  watch(
    () => props.type,
    () => {
      keyword.value = '';
      msAdvanceFilterRef.value?.clearFilter();
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
  }

  const requestMethodsOptions = computed(() => {
    const otherMethods = props.protocolOptions.filter((e) => e.value !== 'HTTP');
    const httpMethods = Object.values(RequestMethods).map((e) => {
      return {
        value: e,
        label: e,
      };
    });
    return [...httpMethods, ...otherMethods];
  });
  const caseFilterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      type: FilterType.INPUT,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleId',
      type: FilterType.TREE_SELECT,
      treeSelectData: props.moduleTree,
      treeSelectProps: {
        fieldNames: {
          title: 'name',
          key: 'id',
          children: 'children',
        },
        multiple: true,
        treeCheckable: true,
        treeCheckStrictly: true,
      },
    },
    {
      title: 'apiTestManagement.protocol',
      dataIndex: 'protocol',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: props.protocolOptions,
      },
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: casePriorityOptions,
      },
    },
    {
      title: 'case.apiParamsChange',
      dataIndex: 'apiChange',
      type: FilterType.SELECT_EQUAL,
      selectProps: {
        options: [
          { label: t('case.withoutChanges'), value: false },
          { label: t('case.withChanges'), value: true },
        ],
      },
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      type: FilterType.INPUT,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: caseStatusOptions.map((item) => ({ label: t(item.label), value: item.value })),
      },
    },
    {
      title: 'case.lastReportStatus',
      dataIndex: 'lastReportStatus',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: lastReportStatusListOptions.value,
      },
    },
    {
      title: 'case.caseEnvironment',
      dataIndex: 'environmentName',
      type: FilterType.SELECT,
      selectProps: {
        labelKey: 'name',
        valueKey: 'id',
        multiple: true,
        options: appStore.envList,
      },
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      type: FilterType.TAGS_INPUT,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
    {
      title: 'common.creator',
      dataIndex: 'createUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
    {
      title: 'common.updateUserName',
      dataIndex: 'updateUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.updateTime',
      dataIndex: 'updateTime',
      type: FilterType.DATE_PICKER,
    },
  ]);
  const apiFilterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'apiTestManagement.apiName',
      dataIndex: 'name',
      type: FilterType.INPUT,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleId',
      type: FilterType.TREE_SELECT,
      treeSelectData: props.moduleTree,
      treeSelectProps: {
        fieldNames: {
          title: 'name',
          key: 'id',
          children: 'children',
        },
        multiple: true,
        treeCheckable: true,
        treeCheckStrictly: true,
      },
    },
    {
      title: 'apiTestManagement.protocol',
      dataIndex: 'protocol',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: props.protocolOptions,
      },
    },
    {
      title: 'apiTestManagement.apiType',
      dataIndex: 'method',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: requestMethodsOptions.value,
      },
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      type: FilterType.INPUT,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'name',
        options: apiStatusOptions,
      },
    },
    {
      title: 'apiTestManagement.caseTotal',
      dataIndex: 'caseTotal',
      type: FilterType.NUMBER,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      type: FilterType.TAGS_INPUT,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
    {
      title: 'common.creator',
      dataIndex: 'createUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
    {
      title: 'common.updateTime',
      dataIndex: 'updateTime',
      type: FilterType.DATE_PICKER,
    },
  ]);
  const scenarioFilterConfigList = computed<FilterFormItem[]>(() => {
    return [
      {
        title: 'caseManagement.featureCase.tableColumnID',
        dataIndex: 'num',
        type: FilterType.INPUT,
      },
      {
        title: 'apiScenario.table.columns.name',
        dataIndex: 'name',
        type: FilterType.INPUT,
      },
      {
        title: 'common.belongModule',
        dataIndex: 'moduleId',
        type: FilterType.TREE_SELECT,
        treeSelectData: props.moduleTree,
        treeSelectProps: {
          fieldNames: {
            title: 'name',
            key: 'id',
            children: 'children',
          },
          multiple: true,
          treeCheckable: true,
          treeCheckStrictly: true,
        },
      },
      {
        title: 'apiScenario.table.columns.level',
        dataIndex: 'priority',
        type: FilterType.SELECT,
        selectProps: {
          multiple: true,
          options: casePriorityOptions,
        },
      },
      {
        title: 'apiScenario.table.columns.status',
        dataIndex: 'status',
        type: FilterType.SELECT,
        selectProps: {
          multiple: true,
          options: scenarioStatusOptions,
        },
      },
      {
        title: 'apiScenario.table.columns.runResult',
        dataIndex: 'lastReportStatus',
        type: FilterType.SELECT,
        selectProps: {
          multiple: true,
          options: lastReportStatusListOptions.value,
        },
      },
      {
        title: 'common.tag',
        dataIndex: 'tags',
        type: FilterType.TAGS_INPUT,
        numberProps: {
          min: 0,
          precision: 0,
        },
      },
      {
        title: 'apiScenario.table.columns.scenarioEnv',
        dataIndex: 'environmentName',
        type: FilterType.SELECT,
        selectProps: {
          labelKey: 'name',
          valueKey: 'id',
          multiple: true,
          options: appStore.envList,
        },
      },
      {
        title: 'apiScenario.table.columns.steps',
        dataIndex: 'stepTotal',
        type: FilterType.NUMBER,
        numberProps: {
          min: 0,
          precision: 0,
        },
      },
      {
        title: 'common.creator',
        dataIndex: 'createUser',
        type: FilterType.MEMBER,
      },
      {
        title: 'common.createTime',
        dataIndex: 'createTime',
        type: FilterType.DATE_PICKER,
      },
      {
        title: 'apiScenario.table.columns.updateUser',
        dataIndex: 'updateUser',
        type: FilterType.MEMBER,
      },
      {
        title: 'common.updateTime',
        dataIndex: 'updateTime',
        type: FilterType.DATE_PICKER,
      },
    ];
  });

  const filterConfigList = computed<FilterFormItem[]>(() => {
    switch (props.type) {
      case 'api':
        return apiFilterConfigList.value;
      case 'case':
        return caseFilterConfigList.value;
      case 'scenario':
      default:
        return scenarioFilterConfigList.value;
    }
  });

  const viewType = computed(() => {
    switch (props.type) {
      case 'api':
        return ViewTypeEnum.API_DEFINITION;
      case 'case':
        return ViewTypeEnum.API_CASE;
      case 'scenario':
      default:
        return ViewTypeEnum.API_SCENARIO;
    }
  });

  // 高级检索
  const handleAdvSearch = async (filter: FilterResult, id: string, isStartAdvance: boolean) => {
    emit('handleAdvSearch', isStartAdvance);
    currentTable.value.setAdvanceFilter(filter, id);
    resetTable(); // 基础筛选都清空
    if (isStartAdvance) {
      loadPage();
    }
  };

  const tableSelectedProps = ref({
    modulesTree: props.moduleTree,
    moduleCount: props.modulesCount,
  });

  watch(
    () => props.moduleTree,
    (val) => {
      if (val) {
        tableSelectedProps.value.modulesTree = val;
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.modulesCount,
    (val) => {
      if (val) {
        tableSelectedProps.value.moduleCount = val;
      }
    },
    {
      immediate: true,
    }
  );

  const apiUseModuleSelection = useModuleSelection(
    innerApiSelectedModulesMaps.value,
    useApiTable.propsRes.value,
    tableSelectedProps.value
  );

  const caseUseModuleSelection = useModuleSelection(
    innerCaseSelectedModulesMaps.value,
    useCaseTable.propsRes.value,
    tableSelectedProps.value
  );

  const scenarioUseModuleSelection = useModuleSelection(
    innerScenarioSelectedModulesMaps.value,
    useScenarioTable.propsRes.value,
    tableSelectedProps.value
  );

  const useModuleHooksMap = ref<Record<string, any>>({
    API: apiUseModuleSelection,
    CASE: caseUseModuleSelection,
    SCENARIO: scenarioUseModuleSelection,
  });

  const currentUseModuleSelection = ref();

  watch(
    () => props.type,
    (val) => {
      const key = (val || 'api').toLocaleUpperCase();
      currentUseModuleSelection.value = useModuleHooksMap.value[key];
    },
    {
      immediate: true,
    }
  );

  function clearSelectorAll() {
    apiUseModuleSelection.clearSelector();
    caseUseModuleSelection.clearSelector();
    scenarioUseModuleSelection.clearSelector();
  }

  const selectedKey = ref('');

  watch(
    () => selectedKey.value,
    (val) => {
      const selectedData = currentTable.value.propsRes.value.data.find((e: any) => e.id === val);
      if (selectedData) {
        clearSelectorAll();
        currentUseModuleSelection.value.setUnSelectNode(selectedData.moduleId);
        currentUseModuleSelection.value.updateSelectModule(selectedData.moduleId, selectedData.id);
      }
    }
  );

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
    loadPage,
  });
</script>

<style lang="less" scoped></style>
