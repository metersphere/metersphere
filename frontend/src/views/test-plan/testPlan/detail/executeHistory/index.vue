<template>
  <div class="p-[16px]">
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode as keyof typeof TriggerModeLabel]) }}</span>
      </template>
      <template #lastExecResult="{ record }">
        <ExecutionStatus :status="record.status" :module-type="ReportEnum.API_REPORT" />
      </template>
      <template #executionStartAndEndTime="{ record }">
        <!-- TODO 样式 -->
        <div>{{ record.startTime }} 至 {{ record.endTime ?? '-' }}</div>
      </template>
      <template #operation="{ record }">
        <MsButton class="!mr-0" @click="toReport(record)">
          {{ t('apiScenario.executeHistory.execution.operation') }}
        </MsButton>
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
  import useAppStore from '@/store/modules/app';

  import type { PlanDetailExecuteHistoryItem } from '@/models/testPlan/testPlan';
  import { ReportEnum, TriggerModeLabel } from '@/enums/reportEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { triggerModeOptions } from '@/views/api-test/report/utils';
  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';

  const { t } = useI18n();
  const route = useRoute();
  const appStore = useAppStore();
  const { openNewPage } = useOpenNewPage();

  const planId = ref(route.query.id as string);

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
      dataIndex: 'lastExecResult',
      slotName: 'lastExecResult',
      filterConfig: {
        valueKey: 'key',
        labelKey: 'statusText',
        options: Object.values(executionResultMap),
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
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
      width: 200,
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
      projectId: appStore.currentProjectId,
      id: planId.value,
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
