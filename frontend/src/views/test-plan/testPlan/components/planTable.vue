<template>
  <MsAdvanceFilter
    v-model:keyword="keyword"
    :filter-config-list="filterConfigList"
    :custom-fields-config-list="searchCustomFields"
    :row-count="filterRowCount"
    :search-placeholder="t('common.searchByIDNameTag')"
    @keyword-search="fetchData"
    @adv-search="fetchData"
    @refresh="fetchData"
  >
    <template #left>
      <div class="flex w-full items-center justify-between">
        <div>
          <a-radio-group v-model="showType" type="button" class="file-show-type mr-2">
            <a-radio :value="testPlanTypeEnum.ALL" class="show-type-icon p-[2px]">{{
              t('testPlan.testPlanIndex.all')
            }}</a-radio>
            <a-radio :value="testPlanTypeEnum.TEST_PLAN" class="show-type-icon p-[2px]">{{
              t('testPlan.testPlanIndex.plan')
            }}</a-radio>
            <a-radio :value="testPlanTypeEnum.GROUP" class="show-type-icon p-[2px]">{{
              t('testPlan.testPlanIndex.testPlanGroup')
            }}</a-radio>
          </a-radio-group>
        </div>
        <div class="mr-[24px]">
          <a-switch v-model="isArchived" size="small" type="line" @change="archivedChangeHandler" />
          <span class="ml-1 text-[var(--color-text-3)]">{{ t('testPlan.testPlanGroup.seeArchived') }}</span>
        </div>
      </div>
    </template>
  </MsAdvanceFilter>
  <MsBaseTable
    v-bind="propsRes"
    ref="tableRef"
    class="mt-4"
    :action-config="testPlanBatchActions"
    :selectable="hasOperationPermission && showType !== testPlanTypeEnum.ALL"
    filter-icon-align-left
    :expanded-keys="expandedKeys"
    :disabled-config="{
      disabledChildren: true,
      parentKey: 'parent',
    }"
    v-on="propsEvent"
    @batch-action="handleTableBatch"
    @filter-change="filterChange"
    @drag-change="handleDragChange"
    @sorter-change="saveSort"
  >
    <!-- TODO: 快捷创建暂时不上 -->
    <!-- <template v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+ADD'])" #quickCreate>
      <a-form
        v-if="showQuickCreateForm"
        ref="quickCreateFormRef"
        :model="quickCreateForm"
        layout="inline"
        size="small"
        class="flex items-center"
      >
        <a-form-item
          field="name"
          :rules="[{ required: true, message: t('project.projectVersion.versionNameRequired') }]"
          no-style
        >
          <a-input
            v-model:model-value="quickCreateForm.name"
            :max-length="255"
            :placeholder="t('testPlan.testPlanGroup.newPlanPlaceHolder')"
            class="w-[262px]"
          />
        </a-form-item>
        <a-form-item no-style>
          <a-button type="outline" size="mini" class="ml-[12px] mr-[8px] px-[8px]" @click="quickCreateConfirm">
            {{ t('common.confirm') }}
          </a-button>
          <a-button type="outline" class="arco-btn-outline--secondary px-[8px]" size="mini" @click="quickCreateCancel">
            {{ t('common.cancel') }}
          </a-button>
        </a-form-item>
      </a-form>
      <MsButton v-if="!showQuickCreateForm && showType !== testPlanTypeEnum.ALL" @click="showQuickCreateForm = true">
        <MsIcon type="icon-icon_add_outlined" size="14" class="mr-[8px]" />
        {{ t('common.newCreate') }}
      </MsButton>
      <a-dropdown position="br" @select="handleSelect">
        <MsButton v-if="!showQuickCreateForm && showType === testPlanTypeEnum.ALL">
          <MsIcon type="icon-icon_add_outlined" size="14" class="mr-[8px]" />
          {{ t('common.newCreate') }}
        </MsButton>
        <template #content>
          <a-doption :value="testPlanTypeEnum.TEST_PLAN">{{ t('testPlan.testPlanIndex.createTestPlan') }}</a-doption>
          <a-doption :value="testPlanTypeEnum.GROUP">{{ t('testPlan.testPlanIndex.createTestPlanGroup') }}</a-doption>
        </template>
      </a-dropdown>
    </template> -->
    <template #num="{ record }">
      <div class="flex items-center">
        <div
          v-if="record.type === testPlanTypeEnum.GROUP"
          class="mr-2 flex items-center"
          @click="expandHandler(record)"
        >
          <MsIcon
            type="icon-icon_split_turn-down_arrow"
            class="arrowIcon mr-1 cursor-pointer text-[16px]"
            :class="getIconClass(record)"
          />
          <span :class="getIconClass(record)">{{ record.childrenCount || 0 }}</span>
        </div>

        <div v-if="record.type === testPlanTypeEnum.TEST_PLAN" :class="`one-line-text ${hasIndent(record)}`">
          <MsButton type="text" @click="openDetail(record.id)"
            ><a-tooltip :content="record.num.toString()"
              ><span>{{ record.num }}</span></a-tooltip
            ></MsButton
          >
        </div>
        <a-tooltip v-else :content="record.num.toString()">
          <div :class="`one-line-text ${hasIndent(record)}`">{{ record.num }}</div>
        </a-tooltip>
        <a-tooltip position="right" :disabled="!getSchedule(record.id)" :mouse-enter-delay="300">
          <MsTag
            v-if="getSchedule(record.id)"
            size="small"
            :type="getScheduleEnable(record.id) ? 'link' : 'default'"
            theme="outline"
            class="ml-2"
            :tooltip-disabled="true"
            @click="handleScheduledTask(record)"
            >{{ t('testPlan.testPlanIndex.timing') }}</MsTag
          >
          <template #content>
            <div v-if="getScheduleEnable(record.id)">
              <div>{{ t('testPlan.testPlanIndex.scheduledTaskOpened') }}</div>
              <div>{{ t('testPlan.testPlanIndex.nextExecutionTime') }}</div>
              <div> {{ dayjs(defaultCountDetailMap[record.id]?.nextTriggerTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
            </div>
            <div v-else> {{ t('testPlan.testPlanIndex.scheduledTaskUnEnable') }} </div>
          </template>
        </a-tooltip>
      </div>
    </template>
    <template #[FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER]="{ filterContent }">
      <MsStatusTag :status="filterContent.value" />
    </template>
    <template #status="{ record }">
      <MsStatusTag :status="record.status" />
    </template>
    <template #createUser="{ record }">
      <a-tooltip :content="`${record.createUserName}`" position="tl">
        <div class="one-line-text">{{ characterLimit(record.createUserName) }}</div>
      </a-tooltip>
    </template>
    <template #createTime="{ record }">
      <a-tooltip :content="`${dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss')}`" position="tl">
        <div class="one-line-text">{{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
      </a-tooltip>
    </template>
    <template #moduleId="{ record }">
      <a-tooltip :content="getModules(record.moduleId, props.moduleTree)" position="top">
        <div class="one-line-text">
          {{ getModules(record.moduleId, props.moduleTree) }}
        </div>
      </a-tooltip>
    </template>
    <template #planStartToEndTime="{ record }">
      <div>
        {{ record.plannedStartTime ? dayjs(record.plannedStartTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        {{ t('common.to') }}
        <a-tooltip
          class="ms-tooltip-red"
          :content="t('testPlan.planStartToEndTimeTip')"
          :disabled="record.execStatus !== LastExecuteResults.ERROR"
        >
          <span :class="[`${record.execStatus === LastExecuteResults.ERROR ? 'text-[rgb(var(--danger-6))' : ''}`]">
            {{ record?.plannedEndTime ? dayjs(record.plannedEndTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </span>
        </a-tooltip>
      </div>
    </template>
    <template #actualStartToEndTime="{ record }">
      {{ record?.actualStartTime ? dayjs(record.actualStartTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
      {{ t('common.to') }} {{ record.actualEndTime ? dayjs(record.actualEndTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
    </template>

    <template #passRate="{ record }">
      <div v-if="record.type === testPlanTypeEnum.TEST_PLAN" class="mr-[8px] w-[100px]">
        <StatusProgress :status-detail="defaultCountDetailMap[record.id]" height="5px" />
      </div>
      <div v-if="record.type === testPlanTypeEnum.TEST_PLAN" class="text-[var(--color-text-1)]">
        {{ `${defaultCountDetailMap[record.id]?.passRate ? defaultCountDetailMap[record.id].passRate : '-'}%` }}
      </div>
      <span v-else> - </span>
    </template>
    <template #passRateTitleSlot="{ columnConfig }">
      <div class="flex items-center text-[var(--color-text-3)]">
        {{ t(columnConfig.title as string) }}
        <a-tooltip position="right" :content="t('testPlan.testPlanIndex.passRateTitleTip')">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </a-tooltip>
      </div>
    </template>
    <template #functionalCaseCount="{ record }">
      <a-popover
        v-if="record.type === testPlanTypeEnum.TEST_PLAN"
        position="bottom"
        content-class="p-[16px]"
        :disabled="getFunctionalCount(record.id) < 1"
      >
        <div v-if="record.type === testPlanTypeEnum.TEST_PLAN">{{ getFunctionalCount(record.id) }}</div>
        <template #content>
          <table class="min-w-[140px] max-w-[176px]">
            <tr>
              <td class="popover-label-td">
                <div>{{ t('testPlan.testPlanIndex.TotalCases') }}</div>
              </td>
              <td class="popover-value-td">
                {{ defaultCountDetailMap[record.id]?.caseTotal ?? '0' }}
              </td>
            </tr>
            <tr>
              <td class="popover-label-td">
                <div class="text-[var(--color-text-1)]">{{ t('testPlan.testPlanIndex.functionalUseCase') }}</div>
              </td>
              <td class="popover-value-td">
                {{ defaultCountDetailMap[record.id]?.functionalCaseCount ?? '0' }}
              </td>
            </tr>
            <tr>
              <td class="popover-label-td">
                <div class="text-[var(--color-text-1)]">{{ t('testPlan.testPlanIndex.apiCase') }}</div>
              </td>
              <td class="popover-value-td">
                {{ defaultCountDetailMap[record.id]?.apiCaseCount ?? '0' }}
              </td>
            </tr>
            <tr>
              <td class="popover-label-td">
                <div class="text-[var(--color-text-1)]">{{ t('testPlan.testPlanIndex.apiScenarioCase') }}</div>
              </td>
              <td class="popover-value-td">
                {{ defaultCountDetailMap[record.id]?.apiScenarioCount ?? '0' }}
              </td>
            </tr>
          </table>
        </template>
      </a-popover>
      <span v-else>-</span>
    </template>

    <template #operation="{ record }">
      <div class="flex items-center">
        <MsButton
          v-if="isShowExecuteButton(record) && hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE'])"
          class="!mx-0"
          @click="executePlan(record)"
          >{{ t('testPlan.testPlanIndex.execution') }}</MsButton
        >
        <a-divider
          v-if="isShowExecuteButton(record) && hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE'])"
          direction="vertical"
          :margin="8"
        ></a-divider>

        <MsButton
          v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) && record.status !== 'ARCHIVED'"
          class="!mx-0"
          @click="emit('edit', record)"
          >{{ t('common.edit') }}</MsButton
        >
        <a-divider
          v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) && record.status !== 'ARCHIVED'"
          direction="vertical"
          :margin="8"
        ></a-divider>

        <MsButton
          v-if="
            !isShowExecuteButton(record) &&
            hasAnyPermission(['PROJECT_TEST_PLAN:READ+ADD']) &&
            record.status !== 'ARCHIVED'
          "
          class="!mx-0"
          @click="copyTestPlanOrGroup(record.id)"
          >{{ t('common.copy') }}</MsButton
        >
        <a-divider
          v-if="
            !isShowExecuteButton(record) &&
            hasAnyPermission(['PROJECT_TEST_PLAN:READ+ADD']) &&
            record.status !== 'ARCHIVED'
          "
          direction="vertical"
          :margin="8"
        ></a-divider>
        <MsTableMoreAction :list="getMoreActions(record)" @select="handleMoreActionSelect($event, record)" />
      </div>
    </template>
    <template v-if="(keyword || '').trim() === ''" #empty>
      <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
        {{ t('common.noData') }}
        <MsButton v-permission="['PROJECT_TEST_PLAN:READ+ADD']" class="ml-[8px]" @click="emit('new', 'testPlan')">
          {{ t('testPlan.testPlanIndex.createTestPlan') }}
        </MsButton>
        {{ t('caseManagement.featureCase.or') }}
        <MsButton v-permission="['PROJECT_TEST_PLAN:READ+ADD']" class="ml-[8px]" @click="emit('new', 'group')">
          {{ t('testPlan.testPlanIndex.createTestPlanGroup') }}
        </MsButton>
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
    <a-radio-group v-model="executeForm.runMode">
      <a-radio value="SERIAL">{{ t('testPlan.testPlanIndex.serial') }}</a-radio>
      <a-radio value="PARALLEL">{{ t('testPlan.testPlanIndex.parallel') }}</a-radio>
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
    :type="showType"
    @save="handleMoveOrCopy"
  />
  <ScheduledModal
    v-model:visible="showScheduledTaskModal"
    :type="planType"
    :source-id="planSourceId"
    :task-config="taskForm"
    @handle-success="fetchData()"
  />
  <ActionModal
    v-model:visible="showStatusDeleteModal"
    :record="activeRecord"
    :schedule-config="defaultCountDetailMap[activeRecord?.id || 'none']?.scheduleConfig"
    @success="fetchData()"
  />
  <BatchEditModal
    v-model:visible="showEditModel"
    :batch-params="batchParams"
    :active-folder="props.activeFolder"
    :offspring-ids="props.offspringIds"
    :condition="conditionParams"
    :show-type="showType"
    @success="successHandler"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type {
    BatchActionParams,
    BatchActionQueryParams,
    MsTableColumn,
    MsTableProps,
  } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';
  import ActionModal from './actionModal.vue';
  import BatchEditModal from './batchEditModal.vue';
  import BatchMoveOrCopy from './batchMoveOrCopy.vue';
  import ScheduledModal from './scheduledModal.vue';
  import StatusProgress from './statusProgress.vue';

  import {
    addTestPlan,
    archivedPlan,
    batchArchivedPlan,
    batchCopyPlan,
    batchDeletePlan,
    batchEditTestPlan,
    batchMovePlan,
    deletePlan,
    deleteScheduleTask,
    dragPlanOnGroup,
    executePlanOrGroup,
    executeSinglePlan,
    getPlanPassRate,
    getTestPlanDetail,
    getTestPlanList,
    getTestPlanModule,
    testPlanAndGroupCopy,
    updateTestPlan,
  } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { DragSortParams, ModuleTreeNode, TableQueryParams } from '@/models/common';
  import type {
    AddTestPlanParams,
    BatchExecutePlan,
    BatchMoveParams,
    CreateTask,
    ExecutePlan,
    moduleForm,
    PassRateCountDetail,
    TestPlanItem,
  } from '@/models/testPlan/testPlan';
  import { LastExecuteResults } from '@/enums/caseEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  import { planStatusOptions } from '../config';
  import { getModules } from '@/views/case-management/caseManagementFeature/components/utils';

  const tableStore = useTableStore();
  const appStore = useAppStore();
  const router = useRouter();
  const route = useRoute();
  const { t } = useI18n();
  const { openModal } = useModal();

  const props = defineProps<{
    activeFolder: string;
    activeFolderType: 'folder' | 'module';
    offspringIds: string[]; // 当前选中文件夹的所有子孙节点id
    modulesCount: Record<string, number>; // 模块数量
    nodeName: string; // 选中模块名称
    moduleTree: ModuleTreeNode[];
  }>();

  const emit = defineEmits<{
    (e: 'init', params: any): void;
    (e: 'edit', record: TestPlanItem): void;
    (e: 'new', type: string): void;
  }>();

  const isArchived = ref<boolean>(false);
  const keyword = ref<string>('');

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE', 'PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN:READ+ADD'])
  );
  const showType = ref<keyof typeof testPlanTypeEnum>(testPlanTypeEnum.ALL);

  const columns: MsTableColumn = [
    {
      title: 'testPlan.testPlanIndex.ID',
      slotName: 'num',
      dataIndex: 'num',
      width: 180,
      showInTable: true,
      showDrag: false,
      columnSelectorDisabled: true,
    },
    {
      title: 'testPlan.testPlanIndex.testPlanName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 180,
      editType: hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: false,
      columnSelectorDisabled: true,
    },
    {
      title: 'common.status',
      dataIndex: 'status',
      slotName: 'status',
      filterConfig: {
        options: planStatusOptions,
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER,
      },
      showInTable: true,
      showDrag: true,
      width: 150,
    },
    {
      title: 'common.creator',
      slotName: 'createUser',
      dataIndex: 'createUserName',
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
      slotName: 'functionalCaseCount',
      dataIndex: 'functionalCaseCount',
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
      slotName: 'moduleId',
      dataIndex: 'moduleId',
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
      showTooltip: true,
    },
    {
      title: 'testPlan.testPlanIndex.planStartToEndTime',
      slotName: 'planStartToEndTime',
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 370,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.actualStartToEndTime',
      slotName: 'actualStartToEndTime',
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 370,
      showDrag: true,
    },
    {
      title: hasOperationPermission.value ? 'testPlan.testPlanIndex.operation' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 200 : 50,
      showInTable: true,
      showDrag: false,
    },
  ];

  /**
   * 更新测试计划以及测试计划组
   */
  async function updatePlanName(record: TestPlanItem) {
    try {
      if (record.id) {
        const detail = await getTestPlanDetail(record.id);
        const params = {
          ...detail,
          name: record.name,
        };
        await updateTestPlan(params);
        Message.success(t('common.updateSuccess'));
        return Promise.resolve(true);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return Promise.resolve(false);
    }
  }

  // 设置对齐缩进
  function hasIndent(record: TestPlanItem) {
    return (showType.value === 'ALL' || showType.value === 'GROUP') &&
      record.type === testPlanTypeEnum.TEST_PLAN &&
      record.groupId &&
      record.groupId !== 'NONE'
      ? 'pl-[36px]'
      : '';
  }

  const batchCopyActions = [
    {
      label: 'common.copy',
      eventTag: 'copy',
      permission: ['PROJECT_TEST_PLAN:READ+ADD'],
    },
  ];
  const baseActions = [
    {
      label: 'testPlan.testPlanIndex.execute',
      eventTag: 'execute',
      permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
    },
    {
      label: 'common.edit',
      eventTag: 'edit',
      permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
    },
    // TODO 批量执行不上这个版本
    // {
    //   label: 'common.export',
    //   eventTag: 'export',
    // },
  ];
  const moreAction = [
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
      label: 'common.move',
      eventTag: 'move',
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
  ];

  const testPlanBatchActions = computed(() => {
    if (showType.value === testPlanTypeEnum.GROUP) {
      return {
        baseAction: baseActions,
        moreAction,
      };
    }
    return {
      baseAction: [...baseActions, ...batchCopyActions],
      moreAction,
    };
  });

  const archiveActions: ActionsItem[] = [
    {
      label: 'common.archive',
      eventTag: 'archive',
      permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
    },
  ];
  const copyActions: ActionsItem[] = [
    {
      label: 'common.copy',
      eventTag: 'copy',
      permission: ['PROJECT_TEST_PLAN:READ+ADD'],
    },
  ];

  const createScheduledActions: ActionsItem[] = [
    {
      label: 'testPlan.testPlanIndex.createScheduledTask',
      eventTag: 'createScheduledTask',
      permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
    },
  ];
  const updateAndDeleteScheduledActions: ActionsItem[] = [
    {
      label: 'testPlan.testPlanIndex.updateScheduledTask',
      eventTag: 'updateScheduledTask',
      permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
    },
    {
      label: 'testPlan.testPlanIndex.deleteScheduledTask',
      eventTag: 'deleteScheduledTask',
      permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
    },
  ];

  const defaultCountDetailMap = ref<Record<string, PassRateCountDetail>>({});
  function getFunctionalCount(id: string) {
    return defaultCountDetailMap.value[id]?.caseTotal ?? 0;
  }
  function getSchedule(id: string) {
    return !!defaultCountDetailMap.value[id]?.scheduleConfig;
  }
  function getScheduleEnable(id: string) {
    return defaultCountDetailMap.value[id].scheduleConfig.enable;
  }

  function isShowExecuteButton(record: TestPlanItem) {
    return (
      ((record.type === testPlanTypeEnum.TEST_PLAN && getFunctionalCount(record.id) > 0) ||
        (record.type === testPlanTypeEnum.GROUP && record.childrenCount)) &&
      record.status !== 'ARCHIVED'
    );
  }

  function getMoreActions(record: TestPlanItem) {
    const { status: planStatus } = record;

    // 有用例数量才可以执行 否则不展示执行
    const copyAction =
      isShowExecuteButton(record) && hasAnyPermission(['PROJECT_TEST_PLAN:READ+ADD']) ? copyActions : [];

    let scheduledTaskAction: ActionsItem[] = [];
    if (planStatus !== 'ARCHIVED' && record.groupId && record.groupId === 'NONE') {
      scheduledTaskAction = getSchedule(record.id) ? updateAndDeleteScheduledActions : createScheduledActions;
    }
    // 计划组下没有计划&计划组内计划不允许单独归档
    const archiveAction =
      (record.type === testPlanTypeEnum.GROUP && record.childrenCount < 1) ||
      (record.type === testPlanTypeEnum.TEST_PLAN && record.groupId && record.groupId !== 'NONE')
        ? []
        : archiveActions;

    // 已归档和已完成不展示归档
    if (planStatus === 'ARCHIVED' || planStatus === 'PREPARED' || planStatus === 'UNDERWAY') {
      return [
        ...copyAction,
        ...scheduledTaskAction,
        {
          label: 'common.delete',
          danger: true,
          eventTag: 'delete',
          permission: ['PROJECT_TEST_PLAN:READ+DELETE'],
        },
      ];
    }
    return [
      ...copyAction,
      ...archiveAction,
      ...scheduledTaskAction,
      {
        isDivider: true,
      },
      {
        label: 'common.delete',
        danger: true,
        eventTag: 'delete',
        permission: ['PROJECT_TEST_PLAN:READ+DELETE'],
      },
    ];
  }

  const tableProps = ref<Partial<MsTableProps<TestPlanItem>>>({
    tableKey: TableKeyEnum.TEST_PLAN_ALL_TABLE,
    selectable: true,
    showSetting: true,
    heightUsed: 236,
    paginationSize: 'mini',
    showSelectorAll: false,
    draggable: { type: 'handle' },
    draggableCondition: true,
  });

  function getTags(record: TestPlanItem) {
    if (record.children && record.children.length) {
      record.children = record.children.map((child: TestPlanItem) => getTags(child));
    }
    return {
      ...record,
      tags: (record.tags || []).map((item: any, i: number) => {
        return {
          id: `${record.id}-${i}`,
          name: item,
        };
      }),
    };
  }

  const {
    propsRes,
    propsEvent,
    loadList,
    setLoadListParams,
    resetSelector,
    resetFilterParams,
    setPagination,
    setLoading,
  } = useTable(
    getTestPlanList,
    tableProps.value,
    (item: any) => {
      return getTags(item);
    },
    updatePlanName
  );

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  const conditionParams = computed(() => {
    return {
      keyword: keyword.value,
      filter: propsRes.value.filter,
      combine: batchParams.value.condition,
    };
  });

  async function initTableParams(isSetDefaultKey = false) {
    let moduleIds =
      props.activeFolder && props.activeFolder !== 'all' ? [props.activeFolder, ...props.offspringIds] : [];
    if (isSetDefaultKey) {
      moduleIds = [];
    }

    const filterParams = {
      ...propsRes.value.filter,
    };
    if (isArchived.value) {
      filterParams.status = ['ARCHIVED'];
    }
    return {
      type: showType.value,
      moduleIds,
      projectId: appStore.currentProjectId,
      excludeIds: batchParams.value.excludeIds || [],
      selectAll: !!batchParams.value?.selectAll,
      selectIds: batchParams.value.selectedIds || [],
      keyword: keyword.value,
      condition: {
        filter: filterParams,
        keyword: keyword.value,
      },
      filter: filterParams,
      combine: {
        ...batchParams.value.condition,
      },
    };
  }

  async function loadPlanList() {
    setLoadListParams(await initTableParams());
    loadList();
  }
  // 排序
  const sort = ref<{ [key: string]: string }>({});
  function saveSort(sortObj: { [key: string]: string }) {
    sort.value = sortObj;
  }

  // 获取父组件模块数量
  async function emitTableParams(isInit = false) {
    const tableParams = await initTableParams(isInit);
    emit('init', {
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
      filter: propsRes.value.filter,
    });
  }

  async function getStatistics(selectedPlanIds: (string | undefined)[]) {
    try {
      const result = await getPlanPassRate(selectedPlanIds);
      result.forEach((item: PassRateCountDetail) => {
        defaultCountDetailMap.value[item.id] = item;
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 测试计划详情
  function openDetail(id: string) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
      query: {
        id,
      },
    });
  }

  async function fetchData() {
    resetSelector();
    await loadPlanList();
    emitTableParams();
  }

  /**
   * 批量执行
   */
  const initExecuteForm: BatchExecutePlan = {
    projectId: appStore.currentProjectId,
    executeIds: [],
    runMode: 'SERIAL',
    executionSource: 'MANUAL',
  };

  const executeForm = ref<BatchExecutePlan>(cloneDeep(initExecuteForm));
  const executeVisible = ref<boolean>(false);

  function handleExecute() {
    executeForm.value.executeIds = batchParams.value.selectedIds || [];
    executeVisible.value = true;
  }

  function cancelHandler() {
    executeVisible.value = false;
    executeForm.value = cloneDeep(initExecuteForm);
  }

  /**
   * 批量执行
   */

  const confirmLoading = ref<boolean>(false);

  async function batchHandleExecute() {
    confirmLoading.value = true;
    try {
      await executePlanOrGroup(executeForm.value);
      cancelHandler();
      Message.success(t('case.detail.execute.success'));
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  const executeId = ref<string>('');
  async function singleExecute(id: string) {
    confirmLoading.value = true;
    executeId.value = id;
    try {
      const params: ExecutePlan = {
        executeId: id,
        runMode: executeForm.value.runMode,
        executionSource: 'MANUAL',
      };
      await executeSinglePlan(params);
      Message.success(t('case.detail.execute.success'));
      fetchData();
      cancelHandler();
    } catch (error) {
      console.log(error);
    } finally {
      executeId.value = '';
      confirmLoading.value = false;
      setLoading(false);
    }
  }

  async function executeHandler() {
    if (executeId.value) {
      singleExecute(executeId.value);
    } else {
      batchHandleExecute();
    }
  }

  // 测试计划详情
  function executePlan(record: TestPlanItem) {
    const { type, id } = record;
    executeId.value = id;
    if (type === testPlanTypeEnum.GROUP) {
      executeVisible.value = true;
      return;
    }
    if (type === testPlanTypeEnum.TEST_PLAN) {
      // 如果都为功能用例直接执行
      if (defaultCountDetailMap.value[id]) {
        const { apiScenarioCount, apiCaseCount } = defaultCountDetailMap.value[id];
        if (!apiScenarioCount && !apiCaseCount) {
          router.push({
            name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
            query: {
              id,
              type: 'featureCase',
            },
          });
        } else {
          setLoading(true);
          singleExecute(record.id);
        }
      }
    }
  }

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
  async function handleMoveOrCopy(moveForm: moduleForm) {
    okLoading.value = true;
    try {
      const params: BatchMoveParams = {
        excludeIds: batchParams.value?.excludeIds || [],
        selectIds: batchParams.value.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        condition: {
          keyword: keyword.value,
          filter: {},
          combine: batchParams.value.condition,
        },
        projectId: appStore.currentProjectId,
        moduleIds: [...selectNodeKeys.value],
        type: showType.value,
        moduleId: selectNodeKeys.value[0],
        targetId: moveForm.moveType === 'MODULE' ? selectNodeKeys.value[0] : moveForm.targetId,
        moveType: moveForm.moveType,
      };
      if (modeType.value === 'copy') {
        await batchCopyPlan(params);
        Message.success(t('common.batchCopySuccess'));
      } else {
        await batchMovePlan(params);
        Message.success(t('common.batchMoveSuccess'));
      }
      showBatchModal.value = false;
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      okLoading.value = false;
    }
  }

  /**
   * 打开关闭定时任务
   */
  async function handleStatusTimingTask(enable: boolean) {
    const filterParams = {
      ...propsRes.value.filter,
    };
    if (isArchived.value) {
      filterParams.status = ['ARCHIVED'];
    }
    try {
      const { selectedIds, selectAll, excludeIds } = batchParams.value;
      const params: TableQueryParams = {
        selectIds: selectedIds || [],
        selectAll: !!selectAll,
        excludeIds: excludeIds || [],
        projectId: appStore.currentProjectId,
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
        condition: {
          filter: filterParams,
          keyword: keyword.value,
        },
        type: showType.value,
        scheduleOpen: enable,
        editColumn: 'SCHEDULE',
      };
      await batchEditTestPlan(params);
      Message.success(
        enable
          ? t('testPlan.testPlanGroup.enableScheduleTaskSuccess')
          : t('testPlan.testPlanGroup.closeScheduleTaskSuccess')
      );
      fetchData();
    } catch (error) {
      console.log(error);
    }
  }

  /**
   * 归档测试计划以及计划组
   */
  function handleArchive() {
    openModal({
      type: 'warning',
      title: t(
        showType.value === testPlanTypeEnum.TEST_PLAN
          ? 'testPlan.testPlanIndex.confirmBatchArchivePlan'
          : 'testPlan.testPlanGroup.batchArchivedGroup',
        {
          count: batchParams.value.currentSelectCount,
        }
      ),
      content: t('testPlan.testPlanIndex.confirmBatchArchivePlanContent'),
      okText: t('common.archive'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {
        try {
          await batchArchivedPlan({
            excludeIds: batchParams.value?.excludeIds || [],
            selectIds: batchParams.value.selectedIds || [],
            selectAll: !!batchParams.value?.selectAll,
            condition: {
              keyword: keyword.value,
              filter: propsRes.value.filter,
              combine: batchParams.value.condition,
            },
            projectId: appStore.currentProjectId,
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
            type: showType.value,
            moduleId: props.activeFolder,
          });
          Message.success(t('common.batchArchiveSuccess'));
          fetchData();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }
  /**
   * 删除测试计划以及计划组
   */
  function handleDelete() {
    openModal({
      type: 'error',
      title: t(
        showType.value === testPlanTypeEnum.GROUP
          ? 'testPlan.testPlanGroup.confirmBatchDeletePlanGroup'
          : 'testPlan.testPlanIndex.confirmBatchDeletePlan',
        {
          count: batchParams.value.currentSelectCount,
        }
      ),
      content: t('testPlan.testPlanIndex.confirmBatchDeletePlanContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const { selectedIds, selectAll, excludeIds } = batchParams.value;
          await batchDeletePlan({
            projectId: appStore.currentProjectId,
            selectIds: selectedIds || [],
            excludeIds: excludeIds || [],
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
            condition: {
              keyword: keyword.value,
              filter: {},
              combine: batchParams.value.condition,
            },
            selectAll: !!selectAll,
            type: showType.value,
          });
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
   * 批量编辑
   */
  const showEditModel = ref<boolean>(false);

  function handleEdit() {
    showEditModel.value = true;
  }

  function successHandler() {
    fetchData();
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
      case 'edit':
        handleEdit();
        break;

      default:
        break;
    }
  }

  const showScheduledTaskModal = ref<boolean>(false);
  const activeRecord = ref<TestPlanItem>();

  const taskForm = ref<CreateTask>();
  const planSourceId = ref<string>();
  const planType = ref<keyof typeof testPlanTypeEnum>(testPlanTypeEnum.TEST_PLAN);
  function handleScheduledTask(record: TestPlanItem) {
    if (!hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE'])) {
      return;
    }
    planType.value = record.type;
    planSourceId.value = record.id;
    taskForm.value = defaultCountDetailMap.value[record.id]?.scheduleConfig;
    showScheduledTaskModal.value = true;
  }

  const showStatusDeleteModal = ref<boolean>(false);

  // 计划组删除: 没有计划直接删除
  async function handleDeleteGroup(record: TestPlanItem) {
    try {
      await deletePlan(record.id);
      fetchData();
      Message.success(t('common.deleteSuccess'));
    } catch (error) {
      console.log(error);
    }
  }

  function deleteStatusHandler(record: TestPlanItem) {
    if (record.type === testPlanTypeEnum.GROUP && !record.childrenCount) {
      handleDeleteGroup(record);
      return;
    }
    activeRecord.value = cloneDeep(record);
    showStatusDeleteModal.value = true;
  }

  // 拖拽排序
  async function handleDragChange(params: DragSortParams) {
    try {
      await dragPlanOnGroup(params);
      Message.success(t('caseManagement.featureCase.sortSuccess'));
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 归档计划以及计划组
  function archiveHandle(record: TestPlanItem) {
    let archiveTitle = t('common.archiveConfirmTitle', { name: characterLimit(record.name) });
    let archiveContent = t('testPlan.testPlanIndex.confirmArchivePlan');
    if (record.type === 'GROUP') {
      archiveTitle = t('testPlan.testPlanGroup.planGroupArchiveTitle', {
        name: characterLimit(record.name),
      });
      archiveContent = t('testPlan.testPlanGroup.planGroupArchiveContent');
    }
    openModal({
      type: 'warning',
      title: archiveTitle,
      content: archiveContent,
      okText: t('common.archive'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {
        try {
          await archivedPlan(record.id);
          Message.success(t('common.batchArchiveSuccess'));
          fetchData();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  async function copyTestPlanOrGroup(id: string) {
    try {
      await testPlanAndGroupCopy(id);
      Message.success(t('common.copySuccess'));
      fetchData();
    } catch (error) {
      console.log(error);
    }
  }

  async function handleDeleteScheduled(record: TestPlanItem) {
    try {
      await deleteScheduleTask(record.id);
      Message.success(t('testPlan.testPlanGroup.deleteScheduleTaskSuccess'));
      fetchData();
    } catch (error) {
      console.log(error);
    }
  }

  function handleMoreActionSelect(item: ActionsItem, record: TestPlanItem) {
    switch (item.eventTag) {
      case 'copy':
        copyTestPlanOrGroup(record.id as string);
        break;
      case 'createScheduledTask':
        handleScheduledTask(record);
        break;
      case 'updateScheduledTask':
        handleScheduledTask(record);
        break;
      case 'deleteScheduledTask':
        handleDeleteScheduled(record);
        break;
      case 'delete':
        deleteStatusHandler(record);
        break;
      case 'archive':
        archiveHandle(record);
        break;
      default:
        break;
    }
  }

  const expandedKeys = ref<string[]>([]);
  // 展开折叠
  function expandHandler(record: TestPlanItem) {
    if (expandedKeys.value.includes(record.id)) {
      expandedKeys.value = expandedKeys.value.filter((key) => key !== record.id);
    } else {
      expandedKeys.value = [...expandedKeys.value, record.id];
      if (record.type === 'GROUP' && record.childrenCount) {
        const testPlanId = record.children.map((item: TestPlanItem) => item.id);
        getStatistics(testPlanId);
      }
    }
  }
  function getIconClass(record: TestPlanItem) {
    return expandedKeys.value.includes(record.id) ? 'text-[rgb(var(--primary-5))]' : 'text-[var(--color-text-4)]';
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
        tableProps.value.draggableCondition =
          hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) && val !== 'TEST_PLAN' && !Object.keys(sort.value).length;
        setPagination({
          current: 1,
        });
        expandedKeys.value = [];
        resetFilterParams();
        fetchData();
      }
    }
  );

  watch(
    () => sort.value,
    (val) => {
      if (val) {
        tableProps.value.draggableCondition =
          hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) &&
          showType.value !== 'GROUP' &&
          !Object.keys(sort.value).length;
      }
    },
    {
      deep: true,
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

  onBeforeMount(() => {
    if (route.query.id) {
      openDetail(route.query.id as string);
    }
    fetchData();
  });

  const planData = computed(() => {
    return propsRes.value.data;
  });

  watch(
    () => planData.value,
    (val) => {
      if (val) {
        const selectedPlanIds: (string | undefined)[] = propsRes.value.data.map((e) => e.id) || [];
        if (selectedPlanIds.length) {
          getStatistics(selectedPlanIds);
        }
      }
    },
    {
      immediate: true,
    }
  );

  function filterChange() {
    emitTableParams();
  }

  const showQuickCreateForm = ref(false);
  const quickCreateFormRef = ref<FormInstance>();

  const initPlanGroupForm: AddTestPlanParams = {
    groupId: 'NONE',
    name: '',
    projectId: appStore.currentProjectId,
    moduleId: '',
    cycle: [],
    tags: [],
    description: '',
    testPlanning: false,
    automaticStatusUpdate: true,
    repeatCase: false,
    passThreshold: 100,
    type: testPlanTypeEnum.GROUP,
    baseAssociateCaseRequest: { selectIds: [], selectAll: false, condition: {} },
  };
  const quickCreateForm = ref<AddTestPlanParams>(cloneDeep(initPlanGroupForm));

  const quickCreateLoading = ref(false);

  function quickCreateCancel() {
    showQuickCreateForm.value = false;
    quickCreateForm.value = cloneDeep(initPlanGroupForm);
    quickCreateFormRef.value?.resetFields();
  }

  /**
   * 快速创建测试计划或者测试计划组
   */
  const createType = ref<keyof typeof testPlanTypeEnum>(showType.value);
  //  TODO: 快捷创建先不上
  function quickCreateConfirm() {
    quickCreateFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          quickCreateLoading.value = true;
          const params = {
            ...cloneDeep(quickCreateForm.value),
            groupId: 'NONE',
            projectId: appStore.currentProjectId,
            moduleId: props.activeFolder === 'all' ? 'root' : props.activeFolder,
            testPlanning: false,
            automaticStatusUpdate: true,
            repeatCase: false,
            passThreshold: 100,
            type: showType.value === testPlanTypeEnum.ALL ? createType.value : showType.value,
          };
          await addTestPlan(params);
          Message.success(t('common.createSuccess'));
          quickCreateCancel();
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          quickCreateLoading.value = false;
        }
      }
    });
  }
  // TODO: 快捷创建先不上
  function handleSelect(value: string | number | Record<string, any> | undefined) {
    showQuickCreateForm.value = true;
    createType.value = value as keyof typeof testPlanTypeEnum;
  }

  // 查看已归档测试计划以及计划组
  function archivedChangeHandler() {
    resetFilterParams();
    fetchData();
  }

  defineExpose({
    fetchData,
    emitTableParams,
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
  .popover-label-td {
    @apply flex items-center;

    padding: 8px 8px 0 0;
    color: var(--color-text-4);
  }
  .popover-value-td {
    @apply text-right font-medium;

    padding-top: 8px;
    color: var(--color-text-1);
  }
  :deep(.parent-tr) {
    .arco-table-drag-handle {
      pointer-events: none;
      .arco-table-cell {
        svg {
          color: transparent;
        }
      }
    }
  }
</style>
