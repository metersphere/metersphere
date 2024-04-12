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
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #resourceNum="{ record }">
        <div
          v-if="!record.integrated"
          type="text"
          class="one-line-text flex w-full"
          :class="[hasJumpPermission ? 'text-[rgb(var(--primary-5))]' : '']"
          @click="showDetail(record.resourceId)"
          >{{ record.resourceNum }}
        </div>
      </template>
      <template #resourceName="{ record }">
        <div
          v-if="!record.integrated"
          type="text"
          class="one-line-text flex max-w-[300px]"
          :class="[hasJumpPermission ? 'text-[rgb(var(--primary-5))]' : '']"
          @click="showDetail(record.resourceId)"
          >{{ record.resourceName }}
        </div>
      </template>
      <template #statusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="statusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <a-button type="text" class="arco-btn-text--secondary p-[8px_4px]" @click="statusFilterVisible = true">
            <div class="font-medium">
              {{ t(columnConfig.title as string) }}
            </div>
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group
                  v-model:model-value="statusFiltersMap[props.moduleType]"
                  direction="vertical"
                  size="small"
                >
                  <a-checkbox v-for="key of statusFilters" :key="key" :value="key">
                    <ExecutionStatus :module-type="props.moduleType" :status="key" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="arco-table-filters-bottom">
                <a-button size="mini" type="secondary" @click="handleFilterReset">
                  {{ t('common.reset') }}
                </a-button>
                <a-button size="mini" type="primary" @click="handleFilterSubmit()">
                  {{ t('common.confirm') }}
                </a-button>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #status="{ record }">
        <ExecutionStatus
          :module-type="props.moduleType"
          :status="record.status"
          :script-identifier="record.scriptIdentifier"
        />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(ExecutionMethodsLabel[record.triggerMode]) }}</span>
      </template>
      <template #triggerModeFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="triggerModeVisible"
          v-model:status-filters="triggerModeFiltersMap[props.moduleType]"
          :title="(columnConfig.title as string)"
          :list="triggerModeList"
          @search="initData()"
        >
          <template #item="{ item }">
            {{ item.label }}
          </template>
        </TableFilter>
      </template>
      <template v-if="appStore.packageType === 'enterprise'" #orgFilterName="{ columnConfig }">
        <TableFilter
          v-model:visible="orgFilterVisible"
          v-model:status-filters="orgFiltersMap[props.moduleType]"
          :title="(columnConfig.title as string)"
          mode="remote"
          value-key="id"
          label-key="name"
          :type="UserRequestTypeEnum.SYSTEM_ORGANIZATION_LIST"
          :placeholder-text="t('project.taskCenter.filterOrgPlaceholderText')"
          @search="initData()"
        >
        </TableFilter>
      </template>
      <template #projectFilterName="{ columnConfig }">
        <TableFilter
          v-model:visible="projectFilterVisible"
          v-model:status-filters="projectFiltersMap[props.moduleType]"
          :title="(columnConfig.title as string)"
          mode="remote"
          :load-option-params="{ organizationId: appStore.currentOrgId }"
          :placeholder-text="t('project.taskCenter.filterProPlaceholderText')"
          :type="UserRequestTypeEnum.SYSTEM_ORGANIZATION_PROJECT"
          @search="initData()"
        >
        </TableFilter>
      </template>
      <template #operationTime="{ record }">
        <span>{{ dayjs(record.operationTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #operation="{ record, rowIndex }">
        <MsButton
          class="!mr-0"
          :disabled="!hasAnyPermission(permissionsMap[props.group][props.moduleType].report)"
          @click="viewReport(record.id, rowIndex)"
          >{{ t('project.taskCenter.viewReport') }}
        </MsButton>
        <a-divider v-if="['RUNNING', 'RERUNNING'].includes(record.status)" direction="vertical" />
        <MsButton
          v-if="
            ['RUNNING', 'RERUNNING'].includes(record.status) &&
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
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';
  import ExecutionStatus from './executionStatus.vue';
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ReportDetailDrawer from '@/views/api-test/report/component/reportDetailDrawer.vue';
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';

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
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { RouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { ExecutionMethodsLabel, TaskCenterEnum } from '@/enums/taskCenter';

  import { ordAndProjectColumn, TaskStatus } from './utils';

  const { openNewPage } = useOpenNewPage();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const { openModal } = useModal();

  const { t } = useI18n();
  const props = defineProps<{
    group: 'system' | 'organization' | 'project';
    moduleType: keyof typeof TaskCenterEnum;
    name: string;
  }>();
  const keyword = ref<string>('');
  const statusFilterVisible = ref(false);

  const permissionsMap = {
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
        stop: ['SYSTEM_TASK_CENTER:READ'],
        jump: ['PROJECT_API_DEFINITION_CASE:READ'],
        report: ['PROJECT_API_DEFINITION_CASE:READ+EXECUTE', 'PROJECT_API_REPORT:READ'],
      },
      API_SCENARIO: {
        stop: ['SYSTEM_TASK_CENTER:READ'],
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

  const columns: MsTableColumn = [
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
      titleSlotName: 'statusFilter',
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
      titleSlotName: 'triggerModeFilter',
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
      width: hasOperationPermission.value ? 180 : 50,
    },
  ];

  const groupColumnsMap = {
    system: {
      key: TableKeyEnum.TASK_API_CASE_SYSTEM,
      columns: [...ordAndProjectColumn, ...columns],
    },
    organization: {
      key: TableKeyEnum.TASK_API_CASE_ORGANIZATION,
      columns: [...ordAndProjectColumn.slice(-1), ...columns],
    },
    project: {
      key: TableKeyEnum.TASK_API_CASE_PROJECT,
      columns,
    },
  };

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    loadRealMap.value[props.group].list,
    {
      tableKey: groupColumnsMap[props.group].key,
      scroll: {
        x: 1400,
      },
      showSetting: true,
      selectable: true,
      heightUsed: 330,
      enableDrag: false,
      showSelectAll: true,
    }
  );
  const triggerModeList = ref([
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
  ]);
  const triggerModeVisible = ref<boolean>(false);
  const triggerModeApiCase = ref([]);
  const triggerModeApiScenario = ref([]);

  const triggerModeFiltersMap = ref<Record<string, string[]>>({
    API_CASE: triggerModeApiCase.value,
    API_SCENARIO: triggerModeApiScenario.value,
  });

  const statusFilterApiCase = ref([]);
  const statusFilterApiScenario = ref([]);

  const statusFiltersMap = ref<Record<string, string[]>>({
    API_CASE: statusFilterApiCase.value,
    API_SCENARIO: statusFilterApiScenario.value,
  });

  const orgFilterVisible = ref<boolean>(false);
  const projectFilterVisible = ref<boolean>(false);
  const orgApiCaseFilter = ref([]);
  const orgApiScenarioFilter = ref([]);

  const orgFiltersMap = ref<Record<string, string[]>>({
    API_CASE: orgApiCaseFilter.value,
    API_SCENARIO: orgApiScenarioFilter.value,
  });

  const projectApiCaseFilter = ref([]);
  const projectApiScenarioFilter = ref([]);
  const projectFiltersMap = ref<Record<string, string[]>>({
    API_CASE: projectApiCaseFilter.value,
    API_SCENARIO: projectApiScenarioFilter.value,
  });

  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      moduleType: props.moduleType,
      filter: {
        status: statusFiltersMap.value[props.moduleType],
        triggerMode: triggerModeFiltersMap.value[props.moduleType],
        organizationIds: orgFiltersMap.value[props.moduleType],
        projectIds: projectFiltersMap.value[props.moduleType],
      },
    });
    loadList();
  }

  function handleFilterReset() {
    statusFiltersMap.value[props.moduleType] = [];
    statusFilterVisible.value = false;
    initData();
  }

  function handleFilterSubmit() {
    statusFilterVisible.value = false;
    initData();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.taskCenter.batchStop',
        eventTag: 'batchStop',
        permission: permissionsMap[props.group][props.moduleType].stop,
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
      title: t('project.taskCenter.batchStopTask', { num: batchParams.value.selectIds.length }),
      content: t('project.taskCenter.stopTaskContent'),
      okText: t('project.taskCenter.confirmStop'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const { selectIds, selectAll } = batchParams.value;
          await loadRealMap.value[props.group].batchStop({
            selectIds: selectAll ? [] : selectIds,
            selectAll,
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
    batchParams.value = { ...params, selectIds: params?.selectedIds || [], condition: {} };
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
      title: t('project.taskCenter.stopTask', { name: characterLimit(record.name) }),
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

  const statusFilters = computed(() => {
    return Object.keys(TaskStatus[props.moduleType]);
  });

  function handleFilterHidden(val: boolean) {
    if (!val) {
      initData();
    }
  }

  /**
   * 报告详情 showReportDetail
   */
  const activeDetailId = ref<string>('');
  const activeReportIndex = ref<number>(0);
  const showDetailDrawer = ref<boolean>(false);
  const showCaseDetailDrawer = ref<boolean>(false);

  function viewReport(id: string, rowIndex: number) {
    if (props.moduleType === 'API_CASE') {
      showCaseDetailDrawer.value = true;
    } else {
      showDetailDrawer.value = true;
    }
    activeDetailId.value = id;
    activeReportIndex.value = rowIndex - 1;
  }

  watch(
    () => props.moduleType,
    (val) => {
      if (val) {
        resetSelector();
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

  onMounted(async () => {
    await tableStore.initColumn(groupColumnsMap[props.group].key, groupColumnsMap[props.group].columns, 'drawer', true);
  });
</script>

<style scoped></style>
