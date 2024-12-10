<template>
  <MsDrawer v-model:visible="visible" :title="title" :width="800" :footer="false">
    <ms-base-table v-bind="propsRes" ref="tableRef" v-on="propsEvent" @filter-change="filterChange">
      <template #name="{ record }">
        <a-button type="text" class="max-w-full justify-start px-0" @click="showReportDetail(record)">
          <div class="one-line-text">
            {{ record.name }}
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
        <ExecStatus v-if="record.execStatus" :status="record.execStatus" />
        <span v-else>-</span>
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
      <template #createTime="{ record }">
        <span>{{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
    </ms-base-table>
  </MsDrawer>
  <CaseReportDrawer
    v-model:visible="showCaseDetailDrawer"
    :report-id="activeDetailId"
    :active-report-index="activeReportIndex"
    :table-data="propsRes.data"
    :page-change="propsEvent.pageChange"
    :pagination="{
      current: 1,
      pageSize: 10,
      total: 1,
    }"
    :share-time="shareTime"
  />
  <ReportDetailDrawer
    v-model:visible="showDetailDrawer"
    :report-id="activeDetailId"
    :active-report-index="activeReportIndex"
    :table-data="propsRes.data"
    :page-change="propsEvent.pageChange"
    :pagination="{
      current: 1,
      pageSize: 10,
      total: 1,
    }"
    :share-time="shareTime"
  />
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import CaseReportDrawer from '@/views/api-test/report/component/caseReportDrawer.vue';
  import ReportDetailDrawer from '@/views/api-test/report/component/reportDetailDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import ExecStatus from '@/views/test-plan/report/component/execStatus.vue';

  import { getShareTime } from '@/api/modules/api-test/report';
  import { organizationBatchTaskReportList } from '@/api/modules/taskCenter/organization';
  import { projectBatchTaskReportList } from '@/api/modules/taskCenter/project';
  import { systemBatchTaskReportList } from '@/api/modules/taskCenter/system';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { TaskCenterTaskItem } from '@/models/taskCenter';
  import { ReportEnum, ReportStatus, TriggerModeLabel } from '@/enums/reportEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ExecuteTaskType } from '@/enums/taskCenter';

  import { executeMethodMap } from './config';

  const props = defineProps<{
    range: 'system' | 'project' | 'org';
    type: 'CASE' | 'SCENARIO';
    moduleType: keyof typeof ReportEnum;
    taskId: string;
    batchType: ExecuteTaskType;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const visible = defineModel<boolean>('visible', { required: true });
  const title = computed(() =>
    props.type === 'CASE' ? t('ms.taskCenter.batchCaseTask') : t('ms.taskCenter.batchScenarioTask')
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
      fixed: 'left',
    },
    {
      title: 'report.type',
      slotName: 'integrated',
      dataIndex: 'integrated',
      width: 120,
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
      width: 120,
    },
    {
      title: 'report.trigger.mode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      showInTable: true,
      filterConfig: {
        options: Object.keys(executeMethodMap).map((key) => ({
          label: t(executeMethodMap[key]),
          value: key,
        })),
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_METHOD,
      },
      width: 100,
    },
    {
      title: 'report.operator',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 150,
      showTooltip: true,
    },
    {
      title: 'report.operating',
      dataIndex: 'createTime',
      slotName: 'createTime',
      width: 180,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
  ];

  const currentList = {
    system: systemBatchTaskReportList,
    org: organizationBatchTaskReportList,
    project: projectBatchTaskReportList,
  }[props.range];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    currentList,
    {
      columns,
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
      taskId: props.taskId,
      batchType: props.batchType,
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

  /**
   * 报告详情 showReportDetail
   */
  const activeDetailId = ref<string>('');
  const activeReportIndex = ref<number>(0);
  const showDetailDrawer = ref<boolean>(false);
  const showCaseDetailDrawer = ref<boolean>(false);

  function showReportDetail(record: TaskCenterTaskItem) {
    activeDetailId.value = record.id;
    if ([ExecuteTaskType.API_SCENARIO_BATCH, ExecuteTaskType.TEST_PLAN_API_SCENARIO_BATCH].includes(props.batchType)) {
      showDetailDrawer.value = true;
    } else {
      showCaseDetailDrawer.value = true;
    }
  }

  const shareTime = ref<string>('');
  async function getTime() {
    if (!appStore.currentProjectId) {
      return;
    }
    try {
      const res = await getShareTime(appStore.currentProjectId);
      const match = res.match(/^(\d+)([MYHD])$/);
      if (match) {
        const value = parseInt(match[1], 10);
        const type = match[2];
        const translations: Record<string, string> = {
          M: t('msTimeSelector.month'),
          Y: t('msTimeSelector.year'),
          H: t('msTimeSelector.hour'),
          D: t('msTimeSelector.day'),
        };
        shareTime.value = value + (translations[type] || translations.D);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        initData();
        getTime();
      }
    },
    { immediate: true }
  );
</script>

<style lang="less" scoped></style>
