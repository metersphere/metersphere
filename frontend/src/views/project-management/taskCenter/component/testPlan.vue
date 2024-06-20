<template>
  <div class="px-[16px]">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="flex items-center"></div>
      <div class="items-right flex gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('system.organization.searchIndexPlaceholder')"
          allow-clear
          class="mx-[8px] w-[240px]"
          @search="searchList"
          @press-enter="searchList"
          @clear="searchList"
        ></a-input-search>
      </div>
    </div>
    <ms-base-table
      v-bind="propsRes"
      ref="tableRef"
      :action-config="tableBatchActions"
      :selectable="hasOperationPermission"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #resourceNum="{ record }">
        <div
          v-if="!record.integrated"
          type="text"
          class="one-line-text w-full"
          :class="[hasJumpPermission ? 'text-[rgb(var(--primary-5))]' : '']"
          @click="showDetail(record.resourceId)"
          >{{ record.resourceNum }}
        </div>
      </template>
      <template #resourceName="{ record }">
        <div
          v-if="!record.integrated"
          class="one-line-text max-w-[300px]"
          :class="[hasJumpPermission ? 'text-[rgb(var(--primary-5))]' : '']"
          @click="showDetail(record.resourceId)"
          >{{ record.resourceName }}
        </div>
      </template>
      <template #status="{ record }">
        <ExecutionStatus :status="record.status" />
      </template>
      <template #execStatus="{ record }">
        <ExecStatus :status="record.execStatus" />
      </template>
      <template #[FilterSlotNameEnum.TEST_PLAN_REPORT_EXEC_STATUS]="{ filterContent }">
        <ExecStatus :status="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER]="{ filterContent }">
        <ExecutionStatus :status="filterContent.value" />
      </template>
      <template #projectName="{ record }">
        <a-tooltip :content="`${record.projectName}`" position="tl">
          <div class="one-line-text">{{ characterLimit(record.projectName) }}</div>
        </a-tooltip>
      </template>
      <template #organizationName="{ record }">
        <a-tooltip :content="`${record.organizationName}`" position="tl">
          <div class="one-line-text">{{ characterLimit(record.organizationName) }}</div>
        </a-tooltip>
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(ExecutionMethodsLabel[record.triggerMode as keyof typeof ExecutionMethodsLabel]) }}</span>
      </template>
      <template #operationTime="{ record }">
        <span>{{ dayjs(record.operationTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #operation="{ record }">
        <div v-if="record.historyDeleted">
          <a-tooltip :content="t('project.executionHistory.cleared')">
            <MsButton
              class="!mr-0"
              :disabled="record.historyDeleted || !hasAnyPermission(permissionsMap[props.group].report)"
              @click="viewReport(record.id, record.integrated)"
              >{{ t('project.taskCenter.viewReport') }}
            </MsButton>
          </a-tooltip>
        </div>
        <div v-else>
          <MsButton
            class="!mr-0"
            :disabled="record.historyDeleted || !hasAnyPermission(permissionsMap[props.group].report)"
            @click="viewReport(record.id, record.integrated)"
            >{{ t('project.taskCenter.viewReport') }}
          </MsButton>
        </div>

        <a-divider v-if="['RUNNING', 'RERUNNING'].includes(record.execStatus)" direction="vertical" />
        <MsButton
          v-if="
            ['RUNNING', 'RERUNNING'].includes(record.execStatus) && hasAnyPermission(permissionsMap[props.group].stop)
          "
          class="!mr-0"
          @click="stop(record)"
          >{{ t('project.taskCenter.stop') }}
        </MsButton>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ExecStatus from '@/views/test-plan/report/component/execStatus.vue';
  import ExecutionStatus from '@/views/test-plan/report/component/reportStatus.vue';

  import {
    batchStopRealOrgPlan,
    batchStopRealProPlan,
    batchStopRealSysPlan,
    getRealOrgPlanList,
    getRealProPlanList,
    getRealSysPlanList,
    stopRealOrgPlan,
    stopRealProPlan,
    stopRealSysPlan,
  } from '@/api/modules/project-management/taskCenter';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { ReportExecStatus } from '@/enums/apiEnum';
  import { PlanReportStatus } from '@/enums/reportEnum';
  import { RouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ExecutionMethodsLabel, TaskCenterEnum } from '@/enums/taskCenter';

  import { getOrgColumns, getProjectColumns, Group } from './utils';

  const { openNewPage } = useOpenNewPage();

  const tableStore = useTableStore();
  const { openModal } = useModal();

  const { t } = useI18n();
  const props = defineProps<{
    group: Group;
    name: string;
  }>();
  const keyword = ref<string>('');

  const permissionsMap: Record<Group, any> = {
    organization: {
      stop: ['ORGANIZATION_TASK_CENTER:READ+STOP', 'PROJECT_TEST_PLAN:READ+EXECUTE'],
      jump: ['PROJECT_TEST_PLAN:READ'],
      report: ['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN_REPORT:READ'],
    },
    system: {
      stop: ['SYSTEM_TASK_CENTER:READ+STOP', 'PROJECT_TEST_PLAN:READ+EXECUTE'],
      jump: ['PROJECT_TEST_PLAN:READ'],
      report: ['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN_REPORT:READ'],
    },
    project: {
      stop: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
      jump: ['PROJECT_TEST_PLAN:READ'],
      report: ['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN_REPORT:READ'],
    },
  };

  const loadRealMap = ref({
    system: {
      list: getRealSysPlanList,
      stop: stopRealSysPlan,
      batchStop: batchStopRealSysPlan,
    },
    organization: {
      list: getRealOrgPlanList,
      stop: stopRealOrgPlan,
      batchStop: batchStopRealOrgPlan,
    },
    project: {
      list: getRealProPlanList,
      stop: stopRealProPlan,
      batchStop: batchStopRealProPlan,
    },
  });
  const hasJumpPermission = computed(() => hasAnyPermission(permissionsMap[props.group].jump));
  const hasOperationPermission = computed(() => hasAnyPermission(permissionsMap[props.group].stop));

  const statusResultOptions = computed(() => {
    return Object.keys(PlanReportStatus).map((key) => {
      return {
        value: key,
        label: PlanReportStatus[key].statusText,
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

  const triggerModeList = [
    {
      value: 'SCHEDULE',
      label: t('project.taskCenter.scheduledTask'),
    },
    {
      value: 'MANUAL',
      label: t('project.taskCenter.manualExecution'),
    },
    {
      value: 'API',
      label: t('project.taskCenter.interfaceCall'),
    },
    {
      value: 'BATCH',
      label: t('project.taskCenter.batchExecution'),
    },
  ];

  const staticColumns: MsTableColumn = [
    {
      title: 'project.taskCenter.resourceID',
      dataIndex: 'resourceNum',
      slotName: 'resourceNum',
      width: 200,
      sortIndex: 1,
      fixed: 'left',
      showTooltip: true,
      showInTable: true,
      showDrag: false,
      columnSelectorDisabled: true,
    },
    {
      title: 'project.taskCenter.resourceName',
      slotName: 'resourceName',
      dataIndex: 'resourceName',
      width: 300,
      showDrag: false,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'project.taskCenter.executionResult',
      dataIndex: 'status',
      slotName: 'status',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: statusResultOptions.value,
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.status',
      dataIndex: 'execStatus',
      slotName: 'execStatus',
      filterConfig: {
        options: ExecStatusList.value,
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_REPORT_EXEC_STATUS,
      },
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.executionMode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: triggerModeList,
      },
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.operator',
      slotName: 'operationName',
      dataIndex: 'operationName',
      showInTable: true,
      showDrag: true,
      showTooltip: true,
      width: 200,
    },
    {
      title: 'project.taskCenter.operating',
      dataIndex: 'operationTime',
      slotName: 'operationTime',
      width: 180,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 180 : 100,
    },
  ];

  const tableKeysMap: Record<string, any> = {
    system: TableKeyEnum.TASK_PLAN_SYSTEM,
    organization: TableKeyEnum.TASK_PLAN_ORGANIZATION,
    project: TableKeyEnum.TASK_PLAN_PROJECT,
  };

  const groupColumnsMap: Record<string, any> = {
    system: [getOrgColumns(), getProjectColumns(tableKeysMap[props.group]), ...staticColumns],
    organization: [getProjectColumns(tableKeysMap[props.group]), ...staticColumns],
    project: staticColumns,
  };

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, resetFilterParams } = useTable(
    loadRealMap.value[props.group].list,
    {
      tableKey: tableKeysMap[props.group],
      scroll: {
        x: 1400,
      },
      showSetting: true,
      selectable: hasOperationPermission.value,
      heightUsed: 330,
      enableDrag: false,
      showSelectAll: true,
    }
  );

  function initData() {
    setLoadListParams({
      moduleType: TaskCenterEnum.TEST_PLAN,
      keyword: keyword.value,
      filter: {
        ...propsRes.value.filter,
      },
    });
    loadList();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.taskCenter.batchStop',
        eventTag: 'batchStop',
        anyPermission: permissionsMap[props.group].stop,
      },
    ],
  };
  const batchParams = ref<BatchApiParams>({
    selectIds: [],
    selectAll: false,
    excludeIds: [] as string[],
    condition: {},
  });
  function batchStopRealTask() {
    openModal({
      type: 'warning',
      title: t('project.taskCenter.batchStopTask', { num: batchParams.value.currentSelectCount }),
      content: t('project.taskCenter.stopTaskContent'),
      okText: t('project.taskCenter.confirmStop'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const { selectIds, selectAll, excludeIds } = batchParams.value;
          await loadRealMap.value[props.group].batchStop({
            selectIds: selectIds || [],
            selectAll,
            excludeIds: excludeIds || [],
            condition: {
              keyword: keyword.value,
              filter: {
                ...propsRes.value.filter,
              },
            },
          });
          resetSelector();
          Message.success(t('project.taskCenter.stopSuccess'));
          initData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = { ...params, selectIds: params?.selectedIds || [], condition: params?.condition || {} };
    if (event.eventTag === 'batchStop') {
      batchStopRealTask();
    }
  }

  function viewReport(id: string, type: boolean) {
    openNewPage(RouteEnum.TEST_PLAN_REPORT_DETAIL, {
      id,
      type: type ? 'GROUP' : 'TEST_PLAN',
    });
  }

  function showDetail(id: string) {
    if (!hasJumpPermission.value) {
      return;
    }
    openNewPage(RouteEnum.TEST_PLAN_INDEX_DETAIL, {
      id,
    });
  }

  function stop(record: any) {
    openModal({
      type: 'warning',
      title: t('project.taskCenter.stopTask', { name: characterLimit(record.resourceName) }),
      content: t('project.taskCenter.stopTaskContent'),
      okText: t('project.taskCenter.confirmStop'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await loadRealMap.value[props.group].stop(record.id);
          resetSelector();
          Message.success(t('project.taskCenter.stopSuccess'));
          initData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function searchList() {
    resetSelector();
    initData();
  }

  onBeforeMount(async () => {
    initData();
  });

  watch(
    () => props.group,
    (val) => {
      if (val) {
        resetSelector();
        resetFilterParams();
        initData();
      }
    }
  );

  await tableStore.initColumn(tableKeysMap[props.group], groupColumnsMap[props.group], 'drawer', true);
</script>

<style scoped></style>
