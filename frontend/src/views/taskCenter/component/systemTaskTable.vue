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
    no-disable
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
    <template #num="{ record }">
      <a-button type="text" class="max-w-full justify-start px-0" @click="checkDetail(record)">
        <a-tooltip :content="record.id">
          <div class="one-line-text">
            {{ record.num }}
          </div>
        </a-tooltip>
      </a-button>
    </template>
    <template #status="{ record }">
      <a-switch
        v-model:model-value="record.enable"
        size="small"
        :before-change="() => handleBeforeEnableChange(record)"
      ></a-switch>
    </template>
    <template #resourceType="{ record }">
      {{ t(scheduleTaskTypeMap[record.resourceType]) }}
    </template>
    <template #runRule="{ record }">
      <MsCronSelect
        v-if="hasAnyPermission([getCurrentPermission('EDIT')])"
        v-model:model-value="record.value"
        v-model:loading="record.runRuleLoading"
        @change="(val) => handleRunRuleChange(val, record)"
      />
      <span v-else>{{ record.value }}</span>
    </template>
    <template #action="{ record }">
      <MsButton
        v-if="['API_IMPORT', 'TEST_PLAN', 'API_SCENARIO'].includes(record.resourceType)"
        v-permission="[getCurrentPermission('DELETE')]"
        class="!mr-[12px]"
        @click="deleteTask(record)"
      >
        {{ t('common.delete') }}
      </MsButton>
      <MsButton class="!mr-0" @click="checkDetail(record)">
        {{ t('common.detail') }}
      </MsButton>
    </template>
  </ms-base-table>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCronSelect from '@/components/pure/ms-cron-select/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import {
    getOrganizationScheduleList,
    organizationBatchCloseTask,
    organizationBatchOpenTask,
    organizationDeleteSchedule,
    organizationEditCron,
    organizationScheduleSwitch,
  } from '@/api/modules/taskCenter/organization';
  import {
    getProjectScheduleList,
    projectBatchCloseTask,
    projectBatchOpenTask,
    projectDeleteSchedule,
    projectEditCron,
    projectScheduleSwitch,
  } from '@/api/modules/taskCenter/project';
  import {
    getSystemScheduleList,
    systemBatchCloseTask,
    systemBatchOpenTask,
    systemDeleteSchedule,
    systemEditCron,
    systemScheduleSwitch,
  } from '@/api/modules/taskCenter/system';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';
  import { useAppStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { TaskCenterSystemTaskItem } from '@/models/taskCenter';
  import { MenuEnum } from '@/enums/commonEnum';
  import { ApiTestRouteEnum, ProjectManagementRouteEnum, TestPlanRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { SystemTaskType } from '@/enums/taskCenter';

  import { scheduleTaskTypeMap } from './config';

  const props = defineProps<{
    type: 'system' | 'project' | 'org';
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();
  const { openNewPage } = useOpenNewPage();
  const tableStore = useTableStore();
  const appStore = useAppStore();

  const keyword = ref('');
  const batchModalParams = ref();
  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      width: 100,
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
      title: 'common.status',
      dataIndex: 'status',
      slotName: 'status',
      width: 50,
    },
    {
      title: 'ms.taskCenter.type',
      dataIndex: 'resourceType',
      slotName: 'resourceType',
      width: 120,
    },
    {
      title: 'ms.taskCenter.runRule',
      slotName: 'runRule',
      dataIndex: 'value',
      width: 220,
    },
    {
      title: 'ms.taskCenter.operationUser',
      dataIndex: 'createUserName',
      width: 150,
      showTooltip: true,
    },
    {
      title: 'ms.taskCenter.operationTime',
      dataIndex: 'createTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'ms.taskCenter.lastFinishTime',
      dataIndex: 'lastFinishTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'ms.taskCenter.nextExecuteTime',
      dataIndex: 'nextTime',
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
      width: 110,
    },
  ];
  if (props.type === 'system') {
    columns.splice(1, 0, [
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
    columns.splice(1, 0, [
      {
        title: 'common.belongProject',
        dataIndex: 'belongProject',
        showTooltip: true,
        width: 100,
      },
    ]);
  }

  await tableStore.initColumn(TableKeyEnum.TASK_CENTER_SYSTEM_TASK, columns, 'drawer');

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.open',
        eventTag: 'open',
      },
      {
        label: 'common.close',
        eventTag: 'close',
      },
    ],
  };

  const currentScheduleList = {
    system: getSystemScheduleList,
    org: getOrganizationScheduleList,
    project: getProjectScheduleList,
  }[props.type];

  const { propsRes, propsEvent, loadList, setLoadListParams, getTableQueryParams, resetSelector } = useTable(
    currentScheduleList,
    {
      tableKey: TableKeyEnum.TASK_CENTER_SYSTEM_TASK,
      scroll: { x: '100%' },
      selectable: true,
      heightUsed: 288,
      showSetting: true,
      size: 'default',
    },
    (item) => {
      return {
        ...item,
        runRuleLoading: false,
        createTime: item.createTime ? dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') : '-',
        lastFinishTime: item.lastFinishTime ? dayjs(item.lastFinishTime).format('YYYY-MM-DD HH:mm:ss') : '-',
        nextTime: item.nextTime ? dayjs(item.nextTime).format('YYYY-MM-DD HH:mm:ss') : '-',
      };
    }
  );

  function getCurrentPermission(action: 'DELETE' | 'EDIT') {
    return {
      system: {
        DELETE: 'SYSTEM_SCHEDULE_TASK_CENTER:READ+DELETE',
        EDIT: 'SYSTEM_SCHEDULE_TASK_CENTER:READ+UPDATE',
      },
      org: {
        DELETE: 'ORGANIZATION_SCHEDULE_TASK_CENTER:READ+DELETE',
        EDIT: 'ORGANIZATION_SCHEDULE_TASK_CENTER:READ+UPDATE',
      },
      project: {
        DELETE: 'PROJECT_SCHEDULE_TASK_CENTER:READ+DELETE',
        EDIT: 'PROJECT_SCHEDULE_TASK_CENTER:READ+UPDATE',
      },
    }[props.type][action];
  }

  function searchTask() {
    setLoadListParams({ keyword: keyword.value });
    loadList();
  }

  const currentDeleteSchedule = {
    system: systemDeleteSchedule,
    org: organizationDeleteSchedule,
    project: projectDeleteSchedule,
  }[props.type];

  /**
   * 删除任务
   */
  function deleteTask(record: TaskCenterSystemTaskItem) {
    const title = t('ms.taskCenter.deleteTaskTitle', { name: characterLimit(record?.taskName) });
    openModal({
      type: 'error',
      title,
      content: t('ms.taskCenter.deleteTimeTaskTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await currentDeleteSchedule(record?.id || '');
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

  const currentBatchOpenSchedule = {
    system: systemBatchOpenTask,
    org: organizationBatchOpenTask,
    project: projectBatchOpenTask,
  }[props.type];
  async function openTask(params?: BatchActionQueryParams) {
    try {
      await currentBatchOpenSchedule({
        selectIds: params?.selectIds || [],
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
        ...getTableQueryParams(),
      });
      Message.success(t('ms.taskCenter.openTaskSuccess'));
      resetSelector();
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const currentBatchCloseSchedule = {
    system: systemBatchCloseTask,
    org: organizationBatchCloseTask,
    project: projectBatchCloseTask,
  }[props.type];
  async function closeTask(params?: BatchActionQueryParams) {
    try {
      await currentBatchCloseSchedule({
        selectIds: params?.selectIds || [],
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
        ...getTableQueryParams(),
      });
      Message.success(t('ms.taskCenter.closeTaskSuccess'));
      resetSelector();
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function checkDetail(record: TaskCenterSystemTaskItem) {
    switch (record.resourceType) {
      case SystemTaskType.API_IMPORT:
        openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
          taskDrawer: true,
        });
        break;
      case SystemTaskType.TEST_PLAN:
        openNewPage(TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL, {
          id: record.resourceId,
        });
        break;
      case SystemTaskType.API_SCENARIO:
        openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, {
          id: record.resourceId,
        });
        break;
      case SystemTaskType.BUG_SYNC:
        openNewPage(ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, {
          module: MenuEnum.bugManagement,
        });
        break;
      case SystemTaskType.DEMAND_SYNC:
        openNewPage(ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, {
          module: MenuEnum.caseManagement,
        });
        break;
      default:
        break;
    }
  }

  const currentSwitchSchedule = {
    system: systemScheduleSwitch,
    org: organizationScheduleSwitch,
    project: projectScheduleSwitch,
  }[props.type];
  /**
   * 启用/关闭任务
   */
  async function handleBeforeEnableChange(record: TaskCenterSystemTaskItem) {
    try {
      await currentSwitchSchedule(record.id);
      Message.success(t(record.enable ? 'ms.taskCenter.closeTaskSuccess' : 'ms.taskCenter.openTaskSuccess'));
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchModalParams.value = params;
    switch (event.eventTag) {
      case 'open':
        openTask(params);
        break;
      case 'close':
        closeTask(params);
        break;
      default:
        break;
    }
  }

  const currentEditCron = {
    system: systemEditCron,
    org: organizationEditCron,
    project: projectEditCron,
  }[props.type];
  async function handleRunRuleChange(
    val: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[],
    record: TaskCenterSystemTaskItem
  ) {
    try {
      record.runRuleLoading = true;
      await currentEditCron(val as string, record.id);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      record.runRuleLoading = false;
    }
  }

  onMounted(() => {
    loadList();
  });

  watch(
    () => appStore.currentProjectId,
    () => {
      searchTask();
    }
  );
</script>

<style lang="less" scoped></style>
