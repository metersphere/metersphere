<template>
  <div>
    <ms-base-table v-bind="propsRes" no-disable filter-icon-align-left v-on="propsEvent">
      <template #num="{ record }">
        <span type="text" class="px-0">{{ record.num }}</span>
      </template>
      <template #[FilterSlotNameEnum.API_TEST_SCENARIO_EXECUTE_RESULT]="{ filterContent }">
        <executeStatus :status="filterContent.value" />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode as keyof typeof TriggerModeLabel]) }}</span>
      </template>
      <template #status="{ record }">
        <executeStatus :status="record.status" />
      </template>
      <template #operation="{ record }">
        <div v-if="record.historyDeleted">
          <a-tooltip :content="t('project.executionHistory.cleared')" position="top">
            <MsButton
              :disabled="
                record.historyDeleted ||
                !hasAnyPermission(['PROJECT_API_SCENARIO:READ+EXECUTE', 'PROJECT_API_REPORT:READ'])
              "
              class="!mr-0"
              @click="showResult(record)"
              >{{ t('apiScenario.executeHistory.execution.operation') }}
            </MsButton>
          </a-tooltip>
        </div>
        <div v-else>
          <MsButton
            :disabled="
              record.historyDeleted ||
              !hasAnyPermission(['PROJECT_API_SCENARIO:READ+EXECUTE', 'PROJECT_API_REPORT:READ'])
            "
            class="!mr-0"
            @click="showResult(record)"
            >{{ t('apiScenario.executeHistory.execution.operation') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <!-- 场景报告抽屉 -->
    <caseAndScenarioReportDrawer v-model:visible="showScenarioReportVisible" is-scenario :report-id="reportId" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecuteStatus from '@/views/api-test/scenario/components/executeStatus.vue';

  import { getExecuteHistory } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import { ExecuteHistoryItem } from '@/models/apiTest/scenario';
  import { ExecuteStatusFilters } from '@/enums/apiEnum';
  import { TriggerModeLabel } from '@/enums/reportEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { triggerModeOptions } from '@/views/api-test/report/utils';

  const tableQueryParams = ref<any>();

  const keyword = ref('');
  const { t } = useI18n();
  const props = defineProps<{
    scenarioId?: string | number; // 详情 id
    readOnly?: boolean;
  }>();

  const executeStatusFilters = computed(() => {
    return Object.values(ExecuteStatusFilters).map((key) => {
      return {
        value: key,
        label: key,
      };
    });
  });

  const columns: MsTableColumn = [
    {
      title: 'apiScenario.executeHistory.num',
      dataIndex: 'id',
      slotName: 'num',
      fixed: 'left',
      width: 150,
    },
    {
      title: 'apiScenario.executeHistory.execution.triggerMode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      showTooltip: true,
      filterConfig: {
        options: triggerModeOptions,
      },
      width: 100,
    },
    {
      title: 'apiScenario.executeHistory.execution.status',
      dataIndex: 'status',
      slotName: 'status',
      filterConfig: {
        options: executeStatusFilters.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_SCENARIO_EXECUTE_RESULT,
      },
      width: 100,
    },
    {
      title: 'apiScenario.executeHistory.execution.operator',
      dataIndex: 'operationUser',
      showTooltip: true,
      width: 100,
    },
    {
      title: 'apiScenario.executeHistory.execution.operatorTime',
      dataIndex: 'startTime',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      showInTable: true,
      showDrag: false,
      width: 100,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getExecuteHistory,
    {
      columns,
      scroll: { x: '100%' },
      showSetting: false,
      selectable: false,
      heightUsed: 398,
    },
    (item) => ({
      ...item,
      startTime: dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  // 加载评审历史列表
  function loadExecuteHistoryList() {
    const params = {
      keyword: keyword.value,
      id: props.scenarioId,
    };
    setLoadListParams(params);
    loadList();
    tableQueryParams.value = {
      ...params,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    };
  }

  const showScenarioReportVisible = ref(false);
  const reportId = ref('');
  function showResult(record: ExecuteHistoryItem) {
    reportId.value = record.id;
    showScenarioReportVisible.value = true;
  }

  onBeforeMount(() => {
    loadExecuteHistoryList();
  });
</script>

<style lang="less" scoped></style>
