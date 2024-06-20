<template>
  <div class="p-[16px]">
    <MsAdvanceFilter
      v-model:keyword="keyword"
      :filter-config-list="[]"
      :custom-fields-config-list="[]"
      :row-count="0"
      :count="props.modulesCount[props.activeModule] || 0"
      :name="moduleNamePath"
      :search-placeholder="t('ms.case.associate.searchPlaceholder')"
      @keyword-search="loadCaseList()"
      @adv-search="loadCaseList()"
      @refresh="loadCaseList()"
    />
    <MsBaseTable
      ref="tableRef"
      class="mt-[16px]"
      v-bind="propsRes"
      :action-config="batchActions"
      :selectable="hasOperationPermission"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
      @drag-change="handleDragChange"
      @selected-change="handleTableSelect"
      @filter-change="getModuleCount"
      @module-change="loadCaseList(false)"
    >
      <template #num="{ record }">
        <MsButton type="text" @click="toCaseDetail(record)">{{ record.num }}</MsButton>
      </template>
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
        <CaseLevel :case-level="filterContent.value" />
      </template>
      <template #caseLevel="{ record }">
        <CaseLevel :case-level="record.caseLevel" />
      </template>
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
        <ExecuteResult :execute-result="filterContent.key" />
      </template>
      <template #lastExecResult="{ record }">
        <a-select
          v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE']) && props.canEdit"
          v-model:model-value="record.lastExecResult"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full"
          @change="() => handleEditLastExecResult(record)"
        >
          <template #label>
            <span class="text-[var(--color-text-2)]"><ExecuteResult :execute-result="record.lastExecResult" /></span>
          </template>
          <a-option v-for="item in Object.values(executionResultMap)" :key="item.key" :value="item.key">
            <ExecuteResult :execute-result="item.key" />
          </a-option>
        </a-select>
        <span v-else class="text-[var(--color-text-2)]"><ExecuteResult :execute-result="record.lastExecResult" /></span>
      </template>
      <template #bugCount="{ record }">
        <BugCountPopover :case-item="record" :can-edit="props.canEdit" @load-list="loadList" />
      </template>
      <template v-if="props.canEdit" #operation="{ record }">
        <MsButton
          v-permission="['PROJECT_TEST_PLAN:READ+EXECUTE']"
          type="text"
          class="!mr-0"
          @click="toCaseDetail(record)"
        >
          {{ t('common.execute') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']" direction="vertical" :margin="8"></a-divider>
        <MsPopconfirm
          :title="t('testPlan.featureCase.disassociateTip', { name: characterLimit(record.name) })"
          :sub-title-tip="t('testPlan.featureCase.disassociateTipContent')"
          :ok-text="t('common.confirm')"
          :loading="disassociateLoading"
          type="error"
          @confirm="(val, done) => handleDisassociateCase(record, done)"
        >
          <MsButton v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']" type="text" class="!mr-0">
            {{ t('common.cancelLink') }}
          </MsButton>
        </MsPopconfirm>
      </template>
    </MsBaseTable>
    <!-- 批量执行 -->
    <a-modal
      v-model:visible="batchExecuteModalVisible"
      title-align="start"
      body-class="p-0"
      :width="800"
      :cancel-button-props="{ disabled: batchLoading }"
      :ok-loading="batchLoading"
      :ok-text="t('caseManagement.caseReview.commitResult')"
      @before-ok="handleBatchExecute"
      @close="resetBatchForm"
    >
      <template #title>
        {{ t('testPlan.testPlanIndex.batchExecution') }}
        <div class="text-[var(--color-text-4)]">
          {{
            t('testPlan.testPlanIndex.selectedCount', {
              count: batchParams.currentSelectCount || tableSelected.length,
            })
          }}
        </div>
      </template>
      <ExecuteForm v-model:form="batchExecuteForm" />
    </a-modal>
    <!-- 批量修改执行人 -->
    <BatchUpdateExecutorModal
      v-model:visible="batchUpdateExecutorModalVisible"
      :count="batchParams.currentSelectCount || tableSelected.length"
      :params="batchUpdateParams"
      :batch-update-executor="batchUpdateCaseExecutor"
      @load-list="resetSelectorAndCaseList"
    />
    <!-- 批量移动 -->
    <BatchApiMoveModal
      v-model:visible="batchMoveModalVisible"
      :module-tree="props.moduleTree"
      :count="batchParams.currentSelectCount || tableSelected.length"
      :params="batchUpdateParams"
      :batch-move="batchMoveFeatureCase"
      @load-list="resetCaseList"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsPopconfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type {
    BatchActionParams,
    BatchActionQueryParams,
    MsTableColumn,
    MsTableProps,
  } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import BugCountPopover from './bugCountPopover.vue';
  import BatchApiMoveModal from '@/views/test-plan/testPlan/components/batchApiMoveModal.vue';
  import BatchUpdateExecutorModal from '@/views/test-plan/testPlan/components/batchUpdateExecutorModal.vue';
  import ExecuteForm from '@/views/test-plan/testPlan/detail/featureCase/components/executeForm.vue';

  import {
    batchDisassociateCase,
    batchExecuteCase,
    batchMoveFeatureCase,
    batchUpdateCaseExecutor,
    disassociateCase,
    getPlanDetailFeatureCaseList,
    runFeatureCase,
    sortFeatureCase,
  } from '@/api/modules/test-plan/testPlan';
  import { defaultExecuteForm } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { DragSortParams, ModuleTreeNode } from '@/models/common';
  import type {
    ExecuteFeatureCaseFormParams,
    PlanDetailFeatureCaseItem,
    PlanDetailFeatureCaseListQueryParams,
  } from '@/models/testPlan/testPlan';
  import { LastExecuteResults } from '@/enums/caseEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';
  import { executionResultMap, getCaseLevels } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    modulesCount: Record<string, number>; // 模块数量统计对象
    moduleName: string;
    moduleParentId: string;
    activeModule: string;
    offspringIds: string[];
    planId: string;
    moduleTree: ModuleTreeNode[];
    canEdit: boolean;
    treeType: 'MODULE' | 'COLLECTION';
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: PlanDetailFeatureCaseListQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
  }>();

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const tableStore = useTableStore();
  const { openModal } = useModal();

  const keyword = ref('');
  const moduleNamePath = computed(() => {
    return props.activeModule === 'all' ? t('caseManagement.featureCase.allCase') : props.moduleName;
  });

  const hasOperationPermission = computed(
    () => hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN:READ+ASSOCIATION']) && props.canEdit
  );
  const columns = computed<MsTableColumn>(() => [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      width: 100,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      columnSelectorDisabled: true,
    },
    {
      title: 'ms.minders.testSet',
      dataIndex: 'testPlanCollectionName',
      width: 150,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'caseLevel',
      slotName: 'caseLevel',
      filterConfig: {
        options: casePriorityOptions,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'lastExecResult',
      slotName: 'lastExecResult',
      filterConfig: {
        valueKey: 'key',
        labelKey: 'statusText',
        options: Object.values(executionResultMap),
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'common.belongProject',
      dataIndex: 'projectName',
      showTooltip: true,
      showInTable: false,
      showDrag: true,
      width: 150,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      slotName: 'bugCount',
      width: 100,
      showDrag: true,
    },
    {
      title: 'case.tableColumnCreateUser',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUserName',
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 200 : 50,
    },
  ]);

  const tableProps = ref<Partial<MsTableProps<PlanDetailFeatureCaseItem>>>({
    scroll: { x: '100%' },
    tableKey: TableKeyEnum.TEST_PLAN_DETAIL_FEATURE_CASE_TABLE,
    showSetting: true,
    heightUsed: 460,
    showSubdirectory: true,
    draggable: { type: 'handle' },
    draggableCondition: true,
  });

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, getTableQueryParams } = useTable(
    getPlanDetailFeatureCaseList,
    tableProps.value,
    (record) => {
      return {
        ...record,
        lastExecResult: record.lastExecResult ?? LastExecuteResults.PENDING,
        caseLevel: getCaseLevels(record.customFields),
      };
    }
  );

  const batchActions = computed(() => {
    return {
      baseAction: [
        {
          label: 'common.execute',
          eventTag: 'execute',
          permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
        },
        {
          label: 'testPlan.featureCase.changeExecutor',
          eventTag: 'changeExecutor',
          permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
        },
        ...(props.treeType === 'COLLECTION'
          ? [{ label: 'common.move', eventTag: 'move', permission: ['PROJECT_TEST_PLAN:READ+UPDATE'] }]
          : []),
        {
          label: 'common.cancelLink',
          eventTag: 'disassociate',
          permission: ['PROJECT_TEST_PLAN:READ+ASSOCIATION'],
        },
      ],
    };
  });

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  watch(
    () => hasOperationPermission.value,
    () => {
      tableRef.value?.initColumn(columns.value);
    }
  );

  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all') {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.TEST_PLAN_DETAIL_FEATURE_CASE_TABLE);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return moduleIds;
  }
  const collectionId = computed(() => (props.activeModule === 'all' ? '' : props.activeModule));
  async function getTableParams(isBatch: boolean) {
    const selectModules = await getModuleIds();
    const commonParams = {
      testPlanId: props.planId,
      ...(props.treeType === 'COLLECTION' ? { collectionId: collectionId.value } : { moduleIds: selectModules }),
    };
    if (isBatch) {
      return {
        condition: {
          keyword: keyword.value,
          filter: propsRes.value.filter,
        },
        projectId: props.activeModule !== 'all' && props.treeType === 'MODULE' ? props.moduleParentId : '',
        ...commonParams,
      };
    }
    return {
      keyword: keyword.value,
      filter: propsRes.value.filter,
      treeType: props.treeType,
      ...commonParams,
    };
  }

  watch(
    [() => props.canEdit, () => props.treeType, () => collectionId.value.length],
    () => {
      tableProps.value.draggableCondition =
        hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) &&
        props.canEdit &&
        props.treeType === 'COLLECTION' &&
        !!collectionId.value.length;
    },
    {
      immediate: true,
    }
  );

  async function loadCaseList(refreshTreeCount = true) {
    const tableParams = await getTableParams(false);
    setLoadListParams({
      ...tableParams,
      projectId: props.activeModule !== 'all' && props.treeType === 'MODULE' ? props.moduleParentId : '',
    });
    loadList();
    if (refreshTreeCount) {
      emit('getModuleCount', {
        ...tableParams,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
      });
    }
  }
  watch(
    () => props.activeModule,
    () => {
      loadCaseList();
    }
  );

  async function getModuleCount() {
    const tableParams = await getTableParams(false);
    emit('getModuleCount', {
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }

  const tableSelected = ref<(string | number)[]>([]); // 表格选中的
  const batchParams = ref<BatchActionQueryParams>({
    selectIds: [],
    selectAll: false,
    excludeIds: [],
    condition: {},
    currentSelectCount: 0,
  });
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  function resetCaseList() {
    resetSelector();
    getModuleCount();
    loadList();
  }

  function resetSelectorAndCaseList() {
    resetSelector();
    loadList();
  }

  // 拖拽排序
  async function handleDragChange(params: DragSortParams) {
    try {
      await sortFeatureCase({ ...params, testCollectionId: collectionId.value });
      Message.success(t('caseManagement.featureCase.sortSuccess'));
      loadCaseList(false);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 更新执行结果
  async function handleEditLastExecResult(record: PlanDetailFeatureCaseItem) {
    try {
      await runFeatureCase({
        id: record.id,
        projectId: appStore.currentProjectId,
        testPlanId: props.planId,
        caseId: record.caseId,
        lastExecResult: record.lastExecResult,
      });
      Message.success(t('common.updateSuccess'));
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 取消关联
  const disassociateLoading = ref(false);
  async function handleDisassociateCase(record: PlanDetailFeatureCaseItem, done?: () => void) {
    try {
      disassociateLoading.value = true;
      await disassociateCase({ testPlanId: props.planId, id: record.id });
      if (done) {
        done();
      }
      Message.success(t('common.unLinkSuccess'));
      resetCaseList();
      emit('initModules');
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      disassociateLoading.value = false;
    }
  }

  // 批量取消关联用例
  function handleBatchDisassociateCase() {
    openModal({
      type: 'warning',
      title: t('caseManagement.caseReview.disassociateConfirmTitle', {
        count: batchParams.value.currentSelectCount || tableSelected.value.length,
      }),
      content: t('testPlan.featureCase.batchDisassociateTipContent'),
      okText: t('common.cancelLink'),
      cancelText: t('common.cancel'),
      onBeforeOk: async () => {
        try {
          const tableParams = await getTableParams(true);
          await batchDisassociateCase({
            selectIds: tableSelected.value as string[],
            selectAll: batchParams.value.selectAll,
            excludeIds: batchParams.value?.excludeIds || [],
            ...tableParams,
          });
          Message.success(t('common.updateSuccess'));
          resetCaseList();
          emit('initModules');
          emit('refresh');
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 批量执行
  const batchLoading = ref(false);
  const batchExecuteModalVisible = ref(false);
  const batchExecuteForm = ref<ExecuteFeatureCaseFormParams>({ ...defaultExecuteForm });
  async function handleBatchExecute() {
    try {
      batchLoading.value = true;
      const tableParams = await getTableParams(true);
      await batchExecuteCase({
        ...tableParams,
        ...batchExecuteForm.value,
        notifier: batchExecuteForm.value?.commentIds?.join(';'),
        selectIds: tableSelected.value as string[],
        selectAll: batchParams.value.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
      });
      Message.success(t('common.updateSuccess'));
      resetSelector();
      loadList();
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchLoading.value = false;
    }
  }

  function resetBatchForm() {
    batchExecuteForm.value = { ...defaultExecuteForm };
  }

  // 批量修改执行人 和 批量移动
  const batchUpdateParams = ref();
  const batchUpdateExecutorModalVisible = ref(false);
  const batchMoveModalVisible = ref(false);

  // 处理表格选中后批量操作
  async function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = { ...params, selectIds: params?.selectedIds };
    const tableParams = await getTableParams(true);
    batchUpdateParams.value = {
      selectIds: tableSelected.value as string[],
      selectAll: batchParams.value.selectAll,
      excludeIds: batchParams.value?.excludeIds || [],
      ...tableParams,
    };
    switch (event.eventTag) {
      case 'execute':
        batchExecuteModalVisible.value = true;
        break;
      case 'disassociate':
        handleBatchDisassociateCase();
        break;
      case 'changeExecutor':
        batchUpdateExecutorModalVisible.value = true;
        break;
      case 'move':
        batchMoveModalVisible.value = true;
        break;
      default:
        break;
    }
  }

  // 去用例详情页面
  function toCaseDetail(record: PlanDetailFeatureCaseItem) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL_FEATURE_CASE_DETAIL,
      query: {
        ...route.query,
        caseId: record.caseId,
        testPlanCaseId: record.id,
        canEdit: String(props.canEdit),
      },
      state: {
        params: JSON.stringify(getTableQueryParams()),
      },
    });
  }

  onBeforeMount(() => {
    loadCaseList();
  });

  defineExpose({
    resetSelector,
    loadCaseList,
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_DETAIL_FEATURE_CASE_TABLE, columns.value, 'drawer', true);
</script>

<style lang="less" scoped>
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
        color: var(--color-text-brand);
      }
    }
  }
</style>
