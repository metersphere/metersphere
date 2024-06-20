<template>
  <MsBaseTable
    ref="tableRef"
    class="mt-[16px]"
    v-bind="propsRes"
    :action-config="{
      baseAction: [],
      moreAction: [],
    }"
    v-on="propsEvent"
    @filter-change="getModuleCount"
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
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import type { TableQueryParams } from '@/models/common';
  import { CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { SpecialColumnEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { getPublicLinkCaseListMap } from './utils/page';
  import { casePriorityOptions } from '@/views/api-test/components/config';

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
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: TableQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
    (e: 'update:selectedIds'): void;
  }>();

  const appStore = useAppStore();
  const tableStore = useTableStore();

  const statusList = computed(() => {
    return Object.keys(ReportStatus).map((key) => {
      return {
        value: key,
        label: t(ReportStatus[key].label),
      };
    });
  });

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
      showInTable: false,
      width: 100,
    },
    {
      title: 'apiScenario.table.columns.createUser',
      dataIndex: 'createUser',
      slotName: 'createUserName',
      showInTable: false,
      showTooltip: true,
      showDrag: true,
      width: 109,
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
        placeholderText: t('caseManagement.featureCase.PleaseSelect'),
      },
    },
    {
      title: 'apiScenario.table.columns.tags',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      showDrag: true,
    },
    {
      title: '',
      dataIndex: 'action',
      width: 24,
      slotName: SpecialColumnEnum.ACTION,
      fixed: 'right',
    },
  ];
  const getPageList = computed(() => {
    return getPublicLinkCaseListMap[props.getPageApiType][props.activeSourceType];
  });
  const {
    propsRes,
    propsEvent,
    loadList,
    setLoadListParams,
    resetSelector,
    setPagination,
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
      condition: {
        keyword: props.keyword,
        filter: propsRes.value.filter,
      },
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
    setLoadListParams(tableParams);
    loadList();
    emit('getModuleCount', {
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
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

  // 去接口场景详情页面
  function toDetail(record: ApiCaseDetail) {
    openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, {
      id: record.id,
      pId: record.projectId,
    });
  }
  watch([() => props.currentProject, () => props.activeModule], () => {
    resetSelector();
    resetFilterParams();
    loadScenarioList();
  });

  onMounted(() => {
    loadScenarioList();
  });

  defineExpose({
    getScenarioSaveParams,
    loadScenarioList,
  });

  await tableStore.initColumn(TableKeyEnum.ASSOCIATE_CASE_API_SCENARIO, columns, 'drawer');
</script>

<style lang="less" scoped>
  :deep(.arco-table-cell-align-left) {
    padding: 0 8px !important;
  }
</style>
