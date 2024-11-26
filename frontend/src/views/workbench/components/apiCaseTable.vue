<template>
  <MsCard auto-height simple>
    <div class="flex items-center justify-between">
      <div class="cursor-pointer font-medium text-[var(--color-text-1)]" @click="goApiCase">
        {{ t('ms.workbench.myFollowed.feature.API_CASE') }}
      </div>
    </div>
    <ms-base-table v-bind="propsRes" :first-column-width="44" no-disable filter-icon-align-left v-on="propsEvent">
      <template #num="{ record }">
        <div class="flex items-center">
          <MsButton type="text" @click="openCase(record.id)">
            {{ record.num }}
          </MsButton>
        </div>
      </template>
      <template #caseLevel="{ record }">
        <span class="text-[var(--color-text-2)]"> <caseLevel :case-level="record.priority" /></span>
      </template>
      <!-- 用例等级 -->
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
        <caseLevel :case-level="filterContent.value" />
      </template>
      <template #status="{ record }">
        <apiStatus :status="record.status" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_STATUS]="{ filterContent }">
        <apiStatus :status="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #lastReportStatus="{ record }">
        <ExecutionStatus
          :module-type="ReportEnum.API_REPORT"
          :status="record.lastReportStatus"
          :class="[!record.lastReportId ? '' : 'cursor-pointer']"
          @click="showResult(record)"
        />
      </template>
      <template #passRateColumn>
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t('case.passRate') }}
          <a-tooltip :content="t('case.passRateTip')" position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
      </template>
    </ms-base-table>
  </MsCard>
  <!-- 执行结果抽屉 -->
  <caseAndScenarioReportDrawer v-model:visible="showExecuteResult" :report-id="activeReportId" />
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import { workbenchApiCaseList } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions, caseStatusOptions } from '@/views/api-test/components/config';

  const props = defineProps<{
    project: string;
    type: 'my_follow' | 'my_create';
    refreshId: string;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const lastReportStatusListOptions = computed(() => {
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
      width: 100,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      width: 180,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      filterConfig: {
        options: casePriorityOptions,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 100,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: caseStatusOptions,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_STATUS,
        disabledTooltip: true,
      },
      width: 100,
    },
    {
      title: 'case.lastReportStatus',
      dataIndex: 'lastReportStatus',
      slotName: 'lastReportStatus',
      filterConfig: {
        options: lastReportStatusListOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS,
      },
      width: 100,
      showDrag: true,
    },
    {
      title: 'case.caseEnvironment',
      dataIndex: 'environmentName',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'case.tableColumnCreateUser',
      slotName: 'createName',
      dataIndex: 'createName',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'case.tableColumnCreateTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(workbenchApiCaseList, {
    columns,
    scroll: { x: '100%' },
    showSetting: false,
    selectable: false,
    showSelectAll: false,
    paginationSize: 'mini',
  });

  const activeReportId = ref('');
  const showExecuteResult = ref(false);
  async function showResult(record: ApiCaseDetail) {
    if (!record.lastReportId) return;
    activeReportId.value = record.lastReportId;
    showExecuteResult.value = true;
  }

  function openCase(id: number) {
    openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, { tab: 'case', cId: id, pId: props.project });
  }

  function goApiCase() {
    openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
      tab: 'case',
      view: props.type,
      pId: props.project,
    });
  }

  function init() {
    setLoadListParams({
      projectId: props.project,
      viewId: props.type,
    });
    loadList();
  }

  watch(
    () => props.refreshId,
    () => {
      init();
    }
  );

  onBeforeMount(() => {
    init();
  });
</script>

<style lang="less" scoped></style>
