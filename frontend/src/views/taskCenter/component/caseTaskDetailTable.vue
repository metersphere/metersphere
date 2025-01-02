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
      multiple
      :options="resourcePoolOptions"
      :placeholder="t('common.pleaseSelect')"
      option-size="small"
      class="w-[300px]"
      :prefix="t('ms.taskCenter.resourcePool')"
      :virtual-list-props="{ height: 200 }"
      strictly
      label-path-mode
      @clear="clearResourcePools"
      @popup-visible-change="handleResourcePoolVisibleChange"
      @change="handleResourcePoolChange"
    >
    </MsCascader>
    <MsTag no-margin size="large" :tooltip-disabled="true" class="cursor-pointer" theme="outline" @click="searchTask">
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
    <template #resourcePoolNode="{ record }">
      <template v-if="record.resourcePoolNode">
        <a-tooltip :content="record.resourcePoolNode">
          <div class="one-line-text">{{ record.resourcePoolNode }}</div>
        </a-tooltip>
        <a-tooltip v-if="record.resourcePoolNodeStatus === false" :content="t('ms.taskCenter.nodeErrorTip')">
          <icon-exclamation-circle-fill class="min-w-[18px] !text-[rgb(var(--warning-6))]" :size="18" />
        </a-tooltip>
      </template>
      <span v-else>-</span>
    </template>
    <template #lineNum="{ record }">
      <a-tooltip
        v-if="record.errorMessage || !record.lineNum"
        :content="!record.lineNum ? t('ms.taskCenter.taskDetailErrorMsg') : record.errorMessage"
      >
        <icon-exclamation-circle-fill class="min-w-[18px] !text-[rgb(var(--warning-6))]" />
      </a-tooltip>
      <div v-else>{{ record.lineNum }}</div>
    </template>
    <template #action="{ record }">
      <MsButton
        v-if="[ExecuteStatusEnum.RUNNING, ExecuteStatusEnum.RERUNNING].includes(record.status)"
        v-permission="[getCurrentPermission('STOP')]"
        @click="stopTask(record)"
      >
        {{ t('common.stop') }}
      </MsButton>
      <a-tooltip
        v-if="record.status !== ExecuteStatusEnum.PENDING"
        :content="t('common.executionResultCleaned')"
        :disabled="!record.deleted"
      >
        <MsButton :disabled="record.resultDeleted" @click="checkExecuteResult(record)">
          {{ t('ms.taskCenter.executeResult') }}
        </MsButton>
      </a-tooltip>
    </template>
  </ms-base-table>
  <caseExecuteResultDrawer
    v-if="caseExecuteResultDrawerVisible"
    :id="activeRecord.id"
    v-model:visible="caseExecuteResultDrawerVisible"
    :user-name="activeRecord.userName"
    :resource-name="activeRecord.resourceName"
  />
  <scenarioExecuteResultDrawer
    v-if="scenarioExecuteResultDrawerVisible"
    :id="activeRecord.id"
    v-model:visible="scenarioExecuteResultDrawerVisible"
    :user-name="activeRecord.userName"
  />
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { CascaderOption, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsCascader from '@/components/business/ms-cascader/index.vue';
  import execStatus from './execStatus.vue';
  import executeResultStatus from './executeResultStatus.vue';

  import {
    getOrganizationExecuteTaskDetailList,
    getOrgTaskCenterResourcePools,
    organizationBatchStopTaskDetail,
    organizationProjectOptions,
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
    systemOrgOptions,
    systemProjectOptions,
    systemStopTaskDetail,
    systemTaskOrder,
  } from '@/api/modules/taskCenter/system';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import { useAppStore } from '@/store';
  import { characterLimit, mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { TaskCenterTaskDetailItem } from '@/models/taskCenter';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ExecuteStatusEnum } from '@/enums/taskCenter';

  import { executeMethodMap, executeResultMap, executeStatusMap } from './config';

  const scenarioExecuteResultDrawer = defineAsyncComponent(() => import('./scenarioExecuteResultDrawer.vue'));
  const caseExecuteResultDrawer = defineAsyncComponent(() => import('./caseExecuteResultDrawer.vue'));

  const props = defineProps<{
    type: 'system' | 'project' | 'org';
    id?: string;
  }>();

  const route = useRoute();
  const { t } = useI18n();
  const { openModal } = useModal();
  const appStore = useAppStore();
  const tableStore = useTableStore();

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  const keyword = ref('');
  const resourcePool = ref<string[]>([]);
  const resourcePoolOptions = ref<CascaderOption[]>([]);
  const tableSelected = ref<string[]>([]);
  const batchModalParams = ref();

  const columns: MsTableColumn = [
    {
      title: 'ms.taskCenter.taskID',
      dataIndex: 'num',
      width: 100,
      columnSelectorDisabled: true,
      showTooltip: true,
    },
    {
      title: 'ms.taskCenter.taskName',
      dataIndex: 'taskName',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.caseName',
      dataIndex: 'resourceName',
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.executeStatus',
      dataIndex: 'status',
      slotName: 'status',
      width: 120,
      filterConfig: {
        options: Object.keys(executeStatusMap)
          .map((key) => ({
            label: t(executeStatusMap[key as ExecuteStatusEnum].label),
            value: key,
          }))
          .filter((e) => e.value !== ExecuteStatusEnum.RERUNNING),
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
          icon: executeResultMap[key].icon,
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
      title: 'ms.taskCenter.resourcePool',
      dataIndex: 'resourcePoolName',
      isStringTag: true,
      isTag: true,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.node',
      dataIndex: 'resourcePoolNode',
      slotName: 'resourcePoolNode',
      width: 180,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.queue',
      dataIndex: 'lineNum',
      slotName: 'lineNum',
      width: 100,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.threadID',
      dataIndex: 'threadId',
      showTooltip: true,
      width: 190,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.startExecuteTime',
      dataIndex: 'startTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.endExecuteTime',
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
      dataIndex: 'userName',
      width: 100,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 150,
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

  function getCurrentPermission(action: 'STOP') {
    return {
      system: {
        STOP: 'SYSTEM_CASE_TASK_CENTER:EXEC+STOP',
      },
      org: {
        STOP: 'ORGANIZATION_CASE_TASK_CENTER:EXEC+STOP',
      },
      project: {
        STOP: 'PROJECT_CASE_TASK_CENTER:EXEC+STOP',
      },
    }[props.type][action];
  }

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
    ],
  };

  const currentExecuteTaskDetailList = {
    system: getSystemExecuteTaskDetailList,
    project: getProjectExecuteTaskDetailList,
    org: getOrganizationExecuteTaskDetailList,
  }[props.type];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    currentExecuteTaskDetailList,
    {
      tableKey: TableKeyEnum.TASK_CENTER_CASE_TASK_DETAIL,
      scroll: { x: '1000px', y: '100%' },
      selectable: hasAnyPermission([getCurrentPermission('STOP')]),
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

  const resourcePoolIds = ref<Set<string>>(new Set([]));
  const resourcePoolNodes = ref<Set<string>>(new Set([]));
  function searchTask() {
    setLoadListParams({
      keyword: keyword.value,
      resourcePoolIds: Array.from(resourcePoolIds.value),
      resourcePoolNodes: Array.from(resourcePoolNodes.value),
    });
    loadList();
  }

  function handleResourcePoolVisibleChange(val: boolean) {
    if (!val) {
      searchTask();
    }
  }

  function clearResourcePools() {
    resourcePoolIds.value = new Set([]);
    resourcePoolNodes.value = new Set([]);
    searchTask();
  }

  function handleResourcePoolChange(value: string[]) {
    if (resourcePool.value.length < value.length) {
      // 添加选中节点
      const lastValue = value[value.length - 1];
      const resourceClass = resourcePoolOptions.value.find((e) => e.value === lastValue);
      if (resourceClass && resourceClass.children && resourceClass.children.length > 0) {
        const childIds = resourceClass.children.map((e) => e.value as string);
        resourcePool.value.push(...value, ...childIds);
        resourcePool.value = Array.from(new Set(resourcePool.value));
        childIds.forEach((e) => {
          resourcePoolNodes.value.add(e);
        });
      }
      if (resourceClass) {
        // 是资源池分类
        resourcePoolIds.value.add(resourceClass.value as string);
      } else {
        // 是资源池节点
        resourcePoolNodes.value.add(lastValue);
      }
    } else if (value.length === 0) {
      clearResourcePools();
    } else {
      // 移除选中节点
      const lastValue = value[value.length - 1];
      const resourceClass = resourcePoolOptions.value.find((e) => e.value === lastValue);
      if (resourceClass) {
        // 是资源池分类
        resourcePoolIds.value.delete(resourceClass.value as string);
      } else {
        // 是资源池节点
        resourcePoolNodes.value.delete(lastValue);
      }
    }
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

  const conditionParams = computed(() => {
    return {
      keyword: keyword.value,
      filter: propsRes.value.filter,
    };
  });

  function stopTask(record?: TaskCenterTaskDetailItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('ms.taskCenter.stopTaskTitle', { name: characterLimit(record?.taskName) });
    if (isBatch) {
      title = t('ms.taskCenter.batchStopTaskTitle', {
        count: params?.currentSelectCount || tableSelected.value.length,
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

  const activeRecord = ref<TaskCenterTaskDetailItem>({} as TaskCenterTaskDetailItem);
  const caseExecuteResultDrawerVisible = ref(false);
  const scenarioExecuteResultDrawerVisible = ref(false);
  function checkExecuteResult(record: TaskCenterTaskDetailItem) {
    activeRecord.value = record;
    if (record.resourceType.includes('API_SCENARIO')) {
      scenarioExecuteResultDrawerVisible.value = true;
    } else {
      caseExecuteResultDrawerVisible.value = true;
    }
  }

  const currentResourcePoolRequest = {
    system: getSystemTaskCenterResourcePools,
    project: getProjectTaskCenterResourcePools,
    org: getOrgTaskCenterResourcePools,
  }[props.type];

  async function initResourcePools() {
    try {
      const res = await currentResourcePoolRequest();
      resourcePoolOptions.value = mapTree(res, (node) => ({
        label: node.name,
        value: node.id,
        children: node.children,
      }));
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
        if (item.errorMessage) {
          item.lineNum = '';
        } else if (!item.resourcePoolNode.includes(':')) {
          item.lineNum = '-';
        } else if (queue === -1) {
          item.lineNum = t('ms.taskCenter.waitQueue');
        } else if (queue) {
          item.lineNum = queue;
        } else if (
          [ExecuteStatusEnum.COMPLETED, ExecuteStatusEnum.STOPPED, ExecuteStatusEnum.RUNNING].includes(item.status) ||
          !item.resourcePoolNode
        ) {
          item.lineNum = '-';
        } else {
          item.lineNum = '';
        }
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

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

  watch(
    () => appStore.currentProjectId,
    () => {
      searchTask();
    }
  );

  onMounted(async () => {
    if (props.id) {
      keyword.value = props.id;
      setLoadListParams({ keyword: props.id });
    }
    initResourcePools();
    if (route.query.task && route.query.id) {
      const { id, userName, resourceType } = route.query;
      checkExecuteResult({ id, userName, resourceType: [resourceType] } as unknown as TaskCenterTaskDetailItem);
    }
    await loadList();
    initCurrentPageResourcePoolsStatus();
    initCurrentPageQueue();
  });

  await tableStore.initColumn(TableKeyEnum.TASK_CENTER_CASE_TASK_DETAIL, columns, 'drawer');
</script>

<style lang="less" scoped></style>
