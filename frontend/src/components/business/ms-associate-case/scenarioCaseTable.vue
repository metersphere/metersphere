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
      <MsButton type="text">{{ record.num }}</MsButton>
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
  import useAppStore from '@/store/modules/app';

  import type { TableQueryParams } from '@/models/common';
  import { CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { getPublicLinkCaseListMap } from './utils/page';
  import { casePriorityOptions } from '@/views/api-test/components/config';

  const { t } = useI18n();

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
  ];
  const getPageList = computed(() => {
    return getPublicLinkCaseListMap[props.getPageApiType][props.activeSourceType];
  });
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setPagination, resetFilterParams } =
    useTable(getPageList.value, {
      columns,
      showSetting: false,
      selectable: true,
      showSelectAll: true,
      heightUsed: 310,
      showSelectorAll: false,
    });

  async function getTableParams() {
    return {
      keyword: props.keyword,
      projectId: props.currentProject,
      moduleIds: props.activeModule === 'all' || !props.activeModule ? [] : [props.activeModule, ...props.offspringIds],
      excludeIds: [...(props.associatedIds || [])], // 已经存在的关联的id列表
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

  watch(
    () => props.currentProject,
    () => {
      setPagination({
        current: 1,
      });
      resetSelector();
      resetFilterParams();
      emit('refresh');
    }
  );

  function getScenarioSaveParams() {
    const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
    const tableParams = getTableParams();
    return {
      ...tableParams,
      excludeIds: [...excludeKeys].concat(...(props.associatedIds || [])),
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
    };
  }

  onMounted(() => {
    loadScenarioList();
  });

  watch(
    () => props.activeModule,
    () => {
      resetSelector();
      resetFilterParams();
      loadScenarioList();
    }
  );

  defineExpose({
    getScenarioSaveParams,
    loadScenarioList,
  });
</script>

<style scoped></style>
