<template>
  <MsAdvanceFilter
    v-model:keyword="keyword"
    :filter-config-list="filterConfigList"
    :custom-fields-config-list="searchCustomFields"
    :row-count="filterRowCount"
    :search-placeholder="t('common.searchByNameAndId')"
    @keyword-search="fetchData"
    @adv-search="fetchData"
    @refresh="fetchData"
  >
    <template #left>
      <a-radio-group v-model="showType" type="button" class="file-show-type mr-2">
        <a-radio :value="testPlanTypeEnum.ALL" class="show-type-icon p-[2px]">{{
          t('testPlan.testPlanIndex.all')
        }}</a-radio>
        <a-radio :value="testPlanTypeEnum.TEST_PLAN" class="show-type-icon p-[2px]">{{
          t('testPlan.testPlanIndex.testPlan')
        }}</a-radio>
        <!-- <a-radio value="testPlanGroup" class="show-type-icon p-[2px]">{{
            t('testPlan.testPlanIndex.testPlanGroup')
          }}</a-radio> -->
      </a-radio-group>
    </template>
  </MsAdvanceFilter>
  <MsBaseTable
    v-bind="propsRes"
    ref="tableRef"
    class="mt-4"
    :action-config="tableBatchActions"
    :expanded-keys="expandedKeys"
    filter-icon-align-left
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
    <template #num="{ record }">
      <div class="flex items-center">
        <div v-if="record.childrenCount" class="mr-2 flex items-center" @click="expandHandler(record)">
          <MsIcon
            type="icon-icon_split-turn-down-left"
            class="arrowIcon mr-1 text-[16px]"
            :class="getIconClass(record)"
          />
          <span :class="getIconClass(record)">{{ record.childrenCount }}</span>
        </div>
        <div
          :class="[record.childrenCount ? 'pl-0' : 'pl-[36px]']"
          class="one-line-text text-[rgb(var(--primary-5))]"
          >{{ record.num }}</div
        >
        <a-tooltip position="right" :disabled="!record.schedule" :mouse-enter-delay="300">
          <MsTag v-if="record.schedule" size="small" type="link" theme="outline" class="ml-2">{{
            t('testPlan.testPlanIndex.timing')
          }}</MsTag>
          <template #content>
            <div>
              <div>{{ t('testPlan.testPlanIndex.scheduledTaskOpened') }}</div>
              <div>{{ t('testPlan.testPlanIndex.nextExecutionTime') }}</div>
              <!-- TODO 缺少字段 -->
              <div>---</div>
            </div>
            <!-- TODO 缺少字段 -->
            <!-- <div> {{ t('testPlan.testPlanIndex.scheduledTaskUnEnable') }} </div> -->
          </template>
        </a-tooltip>
      </div>
    </template>
    <template #statusFilter="{ columnConfig }">
      <a-trigger v-model:popup-visible="statusFilterVisible" trigger="click" @popup-visible-change="handleFilterHidden">
        <a-button type="text" class="arco-btn-text--secondary" @click="statusFilterVisible = true">
          {{ t(columnConfig.title as string) }}
          <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
        </a-button>
        <template #content>
          <div class="arco-table-filters-content">
            <div class="flex items-center justify-center px-[6px] py-[2px]">
              <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                <a-checkbox v-for="key of Object.keys(reviewStatusMap)" :key="key" :value="key">
                  <a-tag
                    :color="reviewStatusMap[key as ReviewStatus].color"
                    :class="[reviewStatusMap[key as ReviewStatus].class, 'px-[4px]']"
                    size="small"
                  >
                    {{ t(reviewStatusMap[key as ReviewStatus].label) }}
                  </a-tag>
                </a-checkbox>
              </a-checkbox-group>
            </div>
            <div class="filter-button">
              <a-button size="mini" class="mr-[8px]" @click="resetStatusFilter">
                {{ t('common.reset') }}
              </a-button>
              <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                {{ t('system.orgTemplate.confirm') }}
              </a-button>
            </div>
          </div>
        </template>
      </a-trigger>
    </template>

    <template #status="{ record }">
      <statusTag :status="record.status" />
    </template>

    <template #passRate="{ record }">
      <div class="mr-[8px] w-[100px]">
        <StatusProgress :status-detail="record.statusDetail" height="5px" />
      </div>
      <div class="text-[var(--color-text-1)]">
        {{ `${record.passRate || 0}%` }}
      </div>
    </template>
    <template #passRateTitleSlot="{ columnConfig }">
      <div class="flex items-center text-[var(--color-text-3)]">
        {{ t(columnConfig.title as string) }}
        <a-tooltip position="right">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
          <template #content>
            <!-- TODO 需要提供文案 -->
            <!-- <div>{{ t('apiTestDebug.encodeTip1') }}</div>
            <div>{{ t('apiTestDebug.encodeTip2') }}</div> -->
          </template>
        </a-tooltip>
      </div>
    </template>
    <template #useCount="{ record }">
      <a-popover position="bottom" content-class="p-[16px]" trigger="click">
        <div>{{ record.useCaseCount.caseCount }}</div>
        <template #content>
          <table class="min-w-[144px]">
            <tr>
              <td class="popover-label-td">
                <div>{{ t('project.testPlanIndex.TotalCases') }}</div>
              </td>
              <td class="popover-value-td">
                {{ record.useCaseCount.caseCount }}
              </td>
            </tr>
            <tr>
              <td class="popover-label-td">
                <div class="text-[var(--color-text-1)]">{{ t('project.testPlanIndex.functionalUseCase') }}</div>
              </td>
              <td class="popover-value-td">
                {{ record.useCaseCount.caseCount }}
              </td>
            </tr>
            <tr>
              <td class="popover-label-td">
                <div class="text-[var(--color-text-1)]">{{ t('project.testPlanIndex.apiCase') }}</div>
              </td>
              <td class="popover-value-td">
                {{ record.useCaseCount.caseCount }}
              </td>
            </tr>
            <tr>
              <td class="popover-label-td">
                <div class="text-[var(--color-text-1)]">{{ t('project.testPlanIndex.apiScenarioCase') }}</div>
              </td>
              <td class="popover-value-td">
                {{ record.useCaseCount.caseCount }}
              </td>
            </tr>
          </table>
        </template>
      </a-popover>
    </template>

    <template #operation="{ record }">
      <div class="flex items-center">
        <MsButton class="!mx-0">{{ t('testPlan.testPlanIndex.execution') }}</MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>

        <MsButton v-permission="['PROJECT_TEST_PLAN:READ+UPDATE']" class="!mx-0">{{ t('common.edit') }}</MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>

        <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect($event, record)" />
      </div>
    </template>
  </MsBaseTable>
  <a-modal
    v-model:visible="executeVisible"
    class="ms-modal-form ms-modal-small ms-modal-response-body"
    unmount-on-close
    title-align="start"
    :mask="true"
    :mask-closable="false"
    @close="cancelHandler"
  >
    <template #title>
      {{ t('testPlan.testPlanIndex.batchExecution') }}
    </template>
    <a-radio-group>
      <a-radio value="serial">{{ t('testPlan.testPlanIndex.serial') }}</a-radio>
      <a-radio value="parallel">{{ t('testPlan.testPlanIndex.parallel') }}</a-radio>
    </a-radio-group>
    <template #footer>
      <div class="flex justify-end">
        <a-button type="secondary" @click="cancelHandler">
          {{ t('common.cancel') }}
        </a-button>
        <a-button class="ml-3" type="primary" :loading="confirmLoading" @click="executeHandler">
          {{ t('common.execute') }}
        </a-button>
      </div>
    </template>
  </a-modal>
  <BatchMoveOrCopy
    v-model:visible="showBatchModal"
    v-model:selected-node-keys="selectNodeKeys"
    :mode="modeType"
    :current-select-count="batchParams.currentSelectCount || 0"
    :get-module-tree-api="getTestPlanModule"
    :ok-loading="okLoading"
    @save="handleMoveOrCopy"
  />
  <ScheduledModal v-model:visible="showScheduledTaskModal" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import BatchMoveOrCopy from './batchMoveOrCopy.vue';
  import ScheduledModal from './scheduledModal.vue';
  import StatusProgress from './statusProgress.vue';
  import statusTag from '@/views/case-management/caseReview/components/statusTag.vue';

  import { getTestPlanList, getTestPlanModule } from '@/api/modules/test-plan/testPlan';
  import { reviewStatusMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';

  import { ReviewStatus } from '@/models/caseManagement/caseReview';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  const tableStore = useTableStore();
  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const props = defineProps<{
    activeFolder: string;
    activeFolderType: 'folder' | 'module';
    offspringIds: string[]; // 当前选中文件夹的所有子孙节点id
    modulesCount: Record<string, number>; // 模块数量
  }>();

  const emit = defineEmits<{
    (e: 'init', params: any): void;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'testPlan.testPlanIndex.ID',
      slotName: 'num',
      dataIndex: 'num',
      width: 200,
      showInTable: true,
      showDrag: false,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'testPlan.testPlanIndex.testPlanName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 180,
      editType: ColumnEditTypeEnum.INPUT,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: false,
      columnSelectorDisabled: true,
    },
    {
      title: 'testPlan.testPlanIndex.desc',
      slotName: 'desc',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.executionResult',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      showInTable: true,
      showDrag: true,
      width: 150,
    },
    {
      title: 'common.creator',
      slotName: 'createUser',
      dataIndex: 'createUser',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.passRate',
      dataIndex: 'passRate',
      slotName: 'passRate',
      titleSlotName: 'passRateTitleSlot',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.useCount',
      slotName: 'useCount',
      dataIndex: 'useCount',
      showTooltip: true,
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.tag',
      slotName: 'tags',
      dataIndex: 'tags',
      showInTable: true,
      isTag: true,
      width: 300,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.belongModule',
      slotName: 'moduleName',
      dataIndex: 'moduleName',
      showInTable: true,
      showDrag: true,
      width: 200,
    },
    {
      title: 'testPlan.testPlanIndex.createTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.planStartToEndTime',
      slotName: 'planStartToEndTime',
      dataIndex: 'planStartToEndTime',
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.actualStartToEndTime',
      slotName: 'actualStartToEndTime',
      dataIndex: 'actualStartToEndTime',
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 200,
      showInTable: true,
      showDrag: false,
    },
  ];

  /**
   * 更新测试计划名称
   */
  async function updatePlanName() {
    try {
      Message.success(t('common.updateSuccess'));
      return Promise.resolve(true);
    } catch (error) {
      console.log(error);
      return Promise.resolve(false);
    }
  }

  const keyword = ref<string>('');
  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>([]);

  const tableBatchActions = {
    baseAction: [
      {
        label: 'testPlan.testPlanIndex.execute',
        eventTag: 'execute',
        permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
      },
      {
        label: 'common.copy',
        eventTag: 'copy',
        permission: ['PROJECT_TEST_PLAN:READ+ADD'],
      },
      // {
      //   label: 'common.export',
      //   eventTag: 'export',
      // },
      {
        label: 'common.move',
        eventTag: 'move',
        permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
      },
    ],
    moreAction: [
      {
        label: 'testPlan.testPlanIndex.openTimingTask',
        eventTag: 'openTimingTask',
        permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
      },
      {
        label: 'testPlan.testPlanIndex.closeTimingTask',
        eventTag: 'closeTimingTask',
        permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
      },
      {
        label: 'common.archive',
        eventTag: 'archive',
        permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
      },
      {
        isDivider: true,
      },
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['PROJECT_TEST_PLAN:READ+DELETE'],
      },
    ],
  };

  const moreActions: ActionsItem[] = [
    {
      label: 'common.copy',
      eventTag: 'copy',
    },
    {
      label: 'testPlan.testPlanIndex.createScheduledTask',
      eventTag: 'createScheduledTask',
    },
    {
      label: 'testPlan.testPlanIndex.configuration',
      eventTag: 'config',
    },
    {
      label: 'common.archive',
      eventTag: 'archive',
    },
    {
      isDivider: true,
    },
    {
      label: 'common.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];
  // TODO 临时数据
  const data = [
    {
      id: '100944',
      projectId: 'string',
      num: '100944',
      name: '系统示例',
      status: 'PREPARED',
      tags: ['string'],
      schedule: 'string',
      createUser: 'string',
      createTime: 'string',
      moduleName: 'string',
      moduleId: 'string',
      passCount: 0,
      unPassCount: 0,
      reviewedCount: 0,
      underReviewedCount: 0,
      childrenCount: 2,
      statusDetail: {
        tolerance: 100,
        UNPENDING: 100,
        RUNNING: 30,
        SUCCESS: 30,
        ERROR: 30,
        executionProgress: '100%',
      },
      useCaseCount: {
        caseCount: 3,
        apiCount: 3,
        scenarioCount: 3,
      },
      children: [
        {
          id: '100945',
          projectId: 'string',
          num: '100945',
          name: '系统示例',
          status: 'COMPLETED',
          tags: ['string'],
          schedule: 'string',
          createUser: 'string',
          createTime: 'string',
          moduleName: 'string',
          moduleId: 'string',
          testPlanItem: [],
          testPlanGroupId: 'string',
          passCount: 0,
          unPassCount: 0,
          reviewedCount: 0,
          underReviewedCount: 0,
          childrenCount: 0,
          useCaseCount: {
            caseCount: 3,
            apiCount: 3,
            scenarioCount: 3,
          },
          statusDetail: {
            tolerance: 100,
            UNPENDING: 100,
            RUNNING: 30,
            SUCCESS: 30,
            ERROR: 30,
            executionProgress: '100%',
          },
        },
        {
          id: '100955',
          projectId: 'string',
          num: '100955',
          name: '系统示例',
          status: 'COMPLETED',
          tags: ['string'],
          schedule: 'string',
          createUser: 'string',
          createTime: 'string',
          moduleName: 'string',
          moduleId: 'string',
          testPlanItem: [],
          testPlanGroupId: 'string',
          passCount: 0,
          unPassCount: 0,
          reviewedCount: 0,
          underReviewedCount: 0,
          childrenCount: 0,
          useCaseCount: {
            caseCount: 3,
            apiCount: 3,
            scenarioCount: 3,
          },
          statusDetail: {
            tolerance: 100,
            UNPENDING: 100,
            RUNNING: 30,
            SUCCESS: 30,
            ERROR: 30,
            executionProgress: '100%',
          },
        },
      ],
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setProps } = useTable(
    getTestPlanList,
    {
      tableKey: TableKeyEnum.TEST_PLAN_ALL_TABLE,
      selectable: true,
      showSetting: true,
      heightUsed: 128,
      paginationSize: 'mini',
    },
    (item) => {
      return {
        ...item,
        tags: (item.tags || []).map((e: string) => ({ id: e, name: e })),
      };
    }
  );

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  const conditionParams = ref({
    keyword: '',
    filter: {},
    combine: {},
  });

  const showType = ref<keyof typeof testPlanTypeEnum>('ALL');

  async function initTableParams() {
    conditionParams.value = {
      keyword: keyword.value,
      filter: propsRes.value.filter,
      combine: batchParams.value.condition,
    };

    return {
      type: showType.value,
      moduleIds: props.activeFolder && props.activeFolder !== 'all' ? [props.activeFolder, ...props.offspringIds] : [],
      projectId: appStore.currentProjectId,
      excludeIds: batchParams.value.excludeIds || [],
      selectAll: !!batchParams.value?.selectAll,
      selectIds: batchParams.value.selectedIds || [],
      keyword: keyword.value,
      filter: {},
      condition: {
        keyword: keyword.value,
      },
      combine: {
        ...batchParams.value.condition,
      },
    };
  }

  async function fetchData() {
    resetSelector();
    setLoadListParams(await initTableParams());
    loadList();
    const tableParams = await initTableParams();
    emit('init', {
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }

  /**
   * 批量执行
   */
  const executeVisible = ref<boolean>(false);
  function handleExecute() {
    executeVisible.value = true;
  }

  function cancelHandler() {
    executeVisible.value = false;
  }

  const confirmLoading = ref<boolean>(false);

  /**
   * 执行
   */
  function executeHandler() {}

  /**
   * 批量复制或者移动
   */
  const modeType = ref<'move' | 'copy'>('move');
  const showBatchModal = ref<boolean>(false);
  const selectNodeKeys = ref<(string | number)[]>([]);
  const okLoading = ref<boolean>(false);
  function handleCopyOrMove(type: 'move' | 'copy') {
    modeType.value = type;
    selectNodeKeys.value = [];
    showBatchModal.value = true;
  }

  /**
   * 批量移动或复制保存
   */
  async function handleMoveOrCopy() {
    okLoading.value = true;
    try {
      const params = {
        selectIds: batchParams.value.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: {
          keyword: keyword.value,
          filter: {
            reviewStatus: statusFilters.value,
          },
          combine: batchParams.value.condition,
        },
        projectId: appStore.currentProjectId,
        moduleIds: [...selectNodeKeys.value],
      };
      if (modeType.value === 'copy') {
        Message.success(t('common.batchCopySuccess'));
      } else {
        Message.success(t('common.batchMoveSuccess'));
      }
    } catch (error) {
      console.log(error);
    } finally {
      okLoading.value = false;
    }
  }

  /**
   * 打开关闭定时任务
   */
  function handleStatusTimingTask(status: boolean) {}

  /**
   * 归档
   */
  function handleArchive() {
    openModal({
      type: 'warning',
      title: t('testPlan.testPlanIndex.confirmBatchArchivePlan', {
        count: batchParams.value.currentSelectCount,
      }),
      content: t('testPlan.testPlanIndex.confirmBatchDeletePlanContentTip'),
      okText: t('common.archive'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {
        try {
          const { selectedIds, selectAll, excludeIds } = batchParams.value;
          Message.success(t('common.deleteSuccess'));
          fetchData();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }
  /**
   * 归档
   */
  function handleDelete() {
    openModal({
      type: 'error',
      title: t('testPlan.testPlanIndex.confirmBatchDeletePlan', {
        count: batchParams.value.currentSelectCount,
      }),
      content: t('testPlan.testPlanIndex.confirmBatchDeletePlanContentTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const { selectedIds, selectAll, excludeIds } = batchParams.value;
          Message.success(t('common.deleteSuccess'));
          fetchData();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 批量操作
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = params;
    switch (event.eventTag) {
      case 'execute':
        handleExecute();
        break;
      case 'copy':
        handleCopyOrMove('copy');
        break;
      case 'move':
        handleCopyOrMove('move');
        break;
      case 'openTimingTask':
        handleStatusTimingTask(true);
        break;
      case 'closeTimingTask':
        handleStatusTimingTask(false);
        break;
      case 'archive':
        handleArchive();
        break;
      case 'delete':
        handleDelete();
        break;

      default:
        break;
    }
  }

  function deletePlan(record: any) {}

  function copyHandler() {}

  const showScheduledTaskModal = ref<boolean>(false);
  function handleScheduledTask() {
    showScheduledTaskModal.value = true;
  }

  function handleMoreActionSelect(item: ActionsItem, record: any) {
    switch (item.eventTag) {
      case 'copy':
        copyHandler();
        break;
      case 'createScheduledTask':
        handleScheduledTask();
        break;
      default:
        break;
    }
  }

  const expandedKeys = ref<string[]>([]);

  function expandHandler(record: any) {
    if (expandedKeys.value.includes(record.id)) {
      expandedKeys.value = expandedKeys.value.filter((key) => key !== record.id);
    } else {
      expandedKeys.value = [...expandedKeys.value, record.id];
    }
  }

  function getIconClass(record: any) {
    return expandedKeys.value.includes(record.id) ? 'text-[rgb(var(--primary-5))]' : 'text-[var(--color-text-4)]';
  }

  function handleFilterHidden(val: boolean) {
    if (!val) {
      statusFilterVisible.value = false;
      fetchData();
    }
  }

  function resetStatusFilter() {
    statusFilterVisible.value = false;
    statusFilters.value = [];
    fetchData();
  }

  /** *
   * 高级检索
   */
  const filterConfigList = ref<FilterFormItem[]>([]);
  const searchCustomFields = ref<FilterFormItem[]>([]);
  const filterRowCount = ref(0);

  watch(
    () => showType.value,
    (val) => {
      if (val) {
        fetchData();
      }
    }
  );

  watch(
    () => props.activeFolder,
    (val) => {
      if (val) {
        fetchData();
      }
    }
  );

  // TODO 临时数据模拟
  // onMounted(() => {
  //   setProps({ data });
  // });

  onBeforeMount(() => {
    fetchData();
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_ALL_TABLE, columns, 'drawer');
</script>

<style scoped lang="less">
  :deep(.arco-table-cell-expand-icon .arco-table-cell-inline-icon) {
    display: none;
  }
  :deep(.arco-table-cell-align-left) > span:first-child {
    padding-left: 0 !important;
  }
  .arrowIcon {
    transform: scaleX(-1);
  }
  :deep(.ms-modal-form .arco-modal-body) {
    padding: 0 !important;
  }
  .popover-label-td {
    @apply flex items-center;

    padding: 8px 8px 0 0;
    color: var(--color-text-4);
  }
  .popover-value-td {
    @apply font-medium;

    padding-top: 8px;
    color: var(--color-text-1);
  }
</style>
