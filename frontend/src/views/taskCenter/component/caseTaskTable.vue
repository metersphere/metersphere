<template>
  <div class="my-[16px] flex items-center justify-end">
    <a-input-search
      v-model:model-value="keyword"
      :placeholder="t('ms.taskCenter.search')"
      class="mr-[12px] w-[240px]"
      allow-clear
      @search="searchTask"
      @press-enter="searchTask"
      @clear="searchTask"
    />
    <MsTag no-margin size="large" :tooltip-disabled="true" class="cursor-pointer" theme="outline" @click="refresh">
      <MsIcon class="text-[16px] text-[var(color-text-4)]" :size="32" type="icon-icon_reset_outlined" />
    </MsTag>
  </div>
  <ms-base-table
    v-bind="propsRes"
    :action-config="tableBatchActions"
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
    <template #num="{ record }">
      <a-button type="text" class="max-w-full justify-start px-0" @click="showTaskDetail(record.num)">
        <a-tooltip :content="record.num">
          <div class="one-line-text">
            {{ record.num }}
          </div>
        </a-tooltip>
      </a-button>
    </template>
    <template #status="{ record }">
      <execStatus :status="record.status" />
    </template>
    <template #[FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_STATUS]="{ filterContent }">
      <execStatus :status="filterContent.value" />
    </template>
    <template #result="{ record }">
      <executionStatus :status="record.result" />
    </template>
    <template #[FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_RESULT]="{ filterContent }">
      <executionStatus :status="filterContent.value" />
    </template>
    <template #triggerMode="{ record }">
      {{ t(executeMethodMap[record.triggerMode]) }}
    </template>
    <template #executeRate="{ record }">
      <a-popover
        v-model:popup-visible="record.executeRatePopVisible"
        trigger="hover"
        position="bottom"
        :disabled="record.caseTotal === 0 || record.status === ExecuteStatusEnum.PENDING"
        @popup-visible-change="($event) => handleExecuteRatePopVisibleChange($event, record)"
      >
        <div>{{ record.executeRate || '0.00' }}%</div>
        <template #content>
          <a-spin :loading="record.loading" class="flex w-[130px] flex-col gap-[8px]">
            <div class="ms-taskCenter-execute-rate-item">
              <div class="ms-taskCenter-execute-rate-item-label">
                {{ t('ms.taskCenter.executeFinishedRate') }}
              </div>
              <div class="ms-taskCenter-execute-rate-item-value">
                {{ `${record.executeRate}%` }}
              </div>
            </div>
            <div class="ms-taskCenter-execute-rate-item">
              <div class="ms-taskCenter-execute-rate-item-label">
                <div
                  :class="`ms-taskCenter-execute-rate-item-label-point bg-[${executeFinishedRateMap.UN_EXECUTE.color}]`"
                ></div>
                {{ t(executeFinishedRateMap.UN_EXECUTE.label) }}
              </div>
              <div class="ms-taskCenter-execute-rate-item-value">{{ record.pendingCount }}</div>
            </div>
            <div class="ms-taskCenter-execute-rate-item">
              <div class="ms-taskCenter-execute-rate-item-label">
                <div
                  :class="`ms-taskCenter-execute-rate-item-label-point bg-[${executeFinishedRateMap.SUCCESS.color}]`"
                ></div>
                {{ t(executeFinishedRateMap.SUCCESS.label) }}
              </div>
              <div class="ms-taskCenter-execute-rate-item-value">{{ record.successCount }}</div>
            </div>
            <div class="ms-taskCenter-execute-rate-item">
              <div class="ms-taskCenter-execute-rate-item-label">
                <div
                  :class="`ms-taskCenter-execute-rate-item-label-point bg-[${executeFinishedRateMap.FAKE_ERROR.color}]`"
                ></div>
                {{ t(executeFinishedRateMap.FAKE_ERROR.label) }}
              </div>
              <div class="ms-taskCenter-execute-rate-item-value">{{ record.fakeErrorCount }}</div>
            </div>
            <div class="ms-taskCenter-execute-rate-item">
              <div class="ms-taskCenter-execute-rate-item-label">
                <div
                  :class="`ms-taskCenter-execute-rate-item-label-point bg-[${executeFinishedRateMap.ERROR.color}]`"
                ></div>
                {{ t(executeFinishedRateMap.ERROR.label) }}
              </div>
              <div class="ms-taskCenter-execute-rate-item-value">{{ record.errorCount }}</div>
            </div>
          </a-spin>
        </template>
      </a-popover>
    </template>
    <template #action="{ record }">
      <MsButton
        v-if="[ExecuteStatusEnum.RUNNING, ExecuteStatusEnum.RERUNNING].includes(record.status)"
        v-permission="['SYSTEM_USER:READ+DELETE']"
        @click="stopTask(record)"
      >
        {{ t('common.stop') }}
      </MsButton>
      <MsButton v-else v-permission="['SYSTEM_USER:READ+DELETE']" @click="deleteTask(record)">
        {{ t('common.delete') }}
      </MsButton>
      <MsButton
        v-if="record.status === ExecuteStatusEnum.COMPLETED && record.result === ExecuteResultEnum.ERROR"
        v-permission="['SYSTEM_USER:READ+DELETE']"
        @click="rerunTask(record)"
      >
        {{ t('ms.taskCenter.rerun') }}
      </MsButton>
      <MsButton
        v-if="record.status === ExecuteStatusEnum.COMPLETED"
        v-permission="['SYSTEM_USER:READ+DELETE']"
        @click="checkReport(record)"
      >
        {{ t('ms.taskCenter.executeResult') }}
      </MsButton>
    </template>
  </ms-base-table>
  <batchTaskReportDrawer v-model:visible="taskReportDrawerVisible" type="case" :module-type="reportModuleType" />
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
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import batchTaskReportDrawer from './batchTaskReportDrawer.vue';
  import execStatus from './execStatus.vue';
  import executionStatus from './executionStatus.vue';
  import CaseReportDrawer from '@/views/api-test/report/component/caseReportDrawer.vue';
  import ReportDetailDrawer from '@/views/api-test/report/component/reportDetailDrawer.vue';

  import { getShareTime } from '@/api/modules/api-test/report';
  import {
    getOrganizationExecuteTaskList,
    getOrganizationExecuteTaskStatistics,
    getProjectExecuteTaskList,
    getProjectExecuteTaskStatistics,
    getSystemExecuteTaskList,
    getSystemExecuteTaskStatistics,
    organizationDeleteTask,
    organizationStopTask,
    projectDeleteTask,
    projectStopTask,
    systemDeleteTask,
    systemStopTask,
  } from '@/api/modules/taskCenter';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { TaskCenterTaskItem } from '@/models/taskCenter';
  import { ReportEnum } from '@/enums/reportEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ExecuteResultEnum, ExecuteStatusEnum, ExecuteTaskType } from '@/enums/taskCenter';

  import { executeFinishedRateMap, executeMethodMap, executeResultMap, executeStatusMap } from './config';

  const props = defineProps<{
    type: 'system' | 'project' | 'org';
  }>();
  const emit = defineEmits<{
    (e: 'goDetail', id: string): void;
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();
  const { openNewPage } = useOpenNewPage();
  const tableStore = useTableStore();
  const appStore = useAppStore();

  const keyword = ref('');
  const tableSelected = ref<string[]>([]);
  const batchModalParams = ref();
  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      width: 100,
      columnSelectorDisabled: true,
      fixed: 'left',
    },
    {
      title: 'ms.taskCenter.taskName',
      dataIndex: 'taskName',
      showTooltip: true,
      width: 200,
      fixed: 'left',
    },
    {
      title: 'ms.taskCenter.executeStatus',
      dataIndex: 'status',
      slotName: 'status',
      width: 90,
      filterConfig: {
        options: Object.keys(executeStatusMap).map((key) => ({
          label: t(executeStatusMap[key as ExecuteStatusEnum].label),
          value: key,
        })),
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_STATUS,
      },
    },
    {
      title: 'ms.taskCenter.executeMethod',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      width: 90,
      filterConfig: {
        options: Object.keys(executeMethodMap).map((key) => ({
          label: t(executeMethodMap[key]),
          value: key,
        })),
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_METHOD,
      },
    },
    {
      title: 'ms.taskCenter.executeResult',
      dataIndex: 'result',
      slotName: 'result',
      width: 90,
      filterConfig: {
        options: Object.keys(executeResultMap).map((key) => ({
          label: t(executeResultMap[key].label),
          value: key,
          icon: executeResultMap[key]?.icon,
        })),
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_RESULT,
      },
    },
    {
      title: 'ms.taskCenter.caseCount',
      dataIndex: 'caseCount',
      width: 90,
    },
    {
      title: 'ms.taskCenter.executeFinishedRate',
      dataIndex: 'executeRate',
      slotName: 'executeRate',
      width: 100,
    },
    {
      title: 'ms.taskCenter.createTime',
      dataIndex: 'createTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'ms.taskCenter.startTime',
      dataIndex: 'startTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'ms.taskCenter.endTime',
      dataIndex: 'endTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'ms.taskCenter.operationUser',
      dataIndex: 'createUserName',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 180,
    },
  ];

  if (props.type === 'system') {
    columns.splice(
      2,
      0,
      {
        title: 'common.belongProject',
        dataIndex: 'belongProject',
        showTooltip: true,
        width: 100,
      },
      {
        title: 'common.belongOrg',
        dataIndex: 'belongOrg',
        showTooltip: true,
        width: 100,
      }
    );
  } else if (props.type === 'org') {
    columns.splice(2, 0, {
      title: 'common.belongProject',
      dataIndex: 'belongProject',
      showTooltip: true,
      width: 100,
    });
  }

  const currentExecuteTaskList = {
    system: getSystemExecuteTaskList,
    project: getProjectExecuteTaskList,
    org: getOrganizationExecuteTaskList,
  }[props.type];

  const currentExecuteTaskStatistics = {
    system: getSystemExecuteTaskStatistics,
    project: getProjectExecuteTaskStatistics,
    org: getOrganizationExecuteTaskStatistics,
  }[props.type];

  const currentStopTask = {
    system: projectStopTask,
    project: organizationStopTask,
    org: systemStopTask,
  }[props.type];

  const currentDeleteTask = {
    system: projectDeleteTask,
    project: organizationDeleteTask,
    org: systemDeleteTask,
  }[props.type];

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.stop',
        eventTag: 'stop',
      },
      {
        label: 'ms.taskCenter.rerun',
        eventTag: 'rerun',
      },
      {
        label: 'common.delete',
        eventTag: 'delete',
      },
    ],
  };
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    currentExecuteTaskList,
    {
      tableKey: TableKeyEnum.TASK_CENTER_CASE_TASK,
      scroll: { x: '1000px' },
      selectable: true,
      showSetting: true,
      showPagination: true,
    },
    (item) => {
      return {
        ...item,
        loading: false,
        executeRatePopVisible: false,
        startTime: item.startTime ? dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss') : '-',
        createTime: item.createTime ? dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') : '-',
        endTime: item.endTime ? dayjs(item.endTime).format('YYYY-MM-DD HH:mm:ss') : '-',
      };
    }
  );

  function searchTask() {
    setLoadListParams({ keyword: keyword.value });
    loadList();
  }

  async function initTaskStatistics() {
    try {
      const ids = propsRes.value.data.map((item) => item.id);
      if (ids.length > 0) {
        const res = await currentExecuteTaskStatistics(ids);
        res.forEach((item) => {
          const target = propsRes.value.data.find((task) => task.id === item.id);
          if (target) {
            target.executeRate = item.executeRate;
            target.pendingCount = item.pendingCount;
            target.successCount = item.successCount;
            target.fakeErrorCount = item.fakeErrorCount;
            target.errorCount = item.errorCount;
            target.caseTotal = item.caseTotal;
          }
        });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function refresh() {
    await loadList();
    initTaskStatistics();
  }

  async function handleExecuteRatePopVisibleChange(visible: boolean, record: TaskCenterTaskItem) {
    if (visible) {
      try {
        record.loading = true;
        const res = await currentExecuteTaskStatistics([record.id]);
        record.executeRate = res[0].executeRate;
        record.pendingCount = res[0].pendingCount;
        record.successCount = res[0].successCount;
        record.fakeErrorCount = res[0].fakeErrorCount;
        record.errorCount = res[0].errorCount;
        record.caseTotal = res[0].caseTotal;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        record.loading = false;
      }
    }
  }

  function showTaskDetail(id: string) {
    emit('goDetail', id);
  }

  /**
   * 删除任务
   */
  function deleteTask(record?: TaskCenterTaskItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('ms.taskCenter.deleteTaskTitle', { name: characterLimit(record?.taskName) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('ms.taskCenter.deleteCaseTaskTitle', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('ms.taskCenter.deleteCaseTaskTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            // await deleteUserInfo({
            //   selectIds,
            //   selectAll: !!params?.selectAll,
            //   excludeIds: params?.excludeIds || [],
            //   condition: { keyword: keyword.value },
            // });
          } else {
            await currentDeleteTask(record?.id || '');
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          refresh();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function stopTask(record?: TaskCenterTaskItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('ms.taskCenter.stopTaskTitle', { name: characterLimit(record?.taskName) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('ms.taskCenter.batchStopTaskTitle', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'warning',
      title,
      content: t('ms.taskCenter.stopTimeTaskTip'),
      okText: t('common.stopConfirm'),
      cancelText: t('common.cancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            // await deleteUserInfo({
            //   selectIds,
            //   selectAll: !!params?.selectAll,
            //   excludeIds: params?.excludeIds || [],
            //   condition: { keyword: keyword.value },
            // });
          } else {
            await currentStopTask(record?.id || '');
          }
          Message.success(t('common.stopped'));
          resetSelector();
          refresh();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params.selectedIds || [];
    batchModalParams.value = params;
    switch (event.eventTag) {
      case 'delete':
        deleteTask(undefined, true, params);
        break;
      case 'stop':
        stopTask(undefined, true, params);
        break;
      default:
        break;
    }
  }

  function rerunTask(record: TaskCenterTaskItem) {
    console.log('rerunTask', record);
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
    if (record.taskType === 'API_SCENARIO') {
      showDetailDrawer.value = true;
    } else {
      showCaseDetailDrawer.value = true;
    }
  }

  const shareTime = ref<string>('');
  async function getTime() {
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

  const taskReportDrawerVisible = ref(false);
  const reportModuleType = ref();
  function checkReport(record: TaskCenterTaskItem) {
    if (record.taskType.includes('BATCH')) {
      reportModuleType.value = record.taskType.includes('CASE')
        ? ReportEnum.API_REPORT
        : ReportEnum.API_SCENARIO_REPORT;
      taskReportDrawerVisible.value = true;
    } else if (['API_CASE', 'API_SCENARIO'].includes(record.taskType)) {
      showReportDetail(record);
    } else if (record.taskType === ExecuteTaskType.TEST_PLAN) {
      openNewPage(TestPlanRouteEnum.TEST_PLAN_REPORT);
    }
  }

  onMounted(async () => {
    searchTask();
    getTime();
  });

  watch(
    () => propsRes.value.data,
    () => {
      initTaskStatistics();
    },
    {
      immediate: true,
    }
  );

  await tableStore.initColumn(TableKeyEnum.TASK_CENTER_CASE_TASK, columns, 'drawer');
</script>

<style lang="less">
  .ms-taskCenter-execute-rate-item {
    @apply flex items-center justify-between;
    .ms-taskCenter-execute-rate-item-label {
      @apply flex items-center;

      gap: 4px;
      color: var(--color-text-4);
      .ms-taskCenter-execute-rate-item-label-point {
        width: 6px;
        height: 6px;
        border-radius: 100%;
      }
    }
    .ms-taskCenter-execute-rate-item-value {
      font-weight: 500;
      color: var(--color-text-1);
    }
  }
</style>
