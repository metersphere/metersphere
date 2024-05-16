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
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode as keyof typeof TriggerModeLabel]) }}</span>
      </template>
      <template #status="{ record }">
        <ExecutionStatus :status="record.status" :module-type="ReportEnum.API_REPORT" />
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
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import { getApiCaseExecuteHistory } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { ApiCaseExecuteHistoryItem } from '@/models/apiTest/management';
  import { ReportEnum, ReportStatus, TriggerModeLabel } from '@/enums/reportEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { triggerModeOptions } from '@/views/api-test/report/utils';

  const appStore = useAppStore();
  const { t } = useI18n();
  const statusList = computed(() => {
    return Object.keys(ReportStatus[ReportEnum.API_REPORT]).map((key) => {
      return {
        value: key,
        label: t(ReportStatus[ReportEnum.API_REPORT][key].label),
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
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 150,
    },
    {
      title: 'apiTestManagement.executeResult',
      dataIndex: 'status',
      slotName: 'status',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: statusList.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT,
      },
      width: 150,
    },
    {
      title: 'apiTestManagement.taskOperator',
      dataIndex: 'operationUser',
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
