<template>
  <MsCard auto-height simple>
    <div class="flex items-center justify-between">
      <div class="cursor-pointer font-medium text-[var(--color-text-1)]" @click="goScenario">
        {{ t('ms.workbench.myFollowed.feature.API_SCENARIO') }}
      </div>
    </div>
    <ms-base-table
      class="mt-[16px]"
      v-bind="propsRes"
      :first-column-width="44"
      no-disable
      filter-icon-align-left
      v-on="propsEvent"
    >
      <template #num="{ record }">
        <div class="flex items-center">
          <MsButton type="text" class="float-left" style="margin-right: 4px" @click="openScenario(record.id)">
            {{ record.num }}
          </MsButton>
        </div>
      </template>
      <template #priority="{ record }">
        <span class="text-[var(--color-text-2)]"> <caseLevel :case-level="record.priority" /></span>
      </template>
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
        <caseLevel :case-level="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_STATUS]="{ filterContent }">
        <apiStatus :status="filterContent.value" />
      </template>
      <template #status="{ record }">
        <apiStatus :status="record.status" />
      </template>
      <template #createUserName="{ record }">
        <a-tooltip :content="`${record.createUserName}`" position="tl">
          <div class="one-line-text">{{ characterLimit(record.createUserName) }}</div>
        </a-tooltip>
      </template>
      <!-- 报告结果筛选 -->
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #lastReportStatus="{ record }">
        <ExecutionStatus
          :module-type="ReportEnum.API_SCENARIO_REPORT"
          :status="record.lastReportStatus ? record.lastReportStatus : 'PENDING'"
          :script-identifier="record.scriptIdentifier"
          :class="record.lastReportId ? 'cursor-pointer' : ''"
          @click="openScenarioReportDrawer(record)"
        />
      </template>
      <template #stepTotal="{ record }">
        {{ record.stepTotal }}
      </template>
    </ms-base-table>
  </MsCard>
  <!-- 场景报告抽屉 -->
  <caseAndScenarioReportDrawer
    v-model:visible="showScenarioReportVisible"
    is-scenario
    :report-id="tableRecord?.lastReportId || ''"
  />
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import { workbenchScenarioList } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { characterLimit } from '@/utils';

  import { ApiScenarioTableItem } from '@/models/apiTest/scenario';
  import { ApiScenarioStatus } from '@/enums/apiEnum';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';

  const props = defineProps<{
    project: string;
    type: 'my_follow' | 'my_create';
    refreshId: string;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();
  const tableRecord = ref<ApiScenarioTableItem>();

  const requestApiScenarioStatusOptions = computed(() => {
    return Object.values(ApiScenarioStatus).map((key) => {
      return {
        value: key,
        label: key,
      };
    });
  });

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
      width: 100,
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
      fixed: 'left',
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
      title: 'apiScenario.table.columns.status',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: requestApiScenarioStatusOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_STATUS,
        disabledTooltip: true,
      },
      showDrag: true,
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
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS,
      },
      width: 200,
    },
    {
      title: 'apiScenario.table.columns.scenarioEnv',
      dataIndex: 'environmentName',
      showDrag: true,
      width: 159,
      showTooltip: true,
    },
    {
      title: 'apiScenario.table.columns.createUser',
      dataIndex: 'createUser',
      slotName: 'createUserName',
      showInTable: false,
      width: 150,
    },
    {
      title: 'apiScenario.table.columns.createTime',
      dataIndex: 'createTime',
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      showDrag: true,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    workbenchScenarioList,
    {
      columns,
      scroll: { x: '100%' },
      showSetting: false,
      selectable: false,
      showSelectAll: false,
      heightUsed: 282,
      paginationSize: 'mini',
    },
    (item) => ({
      ...item,
      requestPassRate: item.requestPassRate ? `${item.requestPassRate}%` : '-',
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  const showScenarioReportVisible = ref(false);
  function openScenarioReportDrawer(record: ApiScenarioTableItem) {
    if (record.lastReportId) {
      tableRecord.value = record;
      showScenarioReportVisible.value = true;
    }
  }

  function openScenario(id: number) {
    openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, { id, pId: props.project });
  }

  function goScenario() {
    openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, {
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
