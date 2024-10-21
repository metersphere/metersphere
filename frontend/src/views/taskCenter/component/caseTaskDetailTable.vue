<template>
  <div class="my-[16px] flex items-center justify-end gap-[12px]">
    <a-input-search
      v-model:model-value="keyword"
      :placeholder="t('ms.taskCenter.search')"
      class="w-[240px]"
      allow-clear
      @search="searchTask"
      @press-enter="searchTask"
      @clear="searchTask"
    />
    <MsCascader
      v-model:model-value="resourcePool"
      mode="native"
      :options="resourcePoolOptions"
      :placeholder="t('common.pleaseSelect')"
      option-size="small"
      label-key="name"
      value-key="id"
      class="w-[240px]"
      :prefix="t('ms.taskCenter.resourcePool')"
      label-path-mode
      @change="searchTask"
    >
    </MsCascader>
    <MsTag no-margin size="large" :tooltip-disabled="true" class="cursor-pointer" theme="outline" @click="searchTask">
      <MsIcon class="text-[16px] text-[var(color-text-4)]" :size="32" type="icon-icon_reset_outlined" />
    </MsTag>
  </div>
  <ms-base-table
    v-bind="propsRes"
    :action-config="tableBatchActions"
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
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
    <template #resourcePoolNode="{ record }">
      <div>{{ record.resourcePoolNode }}</div>
      <a-tooltip v-if="record.resourcePoolNodeStatus === false" :content="t('ms.taskCenter.nodeErrorTip')">
        <icon-exclamation-circle-fill class="ml-[4px] !text-[rgb(var(--warning-6))]" :size="18" />
      </a-tooltip>
    </template>
    <template #action="{ record }">
      <MsButton
        v-if="[ExecuteStatusEnum.RUNNING, ExecuteStatusEnum.RERUNNING].includes(record.status)"
        v-permission="['SYSTEM_USER:READ+DELETE']"
        @click="stopTask(record)"
      >
        {{ t('common.stop') }}
      </MsButton>
      <!-- <MsButton
        v-if="record.status === ExecuteStatusEnum.COMPLETED && record.result === ExecuteResultEnum.ERROR"
        v-permission="['SYSTEM_USER:READ+DELETE']"
        @click="rerunTask(record)"
      >
        {{ t('ms.taskCenter.rerun') }}
      </MsButton> -->
      <MsButton
        v-if="record.status === ExecuteStatusEnum.COMPLETED"
        v-permission="['SYSTEM_USER:READ+DELETE']"
        @click="checkExecuteResult(record)"
      >
        {{ t('ms.taskCenter.executeResult') }}
      </MsButton>
    </template>
  </ms-base-table>
  <caseExecuteResultDrawer :id="executeResultId" v-model:visible="caseExecuteResultDrawerVisible" />
  <scenarioExecuteResultDrawer :id="executeResultId" v-model:visible="scenarioExecuteResultDrawerVisible" />
</template>

