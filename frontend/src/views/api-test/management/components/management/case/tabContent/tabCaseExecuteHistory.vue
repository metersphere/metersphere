<template>
  <div class="history-container">
    <div class="history-table-before">
      <span class="text-[var(--color-text-1)]">{{ t('case.detail.execute.history.list') }}</span>
      <!--      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('apiTestManagement.quoteSearchPlaceholder')"
        allow-clear
        class="mr-[8px] w-[240px]"
        @search="loadExecuteList"
        @press-enter="loadExecuteList"
      />-->
    </div>
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #num="{ record }">
        <div class="flex items-center justify-start">
          <span type="text" class="px-0">{{ record.num }}</span>
          <a-tooltip v-if="record.testPlanNum" :content="record.testPlanNum">
            <MsTag
              class="ml-2"
              :self-style="{
                border: `1px solid ${color}`,
                color: color,
                backgroundColor: 'var(--color-text-fff)',
              }"
            >
              {{ record.testPlanNum }}
            </MsTag>
          </a-tooltip>
        </div>
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode as keyof typeof TriggerModeLabel]) }}</span>
      </template>
      <template #status="{ record }">
        <ExecutionStatus :status="record.status" :module-type="ReportEnum.API_REPORT" />
      </template>
      <template #executeStatus="{ record }">
        <ExecStatus :status="record.execStatus" />
      </template>
      <template #operation="{ record, rowIndex }">
        <a-tooltip
          v-if="record.execStatus !== ExecuteStatusEnum.PENDING"
          :content="t('common.executionResultCleaned')"
          position="top"
          :disabled="!record.resultDeleted"
        >
          <MsButton
            :disabled="
              record.resultDeleted ||
              !hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+EXECUTE', 'PROJECT_API_REPORT:READ'])
            "
            class="!mr-0"
            @click="showResult(record, rowIndex)"
            >{{ t('apiScenario.executeHistory.execution.operation') }}
          </MsButton>
        </a-tooltip>
      </template>
    </ms-base-table>
  </div>
  <caseExecuteResultDrawer
    v-if="showResponse"
    :id="activeReport.id"
    v-model:visible="showResponse"
    :user-name="activeReport.createUser"
    :status="activeReport.execStatus"
    :result="activeReport.status"
    :resource-name="activeReport.name"
  />
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import ExecStatus from '@/views/taskCenter/component/execStatus.vue';

  import { getApiCaseExecuteHistory } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { ExecuteHistoryItem } from '@/models/apiTest/scenario';
  import { ReportEnum, ReportStatus, TriggerModeLabel } from '@/enums/reportEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ExecuteStatusEnum } from '@/enums/taskCenter';

  import { triggerModeOptions } from '@/views/api-test/report/utils';

  const caseExecuteResultDrawer = defineAsyncComponent(
    () => import('@/views/taskCenter/component/caseExecuteResultDrawer.vue')
  );

  const appStore = useAppStore();
  const { t } = useI18n();
  const statusList = computed(() => {
    return Object.keys(ReportStatus).map((key) => {
      return {
        value: key,
        label: t(ReportStatus[key].label),
      };
    });
  });

  const showResponse = ref(false);

  const props = defineProps<{
    sourceId: string | number;
    moduleType: string;
    protocol: string;
  }>();

  const keyword = ref('');

  const columns: MsTableColumn = [
    {
      title: 'apiTestManagement.order',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      width: 280,
    },
    {
      title: 'apiTestManagement.executeMethod',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      filterConfig: {
        options: triggerModeOptions,
      },
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
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
      title: 'report.result',
      dataIndex: 'status',
      slotName: 'status',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: statusList.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS,
      },
      width: 150,
    },
    {
      title: 'apiTestManagement.taskOperator',
      dataIndex: 'operationUser',
      showTooltip: true,
      width: 100,
    },
    {
      title: 'apiTestManagement.taskOperationTime',
      dataIndex: 'startTime',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 150,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      showInTable: true,
      showDrag: false,
      width: 150,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getApiCaseExecuteHistory,
    {
      columns,
      scroll: { x: '100%' },
      selectable: false,
      heightUsed: 374,
    },
    // eslint-disable-next-line no-return-assign
    (item) => ({
      ...item,
      startTime: dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  function loadExecuteList(sourceId?: string) {
    setLoadListParams({
      projectId: appStore.currentProjectId,
      keyword: keyword.value,
      id: sourceId ?? props.sourceId,
    });
    loadList();
  }

  const activeReportIndex = ref<number>(0);
  const activeReport = ref<ExecuteHistoryItem>({} as ExecuteHistoryItem);

  async function showResult(record: ExecuteHistoryItem, rowIndex: number) {
    activeReport.value = record;
    activeReportIndex.value = rowIndex;
    showResponse.value = true;
  }

  const color = 'rgb(var(--primary-7))';

  onBeforeMount(() => {
    loadExecuteList();
  });

  defineExpose({
    loadExecuteList,
  });
</script>

<style lang="less" scoped>
  .history-container {
    @apply h-full overflow-y-auto;

    .ms-scroll-bar();
  }
  .history-table-before {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-direction: row;
    margin-bottom: 21px;
  }
</style>
