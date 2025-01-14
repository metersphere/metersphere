<template>
  <div class="p-[16px]">
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #[FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode as keyof typeof TriggerModeLabel]) }}</span>
      </template>
      <template #executeStatus="{ record }">
        <ExecStatus :status="record.execStatus" />
      </template>
      <template #lastExecResult="{ record }">
        <ExecutionStatus v-if="record.execResult" :status="record.execResult" :module-type="ReportEnum.API_REPORT" />
      </template>
      <template #executionStartAndEndTime="{ record }">
        <a-tooltip :content="getStartAndEndTime(record)" :mouse-enter-delay="300">
          <div class="one-line-text">
            {{ getStartAndEndTime(record) }}
          </div>
        </a-tooltip>
      </template>
      <template #operation="{ record }">
        <a-tooltip
          v-if="record.execStatus !== ExecuteStatusEnum.PENDING"
          :content="t('common.executionResultCleaned')"
          :disabled="!record.resultDeleted"
        >
          <MsButton
            :disabled="record.resultDeleted || !hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ'])"
            class="!mr-0"
            @click="toReport(record)"
            >{{ t('apiScenario.executeHistory.execution.operation') }}
          </MsButton>
        </a-tooltip>
      </template>
    </ms-base-table>
  </div>
  <executeResultDrawer v-model:visible="resultVisible" :plan-detail="resultRecord" />
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import executeResultDrawer from '../executeResultDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import ExecStatus from '@/views/taskCenter/component/execStatus.vue';

  import { getPlanDetailExecuteHistory } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import type { PlanDetailExecuteHistoryItem } from '@/models/testPlan/testPlan';
  import { PlanReportStatus, ReportEnum, TriggerModeLabel } from '@/enums/reportEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ExecuteStatusEnum } from '@/enums/taskCenter';

  import { triggerModeOptions } from '@/views/api-test/report/utils';

  const props = defineProps<{
    isGroup?: boolean;
    planId?: string;
  }>();

  const { t } = useI18n();
  const route = useRoute();
  // const { openNewPage } = useOpenNewPage();

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
      width: 150,
    },
    {
      title: 'ms.taskCenter.executeStatus',
      dataIndex: 'executeStatus',
      slotName: 'executeStatus',
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
      width: 350,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 100,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getPlanDetailExecuteHistory, {
    columns,
    scroll: { x: '100%' },
    selectable: false,
  });

  function loadExecuteList() {
    setLoadListParams({
      testPlanId: props.planId || route.query.id,
    });
    loadList();
  }

  const resultVisible = ref(false);
  const resultRecord = ref<PlanDetailExecuteHistoryItem>({} as PlanDetailExecuteHistoryItem);
  // 查看报告详情
  function toReport(record: PlanDetailExecuteHistoryItem) {
    resultVisible.value = true;
    resultRecord.value = record;
  }

  function getStartAndEndTime(record: PlanDetailExecuteHistoryItem) {
    return `${record.startTime ? dayjs(record.startTime).format('YYYY-MM-DD HH:mm:ss') : '-'}${t('common.to')}${
      record.endTime ? dayjs(record.endTime).format('YYYY-MM-DD HH:mm:ss') : '-'
    }`;
  }

  onBeforeMount(() => {
    loadExecuteList();
  });
</script>
