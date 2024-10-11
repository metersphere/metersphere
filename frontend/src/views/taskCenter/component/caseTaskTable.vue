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
    <template #num="{ record }">
      <a-button type="text" class="max-w-full justify-start px-0" @click="showTaskDetail(record.id)">
        <div class="one-line-text">
          {{ record.num }}
        </div>
      </a-button>
    </template>
    <template #executeStatus="{ record }">
      <execStatus :status="record.executeStatus" />
    </template>
    <template #[FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_STATUS]="{ filterContent }">
      <execStatus :status="filterContent.value" />
    </template>
    <template #executeResult="{ record }">
      <executionStatus :status="record.executeResult" />
    </template>
    <template #[FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_RESULT]="{ filterContent }">
      <executionStatus :status="filterContent.value" />
    </template>
    <template #executeFinishedRate="{ record }">
      <a-popover trigger="hover" position="bottom">
        <div>{{ record.executeFinishedRate }}</div>
        <template #content>
          <div class="flex w-[130px] flex-col gap-[8px]">
            <div class="ms-taskCenter-execute-rate-item">
              <div class="ms-taskCenter-execute-rate-item-label">{{ t('ms.taskCenter.passThreshold') }}</div>
              <div class="ms-taskCenter-execute-rate-item-value">{{ record.passThreshold }}</div>
            </div>
            <div class="ms-taskCenter-execute-rate-item">
              <div class="ms-taskCenter-execute-rate-item-label">
                {{ record.testPlanId ? t('ms.taskCenter.executeFinishedRate') : t('ms.taskCenter.executeProgress') }}
              </div>
              <div class="ms-taskCenter-execute-rate-item-value">
                {{ `${((record.unExecuteCount / record.caseCount) * 100).toFixed(2)}%` }}
              </div>
            </div>
            <div class="ms-taskCenter-execute-rate-item">
              <div class="ms-taskCenter-execute-rate-item-label">
                <div
                  :class="`ms-taskCenter-execute-rate-item-label-point bg-[${executeFinishedRateMap.UN_EXECUTE.color}]`"
                ></div>
                {{ t(executeFinishedRateMap.UN_EXECUTE.label) }}
              </div>
              <div class="ms-taskCenter-execute-rate-item-value">{{ record.unExecuteCount }}</div>
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
            <div v-if="record.testPlanId" class="ms-taskCenter-execute-rate-item">
              <div class="ms-taskCenter-execute-rate-item-label">
                <div
                  :class="`ms-taskCenter-execute-rate-item-label-point`"
                  :style="{ backgroundColor: executeFinishedRateMap.BLOCK.color }"
                ></div>
                {{ t(executeFinishedRateMap.BLOCK.label) }}
              </div>
              <div class="ms-taskCenter-execute-rate-item-value">{{ record.blockCount }}</div>
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
          </div>
        </template>
      </a-popover>
    </template>
    <template #action="{ record }">
      <MsButton v-permission="['SYSTEM_USER:READ+DELETE']" @click="stopTask(record)">
        {{ t('common.stop') }}
      </MsButton>
      <MsButton v-permission="['SYSTEM_USER:READ+DELETE']" @click="deleteTask(record)">
        {{ t('common.delete') }}
      </MsButton>
      <MsButton v-permission="['SYSTEM_USER:READ+DELETE']" @click="rerunTask(record)">
        {{ t('ms.taskCenter.rerun') }}
      </MsButton>
      <MsButton v-permission="['SYSTEM_USER:READ+DELETE']" @click="checkReport(record)">
        {{ t('ms.taskCenter.checkReport') }}
      </MsButton>
    </template>
  </ms-base-table>
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
  import execStatus from './execStatus.vue';
  import executionStatus from './executionStatus.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { executeFinishedRateMap, executeMethodMap, executeResultMap, executeStatusMap } from './utils';

  const props = defineProps<{
    type: 'system' | 'project' | 'org';
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();
  const tableStore = useTableStore();

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
    },
    {
      title: 'ms.taskCenter.taskName',
      dataIndex: 'name',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'ms.taskCenter.executeStatus',
      dataIndex: 'executeStatus',
      slotName: 'executeStatus',
      width: 90,
      filterConfig: {
        options: Object.keys(executeStatusMap).map((key) => ({
          label: t(executeStatusMap[key].label),
          value: key,
        })),
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_EXEC_STATUS,
      },
    },
    {
      title: 'ms.taskCenter.executeMethod',
      dataIndex: 'executeMethod',
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
      dataIndex: 'executeResult',
      slotName: 'executeResult',
      width: 90,
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
      title: 'ms.taskCenter.caseCount',
      dataIndex: 'caseCount',
      width: 90,
    },
    {
      title: 'ms.taskCenter.executeFinishedRate',
      dataIndex: 'executeFinishedRate',
      slotName: 'executeFinishedRate',
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
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 220,
    },
  ];

  if (props.type === 'system') {
    columns.splice(
      1,
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
    columns.splice(1, 0, {
      title: 'common.belongProject',
      dataIndex: 'belongProject',
      showTooltip: true,
      width: 100,
    });
  }

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
    () =>
      Promise.resolve({
        list: [
          {
            id: '1',
            num: 10086,
            name: '测试任务',
            belongProject: '测试项目',
            belongOrg: '测试组织',
            executeStatus: 'PENDING',
            executeMethod: '手动执行',
            executeResult: 'SUCCESS',
            caseCount: 100,
            executeFinishedRate: '100%',
            startTime: 1630000000000,
            createTime: 1630000000000,
            endTime: 1630000000000,
            passThreshold: '100%',
            unExecuteCount: 0,
            successCount: 100,
            fakeErrorCount: 0,
            blockCount: 0,
            errorCount: 0,
          },
        ],
        total: 1,
      }),
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
        startTime: dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss'),
        createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
        endTime: dayjs(item.endTime).format('YYYY-MM-DD HH:mm:ss'),
      };
    }
  );

  function searchTask() {
    setLoadListParams({ keyword: keyword.value });
    loadList();
  }

  function showTaskDetail(id: string) {
    console.log('showTaskDetail', id);
  }

  /**
   * 删除任务
   */
  function deleteTask(record?: any, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('ms.taskCenter.deleteTaskTitle', { name: characterLimit(record?.name) });
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
          // await deleteUserInfo({
          //   selectIds,
          //   selectAll: !!params?.selectAll,
          //   excludeIds: params?.excludeIds || [],
          //   condition: { keyword: keyword.value },
          // });
          Message.success(t('common.deleteSuccess'));
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

  function stopTask(record?: any, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('ms.taskCenter.stopTaskTitle', { name: characterLimit(record?.name) });
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
          // await deleteUserInfo({
          //   selectIds,
          //   selectAll: !!params?.selectAll,
          //   excludeIds: params?.excludeIds || [],
          //   condition: { keyword: keyword.value },
          // });
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

  function rerunTask(record: any) {
    console.log('rerunTask', record);
  }

  function checkReport(record: any) {
    console.log('checkReport', record);
  }

  onMounted(() => {
    loadList();
  });

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
