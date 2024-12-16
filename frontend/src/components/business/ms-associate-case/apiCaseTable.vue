<template>
  <MsBaseTable
    ref="tableRef"
    v-bind="propsRes"
    class="mt-[16px]"
    :action-config="{
      baseAction: [],
      moreAction: [],
    }"
    :not-show-table-filter="props.isAdvancedSearchMode"
    always-show-selected-count
    v-on="propsEvent"
    @row-select-change="rowSelectChange"
    @select-all-change="selectAllChange"
    @clear-selector="clearSelector"
  >
    <template #num="{ record }">
      <MsButton type="text" @click="toDetail(record)">{{ record.num }}</MsButton>
    </template>
    <template #lastReportStatus="{ record }">
      <ExecutionStatus
        v-if="record.lastReportStatus !== 'PENDING'"
        :module-type="ReportEnum.API_REPORT"
        :status="record.lastReportStatus"
        :class="[!record.lastReportId ? '' : 'cursor-pointer']"
      />
      <span v-else>-</span>
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
      <CaseLevel :case-level="filterContent.value" />
    </template>
    <template #caseLevel="{ record }">
      <CaseLevel :case-level="record.priority" />
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
      <ExecuteResult :execute-result="filterContent.value" />
    </template>
    <template #lastExecResult="{ record }">
      <ExecuteResult :execute-result="record.lastExecResult" />
    </template>
    <template #createName="{ record }">
      <a-tooltip :content="`${record.createName}`" position="tl">
        <div class="one-line-text">{{ characterLimit(record.createName) }}</div>
      </a-tooltip>
    </template>
    <template #[FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS]="{ filterContent }">
      <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
    </template>
    <template #count>
      <slot></slot>
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { TableData } from '@arco-design/web-vue';
  import { type SelectOptionData } from '@arco-design/web-vue';

  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import type { TableQueryParams } from '@/models/common';
  import { FilterType } from '@/enums/advancedFilterEnum';
  import { CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { ReportEnum } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { SpecialColumnEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import type { moduleKeysType } from './types';
  import useModuleSelection from './useModuleSelection';
  import { getPublicLinkCaseListMap } from './utils/page';
  import {
    casePriorityOptions,
    caseStatusOptions,
    lastReportStatusListOptions,
  } from '@/views/api-test/components/config';

  const { openNewPage } = useOpenNewPage();
  const { t } = useI18n();
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
    protocols: string[];
    allProtocolList: string[];
    isAdvancedSearchMode?: boolean;
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

  const innerSelectedModulesMaps = defineModel<Record<string, moduleKeysType>>('selectedModulesMaps', {
    required: true,
  });

  const tableStore = useTableStore();

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
        showTooltip: true,
        columnSelectorDisabled: true,
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
        dataIndex: 'priority',
        slotName: 'caseLevel',
        filterConfig: {
          options: casePriorityOptions,
          filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
        },
        width: 150,
        showDrag: true,
      },
      {
        title: 'case.lastReportStatus',
        dataIndex: 'lastReportStatus',
        slotName: 'lastReportStatus',
        filterConfig: {
          options: lastReportStatusListOptions.value,
          filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS,
        },
        showInTable: false,
        width: 150,
        showDrag: true,
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
        slotName: 'tags',
        dataIndex: 'tags',
        isTag: true,
        width: 300,
      },
      {
        title: 'caseManagement.featureCase.tableColumnCreateUser',
        slotName: 'createName',
        dataIndex: 'createUser',
        filterConfig: {
          mode: 'remote',
          loadOptionParams: {
            projectId: props.currentProject,
          },
          remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
        },
        width: 200,
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
    return props.activeSourceType !== 'API'
      ? getPublicLinkCaseListMap[props.getPageApiType][props.activeSourceType]
      : getPublicLinkCaseListMap[props.getPageApiType][props.activeSourceType].CASE;
  });

  function getCaseLevel(record: TableData) {
    if (record.customFields && record.customFields.length) {
      const caseItem = record.customFields.find(
        (item: any) => item.fieldName === t('common.casePriority') && item.internal
      );
      return caseItem?.options.find((item: any) => item.value === caseItem?.defaultValue).text;
    }
    return undefined;
  }

  const {
    propsRes,
    propsEvent,
    viewId,
    advanceFilter,
    setAdvanceFilter,
    loadList,
    setLoadListParams,
    resetSelector,
    setPagination,
    resetFilterParams,
    setTableSelected,
  } = useTable(
    getPageList.value,
    {
      tableKey: TableKeyEnum.ASSOCIATE_CASE_API_CASE,
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
        caseLevel: getCaseLevel(record),
        tags: (record.tags || []).map((item: string, i: number) => {
          return {
            id: `${record.id}-${i}`,
            name: item,
          };
        }),
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
      protocols: props.protocols,
      ...props.extraTableParams,
    };
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
        options: props.allProtocolList?.map((item) => ({ label: item, value: item })),
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
      title: 'ms.taskCenter.taskBelongTestPlan',
      dataIndex: 'belongTestPlan',
      type: FilterType.SELECT_EQUAL,
      selectProps: {
        options: props.testPlanList,
        optionTooltipPosition: 'tr',
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
  function setCaseAdvanceFilter(filter: FilterResult, id: string) {
    setAdvanceFilter(filter, id);
  }

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();

  watch(
    [() => props.currentProject, () => props.protocols],
    () => {
      setPagination({
        current: 1,
      });
      tableRef.value?.initColumn(columns.value);
      resetFilterParams();
      loadCaseList();
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
        loadCaseList();
      }
    }
  );

  function getApiCaseSaveParams() {
    const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
    const tableParams = getTableParams();
    return {
      ...tableParams,
      excludeIds: [...excludeKeys],
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
      associateApiType: 'API_CASE',
    };
  }

  // 去接口用例详情页面
  function toDetail(record: ApiCaseDetail) {
    openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
      cId: record.id,
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
    loadCaseList();
  });

  defineExpose({
    getApiCaseSaveParams,
    loadCaseList,
    filterConfigList,
    setCaseAdvanceFilter,
  });

  await tableStore.initColumn(TableKeyEnum.ASSOCIATE_CASE_API_CASE, columns.value, 'drawer');
</script>

<style lang="less" scoped>
  :deep(.operator-class) {
    .arco-table-cell-align-left {
      padding: 0 8px !important;
    }
  }
</style>
