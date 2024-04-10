<template>
  <div class="p-4 pt-0">
    <div class="mb-4 flex items-center justify-between">
      <!-- TODO这个版本不上 -->
      <!--      <a-button type="primary">
        {{ t('project.taskCenter.createTask') }}
      </a-button>-->
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('system.organization.searchIndexPlaceholder')"
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
      <template #resourceNum="{ record }">
        <div
          type="text"
          class="one-line-text flex w-full text-[rgb(var(--primary-5))]"
          @click="showDetail(record.resourceId)"
          >{{ record.resourceNum }}</div
        >
      </template>
      <template #resourceName="{ record }">
        <div type="text" class="flex w-full">{{ record.resourceName }}</div>
      </template>
      <template #resourceType="{ record }">
        <div type="text" class="flex w-full">{{ t(resourceTypeMap[record.resourceType].label) }}</div>
      </template>
      <template #value="{ record }">
        <a-select
          v-model:model-value="record.value"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full min-w-[250px]"
          :disabled="!record.enable"
          @change="() => changeRunRules(record)"
        >
          <a-option v-for="item of syncFrequencyOptions" :key="item.value" :value="item.value">
            <span class="text-[var(--color-text-2)]"> {{ item.value }}</span
            ><span class="ml-1 text-[var(--color-text-n4)] hover:text-[rgb(var(--primary-5))]">
              {{ item.label }}
            </span>
          </a-option>
        </a-select>
      </template>
      <template #operation="{ record }">
        <a-switch
          v-model="record.enable"
          size="small"
          type="line"
          :before-change="() => handleBeforeEnableChange(record)"
        />
        <a-divider direction="vertical" />
        <MsButton class="!mr-0" @click="delSchedule(record)">{{ t('common.delete') }}</MsButton>
        <!-- TODO这一版不上 -->
        <!-- <a-divider direction="vertical" />
        <MsButton class="!mr-0" @click="edit(record)">{{ t('common.edit') }}</MsButton>
        <a-divider direction="vertical" />
        <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect" /> -->
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { switchDefinitionSchedule } from '@/api/modules/api-test/management';
  import {
    deleteScheduleSysTask,
    getScheduleOrgApiCaseList,
    getScheduleProApiCaseList,
    getScheduleSysApiCaseList,
    switchSchedule,
    updateRunRules,
  } from '@/api/modules/project-management/taskCenter';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useTableStore } from '@/store';

  import { TimingTaskCenterApiCaseItem } from '@/models/projectManagement/taskCenter';
  import { RouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { TaskCenterEnum } from '@/enums/taskCenter';

  import { resourceTypeMap } from './utils';

  const { openNewPage } = useOpenNewPage();

  const tableStore = useTableStore();
  const { openModal } = useModal();
  const { t } = useI18n();

  const props = defineProps<{
    group: 'system' | 'organization' | 'project';
    moduleType: keyof typeof TaskCenterEnum;
    name: string;
  }>();

  const keyword = ref<string>('');
  const columns: MsTableColumn = [
    {
      title: 'project.taskCenter.resourceID',
      dataIndex: 'resourceNum',
      slotName: 'resourceNum',
      width: 140,
      showInTable: true,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'project.taskCenter.resourceName',
      slotName: 'resourceName',
      dataIndex: 'resourceName',
      width: 200,
      showDrag: true,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'project.taskCenter.resourceClassification',
      dataIndex: 'resourceType',
      slotName: 'resourceType',
      showInTable: true,
      width: 150,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'project.taskCenter.operationRule',
      dataIndex: 'value',
      slotName: 'value',
      showInTable: true,
      width: 300,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'project.taskCenter.operator',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 200,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'project.taskCenter.operating',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.nextExecutionTime',
      slotName: 'nextTime',
      dataIndex: 'nextTime',
      showInTable: true,
      width: 200,
      showDrag: true,
      // sortable: {
      //   sortDirections: ['ascend', 'descend'],
      //   sorter: true,
      // },
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 180,
      fixed: 'right',
      showDrag: false,
    },
  ];
  const syncFrequencyOptions = [
    { label: t('apiTestManagement.timeTaskHour'), value: '0 0 0/1 * * ?' },
    { label: t('apiTestManagement.timeTaskSixHour'), value: '0 0 0/6 * * ?' },
    { label: t('apiTestManagement.timeTaskTwelveHour'), value: '0 0 0/12 * * ?' },
    { label: t('apiTestManagement.timeTaskDay'), value: '0 0 0 * * ?' },
  ];

  const loadRealMap = ref({
    system: getScheduleSysApiCaseList,
    organization: getScheduleOrgApiCaseList,
    project: getScheduleProApiCaseList,
  });

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    loadRealMap.value[props.group],
    {
      tableKey: TableKeyEnum.TASK_SCHEDULE_TASK,
      scroll: {
        x: 1200,
      },
      showSetting: true,
      selectable: true,
      heightUsed: 300,
      enableDrag: false,
      showSelectorAll: true,
    },
    // eslint-disable-next-line no-return-assign
    (item) => ({
      ...item,
      nextTime: dayjs(item.nextTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      scheduleTagType: props.moduleType,
    });
    loadList();
  }

  function searchList() {
    resetSelector();
    initData();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.taskCenter.batchStop',
        eventTag: 'batchStop',
      },
      {
        label: 'project.taskCenter.batchExecution',
        eventTag: 'batchExecution',
      },
    ],
  };

  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {}

  function edit(record: any) {}

  function delSchedule(record: any) {
    openModal({
      type: 'error',
      title: t('project.taskCenter.delSchedule'),
      content: t('project.taskCenter.delSchedule.tip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteScheduleSysTask(record?.id as string);
          Message.success(t('project.taskCenter.delScheduleSuccess'));
          initData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  async function handleBeforeEnableChange(record: TimingTaskCenterApiCaseItem) {
    try {
      await switchSchedule(record?.id as string);
      Message.success(
        t(record.enable ? 'project.taskCenter.disableScheduleSuccess' : 'project.taskCenter.enableScheduleSuccess')
      );
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }
  /**
   * 更新运行规则
   */
  async function changeRunRules(record: TimingTaskCenterApiCaseItem) {
    try {
      await updateRunRules(record.id, record.value);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      console.log(error);
    }
  }

  /**
   * 跳转接口用例详情
   */

  function showDetail(id: string) {
    if (props.moduleType === 'TEST_RESOURCE') {
      openNewPage(RouteEnum.API_TEST_SCENARIO, {
        sId: id,
      });
    }
  }

  const moreActions: ActionsItem[] = [
    {
      label: 'common.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  function handleMoreActionSelect(item: ActionsItem) {}

  onBeforeMount(() => {
    initData();
  });

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
    await tableStore.initColumn(TableKeyEnum.TASK_SCHEDULE_TASK, columns, 'drawer', true);
  });
</script>

<style scoped lang="less">
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-1);
      }
    }
  }
</style>
