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
                backgroundColor: 'white',
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
      <template #execStatus="{ record }">
        <ExecStatus :status="record.execStatus" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT]="{ filterContent }">
        <ExecStatus :status="filterContent.value" />
      </template>
      <template #operation="{ record, rowIndex }">
        <div v-if="record.historyDeleted">
          <a-tooltip :content="t('project.executionHistory.cleared')" position="top">
            <MsButton
              :disabled="
                record.historyDeleted ||
                !hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+EXECUTE', 'PROJECT_API_REPORT:READ'])
              "
              class="!mr-0"
              @click="showResult(record, rowIndex)"
              >{{ t('apiScenario.executeHistory.execution.operation') }}
            </MsButton>
          </a-tooltip>
        </div>
        <div v-else>
          <MsButton
            :disabled="
              record.historyDeleted ||
              !hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+EXECUTE', 'PROJECT_API_REPORT:READ'])
            "
            class="!mr-0"
            @click="showResult(record, rowIndex)"
            >{{ t('apiScenario.executeHistory.execution.operation') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
  </div>
  <caseAndScenarioReportDrawer v-model:visible="showResponse" :report-id="activeReportId" />
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecStatus from '@/views/api-test/report/component/execStatus.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import { getApiCaseExecuteHistory } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { ApiCaseExecuteHistoryItem } from '@/models/apiTest/management';
  import { ReportExecStatus } from '@/enums/apiEnum';
  import { ReportEnum, ReportStatus, TriggerModeLabel } from '@/enums/reportEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { triggerModeOptions } from '@/views/api-test/report/utils';

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

  const ExecStatusList = computed(() => {
    return Object.values(ReportExecStatus).map((e) => {
      return {
        value: e,
        key: e,
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
      title: 'report.status',
      dataIndex: 'execStatus',
      slotName: 'execStatus',
      filterConfig: {
        options: ExecStatusList.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT,
      },
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      width: 150,
      showDrag: true,
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

  function loadExecuteList() {
    setLoadListParams({
      projectId: appStore.currentProjectId,
      keyword: keyword.value,
      id: props.sourceId,
    });
    loadList();
  }

  const activeReportIndex = ref<number>(0);
  const activeReportId = ref('');

  async function showResult(record: ApiCaseExecuteHistoryItem, rowIndex: number) {
    activeReportId.value = record.id;
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
