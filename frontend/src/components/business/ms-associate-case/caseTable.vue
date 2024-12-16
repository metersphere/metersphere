<template>
  <MsBaseTable
    ref="tableRef"
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
    <template #reviewStatus="{ record }">
      <MsIcon
        :type="statusIconMap[record.reviewStatus]?.icon || ''"
        class="mr-1"
        :class="[statusIconMap[record.reviewStatus].color]"
      ></MsIcon>
      <span>{{ statusIconMap[record.reviewStatus]?.statusText || '' }} </span>
    </template>
    <template #lastExecuteResult="{ record }">
      <ExecuteResult v-if="record.lastExecuteResult" :execute-result="record.lastExecuteResult" />
      <span v-else>-</span>
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
      <CaseLevel :case-level="filterContent.value" />
    </template>
    <template #caseLevel="{ record }">
      <CaseLevel :case-level="record.caseLevel" />
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
      <ExecuteResult :execute-result="filterContent.value" />
    </template>
    <template #lastExecResult="{ record }">
      <ExecuteResult :execute-result="record.lastExecResult" />
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
  import { ref } from 'vue';
  import { type SelectOptionData } from '@arco-design/web-vue';

  import { getFilterCustomFields } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { getCustomFieldsTable } from '@/api/modules/case-management/featureCase';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';

  import type { CaseManagementTable } from '@/models/caseManagement/featureCase';
  import type { TableQueryParams } from '@/models/common';
  import { FilterType } from '@/enums/advancedFilterEnum';
  import { CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { SpecialColumnEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import type { moduleKeysType } from './types';
  import useModuleSelection from './useModuleSelection';
  import { getPublicLinkCaseListMap } from './utils/page';
  import { casePriorityOptions } from '@/views/api-test/components/config';
  import {
    executionResultMap,
    getCaseLevels,
    statusIconMap,
  } from '@/views/case-management/caseManagementFeature/components/utils';

  const { openNewPage } = useOpenNewPage();

  const props = defineProps<{
    associationType: string; // 关联类型 项目 | 测试计划 | 用例评审
    activeModule: string;
    offspringIds: string[];
    currentProject: string;
    associatedIds?: string[]; // 已关联ids
    activeSourceType: keyof typeof CaseLinkEnum;
    keyword: string;
    moduleTree: MsTreeNodeData[];
    modulesCount: Record<string, any>;
    getPageApiType: keyof typeof CasePageApiTypeEnum; // 获取未关联分页Api
    extraTableParams?: TableQueryParams; // 查询表格的额外参数
    isAdvancedSearchMode?: boolean;
    testPlanList: SelectOptionData[];
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: TableQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
    (e: 'update:selectedIds'): void;
    (e: 'clearSelect'): void;
  }>();

  const tableStore = useTableStore();

  const innerSelectedIds = defineModel<string[]>('selectedIds', { required: true });

  const innerSelectedModulesMaps = defineModel<Record<string, moduleKeysType>>('selectedModulesMaps', {
    required: true,
  });

  const reviewResultOptions = computed(() => {
    return Object.keys(statusIconMap).map((key) => {
      return {
        value: key,
        label: statusIconMap[key].statusText,
      };
    });
  });
  const executeResultOptions = computed(() => {
    return Object.keys(executionResultMap).map((key) => {
      return {
        value: key,
        label: executionResultMap[key].statusText,
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
        width: 150,
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
        columnSelectorDisabled: true,
      },
      {
        title: 'case.caseLevel',
        dataIndex: 'caseLevel',
        slotName: 'caseLevel',
        filterConfig: {
          options: casePriorityOptions,
          filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
        },
        width: 150,
        showDrag: true,
      },
      {
        title: 'caseManagement.featureCase.tableColumnReviewResult',
        dataIndex: 'reviewStatus',
        slotName: 'reviewStatus',
        filterConfig: {
          options: reviewResultOptions.value,
          filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_RESULT,
        },
        showInTable: true,
        width: 150,
        showDrag: true,
      },
      {
        title: 'caseManagement.featureCase.tableColumnExecutionResult',
        dataIndex: 'lastExecuteResult',
        slotName: 'lastExecuteResult',
        filterConfig: {
          options: executeResultOptions.value,
          filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
        },
        showInTable: true,
        width: 150,
        showDrag: true,
      },
      {
        title: 'common.tag',
        dataIndex: 'tags',
        isTag: true,
        isStringTag: true,
        width: 400,
        showDrag: true,
      },
      {
        title: 'caseManagement.featureCase.tableColumnCreateUser',
        slotName: 'createUserName',
        dataIndex: 'createUser',
        width: 150,
        filterConfig: {
          mode: 'remote',
          loadOptionParams: {
            projectId: props.currentProject,
          },
          remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
        },
        showDrag: true,
      },
      {
        title: 'caseManagement.featureCase.tableColumnCreateTime',
        slotName: 'createTime',
        dataIndex: 'createTime',
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
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

  const getPageList = computed(() => {
    return getPublicLinkCaseListMap[props.getPageApiType][props.activeSourceType];
  });

  const {
    propsRes,
    propsEvent,
    viewId,
    advanceFilter,
    setAdvanceFilter,
    loadList,
    setLoadListParams,
    resetFilterParams,
    setTableSelected,
  } = useTable(
    getPageList.value,
    {
      tableKey: TableKeyEnum.ASSOCIATE_CASE,
      showSetting: true,
      isSimpleSetting: true,
      onlyPageSize: true,
      selectable: true,
      showSelectAll: true,
      heightUsed: 310,
      showSelectorAll: false,
    },
    (record) => {
      return {
        ...record,
        caseLevel: getCaseLevels(record.customFields),
      };
    }
  );

  async function getTableParams() {
    const { excludeKeys } = propsRes.value;
    return {
      keyword: props.keyword,
      projectId: props.currentProject,
      moduleIds: props.activeModule === 'all' || !props.activeModule ? [] : [props.activeModule, ...props.offspringIds],
      excludeIds: [...excludeKeys],
      filter: propsRes.value.filter,
      ...props.extraTableParams,
    };
  }

  async function getModuleCount() {
    const tableParams = await getTableParams();
    // 这里的count始终都是全量的
    emit('getModuleCount', {
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }

  async function loadCaseList() {
    if (props.associatedIds && props.associatedIds.length) {
      props.associatedIds.forEach((hasNotAssociatedId) => {
        setTableSelected(hasNotAssociatedId);
      });
    }
    const tableParams = await getTableParams();
    setLoadListParams({
      ...tableParams,
      moduleIds: props.isAdvancedSearchMode ? [] : tableParams.moduleIds,
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
      title: 'caseManagement.featureCase.tableColumnName',
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
      title: 'caseManagement.featureCase.tableColumnReviewResult',
      dataIndex: 'reviewStatus',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: reviewResultOptions.value,
      },
    },
    {
      title: 'caseManagement.featureCase.tableColumnExecutionResult',
      dataIndex: 'lastExecuteResult',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: executeResultOptions.value,
      },
    },
    {
      title: 'caseManagement.featureCase.associatedDemand',
      dataIndex: 'demand',
      type: FilterType.INPUT,
    },
    {
      title: 'caseManagement.featureCase.relatedAttachments',
      dataIndex: 'attachment',
      type: FilterType.INPUT,
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
    {
      title: 'common.tag',
      dataIndex: 'tags',
      type: FilterType.TAGS_INPUT,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
  ]);
  const searchCustomFields = ref<FilterFormItem[]>([]);
  async function initFilter() {
    try {
      const result = await getCustomFieldsTable(props.currentProject);
      searchCustomFields.value = getFilterCustomFields(result); // 处理自定义字段
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  function setCaseAdvanceFilter(filter: FilterResult, id: string) {
    setAdvanceFilter(filter, id);
  }

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();

  function getFunctionalSaveParams() {
    const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
    const tableParams = getTableParams();
    return {
      ...tableParams,
      excludeIds: [...excludeKeys],
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
    };
  }

  // 去功能用例详情页面
  function toDetail(record: CaseManagementTable) {
    openNewPage(CaseManagementRouteEnum.CASE_MANAGEMENT_CASE, {
      id: record.id,
      pId: record.projectId,
    });
  }

  const selectIds = computed(() => {
    return [...propsRes.value.selectedKeys];
  });

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

  watch(
    () => selectIds.value,
    (val) => {
      innerSelectedIds.value = val;
    }
  );

  watch(
    () => props.currentProject,
    () => {
      tableRef.value?.initColumn(columns.value);
      resetFilterParams();
      loadCaseList();
      initFilter();
    }
  );

  watch(
    () => props.activeModule,
    () => {
      if (!props.isAdvancedSearchMode) {
        resetFilterParams();
        loadCaseList();
        initFilter();
      }
    }
  );

  onMounted(() => {
    loadCaseList();
    initFilter();
  });

  defineExpose({
    getFunctionalSaveParams,
    loadCaseList,
    filterConfigList,
    searchCustomFields,
    setCaseAdvanceFilter,
  });

  await tableStore.initColumn(TableKeyEnum.ASSOCIATE_CASE, columns.value, 'drawer');
</script>

<style lang="less" scoped>
  :deep(.operator-class) {
    .arco-table-cell-align-left {
      padding: 0 8px !important;
    }
  }
</style>
