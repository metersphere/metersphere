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
    ref="tableRef"
    v-bind="propsRes"
    :action-config="tableBatchActions"
    no-disable
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
    <template #num="{ record }">
      <a-button type="text" class="max-w-full justify-start px-0" @click="checkDetail(record)">
        <a-tooltip :content="record.num">
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
        :disabled="!hasAnyPermission([getCurrentPermission('EDIT')])"
      ></a-switch>
    </template>
    <template #resourceType="{ record }">
      {{ t(scheduleTaskTypeMap[record.resourceType]) }}
    </template>
    <template #[FilterSlotNameEnum.GLOBAL_TASK_CENTER_SYSTEM_TASK_TYPE]="{ filterContent }">
      {{ t(scheduleTaskTypeMap[Array.isArray(filterContent.value) ? filterContent.value[0] : filterContent.value]) }}
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
        v-if="['API_IMPORT', 'TEST_PLAN', 'TEST_PLAN_GROUP', 'API_SCENARIO'].includes(record.resourceType)"
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
  import type {
    BatchActionConfig,
    BatchActionParams,
    BatchActionQueryParams,
    MsTableColumn,
  } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import {
    getOrganizationScheduleList,
    organizationBatchCloseTask,
    organizationBatchOpenTask,
    organizationDeleteSchedule,
    organizationEditCron,
    organizationProjectOptions,
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
    systemOrgOptions,
    systemProjectOptions,
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
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
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

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  const keyword = ref('');
  const taskTypeOptions: Record<string, any>[] = [];
  const thirdPartyTypeKeys: string[] = [];
  Object.keys(scheduleTaskTypeMap).forEach((key) => {
    if (t(scheduleTaskTypeMap[key]) === t('ms.taskCenter.thirdPartSync')) {
      thirdPartyTypeKeys.push(key);
    }
  });
  Object.keys(scheduleTaskTypeMap).forEach((key) => {
    if (!taskTypeOptions.some((item) => item.label === t(scheduleTaskTypeMap[key]))) {
      taskTypeOptions.push({
        label: t(scheduleTaskTypeMap[key]),
        value: t(scheduleTaskTypeMap[key]) === t('ms.taskCenter.thirdPartSync') ? thirdPartyTypeKeys : key,
      });
    }
  });

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
      title: 'common.status',
      dataIndex: 'status',
      slotName: 'status',
      width: 50,
      showDrag: true,
      filterConfig: {
        options: [
          {
            label: t('common.open'),
            value: 1,
          },
          {
            label: t('common.close'),
            value: 0,
          },
        ],
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_SYSTEM_TASK_STATUS,
      },
    },
    {
      title: 'ms.taskCenter.type',
      dataIndex: 'resourceType',
      slotName: 'resourceType',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: taskTypeOptions,
        filterSlotName: FilterSlotNameEnum.GLOBAL_TASK_CENTER_SYSTEM_TASK_TYPE,
      },
      width: 120,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.runRule',
      slotName: 'runRule',
      dataIndex: 'value',
      width: 220,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.operationUser',
      dataIndex: 'createUserName',
      width: 150,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.operationTime',
      dataIndex: 'createTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.lastFinishTime',
      dataIndex: 'lastTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'ms.taskCenter.nextExecuteTime',
      dataIndex: 'nextTime',
      slotName: 'nextTime',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 110,
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

  await tableStore.initColumn(TableKeyEnum.TASK_CENTER_SYSTEM_TASK, columns, 'drawer');

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

  const tableBatchActions: BatchActionConfig = {
    baseAction: [
      {
        label: 'common.open',
        eventTag: 'open',
        anyPermission: [getCurrentPermission('EDIT')],
      },
      {
        label: 'common.close',
        eventTag: 'close',
        anyPermission: [getCurrentPermission('EDIT')],
      },
    ],
  };

  const currentScheduleList = {
    system: getSystemScheduleList,
    org: getOrganizationScheduleList,
    project: getProjectScheduleList,
  }[props.type];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    currentScheduleList,
    {
      tableKey: TableKeyEnum.TASK_CENTER_SYSTEM_TASK,
      scroll: { x: '100%', y: '100%' },
      selectable: hasAnyPermission([getCurrentPermission('EDIT')]),
      heightUsed: 288,
      showSetting: true,
      size: 'default',
    },
    (item) => {
      return {
        ...item,
        runRuleLoading: false,
        createTime: item.createTime ? dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') : '-',
        lastTime: item.lastTime && item.lastTime !== -1 ? dayjs(item.lastTime).format('YYYY-MM-DD HH:mm:ss') : '-',
        nextTime: item.nextTime ? dayjs(item.nextTime).format('YYYY-MM-DD HH:mm:ss') : '-',
      };
    }
  );

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

  const conditionParams = computed(() => {
    return {
      keyword: keyword.value,
      filter: propsRes.value.filter,
    };
  });

  const currentBatchOpenSchedule = {
    system: systemBatchOpenTask,
    org: organizationBatchOpenTask,
    project: projectBatchOpenTask,
  }[props.type];
  async function openTask(params?: BatchActionQueryParams) {
    try {
      propsRes.value.loading = true;
      await currentBatchOpenSchedule({
        selectIds: params?.selectedIds || [],
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
        condition: conditionParams.value,
      });
      Message.success(t('ms.taskCenter.openTaskSuccess'));
      resetSelector();
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      propsRes.value.loading = false;
    }
  }

  const currentBatchCloseSchedule = {
    system: systemBatchCloseTask,
    org: organizationBatchCloseTask,
    project: projectBatchCloseTask,
  }[props.type];
  async function closeTask(params?: BatchActionQueryParams) {
    try {
      propsRes.value.loading = true;
      await currentBatchCloseSchedule({
        selectIds: params?.selectedIds || [],
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
        condition: conditionParams.value,
      });
      Message.success(t('ms.taskCenter.closeTaskSuccess'));
      resetSelector();
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      propsRes.value.loading = false;
    }
  }

  function checkDetail(record: TaskCenterSystemTaskItem) {
    switch (record.resourceType) {
      case SystemTaskType.API_IMPORT:
        openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
          orgId: record.organizationId,
          pId: record.projectId,
          taskDrawer: true,
        });
        break;
      case SystemTaskType.TEST_PLAN:
        openNewPage(TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL, {
          orgId: record.organizationId,
          pId: record.projectId,
          id: record.resourceId,
        });
        break;
      case SystemTaskType.TEST_PLAN_GROUP:
        openNewPage(TestPlanRouteEnum.TEST_PLAN_INDEX, {
          orgId: record.organizationId,
          pId: record.projectId,
          groupId: record.resourceNum,
        });
        break;
      case SystemTaskType.API_SCENARIO:
        openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, {
          orgId: record.organizationId,
          pId: record.projectId,
          id: record.resourceId,
        });
        break;
      case SystemTaskType.BUG_SYNC:
        openNewPage(ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, {
          orgId: record.organizationId,
          pId: record.projectId,
          module: MenuEnum.bugManagement,
        });
        break;
      case SystemTaskType.DEMAND_SYNC:
        openNewPage(ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, {
          orgId: record.organizationId,
          pId: record.projectId,
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
      loadList();
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
