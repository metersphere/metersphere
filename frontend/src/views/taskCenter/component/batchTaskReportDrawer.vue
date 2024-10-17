<template>
  <MsDrawer v-model:visible="visible" :title="title" :width="800" :footer="false">
    <ms-base-table v-bind="propsRes" ref="tableRef" v-on="propsEvent" @filter-change="filterChange">
      <template #name="{ record }">
        <a-button type="text" class="max-w-full justify-start px-0" @click="showReportDetail(record)">
          <div class="one-line-text">
            {{ record.num }}
          </div>
        </a-button>
      </template>
      <!-- 报告类型 -->
      <template #integrated="{ record }">
        <MsTag theme="light" :type="record.integrated ? 'primary' : undefined">
          {{ record.integrated ? t('report.collection') : t('report.independent') }}
        </MsTag>
      </template>
      <template #status="{ record }">
        <ExecutionStatus
          :module-type="props.moduleType"
          :status="record.status"
          :script-identifier="props.moduleType === ReportEnum.API_SCENARIO_REPORT ? record.scriptIdentifier : null"
        />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS]="{ filterContent }">
        <ExecutionStatus :module-type="props.moduleType" :status="filterContent.value" />
      </template>
      <template #execStatus="{ record }">
        <ExecStatus :status="record.execStatus" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_EXECUTE_RESULT]="{ filterContent }">
        <ExecStatus :status="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_REPORT_TYPE]="{ filterContent }">
        <MsTag theme="light" :type="filterContent.value ? 'primary' : undefined">
          {{ filterContent.value ? t('report.collection') : t('report.independent') }}
        </MsTag>
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode as keyof typeof TriggerModeLabel]) }}</span>
      </template>
      <template #operationTime="{ record }">
        <span>{{ dayjs(record.operationTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
    </ms-base-table>
  </MsDrawer>
  <ReportDrawer v-model:visible="reportVisible" :report-id="independentReportId" />
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import ExecStatus from '@/views/test-plan/report/component/execStatus.vue';
  import ReportDrawer from '@/views/test-plan/testPlan/detail/reportDrawer.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import { ReportExecStatus } from '@/enums/apiEnum';
  import { ReportEnum, ReportStatus, TriggerModeLabel } from '@/enums/reportEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const props = defineProps<{
    type: 'case' | 'scenario';
    moduleType: keyof typeof ReportEnum;
  }>();

  const { t } = useI18n();
  const tableStore = useTableStore();
  const appStore = useAppStore();

  const visible = defineModel<boolean>('visible', { required: true });
  const title = computed(() =>
    props.type === 'case' ? t('ms.taskCenter.batchCaseTask') : t('ms.taskCenter.batchScenarioTask')
  );
  const keyword = ref<string>('');

  type ReportShowType = 'All' | 'INDEPENDENT' | 'INTEGRATED';
  const showType = ref<ReportShowType>('All');

  const typeFilter = computed(() => {
    if (showType.value === 'All') {
      return [];
    }
    return showType.value === 'INDEPENDENT' ? [false] : [true];
  });

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
  const columns: MsTableColumn = [
    {
      title: 'report.name',
      dataIndex: 'name',
      slotName: 'name',
      width: 300,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      ellipsis: true,
    },
    {
      title: 'report.type',
      slotName: 'integrated',
      dataIndex: 'integrated',
      width: 150,
    },
    {
      title: 'report.result',
      dataIndex: 'status',
      slotName: 'status',
      filterConfig: {
        options: statusList.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS,
      },
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      width: 200,
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
      width: 200,
    },
    {
      title: 'report.trigger.mode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      showInTable: true,
      width: 150,
    },
    {
      title: 'report.operator',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 300,
      showTooltip: true,
    },
    {
      title: 'report.operating',
      dataIndex: 'startTime',
      slotName: 'startTime',
      width: 180,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
  ];

  await tableStore.initColumn(TableKeyEnum.API_TEST_REPORT, columns, 'drawer');

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    () => Promise.resolve({ list: [], total: 0 }),
    {
      tableKey: TableKeyEnum.API_TEST_REPORT,
      scroll: {
        x: '100%',
      },
      heightUsed: 256,
      paginationSize: 'mini',
    },
    (item) => ({
      ...item,
      startTime: dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  function initData(dataIndex?: string, value?: string[] | (string | number | boolean)[] | undefined) {
    const filterParams = {
      ...propsRes.value.filter,
    };
    if (dataIndex && value) {
      filterParams[dataIndex] = value;
    }
    setLoadListParams({
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleType: props.moduleType,
      filter: {
        integrated: typeFilter.value,
        ...filterParams,
      },
    });
    loadList();
  }

  function filterChange(dataIndex: string, value: string[] | (string | number | boolean)[] | undefined) {
    initData(dataIndex, value);
  }

  const reportVisible = ref(false);
  const independentReportId = ref<string>('');

  function showReportDetail(record: any) {
    independentReportId.value = record.id;
    reportVisible.value = true;
  }
</script>

<style lang="less" scoped></style>