<script setup lang="ts">
  import { CascaderOption, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsCascader from '@/components/business/ms-cascader/index.vue';
  import caseExecuteResultDrawer from './caseExecuteResultDrawer.vue';
  import execStatus from './execStatus.vue';
  import executionStatus from './executionStatus.vue';
  import scenarioExecuteResultDrawer from './scenarioExecuteResultDrawer.vue';

  import {
    getOrganizationExecuteTaskDetailList,
    getOrgTaskCenterResourcePools,
    organizationBatchStopTaskDetail,
    organizationStopTaskDetail,
    organizationTaskOrder,
  } from '@/api/modules/taskCenter/organization';
  import {
    getProjectExecuteTaskDetailList,
    getProjectTaskCenterResourcePools,
    projectBatchStopTaskDetail,
    projectStopTaskDetail,
    projectTaskOrder,
  } from '@/api/modules/taskCenter/project';
  import {
    getResourcePoolsStatus,
    getSystemExecuteTaskDetailList,
    getSystemTaskCenterResourcePools,
    systemBatchStopTaskDetail,
    systemStopTaskDetail,
    systemTaskOrder,
  } from '@/api/modules/taskCenter/system';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { TaskCenterTaskDetailItem } from '@/models/taskCenter';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ExecuteResultEnum, ExecuteStatusEnum } from '@/enums/taskCenter';

  import { executeMethodMap, executeResultMap, executeStatusMap } from './config';

  const props = defineProps<{
    type: 'system' | 'project' | 'org';
    id?: string;
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();
  const tableStore = useTableStore();

  const keyword = ref('');
  const resourcePool = ref([]);
  const resourcePoolOptions = ref<CascaderOption[]>([]);
  const tableSelected = ref<string[]>([]);
  const batchModalParams = ref();

  const columns: MsTableColumn = [
    {
      title: t('ms.taskCenter.taskID'),
      dataIndex: 'num',
      width: 100,
      columnSelectorDisabled: true,
      showTooltip: true,
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
      width: 100,
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
      width: 100,
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
      width: 100,
      filterConfig: {
        options: Object.keys(executeResultMap).map((key) => ({
          label: t(executeResultMap[key].label),
          value: key,
          icon: executeResultMap[key].icon,
        })),
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_RESULT,
      },
    },
    {
      title: 'ms.taskCenter.resourcePool',
      dataIndex: 'resourcePoolName',
      isStringTag: true,
      isTag: true,
    },
    {
      title: 'ms.taskCenter.node',
      dataIndex: 'resourcePoolNode',
      slotName: 'resourcePoolNode',
      width: 180,
    },
    {
      title: 'ms.taskCenter.queue',
      dataIndex: 'lineNum',
      width: 100,
    },
    {
      title: 'ms.taskCenter.threadID',
      dataIndex: 'threadId',
      showTooltip: true,
      width: 190,
    },
    {
      title: 'ms.taskCenter.startExecuteTime',
      dataIndex: 'startTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'ms.taskCenter.endExecuteTime',
      dataIndex: 'endTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'ms.taskCenter.operationUser',
      dataIndex: 'userName',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 150,
    },
  ];

  if (props.type === 'system') {
    columns.splice(2, 0, [
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
      },
    ]);
  } else if (props.type === 'org') {
    columns.splice(2, 0, [
      {
        title: 'common.belongProject',
        dataIndex: 'belongProject',
        showTooltip: true,
        width: 100,
      },
    ]);
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.stop',
        eventTag: 'stop',
      },
      // {
      //   label: 'ms.taskCenter.rerun',
      //   eventTag: 'rerun',
      // },
    ],
  };

  const currentExecuteTaskDetailList = {
    system: getSystemExecuteTaskDetailList,
    project: getProjectExecuteTaskDetailList,
    org: getOrganizationExecuteTaskDetailList,
  }[props.type];

  const { propsRes, propsEvent, loadList, setLoadListParams, getTableQueryParams, resetSelector } = useTable(
    currentExecuteTaskDetailList,
    {
      tableKey: TableKeyEnum.TASK_CENTER_CASE_TASK_DETAIL,
      scroll: { x: '1000px' },
      selectable: true,
      heightUsed: 288,
      showSetting: true,
      size: 'default',
    },
    (item) => {
      return {
        ...item,
        resourcePoolName: [item.resourcePoolName],
        startTime: item.startTime ? dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss') : '-',
        endTime: item.endTime ? dayjs(item.endTime).format('YYYY-MM-DD HH:mm:ss') : '-',
      };
    }
  );

  function searchTask() {
    setLoadListParams({ keyword: keyword.value, resourcePools: resourcePool.value });
    loadList();
  }

  const currentStopTask = {
    system: systemStopTaskDetail,
    project: projectStopTaskDetail,
    org: organizationStopTaskDetail,
  }[props.type];

  const currentBatchStopTask = {
    system: systemBatchStopTaskDetail,
    project: projectBatchStopTaskDetail,
    org: organizationBatchStopTaskDetail,
  }[props.type];

  function stopTask(record?: TaskCenterTaskDetailItem, isBatch?: boolean, params?: BatchActionQueryParams) {
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
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            await currentBatchStopTask({
              selectIds,
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              ...getTableQueryParams(),
            });
          } else {
            await currentStopTask(record?.id || '');
          }
          Message.success(t('common.stopped'));
          resetSelector();
          loadList();
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
    batchModalParams.value = params;
    switch (event.eventTag) {
      case 'stop':
        stopTask(undefined, true, params);
        break;
      default:
        break;
    }
  }

  const executeResultId = ref('');
  const caseExecuteResultDrawerVisible = ref(false);
  const scenarioExecuteResultDrawerVisible = ref(false);
  function checkExecuteResult(record: TaskCenterTaskDetailItem) {
    executeResultId.value = record.id;
    if (record.resourceType === 'API_SCENARIO') {
      scenarioExecuteResultDrawerVisible.value = true;
    } else {
      caseExecuteResultDrawerVisible.value = true;
    }
  }

  const currentResourcePoolRequest = {
    system: getProjectTaskCenterResourcePools,
    project: getOrgTaskCenterResourcePools,
    org: getSystemTaskCenterResourcePools,
  }[props.type];

  async function initResourcePools() {
    try {
      const res = await currentResourcePoolRequest();
      resourcePoolOptions.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 初始化当前页所有任务的资源池状态
   */
  async function initCurrentPageResourcePoolsStatus() {
    const ids = propsRes.value.data.map((item) => item.id);
    if (ids.length === 0) {
      return;
    }
    try {
      const res = await getResourcePoolsStatus(ids);
      res.forEach((item) => {
        const target = propsRes.value.data.find((task) => task.id === item.id);
        if (target) {
          target.resourcePoolNodeStatus = item.status;
        }
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const currentQueueRequest = {
    system: systemTaskOrder,
    project: projectTaskOrder,
    org: organizationTaskOrder,
  }[props.type];

  /**
   * 初始化当前页所有任务的排队状态
   */
  async function initCurrentPageQueue() {
    const ids = propsRes.value.data.map((item) => item.id);
    if (ids.length === 0) {
      return;
    }
    try {
      const res = await currentQueueRequest(ids);
      propsRes.value.data.forEach((item) => {
        const queue = res[item.id];
        if (queue) {
          item.lineNum = queue;
        } else {
          item.lineNum = t('ms.taskCenter.waitQueue');
        }
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // async function rerunTask(record: TaskCenterTaskDetailItem) {
  //   try {
  //     // await deleteUserInfo({
  //     //   selectIds,
  //     //   selectAll: !!params?.selectAll,
  //     //   excludeIds: params?.excludeIds || [],
  //     //   condition: { keyword: keyword.value },
  //     // });
  //     Message.success(t('common.executionSuccess'));
  //     resetSelector();
  //     await loadList();
  //     initCurrentPageResourcePoolsStatus();
  //   } catch (error) {
  //     // eslint-disable-next-line no-console
  //     console.log(error);
  //   }
  // }

  watch(
    () => propsRes.value.data,
    () => {
      initCurrentPageResourcePoolsStatus();
      initCurrentPageQueue();
    },
    {
      immediate: true,
    }
  );

  onMounted(async () => {
    if (props.id) {
      keyword.value = props.id;
      setLoadListParams({ keyword: props.id });
    }
    initResourcePools();
    await loadList();
    initCurrentPageResourcePoolsStatus();
    initCurrentPageQueue();
  });

  await tableStore.initColumn(TableKeyEnum.TASK_CENTER_CASE_TASK_DETAIL, columns, 'drawer');
</script>

<style lang="less" scoped></style>
