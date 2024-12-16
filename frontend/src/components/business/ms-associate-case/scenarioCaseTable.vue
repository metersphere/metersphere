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
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
      <CaseLevel :case-level="filterContent.value" />
    </template>
    <template #priority="{ record }">
      <CaseLevel :case-level="record.priority" />
    </template>
    <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT]="{ filterContent }">
      <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
    </template>
    <template #lastReportStatus="{ record }">
      <ExecutionStatus
        :module-type="ReportEnum.API_SCENARIO_REPORT"
        :status="record.lastReportStatus ? record.lastReportStatus : 'PENDING'"
        :script-identifier="record.scriptIdentifier"
      />
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

  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import type { TableQueryParams } from '@/models/common';
  import { FilterType } from '@/enums/advancedFilterEnum';
  import { CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { SpecialColumnEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import type { moduleKeysType } from './types';
  import useModuleSelection from './useModuleSelection';
  import { getPublicLinkCaseListMap } from './utils/page';
  import { casePriorityOptions } from '@/views/api-test/components/config';
  import { scenarioStatusOptions } from '@/views/api-test/scenario/components/config';
  import type { SelectOptionData } from '@arco-design/web-vue';

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const props = defineProps<{
    associationType: string; // 关联类型 项目 | 测试计划 | 用例评审
    activeModule: string;
    offspringIds: string[];
    currentProject: string;
    associatedIds?: string[]; // 已关联ids
    activeSourceType: keyof typeof CaseLinkEnum;
    keyword: string;
    getPageApiType: keyof typeof CasePageApiTypeEnum; // 获取未关联分页Api
    extraTableParams?: TableQueryParams; // 查询表格的额外参数
    moduleTree: MsTreeNodeData[];
    modulesCount: Record<string, any>;
    isAdvancedSearchMode?: boolean;
    testPlanList: SelectOptionData[];
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: TableQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
    (e: 'update:selectedIds'): void;
  }>();

  const appStore = useAppStore();
  const tableStore = useTableStore();

  const innerSelectedModulesMaps = defineModel<Record<string, moduleKeysType>>('selectedModulesMaps', {
    required: true,
  });

  const statusList = computed(() => {
    return Object.keys(ReportStatus).map((key) => {
      return {
        value: key,
        label: t(ReportStatus[key].label),
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
        width: 160,
        showTooltip: false,
        columnSelectorDisabled: true,
      },
      {
        title: 'apiScenario.table.columns.name',
        dataIndex: 'name',
        slotName: 'name',
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
        showDrag: true,
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
        filterConfig: {
          options: casePriorityOptions,
          filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
        },
        width: 140,
      },
      {
        title: 'apiScenario.table.columns.runResult',
        dataIndex: 'lastReportStatus',
        slotName: 'lastReportStatus',
        showTooltip: false,
        showDrag: true,
        filterConfig: {
          options: statusList.value,
          filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT,
        },
        width: 200,
      },
      {
        title: 'apiScenario.table.columns.passRate',
        dataIndex: 'requestPassRate',
        showDrag: true,
        width: 100,
      },
      {
        title: 'apiScenario.table.columns.tags',
        dataIndex: 'tags',
        isTag: true,
        isStringTag: true,
        showDrag: true,
      },
      {
        title: 'apiScenario.table.columns.createUser',
        dataIndex: 'createUser',
        slotName: 'createUserName',
        width: 109,
        filterConfig: {
          mode: 'remote',
          loadOptionParams: {
            projectId: props.currentProject,
          },
          remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
        },
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
  } = useTable(getPageList.value, {
    tableKey: TableKeyEnum.ASSOCIATE_CASE_API_SCENARIO,
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

  async function loadScenarioList() {
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

  const filterConfigList = computed<FilterFormItem[]>(() => {
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
          options: statusList.value,
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
  function setCaseAdvanceFilter(filter: FilterResult, id: string) {
    setAdvanceFilter(filter, id);
  }

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();

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

  function getScenarioSaveParams() {
    const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
    const tableParams = getTableParams();
    return {
      ...tableParams,
      excludeIds: [...excludeKeys],
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
    };
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

  // 去接口场景详情页面
  function toDetail(record: ApiCaseDetail) {
    openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, {
      id: record.id,
      pId: record.projectId,
    });
  }
  watch(
    () => props.currentProject,
    () => {
      tableRef.value?.initColumn(columns.value);
      resetFilterParams();
      loadScenarioList();
    }
  );

  watch(
    () => props.activeModule,
    () => {
      if (!props.isAdvancedSearchMode) {
        resetFilterParams();
        loadScenarioList();
      }
    }
  );

  onMounted(() => {
    loadScenarioList();
  });

  defineExpose({
    filterConfigList,
    setCaseAdvanceFilter,
    getScenarioSaveParams,
    loadScenarioList,
  });

  await tableStore.initColumn(TableKeyEnum.ASSOCIATE_CASE_API_SCENARIO, columns.value, 'drawer');
</script>

<style lang="less" scoped>
  :deep(.operator-class) {
    .arco-table-cell-align-left {
      padding: 0 8px !important;
    }
  }
</style>
