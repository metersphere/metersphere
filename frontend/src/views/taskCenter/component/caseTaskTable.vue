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
    ref="tableRef"
    v-bind="propsRes"
    :action-config="tableBatchActions"
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
    <template #num="{ record }">
      <a-button type="text" class="max-w-full justify-start px-0" @click="showTaskDetail(record.num)">
        <a-tooltip :content="`${record.num}`">
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
      <executeResultStatus :status="record.result" />
    </template>
    <template #[FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_RESULT]="{ filterContent }">
      <executeResultStatus :status="filterContent.value" />
    </template>
    <template #triggerMode="{ record }">
      {{ t(executeMethodMap[record.triggerMode]) }}
    </template>
    <template #executeRate="{ record }">
      <executeRatePopper
        v-model:visible="record.executeRatePopVisible"
        :record="record"
        :execute-task-statistics-request="currentExecuteTaskStatistics"
      />
    </template>
    <template #action="{ record }">
      <MsButton
        v-if="[ExecuteStatusEnum.RUNNING, ExecuteStatusEnum.RERUNNING].includes(record.status)"
        v-permission="[getCurrentPermission('STOP')]"
        @click="stopTask(record)"
      >
        {{ t('common.stop') }}
      </MsButton>
      <MsButton v-else v-permission="[getCurrentPermission('DELETE')]" @click="deleteTask(record)">
        {{ t('common.delete') }}
      </MsButton>
      <MsButton
        v-if="record.result === ExecuteResultEnum.ERROR"
        v-permission="[getCurrentPermission('STOP')]"
        @click="rerunTask(record)"
      >
        {{ t('ms.taskCenter.rerun') }}
      </MsButton>
      <MsButton v-if="record.status === ExecuteStatusEnum.COMPLETED" @click="checkReport(record)">
        {{ t('ms.taskCenter.executeResult') }}
      </MsButton>
    </template>
  </ms-base-table>
  <batchTaskReportDrawer
    v-model:visible="taskReportDrawerVisible"
    :range="props.type"
    :type="reportType"
    :module-type="reportModuleType"
    :task-id="reportBatchTaskId"
    :batch-type="reportBatchType"
  />
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
  <TestPlanExecuteResultDrawer
    :id="activeDetailId"
    v-model:visible="showTestPlanDetailDrawer"
    :is-group="isTestPlanGroup"
  />
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
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
  import executeRatePopper from './executeRatePopper.vue';
  import executeResultStatus from './executeResultStatus.vue';
  import TestPlanExecuteResultDrawer from './testPlanExecuteResultDrawer.vue';
  import CaseReportDrawer from '@/views/api-test/report/component/caseReportDrawer.vue';
  import ReportDetailDrawer from '@/views/api-test/report/component/reportDetailDrawer.vue';

  import { getShareTime } from '@/api/modules/api-test/report';
  import {
    getOrganizationExecuteTaskList,
    getOrganizationExecuteTaskStatistics,
    organizationBatchDeleteTask,
    organizationBatchStopTask,
    organizationDeleteTask,
    organizationProjectOptions,
    organizationStopTask,
    organizationTaskRerun,
  } from '@/api/modules/taskCenter/organization';
  import {
    getProjectExecuteTaskList,
    getProjectExecuteTaskStatistics,
    projectBatchDeleteTask,
    projectBatchStopTask,
    projectDeleteTask,
    projectStopTask,
    projectTaskRerun,
  } from '@/api/modules/taskCenter/project';
  import {
    getSystemExecuteTaskList,
    getSystemExecuteTaskStatistics,
    systemBatchDeleteTask,
    systemBatchStopTask,
    systemDeleteTask,
    systemOrgOptions,
    systemProjectOptions,
    systemStopTask,
    systemTaskRerun,
  } from '@/api/modules/taskCenter/system';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { TaskCenterTaskItem } from '@/models/taskCenter';
  import { ReportEnum } from '@/enums/reportEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ExecuteResultEnum, ExecuteStatusEnum, ExecuteTaskType, TaskCenterEnum } from '@/enums/taskCenter';

  import { executeMethodMap, executeResultMap, executeStatusMap } from './config';

  const props = defineProps<{
    type: 'system' | 'project' | 'org';
  }>();
  const emit = defineEmits<{
    (e: 'goDetail', id: string): void;
    (e: 'init'): void;
  }>();

  const route = useRoute();
  const { t } = useI18n();
  const { openModal } = useModal();
  const tableStore = useTableStore();
  const appStore = useAppStore();

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  const keyword = ref('');
  const columns: MsTableColumn = [
    {
      title: 'ms.taskCenter.taskID',
      dataIndex: 'num',
      slotName: 'num',
      width: 100,
      columnSelectorDisabled: true,
    },
    {
      title: 'ms.taskCenter.taskName',
      dataIndex: 'taskName',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.executeStatus',
      dataIndex: 'status',
      slotName: 'status',
      width: 120,
      filterConfig: {
        options: Object.keys(executeStatusMap).map((key) => ({
          label: t(executeStatusMap[key as ExecuteStatusEnum].label),
          value: key,
        })),
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_STATUS,
      },
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.executeMethod',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      width: 120,
      filterConfig: {
        options: Object.keys(executeMethodMap).map((key) => ({
          label: t(executeMethodMap[key]),
          value: key,
        })),
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_METHOD,
      },
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.executeResult',
      dataIndex: 'result',
      slotName: 'result',
      width: 120,
      filterConfig: {
        options: Object.keys(executeResultMap).map((key) => ({
          label: t(executeResultMap[key].label),
          value: key,
          icon: executeResultMap[key]?.icon,
        })),
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_RESULT,
      },
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.caseCount',
      dataIndex: 'caseCount',
      width: 90,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.executeFinishedRate',
      dataIndex: 'executeRate',
      slotName: 'executeRate',
      width: 100,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.createTime',
      dataIndex: 'createTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.startTime',
      dataIndex: 'startTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.endTime',
      dataIndex: 'endTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.operationUser',
      dataIndex: 'createUserName',
      width: 100,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 180,
    },
  ];

  async function initProjectAndOrgs() {
    try {
      if (props.type === 'system') {
        const projects = await systemProjectOptions();
        const orgs = await systemOrgOptions();
        columns.splice(
          2,
          0,
          {
            title: 'common.belongProject',
            dataIndex: 'projectName',
            showTooltip: true,
            showDrag: true,
            width: 200,
            filterConfig: {
              options: projects.map((item) => ({
                label: item.name,
                value: item.id,
              })),
              filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_BELONG_PROJECT,
            },
          },
          {
            title: 'common.belongOrg',
            dataIndex: 'organizationName',
            showTooltip: true,
            showDrag: true,
            width: 200,
            filterConfig: {
              options: orgs.map((item) => ({
                label: item.name,
                value: item.id,
              })),
              filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_BELONG_PROJECT,
            },
          }
        );
      } else if (props.type === 'org') {
        const projects = await organizationProjectOptions();
        columns.splice(2, 0, {
          title: 'common.belongProject',
          dataIndex: 'projectName',
          showTooltip: true,
          showDrag: true,
          width: 200,
          filterConfig: {
            options: projects.map((item) => ({
              label: item.name,
              value: item.id,
            })),
            filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_BELONG_PROJECT,
          },
        });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  await initProjectAndOrgs();

  function getCurrentPermission(action: 'STOP' | 'DELETE') {
    return {
      system: {
        STOP: 'SYSTEM_CASE_TASK_CENTER:EXEC+STOP',
        DELETE: 'SYSTEM_CASE_TASK_CENTER:READ+DELETE',
      },
      org: {
        STOP: 'ORGANIZATION_CASE_TASK_CENTER:EXEC+STOP',
        DELETE: 'ORGANIZATION_CASE_TASK_CENTER:READ+DELETE',
      },
      project: {
        STOP: 'PROJECT_CASE_TASK_CENTER:EXEC+STOP',
        DELETE: 'PROJECT_CASE_TASK_CENTER:READ+DELETE',
      },
    }[props.type][action];
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

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.stop',
        eventTag: 'stop',
        anyPermission: [getCurrentPermission('STOP')],
      },
      // {
      //   label: 'ms.taskCenter.rerun',
      //   eventTag: 'rerun',
      // },
      {
        label: 'common.delete',
        eventTag: 'delete',
        anyPermission: [getCurrentPermission('DELETE')],
      },
    ],
  };
  const { propsRes, propsEvent, loadList, setLoadListParams, setLoading, resetSelector } = useTable(
    currentExecuteTaskList,
    {
      tableKey: TableKeyEnum.TASK_CENTER_CASE_TASK,
      scroll: { x: '1000px', y: '100%' },
      selectable: hasAnyPermission([getCurrentPermission('STOP'), getCurrentPermission('DELETE')]),
      heightUsed: 288,
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

  function showTaskDetail(id: string) {
    emit('goDetail', id);
  }

  const currentDeleteTask = {
    system: systemDeleteTask,
    project: projectDeleteTask,
    org: organizationDeleteTask,
  }[props.type];

  const currentBatchDeleteTask = {
    system: systemBatchDeleteTask,
    project: projectBatchDeleteTask,
    org: organizationBatchDeleteTask,
  }[props.type];

  const conditionParams = computed(() => {
    return {
      keyword: keyword.value,
      filter: propsRes.value.filter,
    };
  });

  /**
   * 删除任务
   */
  function deleteTask(record?: TaskCenterTaskItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('ms.taskCenter.deleteTaskTitle', { name: characterLimit(record?.taskName) });
    if (isBatch) {
      title = t('ms.taskCenter.deleteCaseTaskTitle', {
        count: params?.currentSelectCount || (params?.selectedIds || []).length,
      });
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
          setLoading(true);
          if (isBatch) {
            await currentBatchDeleteTask({
              selectIds: params?.selectedIds || [],
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              condition: conditionParams.value,
            });
          } else {
            await currentDeleteTask(record?.id || '');
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          refresh();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          setLoading(false);
        }
      },
      hideCancel: false,
    });
  }

  const currentStopTask = {
    system: systemStopTask,
    project: projectStopTask,
    org: organizationStopTask,
  }[props.type];

  const currentBatchStopTask = {
    system: systemBatchStopTask,
    project: projectBatchStopTask,
    org: organizationBatchStopTask,
  }[props.type];

  /**
   * 停止任务
   */
  function stopTask(record?: TaskCenterTaskItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('ms.taskCenter.stopTaskTitle', { name: characterLimit(record?.taskName) });
    if (isBatch) {
      title = t('ms.taskCenter.batchStopTaskTitle', {
        count: params?.currentSelectCount || (params?.selectedIds || []).length,
      });
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
          setLoading(true);
          if (isBatch) {
            await currentBatchStopTask({
              selectIds: params?.selectedIds || [],
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              condition: conditionParams.value,
            });
          } else {
            await currentStopTask(record?.id || '');
          }
          Message.success(t('common.stopped'));
          resetSelector();
          refresh();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          setLoading(false);
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

  const currentRerunTask = {
    system: systemTaskRerun,
    project: projectTaskRerun,
    org: organizationTaskRerun,
  }[props.type];
  async function rerunTask(record: TaskCenterTaskItem) {
    try {
      setLoading(true);
      await currentRerunTask(record.id);
      Message.success(t('common.executionSuccess'));
      resetSelector();
      await loadList();
      initTaskStatistics();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      setLoading(false);
    }
  }

  /**
   * 报告详情 showReportDetail
   */
  const activeDetailId = ref<string>('');
  const activeReportIndex = ref<number>(0);
  const showDetailDrawer = ref<boolean>(false);
  const showCaseDetailDrawer = ref<boolean>(false);
  const showTestPlanDetailDrawer = ref<boolean>(false);
  const isTestPlanGroup = ref(false);

  function showReportDetail(record: TaskCenterTaskItem) {
    activeDetailId.value = record.reportId;
    if (
      [ExecuteTaskType.API_SCENARIO, ExecuteTaskType.TEST_PLAN_API_SCENARIO].includes(record.taskType) ||
      (record.taskType.includes('SCENARIO') && record.integrated === true)
    ) {
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

  const taskReportDrawerVisible = ref(false);
  const reportModuleType = ref();
  const reportType = ref<'CASE' | 'SCENARIO'>('CASE');
  const reportBatchType = ref<ExecuteTaskType>(ExecuteTaskType.API_CASE);
  const reportBatchTaskId = ref('');
  function checkReport(record: TaskCenterTaskItem) {
    if (record.taskType.includes('BATCH') && record.integrated !== true) {
      // integrated 为 true 时，表示是集成报告，直接查看报告
      reportModuleType.value = record.taskType.includes('CASE')
        ? ReportEnum.API_REPORT
        : ReportEnum.API_SCENARIO_REPORT;
      reportType.value = record.taskType.includes('CASE') ? 'CASE' : 'SCENARIO';
      reportBatchType.value = record.taskType;
      reportBatchTaskId.value = record.id;
      taskReportDrawerVisible.value = true;
    } else if (
      [
        ExecuteTaskType.API_CASE,
        ExecuteTaskType.API_SCENARIO,
        ExecuteTaskType.TEST_PLAN_API_CASE,
        ExecuteTaskType.TEST_PLAN_API_SCENARIO,
      ].includes(record.taskType) ||
      record.integrated === true
    ) {
      showReportDetail(record);
    } else if ([ExecuteTaskType.TEST_PLAN_GROUP, ExecuteTaskType.TEST_PLAN].includes(record.taskType)) {
      showTestPlanDetailDrawer.value = true;
      activeDetailId.value = record.id;
      isTestPlanGroup.value = record.taskType === ExecuteTaskType.TEST_PLAN_GROUP;
    }
  }

  onMounted(async () => {
    searchTask();
    getTime();
    emit('init');
    if (route.query.task && route.query.type === TaskCenterEnum.CASE && route.query.id) {
      const { id, taskType, integrated, reportId } = route.query;
      checkReport({
        id: id as string,
        taskType: taskType as ExecuteTaskType,
        integrated,
        reportId,
      } as unknown as TaskCenterTaskItem);
    }
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

  watch(
    () => appStore.currentProjectId,
    () => {
      searchTask();
    }
  );

  await tableStore.initColumn(TableKeyEnum.TASK_CENTER_CASE_TASK, columns, 'drawer');
</script>

<style lang="less"></style>
