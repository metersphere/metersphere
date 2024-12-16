<template>
  <MsBaseTable
    v-if="props.showType === 'API'"
    ref="apiTableRef"
    class="mt-[16px]"
    v-bind="propsRes"
    :action-config="{
      baseAction: [],
      moreAction: [],
    }"
    :not-show-table-filter="props.isAdvancedSearchMode"
    always-show-selected-count
    v-on="propsEvent"
    @filter-change="getModuleCount"
    @row-select-change="rowSelectChange"
    @select-all-change="selectAllChange"
    @clear-selector="clearSelector"
  >
    <template #num="{ record }">
      <MsButton type="text" @click="toDetail(record)">{{ record.num }}</MsButton>
    </template>
    <template #[FilterSlotNameEnum.API_TEST_API_REQUEST_METHODS]="{ filterContent }">
      <apiMethodName :method="filterContent.value" />
    </template>
    <template #method="{ record }">
      <apiMethodName :method="record.method" is-tag />
    </template>
    <template #caseTotal="{ record }">
      {{ record.caseTotal }}
    </template>
    <template #createUserName="{ record }">
      <a-tooltip :content="`${record.createUserName}`" position="tl">
        <div class="one-line-text">{{ record.createUserName }}</div>
      </a-tooltip>
    </template>
    <template #count>
      <slot></slot>
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { type SelectOptionData } from '@arco-design/web-vue';

  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import { ApiDefinitionDetail } from '@/models/apiTest/management';
  import type { TableQueryParams } from '@/models/common';
  import { FilterType } from '@/enums/advancedFilterEnum';
  import { RequestMethods } from '@/enums/apiEnum';
  import { CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { SpecialColumnEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import type { moduleKeysType } from './types';
  import useModuleSelection from './useModuleSelection';
  import { getPublicLinkCaseListMap } from './utils/page';
  import { apiStatusOptions } from '@/views/api-test/components/config';

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();
  const appStore = useAppStore();

  const props = defineProps<{
    associationType: string; // 关联类型 项目 | 测试计划 | 用例评审
    activeModule: string;
    offspringIds: string[];
    currentProject: string;
    associatedIds?: string[]; // 已关联ids
    activeSourceType: keyof typeof CaseLinkEnum;
    selectorAll?: boolean;
    keyword: string;
    showType: string;
    getPageApiType: keyof typeof CasePageApiTypeEnum; // 获取未关联分页Api
    extraTableParams?: TableQueryParams; // 查询表格的额外参数
    allProtocolList: string[];
    isAdvancedSearchMode?: boolean;
    protocols: string[];
    moduleTree: MsTreeNodeData[];
    modulesCount: Record<string, any>;
    testPlanList: SelectOptionData[];
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: TableQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
    (e: 'update:selectedIds'): void;
  }>();

  const tableStore = useTableStore();
  const innerSelectedModulesMaps = defineModel<Record<string, moduleKeysType>>('selectedModulesMaps', {
    required: true,
  });

  const requestMethodsOptions = computed(() => {
    return Object.values(RequestMethods).map((e) => {
      return {
        value: e,
        key: e,
      };
    });
  });

  const columns = computed<MsTableColumn>(() => {
    return [
      {
        title: 'ID',
        dataIndex: 'num',
        slotName: 'num',
        sortIndex: 1,
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
        width: 100,
        columnSelectorDisabled: true,
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
        columnSelectorDisabled: true,
      },
      {
        title: 'apiTestManagement.apiType',
        dataIndex: 'method',
        slotName: 'method',
        width: 140,
        showDrag: true,
        filterConfig: {
          options: requestMethodsOptions.value,
          filterSlotName: FilterSlotNameEnum.API_TEST_API_REQUEST_METHODS,
        },
      },
      {
        title: 'apiTestManagement.path',
        dataIndex: 'path',
        showTooltip: true,
        width: 200,
        showDrag: true,
      },
      {
        title: 'common.tag',
        dataIndex: 'tags',
        isTag: true,
        isStringTag: true,
        showDrag: true,
      },
      {
        title: 'apiTestManagement.caseTotal',
        dataIndex: 'caseTotal',
        showTooltip: true,
        width: 100,
        showDrag: true,
        slotName: 'caseTotal',
      },
      {
        title: 'common.creator',
        slotName: 'createUserName',
        dataIndex: 'createUser',
        filterConfig: {
          mode: 'remote',
          loadOptionParams: {
            projectId: appStore.currentProjectId,
          },
          remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
        },
        showInTable: true,
        width: 200,
        showDrag: true,
      },
      {
        title: '',
        dataIndex: 'action',
        width: 24,
        slotName: SpecialColumnEnum.ACTION,
        fixed: 'right',
        cellClass: 'operator-class',
      },
    ];
  });

  const {
    propsRes,
    propsEvent,
    viewId,
    advanceFilter,
    setAdvanceFilter,
    loadList,
    setLoadListParams,
    setPagination,
    resetFilterParams,
    setTableSelected,
    resetSelector,
  } = useTable(getPublicLinkCaseListMap[props.getPageApiType][props.activeSourceType].API, {
    tableKey: TableKeyEnum.ASSOCIATE_CASE_API,
    showSetting: true,
    isSimpleSetting: true,
    onlyPageSize: true,
    selectable: true,
    showSelectAll: true,
    heightUsed: 310,
    showSelectorAll: false,
  });

  async function getTableParams() {
    const { excludeKeys } = propsRes.value;
    return {
      keyword: props.keyword,
      projectId: props.currentProject,
      moduleIds: props.activeModule === 'all' || !props.activeModule ? [] : [props.activeModule, ...props.offspringIds],
      excludeIds: [...excludeKeys],
      filter: propsRes.value.filter,
      ...props.extraTableParams,
      protocols: props.protocols || [],
    };
  }

  async function getModuleCount() {
    const tableParams = await getTableParams();
    emit('getModuleCount', {
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }

  async function loadApiList() {
    if (props.associatedIds && props.associatedIds.length) {
      props.associatedIds.forEach((hasNotAssociatedId) => {
        setTableSelected(hasNotAssociatedId);
      });
    }
    const tableParams = await getTableParams();
    setLoadListParams({
      ...tableParams,
      moduleIds: props.isAdvancedSearchMode ? [] : tableParams.moduleIds,
      protocols: props.isAdvancedSearchMode ? props.allProtocolList : props.protocols || [],
      viewId: viewId.value,
      combineSearch: advanceFilter,
    });
    loadList();
    if (!props.isAdvancedSearchMode) {
      emit('getModuleCount', {
        ...tableParams,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
      });
    }
  }

  const filterConfigList = computed<FilterFormItem[]>(() => [
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
        options: props.allProtocolList?.map((item) => ({ label: item, value: item })),
      },
    },
    {
      title: 'apiTestManagement.apiType',
      dataIndex: 'method',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'key',
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
      title: 'ms.taskCenter.taskBelongTestPlan',
      dataIndex: 'belongTestPlan',
      type: FilterType.SELECT_EQUAL,
      selectProps: {
        options: props.testPlanList,
        optionTooltipPosition: 'tr',
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
  function setCaseAdvanceFilter(filter: FilterResult, id: string) {
    setAdvanceFilter(filter, id);
  }
  const apiTableRef = ref<InstanceType<typeof MsBaseTable>>();

  watch(
    [() => props.currentProject, () => props.protocols],
    () => {
      setPagination({
        current: 1,
      });
      apiTableRef.value?.initColumn(columns.value);
      resetFilterParams();
      loadApiList();
    },
    {
      deep: true,
    }
  );

  const innerSelectedIds = defineModel<string[]>('selectedIds', { required: true });
  const selectIds = computed(() => {
    return [...propsRes.value.selectedKeys];
  });

  watch(
    () => selectIds.value,
    (val) => {
      innerSelectedIds.value = val;
    }
  );

  watch(
    () => props.activeModule,
    (val) => {
      if (val && !props.isAdvancedSearchMode) {
        resetSelector();
        resetFilterParams();
        loadApiList();
      }
    }
  );

  function getApiSaveParams() {
    const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
    const tableParams = getTableParams();
    return {
      ...tableParams,
      excludeIds: [...excludeKeys],
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
      associateApiType: 'API',
    };
  }

  // 去接口详情页面
  function toDetail(record: ApiDefinitionDetail) {
    openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
      dId: record.id,
      pId: record.projectId,
    });
  }

  const tableSelectedProps = ref({
    modulesTree: props.moduleTree,
    moduleCount: props.modulesCount,
  });
  const { rowSelectChange, selectAllChange, clearSelector } = useModuleSelection(
    innerSelectedModulesMaps.value,
    propsRes.value,
    tableSelectedProps.value
  );

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

  onMounted(() => {
    loadApiList();
  });

  defineExpose({
    getApiSaveParams,
    loadApiList,
    filterConfigList,
    setCaseAdvanceFilter,
  });

  await tableStore.initColumn(TableKeyEnum.ASSOCIATE_CASE_API, columns.value, 'drawer');
</script>

<style lang="less" scoped>
  :deep(.operator-class) {
    .arco-table-cell-align-left {
      padding: 0 8px !important;
    }
  }
</style>
