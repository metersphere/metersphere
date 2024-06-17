<template>
  <div class="p-[16px]">
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #[FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode as keyof typeof TriggerModeLabel]) }}</span>
      </template>
      <template #lastExecResult="{ record }">
        <ExecutionStatus v-if="record.execResult" :status="record.execResult" :module-type="ReportEnum.API_REPORT" />
      </template>
      <template #executionStartAndEndTime="{ record }">
        <div>
          {{ dayjs(record.startTime).format('YYYY-MM-DD HH:mm:ss') }} 至
          {{ record.endTime ? dayjs(record.endTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </div>
      </template>
      <template #operation="{ record }">
        <a-tooltip :content="t('project.executionHistory.cleared')" :disabled="!record.deleted">
          <MsButton
            :disabled="record.deleted || !hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ'])"
            class="!mr-0"
            @click="toReport(record)"
            >{{ t('apiScenario.executeHistory.execution.operation') }}
          </MsButton>
        </a-tooltip>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import { getPlanDetailExecuteHistory } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { hasAnyPermission } from '@/utils/permission';

  import type { PlanDetailExecuteHistoryItem } from '@/models/testPlan/testPlan';
  import { PlanReportStatus, ReportEnum, TriggerModeLabel } from '@/enums/reportEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { triggerModeOptions } from '@/views/api-test/report/utils';

  const { t } = useI18n();
  const route = useRoute();
  const { openNewPage } = useOpenNewPage();

  const planId = ref(route.query.id as string);

  const statusResultOptions = computed(() => {
    return Object.keys(PlanReportStatus).map((key) => {
      return {
        value: key,
        label: PlanReportStatus[key].statusText,
      };
    });
  });

  const columns: MsTableColumn = [
    {
      title: 'apiTestManagement.order',
      dataIndex: 'num',
      sortIndex: 1,
      width: 150,
    },
    {
      title: 'apiTestManagement.executeMethod',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      filterConfig: {
        options: triggerModeOptions,
      },
      showTooltip: true,
      width: 150,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'execResult',
      slotName: 'lastExecResult',
      filterConfig: {
        options: statusResultOptions.value,
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER,
      },
      width: 150,
    },
    {
      title: 'apiTestManagement.taskOperator',
      dataIndex: 'operationUser',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'testPlan.executeHistory.executionStartAndEndTime',
      dataIndex: 'startTime',
      slotName: 'executionStartAndEndTime',
      width: 300,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 100,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getPlanDetailExecuteHistory,
    {
      columns,
      scroll: { x: '100%' },
      selectable: false,
      heightUsed: 398,
    },
    (record) => {
      return {
        ...record,
        startTime: dayjs(record.startTime).format('YYYY-MM-DD HH:mm:ss'),
      };
    }
  );

  function loadExecuteList() {
    setLoadListParams({
      testPlanId: planId.value,
    });
    loadList();
  }

  // 查看报告详情
  function toReport(record: PlanDetailExecuteHistoryItem) {
    openNewPage(TestPlanRouteEnum.TEST_PLAN_REPORT_DETAIL, {
      id: record.id,
    });
  }

  onBeforeMount(() => {
    loadExecuteList();
  });
</script>
