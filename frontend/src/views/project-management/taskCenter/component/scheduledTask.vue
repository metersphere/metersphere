<template>
  <div class="p-4 pt-0">
    <a-radio-group
      v-if="props.moduleType === 'TEST_PLAN'"
      v-model:model-value="showType"
      type="button"
      class="file-show-type"
      @change="changeShowType"
    >
      <a-radio value="All">{{ t('report.all') }}</a-radio>
      <a-radio value="TEST_PLAN">{{ t('project.taskCenter.plan') }}</a-radio>
      <a-radio value="GROUP">{{ t('project.taskCenter.planGroup') }}</a-radio>
    </a-radio-group>
    <div class="mb-4 flex items-center justify-end">
      <!-- TODO这个版本不上 -->
      <!--      <a-button type="primary">
        {{ t('project.taskCenter.createTask') }}
      </a-button>-->
      <div class="flex items-center"></div>
      <div class="items-right flex gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="
            props.moduleType === 'API_IMPORT'
              ? t('apiTestManagement.searchTaskPlaceholder')
              : t('system.organization.searchIndexPlaceholder')
          "
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
          v-if="props.moduleType === TaskCenterEnum.API_SCENARIO"
          type="text"
          class="one-line-text w-full"
          :class="[hasJumpPermission ? 'text-[rgb(var(--primary-5))]' : '']"
          @click="showDetail(record.resourceId)"
          >{{ record.resourceNum }}
        </div>
      </template>
      <template #resourceName="{ record }">
        <div
          v-if="props.moduleType === TaskCenterEnum.API_SCENARIO"
          class="one-line-text max-w-[300px]"
          :class="[hasJumpPermission ? 'text-[rgb(var(--primary-5))]' : '']"
          @click="showDetail(record.resourceId)"
          >{{ record.resourceName }}
        </div>
      </template>
      <template #resourceType="{ record }">
        <div type="text" class="flex w-full">
          {{ t(resourceTypeMap[record.resourceType as ResourceTypeMapKey].label) }}
        </div>
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
      <template #value="{ record }">
        <a-select
          v-model:model-value="record.value"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full min-w-[250px]"
          :disabled="!record.enable || !hasAnyPermission(permissionsMap[props.group][props.moduleType]?.edit)"
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
          :disabled="!hasAnyPermission(permissionsMap[props.group][props.moduleType]?.edit)"
        />
        <a-divider direction="vertical" />
        <MsButton
          class="!mr-0"
          :disabled="!hasAnyPermission(permissionsMap[props.group][props.moduleType]?.edit)"
          @click="delSchedule(record)"
          >{{ t('common.delete') }}
        </MsButton>
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
  import { computed, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import {
    batchDisableScheduleOrgTask,
    batchDisableScheduleProTask,
    batchDisableScheduleSysTask,
    batchEnableScheduleOrgTask,
    batchEnableScheduleProTask,
    batchEnableScheduleSysTask,
    deleteScheduleOrgTask,
    deleteScheduleProTask,
    deleteScheduleSysTask,
    enableScheduleOrgTask,
    enableScheduleProTask,
    enableScheduleSysTask,
    getScheduleOrgApiCaseList,
    getScheduleProApiCaseList,
    getScheduleSysApiCaseList,
    updateRunRules,
    updateRunRulesOrg,
    updateRunRulesPro,
  } from '@/api/modules/project-management/taskCenter';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { TimingTaskCenterApiCaseItem } from '@/models/projectManagement/taskCenter';
  import { RouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import type { ResourceTypeMapKey } from '@/enums/taskCenter';
  import { TaskCenterEnum } from '@/enums/taskCenter';

  import { getOrgColumns, getProjectColumns, Group, resourceTypeMap } from './utils';

  const { openNewPage } = useOpenNewPage();

  const tableStore = useTableStore();
  const { openModal } = useModal();
  const { t } = useI18n();

  const props = defineProps<{
    group: Group;
    moduleType: keyof typeof TaskCenterEnum;
    name: string;
  }>();

  const keyword = ref<string>('');
  type ReportShowType = 'All' | 'TEST_PLAN' | 'GROUP';
  const showType = ref<ReportShowType>('All');
  const syncFrequencyOptions = [
    { label: t('apiTestManagement.timeTaskHour'), value: '0 0 0/1 * * ?' },
    { label: t('apiTestManagement.timeTaskSixHour'), value: '0 0 0/6 * * ?' },
    { label: t('apiTestManagement.timeTaskTwelveHour'), value: '0 0 0/12 * * ?' },
    { label: t('apiTestManagement.timeTaskDay'), value: '0 0 0 * * ?' },
  ];

  const loadRealMap = ref({
    system: {
      list: getScheduleSysApiCaseList,
      enable: enableScheduleSysTask,
      delete: deleteScheduleSysTask,
      edit: updateRunRules,
      batchEnable: batchEnableScheduleSysTask,
      batchDisable: batchDisableScheduleSysTask,
    },
    organization: {
      list: getScheduleOrgApiCaseList,
      enable: enableScheduleOrgTask,
      delete: deleteScheduleOrgTask,
      edit: updateRunRulesOrg,
      batchEnable: batchEnableScheduleOrgTask,
      batchDisable: batchDisableScheduleOrgTask,
    },
    project: {
      list: getScheduleProApiCaseList,
      enable: enableScheduleProTask,
      delete: deleteScheduleProTask,
      edit: updateRunRulesPro,
      batchEnable: batchEnableScheduleProTask,
      batchDisable: batchDisableScheduleProTask,
    },
  });

  const permissionsMap: Record<Group, any> = {
    organization: {
      API_IMPORT: {
        edit: ['ORGANIZATION_TASK_CENTER:READ+STOP', 'PROJECT_API_DEFINITION:READ+IMPORT'],
      },
      API_SCENARIO: {
        edit: ['ORGANIZATION_TASK_CENTER:READ+STOP', 'PROJECT_API_SCENARIO:READ+EXECUTE'],
        jump: ['PROJECT_API_SCENARIO:READ'],
      },
      TEST_PLAN: {
        edit: ['ORGANIZATION_TASK_CENTER:READ+STOP', 'PROJECT_TEST_PLAN:READ+EXECUTE'],
        jump: ['PROJECT_TEST_PLAN:READ'],
      },
    },
    system: {
      API_IMPORT: {
        edit: ['SYSTEM_TASK_CENTER:READ+STOP', 'PROJECT_API_DEFINITION:READ+IMPORT'],
      },
      API_SCENARIO: {
        edit: ['SYSTEM_TASK_CENTER:READ+STOP', 'PROJECT_API_SCENARIO:READ+EXECUTE'],
        jump: ['PROJECT_API_SCENARIO:READ'],
      },
      TEST_PLAN: {
        edit: ['SYSTEM_TASK_CENTER:READ+STOP', 'PROJECT_TEST_PLAN:READ+EXECUTE'],
        jump: ['PROJECT_TEST_PLAN:READ'],
      },
    },
    project: {
      API_IMPORT: {
        edit: ['PROJECT_API_DEFINITION:READ+IMPORT'],
      },
      API_SCENARIO: {
        edit: ['PROJECT_API_SCENARIO:READ+EXECUTE'],
        jump: ['PROJECT_API_SCENARIO:READ'],
      },
      TEST_PLAN: {
        edit: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
        jump: ['PROJECT_TEST_PLAN:READ'],
      },
    },
  };
  const hasOperationPermission = computed(() =>
    hasAnyPermission([...(permissionsMap[props.group][props.moduleType]?.edit || '')])
  );

  const resourceColumns: MsTableColumn = [
    {
      title: 'project.taskCenter.resourceID',
      dataIndex: 'resourceNum',
      slotName: 'resourceNum',
      width: 140,
      showInTable: true,
      showTooltip: true,
      showDrag: false,
      sortIndex: 1,
      columnSelectorDisabled: true,
    },
    {
      title: 'project.taskCenter.resourceName',
      slotName: 'resourceName',
      dataIndex: 'resourceName',
      width: 300,
      showDrag: false,
      showTooltip: true,
      sortIndex: 2,
      columnSelectorDisabled: true,
      showInTable: true,
    },
  ];

  const staticColumns: MsTableColumn = [
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
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      showDrag: false,
      width: 180,
    },
  ];

  const swaggerUrlColumn: MsTableColumn = [
    {
      title: 'project.taskCenter.swaggerUrl',
      slotName: 'swaggerUrl',
      dataIndex: 'swaggerUrl',
      width: 300,
      showDrag: false,
      showTooltip: true,
      columnSelectorDisabled: true,
      showInTable: true,
    },
  ];

  const tableKeyMap: Record<string, any> = {
    system: {
      API_IMPORT: TableKeyEnum.TASK_SCHEDULE_TASK_API_IMPORT_SYSTEM,
      API_SCENARIO: TableKeyEnum.TASK_SCHEDULE_TASK_API_SCENARIO_SYSTEM,
      TEST_PLAN: TableKeyEnum.TASK_SCHEDULE_TASK_TEST_PLAN_SYSTEM,
    },
    organization: {
      API_IMPORT: TableKeyEnum.TASK_SCHEDULE_TASK_API_IMPORT_ORGANIZATION,
      API_SCENARIO: TableKeyEnum.TASK_SCHEDULE_TASK_API_SCENARIO_ORGANIZATION,
      TEST_PLAN: TableKeyEnum.TASK_SCHEDULE_TASK_TEST_PLAN_ORGANIZATION,
    },
    project: {
      API_IMPORT: TableKeyEnum.TASK_SCHEDULE_TASK_API_IMPORT_PROJECT,
      API_SCENARIO: TableKeyEnum.TASK_SCHEDULE_TASK_API_SCENARIO_PROJECT,
      TEST_PLAN: TableKeyEnum.TASK_SCHEDULE_TASK_TEST_PLAN_PROJECT,
    },
  };

  const groupColumnsMap: Record<string, any> = {
    system: {
      API_IMPORT: [
        getOrgColumns(),
        getProjectColumns(tableKeyMap[props.group][props.moduleType]),
        ...resourceColumns,
        ...swaggerUrlColumn,
        ...staticColumns,
      ],

      API_SCENARIO: [
        getOrgColumns(),
        getProjectColumns(tableKeyMap[props.group][props.moduleType]),
        ...resourceColumns,
        ...staticColumns,
      ],
      TEST_PLAN: [
        getOrgColumns(),
        getProjectColumns(tableKeyMap[props.group][props.moduleType]),
        ...resourceColumns,
        ...staticColumns,
      ],
    },
    organization: {
      API_IMPORT: [
        getProjectColumns(tableKeyMap[props.group][props.moduleType]),
        ...resourceColumns,
        ...swaggerUrlColumn,
        ...staticColumns,
      ],
      API_SCENARIO: [
        getProjectColumns(tableKeyMap[props.group][props.moduleType]),
        ...resourceColumns,
        ...staticColumns,
      ],
      TEST_PLAN: [getProjectColumns(tableKeyMap[props.group][props.moduleType]), ...resourceColumns, ...staticColumns],
    },
    project: {
      API_IMPORT: [...resourceColumns, ...swaggerUrlColumn, ...staticColumns],
      API_SCENARIO: [...resourceColumns, ...staticColumns],
      TEST_PLAN: [...resourceColumns, ...staticColumns],
    },
  };

  const typeFilter = computed(() => {
    if (showType.value === 'All') {
      return [];
    }
    return [showType.value];
  });

  const hasJumpPermission = computed(() => hasAnyPermission(permissionsMap[props.group][props.moduleType].jump));

  const { propsRes, propsEvent, loadList, setLoadListParams, setPagination, resetSelector, resetFilterParams } =
    useTable(
      loadRealMap.value[props.group].list,
      {
        tableKey: tableKeyMap[props.group][props.moduleType],
        scroll: {
          x: 1200,
        },
        showSetting: true,
        selectable: hasOperationPermission.value,
        heightUsed: 300,
        enableDrag: false,
        showSelectorAll: true,
      },
      // eslint-disable-next-line no-return-assign
      (item) => ({
        ...item,
        nextTime: item.nextTime ? dayjs(item.nextTime).format('YYYY-MM-DD HH:mm:ss') : null,
      })
    );

  function initData() {
    const filterParams = {
      ...propsRes.value.filter,
    };

    setLoadListParams({
      keyword: keyword.value,
      scheduleTagType: props.moduleType,
      filter: {
        ...(props.moduleType === 'TEST_PLAN'
          ? {
              type: typeFilter.value,
              ...filterParams,
            }
          : { ...filterParams }),
      },
    });
    loadList();
  }

  function changeShowType(val: string | number | boolean) {
    showType.value = val as ReportShowType;
    resetFilterParams();
    resetSelector();
    // 重置分页
    setPagination({
      current: 1,
    });
    initData();
  }

  function searchList() {
    resetSelector();
    initData();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.taskCenter.batchEnable',
        eventTag: 'batchEnable',
        anyPermission: permissionsMap[props.group][props.moduleType]?.edit,
      },
      {
        label: 'project.taskCenter.batchDisable',
        eventTag: 'batchDisable',
        anyPermission: permissionsMap[props.group][props.moduleType]?.edit,
      },
    ],
  };

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
          await loadRealMap.value[props.group].delete(props.moduleType, record?.id as string);
          Message.success(t('project.basicInfo.deleted'));
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
      await loadRealMap.value[props.group].enable(props.moduleType, record?.id as string);
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
      await loadRealMap.value[props.group].edit(props.moduleType, record.id, record.value);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      console.log(error);
    }
  }

  /**
   * 跳转接口用例详情
   */

  function showDetail(id: string) {
    if (!hasJumpPermission.value) {
      return;
    }
    if (props.moduleType === 'API_SCENARIO') {
      openNewPage(RouteEnum.API_TEST_SCENARIO, {
        id,
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

  const batchParams = ref<BatchApiParams>({
    selectIds: [],
    selectAll: false,
    excludeIds: [] as string[],
    condition: {},
  });

  function batchEnableTask() {
    openModal({
      type: 'warning',
      title: t('project.taskCenter.batchEnableTask', { num: batchParams.value.currentSelectCount }),
      content: t('project.taskCenter.batchEnableTaskContent'),
      okText: t('project.taskCenter.confirmEnable'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const { selectIds, selectAll, excludeIds } = batchParams.value;

          await loadRealMap.value[props.group].batchEnable({
            selectIds: selectIds || [],
            selectAll: !!selectAll,
            scheduleTagType: props.moduleType,
            excludeIds,
            condition: {
              keyword: keyword.value,
              filter: {
                ...(props.moduleType === 'TEST_PLAN'
                  ? {
                      type: typeFilter.value,
                      ...propsRes.value.filter,
                    }
                  : { ...propsRes.value.filter }),
              },
            },
          });
          resetSelector();
          Message.success(t('project.taskCenter.enableSuccess'));
          initData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function batchDisableTask() {
    openModal({
      type: 'warning',
      title: t('project.taskCenter.batchDisableTask', { num: batchParams.value.currentSelectCount }),
      content: t('project.taskCenter.batchDisableTaskContent'),
      okText: t('project.taskCenter.confirmDisable'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const { selectIds, selectAll, excludeIds } = batchParams.value;
          await loadRealMap.value[props.group].batchDisable({
            selectIds: selectIds || [],
            selectAll: !!selectAll,
            excludeIds: excludeIds || [],
            scheduleTagType: props.moduleType,
            condition: {
              keyword: keyword.value,
              filter: {
                ...(props.moduleType === 'TEST_PLAN'
                  ? { type: typeFilter.value, ...propsRes.value.filter }
                  : { ...propsRes.value.filter }),
              },
            },
          });
          resetSelector();
          Message.success(t('project.taskCenter.disableSuccess'));
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
    if (event.eventTag === 'batchEnable') {
      batchEnableTask();
    } else if (event.eventTag === 'batchDisable') {
      batchDisableTask();
    }
  }

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

  await tableStore.initColumn(
    tableKeyMap[props.group][props.moduleType],
    groupColumnsMap[props.group][props.moduleType],
    'drawer',
    true
  );

  const tableRef = ref();
  watch(
    () => props.moduleType,
    (val) => {
      if (val) {
        resetFilterParams();
        tableRef.value.initColumn(groupColumnsMap[props.group][props.moduleType]);
      }
    }
  );
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
