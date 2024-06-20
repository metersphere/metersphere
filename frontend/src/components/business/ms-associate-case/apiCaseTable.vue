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
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';
  import { characterLimit } from '@/utils';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import type { TableQueryParams } from '@/models/common';
  import { CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { SpecialColumnEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { getPublicLinkCaseListMap } from './utils/page';
  import { casePriorityOptions } from '@/views/api-test/components/config';

  const { openNewPage } = useOpenNewPage();

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
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: TableQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
    (e: 'update:selectedIds'): void;
  }>();

  const tableStore = useTableStore();

  const lastReportStatusListOptions = computed(() => {
    return Object.keys(ReportStatus).map((key) => {
      return {
        value: key,
        ...Object.keys(ReportStatus[key]),
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
      title: 'common.tag',
      slotName: 'tags',
      dataIndex: 'tags',
      isTag: true,
      width: 300,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'createName',
      dataIndex: 'createName',
      showTooltip: true,
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
    },
  ];

  const getPageList = computed(() => {
    return props.activeSourceType !== 'API'
      ? getPublicLinkCaseListMap[props.getPageApiType][props.activeSourceType]
      : getPublicLinkCaseListMap[props.getPageApiType][props.activeSourceType].CASE;
  });

  function getCaseLevel(record: TableData) {
    if (record.customFields && record.customFields.length) {
      const caseItem = record.customFields.find((item: any) => item.fieldName === '用例等级' && item.internal);
      return caseItem?.options.find((item: any) => item.value === caseItem?.defaultValue).text;
    }
    return undefined;
  }

  const {
    propsRes,
    propsEvent,
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
      condition: {
        keyword: props.keyword,
        filter: propsRes.value.filter,
      },
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
    setLoadListParams(tableParams);
    loadList();
  }

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();

  watch(
    () => props.showType,
    (val) => {
      if (val === 'API_CASE') {
        resetSelector();
        resetFilterParams();
        loadCaseList();
      }
    }
  );

  watch(
    () => () => props.currentProject,
    () => {
      setPagination({
        current: 1,
      });
      resetSelector();
      resetFilterParams();
      loadCaseList();
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
      if (val) {
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

  defineExpose({
    getApiCaseSaveParams,
    loadCaseList,
  });

  await tableStore.initColumn(TableKeyEnum.ASSOCIATE_CASE_API_CASE, columns, 'drawer');
</script>

<style lang="less" scoped>
  :deep(.arco-table-cell-align-left) {
    padding: 0 8px !important;
  }
</style>
