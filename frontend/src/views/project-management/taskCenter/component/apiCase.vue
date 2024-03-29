<template>
  <div class="px-[16px]">
    <div class="mb-4 flex items-center justify-between">
      <span>{{ t('project.taskCenter.apiCaseList', { type: props.name }) }}</span>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
      ></a-input-search>
    </div>

    <ms-base-table
      v-bind="propsRes"
      ref="tableRef"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
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
                <a-checkbox-group v-model:model-value="statusListFilters" direction="vertical" size="small">
                  <a-checkbox v-for="key of statusFilters" :key="key" :value="key">
                    <ExecutionStatus :module-type="props.moduleType" :status="key" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #status="{ record }">
        <ExecutionStatus :module-type="props.moduleType" :status="record.status" />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(ExecutionMethodsLabel[record.triggerMode]) }}</span>
      </template>
      <template #operationTime="{ record }">
        <span>{{ dayjs(record.operationTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #operation="{ record, rowIndex }">
        <MsButton class="!mr-0" @click="viewReport(record.id, rowIndex)">{{
          t('project.taskCenter.viewReport')
        }}</MsButton>
        <span></span>
        <a-divider v-if="['RUNNING', 'RERUNNING'].includes(record.status)" direction="vertical" />
        <MsButton
          v-if="['RUNNING', 'RERUNNING'].includes(record.status) && hasAnyPermission(permissionsMap[props.group].stop)"
          class="!mr-0"
          @click="stop(record)"
          >{{ t('project.taskCenter.stop') }}</MsButton
        >
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
  <CaseReportDrawer
    v-model:visible="showCaseDetailDrawer"
    :report-id="activeDetailId"
    :active-report-index="activeReportIndex"
    :table-data="propsRes.data"
    :page-change="propsEvent.pageChange"
    :pagination="propsRes.msPagination!"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ExecutionStatus from './executionStatus.vue';
  import CaseReportDrawer from '@/views/api-test/report/component/caseReportDrawer.vue';
  import ReportDetailDrawer from '@/views/api-test/report/component/reportDetailDrawer.vue';

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
  import { useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { ExecutionMethodsLabel, TaskCenterEnum } from '@/enums/taskCenter';

  import { TaskStatus } from './utils';

  const tableStore = useTableStore();

  const { openModal } = useModal();

  const { t } = useI18n();
  const props = defineProps<{
    group: 'system' | 'organization' | 'project';
    moduleType: keyof typeof TaskCenterEnum;
    name: string;
  }>();
  const keyword = ref<string>('');
  const statusFilterVisible = ref(false);
  const statusListFilters = ref<string[]>([]);

  const permissionsMap = {
    organization: {
      stop: ['ORGANIZATION_TASK_CENTER:READ+STOP'],
    },
    system: {
      stop: ['ORGANIZATION_TASK_CENTER:READ+STOP'],
    },
    project: {
      stop: ['PROJECT_API_REPORT:READ'],
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

  const columns: MsTableColumn = [
    {
      title: 'project.taskCenter.resourceID',
      dataIndex: 'resourceId',
      slotName: 'resourceId',
      width: 200,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'project.taskCenter.resourceName',
      slotName: 'resourceName',
      dataIndex: 'resourceName',
      width: 300,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'project.taskCenter.executionResult',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.executionMode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
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
    },
    {
      title: 'project.taskCenter.operator',
      slotName: 'operationName',
      dataIndex: 'operationName',
      showInTable: true,
      showDrag: true,
      showTooltip: true,
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
      width: 180,
      fixed: 'right',
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    loadRealMap.value[props.group].list,
    {
      tableKey: TableKeyEnum.TASK_API_CASE,
      scroll: {
        x: '100%',
      },
      showSetting: true,
      selectable: true,
      heightUsed: 330,
      showSelectAll: true,
    }
  );

  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      moduleType: props.moduleType,
      filter: { status: statusListFilters.value },
    });
    loadList();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.taskCenter.batchStop',
        eventTag: 'batchStop',
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

  onBeforeMount(() => {
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
  onMounted(async () => {
    await tableStore.initColumn(TableKeyEnum.TASK_API_CASE, columns, 'drawer', true);
  });
</script>

<style scoped></style>
