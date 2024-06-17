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
      <template #[FilterSlotNameEnum.GLOBAL_TASK_CENTER_API_CASE_STATUS]="{ filterContent }">
        <ExecutionStatus :module-type="props.moduleType" :status="filterContent.value" />
      </template>
      <template #status="{ record }">
        <ExecutionStatus
          :module-type="props.moduleType"
          :status="record.status"
          :script-identifier="record.scriptIdentifier"
        />
      </template>
      <template #execStatus="{ record }">
        <ExecStatus :status="record.execStatus" />
      </template>
      <template #[FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_STATUS]="{ filterContent }">
        <ExecStatus :status="filterContent.value" />
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
      <template #operation="{ record, rowIndex }">
        <div v-if="record.historyDeleted">
          <a-tooltip :content="t('project.executionHistory.cleared')">
            <MsButton
              class="!mr-0"
              :disabled="
                record.historyDeleted || !hasAnyPermission(permissionsMap[props.group][props.moduleType].report)
              "
              @click="viewReport(record.id, rowIndex)"
              >{{ t('project.taskCenter.viewReport') }}
            </MsButton>
          </a-tooltip>
        </div>
        <div v-else>
          <MsButton
            class="!mr-0"
            :disabled="record.historyDeleted || !hasAnyPermission(permissionsMap[props.group][props.moduleType].report)"
            @click="viewReport(record.id, rowIndex)"
            >{{ t('project.taskCenter.viewReport') }}
          </MsButton>
        </div>

        <a-divider v-if="['RUNNING', 'RERUNNING'].includes(record.execStatus)" direction="vertical" />
        <MsButton
          v-if="
            ['RUNNING', 'RERUNNING'].includes(record.execStatus) &&
            hasAnyPermission(permissionsMap[props.group][props.moduleType].stop)
          "
          class="!mr-0"
          @click="stop(record)"
          >{{ t('project.taskCenter.stop') }}
        </MsButton>
      </template>
    </ms-base-table>
  </div>
  <ReportDetailDrawer
    v-model:visible="showDetailDrawer"
    :report-id="activeDetailId"
    :active-report-index="activeReportIndex"
    :table-data="propsRes.data"
    :page-change="propsEvent.pageChange"
    :pagination="propsRes.msPagination!"
  />
  <caseAndScenarioReportDrawer v-model:visible="showCaseDetailDrawer" :report-id="activeDetailId" />
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ExecutionStatus from './executionStatus.vue';
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ReportDetailDrawer from '@/views/api-test/report/component/reportDetailDrawer.vue';
  import ExecStatus from '@/views/test-plan/report/component/execStatus.vue';

  import {
    batchStopRealOrdApi,
    batchStopRealProjectApi,
    batchStopRealSystemApi,
    getRealOrdApiCaseList,
    getRealProApiCaseList,
    getRealSysApiCaseList,
    stopRealOrdApi,
    stopRealProjectApi,
    stopRealSysApi,
  } from '@/api/modules/project-management/taskCenter';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { ReportExecStatus } from '@/enums/apiEnum';
  import { RouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ExecutionMethodsLabel } from '@/enums/taskCenter';

  import type { ExtractedKeys } from './utils';
  import { getOrgColumns, getProjectColumns, Group, TaskStatus } from './utils';

  const { openNewPage } = useOpenNewPage();
  const tableStore = useTableStore();
  const { openModal } = useModal();

  const { t } = useI18n();
  const props = defineProps<{
    group: Group;
    moduleType: ExtractedKeys;
    name: string;
  }>();
  const keyword = ref<string>('');

  const permissionsMap: Record<Group, any> = {
    organization: {
      API_CASE: {
        stop: ['ORGANIZATION_TASK_CENTER:READ+STOP', 'PROJECT_API_DEFINITION_CASE:READ+EXECUTE'],
        jump: ['PROJECT_API_DEFINITION_CASE:READ'],
        report: ['PROJECT_API_DEFINITION_CASE:READ+EXECUTE', 'PROJECT_API_REPORT:READ'],
      },
      API_SCENARIO: {
        stop: ['ORGANIZATION_TASK_CENTER:READ+STOP', 'PROJECT_API_SCENARIO:READ+EXECUTE'],
        jump: ['PROJECT_API_SCENARIO:READ'],
        report: ['PROJECT_API_SCENARIO:READ+EXECUTE', 'PROJECT_API_REPORT:READ'],
      },
    },
    system: {
      API_CASE: {
        stop: ['SYSTEM_TASK_CENTER:READ+STOP', 'PROJECT_API_DEFINITION_CASE:READ+EXECUTE'],
        jump: ['PROJECT_API_DEFINITION_CASE:READ'],
        report: ['PROJECT_API_DEFINITION_CASE:READ+EXECUTE', 'PROJECT_API_REPORT:READ'],
      },
      API_SCENARIO: {
        stop: ['SYSTEM_TASK_CENTER:READ+STOP', 'PROJECT_API_SCENARIO:READ+EXECUTE'],
        jump: ['PROJECT_API_SCENARIO:READ'],
        report: ['PROJECT_API_SCENARIO:READ+EXECUTE', 'PROJECT_API_REPORT:READ'],
      },
    },
    project: {
      API_CASE: {
        stop: ['PROJECT_API_DEFINITION_CASE:READ+EXECUTE'],
        jump: ['PROJECT_API_DEFINITION_CASE:READ'],
        report: ['PROJECT_API_DEFINITION_CASE:READ+EXECUTE', 'PROJECT_API_REPORT:READ'],
      },
      API_SCENARIO: {
        stop: ['PROJECT_API_SCENARIO:READ+EXECUTE'],
        jump: ['PROJECT_API_SCENARIO:READ'],
        report: ['PROJECT_API_SCENARIO:READ+EXECUTE', 'PROJECT_API_REPORT:READ'],
      },
    },
  };

  const loadRealMap = ref({
    system: {
      list: getRealSysApiCaseList,
      stop: stopRealSysApi,
      batchStop: batchStopRealSystemApi,
    },
    organization: {
      list: getRealOrdApiCaseList,
      stop: stopRealOrdApi,
      batchStop: batchStopRealOrdApi,
    },
    project: {
      list: getRealProApiCaseList,
      stop: stopRealProjectApi,
      batchStop: batchStopRealProjectApi,
    },
  });
  const hasJumpPermission = computed(() => hasAnyPermission(permissionsMap[props.group][props.moduleType].jump));
  const hasOperationPermission = computed(() => hasAnyPermission(permissionsMap[props.group][props.moduleType].stop));

  const statusFilters = computed(() => {
    return Object.keys(TaskStatus[props.moduleType]).map((key: any) => {
      return {
        value: key,
        ...TaskStatus[props.moduleType][key],
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
        options: statusFilters.value,
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_API_CASE_STATUS,
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
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_STATUS,
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
      title: 'project.taskCenter.resourcePool',
      slotName: 'poolName',
      dataIndex: 'poolName',
      showInTable: true,
      showDrag: true,
      showTooltip: true,
      width: 200,
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
    system: TableKeyEnum.TASK_API_CASE_SYSTEM,
    organization: TableKeyEnum.TASK_API_CASE_ORGANIZATION,
    project: TableKeyEnum.TASK_API_CASE_PROJECT,
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
      keyword: keyword.value,
      moduleType: props.moduleType,
    });
    loadList();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.taskCenter.batchStop',
        eventTag: 'batchStop',
        anyPermission: permissionsMap[props.group][props.moduleType].stop,
      },
      // {
      // label: 'project.taskCenter.batchExecution',
      // eventTag: 'batchExecution',
      // },
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
            moduleType: props.moduleType,
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

  function searchList() {
    resetSelector();
    initData();
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
          await loadRealMap.value[props.group].stop(props.moduleType, record.id);
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

  function execution(record: any) {}

  onBeforeMount(async () => {
    initData();
  });

  /**
   * 报告详情 showReportDetail
   */
  const activeDetailId = ref<string>('');
  const activeReportIndex = ref<number>(0);
  const showDetailDrawer = ref<boolean>(false);
  const showCaseDetailDrawer = ref<boolean>(false);

  function viewReport(id: string, rowIndex: number) {
    activeDetailId.value = id;
    activeReportIndex.value = rowIndex;
    if (props.moduleType === 'API_CASE') {
      showCaseDetailDrawer.value = true;
    } else {
      showDetailDrawer.value = true;
    }
  }

  watch(
    () => props.moduleType,
    (val) => {
      if (val) {
        resetSelector();
        resetFilterParams();
        initData();
      }
    }
  );

  /**
   * 跳转接口用例详情
   */
  function showDetail(id: string) {
    if (!hasJumpPermission.value) {
      return;
    }
    if (props.moduleType === 'API_CASE') {
      openNewPage(RouteEnum.API_TEST_MANAGEMENT, {
        cId: id,
      });
    }
    if (props.moduleType === 'API_SCENARIO') {
      openNewPage(RouteEnum.API_TEST_SCENARIO, {
        id,
      });
    }
  }

  onBeforeUnmount(() => {
    if (props.moduleType === 'API_CASE') {
      showCaseDetailDrawer.value = false;
    } else {
      showDetailDrawer.value = false;
    }
  });

  await tableStore.initColumn(tableKeysMap[props.group], groupColumnsMap[props.group], 'drawer', true);
</script>

<style scoped></style>
